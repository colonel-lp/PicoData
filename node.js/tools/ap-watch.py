#!/usr/bin/env python3
"""Bounded hostapd diagnostics and conservative AP recovery; Python 3.9+.

Install on the Pi with sudo and two --target IPv4 addresses of always-on
hotspot clients, or install --topology sta-only --uplink WIFI_INTERFACE.
STA-only records faults without AP checks or network recovery. All installation-specific values stay in /etc, not source.
No API/MQTT credentials, application data, interface resets or uplink changes.
"""
import argparse
from collections import deque
from datetime import datetime, timezone
import fcntl
import gzip
import hashlib
import http.client
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
import threading
import time
import traceback

VERSION = "1.4"
CONFIG = Path("/etc/ella-ap-watch.json")
RUNTIME = Path("/run/ella-ap-watch")
ARCHIVE = Path("/var/lib/ella-ap-watch")
PROGRAM = Path("/usr/local/sbin/ella-ap-watch")
SYSTEMD = Path("/etc/systemd/system")
SERVICE = SYSTEMD / "ella-ap-watch.service"
MARKER = "# Managed by Ella AP watch\n"
STOP = False
PHASE = "startup"
ERROR_TIMES = {}
ERROR_CAPTURE = threading.local()
RECORDER = None
RING_LOCK = threading.RLock()
SYS_NET = Path('/sys/class/net')
DEBUG_NET = Path('/sys/kernel/debug/ieee80211')
BROKER_LOG = Path('/var/log/mosquitto/mosquitto.log')
PROC_ROUTES = Path('/proc/net/route')
FW_DEBUG = Path('/sys/module/brcmfmac/parameters/debug')


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
    info = path.stat()
    if path.is_symlink() or info.st_uid != 0:
        raise ValueError("Unsafe directory: " + str(path))
    if info.st_mode & 0o7777 != 0o700:
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
    with RING_LOCK:
        _ring(event)


def _ring(event):
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
    recorder = RECORDER
    if recorder is not None:
        recorder.add(record)


def journal(message, stream=None):
    """A journal socket must never block or terminate AP recovery."""
    stream = sys.stdout if stream is None else stream
    data = (str(message)[:1800] + "\n").encode("utf-8", "replace")[:2048]
    try:
        descriptor = stream.fileno()
        os.set_blocking(descriptor, False)
        return os.write(descriptor, data) == len(data)
    except (OSError, ValueError, AttributeError):
        return False


