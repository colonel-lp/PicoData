# Pico base changelog

## 0.4.0 — 2026-10-08

- Receive ElectroDacus JSON from `/Ella/sbms` using the existing MQTT connection and credentials, independently of Pico acquisition. Retry subscription refusal and resubscribe after broker reconnection.
- Decode battery, PV1, PV2 and external-load currents from mA to A, SOC and pack voltage from privately configured active-cell channels. Preserve reported signs/valid zero readings and make unavailable voltage explicit.
- Timestamp reception using Pi UTC and monotonic time; retain source time separately. Ignore retained snapshots and exact repeats of source time/measurements, reject malformed input, and clear current state on timeout/disconnection.
- Keep Pico MQTT fields/values, topic/wire settings, service paths and quiet default unchanged. Add optional `--sbms-stdout`, `--no-sbms`, SBMS config keys and local normalized capture records; capture verification still checks Pico only.
- All 39 automated tests pass, including simultaneous reception/Python payload parity, wire subscription/refusal/reconnection and CLI operation without a Pico. Increase older CLI test startup windows to accommodate concurrent test processes.
- Document private configuration and pending Pi checks. SQLite logging, rollups, directional totals, history API and Android implementation are not included in this acquisition step.

## Repository layout — 2026-10-08 (version unchanged)

- Place the standalone collector in the repository root `node.js/`, matching the Pi installation layout. Update checkout/update commands, documentation links and source provenance.
- Preserve the original SignalK reference under `_old/pico2signalk/`; it is separate from the active collector. MQTT config remains in the parent `PicoData/mqtt`, and the service entry point remains `node.js/bin/pico.js`.

## 0.3.0 — 2026-10-08

- Enable MQTT by default using the parent `PicoData/mqtt` file for an installation directly in `PicoData/node.js`; retain explicit config override and add `--no-mqtt` for reader-only tests.
- Silence normal terminal/journal readings by default. `--stdout` enables diagnostic JSON; connection/error status remains available. MQTT payload fields/values and wire settings are unchanged.
- Add a generic systemd service template with the correct `bin/pico.js` entry point and document the existing-service update.
- Validate parent config resolution independently of working directory, silent publishing/Python parity and missing/conflicting config handling; all 26 tests pass. The unit template passed local systemd validation; actual Pi service startup remains pending.
- Record functional owner-reported recovery/dashboard outcomes without publishing system inventories, resource snapshots or account/address details.

## 0.2.0 — 2026-10-08

- Add optional `--mqtt-config FILE` publishing for the existing Mosquitto/Node-RED dashboard. Reuse Python's server, port, prefix, username and password settings.
- Preserve the configured topic, JSON field structure/values, units, exact labels, QoS 0 and retain=false. Use original raw-65535 conversions when MQTT is enabled; stdout publishes the same compatibility object. JSON formatting is not byte-identical to Python's serializer.
- Keep Pico and broker lifecycles independent. Retry MQTT without retaining/replaying old readings, bound pending writes, and stop both transports cleanly.
- Pin MQTT.js dependencies, ignore local credential files, add placeholder configuration, and document flat Pi installation.
- Extend automated validation to 24 tests, including wire settings, Python payload parity, broker refusal, reconnect, buffering and CLI execution. Actual Pi MQTT/Node-RED verification remains pending.

## 0.1.0 — 2026-10-07

- Build a standalone Node.js acquisition base using the updated pico2signalk connection methods and the owner's Python sensor calculations/Ella JSON.
- Add fragmented TCP response handling, sender filtering, retry/stale recovery and lifecycle cleanup. Exclude SignalK and initially omit MQTT transport.
- Provide raw capture recording, offline verification/replay and Python comparison.
- Make flat installation self-contained by bundling the unchanged Python comparison source; 16 automated tests pass.
- Record the owner's real Pico verification: 108 TCP replies and 1,096 UDP packets pass length/CRC checks; all 1,096 outputs match Python with zero differences. Real reconnect testing remains pending.
