#!/usr/bin/env python3
"""Bounded hostapd diagnostics and conservative AP recovery; Python 3.9+.

Install on the Pi with sudo and two --target IPv4 addresses of always-on
hotspot clients. All installation-specific values stay in /etc, not source.
No API/MQTT credentials, application data, interface resets or uplink changes.
"""
import argparse
from collections import deque
from datetime import datetime, timezone
import fcntl
import gzip
import ipaddress
import json
import math
import os
from pathlib import Path
import re
import select
import shutil
import signal
import socket
import subprocess
import sys
import tempfile
import time

VERSION = "1.0"
CONFIG = Path("/etc/ella-ap-watch.json")
RUNTIME = Path("/run/ella-ap-watch")
ARCHIVE = Path("/var/lib/ella-ap-watch")
PROGRAM = Path("/usr/local/sbin/ella-ap-watch")
SYSTEMD = Path("/etc/systemd/system")
SERVICE = SYSTEMD / "ella-ap-watch.service"
MARKER = "# Managed by Ella AP watch\n"
STOP = False


def run(argv, timeout=5, limit=65536):
    """Bound both run time and captured bytes. Never invoke a shell."""
    try:
        child = subprocess.Popen(argv, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                                 start_new_session=True)
    except OSError as error:
        return {"code": -1, "text": str(error), "limited": False}
    data = bytearray()
    deadline = time.monotonic() + timeout
    limited = False
    try:
        while True:
            remaining = deadline - time.monotonic()
            if remaining <= 0:
                limited = True
                break
            if not select.select([child.stdout], [], [], remaining)[0]:
                limited = True
                break
            block = os.read(child.stdout.fileno(), min(8192, limit + 1 - len(data)))
            if not block:
                break
            data.extend(block)
            if len(data) > limit:
                limited = True
                break
    finally:
        if limited:
            try:
                os.killpg(child.pid, signal.SIGKILL)
            except ProcessLookupError:
                pass
        try:
            code = child.wait(timeout=1)
        except subprocess.TimeoutExpired:
            os.killpg(child.pid, signal.SIGKILL)
            code = child.wait()
            limited = True
        child.stdout.close()
    return {"code": code if not limited else -2,
            "text": bytes(data[:limit]).decode("utf-8", "replace"), "limited": limited}


def mkdir(path):
    path.mkdir(parents=True, exist_ok=True, mode=0o700)
    if path.is_symlink() or path.stat().st_uid != 0:
        raise ValueError("Unsafe directory: " + str(path))
    path.chmod(0o700)


def write_json(path, value):
    # /etc is a shared system directory: never chmod it as a private directory.
    path.parent.mkdir(parents=True, exist_ok=True)
    if path.parent.is_symlink() or path.parent.stat().st_uid != 0:
        raise ValueError("Unsafe parent directory")
    fd, temporary = tempfile.mkstemp(prefix=".write-", dir=path.parent)
    try:
        with os.fdopen(fd, "w") as output:
            json.dump(value, output, allow_nan=False)
            output.write("\n")
        os.replace(temporary, path)
    finally:
        Path(temporary).unlink(missing_ok=True)


def stamp():
    return datetime.now(timezone.utc).isoformat(timespec="milliseconds")


def ring(event):
    mkdir(RUNTIME)
    path = RUNTIME / "events.jsonl"
    record = {"utc": stamp(), "uptime": round(time.monotonic(), 3), **event}
    line = json.dumps(record, ensure_ascii=True)
    if len(line) > 8190:
        line = json.dumps({"utc": record["utc"], "type": "oversized-record",
                           "text": line[:4096], "truncated": True})
    line += "\n"
    if path.exists() and path.stat().st_size + len(line.encode()) > 2 * 1024 * 1024:
        os.replace(path, RUNTIME / "events.previous.jsonl")
    fd = os.open(path, os.O_WRONLY | os.O_APPEND | os.O_CREAT | os.O_NOFOLLOW, 0o600)
    with os.fdopen(fd, "a") as output:
        output.write(line)


def tail(path, limit):
    try:
        with path.open("rb") as source:
            source.seek(max(0, path.stat().st_size - limit))
            return source.read(limit).decode("utf-8", "replace")
    except OSError:
        return ""


