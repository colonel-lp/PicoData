# Next build changes

## Current authorized step: Pico base 0.3.0 for service operation

- [x] Start with updated Node.js discovery/configuration and TCP request methods.
- [x] Carry over the owner's sensor mappings, pitch/roll and Ella JSON calculations/formatting from Python.
- [x] Exclude SignalK integration. The initial MQTT exclusion was superseded by the owner's 2026-10-08 request for publishing to the existing dashboard.
- [x] Add fragmented TCP reads, error/retry handling, sender checks and clean shutdown.
- [x] Compare normal readings with the original Python functions and test simulated connection failures/recovery.
- [x] Provide recording, verification and replay commands for a live Pi test.
- [x] Support installation directly in `~/PicoData/node.js`, including a bundled Python comparison reference so deleted original folders are not needed.
- [x] Receive configurations and live readings from the owner's actual Pico; 1,096 captured outputs match the original Python, with zero differences (owner-reported verification).
- [x] Confirm real receive frame lengths/checksums for the reported capture: 108 TCP responses and 1,096 UDP packets all pass.
- [ ] Confirm automatic discovery mode; retain device identity/runtime inventory privately unless the owner requests publication.
- [x] Owner reports successful Pico reboot/Wi-Fi recovery and correct-looking MQTT output in the existing Node-RED webpage.
- [ ] Audit expected values against the Pico display, test changed IP and actual broker restart, and run a long unattended stability check.

## MQTT addition — 2026-10-08

- [x] Add optional publishing using the existing five-key Python `mqtt` configuration file.
- [x] Preserve the exact configured topic, parsed JSON structure/values/labels/units, QoS 0 and non-retained messages.
- [x] Restore original raw-65535 value handling in MQTT compatibility mode; reject incomplete output snapshots.
- [x] Add independent reconnect, no offline backlog/resync, bounded pending writes and clean shutdown.
- [x] Test actual MQTT wire settings and received CLI payloads against the Python source; verify Pico output continues with broker access denied.
- [x] Owner reports successful publishing/display through the existing Mosquitto/Node-RED setup.

## Service changes — 2026-10-08

- [x] Use the parent `PicoData/mqtt` configuration by default for files installed directly in `PicoData/node.js`, independent of the working directory.
- [x] Remove normal readings from terminal/journal output by default; retain connection/error status. `--stdout` enables diagnostic JSON and `--no-mqtt` enables reader-only testing.
- [x] Add a generic systemd service template with the correct `bin/pico.js` path, network/broker ordering and a 30-second process restart interval.
- [x] Confirm default config resolution and silent MQTT payload parity in an isolated installation; missing config fails visibly.
- [x] Owner reports clean shutdown/restart and successful MQTT/Pico live connections with the updated service.
- Publish functional test outcomes only; exclude owner runtime/process snapshots, device inventory, addresses, credentials and account details.

## Repository layout — 2026-10-08

- [x] Move the standalone collector to the repository root `node.js/` and remove its former folder.
- [x] Preserve the original SignalK reference under `_old/pico2signalk/` to avoid mixing it with the collector.
- [x] Update documentation, links, code provenance and checkout/update examples. The parent MQTT config and deployed service path are unchanged.

## Proposed Pi logging direction

- Keep collection/logging on the always-on, headless Pi Zero 2 W; the head unit is not continuously powered.
- Prefer one focused service over an expanding dashboard/database stack. Measure actual memory/CPU before replacing working services.
- SQLite plus a small history API is the preferred proposal; sampling, retention and final display/app architecture remain to be agreed. No logging/API/UI implementation is included in this MQTT addition.
- Keep history on persistent storage outside zram-managed directories (proposed `/var/lib/ella/`), with batched durable commits; reserve `/var/log` for diagnostics.
- Add ElectroDacus MQTT decoding later, preserving the existing Mosquitto destination during validation.

## Later Android work

- Start the Android port after reviewing the live Pi evidence; confirm target hardware, Android range, layout and background requirements first.
- Add ElectroDacus MQTT reception later. Review Android's role as a viewer/client of the Pi logger; direct Pico/embedded-broker operation can remain a separate future option. The current step implements optional Pico MQTT publishing only.
- Keep SignalK functionality outside the Android scope.

## Database planning — 2026-10-08

- [x] Review existing Pico output and dashboard SBMS mappings; record the discussion draft in [`node.js/DATABASE-PLAN.md`](../node.js/DATABASE-PLAN.md).
- [ ] Agree logged measurements, power/energy inputs and signs, sampling/retention, daily timezone and acceptable uncommitted-data window.
- [ ] Confirm a live SBMS payload, active cell channels and directly available solar measurements; an unwired dashboard gauge is not evidence of a working reading.
- [ ] Implement and benchmark the agreed persistent SQLite history only after implementation is requested. Keep legacy MQTT output unchanged, separate sources, and preserve gaps/validity in summaries and energy totals.
