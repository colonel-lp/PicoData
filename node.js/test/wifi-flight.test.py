"""Synthetic offline diagnostic-window tests; never use owner traffic or live radios."""
import contextlib
import gzip
import importlib.util
import json
import os
from pathlib import Path
import tempfile
import threading
import time
import io
import unittest
from unittest.mock import Mock, patch

spec = importlib.util.spec_from_file_location('flight_watch', Path(__file__).parents[1] / 'tools/ap-watch.py')
w = importlib.util.module_from_spec(spec)
spec.loader.exec_module(w)
REPORT_ERROR = w.report_error
BOOT = '00000000-0000-0000-0000-000000000001'
MACS = ['02:00:00:00:00:01', '02:00:00:00:00:02']
C = {'version': 1, 'mode': 'recover', 'interface': 'ap0', 'unit': 'fixture.service',
     'control': '/run/fixture', 'targets': [{'ip': '192.0.2.' + str(10+i), 'mac': m} for i, m in enumerate(MACS)]}


@contextlib.contextmanager
def fixture():
    with tempfile.TemporaryDirectory() as folder, contextlib.ExitStack() as stack:
        root = Path(folder)
        stack.enter_context(patch.object(w, 'RUNTIME', root / 'ram'))
        stack.enter_context(patch.object(w, 'ARCHIVE', root / 'disk'))
        stack.enter_context(patch.object(w, 'boot_id', return_value=BOOT))
        stack.enter_context(patch.object(w, 'BROKER_LOG', root / 'broker.log'))
        stack.enter_context(patch.object(w, 'report_error'))
        stack.enter_context(patch.object(w.FlightRecorder, '_extra'))
        w.mkdir(w.RUNTIME)
        yield root


def add_health(recorder, at, api=True):
    recorder.add({'type': 'wifi-health', 'utc': 'synthetic', 'uptime': at,
                  'ap': {'present': True}, 'local_api': {'ok': api}})



