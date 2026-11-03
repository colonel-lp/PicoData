"""Offline topology migration and STA recorder regressions; no live radio access."""
import argparse
import contextlib
import importlib.util
import io
from pathlib import Path
import tempfile
import unittest
from unittest.mock import Mock, patch

spec = importlib.util.spec_from_file_location('sta_watch', Path(__file__).parents[1] / 'tools/ap-watch.py')
w = importlib.util.module_from_spec(spec)
spec.loader.exec_module(w)
AP = {'version': 1, 'mode': 'recover', 'interface': 'ap0', 'unit': 'fixture-ap.service',
      'control': '/run/fixture', 'targets': [
          {'ip': '192.0.2.10', 'mac': '02:00:00:00:00:01'},
          {'ip': '192.0.2.11', 'mac': '02:00:00:00:00:02'}]}
STA = {**AP, 'topology': 'sta-only', 'mode': 'observe', 'uplink': 'sta0', 'firmware_console': False}

@contextlib.contextmanager
def environment():
    with tempfile.TemporaryDirectory() as directory, contextlib.ExitStack() as stack:
        root = Path(directory)
        for key, value in {'CONFIG': root/'etc/config.json', 'RUNTIME': root/'ram', 'ARCHIVE': root/'disk',
                           'SYSTEMD': root/'units', 'SERVICE': root/'units/ella-ap-watch.service',
                           'PROGRAM': root/'program', 'FW_DEBUG': root/'fw-debug', 'BROKER_LOG': root/'broker.log'}.items():
            stack.enter_context(patch.object(w, key, value))
        stack.enter_context(patch.object(w, 'boot_id', return_value='00000000-0000-0000-0000-000000000001'))
        stack.enter_context(patch.object(w, 'report_error'))
        w.mkdir(w.RUNTIME)
        yield root