def report_error(error, phase, persistent=False):
    """RAM errors plus one bounded disk record per five minutes for critical errors."""
    now = time.monotonic()
    key = (phase, type(error).__name__)
    if now - ERROR_TIMES.get(key, -math.inf) < 30:
        return
    if len(ERROR_TIMES) >= 16:
        ERROR_TIMES.clear()
    ERROR_TIMES[key] = now
    record = {"version": VERSION, "utc": stamp(), "uptime": now, "phase": str(phase)[:80],
              "error": type(error).__name__[:80], "message": str(error)[:1024],
              "traceback": "".join(traceback.format_exception(type(error), error, error.__traceback__, limit=12))[-8192:]}
    try:
        record["boot"] = boot_id()
    except OSError:
        record["boot"] = None
    while len(json.dumps(record, ensure_ascii=True).encode()) > 16384:
        record["truncated"] = True
        if len(record["traceback"]) > 128:
            record["traceback"] = record["traceback"][-len(record["traceback"]) // 2:]
        else:
            record["message"] = record["message"][:len(record["message"]) // 2]
    try:
        mkdir(RUNTIME)
        write_json(RUNTIME / "last-error.json", record)
    except (OSError, ValueError):
        pass
    if persistent:
        persist_error(record, now)
        recorder = RECORDER
        if recorder is not None and not recorder.closed and not getattr(ERROR_CAPTURE, 'active', False):
            ERROR_CAPTURE.active = True
            try:
                recorder.add({'type': 'monitor-error', 'utc': record['utc'], 'uptime': now,
                              'phase': record['phase'], 'error': record['error'],
                              'message': clean_message(record['message'])})
                recorder.trigger('monitor-error:' + record['phase'], now)
            except Exception:
                # Archive/storage failure must not recurse or replace the original exception.
                pass
            finally:
                ERROR_CAPTURE.active = False


def persist_error(record, now):
    try:
        mkdir(ARCHIVE)
        fd = os.open(ARCHIVE / "error.lock", os.O_RDWR | os.O_CREAT | os.O_NOFOLLOW, 0o600)
        with os.fdopen(fd, "a") as lock:
            # Operator commands and a restarting daemon share the same write limit.
            fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
            try:
                previous = read_json(ARCHIVE / "last-error.json")
                if not isinstance(previous, dict):
                    previous = {}
            except (OSError, ValueError):
                previous = {}
            at = previous.get("uptime")
            same_boot = not (record["boot"] and previous.get("boot") and previous["boot"] != record["boot"])
            recent = (same_boot and
                      type(at) in (int, float) and math.isfinite(at) and 0 <= now - at < 300)
            if not recent:
                write_json(ARCHIVE / "last-error.json", record)
    except (OSError, ValueError, TypeError, AttributeError):
        pass


def safe_ring(event):
    try:
        ring(event)
        return True
    except (OSError, ValueError) as error:
        report_error(error, "event-log")
        return False


def notice(message):
    safe_ring({"type": "notice", "phase": PHASE, "message": str(message)[:1800]})
    if not journal(message):
        report_error(OSError("Journal output unavailable; monitor continues using private RAM evidence"), "journal-output")


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


def sta_only(c):
    return c.get("topology", "ap-sta") == "sta-only"


def validate_config(c):
    if c.get("version") != 1 or c.get("mode") not in ("recover", "observe"):
        raise ValueError("Invalid watch configuration")
    if not re.fullmatch(r"[a-zA-Z0-9_-]{1,15}", c.get("interface", "")):
        raise ValueError("Invalid interface")
    if not re.fullmatch(r"[a-zA-Z0-9_.@-]+\.service", c.get("unit", "")):
        raise ValueError("Invalid hostapd service")
    if not str(c.get("control", "")).startswith("/run/"):
        raise ValueError("Control directory must be under /run")
    if c.get('topology', 'ap-sta') not in ('ap-sta', 'sta-only'):
        raise ValueError('Invalid topology')
    if sta_only(c) and (c.get('mode') != 'observe' or not c.get('uplink')):
        raise ValueError('STA-only requires observe mode and an explicit uplink')
    peers = c.get('peers', [])
    if not isinstance(peers, list) or len(peers) > 4 or len(set(peers)) != len(peers):
        raise ValueError('Configure at most four distinct peer IPv4 addresses')
    for peer in peers:
        address = ipaddress.IPv4Address(peer)
        if address.is_multicast or address.is_unspecified or address.is_loopback:
            raise ValueError('Invalid peer address')
    targets = c.get("targets", [])
    if not isinstance(targets, list) or len(targets) > 4 or (not sta_only(c) and len(targets) < 2):
        raise ValueError("Configure two to four always-on clients")
    for target in targets:
        ipaddress.IPv4Address(target["ip"])
        if not re.fullmatch(r"(?:[0-9a-f]{2}:){5}[0-9a-f]{2}", target["mac"]):
            raise ValueError("Invalid target MAC")
    if len({t["ip"] for t in targets}) != len(targets) or len({t["mac"] for t in targets}) != len(targets):
        raise ValueError("Targets must identify different clients")
    if c.get("uplink") is not None and (not isinstance(c['uplink'], str) or not re.fullmatch(r"[a-zA-Z0-9_-]{1,15}", c["uplink"])):
        raise ValueError("Invalid uplink interface")
    if 'firmware_console' in c and type(c['firmware_console']) is not bool:
        raise ValueError('Invalid firmware console option')
    api = c.get("local_api")
    if api is not None:
        if (not isinstance(api, dict) or set(api) != {"host", "port", "scheme"} or
                type(api["port"]) is not int or not 1 <= api["port"] <= 65535 or
                api["scheme"] not in ("http", "tcp")):
            raise ValueError("Invalid local API probe")
        if not ipaddress.ip_address(api["host"]).is_loopback:
            raise ValueError("API diagnostic probe must use loopback")
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
                    safe_ring({"type": "hostapd-event", "text": data.decode("utf-8", "replace")[:4096]})
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
            match = re.match(r"\s*(rx bytes|rx packets|tx bytes|tx packets|tx failed|tx retries|beacon loss|beacon rx|rx drop misc|connected time):\s*(\d+)", line)
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
        if routed:
            probe = run(["ping", "-n", "-I", iface, "-c", "1", "-W", "2", target["ip"]], timeout=3)["code"] == 0
        observations[mac] = {"ip": target["ip"], "rx_progress": progressed,
                             "probe": probe, "route_ok": routed,
                             "alive": progressed or probe is True}
    return {"utc": stamp(), "uptime": time.monotonic(), "active": active,
            "enabled": enabled, "hostapd": state, "stations": stas,
            "station_query_ok": dump["code"] == 0, "targets": observations}, counters


class Policy:
    """One recovery per unresolved incident; max two in any thirty minutes."""
    def __init__(self, now, saved=None, baseline=None):
        self.started = now
        self.seen = set()
        self.last_good = now
        self.control_bad = None
        self.attempts = []
        self.latched = False
        self.stable_since = None
        self.inhibited = False
        self.baseline_restored = False
        if saved is not None:
            try:
                attempts = saved["attempts"]
                if not isinstance(attempts, list) or len(attempts) > 100:
                    raise ValueError()
                if not all(type(t) in (int, float) and math.isfinite(t) and 0 <= t <= now for t in attempts):
                    raise ValueError()
                if type(saved["latched"]) is not bool:
                    raise ValueError()
                if type(saved.get("inhibited", False)) is not bool:
                    raise ValueError()
                self.attempts = attempts
                self.latched = saved["latched"]
                self.inhibited = saved.get("inhibited", False)
            except (KeyError, TypeError, ValueError):
                self.inhibited = True
        if baseline is not None:
            try:
                seen, last_good = baseline["seen"], baseline["last_good"]
                if (not isinstance(seen, list) or len(seen) > 4 or
                        not all(isinstance(m, str) and re.fullmatch(r"(?:[0-9a-f]{2}:){5}[0-9a-f]{2}", m) for m in seen) or
                        len(set(seen)) != len(seen) or type(last_good) not in (int, float) or
                        not math.isfinite(last_good) or not 0 <= last_good <= now):
                    raise ValueError()
                self.seen = set(seen)
                self.last_good = last_good
                self.baseline_restored = bool(seen)
            except (KeyError, TypeError, ValueError):
                self.inhibited = True

    def saved(self):
        result = {"attempts": self.attempts, "latched": self.latched}
        if self.inhibited:
            result["inhibited"] = True
        return result

    def baseline(self):
        return {"seen": sorted(self.seen), "last_good": self.last_good}

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
            if any(t.get('rx_progress') and t.get('probe') is False for t in targets.values()):
                return 'rx-present-roundtrip-failed', False
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


def binding(c):
    value = {k: c[k] for k in ("interface", "unit", "control")}
    value["targets"] = sorted(c["targets"], key=lambda t: (t["mac"], t["ip"]))
    return hashlib.sha256(json.dumps(value, sort_keys=True, separators=(",", ":")).encode()).hexdigest()


def load_policy(c, identity, now):
    """Trust baseline only in this boot, for these exact targets/AP; never reset limits silently."""
    try:
        saved = read_json(RUNTIME / "state.json")
        if not isinstance(saved, dict) or not re.fullmatch(r"[0-9a-f-]{36}", str(saved.get("boot", ""))):
            raise ValueError("Invalid runtime identity")
        if saved["boot"] != identity:
            return Policy(now)
        limits = saved.get("policy") if isinstance(saved.get("policy"), dict) else {}
        baseline = None
        if "stateVersion" in saved:
            if type(saved["stateVersion"]) is not int or saved["stateVersion"] != 2:
                raise ValueError("Unsupported monitor runtime state")
            if not isinstance(saved.get("binding"), str) or not re.fullmatch(r"[0-9a-f]{64}", saved["binding"]):
                raise ValueError("Invalid runtime configuration binding")
            if saved.get("binding") == binding(c):
                baseline = saved.get("baseline")
                if not isinstance(baseline, dict) or not isinstance(baseline.get("seen"), list):
                    raise ValueError("Missing sensor baseline")
                if not set(baseline["seen"]).issubset({t["mac"] for t in c["targets"]}):
                    raise ValueError("Unexpected baseline client")
            else:
                safe_ring({"type": "baseline-discarded", "reason": "AP/target configuration changed"})
        policy = Policy(now, limits, baseline)
        if policy.inhibited:
            report_error(ValueError("Invalid or inhibited recovery state; explicit reset required"), "restore-state", True)
        return policy
    except FileNotFoundError:
        return Policy(now)
    except (ValueError, TypeError, AttributeError, OSError) as error:
        report_error(error, "restore-state", True)
        return Policy(now, {})


def checkpoint(c, identity, policy, reason, observation):
    try:
        write_json(RUNTIME / "state.json", {"stateVersion": 2, "version": VERSION, "boot": identity,
                   "binding": binding(c), "policy": policy.saved(), "baseline": policy.baseline(),
                   "baselineRestored": policy.baseline_restored,
                   "reason": reason, "mode": c["mode"], "observation": observation})
        return True
    except (OSError, ValueError) as error:
        report_error(error, "state-write", True)
        notice("AP watch: cannot save recovery state; automatic restart inhibited for this check.")
        return False


def snapshot(c, reason, observation):
    """Only incidents/manual captures reach the card; at most five gzip files."""
    if RECORDER is not None:
        return RECORDER.trigger(reason, observation=observation)
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
            "monitor_error": tail(RUNTIME / "last-error.json", 16384),
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


def bounded_text(path, limit=16384):
    try:
        with Path(path).open() as source:
            value = source.read(limit + 1)
        return value if len(value) <= limit else ""
    except OSError:
        return ""


def clean_message(value):
    value = str(value)[:2048]
    return re.sub(r'''(?i)(bearer\s+|(?:token|password|passphrase|psk|secret)\s*[=:]\s*)(?:"[^"]*"|'[^']*'|[^\s,;]+)''',
                  r"\1[REDACTED]", value)


def default_wifi(ap, preferred=None):
    """No scans or DNS; find the actual wireless uplink, not a fixed phy number."""
    names = [p.name for p in SYS_NET.glob('*')
             if p.name != ap and re.fullmatch(r'[a-zA-Z0-9_-]{1,15}', p.name)
             and (p / 'phy80211').exists()]
    if preferred:
        return preferred
    routes = bounded_text(PROC_ROUTES).splitlines()[1:]
    candidates = []
    for line in routes:
        f = line.split()
        if len(f) >= 8 and f[0] in names and f[1] == '00000000' and f[7] == '00000000':
            try:
                if int(f[3], 16) & 1:
                    candidates.append((int(f[6]), f[0]))
            except ValueError:
                pass
    return min(candidates)[1] if candidates else names[0] if len(names) == 1 else None


def interface_sample(name):
    if not name:
        return {'interface': None, 'present': None, 'reason': 'uplink-not-identifiable'}
    path = SYS_NET / name
    if not path.exists():
        return {'interface': name, 'present': False}
    result = {'interface': name, 'present': True,
              'state': bounded_text(path / 'operstate', 64).strip(),
              'carrier': bounded_text(path / 'carrier', 64).strip(), 'counters': {}}
    for key in ('rx_bytes', 'rx_packets', 'rx_errors', 'rx_dropped',
                'tx_bytes', 'tx_packets', 'tx_errors', 'tx_dropped'):
        text = bounded_text(path / 'statistics' / key, 64).strip()
        result['counters'][key] = int(text) if text.isdigit() else None
    try:
        phy = (path / 'phy80211').resolve(strict=True).name
        if not re.fullmatch(r'phy[0-9]+', phy):
            raise ValueError()
        result['phy'] = phy
        text = bounded_text(DEBUG_NET / phy / 'counters')
        values = {}
        for key in ('fc_xoff', 'fc_xon', 'f2txdata'):
            match = re.search(r'^' + key + r':\s*(\d+)\s*$', text, re.M)
            values[key] = int(match[1]) if match else None
        result['firmware'] = values
    except (OSError, ValueError):
        result['phy'] = None
        result['firmware'] = None
    try:
        device = (path / 'device').resolve(strict=True)
        result['driver'] = (device / 'driver').resolve(strict=True).name
        usb = device.parent if ':' in device.name else device
        if (usb / 'idVendor').exists():
            result['usb'] = {key: bounded_text(usb / 'power' / key, 64).strip() or None
                             for key in ('control', 'runtime_status', 'autosuspend_delay_ms')}
    except OSError:
        result['driver'] = None
    return result


def gateway_for(interface):
    rows = []
    for line in bounded_text(PROC_ROUTES).splitlines()[1:]:
        f = line.split()
        if len(f) >= 8 and f[0] == interface and f[1] == '00000000' and f[7] == '00000000':
            try:
                if int(f[3], 16) & 1 and int(f[2], 16):
                    address = socket.inet_ntoa(int(f[2], 16).to_bytes(4, 'little'))
                    rows.append((int(f[6]), address))
            except (ValueError, OverflowError):
                pass
    return min(rows)[1] if rows else None


def local_api_sample(c):
    """Read limited loopback health when supported; older HTTP/TLS probes test availability only."""
    api = c.get('local_api')
    if not api:
        return {'configured': False, 'ok': None, 'reason': 'probe-not-configured'}
    started = time.monotonic()
    try:
        if api['scheme'] == 'http':
            connection = http.client.HTTPConnection(api['host'], api['port'], timeout=0.7)
            try:
                connection.request('GET', '/api/v1/health', headers={'Connection': 'close'})
                response = connection.getresponse()
                # New collectors expose limited loopback-only health. Older versions
                # reply 401, which proves listener availability only.
                sample = {'configured': True, 'kind': 'http-without-credentials',
                          'ok': True, 'expected_status': response.status in (200, 401), 'status': response.status,
                          'elapsed_ms': round((time.monotonic() - started) * 1000)}
                if response.status == 200:
                    raw = response.read(65537)
                    if len(raw) > 65536:
                        raise ValueError('Oversized collector health response')
                    document = json.loads(raw)
                    health = document.get('health')
                    if not isinstance(health, dict) or not isinstance(health.get('sources'), dict) or not isinstance(health.get('logging'), dict):
                        raise ValueError('Invalid collector health response')
                    sample['health'] = health
                    sample['requests'] = document.get('requests', [])[-32:]
                else:
                    sample['health_unavailable'] = True
                return sample
            finally:
                connection.close()
        with socket.create_connection((api['host'], api['port']), timeout=0.7):
            pass
        return {'configured': True, 'kind': 'tcp-only', 'ok': True,
                'elapsed_ms': round((time.monotonic() - started) * 1000)}
    except (OSError, http.client.HTTPException, ValueError, TypeError, AttributeError) as error:
        return {'configured': True, 'kind': api['scheme'], 'ok': False,
                'error': type(error).__name__, 'errno': getattr(error, 'errno', None),
                'elapsed_ms': round((time.monotonic() - started) * 1000)}


class LearnedChecks:
    """Diagnostic triggers only. Never authorizes a network/recovery operation."""
    def __init__(self, saved=None):
        self.started = None
        self.values = saved if isinstance(saved, dict) else {}
        if len(self.values) > 32:
            self.values = {}
        self.values = {k: v for k, v in self.values.items()
                       if isinstance(k, str) and len(k) <= 96 and isinstance(v, dict)
                       and type(v.get('successes')) is int and 0 <= v['successes'] <= 2
                       and type(v.get('failures')) is int and 0 <= v['failures'] <= 10000
                       and type(v.get('first_bad')) in (int, float)
                       and math.isfinite(v['first_bad']) and v['first_bad'] >= 0}

    def check(self, key, ok, now, minimum=2, required=False):
        if self.started is None:
            self.started = now
        if ok is None:
            return None
        if key not in self.values and len(self.values) >= 32:
            self.values.pop(next(iter(self.values)))
        value = self.values.setdefault(key, {'successes': 0, 'failures': 0, 'first_bad': now})
        if ok:
            value['successes'] = min(2, value['successes'] + 1)
            value['failures'] = 0
            value['first_bad'] = now
            return None
        if value['failures'] == 0:
            value['first_bad'] = now
        value['failures'] = min(10000, value['failures'] + 1)
        armed = value['successes'] >= 2 or (required and now - self.started >= 60)
        if armed and value['failures'] >= minimum:
            return value['first_bad']
        return None


def read_fault(path):
    if path.is_symlink():
        raise ValueError('Symlink is not a fault archive')
    opener = gzip.open if path.suffix == '.gz' else open
    with opener(path, 'rb') as source:
        raw = source.read(2 * 1024 * 1024 + 1)
    if len(raw) > 2 * 1024 * 1024:
        raise ValueError('Fault exceeds read bound')
    return json.loads(raw)


def write_fault(path, document, compressed=False):
    mkdir(ARCHIVE)
    raw = json.dumps(document, ensure_ascii=True, allow_nan=False).encode()
    while len(raw) > 2 * 1024 * 1024 and document.get('events'):
        # Preserve the beginning of the fault, rather than overwriting it as
        # a noisy broken driver repeats the same aftermath hundreds of times.
        index = len(document['events']) // 2 if len(document['events']) > 20 else -1
        document['events'].pop(index)
        document['event_context_truncated'] = True
        raw = json.dumps(document, ensure_ascii=True, allow_nan=False).encode()
    if len(raw) > 2 * 1024 * 1024:
        raise ValueError('Fault exceeds archive bound')
    fd, temporary = tempfile.mkstemp(prefix='.fault-', dir=ARCHIVE)
    try:
        with os.fdopen(fd, 'wb') as output:
            if compressed:
                with gzip.GzipFile(fileobj=output, mode='wb', compresslevel=1) as zipped:
                    zipped.write(raw)
            else:
                output.write(raw)
            output.flush()
            os.fsync(output.fileno())
        os.replace(temporary, path)
        directory = os.open(ARCHIVE, os.O_RDONLY | os.O_DIRECTORY)
        try:
            os.fsync(directory)
        finally:
            os.close(directory)
    finally:
        Path(temporary).unlink(missing_ok=True)


def prune_faults():
    files = sorted(ARCHIVE.glob('incident-*.json.gz'), key=lambda p: p.stat().st_mtime_ns)
    for path in files[:-5]:
        path.unlink()


class FlightRecorder:
    """Two-minute incident windows with immediate durable prefix and bounded RAM context."""
    def __init__(self, c, now=time.monotonic):
        self.c, self.now = c, now
        self.boot = boot_id()
        self.signature = hashlib.sha256(json.dumps({'ap': binding(c), 'uplink': c.get('uplink'),
                                                    'api': c.get('local_api'), 'topology': c.get('topology', 'ap-sta'),
                                                    'peers': c.get('peers', [])}, sort_keys=True).encode()).hexdigest()
        self.lock = threading.RLock()
        self.health = deque(maxlen=241)
        self.events = deque(maxlen=512)
        self.active = None
        self.last_bad = -math.inf
        self.last_checkpoint = 0
        self.closed = False
        self.stop_event = threading.Event()
        self.slow = {}
        self.last_slow = -math.inf
        self.slow_seen = -math.inf
        self.slow_busy = False
        self.background = False
        self.last_status = -math.inf
        self.last_saved_at = None
        self.last_saved_change = self.now()
        self.last_pid = None
        self.last_uplink = None
        self.broker_cursor = None
        self.broker_partial = b''
        self.capture_busy = False
        self.limited_events = 0
        self.checks = LearnedChecks()
        try:
            saved = read_json(RUNTIME / 'flight-status.json')
            if saved.get('boot') == self.boot and saved.get('binding') == self.signature:
                self.checks = LearnedChecks(saved.get('learned'))
        except (OSError, ValueError, AttributeError):
            pass
        # Never claim that a stopped process collected a post-event tail.
        mkdir(ARCHIVE)
        for path in sorted(ARCHIVE.glob('incident-*.pending.json'))[:5]:
            try:
                document = read_fault(path)
                document['state'] = 'interrupted'
                document['post_complete'] = False
                document['resumed_on_boot'] = self.boot
                write_fault(path.with_name(path.name.replace('.pending.json', '.json.gz')), document, True)
                path.unlink()
            except (OSError, ValueError, TypeError) as error:
                report_error(error, 'fault-resume', True)
        prune_faults()

    def add(self, record):
        with self.lock:
            if self.closed:
                return
            record = json.loads(json.dumps(record, ensure_ascii=True, allow_nan=False))
            if len(json.dumps(record).encode()) > 16384:
                record = {'uptime': record.get('uptime', self.now()), 'utc': stamp(),
                          'type': 'diagnostic-record-truncated'}
            queue = self.health if record.get('type') == 'wifi-health' else self.events
            if queue is self.events and len(queue) == queue.maxlen:
                self.limited_events += 1
            queue.append(record)
            if self.active and self.active['confirmed_at'] <= record.get('uptime', -1) <= self.active['end']:
                key = 'health' if record.get('type') == 'wifi-health' else 'events'
                records = self.active[key]
                if len(records) < (241 if key == 'health' else 1024):
                    records.append(record)
                else:
                    self.active['event_context_truncated'] = True
                    self.limited_events += 1
            edge = self.now() - 180
            for q in (self.health, self.events):
                while q and q[0].get('uptime', 0) < edge:
                    q.popleft()

    def trigger(self, reason, onset=None, observation=None):
        now = self.now()
        onset = now if onset is None else min(now, max(0, onset))
        with self.lock:
            self.last_bad = now
            if self.active:
                if reason not in self.active['reasons'] and len(self.active['reasons']) < 32:
                    self.active['reasons'].append(reason)
                return self.active['path']
            # One archive for an unresolved episode. Re-arm only after one quiet minute.
            if getattr(self, 'latched', False):
                return 'existing incident; waiting for a healthy minute before a new archive'
            path = ARCHIVE / ('incident-' + str(time.time_ns()) + '.pending.json')
            self.active = {'path': str(path), 'version': VERSION, 'format': 2,
                           'boot': self.boot, 'utc': stamp(), 'onset': onset,
                           'confirmed_at': now, 'end': onset + 60, 'reasons': [reason],
                           'observation': observation, 'state': 'collecting',
                           'pre_complete': bool(self.health and self.health[0]['uptime'] <= onset - 60),
                           'post_complete': False, 'snapshots': [],
                           'health': [r for r in self.health if onset - 60 <= r['uptime'] <= onset + 60],
                           'events': [r for r in self.events if onset - 60 <= r['uptime'] <= onset + 60]}
            self.latched = True
            self._save(False)
            self._extra('at-detection')
            return str(path)

    def _document(self):
        document = dict(self.active)
        document['health'] = list(document['health'])
        document['events'] = list(document['events'])
        document['last_updated'] = self.now()
        document['event_drop_count'] = self.limited_events
        times = [r['uptime'] for r in document['health']]
        document['largest_sample_gap_seconds'] = max((b-a for a, b in zip(times, times[1:])), default=None)
        document['limits'] = {'pre_seconds': 60, 'post_seconds': 60, 'health_interval_seconds': 1,
                              'link_probe_interval_seconds': 10, 'events_max': 512,
                              'archive_bytes_max': 2 * 1024 * 1024}
        return document

    def _save(self, finished):
        document = self._document()
        path = Path(document['path'])
        if finished:
            document['state'] = 'complete' if document['post_complete'] else 'interrupted'
            destination = path.with_name(path.name.replace('.pending.json', '.json.gz'))
            write_fault(destination, document, True)
            path.unlink(missing_ok=True)
            self.active = None
            prune_faults()
        else:
            write_fault(path, document)
        self.last_checkpoint = self.now()

    def _extra(self, label):
        if self.capture_busy or not self.active:
            return
        self.capture_busy = True
        identity = self.active['path']
        def collect():
            try:
                commands = {
                    'kernel': ['journalctl', '-k', '-b', '--since', '-3min', '-n', '200', '--no-pager', '-o', 'short-iso'],
                    'services': ['journalctl', '-b', '-u', self.c['unit'], '-u', 'NetworkManager',
                                 '-u', 'ella-hotspot-dns.service', '-u', 'pico-mqtt.service', '-u', 'mosquitto.service',
                                 '--since', '-3min', '-n', '300', '--no-pager', '-o', 'short-iso'],
                    'routes': ['ip', '-4', 'route'],
                    'addresses': ['ip', '-4', '-brief', 'address'],
                    'firewall': ['nft', 'list', 'table', 'ip', 'ella_hotspot'],
                }
                if sta_only(self.c):
                    commands.pop('firewall')
                    commands['services'] = ['journalctl', '-b', '-u', 'NetworkManager', '-u', 'wpa_supplicant',
                                            '-u', 'pico-mqtt.service', '-u', 'mosquitto.service',
                                            '--since', '-3min', '-n', '300', '--no-pager', '-o', 'short-iso']
                details = {name: run(command, timeout=2, limit=32768) for name, command in commands.items()}
                for result in details.values():
                    result['text'] = '\n'.join(clean_message(line) for line in result['text'].splitlines())
                # These are private system diagnostics; never read credential/config files.
                with self.lock:
                    if self.active and self.active['path'] == identity and not self.closed:
                        self.active['snapshots'].append({'phase': label, 'utc': stamp(), 'details': details})
            except Exception as error:
                report_error(error, 'fault-context', True)
            finally:
                with self.lock:
                    self.capture_busy = False
        threading.Thread(target=collect, name='wifi-fault-context', daemon=True).start()

    def _ping(self, interface, address):
        if not interface or not address:
            return None
        started = self.now()
        result = run(['ping', '-n', '-I', interface, '-c', '1', '-W', '1', address], timeout=1.5, limit=1024)
        match = re.search(r'time[=<]([0-9.]+)\s*ms', result['text'])
        return {'ok': result['code'] == 0, 'rtt_ms': float(match[1]) if match else None,
                'elapsed_ms': round((self.now() - started) * 1000), 'code': result['code'], 'limited': result['limited']}

    def _slow_sample(self, uplink):
        ap = self.c['interface']
        info = ({'code': None, 'text': ''} if sta_only(self.c) else
                run(['iw', 'dev', ap, 'station', 'dump'], timeout=1.5, limit=32768))
        all_stations = stations(info['text']) if info['code'] == 0 else {}
        selected = {t['mac']: all_stations[t['mac']] for t in self.c['targets'] if t['mac'] in all_stations}
        for mac, value in all_stations.items():
            if len(selected) >= 16:
                break
            selected.setdefault(mac, value)
        probes = {}
        for t in ([] if sta_only(self.c) else self.c['targets']):
            try:
                route = ip_json(['route', 'get', t['ip']])
                routed = bool(route and route[0].get('dev') == ap and not route[0].get('gateway'))
            except (ValueError, KeyError, TypeError):
                routed = False
            probe = self._ping(ap, t['ip']) if routed else None
            probes[t['mac']] = {'route_ok': routed, **(probe or {'ok': None, 'rtt_ms': None})}
        peer_probes = {}
        addresses = list(self.c.get('peers', []))
        # Former AP target MACs identify the same sensors after moving to a router.
        # Read the local neighbour cache only: never scan the network or reuse old AP IPs.
        if sta_only(self.c) and uplink:
            wanted = {t['mac'] for t in self.c['targets']}
            for row in ip_json(['neigh', 'show', 'dev', uplink]):
                if str(row.get('lladdr', '')).lower() in wanted:
                    try:
                        address = str(ipaddress.IPv4Address(row['dst']))
                    except (ValueError, KeyError):
                        continue
                    if address not in addresses and len(addresses) < 4:
                        addresses.append(address)
        for address in addresses:
            try:
                route = ip_json(['route', 'get', address])
                routed = bool(route and route[0].get('dev') == uplink and not route[0].get('gateway'))
            except (ValueError, KeyError, TypeError):
                routed = False
            probe = self._ping(uplink, address) if routed else None
            peer_probes[address] = {'route_ok': routed, **(probe or {'ok': None, 'rtt_ms': None})}
        link = run(['iw', 'dev', uplink, 'link'], timeout=1.5, limit=8192) if uplink else None
        peer = run(['iw', 'dev', uplink, 'station', 'dump'], timeout=1.5, limit=8192) if uplink else None
        gateway = gateway_for(uplink) if uplink else None
        gateway_probe = self._ping(uplink, gateway)
        service_read = run(['systemctl', 'show', 'pico-mqtt.service', '-p', 'ActiveState',
                            '-p', 'MainPID', '-p', 'NRestarts'], timeout=1.5, limit=2048)
        service = pairs(service_read['text']) if service_read['code'] == 0 else {}
        text = link['text'] if link and link['code'] == 0 else ''
        properties = {}
        for key in ('SSID', 'signal', 'rx bitrate', 'tx bitrate', 'freq'):
            match = re.search(r'^\s*' + re.escape(key) + r':\s*(.+)$', text, re.M)
            properties[key] = match[1][:128] if match else None
        self.slow = {'sampled_at': self.now(), 'ap_stations': selected,
                     'station_query_ok': info['code'] == 0, 'ap_probes': probes, 'peer_probes': peer_probes,
                     'uplink': uplink, 'associated': (text.startswith('Connected to ') if text else None),
                     'link': properties, 'peer': stations(peer['text']) if peer and peer['code'] == 0 else None,
                     'gateway': gateway, 'gateway_ok': gateway_probe['ok'] if gateway_probe else None,
                     'gateway_probe': gateway_probe, 'collector_service': service,
                     'query_codes': {'ap_stations': info['code'], 'sta_link': link['code'] if link else None,
                                     'sta_peer': peer['code'] if peer else None, 'collector_service': service_read['code']}}

    def broker_events(self):
        """Mosquitto commonly logs to a file instead of journald. Read only new bounded text."""
        try:
            with BROKER_LOG.open('rb') as source:
                info = os.fstat(source.fileno())
                identity = (info.st_dev, info.st_ino)
                if self.broker_cursor is None:
                    self.broker_cursor = (identity, info.st_size)
                    return {'available': True, 'baseline': True}
                previous, offset = self.broker_cursor
                if identity != previous or info.st_size < offset:
                    offset, self.broker_partial = 0, b''
                skipped = info.st_size - offset > 16384
                if skipped:
                    offset, self.broker_partial = max(0, info.st_size - 16384), b''
                    self.limited_events += 1
                source.seek(offset)
                raw = self.broker_partial + source.read(16384)
                self.broker_cursor = (identity, source.tell())
            lines = raw.split(b'\n')
            self.broker_partial = lines.pop()[:2048]
            if skipped and lines:
                lines.pop(0)  # first line may start in the middle of an old message
            for line in lines[:32]:
                message = clean_message(line.decode('utf-8', 'replace'))
                now = self.now()
                self.add({'type': 'broker-file', 'utc': stamp(), 'uptime': now, 'message': message})
                if re.search(r'(?i)malformed packet|exceeded timeout|bad socket|already connected, closing old connection|client .+ disconnected|socket error on client', message):
                    self.trigger('mqtt-broker-client-error', now)
            if len(lines) > 32:
                self.limited_events += len(lines) - 32
            return {'available': True, 'limited': skipped or len(lines) > 32}
        except OSError as error:
            return {'available': False, 'error': type(error).__name__}

    def sample(self):
        started = self.now()
        uplink = default_wifi(self.c['interface'], self.c.get('uplink')) or self.last_uplink
        if uplink:
            self.last_uplink = uplink
        if started - self.last_slow >= 10 and not self.slow_busy:
            self.last_slow = started
            if self.background:
                self.slow_busy = True
                def refresh():
                    try:
                        self._slow_sample(uplink)
                    except Exception as error:
                        report_error(error, 'radio-probes', True)
                    finally:
                        self.slow_busy = False
                threading.Thread(target=refresh, name='wifi-radio-probes', daemon=True).start()
            else:
                self._slow_sample(uplink)
        slow = self.slow
        slow_stamp = slow.get('sampled_at', -math.inf)
        slow_new = slow_stamp != self.slow_seen and slow.get('uplink') == uplink
        if slow_new:
            self.slow_seen = slow_stamp
        now = self.now()
        row = {'type': 'wifi-health', 'utc': stamp(), 'uptime': now,
               'ap': ({'interface': None, 'present': None, 'disabled': True} if sta_only(self.c)
                      else interface_sample(self.c['interface'])), 'sta': interface_sample(uplink),
               'local_api': local_api_sample(self.c), 'slow': slow,
               'broker_log': self.broker_events(),
               'slow_in_progress': self.slow_busy,
               'sample_elapsed_ms': round((self.now() - started) * 1000)}
        self.add(row)
        conditions = [('local-api', row['local_api'].get('ok'), 2)]
        if 'expected_status' in row['local_api']:
            conditions.append(('local-api-http-status', row['local_api']['expected_status'], 2))
        if not sta_only(self.c):
            conditions += [('ap-interface', row['ap'].get('present'), 2)]
        if uplink:
            conditions.append(('sta-interface:' + uplink, row['sta'].get('present'), 2))
        if slow_new:
            for address, probe in slow.get('peer_probes', {}).items():
                conditions.append(('peer-roundtrip:' + address, probe['ok'] if probe['route_ok'] else None, 2))
            for mac, probe in slow.get('ap_probes', {}).items():
                conditions.append(('ap-roundtrip:' + mac, probe['ok'] if probe['route_ok'] else None, 2))
                if probe.get('rtt_ms') is not None:
                    conditions.append(('ap-high-latency:' + mac, probe['rtt_ms'] <= 500, 2))
            conditions.append(('sta-association:' + str(uplink), slow.get('associated'), 2))
            gateway = slow.get('gateway')
            if uplink:
                conditions.append(('sta-default-route:' + uplink, gateway is not None, 2))
            if gateway:
                conditions.append(('sta-gateway:' + str(uplink) + ':' + gateway, slow.get('gateway_ok'), 2))
                latency = (slow.get('gateway_probe') or {}).get('rtt_ms')
                if latency is not None:
                    conditions.append(('sta-high-latency:' + str(uplink), latency <= 500, 2))
            service = slow.get('collector_service', {})
            active = service.get('ActiveState')
            conditions.append(('collector-service', active == 'active' if active else None, 2))
            pid = service.get('MainPID')
            if self.last_pid and pid and pid != '0' and pid != self.last_pid:
                self.trigger('collector-process-replaced', now)
            if pid and pid != '0':
                self.last_pid = pid
        health = row['local_api'].get('health')
        if health:
            for source, state in health['sources'].items():
                if source in ('pico', 'sbms') and isinstance(state, dict) and state.get('state') != 'disabled':
                    conditions.append(('collector-source:' + source, state.get('fresh') is True, 2))
            logging = health['logging']
            conditions.append(('collector-logging', logging.get('state') not in ('failed', 'mapping-error'), 2))
            saved_at = logging.get('savedAt')
            if saved_at != self.last_saved_at:
                self.last_saved_at = saved_at
                self.last_saved_change = now
            conditions.append(('collector-save-progress', now - self.last_saved_change <= 120, 2))
        bad = False
        for key, ok, minimum in conditions:
            required = key in ('local-api', 'local-api-http-status', 'collector-service', 'collector-logging', 'collector-save-progress') or key.startswith('collector-source:')
            onset = self.checks.check(key, ok, now, minimum, required=required)
            if onset is not None:
                bad = True
                self.trigger(key, onset)
        with self.lock:
            if not bad and not self.active and now - self.last_bad >= 60:
                self.latched = False
            if self.active:
                if now >= self.active['onset'] + 45 and len(self.active['snapshots']) < 2:
                    self._extra('post-event')
                if now >= self.active['end']:
                    self.active['post_complete'] = True
                    self._save(True)
                elif now - self.last_checkpoint >= 10:
                    self._save(False)
            if now - self.last_status >= 10:
                write_json(RUNTIME / 'flight-status.json', {'version': VERSION, 'boot': self.boot,
                           'binding': self.signature, 'utc': row['utc'], 'sample': row,
                           'learned': self.checks.values,
                           'fault': self.active['path'] if self.active else None})
                self.last_status = now
        request = RUNTIME / 'incident-request.json'
        if request.exists():
            request.unlink()
            self.trigger('manual-capture', now)

    def journal_event(self, event):
        unit = event.get('_SYSTEMD_UNIT', '')
        kernel = event.get('_TRANSPORT') == 'kernel'
        units = (self.c['unit'], 'NetworkManager.service', 'wpa_supplicant.service',
                 'ella-hotspot-dns.service', 'pico-mqtt.service', 'mosquitto.service', 'pi-wifi.service')
        if not kernel and unit not in units:
            return
        message = clean_message(event.get('MESSAGE', ''))
        now = self.now()
        self.add({'type': 'kernel' if kernel else 'service', 'utc': stamp(), 'uptime': now,
                  'unit': unit, 'priority': str(event.get('PRIORITY', ''))[:8],
                  'journal_monotonic_us': str(event.get('__MONOTONIC_TIMESTAMP', ''))[:24],
                  'journal_utc_us': str(event.get('__REALTIME_TIMESTAMP', ''))[:24], 'message': message})
        critical = (kernel and re.search(r'(?i)(firmware.*(?:crash|halt)|brcmf.*(?:bus is down|timeout|failed|error|out of mem|pktget)|'
                    r'rtl8xxxu.*(?:failed|error)|usb.*(?:disconnect|reset|error)|mmc1.*(?:timeout|inhibit|error))', message))
        if critical:
            self.trigger('kernel-radio-usb-event', now)
        elif unit == 'mosquitto.service' and re.search(
                r'(?i)malformed packet|exceeded timeout|bad socket|already connected, closing old connection|client .+ disconnected|socket error on client', message):
            self.trigger('mqtt-broker-client-error', now)
        elif unit == 'pico-mqtt.service' and re.search(r'"state"\s*:\s*"(?:stale|retrying|failed)"', message):
            self.trigger('collector-source-or-logging-fault', now)

    def follow_journal(self):
        while not self.stop_event.is_set():
            child = None
            try:
                matches = ['_TRANSPORT=kernel']
                for unit in (self.c['unit'], 'NetworkManager.service', 'wpa_supplicant.service',
                             'ella-hotspot-dns.service', 'pico-mqtt.service', 'mosquitto.service', 'pi-wifi.service'):
                    matches.extend(['+', '_SYSTEMD_UNIT=' + unit])
                child = subprocess.Popen(['journalctl', '-f', '-n', '0', '--no-pager', '-o', 'json', *matches],
                                         stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, start_new_session=True)
                os.set_blocking(child.stdout.fileno(), False)
                pending = bytearray()
                second, count = 0, 0
                while not self.stop_event.is_set():
                    if not select.select([child.stdout], [], [], 0.5)[0]:
                        if child.poll() is not None:
                            break
                        continue
                    data = os.read(child.stdout.fileno(), 8192)
                    if not data:
                        break
                    pending.extend(data)
                    while b'\n' in pending:
                        line, _, rest = pending.partition(b'\n')
                        pending = bytearray(rest)
                        tick = int(self.now())
                        if tick != second:
                            second, count = tick, 0
                        if count >= 10 or len(line) > 65536:
                            self.limited_events += 1
                            continue
                        try:
                            value = json.loads(line)
                            # Count relevant events rather than unrelated system journal chatter.
                            if value.get('_TRANSPORT') == 'kernel' or value.get('_SYSTEMD_UNIT') in (
                                    self.c['unit'], 'NetworkManager.service', 'wpa_supplicant.service',
                                    'ella-hotspot-dns.service', 'pico-mqtt.service', 'mosquitto.service', 'pi-wifi.service'):
                                count += 1
                                self.journal_event(value)
                        except (ValueError, TypeError, AttributeError):
                            self.limited_events += 1
                    if len(pending) > 65536:
                        pending.clear()
                        self.limited_events += 1
            except Exception as error:
                report_error(error, 'journal-follow', True)
            finally:
                if child:
                    try:
                        os.killpg(child.pid, signal.SIGKILL)
                    except ProcessLookupError:
                        pass
                    child.wait(timeout=2)
                    child.stdout.close()
            if not self.stop_event.is_set():
                report_error(RuntimeError('Journal event stream exited; retrying'), 'journal-follow', True)
                self.stop_event.wait(5)

    def start(self):
        self.background = True
        def sample_loop():
            while not self.stop_event.is_set():
                at = self.now()
                try:
                    self.sample()
                except Exception as error:
                    report_error(error, 'wifi-diagnostics', True)
                self.stop_event.wait(max(0.1, 1 - (self.now() - at)))
        self.threads = [threading.Thread(target=sample_loop, name='wifi-health', daemon=True),
                        threading.Thread(target=self.follow_journal, name='wifi-journal', daemon=True)]
        for thread in self.threads:
            thread.start()

    def close(self):
        self.stop_event.set()
        with self.lock:
            self.closed = True
            if self.active:
                self.active['post_complete'] = False
                self._save(True)
        for thread in getattr(self, 'threads', []):
            thread.join(timeout=1)


def on_start(c):
    if not sta_only(c):
        power = run(["iw", "dev", c["interface"], "set", "power_save", "off"])
        deadline = time.monotonic() + 10
        answer = control(c, "LOG_LEVEL DEBUG")
        while answer != "OK" and time.monotonic() < deadline and not STOP:
            time.sleep(0.25)
            answer = control(c, "LOG_LEVEL DEBUG")
        safe_ring({"type": "diagnostic-setup", "power_save_off": power["code"] == 0,
              "hostapd_debug": answer == "OK"})
        if power["code"] or answer != "OK":
            notice("AP watch: diagnostic setup incomplete; see events/status.")
    firmware = 'disabled'
    if c.get('firmware_console', True):
        try:
            value = bounded_text(FW_DEBUG, 64).strip()
            if not value:
                firmware = 'unsupported'
            else:
                FW_DEBUG.write_text(str(int(value, 0) | 0x100000))
                firmware = 'enabled'
        except (OSError, ValueError):
            firmware = 'unavailable'
    elif re.fullmatch(r'(?:0x[0-9a-fA-F]+|[0-9]+)', c.get('original_firmware_debug', '')):
        try:
            FW_DEBUG.write_text(str(int(c['original_firmware_debug'], 0)))
        except (OSError, ValueError):
            firmware = 'restore-unavailable'
    safe_ring({'type': 'firmware-console', 'state': firmware})


def check_once(c, identity, policy, counters, old_reason):
    global PHASE
    if sta_only(c):
        raise ValueError('AP policy is unavailable in STA-only topology')
    PHASE = "observe"
    observation, counters = observe(c, counters)
    PHASE = "decide"
    reason, restart = policy.decide(observation, c["mode"])
    PHASE = "sample-log"
    safe_ring({"type": "sample", "reason": reason, "mode": c["mode"],
               "enabled": observation["enabled"], "targets": observation["targets"],
               "stations": observation["stations"]})
    PHASE = "state-write"
    saved = checkpoint(c, identity, policy, reason, observation)
    PHASE = "state-notice"
    if reason != old_reason:
        notice("AP watch: " + reason + " (" + c["mode"] + ")")
    if not restart or not saved:
        return counters, reason, False
    PHASE = "recovery-accounting"
    before = (list(policy.attempts), policy.latched, policy.stable_since, policy.last_good, policy.control_bad)
    policy.recovering(time.monotonic())
    # No AP mutation unless the limiter/latch and baseline were saved successfully.
    if not checkpoint(c, identity, policy, reason, observation):
        # No restart was sent; a transient write failure must not invent an attempt.
        policy.attempts, policy.latched, policy.stable_since, policy.last_good, policy.control_bad = before
        return counters, "recovery-inhibited-state-write", False
    PHASE = "incident-capture"
    try:
        path = snapshot(c, reason, observation)
        notice("AP watch: diagnostics saved to " + path)
    except Exception as error:
        report_error(error, PHASE, True)
        safe_ring({"type": "snapshot-error", "error": str(error)[:1024]})
        notice("AP watch: incident archive failed; continuing bounded recovery.")
    PHASE = "restart-hotspot"
    safe_ring({"type": "recovery-start", "reason": reason})
    result = run(["systemctl", "restart", c["unit"]], timeout=35)
    safe_ring({"type": "recovery-result", **result})
    if result["code"]:
        report_error(RuntimeError("Hotspot restart failed/timed out: " + result["text"][:512]), PHASE, True)
    notice("AP watch: hotspot restart " + ("completed" if result["code"] == 0 else "failed/timed out") +
           "; awaiting real client traffic.")
    return {}, reason, True


def daemon():
    global PHASE, RECORDER
    PHASE = "startup"
    mkdir(RUNTIME)
    lock = (RUNTIME / "lock").open("a")
    fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
    c = config()
    identity = boot_id()
    policy = None if sta_only(c) else load_policy(c, identity, time.monotonic())
    RECORDER = FlightRecorder(c)
    RECORDER.start()
    tap = None if sta_only(c) else Events(c)
    if sta_only(c):
        # Discard old AP status only; keep private incident archives intact.
        (RUNTIME / "state.json").unlink(missing_ok=True)
        safe_ring({"type": "monitor-start", "version": VERSION, "topology": "sta-only", "recovery": False})
        notice("Wi-Fi watch " + VERSION + ": STA-only diagnostics; no network recovery.")
    else:
        safe_ring({"type": "monitor-start", "version": VERSION, "baseline_restored": policy.baseline_restored,
                   "seen_clients": len(policy.seen), "recovery_inhibited": policy.inhibited,
                   "last_good": policy.last_good, "policy": policy.saved()})
        notice("AP watch " + VERSION + ": starting; baseline " + ("restored" if policy.baseline_restored else "requires client observations") + ".")
    on_start(c)
    counters = {}
    old_reason = None
    last_config_read = -math.inf
    while not STOP:
        started = time.monotonic()
        PHASE = "configuration"
        if started - last_config_read >= 10:
            current_config = config()
            if any(current_config.get(k) != c.get(k) for k in ("interface", "unit", "control", "targets", "uplink", "local_api", "topology", "peers")):
                raise ValueError("Target/interface configuration changed; restart watch service")
            c = current_config
            last_config_read = started
        if sta_only(c):
            PHASE = "sta-observe"
            time.sleep(0.5)
            continue
        # Reattach after a hostapd restart without dropping the monitor process.
        PHASE = "event-connect"
        if not tap.alive():
            tap.connect()
        counters, old_reason, restarted = check_once(c, identity, policy, counters, old_reason)
        if restarted:
            tap.close()
            # ExecStartPost applies diagnostics again; no collector operation.
        PHASE = "event-wait"
        end = max(started + 30, time.monotonic() + 1)
        while not STOP and time.monotonic() < end:
            tap.poll(min(1, end - time.monotonic()))
    PHASE = "shutdown"
    safe_ring({"type": "monitor-stop", "reason": "termination requested"})
    if tap is not None:
        tap.close()
    RECORDER.close()
    RECORDER = None
    lock.close()


def managed_write(path, text):
    if path.exists() and not path.read_text().startswith(MARKER):
        raise ValueError("Refusing to replace an unmanaged file: " + str(path))
    path.parent.mkdir(parents=True, exist_ok=True)
    fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_TRUNC | os.O_NOFOLLOW, 0o644)
    with os.fdopen(fd, "w") as output:
        output.write(MARKER + text)


def discover_local_api():
    """Installer only: copy public listen properties, never credentials or TLS keys."""
    command = run(['systemctl', 'show', 'pico-mqtt.service', '-p', 'ExecStart', '--value'], limit=8192)
    if '--api-config' in command['text'] or '--no-api' in command['text']:
        return None
    match = re.search(r'(/[^ ;{}]+/bin/pico\.js)(?:\s|;|$)', command['text'])
    if not match:
        return None
    script = Path(match[1])
    if not script.is_absolute():
        return None
    try:
        value = read_json(script.parents[2] / 'api.json')
        host, port = value.get('host', '127.0.0.1'), value.get('port', 8080)
        if type(port) is not int or not 1 <= port <= 65535:
            return None
        if host not in ('0.0.0.0', '::') and not ipaddress.ip_address(host).is_loopback:
            return None
        return {'host': '127.0.0.1' if host == '0.0.0.0' else '::1' if host == '::' else host,
                'port': port, 'scheme': 'tcp' if value.get('tls') is not None else 'http'}
    except (OSError, ValueError, TypeError, AttributeError, IndexError):
        return None


def install(args):
    for binary in ("ip", "iw", "ping", "systemctl", "journalctl", "systemd-analyze"):
        if not shutil.which(binary):
            raise ValueError("Required command missing: " + binary)
    existing = config() if CONFIG.exists() else None
    interface = args.interface or (existing or {}).get('interface', 'wlan0')
    unit = args.unit or (existing or {}).get('unit', 'ella-hotspot.service')
    control_path = args.control or (existing or {}).get('control', '/run/hostapd-ella')
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
        neighbours = ip_json(["neigh", "show", "dev", interface])
        for row in neighbours:
            if row.get("lladdr"):
                leases.setdefault(row.get("dst"), row["lladdr"].lower())
        local = ip_json(["address", "show", "dev", interface])
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
    c = {"version": 1, "interface": interface, "unit": unit,
         "control": control_path, "targets": targets,
         "mode": existing["mode"] if existing else "recover"}
    if existing and not args.target:
        # An operational-helper update keeps the installation's exact AP/targets/mode.
        c = dict(existing)
    for field in ('interface', 'unit', 'control'):
        if getattr(args, field, None):
            c[field] = getattr(args, field)
    if getattr(args, 'topology', None):
        c['topology'] = args.topology
    if not existing and not sta_only(c):
        c["original_power"] = run(["iw", "dev", c["interface"], "get", "power_save"])["text"].strip()
        c["original_log_level"] = control(c, "LOG_LEVEL")
    elif existing:
        c["original_power"] = existing.get("original_power", "")
        c["original_log_level"] = existing.get("original_log_level", "")
    if getattr(args, 'uplink', None):
        c['uplink'] = args.uplink
    if getattr(args, 'api_port', None) is not None:
        c['local_api'] = {'host': '127.0.0.1', 'port': args.api_port,
                          'scheme': 'http' if getattr(args, 'api_http', False) else 'tcp'}
    elif not c.get('local_api'):
        detected_api = discover_local_api()
        if detected_api:
            c['local_api'] = detected_api
    if 'firmware_console' not in c:
        c['firmware_console'] = True
        c['original_firmware_debug'] = bounded_text(FW_DEBUG, 64).strip()
    if getattr(args, 'no_firmware_console', False):
        c['firmware_console'] = False
    requested_topology = getattr(args, 'topology', None)
    if requested_topology:
        c['topology'] = requested_topology
    if sta_only(c):
        c['mode'] = 'observe'
        if not getattr(args, 'uplink', None) and not (existing and sta_only(existing)):
            raise ValueError('Specify --uplink for STA-only migration')
        c['firmware_console'] = False
    if getattr(args, 'peer_target', None) is not None:
        c['peers'] = args.peer_target
    # Validate before any service/config mutation, using the same rules as runtime.
    if not sta_only(c) and (not 2 <= len(targets) <= 4 or len({t["mac"] for t in targets}) != len(targets)):
        raise ValueError("Supply two to four distinct always-on client addresses")
    if not re.fullmatch(r"[a-zA-Z0-9_-]{1,15}", c["interface"]) or not re.fullmatch(r"[a-zA-Z0-9_.@-]+\.service", c["unit"]):
        raise ValueError("Invalid interface/service name")
    if not c["control"].startswith("/run/") or "\n" in c["control"]:
        raise ValueError("Invalid control directory")
    validate_config(c)
    if not sta_only(c) and (run(["systemctl", "is-active", c["unit"]])["text"].strip() != "active" or pairs(control(c, "STATUS")).get("state") != "ENABLED"):
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
    if sta_only(c):
        dropin.unlink(missing_ok=True)
    else:
        managed_write(dropin, "[Service]\nExecStartPost=-/usr/bin/python3 -u /usr/local/sbin/ella-ap-watch _on-start\n")
    managed_write(SERVICE, """[Unit]
Description=Bounded AP/STA fault recording and hotspot recovery
After=HOTSPOT_UNIT

[Service]
Type=simple
ExecStart=/usr/bin/python3 -u /usr/local/sbin/ella-ap-watch daemon
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
ReadWritePaths=/run/ella-ap-watch /var/lib/ella-ap-watch -/sys/module/brcmfmac/parameters/debug
TimeoutStopSec=10

[Install]
WantedBy=multi-user.target
""".replace("HOTSPOT_UNIT", "NetworkManager.service" if sta_only(c) else c["unit"])
       .replace("Bounded AP/STA fault recording and hotspot recovery",
                "Bounded STA fault recording; no network recovery" if sta_only(c)
                else "Bounded AP/STA fault recording and hotspot recovery"))
    result = run(["systemd-analyze", "verify", str(SERVICE)], timeout=15)
    if result["code"]:
        raise ValueError("Unit verification failed: " + result["text"][:1000])
    for command in (["systemctl", "daemon-reload"], ["systemctl", "enable", "ella-ap-watch.service"],
                    ["systemctl", "restart", "ella-ap-watch.service"]):
        if run(command, timeout=20)["code"]:
            raise ValueError("Watch service installation failed; inspect its journal")
    print("Installed AP watch " + VERSION + ". Hotspot and collector were not restarted.")
    print("AP/STA fault recording enabled: 60 seconds before/after, 5 persistent archives.")
    print("STA-only: no recovery or AP checks." if sta_only(c) else "Existing 30-second AP recovery checks and limits preserved.")
    print("Diagnostic samples aim for 1 second, radio/probes 10 seconds.")
    print("Local API probe: " + (c.get('local_api', {}).get('scheme', 'not configured')))
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
    if not sta_only(c) and match and match[1] in ("EXCESSIVE", "MSGDUMP", "DEBUG", "INFO", "WARNING", "ERROR"):
        control(c, "LOG_LEVEL " + match[1])
    if not sta_only(c) and c.get("original_power") == "Power save: on":
        run(["iw", "dev", c["interface"], "set", "power_save", "on"])
    previous_debug = c.get('original_firmware_debug', '')
    if re.fullmatch(r'(?:0x[0-9a-fA-F]+|[0-9]+)', previous_debug):
        try:
            FW_DEBUG.write_text(str(int(previous_debug, 0)))
        except (OSError, ValueError):
            print('Firmware debug setting could not be restored; a reboot clears the runtime setting.')
    CONFIG.unlink()
    PROGRAM.unlink(missing_ok=True)
    print("Watch and startup drop-in removed. AP/collector not restarted. Incident files retained in " + str(ARCHIVE))


def main():
    os.environ["PATH"] = "/usr/sbin:/usr/bin:/sbin:/bin"
    os.environ["LC_ALL"] = "C"
    os.umask(0o077)
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--version", action="version", version="Ella AP watch " + VERSION)
    parser.add_argument("action", choices=("install", "daemon", "status", "observe", "recover", "capture", "report", "faults", "export", "errors", "reset", "uninstall", "_on-start"))
    parser.add_argument("--topology", choices=('ap-sta', 'sta-only'), help="Explicit radio topology; existing installations preserved unless supplied")
    parser.add_argument("--peer-target", action="append", help="Optional directly reachable sensor IPv4; max four; diagnostic ping only")
    parser.add_argument("--interface", help="AP interface (default wlan0 for a fresh installation)")
    parser.add_argument("--unit", help="AP service (default ella-hotspot.service)")
    parser.add_argument("--control", help="AP control directory (default /run/hostapd-ella)")
    parser.add_argument("--leases", default="/run/ella-hotspot-dns/leases")
    parser.add_argument("--target", action="append")
    parser.add_argument("--uplink", help="Optional explicit wireless client interface; normally discovered")
    parser.add_argument("--api-port", type=int, help="Optional loopback collector TCP probe port")
    parser.add_argument("--api-http", action="store_true", help="Use credential-free HTTP availability probe with --api-port")
    parser.add_argument("--output", help="New private export file; existing files are never overwritten")
    parser.add_argument("--no-firmware-console", action="store_true", help="Restore original firmware-console verbosity; keep other diagnostics")
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
        if sta_only(c) and args.action == 'recover':
            raise ValueError('STA-only never performs recovery; install AP topology explicitly when ready')
        c["mode"] = args.action
        write_json(CONFIG, c)
        print("Mode: " + args.action + "; takes effect within one check cycle.")
    elif args.action == "status":
        c = config()
        print("Monitor version: " + VERSION)
        print("Mode: " + c["mode"])
        print("Topology: " + c.get("topology", "ap-sta"))
        print(run(["systemctl", "is-active", "ella-ap-watch.service"])["text"].strip())
        try:
            if sta_only(c):
                raise ValueError("AP checks disabled")
            s = read_json(RUNTIME / "state.json")
            print("Last check: " + s["observation"]["utc"])
            print("State: " + s["reason"])
            baseline = s.get("baseline", {}).get("seen", [])
            print("Baseline clients seen: " + str(len(baseline)) + "/" + str(len(c["targets"])))
            print("Baseline restored at start: " + ("yes" if s.get("baselineRestored") else "no"))
            print("Recoveries this boot (last 30 minutes): " + str(sum(time.monotonic() - x < 1800 for x in s["policy"]["attempts"])))
            for v in s["observation"]["targets"].values():
                print(v["ip"] + ": " + ("traffic/probe OK" if v["alive"] else "no confirmed traffic"))
        except (OSError, ValueError, KeyError, TypeError, AttributeError):
            print("AP checks and recovery disabled." if sta_only(c) else "No readable runtime status yet; inspect service journal.")
        try:
            flight = read_json(RUNTIME / 'flight-status.json')
            row = flight['sample']
            print('Wi-Fi diagnostics last sample: ' + flight['utc'])
            print('AP: disabled (STA-only)' if sta_only(c) else
                  'AP: ' + str(row['ap'].get('interface')) + ' / phy: ' + str(row['ap'].get('phy')))
            print('STA: ' + str(row['sta'].get('interface')) + ' / phy: ' + str(row['sta'].get('phy')))
            print('Local API availability: ' + str(row['local_api']))
            debug = bounded_text(FW_DEBUG, 64).strip()
            console = bool(int(debug, 0) & 0x100000) if re.fullmatch(r'(?:0x[0-9a-fA-F]+|[0-9]+)', debug) else None
            print('Firmware console diagnostic flag: ' + ('unavailable' if console is None else 'enabled' if console else 'disabled'))
            print('Sensor peer round trips: ' + str(row['slow'].get('peer_probes', {})))
            if not sta_only(c):
                print('Independent AP round trips: ' + str(row['slow'].get('ap_probes', {})))
            print('Active fault capture: ' + str(flight.get('fault') or 'none'))
        except (OSError, ValueError, KeyError, TypeError):
            print('No readable Wi-Fi diagnostic sample yet.')
        try:
            error = read_json(RUNTIME / "last-error.json")
            print("Latest monitor error: " + error["utc"] + " / " + error["phase"] + " / " + error["error"])
            print("Use sudo ella-ap-watch errors for details; this entry may be historical.")
        except (OSError, ValueError, KeyError, TypeError):
            pass
    elif args.action == "errors":
        found = False
        for path in (RUNTIME / "last-error.json", ARCHIVE / "last-error.json"):
            try:
                error = read_json(path)
                print(str(path))
                print(json.dumps(error, indent=2))
                found = True
            except (OSError, ValueError):
                pass
        if not found:
            print("No readable monitor error records.")
    elif args.action == "capture":
        c = config()
        if run(['systemctl', 'is-active', 'ella-ap-watch.service'])['text'].strip() == 'active':
            write_json(RUNTIME / 'incident-request.json', {'utc': stamp()})
            print('Fault window requested. Available prefix is saved on the next diagnostic cycle; tail completes after 60 seconds.')
        elif sta_only(c):
            raise ValueError('Monitor is inactive: start ella-ap-watch.service before requesting a STA fault window')
        else:
            o, _ = observe(c, {})
            print(snapshot(c, "manual-monitor-inactive-instant-only", o))
            print('Monitor is inactive: this is an instantaneous snapshot, without an automatic post-event tail.')
    elif args.action in ('report', 'faults', 'export'):
        files = list(ARCHIVE.glob('incident-*.json.gz')) + list(ARCHIVE.glob('incident-*.pending.json'))
        if not files:
            print("No incident files.")
        elif args.action == 'faults':
            for path in sorted(files, key=lambda p: p.stat().st_mtime_ns, reverse=True):
                print(path.name + (' [collecting/partial]' if path.name.endswith('.pending.json') else ' [saved]'))
        else:
            path = max(files, key=lambda p: p.stat().st_mtime_ns)
            document = read_fault(path)
            if args.action == 'report':
                print(json.dumps(document, indent=2))
            else:
                if not args.output:
                    raise ValueError('Use export --output NEW_FILE; the export contains private network diagnostics')
                fd = os.open(args.output, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW, 0o600)
                with os.fdopen(fd, 'w') as output:
                    json.dump(document, output, indent=2, allow_nan=False)
                    output.write('\n')
                    output.flush()
                    os.fsync(output.fileno())
                    user, group = os.environ.get('SUDO_UID', ''), os.environ.get('SUDO_GID', '')
                    if user.isdigit() and group.isdigit():
                        os.fchown(output.fileno(), int(user), int(group))
                print('Private diagnostic export saved: ' + args.output)
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
        report_error(error, PHASE, True)
        journal("AP watch error [" + PHASE + "]: " + type(error).__name__ + ": " + str(error)[:1024], sys.stderr)
        sys.exit(1)