class Windows(unittest.TestCase):
    def test_broker_file_follows_new_lines_and_redacts_without_replaying_startup(self):
        with fixture():
            w.BROKER_LOG.write_text('1: old fixture line\n')
            r = w.FlightRecorder(C, lambda: 100)
            self.assertTrue(r.broker_events()['baseline'])
            self.assertFalse(r.events)
            with w.BROKER_LOG.open('a') as out:
                out.write('2: fixture password="synthetic secret"\n3: malformed packet\n')
            self.assertTrue(r.broker_events()['available'])
            self.assertEqual(len(r.events), 2)
            self.assertNotIn('synthetic secret', str(list(r.events)))
            self.assertIn('mqtt-broker-client-error', r.active['reasons'])

    def test_broker_file_rotation_and_missing_file_are_explicit(self):
        with fixture():
            r = w.FlightRecorder(C, lambda: 100)
            self.assertFalse(r.broker_events()['available'])
            w.BROKER_LOG.write_text('1: fixture baseline\n')
            r.broker_events()
            w.BROKER_LOG.rename(w.BROKER_LOG.with_suffix('.old'))
            w.BROKER_LOG.write_text('2: fixture reconnected\n')
            self.assertTrue(r.broker_events()['available'])
            self.assertTrue(any('reconnected' in e['message'] for e in r.events))

    def test_broker_file_flood_is_bounded_and_flagged(self):
        with fixture():
            w.BROKER_LOG.write_text('')
            r = w.FlightRecorder(C, lambda: 100)
            r.broker_events()
            w.BROKER_LOG.write_text(('fixture ' + 'x'*100 + '\n') * 1000)
            self.assertTrue(r.broker_events()['limited'])
            self.assertLessEqual(len(r.events), 32)
            self.assertGreater(r.limited_events, 0)

    def test_critical_monitor_error_saves_context_without_recursive_failure(self):
        with fixture():
            now = [100.0]
            r = w.FlightRecorder(C, lambda: now[0])
            for t in range(40, 101):
                add_health(r, t)
            with patch.object(w, 'RECORDER', r), patch.object(w.time, 'monotonic', return_value=100), \
                 patch.dict(w.ERROR_TIMES, {}, clear=True):
                REPORT_ERROR(RuntimeError('fixture failure'), 'fixture-critical', True)
                saved = w.read_fault(Path(r.active['path']))
                self.assertTrue(saved['pre_complete'])
                self.assertIn('monitor-error:fixture-critical', saved['reasons'])
                self.assertTrue(any(e['type'] == 'monitor-error' for e in saved['events']))
                with patch.object(r, 'trigger', side_effect=OSError('fixture storage unavailable')):
                    REPORT_ERROR(RuntimeError('fixture failure'), 'fixture-archive-failed', True)
                    self.assertFalse(getattr(w.ERROR_CAPTURE, 'active', False))

    def test_fault_prefix_survives_later_ram_event_rotation(self):
        with fixture():
            now = [100.0]
            r = w.FlightRecorder(C, lambda: now[0])
            for t in range(40, 101):
                add_health(r, t)
            r.add({'type': 'kernel', 'uptime': 99, 'message': 'initial-cause-marker'})
            r.trigger('fixture')
            now[0] = 130
            for i in range(1000):
                r.add({'type': 'service', 'uptime': 130, 'message': 'aftermath-' + str(i)})
            self.assertFalse(any(e.get('message') == 'initial-cause-marker' for e in r.events))
            r._save(False)
            saved = w.read_fault(Path(r.active['path']))
            self.assertTrue(any(e.get('message') == 'initial-cause-marker' for e in saved['events']))
            self.assertEqual(saved['health'][0]['uptime'], 40)

    def test_normal_sampler_completes_window_and_suppresses_repeat_episode(self):
        with fixture(), patch.object(w, 'default_wifi', return_value='sta0'), \
             patch.object(w, 'interface_sample', side_effect=lambda n: {'interface': n, 'present': True}), \
             patch.object(w, 'local_api_sample', return_value={'ok': True}):
            now = [0.0]
            r = w.FlightRecorder(C, lambda: now[0])
            def slow(unused):
                r.slow = {'sampled_at': now[0], 'uplink': 'sta0', 'ap_probes': {},
                          'collector_service': {}, 'gateway': None, 'associated': None}
            r._slow_sample = slow
            for at in range(101):
                now[0] = at
                r.sample()
            p = Path(r.trigger('fixture', 100))
            for at in range(101, 160):
                now[0] = at
                r.sample()
            self.assertTrue(p.exists())
            self.assertFalse(w.read_fault(p)['post_complete'])
            now[0] = 160
            r.sample()
            self.assertFalse(p.exists())
            v = w.read_fault(next(w.ARCHIVE.glob('*.gz')))
            self.assertTrue(v['pre_complete'])
            self.assertTrue(v['post_complete'])
            self.assertEqual(v['health'][0]['uptime'], 40)
            self.assertEqual(v['health'][-1]['uptime'], 160)

    def test_prefix_is_immediately_durable_and_full_tail_is_retained(self):
        with fixture():
            now = [0.0]
            r = w.FlightRecorder(C, lambda: now[0])
            for t in range(0, 102, 2):
                now[0] = t
                add_health(r, t)
            path = Path(r.trigger('fixture-fault', 100))
            self.assertTrue(path.exists())
            prefix = w.read_fault(path)
            self.assertTrue(prefix['pre_complete'])
            self.assertFalse(prefix['post_complete'])
            self.assertEqual(prefix['health'][0]['uptime'], 40)
            self.assertEqual(os.stat(path).st_mode & 0o777, 0o600)
            for t in range(102, 162, 2):
                now[0] = t
                add_health(r, t)
            r.active['post_complete'] = True
            r._save(True)
            self.assertFalse(path.exists())
            result = w.read_fault(next(w.ARCHIVE.glob('*.gz')))
            self.assertEqual(result['state'], 'complete')
            self.assertEqual([x['uptime'] for x in result['health']], list(range(40, 162, 2)))

    def test_confirmation_delay_uses_first_observed_onset(self):
        with fixture():
            now = [0.0]
            r = w.FlightRecorder(C, lambda: now[0])
            for t in range(0, 182, 2):
                now[0] = t
                add_health(r, t)
            p = Path(r.trigger('confirmed-later', 100))
            v = w.read_fault(p)
            self.assertEqual(v['confirmed_at'], 180)
            self.assertEqual(v['onset'], 100)
            self.assertEqual(v['health'][0]['uptime'], 40)
            self.assertEqual(v['health'][-1]['uptime'], 160)

    def test_short_prebuffer_is_marked_incomplete(self):
        with fixture():
            now = [20.0]
            r = w.FlightRecorder(C, lambda: now[0])
            add_health(r, 20)
            p = Path(r.trigger('early'))
            self.assertFalse(w.read_fault(p)['pre_complete'])

    def test_restart_recovers_durable_prefix_as_interrupted_not_complete(self):
        with fixture():
            r = w.FlightRecorder(C, lambda: 100)
            add_health(r, 90)
            p = Path(r.trigger('fault'))
            w.FlightRecorder(C, lambda: 120)
            self.assertFalse(p.exists())
            v = w.read_fault(next(w.ARCHIVE.glob('*.gz')))
            self.assertEqual(v['state'], 'interrupted')
            self.assertFalse(v['post_complete'])
            self.assertEqual(v['health'][0]['uptime'], 90)

    def test_shutdown_saves_partial_tail(self):
        with fixture():
            r = w.FlightRecorder(C, lambda: 100)
            add_health(r, 100)
            r.trigger('fault')
            r.close()
            v = w.read_fault(next(w.ARCHIVE.glob('*.gz')))
            self.assertFalse(v['post_complete'])
            self.assertEqual(v['state'], 'interrupted')

    def test_repeated_faults_coalesce_and_unresolved_episode_is_latched(self):
        with fixture():
            r = w.FlightRecorder(C, lambda: 100)
            first = r.trigger('fault-a')
            self.assertEqual(r.trigger('fault-b'), first)
            self.assertEqual(r.active['reasons'], ['fault-a', 'fault-b'])
            r._save(True)
            r.trigger('fault-a')
            self.assertEqual(len(list(w.ARCHIVE.glob('incident-*'))), 1)

    def test_retention_is_five_archives_and_atomic_temporary_files_are_removed(self):
        with fixture():
            for i in range(8):
                w.write_fault(w.ARCHIVE / ('incident-' + str(i) + '.json.gz'), {'fixture': i}, True)
                w.prune_faults()
            self.assertEqual(len(list(w.ARCHIVE.glob('*.gz'))), 5)
            self.assertFalse(list(w.ARCHIVE.glob('.fault-*')))

    def test_event_flood_cannot_evict_health_prebuffer(self):
        with fixture():
            r = w.FlightRecorder(C, lambda: 100)
            for t in range(0, 102, 2):
                add_health(r, t)
            for i in range(1000):
                r.add({'type': 'service', 'uptime': 100, 'message': 'fixture-' + str(i)})
            v = w.read_fault(Path(r.trigger('flood')))
            self.assertEqual(v['health'][0]['uptime'], 40)
            self.assertLessEqual(len(v['events']), 512)

    def test_disk_failure_does_not_perform_any_recovery_or_network_mutation(self):
        with fixture(), patch.object(w, 'write_fault', side_effect=OSError('fixture storage unavailable')), \
             patch.object(w, 'run') as commands:
            r = w.FlightRecorder(C, lambda: 100)
            with self.assertRaises(OSError):
                r.trigger('fault')
            commands.assert_not_called()