def read_json(path):
    if path.stat().st_size > 65536:
        raise ValueError("Oversized JSON file")
    with path.open() as source:
        return json.load(source)


def config():
    return validate_config(read_json(CONFIG))


def validate_config(c):
    if c.get("version") != 1 or c.get("mode") not in ("recover", "observe"):
        raise ValueError("Invalid watch configuration")
    if not re.fullmatch(r"[a-zA-Z0-9_-]{1,15}", c.get("interface", "")):
        raise ValueError("Invalid interface")
    if not re.fullmatch(r"[a-zA-Z0-9_.@-]+\.service", c.get("unit", "")):
        raise ValueError("Invalid hostapd service")
    if not str(c.get("control", "")).startswith("/run/"):
        raise ValueError("Control directory must be under /run")
    targets = c.get("targets", [])
    if not 2 <= len(targets) <= 4:
        raise ValueError("Configure two to four always-on clients")
    for target in targets:
        ipaddress.IPv4Address(target["ip"])
        if not re.fullmatch(r"(?:[0-9a-f]{2}:){5}[0-9a-f]{2}", target["mac"]):
            raise ValueError("Invalid target MAC")
    if len({t["ip"] for t in targets}) != len(targets) or len({t["mac"] for t in targets}) != len(targets):
        raise ValueError("Targets must identify different clients")
    return c


class Control:
    """Hostapd's local datagram control protocol, with separate event socket."""
    def __init__(self, c):
        mkdir(RUNTIME)
        fd, name = tempfile.mkstemp(prefix="ctrl-", dir=RUNTIME)
        os.close(fd)
        self.path = Path(name)
        self.path.unlink()
        self.sock = socket.socket(socket.AF_UNIX, socket.SOCK_DGRAM)
        self.sock.settimeout(2)
        try:
            self.sock.bind(name)
            self.sock.connect(str(Path(c["control"]) / c["interface"]))
        except BaseException:
            self.close()
            raise

    def request(self, command):
        self.sock.send(command.encode("ascii"))
        end = time.monotonic() + 2
        while time.monotonic() < end:
            self.sock.settimeout(max(0.01, end - time.monotonic()))
            data = self.sock.recv(16384).decode("utf-8", "replace")
            if not data.startswith("<"):
                return data.strip()
        raise TimeoutError("hostapd control reply timed out")

    def close(self):
        self.sock.close()
        self.path.unlink(missing_ok=True)

    def __enter__(self):
        return self

    def __exit__(self, *unused):
        self.close()


def control(c, command):
    try:
        with Control(c) as connection:
            return connection.request(command)
    except (OSError, TimeoutError) as error:
        return "ERROR: " + str(error)


class Events:
    def __init__(self, c):
        self.c = c
        self.connection = None

    def connect(self):
        self.close()
        try:
            self.connection = Control(self.c)
            for command in ("ATTACH", "LEVEL 2"):
                if self.connection.request(command) != "OK":
                    raise OSError("Event attach failed")
        except (OSError, TimeoutError):
            self.close()

    def poll(self, seconds):
        if self.connection is None:
            time.sleep(seconds)
            return
        try:
            if select.select([self.connection.sock], [], [], seconds)[0]:
                # A per-second event cap prevents a noisy radio monopolising CPU.
                started = time.monotonic()
                for _ in range(64):
                    self.connection.sock.settimeout(0)
                    try:
                        data = self.connection.sock.recv(8192)
                    except BlockingIOError:
                        break
                    ring({"type": "hostapd-event", "text": data.decode("utf-8", "replace")[:4096]})
                else:
                    time.sleep(max(0, 1 - (time.monotonic() - started)))
        except OSError:
            self.close()

    def close(self):
        if self.connection:
            self.connection.close()
        self.connection = None

    def alive(self):
        if self.connection is None:
            return False
        try:
            return self.connection.request("PING") == "PONG"
        except (OSError, TimeoutError):
            return False


def pairs(text):
    return dict(line.split("=", 1) for line in text.splitlines() if "=" in line)


