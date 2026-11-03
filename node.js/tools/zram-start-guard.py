#!/usr/bin/env python3
"""Guard ecdye/zram-config against a tracking list left across a reboot.

Install once with sudo python3 zram-start-guard.py install. The guard runs
before the existing service; it does not replace zram-config or its settings.
"""

import argparse
import os
from pathlib import Path
import subprocess
import sys


INSTALLED = Path('/usr/local/sbin/ella-zram-start-guard')
DROPIN = Path('/etc/systemd/system/zram-config.service.d/60-stale-state.conf')
STATE = Path('/usr/local/lib/zram-config/zram-device-list')
UNIT = '''# Managed by Ella zram startup guard
[Service]
ExecStartPre=/usr/bin/python3 /usr/local/sbin/ella-zram-start-guard check
'''


def check(sys_block=Path('/sys/block'), state=STATE):
    """Archive stale bookkeeping only when no zram device is initialized."""
    if not sys_block.is_dir():
        raise RuntimeError('Cannot inspect block devices; tracking list preserved')
    if state.is_symlink():
        raise RuntimeError('Tracking list is a symlink; refusing to modify it')
    if not state.exists():
        print('Ella zram guard: no tracking list to repair', flush=True)
        return
    if not state.is_file():
        raise RuntimeError('Tracking list is not a regular file; preserved')
    # Read every device first. Missing, unreadable or malformed sizes fail
    # closed, leaving bookkeeping untouched and preventing unsafe startup.
    sizes = []
    for device in sys_block.glob('zram*'):
        size = int((device / 'disksize').read_text().strip())
        if size < 0:
            raise RuntimeError('Invalid zram size; tracking list preserved')
        sizes.append(size)
    if any(sizes):
        print('Ella zram guard: initialized device present; tracking list preserved', flush=True)
        return
    if not state.stat().st_size:
        return
    backup = state.with_name(state.name + '.pre-start-stale')
    if backup.is_symlink():
        raise RuntimeError('Backup is a symlink; tracking list preserved')
    # Atomic rename on the same filesystem; keep the latest stale list only.
    state.replace(backup)
    print('Ella zram guard: archived stale tracking list; normal startup will recreate devices', flush=True)


def install():
    if os.geteuid() != 0:
        raise RuntimeError('Run install with sudo')
    if not Path('/usr/local/sbin/zram-config').is_file():
        raise RuntimeError('Install the existing ecdye/zram-config service first')
    source = Path(__file__).read_bytes()
    INSTALLED.parent.mkdir(parents=True, exist_ok=True)
    temporary = INSTALLED.with_name(INSTALLED.name + '.new')
    with temporary.open('wb') as stream:
        stream.write(source)
    temporary.chmod(0o755)
    temporary.replace(INSTALLED)
    DROPIN.parent.mkdir(parents=True, exist_ok=True)
    DROPIN.write_text(UNIT)
    DROPIN.chmod(0o644)
    subprocess.run(['systemctl', 'daemon-reload'], check=True)
    print('Installed zram startup guard. No services restarted or live mounts changed.')
    print('Reboot normally, then check zramctl, free -h and findmnt -T /var/log.')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action', choices=['install', 'check'])
    args = parser.parse_args()
    try:
        install() if args.action == 'install' else check()
    except (OSError, ValueError, RuntimeError, subprocess.CalledProcessError) as error:
        print('Ella zram guard: ' + str(error), file=sys.stderr, flush=True)
        return 1
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
