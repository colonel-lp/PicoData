# Ella Monitoring

Monitoring for the Ella campervan, combining Simarine Pico sensor data and ElectroDacus SBMS0 battery data on a shared Wi-Fi network.

## Current setup

- `python/pico-mqtt.py` discovers the Pico from UDP broadcasts on port **43210**, retrieves its sensor configuration over TCP port **5001**, decodes live UDP readings and publishes JSON to the Raspberry Pi's **Mosquitto MQTT broker**.
- The ElectroDacus also publishes its data to that broker.
- The Node-RED project in [`node.red/`](node.red/) subscribes to both sources and presents the existing web dashboard.

The Android goal is to read Pico data directly over Wi-Fi and display it alongside ElectroDacus MQTT data. Prefer direct ElectroDacus publishing to an MQTT broker embedded in the app if practical; retain the Raspberry Pi broker as a supported fallback. SignalK integration is outside the Android scope.

## Directories in this repository

| Directory | Purpose |
| --- | --- |
| [`python/`](python/) | Existing Raspberry Pi scripts. `pico-mqtt.py` is the running implementation identified by the owner; `pico-json.py` and `pico-raw.py` are alternative tools. `brainsmoke.py` provides CRC16 support. `mqtt` holds runtime connection settings. |
| [`_old/`](_old/) | Earlier Python variants retained for reference. |
| [`node.js/`](node.js/) | Copy of the updated `pico2signalk` Node.js code (0.0.21). `lib/` contains discovery, TCP protocol, CRC and sensor-list code; `index.js` combines live UDP decoding with SignalK output. `bin/` contains a configuration dump tool and `test/` a CRC encoding test. |
| [`node.red/`](node.red/) | Existing Node-RED dashboard project. [`flows.json`](node.red/flows.json) defines displayed fields, conversions and charts; `package.json` defines the project. It subscribes to `/Ella/Pico/` and `/Ella/sbms`; preserve exact topic spelling and slashes. |
| [`simarine/`](simarine/) | Reference APK, `simarine.apk`, and JADX output in `simarine jadx/`. The inspected app uses .NET MAUI/Mono; Java decompilation does not establish that its managed Pico protocol implementation is available. |
| [`android/`](android/) | Android work and related documentation. The [standalone Pico base](android/pico-base/README.md) is implemented for live Pi validation before the Android port; see [PROCESS.md](android/PROCESS.md) and [BUILD-VALIDATION.md](android/BUILD-VALIDATION.md). |

## Related repositories

| Repository | Purpose and starting points |
| --- | --- |
| [pico2signalk](https://github.com/colonel-lp/pico2signalk) (`master`) | Updated Node.js fork, version 0.0.21. `lib/get-pico-config.js` handles discovery/retries, `lib/pico-protocol.js` TCP configuration/parsing, `lib/crc16.js` checksums and `lib/sensor-list.js` sensor positions. `index.js` contains live UDP decoding mixed with SignalK output. |

Use the newer Pico connectivity as a starting point while preserving the owner's Python additions and existing dashboard behaviour. Reconcile pitch/roll support, current polarity, temperature/pressure units and battery/tank calculations before porting.

Project working instructions: [agents.md](agents.md). Keep credentials out of new source, documentation, logs and test fixtures.
