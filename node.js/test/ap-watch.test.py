"""Offline tests only: no live systemd, radio, network, collector or /etc changes."""
import argparse
import contextlib
import gzip
import importlib.util
import io
import json
import os
from pathlib import Path
import socket
import stat
import sys
import tempfile
import threading
import time
import unittest
from unittest.mock import patch

spec = importlib.util.spec_from_file_location("ap_watch", Path(__file__).parents[1] / "tools/ap-watch.py")
w = importlib.util.module_from_spec(spec)
spec.loader.exec_module(w)
MACS = ["02:00:00:00:00:01", "02:00:00:00:00:02"]
TARGETS = [{"ip": "192.0.2." + str(i + 10), "mac": m} for i, m in enumerate(MACS)]
BOOT = "00000000-0000-0000-0000-000000000001"


def fixture_config():
    return {"version": 1, "mode": "recover", "interface": "test0", "unit": "fixture-ap.service",
            "control": "/run/test", "targets": TARGETS}


@contextlib.contextmanager
def private_fixture():
    with tempfile.TemporaryDirectory() as folder, contextlib.ExitStack() as stack:
        root = Path(folder)
        for name, path in {"RUNTIME": root / "ram", "ARCHIVE": root / "disk"}.items():
            stack.enter_context(patch.object(w, name, path))
        stack.enter_context(patch.object(w, "ERROR_TIMES", {}))
        stack.enter_context(patch.object(w, "boot_id", return_value=BOOT))
        stack.enter_context(patch.object(w, "journal", return_value=True))
        w.mkdir(w.RUNTIME)
        yield root


def observation(t, alive=(), enabled=True, active="active", routed=True):
    return {"uptime": t, "enabled": enabled, "active": active,
            "targets": {m: {"alive": m in alive, "route_ok": routed} for m in MACS}}


def armed():
    p = w.Policy(0)
    p.decide(observation(0, MACS), "recover")
    p.decide(observation(120, MACS), "recover")
    return p