def stations(text):
    result = {}
    current = None
    for line in text.splitlines():
        match = re.match(r"Station ([0-9a-fA-F:]{17}) ", line)
        if match:
            current = match[1].lower()
            result[current] = {}
        elif current:
            match = re.match(r"\s*(rx bytes|rx packets|tx failed|connected time):\s*(\d+)", line)
            if match:
                result[current][match[1]] = int(match[2])
    return result


def ip_json(arguments):
    r = run(["ip", "-j", "-4", *arguments])
    if r["code"]:
        raise ValueError("Cannot read local addressing/routing")
    return json.loads(r["text"])


def observe(c, previous):
    iface = c["interface"]
    state = control(c, "STATUS")
    enabled = pairs(state).get("state") == "ENABLED"
    active = run(["systemctl", "is-active", c["unit"]])["text"].strip()
    dump = run(["iw", "dev", iface, "station", "dump"])
    stas = stations(dump["text"]) if dump["code"] == 0 else {}
    observations = {}
    counters = {}
    for target in c["targets"]:
        mac = target["mac"]
        counter = stas.get(mac, {})
        counters[mac] = counter
        old = previous.get(mac, {})
        # Counter resets/reassociation are not incoming-traffic evidence.
        progressed = any(key in counter and key in old and counter[key] > old[key]
                         for key in ("rx bytes", "rx packets"))
        try:
            route = ip_json(["route", "get", target["ip"]])
            routed = bool(route and route[0].get("dev") == iface and not route[0].get("gateway"))
        except (ValueError, KeyError, TypeError):
            routed = False
        probe = None
        if routed and not progressed:
            probe = run(["ping", "-n", "-I", iface, "-c", "1", "-W", "2", target["ip"]], timeout=3)["code"] == 0
        observations[mac] = {"ip": target["ip"], "rx_progress": progressed,
                             "probe": probe, "route_ok": routed,
                             "alive": progressed or probe is True}
    return {"utc": stamp(), "uptime": time.monotonic(), "active": active,
            "enabled": enabled, "hostapd": state, "stations": stas,
            "station_query_ok": dump["code"] == 0, "targets": observations}, counters


class Policy:
    """One recovery per unresolved incident; max two in any thirty minutes."""
    def __init__(self, now, saved=None):
        self.started = now
        self.seen = set()
        self.last_good = now
        self.control_bad = None
        self.attempts = []
        self.latched = False
        self.stable_since = None
        self.inhibited = False
        if saved is not None:
            try:
                attempts = saved["attempts"]
                if not isinstance(attempts, list) or len(attempts) > 100:
                    raise ValueError()
                if not all(type(t) in (int, float) and math.isfinite(t) and 0 <= t <= now for t in attempts):
                    raise ValueError()
                if type(saved["latched"]) is not bool:
                    raise ValueError()
                self.attempts = attempts
                self.latched = saved["latched"]
            except (KeyError, TypeError, ValueError):
                self.inhibited = True

    def saved(self):
        return {"attempts": self.attempts, "latched": self.latched}

    def decide(self, observation, mode):
        now = observation["uptime"]
        targets = observation["targets"]
        self.attempts = [t for t in self.attempts if now - t < 1800]
        alive = [m for m, t in targets.items() if t["alive"]]
        self.seen.update(alive)
        if alive:
            self.last_good = now
        if observation["enabled"]:
            self.control_bad = None
        elif self.control_bad is None:
            self.control_bad = now
        if alive and observation["enabled"]:
            if self.stable_since is None:
                self.stable_since = now
            if now - self.stable_since >= 90:
                self.latched = False
        else:
            self.stable_since = None
        if observation["active"] != "active":
            self.last_good = now
            self.control_bad = None
            return "paused-hotspot-not-active", False
        if self.inhibited:
            return "recovery-inhibited-invalid-state-use-reset", False
        if now - self.started < 120:
            return "startup-grace", False
        if alive:
            return "traffic-ok" if observation["enabled"] else "traffic-ok-control-warning", False
        if not all(t["route_ok"] for t in targets.values()):
            self.last_good = now
            return "route-problem-no-radio-restart", False
        control_stall = self.control_bad is not None and now - self.control_bad >= 90
        armed = set(targets).issubset(self.seen)
        traffic_stall = armed and now - self.last_good >= 90
        if not control_stall and not traffic_stall:
            return "waiting-for-client-baseline" if not armed else "suspect-no-client-traffic", False
        reason = "hostapd-control-stall" if control_stall else "all-target-traffic-stalled"
        if self.latched:
            return "unresolved-after-recovery-manual-check-needed", False
        if len(self.attempts) >= 2 or (self.attempts and now - self.attempts[-1] < 600):
            return "recovery-rate-limited", False
        return reason, mode == "recover"

    def recovering(self, now):
        self.attempts.append(now)
        self.latched = True
        self.stable_since = None
        self.last_good = now
        self.control_bad = None


