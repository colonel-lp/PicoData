"""Offline installer checks; never changes /etc or executes sudo/visudo."""
import contextlib
import importlib.util
import io
import json
import os
from pathlib import Path
import tempfile
from types import SimpleNamespace
import unittest
from unittest.mock import patch

spec = importlib.util.spec_from_file_location('controls', Path(__file__).parents[1] / 'tools/pi-api-controls.py')
m = importlib.util.module_from_spec(spec)
spec.loader.exec_module(m)


class Rules(unittest.TestCase):
    def test_exact_three_actions_and_no_command_wildcards(self):
        text = m.rule('service-user')
        self.assertEqual(text.count('/usr/local/sbin/pi-wifi'), 3)
        self.assertIn('pi-wifi auto', text)
        self.assertNotIn('*', text)
        for user in ('bad name', 'root\nALL', 'a;other', '../user'):
            with self.assertRaises(ValueError):
                m.rule(user)

    @unittest.skipUnless(os.geteuid() == 0, 'root-owned permission checks use isolated temporary files')
    def test_install_preserves_credentials_owner_backup_and_remove_only_own_rule(self):
        with tempfile.TemporaryDirectory() as directory, contextlib.ExitStack() as stack:
            root = Path(directory)
            helper = root / 'pi-wifi'
            helper.write_text('#!/bin/sh\nexit 0\n')
            helper.chmod(0o755)
            config = root / 'api.json'
            original = json.dumps({'host': '127.0.0.1', 'token': 'a' * 64, 'port': 8080}) + '\n'
            config.write_text(original)
            rule = root / 'sudo-rule'
            stack.enter_context(patch.object(m, 'RULE', rule))
            stack.enter_context(patch.object(m, 'HELPER', helper))
            stack.enter_context(patch.object(m.pwd, 'getpwnam', return_value=SimpleNamespace(pw_uid=0)))
            calls = stack.enter_context(patch.object(m.subprocess, 'run', return_value=SimpleNamespace(returncode=0)))
            stack.enter_context(contextlib.redirect_stdout(io.StringIO()))
            m.install('service-user', config)
            self.assertTrue(json.loads(config.read_text())['pi']['allowWifiControl'])
            self.assertEqual(json.loads(config.read_text())['token'], 'a' * 64)
            self.assertEqual(config.with_name('api.json.before-pi-controls').read_text(), original)
            self.assertEqual(config.stat().st_mode & 0o777, 0o600)
            self.assertEqual(rule.stat().st_mode & 0o777, 0o440)
            calls.assert_called_once()
            m.install('service-user', config, False)
            self.assertFalse(rule.exists())
            self.assertFalse(json.loads(config.read_text())['pi']['allowWifiControl'])
            rule.write_text('existing owner rule')
            with self.assertRaises(ValueError):
                m.install('service-user', config)
            self.assertEqual(rule.read_text(), 'existing owner rule')


if __name__ == '__main__':
    unittest.main()