class STAWatch(unittest.TestCase):
    def test_sta_config_requires_observe_and_explicit_uplink(self):
        w.validate_config(STA)
        w.validate_config({**STA, 'targets': []})
        for value in ({**STA, 'mode': 'recover'}, {**STA, 'uplink': None},
                      {**STA, 'peers': ['127.0.0.1']}, {**STA, 'peers': ['192.0.2.5']*2}):
            with self.assertRaises(ValueError):
                w.validate_config(value)

    def test_ap_recovery_refuses_sta_config(self):
        with patch.object(w, 'observe') as observe:
            with self.assertRaises(ValueError):
                w.check_once(STA, 'fixture', Mock(), {}, None)
            observe.assert_not_called()
        with patch.object(w, 'config', return_value=STA), patch.object(w.os, 'geteuid', return_value=0), \
             patch('sys.argv', ['watch', 'recover']), patch.object(w, 'write_json') as write:
            with self.assertRaises(ValueError):
                w.main()
            write.assert_not_called()

    def test_sta_start_restores_verbose_flag_without_radio_operations(self):
        with environment(), patch.object(w, 'run') as run, patch.object(w, 'control') as control:
            w.FW_DEBUG.write_text(str(32 | 0x100000))
            w.on_start({**STA, 'original_firmware_debug': '32'})
            self.assertEqual(w.FW_DEBUG.read_text(), '32')
            run.assert_not_called()
            control.assert_not_called()

    def test_migration_and_return_to_new_ap_interface_preserve_archives_and_do_not_restart_radios(self):
        with environment(), contextlib.ExitStack() as stack:
            commands = []
            stack.enter_context(patch.object(w.shutil, 'which', return_value='/fixture'))
            stack.enter_context(patch.object(w, 'discover_local_api', return_value=None))
            stack.enter_context(patch.object(w, 'run', side_effect=lambda a, **kw: commands.append(a) or
                                             {'code': 0, 'text': 'active\n', 'limited': False}))
            control = stack.enter_context(patch.object(w, 'control', return_value='state=ENABLED'))
            stack.enter_context(contextlib.redirect_stdout(io.StringIO()))
            w.write_json(w.CONFIG, AP)
            hook = w.SYSTEMD / 'fixture-ap.service.d/70-ap-watch.conf'
            w.managed_write(hook, '[Service]\n')
            w.mkdir(w.ARCHIVE)
            old = w.ARCHIVE/'incident-fixture.json.gz'
            old.write_bytes(b'fixture archive')
            args = argparse.Namespace(target=None, leases='/fixture', interface=None, unit=None, control=None,
                                      topology='sta-only', uplink='sta0', peer_target=None)
            w.install(args)
            self.assertFalse(hook.exists())
            self.assertEqual(old.read_bytes(), b'fixture archive')
            self.assertEqual(w.config()['targets'], AP['targets'])
            self.assertEqual(w.config()['mode'], 'observe')
            self.assertIn('After=NetworkManager.service', w.SERVICE.read_text())
            control.assert_not_called()
            self.assertFalse(any(a[0] in ('iw', 'ip', 'nmcli', 'nft') for a in commands))
            self.assertFalse(any(a[:2] == ['systemctl', 'restart'] and a[2] != 'ella-ap-watch.service' for a in commands))
            # Default reinstallation must keep STA topology rather than implicitly enable AP.
            args.topology, args.uplink = None, None
            w.install(args)
            self.assertTrue(w.sta_only(w.config()))
            # Future USB AP: explicit topology/interface update uses the new interface.
            args.topology, args.interface, args.uplink = 'ap-sta', 'usbap0', 'sta0'
            w.install(args)
            self.assertEqual(w.config()['interface'], 'usbap0')
            self.assertEqual(w.config()['mode'], 'observe')
            self.assertTrue(hook.exists())
            self.assertIn('After=fixture-ap.service', w.SERVICE.read_text())
            self.assertEqual(old.read_bytes(), b'fixture archive')
            # Fresh STA install must not depend on an old AP installation either.
            w.CONFIG.unlink()
            control.reset_mock()
            commands.clear()
            args.topology, args.interface = 'sta-only', None
            w.install(args)
            self.assertEqual(w.config()['targets'], [])
            self.assertEqual(w.config()['uplink'], 'sta0')
            control.assert_not_called()
            self.assertFalse(any(a[0] in ('iw', 'ip', 'nmcli', 'nft') for a in commands))

    def test_sta_slow_sample_no_ap_query_and_uses_current_neighbour_ip(self):
        with environment(), patch.object(w.FlightRecorder, '_extra'), contextlib.ExitStack() as stack:
            commands = []
            def run(a, **kw):
                commands.append(a)
                text = 'Connected to 02:00:00:00:01:01\n' if a[-1:] == ['link'] else ''
                return {'code': 0, 'text': text, 'limited': False}
            def ip(args):
                if args[0] == 'neigh':
                    return [{'dst': '198.51.100.12', 'lladdr': AP['targets'][0]['mac']},
                            {'dst': '198.51.100.13', 'lladdr': '02:00:00:00:00:ff'}]
                return [{'dev': 'sta0'}]
            stack.enter_context(patch.object(w, 'run', side_effect=run))
            stack.enter_context(patch.object(w, 'ip_json', side_effect=ip))
            stack.enter_context(patch.object(w, 'gateway_for', return_value='198.51.100.1'))
            r = w.FlightRecorder(STA, lambda: 100)
            r._slow_sample('sta0')
            self.assertEqual(r.slow['ap_probes'], {})
            self.assertEqual(r.slow['ap_stations'], {})
            self.assertEqual(set(r.slow['peer_probes']), {'198.51.100.12'})
            self.assertFalse(any('ap0' in a for a in commands))
            self.assertFalse(any(t['ip'] in a for a in commands for t in AP['targets']))
            self.assertTrue(any(a[:3] == ['ping', '-n', '-I'] for a in commands))

    def test_sta_daemon_never_constructs_ap_events_or_policy(self):
        with environment(), contextlib.ExitStack() as stack:
            stack.enter_context(patch.object(w, 'config', return_value=STA))
            recorder = stack.enter_context(patch.object(w, 'FlightRecorder'))
            policy = stack.enter_context(patch.object(w, 'load_policy'))
            events = stack.enter_context(patch.object(w, 'Events'))
            stack.enter_context(patch.object(w, 'on_start'))
            stack.enter_context(patch.object(w, 'notice'))
            def stop(unused):
                w.STOP = True
            stack.enter_context(patch.object(w.time, 'sleep', side_effect=stop))
            stack.enter_context(patch.object(w, 'STOP', False))
            w.daemon()
            policy.assert_not_called()
            events.assert_not_called()
            recorder.return_value.start.assert_called_once()
            recorder.return_value.close.assert_called_once()

    def test_sta_gateway_and_peer_failure_archive_without_ap_conditions(self):
        with environment(), patch.object(w.FlightRecorder, '_extra'), \
             patch.object(w, 'default_wifi', return_value='sta0'), \
             patch.object(w, 'interface_sample', return_value={'present': True}), \
             patch.object(w, 'local_api_sample', return_value={'ok': True}):
            now = [100.0]
            r = w.FlightRecorder(STA, lambda: now[0])
            def slow(unused):
                healthy = now[0] < 120
                r.slow = {'sampled_at': now[0], 'uplink': 'sta0', 'associated': True,
                          'gateway': '198.51.100.1', 'gateway_ok': healthy,
                          'peer_probes': {'198.51.100.12': {'route_ok': True, 'ok': healthy}}}
            r._slow_sample = slow
            for at in (100, 110, 120, 130):
                now[0] = at
                r.sample()
            self.assertIn('sta-gateway:sta0:198.51.100.1', r.active['reasons'])
            self.assertIn('peer-roundtrip:198.51.100.12', r.active['reasons'])
            self.assertFalse(any(k.startswith('ap-') for k in r.checks.values))
            self.assertTrue(r.health[-1]['ap']['disabled'])

    def test_broker_disconnect_and_sbms_staleness_trigger_windows(self):
        with environment(), patch.object(w.FlightRecorder, '_extra'):
            r = w.FlightRecorder(STA, lambda: 100)
            w.BROKER_LOG.write_text('')
            r.broker_events()
            w.BROKER_LOG.write_text('1: Client fixture-sensor disconnected.\n')
            r.broker_events()
            self.assertIn('mqtt-broker-client-error', r.active['reasons'])
            r.journal_event({'_SYSTEMD_UNIT': 'pico-mqtt.service',
                             'MESSAGE': '{"source":"sbms","state":"stale"}'})
            self.assertIn('collector-source-or-logging-fault', r.active['reasons'])

if __name__ == '__main__':
    unittest.main()
