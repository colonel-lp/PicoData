# Ella Pico base — 0.7.1

Standalone Node.js starting point for the Android Pico reader. It uses the updated acquisition code preserved in [`../_old/pico2signalk/`](../_old/pico2signalk/), with the owner's [`../python/pico-mqtt.py`](../python/pico-mqtt.py) sensor mappings, calculations and Ella JSON format carried over. MQTT publishing is enabled by default using `PicoData/mqtt` and the existing Ella JSON contract; `--no-mqtt` selects reader-only operation. SignalK remains excluded.

**Status:** version 0.7.2 separates live display deadlines from logging gap limits and keeps an unexpired accepted display through an isolated rejected SBMS payload. Actual stale/disconnects still clear readings and rejected data interrupts logging coverage. Version 0.7.1 exposes the full live SBMS broadcast and boolean On/Off flags without adding them to history. Version 0.7.0 adds an optional authenticated local API for live Pico/SBMS readings, source/measurement metadata and selected history. It uses the existing Node.js service and SQLite schema 2; MQTT payloads, acquisition, combined battery history and retention are unchanged. See [API setup and contract](API.md) and [logging setup](LOGGING.md). The owner's uploaded live history and migration backup passed integrity/preservation checks; The owner has confirmed local Pi API status/live/history requests; LAN/TLS and hardware performance still need testing. Ella Monitoring Android source is in android/source; the owner builds the APK. See [build validation](../android/BUILD-VALIDATION.md) for automated evidence and pending checks.

## Run on the Pi

Use Node.js 22.13+ with built-in SQLite for logging; Node.js 18+ remains sufficient without logging. Run `npm ci --omit=dev` to install the pinned MQTT.js dependency. Reader-only operation with `--no-mqtt` needs no installed npm packages. Python 3 is needed only for comparison tests. The complete test suite, including SQLite tests, requires Node 22.13+ and Python 3; its HTTPS test also uses OpenSSL. The repository's root `node.js/` folder is the self-contained collector and can be installed directly in `~/PicoData/node.js`; the original repository folders are not required. For that installation:

```bash
node --version
cd ~/PicoData/node.js
node bin/pico.js --record pico-capture.jsonl --duration 60
```

For an unchanged repository checkout, use `cd node.js` from the repository root instead. The Pi and Pico must be on the same Wi-Fi LAN. This discovers the Pico via UDP 43210, fetches its configuration via TCP 5001 and publishes one Ella JSON object roughly once per second to MQTT. It also receives SBMS data independently. Live readings are not printed by default; `--stdout` enables Pico terminal JSON and `--sbms-stdout` enables decoded SBMS JSON. Connection status goes to stderr. The local capture records configuration, TCP requests/replies, live Pico packets and decoded SBMS samples; it contains no MQTT credentials. Keep captures private: they contain measurements, source timestamps and runtime metadata.

If discovery fails, also test the known Pico address:

```bash
node bin/pico.js --ip 192.168.1.50 --record pico-fixed-ip.jsonl --duration 60
```

Replace the example address with the Pico's actual address. `DEBUG=pico` enables additional diagnostics. Omit `--duration` to run until Ctrl+C. The code does not change Pico settings or modify the existing Python service. It uses the default MQTT configuration unless `--no-mqtt` is supplied. `--mqtt-config FILE` overrides the location. If binding fails because UDP 43210 is already occupied, stop the existing Pico reader only for the test, then restore it afterwards; otherwise both readers can remain running if the Pi permits the shared UDP binding.

## Publish to the existing Node-RED dashboard

Keep the previous files in `PicoData/python/`, the new base's files directly in `PicoData/node.js/`, and a copy of the existing `mqtt` configuration in `PicoData/mqtt`. Install dependencies and run:

```bash
cd ~/PicoData/node.js
npm ci --omit=dev
node bin/pico.js
```

The default config path is resolved from the script's installed location: `node.js/bin/pico.js` reads `../../mqtt`, which is `PicoData/mqtt`. It does not depend on the working directory and does not read the old Python folder. The same default works in a full repository checkout. For a different config location, pass `--mqtt-config /path/to/mqtt`. `--no-mqtt` disables both publishing and SBMS reception; it cannot be combined with `--mqtt-config`. No credentials need to be shared or entered into source. `mqtt.example` shows the five existing keys: `server`, `port`, `prefix`, `username`, `password`, plus optional SBMS settings. The parser preserves '=' inside a password and uses the exact `prefix` as the publishing topic; it does not append a suffix. The current dashboard subscribes to `/Ella/Pico/`.

