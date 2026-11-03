"""Offline safety checks; no real block devices or systemd writes."""
import importlib.util
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location(
    'guard', Path(__file__).resolve().parents[1] / 'tools/zram-start-guard.py')
guard = importlib.util.module_from_spec(spec)
spec.loader.exec_module(guard)


class GuardTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.blocks = self.root / 'block'
        self.blocks.mkdir()
        self.state = self.root / 'zram-device-list'
        self.state.write_text('swap zram0\nlog zram1 /var/log\n')
        self.original = self.state.read_bytes()
        self.backup = self.state.with_name(self.state.name + '.pre-start-stale')

    def device(self, name, size):
        device = self.blocks / name
        device.mkdir()
        (device / 'disksize').write_text(size)

    def test_no_devices_archives_stale_list(self):
        guard.check(self.blocks, self.state)
        self.assertFalse(self.state.exists())
        self.assertEqual(self.backup.read_bytes(), self.original)

    def test_uninitialized_devices_allow_repair(self):
        self.device('zram0', '0\n')
        guard.check(self.blocks, self.state)
        self.assertEqual(self.backup.read_bytes(), self.original)

    def test_active_device_preserves_state(self):
        self.device('zram12', '1048576\n')
        guard.check(self.blocks, self.state)
        self.assertEqual(self.state.read_bytes(), self.original)
        self.assertFalse(self.backup.exists())

    def test_bad_size_fails_closed(self):
        self.device('zram0', 'not a size')
        with self.assertRaises(ValueError):
            guard.check(self.blocks, self.state)
        self.assertEqual(self.state.read_bytes(), self.original)

    def test_missing_size_fails_closed(self):
        (self.blocks / 'zram0').mkdir()
        with self.assertRaises(OSError):
            guard.check(self.blocks, self.state)
        self.assertEqual(self.state.read_bytes(), self.original)

    def test_empty_list_is_left_alone(self):
        self.state.write_text('')
        guard.check(self.blocks, self.state)
        self.assertTrue(self.state.exists())

    def test_missing_list_is_harmless(self):
        self.state.unlink()
        guard.check(self.blocks, self.state)
        self.assertFalse(self.backup.exists())

    def test_latest_backup_replaced_without_growth(self):
        self.backup.write_text('previous stale list')
        guard.check(self.blocks, self.state)
        self.assertEqual(self.backup.read_bytes(), self.original)

    def test_symlink_tracking_list_refused(self):
        self.state.rename(self.root / 'real-list')
        self.state.symlink_to(self.root / 'real-list')
        with self.assertRaises(RuntimeError):
            guard.check(self.blocks, self.state)
        self.assertEqual((self.root / 'real-list').read_bytes(), self.original)


if __name__ == '__main__':
    unittest.main()
