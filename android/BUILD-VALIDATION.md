# Build validation

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

Synthetic packets are explicitly synthetic; none were captured from the owner's hardware. Loopback tests do not establish Wi-Fi broadcast delivery, firmware compatibility, changed-IP recovery on the real LAN or Android behaviour.

## Pending real Pi checks

- [ ] Record the Pico firmware/device identity and Pi Node.js version.
- [ ] Discover the Pico on the common LAN and retrieve all configuration entries.
- [ ] Pass `verify-capture.js ... --compare-python`, or explain every protocol/invalid-value difference using the real capture.
- [ ] Verify all expected names/values against the current dashboard and Pico display, especially charge/discharge current signs, tank percentage, battery capacity and both inclinometer axes.
- [ ] Test Pico power-cycle, Wi-Fi interruption and automatic discovery after an address change.
- [ ] Check UDP binding while the existing Python reader runs; restore the original service after any temporary test stop.
- [ ] Confirm Ctrl+C exits and a second start binds/connects normally.

Incoming framing/CRC interpretation is derived from upstream request layouts and remains a hardware-validation item. The recorder includes raw TCP data to diagnose failures. The live receiver currently bounds-checks fields but does not enforce receive CRCs before those are confirmed against actual packets.

No real Pico, SBMS0, Pi or Android hardware test has been performed by the agent. No APK, MQTT transport, broker or SignalK integration has been built in this step.
