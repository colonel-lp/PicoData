# Hotspot diagnostics and recovery

Operational helper version 1.0. This does not release or change the Android app
or collector. It is installed separately; the normal collector updater does not
install, enable or run this helper.

This is for the existing stock-hostapd installation, not a NetworkManager AP.
The default AP interface is `wlan0`, its custom service `ella-hotspot.service`,
and its control directory `/run/hostapd-ella`. The installer reads actual client
MACs from local DHCP leases/neighbours. No installation addresses, SSIDs, keys,
credentials or captures belong in this repository.

## Install

Use a working SSH session through the independent upstream connection while
installing. Have both always-on sensor clients connected. Install needs Python
3.9+, systemd, `iw`, `ip`, and `ping`; `ethtool` is optional for incident details.
No pip packages, driver, kernel, firmware or network-profile changes are needed.

Download `tools/ap-watch.py` from this repository to the Pi, then run:

```bash
sudo python3 ~/ap-watch.py install --target SENSOR_ONE_IP --target SENSOR_TWO_IP
sudo ella-ap-watch status
sudo journalctl -u ella-ap-watch.service -n 15 --no-pager
```

Replace both placeholders with the existing reserved hotspot IPv4 addresses.
They must be different, on the AP's directly connected subnet, and have known
MACs in `/run/ella-hotspot-dns/leases` or the local neighbour table. The installer
refuses an unresponsive/inactive hostapd service, invalid or duplicate targets,
and unrelated existing service/drop-in files. Re-running install retains mode
and original diagnostic settings. It restarts only the monitor during install,
not the AP or collector. Never infer successful installation from source tests.

The helper installs:

- `/usr/local/sbin/ella-ap-watch`: this standalone Python program.
- `/etc/ella-ap-watch.json`: root-only configuration, learned target MACs and
  previous diagnostic settings.
- `ella-ap-watch.service`: a low-priority monitor with a 64 MiB memory ceiling.
- An additive `70-ap-watch.conf` drop-in for the existing hotspot service. Its
  startup hook disables AP power saving and enables hostapd DEBUG messages via
  the local control socket. It preserves the original ExecStart/security config;
  it never uses hostapd `-K` or reads the password/API/MQTT configuration.

The monitor also applies those diagnostic settings to the currently running AP.
No initial hotspot restart is needed. DEBUG can increase existing journal traffic;
this does not change journald's storage policy or globally enable kernel tracing.
On the owner's existing installation the system logs use RAM-backed storage.

## What triggers recovery

Checks run about every 30 seconds, using monotonic time. There is a two-minute
startup grace. A sensor is responsive if its incoming Wi-Fi byte/packet counters
advance or it answers a short interface-bound local ping. The IP route must still
point directly through the AP interface. Probes never target the internet.

Traffic recovery arms only after **each configured client has been seen alive**
during this monitor run. If all of them subsequently have no confirmed traffic
or ping responses for at least 90 seconds, the monitor captures evidence and
issues the same `systemctl restart ella-hotspot.service` recovery used manually.
A non-responsive/non-ENABLED hostapd control interface for 90 seconds can also
trigger recovery after the startup grace, provided no target is still responsive
and target routes are correct. The current service's DHCP/DNS dependencies follow
its existing systemd relationships. The collector is never stopped/restarted.

Important limits:

- One responsive sensor suppresses a whole-AP restart. This intentionally does
  not restart the AP for an isolated sensor, MQTT, app/API or internet failure.
- No connected clients, asleep phones or failed internet probes alone do not
  trigger a restart. The route-error state asks for diagnosis instead of changing
  addresses, routing or NAT automatically.
- Both always-on sensors being deliberately powered off can resemble a global
  failure after arming. That can cause one unnecessary restart; use observe mode
  during maintenance. Client traffic does not prove application readings are fresh.
- At most two recoveries per rolling 30 minutes, at least ten minutes apart.
  One attempt per unresolved incident: without 90 seconds of healthy observations
  after recovery, it stays latched and requests manual investigation. It never
  loops on reboots, resets either Wi-Fi interface, reloads drivers, restarts
  NetworkManager or changes the upstream connection.
