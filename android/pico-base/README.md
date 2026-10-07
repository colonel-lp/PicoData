# Ella Pico base — 0.1.0

Standalone Node.js starting point for the Android Pico reader. It uses the updated `../../node.js/` acquisition code, with the owner's `../../python/pico-mqtt.py` sensor mappings, calculations and Ella JSON format carried over. There is no MQTT connection, publishing or SignalK runtime dependency.

**Status:** automated tests pass. The owner's real Pico has not yet been tested with this base. Do not describe it as hardware-verified or start the Android port until the live checks below have supplied evidence.

## Run on the Pi

Use a current checkout of this repository and Node.js 18 or later. No npm packages are required to run the reader. Python 3 is needed only for comparison tests. Check the runtime first:

```bash
node --version
cd android/pico-base
node bin/pico.js --record pico-capture.jsonl --duration 60
```

Run from the repository root before `cd`. The Pi and Pico must be on the same Wi-Fi LAN. This discovers the Pico via UDP 43210, fetches its configuration via TCP 5001 and prints one Ella JSON object per second to stdout. Connection status goes to stderr. The local capture records configuration, TCP requests/replies and live packets; it contains no MQTT credentials.

If discovery fails, also test the known Pico address:

```bash
node bin/pico.js --ip 192.168.1.50 --record pico-fixed-ip.jsonl --duration 60
```

Replace the example address with the Pico's actual address. `DEBUG=pico` enables additional diagnostics. Omit `--duration` to run until Ctrl+C. The code does not change Pico settings, contact Mosquitto or modify the existing Python service. If binding fails because UDP 43210 is already occupied, stop the existing Pico reader only for the test, then restore it afterwards; otherwise both readers can remain running if the Pi permits the shared UDP binding.

## Verify the real capture

```bash
node bin/verify-capture.js pico-capture.jsonl --compare-python
```

The summary reports counts for configuration, TCP replies, live packets, length/checksum matches and Python output matches. Exit status 0 and `"ok":true` mean those checks passed for the recorded samples. Python comparison uses the functions and output block extracted from the existing source via AST; it does not import its MQTT/network setup or run the old service.

Receive framing assumes the big-endian length at header offsets 11–12 follows the layout used by upstream requests. The checker also tests incoming CRCs using the upstream outgoing CRC rule. Neither assumption has been confirmed on the owner's Pico yet. Raw TCP records are retained even when a read fails so an incompatible reply can be examined. Incoming CRCs are reported by the checker, not yet enforced by the live receiver.

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

Two deliberate invalid-data differences are documented: an unavailable/confirmed-invalid voltage is omitted or represented as null rather than 65.535 V; unavailable battery SOC/capacity is null rather than the old out-of-range SOC. Missing measurements are not replaced with zero. Signed raw 65535 temperature/current values retain Python interpretation; this value is not treated as a universal sentinel.

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
| `lib/readings.js` | Owner's conversions and JSON formatter. |
| `bin/pico.js` | Live JSON reader and evidence recorder. |
| `bin/verify-capture.js` | Real capture checks, Python comparison and replay. |
| `test/` | Synthetic fixtures, Python reference extraction and automated tests. |

`npm test` runs the tests. See [build validation](../BUILD-VALIDATION.md) for their results and remaining hardware checks. Preserve the upstream MIT notice in `LICENSE`; this baseline has not been published as a package or release. Once the Pi tests pass, port these components into Android's native networking/data layers and repeat device testing there.
