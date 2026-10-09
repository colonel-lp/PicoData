# Next build changes

## Current authorized step: ElectroDacus reception in collector 0.4.0

- [x] Receive `/Ella/sbms` on the existing MQTT connection independently of Pico availability; resubscribe after reconnection and retry denied subscription without stopping publishing.
- [x] Decode all four mA current fields to amps, SOC and pack voltage from a private configured cell map; include PV2 and preserve valid zeros/current signs.
- [x] Use Pi UTC receipt time and keep monitor time separately. Reject invalid payloads, ignore retained/exact repeated samples, and clear current readings on stale timeout or broker disconnect.
- [x] Preserve original Pico MQTT output and quiet service operation; add `--sbms-stdout`, `--no-sbms` and local SBMS capture records for diagnostics.
- [x] Owner supplied a short real Pi run showing simultaneous Pico/SBMS connection, approximately one SBMS sample per second, valid derived pack voltage and normal timed shutdown. Keep raw readings/timestamps/runtime details private.
- [ ] Check actual broker restart, stale/repeated/retained behaviour and long unattended SBMS collection. Confirm charging polarity under known conditions before directional integration.
- [ ] Implement selected SQLite history, rollups/retention and restart recovery next, followed by the local history API. No database/API is added in 0.4.0.

## Previous Pico base and service work

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
- SQLite plus a small history API is the preferred proposal; the owner's selected logging/retention rules are in `node.js/DATABASE-PLAN.md`, with calculation details and final display/app architecture still to be agreed. No logging/API/UI implementation is included in this MQTT addition.
- Keep history on persistent storage outside zram-managed directories (proposed `/var/lib/ella/`), with batched durable commits; reserve `/var/log` for diagnostics.
- ElectroDacus MQTT decoding is now implemented in 0.4.0, preserving the existing Mosquitto destination during validation.

## Later Android work

- Start the Android port after reviewing the live Pi evidence; confirm target hardware, Android range, layout and background requirements first.
- Review Android's role as a viewer/client of the Pi logger; direct Pico/embedded-broker operation can remain a separate future option. ElectroDacus reception is implemented on the Pi; Android source is not yet implemented.
- Keep SignalK functionality outside the Android scope.

## Database planning — 2026-10-08

- [x] Review existing Pico output and dashboard SBMS mappings; record the discussion draft in [`node.js/DATABASE-PLAN.md`](../node.js/DATABASE-PLAN.md).
- [ ] Agree logged measurements, power/energy inputs and signs, sampling/retention, daily timezone and acceptable uncommitted-data window.
- [x] Confirm a live SBMS payload and active cell channels privately; solar current fields are present. Solar voltage/measurement-point verification remains pending.
- [ ] Implement and benchmark the agreed persistent SQLite history only after implementation is requested. Keep legacy MQTT output unchanged, separate sources, and preserve gaps/validity in summaries and energy totals.

## Owner logging specification — 2026-10-08

- [x] Record the owner's selected measurements and retention in [`node.js/DATABASE-PLAN.md`](../node.js/DATABASE-PLAN.md), superseding the initial broad candidates and retention tiers.
- [ ] Barometer: hourly for 1 month; last valid daily reading indefinitely.
- [ ] All Pico current shunts: `[W, A, V]` minute/hour/day/month summaries retained for 1 day / 1 week / 1 month / indefinitely; add main-battery SOC.
- [ ] Outside temperature: hourly for 1 month; daily minimum/maximum indefinitely.
- [x] Add independent ElectroDacus MQTT subscription/decoding for voltage, total/battery current, PV1/PV2 current and SOC. Pi operation and solar voltage/current associations remain to verify.
- [ ] Agree instantaneous-power averaging, duration-aware rollups and directional energy/charge totals so short-history deletion does not remove information needed for later calculations.
- [ ] Confirm SOC/hourly environmental aggregation, calendar timezone/cutoffs, ElectroDacus retention and durable in-progress bucket recovery.
- This records planning requirements only; no logging implementation or version change is authorized by the calculation question.

## Current polarity requirement — 2026-10-08

