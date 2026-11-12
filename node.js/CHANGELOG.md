# Pico base changelog

## 0.7.8 — 2026-11-12

- Owner-authorized recovery: restore the pre-0.7.6 Pico collection and decoding implementation from commit ddfa437. `lib/client.js`, `lib/pico-protocol.js`, `lib/sensor-list.js`, `lib/readings.js` and `bin/verify-capture.js` are byte-identical to that baseline; collector acquisition/capture event wiring is also restored.
- Remove the new sensor-type/field validation and incoming CRC/UDP-length rejection. Restore original configuration buffering, output throttle and metadata copying. Keep the original parser bounds, sender filtering, TCP framing and offline capture-verification checks. This restores the previous compatibility behavior rather than claiming support for every unknown firmware layout.
- Retain independent API/live-status caching, LAN request diagnostics, API bind retry, explicit IPv6 setup, logger status/error metadata, installer rollback and deployment receipts. Monitor 1.4 and Android are unchanged; no network setting, private configuration or history migration.
- Defer the collection-path review changes (R1/R2/R3/R6). Any future stricter acceptance work must first use representative installation data and diagnostic-only validation; do not silently re-enable rejection. Tests now cover historical opaque types beyond type 10 and the restored buffered/nonblocking UDP behavior. The 0.7.7 deployment still failed; its subsequent retry reason was not supplied. Live recovery with 0.7.8 remains to verify.

## 0.7.7 — 2026-11-12

- Fix the 0.7.6 configuration-validation regression that rejected sensor type 10 and prevented all Pico acquisition. Restore its preceding Python/Node behavior: one opaque live-field slot, with numeric type retained and no invented measurement decoder. Keep CRC/length, duplicate-ID, required-field and other unsupported-type checks.
- Include type 10 before decoded tank/battery/voltage entries in the shared synthetic fixture. Verify unchanged sensor positions, values and MQTT output against the original Python implementation, and exercise configuration/live/history/API paths with that entry present. The failing pre-fix regression was reproduced locally.
- Owner evidence confirms monitor 1.4 and collector 0.7.6 installation; Pico was retrying while SBMS/API remained available. This fix's real-Pi recovery remains to verify. No monitor/Android update or configuration/history migration is required.

## 0.7.6 — 2026-11-12

- Reject incorrect length/CRC and ambiguous sensor configurations. Discard packets received before configuration completes; timestamp only newly received live samples. Use monotonic time for MQTT/stdout throttling across clock adjustments.
- Cache live measurement metadata, avoid per-request live/status SQLite reads, and retain safe logger error categories and committed checkpoint state in memory. History failures remain explicit while current live data is available. Remove unnecessary metadata JSON round trips and capture hex work when recording is off.
- Retry API bind failures every 30 seconds while acquisition/MQTT continue. Accept explicit IPv6 bind configuration without changing existing bindings.
- Add a bounded RAM request ring (128 entries, last 32 returned), request IDs, LAN local/peer addresses, port, status, duration and disconnect indication. No credentials, query strings, bodies or raw exception messages are recorded. Authenticated diagnostics and restricted loopback health let the independent monitor correlate incidents.
- Standalone monitor 1.4 reads current source/logger health, detects never-healthy required components after a 60-second grace and stalled checkpoints after 120 seconds, filters journal input before parsing and reduces configuration rereads. Existing STA-only/observe configuration and incident retention are preserved.
- Restore the prior sudo rule if the Pi-control config update fails. Version the recovered copy-based updater, a sanitised historical pi-wifi reference, and a source manifest/check command. The updater's target and private parent files remain unchanged; installed standalone helpers are not silently replaced.
- Automated validation and hardware limits are recorded in ../android/BUILD-VALIDATION.md. No Android source/version change, Pi installation, AP enabling, automatic switching, history migration or calculation/retention change.

## 0.7.5 — 2026-11-01

