# Pi setup and recovery — quick reference

- **Wi-Fi:** built-in `wlan0` runs the independent monitoring hotspot through hostapd; USB `wlan1` connects upstream through NetworkManager. `wlan0` showing **unmanaged** in `nmcli` is expected.
- **Logging:** `pico-mqtt.service` collects Pico/SBMS data, writes SQLite history and serves the app API. Mosquitto remains required; Node-RED is no longer needed for this collector.
- **Preserve:** private `~/PicoData/{mqtt,logging.json,api.json,history/}`. Source updates must not replace these. Google Drive authentication alone does **not** create a scheduled backup; USB logging was not implemented.

## Setup and updates

- **Update collector:** `~/update-pico.sh` — installs the latest `node.js` source/dependencies and restarts the collector; no `git pull` in the installed folder.
- **Fresh-install essentials:** privately back up the updater, `/usr/local/sbin/pi-wifi`, its mode/timer/service configuration, `/etc/ella-ap-watch.json`, hotspot service units and their hostapd/DHCP/firewall configuration, NetworkManager profiles and the private PicoData files above. The collector updater does not recreate the hotspot or `pi-wifi` installation. [Collector setup](../node.js/README.md).
- **Rclone/Google Drive:** [installation and authentication](RCLONE-SETUP.md). Use the normal login account, without `sudo`, for `rclone config` and `rclone lsd gdrive:`. Preserve its private configuration.
- **AP diagnostics/recovery** — monitor **1.1 installation confirmed**; active in `recover` mode with the configured sensor baseline observed after reboot. Actual recovery from a future stall remains to verify. [Monitor-only update](../node.js/AP-WATCH.md#update-an-existing-monitor-to-11). For reinstalling, with both sensors connected, use their reserved hotspot IPs:
  `sudo python3 ~/PicoData/node.js/tools/ap-watch.py install --target SENSOR_ONE_IP --target SENSOR_TWO_IP`
  [Details/removal](../node.js/AP-WATCH.md).
- **Optional app Pi-mode controls** — collector 0.7.5 needs the [one-time setup](../node.js/PI-STATUS.md#optional-pi-autohomem1-buttons). API resource readouts need no SSH; Open terminal uses separately installed ConnectBot.

## Wi-Fi: check and recover

- **Upstream status:** `sudo pi-wifi status`
- **Automatic home preference/fallback:** `sudo pi-wifi auto`
- **Force home / mobile router:** `sudo pi-wifi home` / `sudo pi-wifi m1` — return to `auto` afterwards.
- **Interfaces/routes:** `nmcli device status`; `ip -4 -brief address`; `ip -4 route`
- **Hotspot services:** `systemctl is-active ella-hotspot.service ella-hotspot-network.service ella-hotspot-dns.service`
- **Connected clients:** `sudo iw dev wlan0 station dump`
- **Restart stalled hotspot:** `sudo systemctl restart ella-hotspot.service` — disconnects hotspot clients briefly; leaves the collector running.
- **Recent evidence:** `sudo journalctl -u ella-hotspot.service -u ella-hotspot-dns.service -n 40 --no-pager`; `sudo journalctl -k -b -n 40 --no-pager`
- **AP power saving:** `iw dev wlan0 get power_save`; temporarily disable with `sudo iw dev wlan0 set power_save off`. The AP watch startup hook maintains this.
- **AP watch:** `sudo ella-ap-watch status`; `sudo ella-ap-watch capture`; version 1.1 adds `sudo ella-ap-watch errors` for private crash/error evidence. `observe` disables automatic recovery; `recover` re-enables it. Private incidents: `/var/lib/ella-ap-watch/`.

## Logging: check and restart

- **Sources and last save:** `node ~/PicoData/node.js/bin/api-check.js --path /api/v1/status` — both sources should be fresh; repeat after a minute to confirm `logging.savedAt` advances.
- **Pi resources, collector 0.7.5+:** `node ~/PicoData/node.js/bin/api-check.js --path /api/v1/pi`
- **Services:** `systemctl is-active pico-mqtt.service mosquitto.service`
- **Collector errors:** `journalctl -u pico-mqtt.service -n 40 --no-pager`
- **MQTT errors:** `sudo tail -n 30 /var/log/mosquitto/mosquitto.log`
- **Restart collector/logger:** `sudo systemctl restart pico-mqtt.service`; recheck API status afterwards.
- **Restart broker only if necessary:** `sudo systemctl restart mosquitto.service` — interrupts MQTT clients briefly.
- **Safe shutdown:** `sudo poweroff`