def snapshot(c, reason, observation):
    """Only incidents/manual captures reach the card; at most five gzip files."""
    mkdir(ARCHIVE)
    commands = {
        "radio": ["iw", "dev", c["interface"], "info"],
        "stations": ["iw", "dev", c["interface"], "station", "dump"],
        "power_save": ["iw", "dev", c["interface"], "get", "power_save"],
        "driver": ["ethtool", "-i", c["interface"]],
        "addresses": ["ip", "-4", "-brief", "address"],
        "routes": ["ip", "-4", "route"],
        "neighbours": ["ip", "neigh", "show", "dev", c["interface"]],
        "service": ["systemctl", "show", c["unit"], "-p", "ActiveState,SubState,MainPID,NRestarts,ExecMainStatus"],
        "kernel": ["journalctl", "-k", "-b", "--since", "-15min", "-n", "500", "--no-pager", "-o", "short-iso"],
        "hotspot_log": ["journalctl", "-b", "-u", c["unit"], "--since", "-15min", "-n", "500", "--no-pager", "-o", "short-iso"],
        "network_log": ["journalctl", "-b", "-u", "NetworkManager", "-u", "ella-hotspot-dns.service", "--since", "-15min", "-n", "250", "--no-pager", "-o", "short-iso"],
    }
    data = {"version": VERSION, "utc": stamp(), "uptime": time.monotonic(),
            "reason": reason, "observation": observation,
            "events": tail(RUNTIME / "events.previous.jsonl", 65536) + tail(RUNTIME / "events.jsonl", 196608),
            "diagnostics": {name: run(command, timeout=5, limit=65536)
                            for name, command in commands.items()}}
    # Reserved name is atomic; interruption leaves only an ignored .partial file.
    for old in ARCHIVE.glob("*.partial"):
        old.unlink()
    while len(list(ARCHIVE.glob("incident-*.json.gz"))) >= 5:
        min(ARCHIVE.glob("incident-*.json.gz"), key=lambda p: p.stat().st_mtime_ns).unlink()
    destination = ARCHIVE / ("incident-" + str(time.time_ns()) + ".json.gz")
    temporary = destination.with_suffix(".partial")
    payload = json.dumps(data, ensure_ascii=True, allow_nan=False).encode()
    if len(payload) > 2 * 1024 * 1024:
        raise ValueError("Diagnostic snapshot exceeded size bound")
    fd = os.open(temporary, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
    with os.fdopen(fd, "wb") as output:
        with gzip.GzipFile(fileobj=output, mode="wb", compresslevel=1) as zipped:
            zipped.write(payload)
        output.flush()
        os.fsync(output.fileno())
    os.replace(temporary, destination)
    return str(destination)


def boot_id():
    return Path("/proc/sys/kernel/random/boot_id").read_text().strip()


def on_start(c):
    power = run(["iw", "dev", c["interface"], "set", "power_save", "off"])
    deadline = time.monotonic() + 10
    answer = control(c, "LOG_LEVEL DEBUG")
    while answer != "OK" and time.monotonic() < deadline and not STOP:
        time.sleep(0.25)
        answer = control(c, "LOG_LEVEL DEBUG")
    ring({"type": "diagnostic-setup", "power_save_off": power["code"] == 0,
          "hostapd_debug": answer == "OK"})
    if power["code"] or answer != "OK":
        print("AP watch: diagnostic setup incomplete; see events/status.", flush=True)


def daemon():
    mkdir(RUNTIME)
    lock = (RUNTIME / "lock").open("a")
    fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
    c = config()
    identity = boot_id()
    previous_state = None
    try:
        saved = read_json(RUNTIME / "state.json")
        if not isinstance(saved, dict) or not re.fullmatch(r"[0-9a-f-]{36}", str(saved.get("boot", ""))):
            raise ValueError("Invalid runtime identity")
        if saved.get("boot") == identity:
            previous_state = saved.get("policy") if isinstance(saved.get("policy"), dict) else {}
    except FileNotFoundError:
        pass
    except (ValueError, TypeError, AttributeError, OSError):
        previous_state = {}  # Fail closed rather than forget restart limits.
    policy = Policy(time.monotonic(), previous_state)
    tap = Events(c)
    on_start(c)
    counters = {}
    old_reason = None
    while not STOP:
        started = time.monotonic()
        current_config = config()
        if any(current_config[k] != c[k] for k in ("interface", "unit", "control", "targets")):
            raise ValueError("Target/interface configuration changed; restart watch service")
        c = current_config
        # Reattach after a hostapd restart without dropping the monitor process.
        if not tap.alive():
            tap.connect()
        o, counters = observe(c, counters)
        reason, restart = policy.decide(o, c["mode"])
        event = {"type": "sample", "reason": reason, "mode": c["mode"],
                 "enabled": o["enabled"], "targets": o["targets"], "stations": o["stations"]}
        ring(event)
        saved = {"boot": identity, "policy": policy.saved(), "reason": reason,
                 "mode": c["mode"], "observation": o}
        write_json(RUNTIME / "state.json", saved)
        if reason != old_reason:
            print("AP watch: " + reason + " (" + c["mode"] + ")", flush=True)
            old_reason = reason
        if restart:
            policy.recovering(time.monotonic())
            saved["policy"] = policy.saved()
            # Persist the limiter in RAM before snapshot/restart, including errors.
            write_json(RUNTIME / "state.json", saved)
            try:
                path = snapshot(c, reason, o)
                print("AP watch: diagnostics saved to " + path, flush=True)
            except Exception as error:
                ring({"type": "snapshot-error", "error": str(error)})
                print("AP watch: incident archive failed; RAM evidence retained.", flush=True)
            ring({"type": "recovery-start", "reason": reason})
            result = run(["systemctl", "restart", c["unit"]], timeout=35)
            ring({"type": "recovery-result", **result})
            print("AP watch: hotspot restart " + ("completed" if result["code"] == 0 else "failed/timed out") +
                  "; awaiting real client traffic.", flush=True)
            tap.close()
            counters = {}
            # ExecStartPost applies diagnostics again; no collector operation.
        end = max(started + 30, time.monotonic() + 1)
        while not STOP and time.monotonic() < end:
            tap.poll(min(1, end - time.monotonic()))
    tap.close()


def managed_write(path, text):
    if path.exists() and not path.read_text().startswith(MARKER):
        raise ValueError("Refusing to replace an unmanaged file: " + str(path))
    path.parent.mkdir(parents=True, exist_ok=True)
    fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_TRUNC | os.O_NOFOLLOW, 0o644)
    with os.fdopen(fd, "w") as output:
        output.write(MARKER + text)


