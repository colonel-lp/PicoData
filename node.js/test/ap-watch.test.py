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


def observation(t, alive=(), enabled=True, active="active", routed=True):
    return {"uptime": t, "enabled": enabled, "active": active,
            "targets": {m: {"alive": m in alive, "route_ok": routed} for m in MACS}}


def armed():
    p = w.Policy(0)
    p.decide(observation(0, MACS), "recover")
    p.decide(observation(120, MACS), "recover")
    return p


class Decisions(unittest.TestCase):
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