class Decisions(unittest.TestCase):
    def test_restart_during_armed_stall_restores_baseline_after_startup_grace(self):
        old = armed()
        old.decide(observation(160), "recover")
        restored = w.Policy(180, old.saved(), old.baseline())
        self.assertTrue(restored.baseline_restored)
        self.assertEqual(restored.decide(observation(299), "recover"), ("startup-grace", False))
        self.assertEqual(restored.decide(observation(300), "recover"), ("all-target-traffic-stalled", True))

    def test_partial_baseline_and_latched_limits_remain_conservative_on_restart(self):
        old = w.Policy(0)
        old.decide(observation(120, MACS[:1]), "recover")
        new = w.Policy(180, old.saved(), old.baseline())
        self.assertEqual(new.decide(observation(600), "recover"), ("waiting-for-client-baseline", False))
        old = armed()
        old.recovering(210)
        new = w.Policy(220, old.saved(), old.baseline())
        self.assertEqual(new.decide(observation(1000), "recover"), ("unresolved-after-recovery-manual-check-needed", False))

    def test_corrupt_baseline_and_inhibition_survive_later_state_saves(self):
        for baseline in ({}, {"seen": MACS, "last_good": 2000},
                         {"seen": MACS, "last_good": float("nan")},
                         {"seen": [MACS[0], MACS[0]], "last_good": 10}):
            bad = w.Policy(1000, {"attempts": [], "latched": False}, baseline)
            self.assertTrue(bad.inhibited)
            self.assertTrue(w.Policy(1100, bad.saved(), bad.baseline()).inhibited)

    def test_startup_grace_and_no_unproven_client_restarts(self):
        p = w.Policy(0)
        self.assertFalse(p.decide(observation(90), "recover")[1])
        self.assertEqual(p.decide(observation(360), "recover"), ("waiting-for-client-baseline", False))

    def test_only_one_sensor_ever_seen_cannot_arm_full_stall_recovery(self):
        p = w.Policy(0)
        p.decide(observation(0, MACS[:1]), "recover")
        self.assertFalse(p.decide(observation(500), "recover")[1])

    def test_real_global_stall_needs_90_seconds(self):
        p = armed()
        self.assertFalse(p.decide(observation(209), "recover")[1])
        self.assertEqual(p.decide(observation(210), "recover"), ("all-target-traffic-stalled", True))

    def test_any_sensor_traffic_prevents_restart(self):
        p = armed()
        self.assertFalse(p.decide(observation(1000, MACS[:1]), "recover")[1])

    def test_healthy_data_prevents_control_socket_only_restart(self):
        p = armed()
        p.decide(observation(120, MACS, enabled=False), "recover")
        self.assertFalse(p.decide(observation(500, MACS, enabled=False), "recover")[1])

    def test_persistent_control_stall_without_baseline(self):
        p = w.Policy(0)
        p.decide(observation(30, enabled=False), "recover")
        self.assertEqual(p.decide(observation(120, enabled=False), "recover"), ("hostapd-control-stall", True))

    def test_wrong_routes_do_not_trigger_radio_recovery(self):
        p = armed()
        self.assertEqual(p.decide(observation(800, routed=False), "recover"), ("route-problem-no-radio-restart", False))

    def test_deliberately_stopped_hotspot_pauses(self):
        p = armed()
        self.assertEqual(p.decide(observation(800, enabled=False, active="inactive"), "recover"), ("paused-hotspot-not-active", False))

    def test_observe_mode_does_not_restart(self):
        self.assertEqual(armed().decide(observation(210), "observe"), ("all-target-traffic-stalled", False))

    def test_failed_recovery_latches_until_90_seconds_of_data(self):
        p = armed()
        p.recovering(210)
        self.assertFalse(p.decide(observation(1000), "recover")[1])
        p.decide(observation(1100, MACS), "recover")
        p.decide(observation(1189, MACS), "recover")
        self.assertTrue(p.latched)
        p.decide(observation(1190, MACS), "recover")
        self.assertFalse(p.latched)

    def test_ten_minute_cooldown_and_two_per_30_minute_limit(self):
        p = armed()
        p.recovering(210)
        p.decide(observation(240, MACS), "recover")
        p.decide(observation(330, MACS), "recover")
        self.assertEqual(p.decide(observation(420), "recover"), ("recovery-rate-limited", False))
        self.assertTrue(p.decide(observation(900), "recover")[1])
        p.recovering(900)
        p.decide(observation(930, MACS), "recover")
        p.decide(observation(1020, MACS), "recover")
        self.assertEqual(p.decide(observation(1600), "recover"), ("recovery-rate-limited", False))
        self.assertTrue(p.decide(observation(2011), "recover")[1])

    def test_valid_saved_limits_survive_monitor_restart(self):
        p = w.Policy(1000, {"attempts": [210, 900], "latched": True})
        self.assertEqual(p.saved(), {"attempts": [210, 900], "latched": True})
        self.assertFalse(p.inhibited)

    def test_corrupt_or_future_saved_state_fails_closed(self):
        for saved in ({}, {"attempts": [float("nan")], "latched": False},
                      {"attempts": [9999], "latched": False}, {"attempts": [], "latched": 0}):
            self.assertTrue(w.Policy(100, saved).inhibited)