- Add authenticated live Pi system/network readouts at GET /api/v1/pi, with one shared ten-second cache, coalesced refresh, bounded network-status commands, whole-system memory/CPU and missing/reset-aware values. Do not write resource snapshots to SQLite or change sensor collection, history, retention or MQTT.
- Add one opt-in POST /api/v1/pi/wifi accepting only auto/home/m1. Require the existing Bearer token, bounded JSON/content length, exact fields, safe root-owned helper and noninteractive privilege for the fixed command. Queue acknowledgement precedes execution; keep one pending command, a cooldown and bounded execution/output. All history routes stay read-only.
- Provide a separate local installer for the narrow sudo rule/API opt-in, preserving private credentials, file ownership and an original backup. No automatic network/service/setup changes are made by the ordinary source update. Document setup/removal, API versus ConnectBot terminal use and source/device verification limits in PI-STATUS.md.

## 0.7.4 — 2026-10-31

- Recover the logging status from clock-step to started only after normal clock progression, completion of any backward-time hold, and a successful save. Preserve the persistent clockSteps counter and all existing interval-skipping/integration safeguards.
- Do not clear a newer unrelated diagnostic or an active logging/storage failure. Add focused forward/backward, failed-save and counter-persistence tests. No database schema, retention, commit interval, selected fields, MQTT or calculation changes.

## 0.7.3 — 2026-10-12

- Extend electrical/selected-voltage hourly retention from 7 to 15 days; keep daily records indefinitely. Preserve the existing 24-hour minute policy, indefinite monthly summaries and longer environmental hourly retention.
- Apply new expiry cutoffs to the existing schema-2 database in place. No reset, schema migration, binding change or recalculation; retained payloads, signed integrals, SOC and source references remain intact. Longer retained detail accumulates going forward; previously expired rows are not recreated.
- Report the new retention through API catalogue metadata; keep default query windows and response shapes compatible. MQTT, acquisition, logging fields, calculations and dependencies are unchanged.
- All 36 focused logger, combined-history and API tests pass, including reopening a seeded existing database, byte-preserving daily/monthly payloads, hourly cutoff boundaries, unchanged minute/environmental expiry, source bindings/schema/integrity and historical API reads. Real Pi service installation remains an owner step.

## 0.7.2 — 2026-10-11

- Separate live API display freshness from logging integration maxGapSeconds. Use the actual receivers' stale deadlines: Pico 15 seconds by default; SBMS 30 seconds or its existing sbms_stale_seconds setting. Preserve receipt age/timestamps and report expired data as stale/null.
- Reject isolated malformed SBMS JSON without clearing an unexpired accepted live reading or renewing its deadline. Emit rejection/diagnostic events; the CLI immediately cuts logging coverage at a rejected payload. Retained/repeated messages still cannot renew freshness; actual disconnect/stale states still clear readings.
- Preserve database schema, logging gap limits, calculations, MQTT publishing/subscription settings, payloads and pinned dependencies. No broker/router/device reconfiguration or real Wi-Fi fix is claimed.

## 0.7.1 — 2026-10-09

- Expose the complete accepted SBMS MQTT broadcast through the live API in original names/units, alongside existing normalized battery values.
- Add live boolean monitor flags, including CFET, DFET, OVLK, UVLK, EOC, IOT, LVC and CELF. True/false represent On/Off; missing/invalid flags are null and stale/disconnected readings unavailable. No flag or auxiliary-field history is added.
- Deliver flag/auxiliary changes within a source-clock second, while ignoring retained snapshots and complete repeats regardless of object-key ordering. Preserve existing electrical calculations, database schema/selection/retention and Pico MQTT output.
- All 76 tests pass under Node 22, including synthetic checks for live flags, full-field preservation, state changes, unavailable values, repeat/retained filtering and unchanged history, plus collector/MQTT/Python parity. Pi flag verification remains pending.

## 0.7.0 — 2026-10-09

