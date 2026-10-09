# Next build changes

## Current authorized correction: live SBMS states in collector 0.7.1

- [x] Expose the complete accepted SBMS broadcast through the live API; preserve existing normalized battery values and original raw field names/units.
- [x] Provide live boolean CFET, DFET, OVLK, UVLK, EOC, IOT, LVC and CELF states for On/Off display. Missing/invalid flags and stale/disconnected readings are unavailable, never silently Off.
- [x] Deliver flag/auxiliary-only changes within a source-clock second while continuing to ignore retained and complete repeated broadcasts, including reordered object keys.
- [x] Keep flags/auxiliary values out of database history; preserve schema, selected measurements, calculations, retention and Pico MQTT output.
- [ ] Update the Pi and confirm live flags match the SBMS/dashboard. Android display remains pending.

## Previous authorized step: local API in collector 0.7.0

- [x] Owner requests the API before Android implementation. Add authenticated, read-only status, measurement catalogue, live Pico/SBMS freshness and bounded combined/per-metric history.
- [x] Reuse the existing service and schema 2 without changing collection, selected statistics, retention, source references, units, signs or MQTT output. Add independent private credentials, optional HTTPS, pagination and explicit partial/gap semantics.
- [x] Generate/check private parent API settings, document setup/routes/limits, test denied access, safe errors, read-only database access and simultaneous collector/API/MQTT operation.
- [x] Inspect the owner's live schema-2 database and migration backup privately: integrity passes, completed history/metadata preserved, active buckets extended consistently. Other shunts/environmental history and primary/secondary voltage references are present.
- [x] Owner enabled the API and confirmed local status/live/history requests; supplied responses pass private freshness/calculation/source-reference review.
- [ ] Test LAN/TLS requests and restarts; privately assess latency/memory while logging continues. Finish long-run retention/midnight and charge-polarity checks.
- [ ] Agree Android targets, screens, charts, discovery, credential storage and offline behaviour before the app build.
- [ ] Keep history on its current persistent storage for now. Plan USB migration with consistent backups; SD/Android/Google Drive destinations, backup schedule and retention remain undecided. No automatic backup/cloud access is included in this API build.

## Previous authorized step: combined battery history in collector 0.6.0

- [x] Owner requests one combined battery record per interval with field-level source references, instead of separate battery/SBMS history rows.
- [x] Include primary/secondary voltage, distinct Pico/SBMS current and SOC, PV1/PV2 and external load, retaining independent coverage and energy/statistics.
- [x] Back up and migrate existing history atomically without recalculating/discarding saved values; keep other shunts, pressure and outside temperature in their current records and periods.
- [x] Add combined inspection and preserve existing per-metric commands as views; support existing private configuration without regeneration.
- [x] Owner updated the Pi and supplied live history plus pre-upgrade backup; read-only review confirms integrity, preserved completed records, source bindings, valid zeros and consistent rollups. Long-run hardware checks remain pending.
- [x] History API is implemented in 0.7.0 above; Android/browser display remains a subsequent stage.

## Previous authorized step: SQLite logging in collector 0.5.0

- [x] Implement optional SQLite history and private sensor selection alongside unchanged Pico MQTT publishing and SBMS reception.
- [x] Log the main battery through its selected battery instance; exclude the duplicate physical main-shunt alias. Keep selected secondary voltage raw and exclude Pico internal voltage from history.
- [x] Use fresh SBMS voltage for derived Pico battery/load watts; retain raw signed current, source/display names, relevant-device SOC and stable database IDs. Check configuration fingerprints before using a selected sensor.
- [x] Implement UTC duration-aware electrical summaries, additive energy/directional totals, coverage, environmental summaries and requested retention. Use fresh SBMS pack voltage for PV1/PV2 battery-side power; leave unverified directional classifications unavailable.
- [x] Save partial summaries every minute, flush graceful stops, resume saved sums without extending over downtime, prevent concurrent writers and report storage failures/clock steps.
- [x] Add configuration generation, read-only inspection and private offline replay. Keep capture/configuration/database/system details out of published source.
- [ ] Install private configuration on the Pi; confirm saved rows, live value/energy comparisons, sign/voltage settings, reboot/network/broker recovery, storage use and long unattended operation.
- [ ] Implement the local history API after live logging checks, then build the Android/browser history display. Neither is included in 0.5.0.

## Previous authorized step: ElectroDacus reception in collector 0.4.0