One reading object is published roughly once per second. When `--stdout` is requested, stdout receives the same object. MQTT uses the original MQTT 3.1.1 protocol, 60-second keepalive, QoS 0 and retain=false. There is no wrapper, status field, added measurement or changed label/unit. JSON structure, keys and values match the Python source, including trailing spaces in labels, battery voltage duplication, hidden-name filtering and rounding. JSON whitespace, Unicode escaping and spelling such as `12` versus `12.0` can differ; Node-RED receives the same parsed object.

With MQTT enabled, decoding preserves Python's original raw-65535 voltage/SOC calculations rather than the newer null/omission handling. Incomplete snapshots are skipped instead of inventing fields. The compatibility object is sent to MQTT and is printed to stdout only with `--stdout`. Normal readings in either mode already matched the owner's real capture. Wire/CLI tests cover normal output, and additional Python comparisons cover the legacy sentinel values.

Broker connection/reconnection runs independently of Pico acquisition. Connection errors go to stderr without credentials. MQTT retries every five seconds, including broker refusal; readings and capture recording continue while it is unavailable. No offline reading backlog or automatic snapshot replay is sent after reconnection: the next fresh reading is published. One pending write limits buffering under backpressure. Ctrl+C stops both transports.

For an unambiguous dashboard comparison, run only one publisher on the original Pico topic at a time; otherwise Python and Node.js updates can interleave. ElectroDacus settings and `/Ella/sbms` are unchanged. The owner reports successful Pico publishing/display through the existing Mosquitto/Node-RED setup. Actual broker-restart testing remains pending.

## Receive ElectroDacus data

SBMS reception shares the existing MQTT connection and credentials, with an exact-topic QoS 0 subscription to `/Ella/sbms`. Pico acquisition continues independently when SBMS data is missing or subscription access is denied; SBMS reception continues when the Pico is unavailable. The broker account must be allowed to subscribe to this topic. No second broker, connection or dependency is added, and SBMS data is not republished.

Add settings to your private parent `PicoData/mqtt` file as needed:

| Optional key | Meaning |
| --- | --- |
| `sbms_topic=/Ella/sbms` | Exact subscription topic; default shown. Must differ from the Pico publishing topic. |
| `sbms_cells=1,2,3,4` | Illustrative active-cell channels, numbered 1–8. Replace with the configured cell channels for your installation. No cell map is guessed by default. |
| `sbms_stale_seconds=30` | Timeout after the last accepted sample; default 30 seconds. Choose against the confirmed reporting cadence. |

All four `currentMA` fields are divided by 1000 into amps, preserving the source signs and valid zero readings. Output fields are `current.battery`, `current.pv1`, `current.pv2` and `current.externalLoad`; `stateOfCharge` is percent. Pack `voltage` is the sum of all configured active `cellsMV` channels divided by 1000. Without a cell map, voltage is null with `voltageStatus: "unconfigured"`. An unavailable selected cell makes voltage null with `voltageStatus: "unavailable"`; current/SOC can still be received. A valid derived voltage has `voltageStatus: "valid"`. Power and directional totals are not calculated at this stage.

Each sample has Pi UTC `receivedAt`, monotonic receipt milliseconds for future integration, and the device `sourceTime` kept separately. An incorrect device calendar does not set the acquisition timestamp. Monotonic values only compare within one process run. Required JSON fields/types are validated and payloads above 64 KiB are rejected. The decoded reading includes `broadcast`, preserving the entire accepted JSON object in original units, and `flags`, exposing known boolean states as true/false or null when missing/invalid. These extra fields are live-only and do not extend database logging.

Retained MQTT snapshots are ignored. Exact repeats of the complete broadcast do not refresh freshness, even when object keys are reordered; advancing source time or changed measurements, flags or auxiliary values within the same second are accepted. Neither malformed messages nor reconnection renews a sample's age. Broker disconnection or the timeout clears the receiver's current sample. These rules do not prove that the original publisher always sends fresh measurements: reporting cadence and replay behaviour need a real Pi check before energy integration.