class Health(unittest.TestCase):
    def test_slow_radio_commands_do_not_block_fast_local_api_samples(self):
        with fixture(), patch.object(w, 'default_wifi', return_value='sta0'), \
             patch.object(w, 'interface_sample', return_value={'present': True}), \
             patch.object(w, 'local_api_sample', return_value={'ok': True}) as api:
            r = w.FlightRecorder(C)
            blocked = threading.Event()
            r._slow_sample = lambda unused: blocked.wait(4)
            r.follow_journal = lambda: r.stop_event.wait(4)
            try:
                r.start()
                deadline = time.monotonic() + 3
                while api.call_count < 2 and time.monotonic() < deadline:
                    time.sleep(0.02)
                self.assertGreaterEqual(api.call_count, 2)
                self.assertFalse(blocked.is_set())
            finally:
                blocked.set()
                r.close()

    def test_firmware_console_uses_only_logging_flag_and_can_restore(self):
        with fixture() as root, patch.object(w, 'run', return_value={'code': 0, 'text': '', 'limited': False}) as commands, \
             patch.object(w, 'control', return_value='OK'):
            parameter = root / 'firmware-debug'
            parameter.write_text('32')
            with patch.object(w, 'FW_DEBUG', parameter):
                w.on_start(C)
                self.assertEqual(int(parameter.read_text()), 32 | 0x100000)
                w.on_start({**C, 'firmware_console': False, 'original_firmware_debug': '32'})
                self.assertEqual(parameter.read_text(), '32')
            self.assertTrue(all(call.args[0][:4] == ['iw', 'dev', 'ap0', 'set'] for call in commands.call_args_list))

    def test_export_is_private_and_refuses_overwriting_an_existing_file(self):
        with fixture() as root:
            w.write_fault(w.ARCHIVE / 'incident-fixture.json.gz', {'fixture': True}, True)
            output = root / 'export.json'
            args = ['watch', 'export', '--output', str(output)]
            with patch('sys.argv', args), patch.dict(os.environ, {'SUDO_UID': str(os.getuid()), 'SUDO_GID': str(os.getgid())}), contextlib.redirect_stdout(io.StringIO()):
                w.main()
            self.assertTrue(json.loads(output.read_text())['fixture'])
            self.assertEqual(output.stat().st_mode & 0o777, 0o600)
            with patch('sys.argv', args), self.assertRaises(FileExistsError):
                w.main()

    def test_ping_preserves_latency_and_timeout_evidence(self):
        with fixture(), patch.object(w, 'run', return_value={'code': 0, 'text': '64 bytes time=123.4 ms', 'limited': False}):
            r = w.FlightRecorder(C, lambda: 100)
            value = r._ping('sta0', '192.0.2.1')
            self.assertTrue(value['ok'])
            self.assertEqual(value['rtt_ms'], 123.4)

    def test_local_listener_loss_creates_incident_while_both_interfaces_remain_present(self):
        with fixture(), patch.object(w, 'default_wifi', return_value=None), \
             patch.object(w, 'interface_sample', return_value={'present': True}), \
             patch.object(w, 'local_api_sample') as api:
            now = [100.0]
            r = w.FlightRecorder(C, lambda: now[0])
            r._slow_sample = lambda unused: None
            for at, ok in ((100, True), (101, True), (102, False), (103, False)):
                now[0] = at
                api.return_value = {'ok': ok}
                r.sample()
            self.assertEqual(r.active['reasons'], ['local-api'])
            self.assertEqual(r.active['onset'], 102)

    def test_malformed_and_symlink_archives_are_refused(self):
        with fixture():
            w.mkdir(w.ARCHIVE)
            original = w.ARCHIVE / 'source.json'
            original.write_text('{"fixture":true}')
            link = w.ARCHIVE / 'link.json'
            link.symlink_to(original)
            with self.assertRaises(ValueError):
                w.read_fault(link)
            original.write_text('x' * (2 * 1024 * 1024 + 1))
            with self.assertRaises(ValueError):
                w.read_fault(original)

    def test_discovery_keeps_only_public_api_properties(self):
        with fixture() as root:
            script = root / 'project/node.js/bin/pico.js'
            script.parent.mkdir(parents=True)
            script.write_text('fixture')
            secret = 'private-token-not-for-diagnostics'
            config = root / 'project/api.json'
            config.write_text(json.dumps({'host': '0.0.0.0', 'port': 9876, 'token': secret}))
            with patch.object(w, 'run', return_value={'code': 0, 'text': 'argv[]=/usr/bin/node ' + str(script) + ' ;'}):
                found = w.discover_local_api()
            self.assertEqual(found, {'host': '127.0.0.1', 'port': 9876, 'scheme': 'http'})
            self.assertNotIn(secret, json.dumps(found))
            config.write_text(json.dumps({'host': '0.0.0.0', 'port': 9876, 'token': secret, 'tls': {'key': 'private.pem'}}))
            with patch.object(w, 'run', return_value={'code': 0, 'text': str(script) + ';'}):
                self.assertEqual(w.discover_local_api()['scheme'], 'tcp')

    def test_roundtrip_is_learned_and_non_pingable_target_never_becomes_failure(self):
        c = w.LearnedChecks()
        for t in range(20):
            self.assertIsNone(c.check('never-pinged', False, t))
        self.assertIsNone(c.check('sensor', True, 20))
        self.assertIsNone(c.check('sensor', True, 21))
        self.assertIsNone(c.check('sensor', False, 22))
        self.assertEqual(c.check('sensor', False, 23), 22)
        self.assertIsNone(c.check('sensor', True, 24))

    def test_malformed_saved_baselines_are_not_trusted(self):
        c = w.LearnedChecks({'bad': {'successes': True, 'failures': -1, 'first_bad': float('nan')}})
        self.assertEqual(c.values, {})

    def test_ping_failure_with_received_data_triggers_diagnostic_only(self):
        with fixture(), patch.object(w, 'default_wifi', return_value='sta0'), \
             patch.object(w, 'interface_sample', side_effect=lambda n: {'interface': n, 'present': True}), \
             patch.object(w, 'local_api_sample', return_value={'ok': True}), patch.object(w, 'run') as commands:
            now = [100.0]
            r = w.FlightRecorder(C, lambda: now[0])
            def slow(unused):
                r.slow = {'uplink': 'sta0', 'sampled_at': now[0],
                          'ap_probes': {m: {'route_ok': True, 'ok': now[0] < 120} for m in MACS},
                          'ap_stations': {m: {'rx bytes': int(now[0]*100)} for m in MACS},
                          'gateway': '192.0.2.1', 'gateway_ok': True, 'associated': True,
                          'collector_service': {'ActiveState': 'active', 'MainPID': '123'}}
            r._slow_sample = slow
            for t in (100, 110, 120, 130):
                now[0] = t
                r.sample()
            self.assertIsNotNone(r.active)
            self.assertIn('ap-roundtrip:' + MACS[0], r.active['reasons'])
            self.assertEqual(r.active['onset'], 120)
            commands.assert_not_called()

    def test_physical_uplink_disappearance_is_checked_using_last_known_interface(self):
        with fixture(), patch.object(w, 'default_wifi', return_value=None), \
             patch.object(w, 'interface_sample', side_effect=lambda n: {'interface': n, 'present': n != 'sta0'}), \
             patch.object(w, 'local_api_sample', return_value={'ok': None}):
            now = [100.0]
            r = w.FlightRecorder(C, lambda: now[0])
            r.last_uplink = 'sta0'
            r._slow_sample = lambda unused: None
            r.checks.check('sta-interface:sta0', True, 90)
            r.checks.check('sta-interface:sta0', True, 92)
            r.sample()
            now[0] = 102
            r.sample()
            self.assertIn('sta-interface:sta0', r.active['reasons'])

    def test_http_401_proves_listener_response_without_sending_credentials(self):
        c = {**C, 'local_api': {'host': '127.0.0.1', 'port': 8080, 'scheme': 'http'}}
        with patch.object(w.http.client, 'HTTPConnection') as factory:
            connection = factory.return_value
            connection.getresponse.return_value.status = 401
            v = w.local_api_sample(c)
            self.assertTrue(v['ok'])
            self.assertEqual(v['status'], 401)
            self.assertNotIn('Authorization', str(connection.request.call_args))
            connection.close.assert_called_once()

    def test_tcp_only_does_not_send_plain_http_to_tls_port(self):
        c = {**C, 'local_api': {'host': '127.0.0.1', 'port': 443, 'scheme': 'tcp'}}
        with patch.object(w.socket, 'create_connection') as connect, \
             patch.object(w.http.client, 'HTTPConnection') as http:
            self.assertTrue(w.local_api_sample(c)['ok'])
            http.assert_not_called()

    def test_api_connection_failure_keeps_safe_error_code(self):
        c = {**C, 'local_api': {'host': '127.0.0.1', 'port': 8080, 'scheme': 'tcp'}}
        with patch.object(w.socket, 'create_connection', side_effect=ConnectionRefusedError(111, 'private details')):
            v = w.local_api_sample(c)
            self.assertFalse(v['ok'])
            self.assertEqual(v['errno'], 111)
            self.assertNotIn('private details', json.dumps(v))

    def test_firmware_counters_follow_interface_phy_not_a_fixed_index(self):
        with fixture() as root:
            net, debug = root / 'sys', root / 'debug'
            ap = net / 'ap0'
            ap.mkdir(parents=True)
            phy = root / 'phys/phy7'
            phy.mkdir(parents=True)
            (ap / 'phy80211').symlink_to(phy, target_is_directory=True)
            (debug / 'phy7').mkdir(parents=True)
            (debug / 'phy7/counters').write_text('fc_xoff: 9\nfc_xon: 8\nf2txdata: 123\n')
            with patch.object(w, 'SYS_NET', net), patch.object(w, 'DEBUG_NET', debug):
                value = w.interface_sample('ap0')
                self.assertEqual(value['phy'], 'phy7')
                self.assertEqual(value['firmware'], {'fc_xoff': 9, 'fc_xon': 8, 'f2txdata': 123})
                self.assertFalse(w.interface_sample('missing')['present'])

    def test_unsupported_firmware_counters_are_unknown_not_zero(self):
        with fixture() as root:
            ap = root / 'net/ap0'
            ap.mkdir(parents=True)
            with patch.object(w, 'SYS_NET', root / 'net'):
                self.assertIsNone(w.interface_sample('ap0')['firmware'])

    def test_journal_credentials_redacted_and_kernel_fault_is_captured(self):
        with fixture():
            r = w.FlightRecorder(C, lambda: 100)
            r.journal_event({'_TRANSPORT': 'kernel', 'MESSAGE': 'usb fixture disconnect password=hidden token=secret'})
            self.assertIn('kernel-radio-usb-event', r.active['reasons'])
            text = json.dumps(w.read_fault(Path(r.active['path'])))
            self.assertNotIn('hidden', text)
            self.assertNotIn('token=secret', text)

    def test_ordinary_hostapd_event_is_context_not_a_fault(self):
        with fixture():
            r = w.FlightRecorder(C, lambda: 100)
            r.journal_event({'_SYSTEMD_UNIT': 'fixture.service', 'MESSAGE': 'pairwise key handshake completed'})
            self.assertIsNone(r.active)
            self.assertEqual(len(r.events), 1)

    def test_learned_probe_state_survives_same_boot_monitor_restart(self):
        with fixture():
            r = w.FlightRecorder(C, lambda: 100)
            r.checks.check('sensor', True, 90)
            r.checks.check('sensor', True, 92)
            w.write_json(w.RUNTIME / 'flight-status.json', {'boot': BOOT, 'binding': r.signature, 'learned': r.checks.values})
            restored = w.FlightRecorder(C, lambda: 110)
            restored.checks.check('sensor', False, 112)
            self.assertEqual(restored.checks.check('sensor', False, 114), 112)

    def test_changed_probe_config_does_not_restore_old_baseline(self):
        with fixture():
            r = w.FlightRecorder(C, lambda: 100)
            w.write_json(w.RUNTIME / 'flight-status.json', {'boot': BOOT, 'binding': r.signature,
                        'learned': {'old': {'successes': 2, 'failures': 0, 'first_bad': 90}}})
            restored = w.FlightRecorder({**C, 'uplink': 'newsta'}, lambda: 110)
            self.assertEqual(restored.checks.values, {})

    def test_probe_configuration_refuses_external_targets(self):
        for api in ({'host': '192.0.2.1', 'port': 80, 'scheme': 'http'},
                    {'host': '127.0.0.1', 'port': True, 'scheme': 'tcp'},
                    {'host': '127.0.0.1', 'port': 80, 'scheme': 'insecure-tls'}):
            with self.assertRaises(ValueError):
                w.validate_config({**C, 'local_api': api})


if __name__ == '__main__':
    unittest.main()