- [x] Owner defines all non-battery Pico shunts as load/current-draw channels; battery channels are net balance and ElectroDacus PV channels are charging supply.
- [ ] Verify voltage association and reported sign separately for every Pico shunt and ElectroDacus current field using known charging/load conditions; current draw is not assumed negative on every channel.
- [ ] Preserve source readings/MQTT output and use explicitly verified channel-specific direction mappings for logging calculations. Unverified channels remain unclassified for directional totals.
- [ ] Keep net main-battery inflow/outflow separate from gross supply/load totals; PV1 is a charging-only supply channel as described by the owner, with its reported polarity still to verify.

## Proposed implementation sequence and history API — 2026-10-08

- [ ] Confirm live SBMS fields/reporting cadence, per-channel voltage/sign mapping and remaining aggregation/timezone choices.
- [x] Add ElectroDacus subscription using the existing Mosquitto destination without altering Pico MQTT output.
- [ ] Implement persistent selected history/rollups/retention after Pi reception validation.
- [ ] Add an authenticated local JSON API: metric catalogue, live readings/freshness, bounded per-channel history queries and retained Wh/Ah totals. Only the Pi opens SQLite; Android/browser clients request data through the API.
- [ ] Define stable API IDs/units/array field meanings, credentials/transport/port/address discovery, partial/gap responses, backups/export and schema migration.
- [ ] Test restart recovery, power interruption, clock/calendar boundaries, pruning, denied MQTT/API access and long unattended collection; privately measure Pi performance.
- Reception is now authorized and implemented; the remaining sequence is planned Pi logging/API work, not an Android build.

## Received ElectroDacus example — 2026-10-08

- [x] Owner supplied a live MQTT JSON payload containing SOC, cell-voltage slots and battery/PV1/PV2/external-load current fields. Publish the field contract only, not the sample readings or device inventory.
- [x] Confirm configured active cells and the example's discharge condition privately. Implement Pi UTC receipt time and retained/stale handling.
- [ ] Verify remaining channel polarity, actual reporting cadence and retained/repeated behaviour on the Pi before integration.
- [ ] Decide whether external-load current is required in the logged channel set; keep it distinct from battery balance and overlapping load-shunt sums.

## Owner-confirmed acquisition details — 2026-10-08

- [x] Confirm Pi acquisition/receipt timestamps for both Pico and ElectroDacus logging; preserve existing MQTT output.
- [x] Confirm the active cells in the supplied example and that its negative battery current is discharge. Keep the actual cell map and present installation inventory private.
- [x] Convert PV1/PV2 mA to A with division by 1000 in reception, preserving valid zero readings.
- [ ] Include both solar channels in electrical history; log valid zeros distinctly from unavailable readings.
- [ ] Include PV2 for future use, with its directional verification performed when connected. This confirmation does not establish its charging polarity from the current zero sample.

## Agreed logging choices and voltage review — 2026-10-09

- [x] Select UTC timestamps and minute/hour/day/month aggregation boundaries; optional Europe/London chart display does not change UTC daily totals.
- [x] Select the more accurate duration-aware average of instantaneous paired A × V measurements, with coverage and additive rollup integrals.
- [x] Store last valid SOC from each relevant device and use identical electrical retention periods for Pico and SBMS0.
- [x] Record owner-described SBMS battery/external-load consumption distinction without adding overlapping currents to totals.
- [x] Inspect current sensor mappings: voltage sensors/battery voltage are decoded separately; extra current-record fields have no verified voltage interpretation, and bracketed sensor names are filtered from legacy public JSON.
- [ ] Confirm battery supply versus converter/panel-side domain for each current channel before assigning the owner's preferred SBMS pack-voltage reference to Pico power calculations. Preserve original Pico MQTT output and label derived voltage provenance.
- [ ] Pair only fresh valid current/voltage measurements; retain current/SOC during voltage gaps and avoid an automatic fallback to untrusted voltage.
- [ ] Implement persistent logging and the authenticated read-only API when requested; this decision/source-inspection update changes no application version or source.