Status records use `source: "sbms"`: `subscribing`, `waiting-data`, `connected`, `stale`, `invalid-message`, `subscription-error`, `disconnected` and `stopped`. `connected` means an accepted sample has arrived; rejected JSON emits a diagnostic without renewing its deadline, and only uses invalid-message state when no accepted display remains; subscription refusal retries after five seconds. Reception resubscribes after broker reconnection. Use `--no-sbms` for Pico-only MQTT operation; `--sbms-stdout` requires MQTT and cannot be combined with `--no-sbms`. Service operation stays silent except for status.

For a short diagnostic check, stop the service first, then restore it afterwards:

```bash
sudo systemctl stop pico-mqtt.service
cd ~/PicoData/node.js
node bin/pico.js --sbms-stdout --duration 15
sudo systemctl start pico-mqtt.service
```

## Update an existing flat installation

These commands copy only the base's files into `PicoData/node.js`; they do not move the old Python files or overwrite the parent `PicoData/mqtt` file:

```bash
pico_update_dir=$(mktemp -d)
git clone --depth 1 --filter=blob:none --sparse https://github.com/colonel-lp/PicoData.git "$pico_update_dir"
git -C "$pico_update_dir" sparse-checkout set node.js
cp -a "$pico_update_dir/node.js/." ~/PicoData/node.js/
cd ~/PicoData/node.js
npm ci --omit=dev
```

## Run as a systemd service

Use [service/pico-mqtt.service](service/pico-mqtt.service) as a template, replacing `YOUR_USER` everywhere with the account running the reader. Check `command -v node` and use that absolute executable path in `ExecStart`. The script path ends in `node.js/bin/pico.js`, not `node.js/pico.js`. No MQTT argument is required with the parent `PicoData/mqtt` layout.

Edit the existing service with `sudo systemctl edit --full pico-mqtt.service`, then apply it:

```bash
sudo systemctl daemon-reload
sudo systemctl restart pico-mqtt.service
sudo systemctl status pico-mqtt.service --no-pager
journalctl -u pico-mqtt.service -n 30 --no-pager
```

Connection/retry/error messages remain available through the journal; regular sensor JSON does not fill it. `RestartSec=30` waits 30 seconds after process exit. Keep explanatory comments on their own lines rather than after a directive. This restart policy is separate from the reader's internal Pico and MQTT reconnect loops. A foreground test reader should be stopped before starting the service so only one publisher updates the topic.

For a terminal-only check without MQTT:

```bash
node bin/pico.js --no-mqtt --stdout --duration 60
```

## Verify the real capture

```bash
node bin/verify-capture.js pico-capture.jsonl --compare-python
```

The summary reports counts for configuration, TCP replies, live packets, length/checksum matches and Python output matches. Exit status 0 and `"ok":true` mean those checks passed for the recorded Pico samples. SBMS records are ignored by this verifier; it does not validate SBMS acquisition or calculations. Python comparison extracts the functions and output block via AST from the bundled `test/reference/pico-mqtt.py`; it does not import its MQTT/network setup or run the old service. That file is an unchanged snapshot of `python/pico-mqtt.py`, Git blob `eb99d3dfc57c7c9d6ca721754015a296fb086741`. Refresh it deliberately if the agreed Python baseline changes. Both comparison tests and capture verification work without a sibling `python/` folder.

Receive framing assumes the big-endian length at header offsets 11–12 follows the layout used by upstream requests. The checker also tests incoming CRCs using the upstream outgoing CRC rule. Both rules passed all recorded TCP replies and UDP packets in the owner's reported 2026-10-07 capture; compatibility with other firmware remains untested. Raw TCP records are retained even when a read fails so an incompatible reply can be examined. Incoming CRCs are reported by the checker, not yet enforced by the live receiver.

If the summary fails, supply the capture and terminal diagnostics for investigation. Do not alter the protocol layout or calculations to make synthetic tests pass. If the capture contains unavailable battery SOC or the upstream-confirmed 65535 voltage sentinel, the checker can report expected differences from the old Python output; review these explicitly.

For offline JSON replay:

```bash
node bin/verify-capture.js pico-capture.jsonl --replay
```

## What is preserved

