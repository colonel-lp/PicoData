#!/usr/bin/env python3
"""Enable/disable only the three existing pi-wifi modes for the collector API.

sudo python3 pi-api-controls.py install --user SERVICE_USER --api-config /path/api.json
Does not change Wi-Fi or restart services. Never prints private API contents.
"""
import argparse
import json
import os
from pathlib import Path
import pwd
import re
import stat
import subprocess
import tempfile

RULE = Path('/etc/sudoers.d/ella-pi-api')
HELPER = Path('/usr/local/sbin/pi-wifi')
MARKER = '# Ella Monitoring fixed Pi Wi-Fi API commands\n'


def rule(user):
    if not re.fullmatch(r'[a-z_][a-z0-9_-]{0,31}', user):
        raise ValueError('Invalid service account name')
    return MARKER + user + ' ALL=(root) NOPASSWD: ' + ', '.join(str(HELPER) + ' ' + mode for mode in ('auto', 'home', 'm1')) + '\n'


def atomic(path, content, mode, owner):
    fd, name = tempfile.mkstemp(prefix='.ella-pi-api-', dir=path.parent)
    try:
        with os.fdopen(fd, 'w') as out:
            os.fchmod(out.fileno(), mode)
            os.fchown(out.fileno(), *owner)
            out.write(content)
            out.flush()
            os.fsync(out.fileno())
        os.replace(name, path)
    finally:
        Path(name).unlink(missing_ok=True)


def install(user, api_file, enabled=True):
    account = pwd.getpwnam(user)
    api_file = api_file.resolve(strict=True)
    info = api_file.stat()
    if not stat.S_ISREG(info.st_mode) or info.st_size > 65536 or info.st_uid not in (0, account.pw_uid):
        raise ValueError('Review private API file ownership/type/size')
    original = api_file.read_text()
    data = json.loads(original)
    if not isinstance(data, dict) or not re.fullmatch('[a-f0-9]{64}', data.get('token', '')):
        raise ValueError('Invalid private API configuration')
    pi = data.setdefault('pi', {})
    if not isinstance(pi, dict):
        raise ValueError('Invalid Pi API configuration')
    if RULE.exists() and RULE.read_text() != rule(user):
        raise ValueError('Existing sudo rule differs; review before replacing it')
    if enabled:
        helper = HELPER.stat()
        if not stat.S_ISREG(helper.st_mode) or helper.st_uid != 0 or helper.st_mode & 0o022:
            raise ValueError('pi-wifi must be a root-owned, non-writable helper')
        text = rule(user)
        fd, temporary = tempfile.mkstemp(prefix='.ella-pi-api-check-', dir=RULE.parent)
        try:
            with os.fdopen(fd, 'w') as out:
                out.write(text)
            result = subprocess.run(['/usr/sbin/visudo', '-cf', temporary], capture_output=True, timeout=10)
            if result.returncode:
                raise ValueError('Sudo rule validation failed; no configuration changed')
        finally:
            Path(temporary).unlink(missing_ok=True)
    backup = api_file.with_name(api_file.name + '.before-pi-controls')
    if not backup.exists():
        # Preserve the pre-change config locally, exclusively, with private mode.
        fd = os.open(backup, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
        with os.fdopen(fd, 'w') as out:
            os.fchown(out.fileno(), info.st_uid, info.st_gid)
            out.write(original)
    pi['allowWifiControl'] = enabled
    previous_rule = RULE.read_text() if RULE.exists() else None
    try:
        if enabled:
            atomic(RULE, text, 0o440, (0, 0))
        else:
            RULE.unlink(missing_ok=True)
        atomic(api_file, json.dumps(data, indent=2) + '\n', 0o600, (info.st_uid, info.st_gid))
    except OSError:
        if previous_rule is None:
            RULE.unlink(missing_ok=True)
        else:
            atomic(RULE, previous_rule, 0o440, (0, 0))
        raise
    print('Pi API mode controls ' + ('enabled' if enabled else 'disabled') + '; private API credentials retained.')
    print('Restart pico-mqtt.service once to read the updated configuration.')


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('action', choices=('install', 'remove'))
    p.add_argument('--user', required=True)
    p.add_argument('--api-config', type=Path, required=True)
    args = p.parse_args()
    if os.geteuid() != 0:
        p.error('Run with sudo')
    os.umask(0o077)
    try:
        install(args.user, args.api_config, args.action == 'install')
    except (OSError, ValueError, subprocess.SubprocessError) as error:
        # Avoid echoing config parser errors, paths, tokens or command output.
        print('Setup failed: ' + (str(error) if isinstance(error, ValueError) and not isinstance(error, json.JSONDecodeError) else type(error).__name__))
        raise SystemExit(1)


if __name__ == '__main__':
    main()
