# Build validation

## Pico base 0.2.0 — optional MQTT addition, 2026-10-08

Source parent: `4e786e95ed70fa6b07ae67537b04dfca80915e7e`. MQTT.js is pinned to 5.16.0 with a committed npm lockfile; the MQTT wire fixture uses mqtt-packet 9.0.2. No version or APK change was made to an Android application; this remains the Node.js base.

`npm test`: **24 passed, 0 failed**, on Node.js 24.19.0 and Python 3.12.14. The existing acquisition/parser/recovery/comparison tests remain included.

New checks cover:

- Existing five-key configuration, exact topic/slashes, authentication and '=' in passwords; invalid/missing configuration is rejected.
- Legacy MQTT JSON compared with the original Python output, including raw 65535 voltage/SOC and signed temperature, accented labels and trailing spaces.
- Incomplete legacy snapshots are rejected rather than filled with invented values.
- Actual loopback MQTT 3.1.1 CONNECT/PUBLISH packets: credentials, keepalive 60, QoS 0, retain=false, exact topic and no will.
- Connection loss/reconnect, denied authentication/retry, no offline replay and stop without a retry loop.
- Bounded pending writes and delayed callbacks across reconnection.
- Full Pico CLI execution with MQTT disabled, available, and access denied. With MQTT available, received payloads equal stdout and the Python oracle; with access denied, Pico stdout/capture still work and the CLI exits normally. Diagnostics exclude credentials.

The MQTT endpoint is a loopback protocol fixture, not Mosquitto. Real Pi MQTT/Node-RED tests and real Pico/Wi-Fi recovery checks remain pending. No collector/logging/API/UI, ElectroDacus subscription or Android APK was added. The owner's 0.1.0 Pico capture below remains the recorded real-hardware evidence.

## Pico base 0.1.0 — 2026-10-07

Source baseline: `PicoData/main` at `2846a9d74fb6bdf4d09f11e43eb9f0746d83a3da`. Acquisition reference: in-repository `node.js/` (pico2signalk 0.0.21). Sensor/output reference: `python/pico-mqtt.py` (blob `eb99d3dfc57c7c9d6ca721754015a296fb086741`). Dashboard reference remains `node.red/flows.json`.

This is a standalone Node.js reference implementation in `android/pico-base/`, not an Android application or APK.

## Completed checks

`npm test`: **16 passed, 0 failed**, using Node.js 24.19.0 and Python 3.12.14 in the development environment. Re-run after correcting support for a flat installation in `~/PicoData/node.js`.

- Golden CRC/request vectors from the upstream test.
- Six synthetic sensor/value sets compared with the actual Python sensor-list functions, reading functions and output block. They cover all handled types, battery/tank calculations, current signs, negative temperatures, pitch/roll, hidden names, trailing spaces and duplicate labels.
- Python ties-to-even rounding and signed temperature edge behaviour.
- Missing data, unavailable SOC and the voltage sentinel; deliberate differences are documented separately.
- Binary type-3 fields, truncated packets, unknown field types and unterminated strings.
- Literal labels that resemble object prototype keys.
- Fragmented TCP replies, incomplete closure, timeout and listener cleanup.
- Discovery filtering/cancellation and live sender/type filtering.
- Loopback TCP/UDP Pico simulators for both discovery and fixed-IP operation, including failed configuration, stale/reconnect recovery, stop and restart.
- Actual CLI execution against the simulator, JSON stdout, capture recording and Python comparison of the recorded samples.
- Capture verification rejects empty evidence and bad CRCs.
- An isolated copy containing only the base's files in `PicoData/node.js` verifies a synthetic capture against Python successfully, without the original `android/` or `python/` folders. The bundled Python reference has the same Git blob hash as the original baseline (`eb99d3dfc57c7c9d6ca721754015a296fb086741`).

The automated tests use synthetic packets. Loopback tests do not establish Wi-Fi broadcast delivery, firmware compatibility, changed-IP recovery on the real LAN or Android behaviour.

## Owner-reported real Pico capture — 2026-10-07

The owner ran the base from `~/PicoData/node.js` on the Pi and supplied this terminal result for `node bin/verify-capture.js pico-capture.jsonl --compare-python`:

```json
{"configurations":2,"packets":1096,"tcpResponses":108,"tcpLengthMatches":108,"tcpCrcMatches":108,"udpLengthMatches":1096,"udpCrcMatches":1096,"pythonMatches":1096,"pythonDifferences":0,"ok":true}
```

All 108 complete TCP responses and 1,096 live UDP packets passed length and CRC checks. All 1,096 decoded output objects matched the original Python calculations/formatting, with zero differences. The same capture passed without Python comparison in 1.432 seconds; Python comparison completed after a longer silent wait because the current verifier launches Python once per packet.

This is owner-reported hardware evidence; the raw capture has not been supplied to the agent for independent inspection. Two configurations alone do not establish recovery after a reboot or Wi-Fi interruption. Device/firmware identity, Pi runtime version, discovery mode and visual comparison remain unrecorded.

## Pending real Pi checks

- [ ] Record the Pico firmware/device identity and Pi Node.js version.
- [x] Retrieve configuration and live packets from the owner's Pico (owner-reported capture result).
- [ ] Confirm automatic discovery mode on the common LAN.
- [x] Pass `verify-capture.js ... --compare-python`: 1,096 matches, zero differences.
- [ ] Verify all expected names/values against the current dashboard and Pico display, especially charge/discharge current signs, tank percentage, battery capacity and both inclinometer axes.
- [ ] Test Pico power-cycle, Wi-Fi interruption and automatic discovery after an address change.
- [ ] Check UDP binding while the existing Python reader runs; restore the original service after any temporary test stop.
- [ ] Confirm Ctrl+C exits and a second start binds/connects normally.

Incoming framing/CRC interpretation was derived from upstream request layouts and passed every recorded response/packet in the owner's reported capture. This validates that capture, not every firmware or failure condition. The recorder includes raw TCP data to diagnose failures. The live receiver currently bounds-checks fields but does not enforce receive CRCs.

No real Pico, SBMS0, Pi or Android hardware test has been performed by the agent. The original 0.1.0 step built no MQTT transport; 0.2.0 adds the optional publisher described above. No APK, production broker or SignalK integration has been built.