- Sensor configuration and live field positions; `PICO INTERNAL` spans six fields, current/barometer two and battery five. Type-13 pitch/roll support is copied from Python; type-14 handling is retained from the newer fork.
- Exact sensor labels, including the two trailing spaces in `Ella  `; names containing `[` remain excluded from the public JSON. Duplicate names follow the Python configuration order.
- Celsius, Python current signs and thresholds, pressure scaling, battery SOC/capacity calculations, tank remaining capacity and ties-to-even percentage rounding.
- The `time`, `barometer`, `inclinometer`, `voltage`, `current`, `temperature`, `tank` and `battery` sections, including battery voltage duplicated into `voltage`. Raw ohm readings remain internal because the old JSON does not export them.
- Local-time timestamp components and roughly one output per second. Key order and numeric spelling such as `12` versus `12.0` are not promised to be byte-identical JSON.

Without MQTT, two deliberate invalid-data differences remain: an unavailable/confirmed-invalid voltage is omitted or represented as null rather than 65.535 V; unavailable battery SOC/capacity is null rather than the old out-of-range SOC. Missing measurements are not replaced with zero. Signed raw 65535 temperature/current values retain Python interpretation; this value is not treated as a universal sentinel.

## Connection changes

The upstream UDP discovery, optional fixed IP, TCP request sequence, CRC, retry timing and sensor-list approach form the base. A single UDP socket remains bound through configuration and live reception. TCP replies are assembled across chunks; failed/closed/incomplete reads cause a fresh configuration attempt. Live data is restricted to the configured sender and Pico message type. After 15 seconds without a usable live packet the client reports stale, retries after 30 seconds and rediscovers the address unless `--ip` fixes it. Stop cancels outstanding connections, timers and listeners; the client can be started again.

Packet fields are bounds-checked. Unknown field types, broken separators and unterminated strings are rejected rather than guessed. A single UDP message supplies each output snapshot; old values are not carried forward as fresh readings.

## Files

| Path | Purpose |
| --- | --- |
| `lib/client.js` | Discovery, configuration, live reception, retry and lifecycle events. |
| `lib/pico-protocol.js` | TCP requests/framing and binary field parser. |
| `lib/crc16.js` | Upstream CRC implementation, unchanged. |
| `lib/sensor-list.js` | Upstream sensor mapping plus the owner's inclinometer support. |
| `lib/readings.js` | Owner's conversions and JSON formatter, with optional legacy Python value handling. |
| `lib/mqtt-publisher.js` | Existing config parser, optional MQTT publisher, reconnect and bounded buffering. |
| `mqtt.example` | Configuration layout with placeholder credentials. |
| `lib/sbms.js` | SBMS config, decoding, subscription/reconnect and independent sample freshness. |
| `lib/api.js`, `lib/api-config.js` | Optional authenticated HTTP/HTTPS API and private listener configuration. |
| `lib/history-reader.js` | Read-only catalogue, indexed range queries, pagination and source-referenced history. |
| `bin/init-api.js`, `bin/api-check.js` | Generate private API credentials and make authenticated local checks. |
| `API.md` | API setup, routes, data contract, limits and pending Pi checks. |
| `bin/pico.js` | MQTT reader with silent readings by default, optional stdout and evidence recording. |
| `service/pico-mqtt.service` | Generic systemd unit template for a flat installation. |
| `bin/verify-capture.js` | Real capture checks, Python comparison and replay. |
| `test/` | Synthetic fixtures, Python reference extraction and automated tests. |

`npm ci` followed by `npm test` runs all tests. [CHANGELOG.md](CHANGELOG.md) records the complete base history. See [build validation](../android/BUILD-VALIDATION.md) for their results and remaining hardware checks. Preserve the upstream MIT notice in `LICENSE`; this baseline has not been published as a package or release. Use this validated acquisition base for the proposed Pi collector/logger; review the Android viewer/direct-connection role before porting and repeat device testing there.


## Continuous history

Follow [LOGGING.md](LOGGING.md) to create private `PicoData/logging.json`, start logging with the existing service, inspect saved summaries and replay a local capture. The default persistent database is `PicoData/history/history.sqlite`, outside `node.js/` so source updates preserve history. [logging.example.json](logging.example.json) uses fictional IDs; generate your real selection from the private configuration capture.

For the combined battery record use `node bin/history.js --battery --limit 3`. Keep the existing private configuration/database during an upgrade; the collector performs the schema migration on startup.

## Local API

Follow [API.md](API.md) to create private parent `PicoData/api.json` and access status, live readings, measurement metadata and selected history. The API is disabled when that file is absent. It requires configured logging; `--api-config FILE` selects a different file and `--no-api` disables it. It uses the same service, without changing the MQTT dashboard or adding npm dependencies. Android remains a subsequent stage.