def install(args):
    for binary in ("ip", "iw", "ping", "systemctl", "journalctl", "systemd-analyze"):
        if not shutil.which(binary):
            raise ValueError("Required command missing: " + binary)
    existing = config() if CONFIG.exists() else None
    targets = []
    if args.target:
        leases = {}
        try:
            for line in Path(args.leases).read_text().splitlines():
                fields = line.split()
                if len(fields) >= 3:
                    leases[fields[2]] = fields[1].lower()
        except OSError:
            pass
        neighbours = ip_json(["neigh", "show", "dev", args.interface])
        for row in neighbours:
            if row.get("lladdr"):
                leases.setdefault(row.get("dst"), row["lladdr"].lower())
        local = ip_json(["address", "show", "dev", args.interface])
        networks = [ipaddress.IPv4Interface(str(a["local"]) + "/" + str(a["prefixlen"])).network
                    for row in local for a in row.get("addr_info", []) if a.get("family") == "inet"]
        for value in args.target:
            address = ipaddress.IPv4Address(value)
            if not any(address in network for network in networks):
                raise ValueError("Target must be on the AP's directly connected IPv4 subnet")
            if value not in leases:
                raise ValueError("No lease/neighbour MAC found for target " + value)
            targets.append({"ip": value, "mac": leases[value]})
    elif existing:
        targets = existing["targets"]
    c = {"version": 1, "interface": args.interface, "unit": args.unit,
         "control": args.control, "targets": targets,
         "mode": existing["mode"] if existing else "recover"}
    if not existing:
        c["original_power"] = run(["iw", "dev", c["interface"], "get", "power_save"])["text"].strip()
        c["original_log_level"] = control(c, "LOG_LEVEL")
    else:
        c["original_power"] = existing.get("original_power", "")
        c["original_log_level"] = existing.get("original_log_level", "")
    # Validate before any service/config mutation, using the same rules as runtime.
    if not 2 <= len(targets) <= 4 or len({t["mac"] for t in targets}) != len(targets):
        raise ValueError("Supply two to four distinct always-on client addresses")
    if not re.fullmatch(r"[a-zA-Z0-9_-]{1,15}", c["interface"]) or not re.fullmatch(r"[a-zA-Z0-9_.@-]+\.service", c["unit"]):
        raise ValueError("Invalid interface/service name")
    if not c["control"].startswith("/run/") or "\n" in c["control"]:
        raise ValueError("Invalid control directory")
    validate_config(c)
    if run(["systemctl", "is-active", c["unit"]])["text"].strip() != "active" or pairs(control(c, "STATUS")).get("state") != "ENABLED":
        raise ValueError("Install while the intended hostapd hotspot is active and responsive")
    dropin = SYSTEMD / (c["unit"] + ".d") / "70-ap-watch.conf"
    for path in (SERVICE, dropin):
        if path.exists() and not path.read_text().startswith(MARKER):
            raise ValueError("Existing unmanaged service/drop-in needs review: " + str(path))
    mkdir(RUNTIME)
    mkdir(ARCHIVE)
    # Configuration remains root-only; source never contains actual targets.
    write_json(CONFIG, c)
    config()
    if Path(__file__).resolve() != PROGRAM:
        fd = os.open(PROGRAM, os.O_WRONLY | os.O_CREAT | os.O_TRUNC | os.O_NOFOLLOW, 0o700)
        with os.fdopen(fd, "w") as output:
            output.write(Path(__file__).read_text())
        PROGRAM.chmod(0o700)
    managed_write(dropin, "[Service]\nExecStartPost=-/usr/bin/python3 /usr/local/sbin/ella-ap-watch _on-start\n")
    managed_write(SERVICE, """[Unit]
Description=Bounded hotspot diagnostics and recovery
After=HOTSPOT_UNIT

[Service]
Type=simple
ExecStart=/usr/bin/python3 /usr/local/sbin/ella-ap-watch daemon
Restart=on-failure
RestartSec=10
RuntimeDirectory=ella-ap-watch
RuntimeDirectoryMode=0700
RuntimeDirectoryPreserve=yes
StateDirectory=ella-ap-watch
StateDirectoryMode=0700
UMask=0077
Nice=10
IOSchedulingClass=idle
MemoryMax=64M
OOMScoreAdjust=500
NoNewPrivileges=yes
PrivateTmp=yes
ProtectHome=yes
ProtectSystem=strict
ReadWritePaths=/run/ella-ap-watch /var/lib/ella-ap-watch
TimeoutStopSec=10

[Install]
WantedBy=multi-user.target
""".replace("HOTSPOT_UNIT", c["unit"]))
    result = run(["systemd-analyze", "verify", str(SERVICE)], timeout=15)
    if result["code"]:
        raise ValueError("Unit verification failed: " + result["text"][:1000])
    for command in (["systemctl", "daemon-reload"], ["systemctl", "enable", "ella-ap-watch.service"],
                    ["systemctl", "restart", "ella-ap-watch.service"]):
        if run(command, timeout=20)["code"]:
            raise ValueError("Watch service installation failed; inspect its journal")
    print("Installed AP watch " + VERSION + ". Hotspot and collector were not restarted.")
    print("30-second checks; 120-second startup grace; requires both clients seen before traffic recovery arms.")
    print("sudo ella-ap-watch status")