class BoundsAndParsing(unittest.TestCase):
    def test_broken_journal_pipe_does_not_cause_python_cleanup_exit_120(self):
        import subprocess
        code = '''import importlib.util, os, sys
spec = importlib.util.spec_from_file_location("watch", sys.argv[1])
w = importlib.util.module_from_spec(spec)
spec.loader.exec_module(w)
reader, writer = os.pipe()
os.close(reader)
sys.stdout = os.fdopen(writer, "w")
assert w.journal("fixture warning") is False
'''
        result = subprocess.run([sys.executable, "-c", code, str(Path(w.__file__))], capture_output=True, timeout=5)
        self.assertEqual(result.returncode, 0, result.stderr)

    def test_full_journal_pipe_is_nonblocking(self):
        reader, writer = os.pipe()
        try:
            os.set_blocking(writer, False)
            with os.fdopen(writer, "w") as stream:
                while True:
                    try:
                        os.write(writer, b"x" * 4096)
                    except BlockingIOError:
                        break
                at = time.monotonic()
                self.assertFalse(w.journal("fixture warning", stream))
                self.assertLess(time.monotonic() - at, 1)
        finally:
            os.close(reader)

    def test_rx_progress_is_health_even_when_client_does_not_answer_ping(self):
        dump = "Station 02:00:00:00:00:01 (on test0)\n\trx bytes: 20\n\trx packets: 2\n"
        def command(argv, **kwargs):
            text = "active\n" if argv[0] == "systemctl" else dump
            return {"code": 0 if argv[0] != "ping" else 1, "text": text, "limited": False}
        c = {"interface": "test0", "unit": "test.service", "targets": TARGETS}
        with patch.object(w, "control", return_value="state=ENABLED"), \
             patch.object(w, "run", side_effect=command) as commands, \
             patch.object(w, "ip_json", return_value=[{"dev": "test0"}]):
            value, counters = w.observe(c, {MACS[0]: {"rx bytes": 10, "rx packets": 1}})
        self.assertTrue(value["targets"][MACS[0]]["alive"])
        self.assertFalse(value["targets"][MACS[1]]["alive"])
        self.assertEqual(counters[MACS[0]]["rx bytes"], 20)
        probes = [call.args[0] for call in commands.call_args_list if call.args[0][0] == "ping"]
        self.assertEqual(probes, [["ping", "-n", "-I", "test0", "-c", "1", "-W", "2", TARGETS[1]["ip"]]])

    def test_probe_is_interface_bound_and_skipped_on_wrong_route(self):
        c = {"interface": "test0", "unit": "test.service", "targets": TARGETS}
        with patch.object(w, "control", return_value="state=ENABLED"), \
             patch.object(w, "run", return_value={"code": 0, "text": "active", "limited": False}) as commands, \
             patch.object(w, "ip_json", return_value=[{"dev": "other0", "gateway": "192.0.2.1"}]):
            value, _ = w.observe(c, {})
        self.assertFalse(any(call.args[0][0] == "ping" for call in commands.call_args_list))
        self.assertTrue(all(not v["route_ok"] for v in value["targets"].values()))

    def test_process_output_is_limited(self):
        result = w.run([sys.executable, "-c", "print('x'*200000)"], limit=100)
        self.assertTrue(result["limited"])
        self.assertEqual(len(result["text"]), 100)

    def test_hanging_command_is_killed(self):
        start = time.monotonic()
        self.assertTrue(w.run([sys.executable, "-c", "import time; time.sleep(5)"], timeout=0.05)["limited"])
        self.assertLess(time.monotonic() - start, 2)

    def test_station_parse(self):
        value = w.stations("Station 02:00:00:00:00:01 (on test0)\n\trx bytes: 123\n\trx packets: 9\n\ttx failed: 3\n")
        self.assertEqual(value[MACS[0]], {"rx bytes": 123, "rx packets": 9, "tx failed": 3})

    def test_configuration_rejects_duplicate_targets(self):
        c = {"version": 1, "mode": "recover", "interface": "test0", "unit": "test.service",
             "control": "/run/test", "targets": [TARGETS[0], TARGETS[0]]}
        with self.assertRaises(ValueError):
            w.validate_config(c)

    def test_unmanaged_files_are_not_replaced(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "file"
            path.write_text("owner configuration\n")
            with self.assertRaises(ValueError):
                w.managed_write(path, "changed")
            self.assertEqual(path.read_text(), "owner configuration\n")


@unittest.skipUnless(os.geteuid() == 0, "private root-owned filesystem helpers tested in isolated root workspace")
class PrivateFiles(unittest.TestCase):
    def test_baseline_is_bound_to_boot_and_exact_targets_and_legacy_keeps_limits(self):
        with private_fixture():
            c = fixture_config()
            old = armed()
            self.assertTrue(w.checkpoint(c, BOOT, old, "suspect", observation(160)))
            same = w.load_policy(c, BOOT, 180)
            self.assertTrue(same.baseline_restored)
            self.assertEqual(same.decide(observation(300), "recover"), ("all-target-traffic-stalled", True))
            reboot = w.load_policy(c, "00000000-0000-0000-0000-000000000002", 180)
            self.assertFalse(reboot.baseline_restored)
            self.assertEqual(reboot.decide(observation(500), "recover"), ("waiting-for-client-baseline", False))
            changed = fixture_config()
            changed["targets"] = [dict(t) for t in TARGETS]
            changed["targets"][0]["ip"] = "192.0.2.55"
            old.recovering(170)
            w.checkpoint(c, BOOT, old, "recovery", observation(170))
            mismatched = w.load_policy(changed, BOOT, 180)
            self.assertFalse(mismatched.baseline_restored)
            self.assertEqual(mismatched.attempts, [170])
            w.write_json(w.RUNTIME / "state.json", {"boot": BOOT, "policy": old.saved()})
            legacy = w.load_policy(c, BOOT, 180)
            self.assertFalse(legacy.baseline_restored)
            self.assertEqual(legacy.attempts, [170])

    def test_invalid_bound_baseline_stays_inhibited_after_reopen(self):
        with private_fixture():
            c = fixture_config()
            w.checkpoint(c, BOOT, armed(), "traffic-ok", observation(120))
            state = w.read_json(w.RUNTIME / "state.json")
            state["baseline"]["seen"] = ["02:00:00:00:00:ff"]
            w.write_json(w.RUNTIME / "state.json", state)
            invalid = w.load_policy(c, BOOT, 180)
            self.assertTrue(invalid.inhibited)
            w.checkpoint(c, BOOT, invalid, "inhibited", observation(200))
            self.assertTrue(w.load_policy(c, BOOT, 220).inhibited)

    def test_broken_journal_and_archive_failure_do_not_prevent_accounted_recovery(self):
        with private_fixture(), patch.object(w, "journal", return_value=False):
            c, policy = fixture_config(), armed()
            o = {**observation(210), "stations": {}}
            with patch.object(w, "observe", return_value=(o, {})), \
                 patch.object(w, "snapshot", side_effect=OSError("fixture archive unavailable")), \
                 patch.object(w, "run", return_value={"code": 0, "text": "", "limited": False}) as command, \
                 patch.object(w.time, "monotonic", return_value=210):
                _, _, restarted = w.check_once(c, BOOT, policy, {}, "traffic-ok")
            self.assertTrue(restarted)
            command.assert_called_once_with(["systemctl", "restart", "fixture-ap.service"], timeout=35)
            saved = w.read_json(w.RUNTIME / "state.json")
            self.assertEqual(saved["policy"]["attempts"], [210])
            self.assertTrue(saved["policy"]["latched"])
            self.assertEqual(saved["baseline"]["seen"], MACS)
            self.assertIn('"type": "recovery-result"', (w.RUNTIME / "events.jsonl").read_text())

    def test_failed_state_or_recovery_accounting_never_restarts_ap(self):
        for fail_at in (1, 2):
            with self.subTest(fail_at=fail_at), private_fixture():
                c, policy = fixture_config(), armed()
                original = w.write_json
                attempts = []
                def write(path, value):
                    if path.name == "state.json":
                        attempts.append(path)
                        if len(attempts) == fail_at:
                            raise OSError("fixture RAM write unavailable")
                    return original(path, value)
                with patch.object(w, "write_json", side_effect=write), \
                     patch.object(w, "observe", return_value=({**observation(210), "stations": {}}, {})), \
                     patch.object(w, "run") as command, \
                     patch.object(w.time, "monotonic", return_value=210):
                    self.assertFalse(w.check_once(c, BOOT, policy, {}, "traffic-ok")[2])
                command.assert_not_called()
                self.assertEqual(policy.attempts, [])
                self.assertFalse(policy.latched)
                self.assertEqual(w.read_json(w.ARCHIVE / "last-error.json")["phase"], "state-write")

    def test_event_ring_failure_does_not_stop_recovery_when_accounting_is_saved(self):
        with private_fixture(), patch.object(w, "ring", side_effect=OSError("fixture event log unavailable")), \
             patch.object(w, "observe", return_value=({**observation(210), "stations": {}}, {})), \
             patch.object(w, "snapshot", return_value="fixture.gz"), \
             patch.object(w, "run", return_value={"code": 0, "text": "", "limited": False}) as command, \
             patch.object(w.time, "monotonic", return_value=210):
            self.assertTrue(w.check_once(fixture_config(), BOOT, armed(), {}, "traffic-ok")[2])
            command.assert_called_once()

    def test_error_evidence_contains_traceback_and_disk_write_is_rate_limited(self):
        with private_fixture(), patch.object(w.time, "monotonic", return_value=500):
            try:
                raise OSError("fixture state failure")
            except OSError as error:
                w.report_error(error, "state-write", True)
            path = w.ARCHIVE / "last-error.json"
            first = path.read_bytes()
            w.report_error(RuntimeError("later failure"), "observe", True)
            self.assertEqual(path.read_bytes(), first)
            detail = json.loads(first)
            self.assertEqual(detail["phase"], "state-write")
            self.assertIn("Traceback", detail["traceback"])
            self.assertLess(len(first), 16384)
            self.assertEqual(stat.S_IMODE(path.stat().st_mode), 0o600)
            with patch.object(w.time, "monotonic", return_value=801):
                w.report_error(RuntimeError("new failure"), "observe", True)
            self.assertEqual(w.read_json(path)["message"], "new failure")

    def test_non_ascii_error_evidence_is_byte_bounded(self):
        with private_fixture():
            w.report_error(ValueError("\U0001f6d1" * 20000), "fixture", True)
            for path in (w.RUNTIME / "last-error.json", w.ARCHIVE / "last-error.json"):
                self.assertLessEqual(path.stat().st_size, 16385)
                self.assertTrue(w.read_json(path)["truncated"])

    def test_error_disk_limit_survives_process_state_reset_and_missing_boot_id(self):
        with private_fixture(), patch.object(w, "boot_id", side_effect=OSError("fixture boot identity unavailable")):
            with patch.object(w.time, "monotonic", return_value=500):
                w.report_error(RuntimeError("first failure"), "startup", True)
            path = w.ARCHIVE / "last-error.json"
            first = path.read_bytes()
            w.ERROR_TIMES.clear()  # A restarted monitor has fresh Python globals.
            with patch.object(w.time, "monotonic", return_value=510):
                w.report_error(RuntimeError("another failure"), "startup", True)
            self.assertEqual(path.read_bytes(), first)

    def test_busy_error_archive_lock_does_not_block_monitor(self):
        with private_fixture():
            w.mkdir(w.ARCHIVE)
            with (w.ARCHIVE / "error.lock").open("a") as lock:
                w.fcntl.flock(lock, w.fcntl.LOCK_EX | w.fcntl.LOCK_NB)
                w.report_error(RuntimeError("fixture error"), "observe", True)
            self.assertTrue((w.RUNTIME / "last-error.json").exists())
            self.assertFalse((w.ARCHIVE / "last-error.json").exists())

    def test_installer_uses_additive_hook_without_restarting_ap_or_collector(self):
        with tempfile.TemporaryDirectory() as folder, contextlib.ExitStack() as stack:
            root = Path(folder)
            replacements = {"CONFIG": root / "etc/config.json", "RUNTIME": root / "ram",
                            "ARCHIVE": root / "disk", "SYSTEMD": root / "units",
                            "SERVICE": root / "units/ella-ap-watch.service", "PROGRAM": root / "installed"}
            for name, path in replacements.items():
                stack.enter_context(patch.object(w, name, path))
            leases = root / "leases"
            leases.write_text("\n".join("0 " + t["mac"] + " " + t["ip"] + " fixture *" for t in TARGETS))
            def ip_result(args):
                if args[0] == "neigh":
                    return []
                return [{"addr_info": [{"local": "192.0.2.1", "prefixlen": 24, "family": "inet"}]}]
            commands = []
            def fake_run(args, **kwargs):
                commands.append(args)
                text = "active\n" if args[:2] == ["systemctl", "is-active"] else "Power save: off\n"
                return {"code": 0, "text": text, "limited": False}
            stack.enter_context(patch.object(w, "ip_json", side_effect=ip_result))
            stack.enter_context(patch.object(w, "run", side_effect=fake_run))
            stack.enter_context(patch.object(w, "control", side_effect=lambda c, op: "state=ENABLED" if op == "STATUS" else "Current level: INFO"))
            stack.enter_context(patch.object(w.shutil, "which", return_value="/fixture"))
            stack.enter_context(contextlib.redirect_stdout(io.StringIO()))
            w.install(argparse.Namespace(target=[t["ip"] for t in TARGETS], leases=str(leases),
                                         interface="test0", unit="fixture-ap.service", control="/run/test"))
            self.assertEqual(w.config()["targets"], TARGETS)
            hook = (root / "units/fixture-ap.service.d/70-ap-watch.conf").read_text()
            self.assertIn("ExecStartPost=", hook)
            self.assertNotIn("ExecStart=", hook)
            self.assertFalse(any("pico" in str(cmd) for cmd in commands))
            self.assertNotIn(["systemctl", "restart", "fixture-ap.service"], commands)
            self.assertIn("After=fixture-ap.service", w.SERVICE.read_text())
            self.assertIn("ExecStart=/usr/bin/python3 -u ", w.SERVICE.read_text())
            # Updating the helper without --target must preserve the exact installation.
            installed = w.config()
            installed["mode"] = "observe"
            w.write_json(w.CONFIG, installed)
            (w.RUNTIME / "state.json").write_text("fixture retained state")
            w.install(argparse.Namespace(target=None, leases=str(leases), interface="wlan0",
                                        unit="ella-hotspot.service", control="/run/hostapd-ella"))
            self.assertEqual(w.config(), installed)
            self.assertEqual((w.RUNTIME / "state.json").read_text(), "fixture retained state")
            self.assertNotIn(["systemctl", "restart", "fixture-ap.service"], commands)
            # A real systemd parser can verify the generated unit without running it.
            output = subprocess_verify(w.SERVICE)
            self.assertEqual(output.returncode, 0, output.stdout)

    def test_config_write_preserves_shared_directory_permissions(self):
        with tempfile.TemporaryDirectory() as folder:
            parent = Path(folder)
            parent.chmod(0o755)
            path = parent / "file.json"
            w.write_json(path, {"safe": True})
            self.assertEqual(stat.S_IMODE(parent.stat().st_mode), 0o755)
            self.assertEqual(stat.S_IMODE(path.stat().st_mode), 0o600)

    def test_ram_ring_rotation_and_valid_json(self):
        with tempfile.TemporaryDirectory() as folder, patch.object(w, "RUNTIME", Path(folder)):
            (Path(folder) / "events.jsonl").write_bytes(b"x" * (2 * 1024 * 1024))
            w.ring({"type": "test", "text": "x" * 20000})
            self.assertTrue((Path(folder) / "events.previous.jsonl").exists())
            self.assertTrue(json.loads((Path(folder) / "events.jsonl").read_text())["truncated"])

    def test_snapshots_keep_five_and_do_not_save_secrets(self):
        with tempfile.TemporaryDirectory() as folder, \
             patch.object(w, "RUNTIME", Path(folder) / "ram"), \
             patch.object(w, "ARCHIVE", Path(folder) / "disk"), \
             patch.object(w, "run", return_value={"code": 0, "text": "fixture", "limited": False}) as command:
            c = {"interface": "test0", "unit": "test.service"}
            for _ in range(7):
                path = w.snapshot(c, "fixture", {})
            self.assertEqual(len(list(w.ARCHIVE.glob("*.gz"))), 5)
            self.assertLess(Path(path).stat().st_size, 2 * 1024 * 1024)
            self.assertEqual(stat.S_IMODE(Path(path).stat().st_mode), 0o600)
            with gzip.open(path, "rt") as source:
                self.assertEqual(json.load(source)["reason"], "fixture")
            commands = [args[0][0] for args in command.call_args_list]
            self.assertFalse(any("mqtt" in str(cmd) or "api.json" in str(cmd) or "hostapd.conf" in str(cmd) for cmd in commands))

    def test_control_protocol_ignores_events_before_reply_mock_socket(self):
        with tempfile.TemporaryDirectory() as folder, patch.object(w, "RUNTIME", Path(folder)), \
             patch.object(w.socket, "socket") as factory:
            client = factory.return_value
            client.recv.side_effect = [b"<2>AP-STA-CONNECTED fixture", b"PONG\n"]
            self.assertEqual(w.control({"control": "/run/test", "interface": "test0"}, "PING"), "PONG")
            client.send.assert_called_once_with(b"PING")
            client.close.assert_called_once()

    def test_event_socket_reconnects_after_hostapd_replacement(self):
        tap = w.Events({})
        from unittest.mock import Mock
        tap.connection = Mock()
        tap.connection.request.side_effect = ConnectionRefusedError()
        self.assertFalse(tap.alive())


def subprocess_verify(path):
    import subprocess
    return subprocess.run(["systemd-analyze", "verify", str(path)], text=True,
                          stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=10)


if __name__ == "__main__":
    unittest.main()
