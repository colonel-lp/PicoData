# Pico base changelog

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