def uninstall():
    c = config()
    if run(["systemctl", "disable", "--now", "ella-ap-watch.service"], timeout=20)["code"]:
        raise ValueError("Could not stop monitor; no files removed")
    dropin = SYSTEMD / (c["unit"] + ".d") / "70-ap-watch.conf"
    for path in (SERVICE, dropin):
        if path.exists() and path.read_text().startswith(MARKER):
            path.unlink()
    run(["systemctl", "daemon-reload"])
    match = re.search(r"Current level:\s*([A-Z]+)", c.get("original_log_level", ""))
    if match and match[1] in ("EXCESSIVE", "MSGDUMP", "DEBUG", "INFO", "WARNING", "ERROR"):
        control(c, "LOG_LEVEL " + match[1])
    if c.get("original_power") == "Power save: on":
        run(["iw", "dev", c["interface"], "set", "power_save", "on"])
    CONFIG.unlink()
    PROGRAM.unlink(missing_ok=True)
    print("Watch and startup drop-in removed. AP/collector not restarted. Incident files retained in " + str(ARCHIVE))


def main():
    os.environ["PATH"] = "/usr/sbin:/usr/bin:/sbin:/bin"
    os.environ["LC_ALL"] = "C"
    os.umask(0o077)
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", choices=("install", "daemon", "status", "observe", "recover", "capture", "report", "reset", "uninstall", "_on-start"))
    parser.add_argument("--interface", default="wlan0")
    parser.add_argument("--unit", default="ella-hotspot.service")
    parser.add_argument("--control", default="/run/hostapd-ella")
    parser.add_argument("--leases", default="/run/ella-hotspot-dns/leases")
    parser.add_argument("--target", action="append")
    args = parser.parse_args()
    if os.geteuid() != 0:
        parser.error("Run with sudo")
    if args.action == "install":
        install(args)
    elif args.action == "daemon":
        def stop(*unused):
            global STOP
            STOP = True
        signal.signal(signal.SIGTERM, stop)
        signal.signal(signal.SIGINT, stop)
        daemon()
    elif args.action == "_on-start":
        on_start(config())
    elif args.action in ("observe", "recover"):
        c = config()
        c["mode"] = args.action
        write_json(CONFIG, c)
        print("Mode: " + args.action + "; takes effect within one check cycle.")
    elif args.action == "status":
        c = config()
        print("Mode: " + c["mode"])
        print(run(["systemctl", "is-active", "ella-ap-watch.service"])["text"].strip())
        try:
            s = read_json(RUNTIME / "state.json")
            print("Last check: " + s["observation"]["utc"])
            print("State: " + s["reason"])
            print("Recoveries this boot (last 30 minutes): " + str(sum(time.monotonic() - x < 1800 for x in s["policy"]["attempts"])))
            for v in s["observation"]["targets"].values():
                print(v["ip"] + ": " + ("traffic/probe OK" if v["alive"] else "no confirmed traffic"))
        except (OSError, ValueError, KeyError, TypeError):
            print("No readable runtime status yet; inspect service journal.")
    elif args.action == "capture":
        c = config()
        o, _ = observe(c, {})
        print(snapshot(c, "manual-no-restart", o))
    elif args.action == "report":
        files = list(ARCHIVE.glob("incident-*.json.gz"))
        if not files:
            print("No incident files.")
        else:
            path = max(files, key=lambda p: p.stat().st_mtime_ns)
            with gzip.open(path, "rt") as source:
                print(json.dumps(json.load(source), indent=2))
    elif args.action == "reset":
        if run(["systemctl", "stop", "ella-ap-watch.service"], timeout=15)["code"]:
            raise ValueError("Could not stop monitor; recovery state retained")
        (RUNTIME / "state.json").unlink(missing_ok=True)
        if run(["systemctl", "start", "ella-ap-watch.service"], timeout=15)["code"]:
            raise ValueError("Monitor state reset but service failed to start; inspect its journal")
        print("Monitor baseline/recovery latch reset; hotspot was not restarted.")
    elif args.action == "uninstall":
        uninstall()


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print("AP watch error: " + str(error), file=sys.stderr)
        sys.exit(1)