- [x] Receive `/Ella/sbms` on the existing MQTT connection independently of Pico availability; resubscribe after reconnection and retry denied subscription without stopping publishing.
- [x] Decode all four mA current fields to amps, SOC and pack voltage from a private configured cell map; include PV2 and preserve valid zeros/current signs.
- [x] Use Pi UTC receipt time and keep monitor time separately. Reject invalid payloads, ignore retained/exact repeated samples, and clear current readings on stale timeout or broker disconnect.
- [x] Preserve original Pico MQTT output and quiet service operation; add `--sbms-stdout`, `--no-sbms` and local SBMS capture records for diagnostics.
- [x] Owner supplied a short real Pi run showing simultaneous Pico/SBMS connection, approximately one SBMS sample per second, valid derived pack voltage and normal timed shutdown. Keep raw readings/timestamps/runtime details private.
- [ ] Check actual broker restart, stale/repeated/retained behaviour and long unattended SBMS collection. Confirm charging polarity under known conditions before directional integration.
- [x] Selected SQLite history, retention and restart recovery are added in 0.5.0 above. No database/API was part of 0.4.0; the API remains pending.

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
- [x] Include PV2 for future use, with its directional verification performed when connected. This confirmation does not establish its charging polarity from the current zero sample.

## Agreed logging choices and voltage review — 2026-10-09

- [x] Select UTC timestamps and minute/hour/day/month aggregation boundaries; optional Europe/London chart display does not change UTC daily totals.
- [x] Select the more accurate duration-aware average of instantaneous paired A × V measurements, with coverage and additive rollup integrals.
- [x] Store last valid SOC from each relevant device and use identical electrical retention periods for Pico and SBMS0.
- [x] Record owner-described SBMS battery/external-load consumption distinction without adding overlapping currents to totals.
- [x] Inspect current sensor mappings: voltage sensors/battery voltage are decoded separately; extra current-record fields have no verified voltage interpretation, and bracketed sensor names are filtered from legacy public JSON.
- [x] Owner confirms all Pico load shunts are on the main battery supply; use SBMS pack voltage as their preferred reference. Preserve original Pico MQTT output and label derived voltage provenance. Other battery/PV measurement points remain separate.
- [ ] Pair only fresh valid current/voltage measurements; retain current/SOC during voltage gaps and avoid an automatic fallback to untrusted voltage.
- [ ] Implement persistent logging and the authenticated read-only API when requested; this decision/source-inspection update changes no application version or source.

## Secondary voltage and stable names — 2026-10-09

- [x] Record selected secondary shunt-module voltage for comparison and exclude Pico internal voltage from regular history/reference selection; keep private sensor selection outside published source/documents.
- [x] Choose database identity separate from name, packet position and dashboard instance: permanent metric ID with source/device, sensor ID and measurement kind; preserve exact source name plus editable display name and time-effective mapping/name history.
- [x] Independently inspect the owner's supplied raw Pico/SBMS capture, validate framing and map actual configured IDs/names privately. Secondary voltage is present; main virtual-battery voltage/current duplicate configured source measurements.
- [x] Identify load-dependent primary/secondary voltage differences; avoid a fixed correction. Extra current fields appear counter-like rather than voltage, with meaning/units still unverified.
- [ ] Verify sensor-ID persistence through rename/reboot/reconfiguration and handle ID reuse/physical channel changes explicitly.
- [ ] Add an internal timestamped, ID-keyed decoded Pico snapshot for the future logger, before legacy filtering/name collisions; preserve all current MQTT output.
- [ ] Log the selected secondary voltage with original readings preserved; evaluate paired voltage/current differences before approving any correction or fallback.
- [ ] Implement the agreed SQLite history/API when requested. This planning/source review changes no application version or runtime code.

## Capture findings for the logger — 2026-10-09

- [x] Pass length/CRC checks for all recorded complete TCP replies and live packets; reconstruct the recorded sensor metadata from configuration. Raw capture/statistics/actual ID-to-name map remain private.
- [ ] Bind the private confirmed sensor map to permanent database metric IDs and editable names; retain versioned mapping metadata and check identity across later configuration changes.
- [ ] Treat duplicate main-battery current/voltage paths as aliases of one measurement; attach the relevant battery SOC without double counting currents/energy.
- [ ] Record the selected secondary voltage unchanged. Any later calibration/fallback requires explicit validation under different operating conditions, not a fixed offset inferred from one sample.
- [ ] Keep extra current-field counter candidates diagnostic-only until scale, deadband, signs, rollover and resets are established. Continue with sample/time-based integration for primary history.
- [ ] Consume raw decoded ID-keyed Pico snapshots before public filtering/throttling, while preserving the original MQTT contract and keeping cross-source timing/freshness explicit.


## PV voltage confirmation — 2026-10-09

- [x] Owner confirms both PV currents are charge supplied to the battery; use fresh SBMS pack voltage for their watts/Wh, not an assumed panel voltage.
- [x] Log current, battery-side power/energy, coverage and valid zeros for both PV channels with the agreed electrical periods/retention. Preserve raw signs; polarity verification remains separate.

## Owner-reported initial history check — 2026-10-09

- [x] Owner inspected an actual saved minute electrical record and could identify voltage/SOC. Keep the posted values/timestamps private. This verifies initial live history visibility, not all channels, energy accuracy, retention or long-run recovery.
