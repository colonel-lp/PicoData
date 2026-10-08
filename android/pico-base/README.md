# Ella Pico base — 0.2.0

Standalone Node.js starting point for the Android Pico reader. It uses the updated `../../node.js/` acquisition code, with the owner's `../../python/pico-mqtt.py` sensor mappings, calculations and Ella JSON format carried over. MQTT publishing is optional and uses the existing Python configuration/topic and Ella JSON contract. SignalK remains excluded.

**Status:** 24 automated tests pass, including MQTT wire tests and Python payload comparison. On 2026-10-07 the owner reported a successful real Pi/Pico capture: all 108 TCP replies and 1,096 UDP packets passed length/CRC checks; all 1,096 decoded outputs matched the original Python, with zero differences. Visual comparison and real restart/Wi-Fi recovery checks remain pending. See [build validation](../BUILD-VALIDATION.md) for evidence and remaining checks.

## Run on the Pi

Use Node.js 18 or later. The reader without MQTT needs no installed npm packages; MQTT publishing requires `npm ci --omit=dev` to install the pinned MQTT.js dependency. Python 3 is needed only for comparison tests. All files inside `pico-base/` can be installed directly in `~/PicoData/node.js`; the original repository folders are not required. For that installation:

```bash
node --version
cd ~/PicoData/node.js
node bin/pico.js --record pico-capture.jsonl --duration 60
```

For an unchanged repository checkout, use `cd android/pico-base` from the repository root instead. The Pi and Pico must be on the same Wi-Fi LAN. This discovers the Pico via UDP 43210, fetches its configuration via TCP 5001 and prints one Ella JSON object per second to stdout. Connection status goes to stderr. The local capture records configuration, TCP requests/replies and live packets; it contains no MQTT credentials.

If discovery fails, also test the known Pico address:

```bash
node bin/pico.js --ip 192.168.1.50 --record pico-fixed-ip.jsonl --duration 60
```

Replace the example address with the Pico's actual address. `DEBUG=pico` enables additional diagnostics. Omit `--duration` to run until Ctrl+C. The code does not change Pico settings or modify the existing Python service. It contacts the MQTT broker only when `--mqtt-config` is supplied. If binding fails because UDP 43210 is already occupied, stop the existing Pico reader only for the test, then restore it afterwards; otherwise both readers can remain running if the Pi permits the shared UDP binding.

## Publish to the existing Node-RED dashboard

Install dependencies, then point the reader at the same `mqtt` configuration file used by Python:

```bash
cd ~/PicoData/node.js
npm ci --omit=dev
node bin/pico.js --mqtt-config /absolute/path/to/your/existing/mqtt
```

Replace the path with the actual file location. If the file is already in `~/PicoData/node.js`, use `--mqtt-config mqtt`. No credentials need to be shared or entered into source. `mqtt.example` shows the five existing keys: `server`, `port`, `prefix`, `username`, `password`. The parser preserves '=' inside a password and uses the exact `prefix` as the publishing topic; it does not append a suffix. The current dashboard subscribes to `/Ella/Pico/`.

One reading object is published roughly once per second, matching stdout. MQTT uses the original MQTT 3.1.1 protocol, 60-second keepalive, QoS 0 and retain=false. There is no wrapper, status field, added measurement or changed label/unit. JSON structure, keys and values match the Python source, including trailing spaces in labels, battery voltage duplication, hidden-name filtering and rounding. JSON whitespace, Unicode escaping and spelling such as `12` versus `12.0` can differ; Node-RED receives the same parsed object.

With MQTT enabled, decoding preserves Python's original raw-65535 voltage/SOC calculations rather than the newer null/omission handling. Incomplete snapshots are skipped instead of inventing fields. The same compatibility object is printed to stdout and sent to MQTT. Normal readings in either mode already matched the owner's real capture. Wire/CLI tests cover normal output, and additional Python comparisons cover the legacy sentinel values.

Broker connection/reconnection runs independently of Pico acquisition. Connection errors go to stderr without credentials. MQTT retries every five seconds, including broker refusal; readings and capture recording continue while it is unavailable. No offline reading backlog or automatic snapshot replay is sent after reconnection: the next fresh reading is published. One pending write limits buffering under backpressure. Ctrl+C stops both transports.

For an unambiguous dashboard comparison, run only one publisher on the original Pico topic at a time; otherwise Python and Node.js updates can interleave. ElectroDacus settings and `/Ella/sbms` are unchanged. Actual Mosquitto/Node-RED testing on the owner's Pi is still pending.

## Verify the real capture

```bash
node bin/verify-capture.js pico-capture.jsonl --compare-python
```

The summary reports counts for configuration, TCP replies, live packets, length/checksum matches and Python output matches. Exit status 0 and `"ok":true` mean those checks passed for the recorded samples. Python comparison extracts the functions and output block via AST from the bundled `test/reference/pico-mqtt.py`; it does not import its MQTT/network setup or run the old service. That file is an unchanged snapshot of `python/pico-mqtt.py`, Git blob `eb99d3dfc57c7c9d6ca721754015a296fb086741`. Refresh it deliberately if the agreed Python baseline changes. Both comparison tests and capture verification work without a sibling `python/` folder.

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
| `bin/pico.js` | Live JSON reader and evidence recorder. |
| `bin/verify-capture.js` | Real capture checks, Python comparison and replay. |
| `test/` | Synthetic fixtures, Python reference extraction and automated tests. |

`npm ci` followed by `npm test` runs all tests. [CHANGELOG.md](CHANGELOG.md) records the complete base history. See [build validation](../BUILD-VALIDATION.md) for their results and remaining hardware checks. Preserve the upstream MIT notice in `LICENSE`; this baseline has not been published as a package or release. Once the Pi tests pass, port these components into Android's native networking/data layers and repeat device testing there.
