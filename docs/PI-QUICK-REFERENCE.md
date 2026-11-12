# Pi setup and recovery — quick reference

- **Current Wi-Fi — confirmed rollback:** built-in `wlan0` is a NetworkManager STA connected to the mobile router; both sensors are on that router. The USB STA adapter has been removed. The collector remained running and both sources were fresh after migration. Private addresses/credentials stay outside this document.
- **Disabled:** standalone hotspot and AP recovery, old NetworkManager hotspot autoconnect, USB uplink profile autoconnect and automatic network-switch timer. Keep these disabled during the stability test. Do not use `pi-wifi auto/home/m1` until its interface/profile bindings are deliberately adapted to the new topology.
- **Logging:** `pico-mqtt.service` collects Pico/SBMS data, writes SQLite history and serves the app API. Mosquitto remains required; Node-RED is no longer needed for this collector.
- **Preserve:** private `~/PicoData/{mqtt,logging.json,api.json,history/}`. Source updates must not replace these. Google Drive authentication alone does **not** create a scheduled backup; USB logging was not implemented.

## Setup and updates

- **Update collector:** `~/update-pico.sh` — installs the latest `node.js` source/dependencies and restarts the collector; no `git pull` in the installed folder.
- **Fresh-install essentials:** privately back up the updater, `/usr/local/sbin/pi-wifi`, its mode/timer/service configuration, `/etc/ella-ap-watch.json`, hotspot service units and their hostapd/DHCP/firewall configuration, NetworkManager profiles and the private PicoData files above. The collector updater does not recreate the hotspot or `pi-wifi` installation. [Collector setup](../node.js/README.md).
- **Rclone/Google Drive:** [installation and authentication](RCLONE-SETUP.md). Use the normal login account, without `sudo`, for `rclone config` and `rclone lsd gdrive:`. Preserve its private configuration.
- **Wi-Fi diagnostics 1.3 — STA-only installation confirmed:** [STA-only install and future USB AP migration](../node.js/AP-WATCH.md#current-sta-only-setup). Install only the helper with `install --topology sta-only --uplink STA_INTERFACE`; this starts diagnostic recording without AP checks or network recovery. Status confirms active STA-only observe mode, AP checks/recovery disabled, normal firmware-console verbosity restored and a responding local API listener. Incident capture and unattended stability remain to verify. Monitor updates do not require restarting the collector.
- **Prepared update, not yet confirmed installed:** collector 0.7.6 via `~/update-pico.sh`; then monitor 1.4 via `sudo python3 ~/PicoData/node.js/tools/ap-watch.py install`. With no topology arguments the existing STA-only/observe setup is preserved. Check `sudo ella-ap-watch status` afterwards.
- **Source verification:** `node ~/PicoData/node.js/bin/deployment-status.js` checks the manifest and identifies separately installed helper hashes. [Updater and historical pi-wifi source](../node.js/DEPLOYMENT.md).
- **App Pi-mode controls:** leave unused in the current topology. API resource readouts need no SSH; Open terminal uses separately installed ConnectBot.

## Wi-Fi: check and recover

- **Interfaces/routes:** `nmcli device status`; `ip -4 -brief address`; `ip -4 route`; `iw dev wlan0 link`
- **Recent system evidence:** `sudo journalctl -k -b -n 40 --no-pager`; `sudo journalctl -u NetworkManager -u wpa_supplicant -n 40 --no-pager`
- **Reconnect STA if necessary:** `sudo nmcli --wait 45 connection up "PROFILE_NAME" ifname wlan0` using the existing private STA profile. This may interrupt SSH. Check routing and source freshness afterwards.
- **After installing monitor 1.3:** `sudo ella-ap-watch status`; `sudo ella-ap-watch errors`; `sudo ella-ap-watch faults`. `capture` requests a manual fault window; `export --output "$HOME/wifi-fault.json"` exports the latest incident to a new private file.
- **Evidence retention:** monitor health stays in bounded RAM; faults save available 60-second pre/post windows under `/var/lib/ella-ap-watch/`, keeping five compressed archives. Normal journal persistence follows existing zram configuration. Exports contain private network details; do not commit them.
- **Future AP:** retain configuration/backups, but leave AP services disabled until a new supported adapter is deliberately configured. [AP setup and monitoring](../node.js/AP-WATCH.md#future-usb-ap-adapter).

## Logging: check and restart

- **Sources and last save:** `node ~/PicoData/node.js/bin/api-check.js --path /api/v1/status` — both sources should be fresh; repeat after a minute to confirm `logging.savedAt` advances.
- **Recent API request diagnostics, collector 0.7.6:** `node ~/PicoData/node.js/bin/api-check.js --path /api/v1/diagnostics` — private LAN addresses/request IDs; do not publish the output.
- **Pi resources, collector 0.7.5+:** `node ~/PicoData/node.js/bin/api-check.js --path /api/v1/pi`
- **Services:** `systemctl is-active pico-mqtt.service mosquitto.service`
- **Collector errors:** `journalctl -u pico-mqtt.service -n 40 --no-pager`
- **MQTT errors:** `sudo tail -n 30 /var/log/mosquitto/mosquitto.log`
- **Restart collector/logger:** `sudo systemctl restart pico-mqtt.service`; recheck API status afterwards.
- **Restart broker only if necessary:** `sudo systemctl restart mosquitto.service` — interrupts MQTT clients briefly.
- **Safe shutdown:** `sudo poweroff`

## Zram: check and preserve startup

- **Checks:** `zramctl`; `free -h`; `findmnt -T /var/log`. The configured swap should appear and the log directory should use an overlay. “active (exited)” alone does not verify device creation.
- **Configuration:** existing ecdye/zram-config uses `/etc/ztab`. Its own log is `/usr/local/share/zram-config/log/zram-config.log`; nightly sync is controlled by `zsync.timer`.
- **Confirmed recovery:** a persistent stale `/usr/local/lib/zram-config/zram-device-list` caused startup to skip absent devices. Archiving it before an orderly reboot restored swap and the log overlay. This does not identify the earlier shutdown failure or establish a Wi-Fi cause.
- **Startup guard — source available; installation/reboot validation pending:** download [zram-start-guard.py](../node.js/tools/zram-start-guard.py), then run `sudo python3 zram-start-guard.py install`. This installs `/usr/local/sbin/ella-zram-start-guard` and `/etc/systemd/system/zram-config.service.d/60-stale-state.conf`, then reloads systemd without restarting services. It leaves the upstream zram script, ztab settings and sync timer unchanged.
- **At each zram service start:** if all zram devices are absent/uninitialized, archive a nonempty tracking list to the single `.pre-start-stale` backup and let normal startup recreate devices. Initialized devices preserve the list; inspection failures refuse cleanup. No filesystem/log data is deleted. Partial initialization with any running zram device is deliberately not auto-repaired.
- **Verify:** reboot normally, repeat the three checks above, and inspect `sudo journalctl -b -u zram-config.service --no-pager`. Verify again after a second orderly reboot. Reboots briefly interrupt acquisition; do not stop/restart live log mounts merely to test this guard.
- **Remove guard:** `sudo rm -f /etc/systemd/system/zram-config.service.d/60-stale-state.conf /usr/local/sbin/ella-zram-start-guard`, then `sudo systemctl daemon-reload`. This removes only the added guard; zram and its data/configuration remain intact.