- Add an optional authenticated read-only HTTP/HTTPS API inside the existing collector, with status, selected metadata, fresh live Pico/SBMS measurements, combined battery history and per-metric compatibility views. No new npm dependency or SQLite schema change.
- Preserve acquisition, MQTT JSON/wire output, source-specific calculations, private selections, UTC retention and quiet service operation. Live API watts follow fresh configured voltage references; stale/missing inputs remain null and valid zero currents remain zero.
- Use indexed bounded history queries, consistent per-response database snapshots, UTC range validation, pagination, partial intervals and existing source/coverage/energy references. Protect access with header tokens, optional TLS, request/connection/response limits and generic errors.
- Add private API configuration generation and authenticated checks, optional/disable CLI flags, operational/API documentation and pending Android/backup decisions.
- All 74 tests pass under Node 22, including authenticated HTTP/HTTPS, read-only simultaneous writer access, pagination/gaps, source-specific freshness and actual collector/API/MQTT/Python parity. Existing private history is served read-only with inspection-equivalent statistics/source references. Raw data, credentials and system details remain excluded.
- Actual Pi API access/performance testing and Android/automatic backups remain later stages.

## 0.6.0 — 2026-10-09

- Store one combined battery record per UTC minute/hour/day/month interval. Keep Pico/SBMS current and relevant-device SOC, primary/secondary voltage, both PV channels and external-load measurements distinct within the record, with stable source references.
- Present named values, per-field coverage, per-measurement energy/sample totals and input receipt timestamps. Preserve all numerical calculations, raw signs, freshness rules, retention and independent source availability.
- Automatically back up and migrate schema-1 battery rows to schema 2 in a transaction using bounded batches. Preserve all existing statistics/name metadata and remove duplicate per-channel battery storage. Keep other shunt/environmental records unchanged; retain the first pre-upgrade backup.
- Add `history.js --battery`; default inspection displays the combined battery alongside other sensors. Existing per-metric commands remain read-only views into combined storage. Existing generated private configurations work without edits; new selections record explicit group bindings.
- All 64 tests pass under Node 22, covering migration/WAL backup, rollback and retry, idempotence, partial checkpoints, missing-versus-zero values, retention, source references and group rebinding protection. Retain full MQTT/Python compatibility and silent collection tests; privately compare migrated recorded statistics without publishing raw data or system inventory.
- No network API or Android app is added in this storage update.

## 0.5.0 — 2026-10-09

- Add optional persistent SQLite logging using Node's built-in module and a private parent `PicoData/logging.json` selection. Keep MQTT fields/values, units, names, sign handling, wire settings and silent default unchanged.
- Use every validated ID-based Pico snapshot before legacy filtering/throttling. Log the selected battery instance once, selected load shunts, raw secondary voltage, pressure/outside temperature, and SBMS battery/PV1/PV2/external-load channels. Do not automatically select the duplicate physical main-shunt alias, Pico internal voltage or other sensors.
- Accumulate duration-weighted instantaneous watts, raw signed amps/volts, relevant-device last SOC, additive Wh/Ah, verified directional totals and separate coverage into UTC minute/hour/day/month summaries. Track last pressure/hourly temperature and daily temperature extrema. Use fresh SBMS pack voltage for both PV channels following confirmation that their current is supplied to the battery. Keep unverified directional classifications unavailable.
- Apply requested retention, checkpoint partial summaries every minute, flush normal shutdown, prevent overlapping writers and recover saved sums without bridging downtime. Invalidate missing/stale inputs, handle clock steps and pause logging visibly on storage failure while retaining MQTT acquisition.
- Separate stable metric IDs, source/display names and configuration fingerprints. Preserve rename history and reject implicit rebinding of existing metrics.
- Add private selection generation, read-only history inspection and offline capture replay; document configuration, storage outside zram, the uncommitted-minute power-loss window, sign/voltage limitations and live Pi checks. The API/Android viewer is not included in this logging step.
- Validate calculations, freshness, UTC/calendar boundaries, retention, restart/crash recovery, single-writer/read-only access, mapping safety, silent combined collection and unchanged MQTT/Python output. Private capture replay passes with the duplicate main shunt and Pico internal voltage excluded. Raw recordings, inventory and system details remain private.

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


