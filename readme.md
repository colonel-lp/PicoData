# Ella Monitoring

Monitoring for the Ella campervan, combining Simarine Pico sensor data and ElectroDacus SBMS0 battery data on a shared Wi-Fi network.

## Current setup

- `node.js/bin/pico.js` discovers the Pico from UDP broadcasts on port **43210**, retrieves its sensor configuration over TCP port **5001**, decodes live UDP readings and publishes the same JSON as the previous `python/pico-mqtt.py` to the Raspberry Pi's **Mosquitto MQTT broker**.
- The ElectroDacus also publishes its data to that broker. The Node.js collector now subscribes to its topic and decodes battery/solar/load current, SOC and configured pack voltage independently of Pico acquisition.
- The Node-RED project in [`node.red/`](node.red/) subscribes to both sources and presents the existing web dashboard.

The current direction is continuous collection/logging on the headless Pi Zero 2 W, with Android displaying live data and history. The [collector 0.7.1](node.js/README.md) publishes the original Ella JSON and receives ElectroDacus MQTT data. Selected persistent SQLite logging and an optional authenticated local live/history API are implemented, including the complete live SBMS broadcast and On/Off flags without flag history. The [Android preview 0.2.0](android/README.md) uses that API, with the Joying EQ companion layout/theme controls, a Settings page, optional persistence, retained-history charts and export; actual phone/head-unit LAN and display tests remain pending. Direct Android Pico reception and an embedded MQTT broker remain future options to reassess. SignalK integration is outside the scope.

## Directories in this repository

| Directory | Purpose |
| --- | --- |
| [`python/`](python/) | Previous Raspberry Pi scripts retained as the behaviour baseline. `pico-mqtt.py` is the original MQTT implementation; `pico-json.py` and `pico-raw.py` are alternative tools. `brainsmoke.py` provides CRC16 support. The active Node.js collector reads its config from the parent `PicoData/mqtt`. |
| [`_old/`](_old/) | Earlier Python variants and the original upstream Node.js reference in [`pico2signalk/`](_old/pico2signalk/) retained for reference. |
| [`node.js/`](node.js/) | Active standalone Pi collector (0.7.1), using the updated Pico connectivity and the owner's Python mappings/calculations. `lib/` contains Pico acquisition/decoding, MQTT publishing, SBMS reception, SQLite history and the authenticated local API; `bin/pico.js` is the service entry point and `bin/verify-capture.js` verifies Pico captures. Includes tests, service template and [Pi instructions](node.js/README.md). SignalK is excluded. |
| [`node.red/`](node.red/) | Existing Node-RED dashboard project. [`flows.json`](node.red/flows.json) defines displayed fields, conversions and charts; `package.json` defines the project. It subscribes to `/Ella/Pico/` and `/Ella/sbms`; preserve exact topic spelling and slashes. |
| [`simarine/`](simarine/) | Reference APK, `simarine.apk`, and JADX output in `simarine jadx/`. The inspected app uses .NET MAUI/Mono; Java decompilation does not establish that its managed Pico protocol implementation is available. |
| [`android/`](android/) | Native Android API viewer in [`source/`](android/source/), with [setup/build instructions](android/README.md), [app changelog](android/CHANGELOG.md), planning and validation records. The Pi collector remains at the repository root `node.js/`; see [PROCESS.md](android/PROCESS.md) and [BUILD-VALIDATION.md](android/BUILD-VALIDATION.md). |

## Related repositories

| Repository | Purpose and starting points |
| --- | --- |
| [pico2signalk](https://github.com/colonel-lp/pico2signalk) (`master`) | Updated Node.js fork, version 0.0.21. `lib/get-pico-config.js` handles discovery/retries, `lib/pico-protocol.js` TCP configuration/parsing, `lib/crc16.js` checksums and `lib/sensor-list.js` sensor positions. `index.js` contains live UDP decoding mixed with SignalK output. |

Use the newer Pico connectivity as a starting point while preserving the owner's Python additions and existing dashboard behaviour. Reconcile pitch/roll support, current polarity, temperature/pressure units and battery/tank calculations before porting.

Project working instructions: [agents.md](agents.md). Keep credentials out of new source, documentation, logs and test fixtures.


Selected continuous SQLite history is implemented in the Pi collector; see [logging setup](node.js/LOGGING.md). The database and runtime sensor selection remain private and outside the source folder. The [local API](node.js/API.md) serves source-referenced live/history JSON for the Android viewer.

Battery history now uses one combined record per UTC interval, with distinct source-referenced Pico/SBMS/solar fields. Existing history is backed up and migrated automatically; other sensor records are preserved.