- Restart limits/latch are saved in RAM and survive monitor-service restarts.
  They reset on Pi reboot. Malformed state inhibits recovery until explicit reset.
  After any monitor restart, traffic arming needs both clients observed again.
- A deliberately inactive/failed AP service pauses this monitor; hostapd's existing
  `Restart=on-failure` handles process exits. Disable monitoring before maintenance.
- A silent AP failure before both sensors have ever been seen cannot be reliably
  distinguished from switched-off sensors; enabled/responsive hostapd then remains
  unarmed. This is a recovery aid, not a proof of radio health or a replacement for
  fixing the cause. Readings missed during an outage cannot be recovered by it.

## Evidence and storage limits

The monitor keeps timestamped station counters, probe results, state transitions
and hostapd control events in `/run/ella-ap-watch`. The event ring uses two files
of up to 2 MiB each. Status and restart state are also in RAM, with no per-check
database writes. Hostapd events are capped in size/rate. Every command has a
deadline and captured-byte limit, including an unresponsive control socket.

Before an automatic restart it writes a root-only compressed incident file in
`/var/lib/ella-ap-watch`. At most five files are retained, each bounded to 2 MiB
uncompressed (at most roughly 10 MiB in total). A partial write from an interrupted
capture is cleaned up on the next capture. An archive failure does not prevent
recovery; RAM evidence and a journal warning remain.

Each incident includes pre-restart monitor events, hostapd state, station counters,
interface/power-save/driver details, addresses/routes/neighbours, selected service
state, and bounded recent kernel/hotspot/network/DHCP journal extracts. This can
expose private network names, MACs, addresses and diagnostic messages. Keep the
reports private; they are never uploaded automatically. Firmware stalls may remain
silent even with this logging, but the timeline/counters can narrow the failure.

Only incident/manual captures write these archives to persistent storage. Up to
five archives survive a reboot; the rolling RAM evidence does not. This does not
change the sensor SQLite history, retention, cadence or backup arrangements.

## Operator commands

```bash
sudo ella-ap-watch status
sudo ella-ap-watch observe     # keep diagnostics; disable automatic recovery
sudo ella-ap-watch recover     # re-enable conservative recovery
sudo ella-ap-watch capture     # save a diagnostic snapshot; no restart
sudo ella-ap-watch report      # print the newest private incident JSON
sudo journalctl -u ella-ap-watch.service -n 30 --no-pager
```

Mode changes take effect within a check cycle. An already-started recovery is not
cancelled by changing mode. `status` shows its last-check timestamp; a stale check
is not live health confirmation. For uninterrupted manual maintenance stop it:

```bash
sudo systemctl disable --now ella-ap-watch.service
```

If recovery stayed latched after manual repair, or state was malformed:

```bash
sudo ella-ap-watch reset
```

This intentionally resets its recovery history and re-arms monitoring after a new
startup/baseline; it does not restart the AP. Use only after investigating/repair.

To remove this installation and restore the diagnostic verbosity/power-save
setting captured on first install:

```bash
sudo ella-ap-watch uninstall
```

It removes the monitor, config and additive startup hook, leaves private incident
files available, and does not restart the AP or collector. A power-save setting
already disabled before install remains disabled on removal. The underlying
hotspot migration/rollback tool is separate and remains unchanged.

## Validation

Run the focused offline tests with `python3 test/ap-watch.test.py` from `node.js`.
They test recovery decisions/arming/grace, isolated-sensor and routing failures,
manual stop/observe mode, cooldown/latching, saved/corrupt state, output/time bounds,
parsing, private file permissions, RAM rotation, incident retention and mocked
control-protocol replies. No live network/systemd operations run in these tests.
Root-owned file helper checks skip when run as a regular user.

Pi radio behavior, service integration, actual resource usage and recovery under
a real AP stall still require owner verification. Do not deliberately disconnect
both live sensors or stop hostapd merely to force a demonstration of recovery.
