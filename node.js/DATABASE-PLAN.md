# Database and logging plan

Updated 2026-10-09. Collector 0.5.0 implements selected SQLite logging, UTC summaries, retention, checkpoints and inspection following the owner's request to start testing. See [LOGGING.md](LOGGING.md) for the implemented contract and setup. The history API remains pending. Earlier proposals/reviews below record design history; the implementation decisions here and LOGGING.md supersede unresolved alternatives.

## Owner-requested measurements and retention

| Measurement | Record | Retain |
| --- | --- | --- |
| Barometer | One reading each hour | 1 month |
| Barometer | Last valid reading of each day | Indefinitely |
| All Pico current shunts | Minute array of watts, amps and volts; main battery adds SOC | 1 day |
| All Pico current shunts | Hour array | 1 week |
| All Pico current shunts | Day array | 1 month |
| All Pico current shunts | Month array | Indefinitely |
| Outside temperature | One reading each hour | 1 month |
| Outside temperature | Daily minimum and maximum | Indefinitely |
| ElectroDacus | Voltage, total/battery current, PV1/PV2 charge currents and SOC; analogous electrical summaries with the extra values | Same electrical retention: minute 1 day / hour 1 week / day 1 month / month indefinitely |

Do not silently include other temperatures, tanks, tilt, cell voltages or flags in regular history. They were initial candidates, not selections in the owner's latest request. Connection/coverage tracking supports correct calculations; user-facing alarm/event history is a separate decision.

The owner accepts the more accurate watts calculation: average paired instantaneous power over actual elapsed valid time instead of multiplying separate current/voltage averages or assuming exactly 60 samples. The duration-aware approach below is the selected calculation direction; directional calibration and storage details remain pending.

## Proposed calculation refinements

- For each valid, time-aligned current/voltage pair calculate power as watts = amps × volts. Average these power values, rather than multiplying average current by average voltage; the two differ when current and voltage vary together.
- Division by 60 is valid only for exactly 60 equally spaced samples covering the minute. Account for actual received samples and elapsed valid time. The Pico and SBMS may have different reporting rates.
- Maintain time integrals: watt-seconds = sum(power × valid elapsed seconds), amp-seconds = sum(current × valid elapsed seconds), and volt-seconds = sum(voltage × valid elapsed seconds). Proposed array values are those integrals divided by their respective valid covered durations.
- Electrical energy is Wh = watt-seconds / 3600; charge is Ah = amp-seconds / 3600. Average watts describes power, not “watts per minute”. Roll up additive integrals and durations into hours/days/months; do not blindly average child averages.
- Power coverage may differ from current/voltage coverage because power requires both fresh inputs. Preserve the applicable durations and timestamps so an average cannot be mistaken for a complete interval.
- Store consumed/generated Wh and discharged/charged Ah separately for bidirectional channels. A net monthly average alone cannot recover both gross totals after smaller intervals are deleted.
- Keep every monitor/channel separate. Do not add Pico and ElectroDacus battery measurements as though they were separate batteries.
- Reject invalid/sentinel values for logging while preserving existing legacy MQTT output. Do not turn missing/stale inputs into zero or extend readings over outages/restarts. Use monotonic elapsed time within a run; UTC timestamps identify stored intervals.
- Daily outside-temperature extrema should inspect all fresh received readings, not just the hourly stored readings.
- Select the last valid barometer reading actually received within the day; do not carry yesterday's value into an empty day.

A compact electrical representation can remain `[average_W, average_A, average_V]`, with the main battery adding SOC. Recommended timestamp/coverage and Wh/Ah totals belong in named database columns alongside that array. SOC uses the last valid value for the relevant device in each interval; do not substitute SBMS SOC for Pico SOC or carry an old value into an empty interval. Per-shunt voltage association must be explicit and correct for the measurement point. Do not present battery-side estimated solar power as directly measured panel-side power.

## Current direction verification

Owner requirement, 2026-10-08: do not assume every current draw is negative or every charging channel follows the same sign convention.

Owner-confirmed roles: every non-battery Pico current shunt measures a load/current draw. Pico and ElectroDacus main-battery current measure the net battery balance; ElectroDacus PV channels measure charging supply. These roles are established by the owner; verification concerns reported polarity, voltage association and actual SBMS field availability, rather than rediscovering whether a non-battery Pico shunt is a load.

- Preserve the current source values and legacy MQTT output unchanged.
- Identify each channel by source and stable sensor ID/MQTT field path. Use the owner-confirmed physical role (battery balance, load or charging supply), record its associated voltage, and separately verify reported polarity. Do not apply a global absolute-value conversion or global sign reversal.
- Check known load-only and charging conditions, including simultaneous supply/load operation where practical, against monitor readings and controlled changes. Dashboard sign inversions are reference transformations, not proof of the sensor's physical direction.
- Proposed internal convention: battery current positive into the battery and negative out; load-channel current positive for consumption; supply-channel current positive for generation. Apply a channel-specific multiplier only after confirming its orientation. Preserve meaningful reverse flow rather than taking absolute values indiscriminately.
- Until polarity is verified, retain the source reading as unclassified; do not assign it to charged/discharged or generated/consumed totals.
- The main battery current is a net charge-minus-load balance. Splitting it by verified sign measures net battery inflow/outflow, not all simultaneous generation/consumption. Pico and ElectroDacus battery balances remain separate comparisons.
- PV1 is owner-described as a charging-only supply channel. Accumulate its supplied energy independently after confirming its reported polarity, measurement point and voltage. Confirm PV2 from an actual payload before defining its mapping.

## Storage proposal

Use a local SQLite database on persistent Pi storage outside zram-managed directories (proposed `/var/lib/ella/history.sqlite`). The collector writes it; a future local API serves graphs to Android or a browser. Existing MQTT payloads remain unchanged.

| Table | Proposed contents |
| --- | --- |
| `metrics` | Source/channel identity, exact label, unit, stable sensor ID or MQTT field path, voltage association and measured/derived provenance. |
| `history` | UTC interval bounds, resolution, electrical averages or environmental readings/extrema, optional SOC, additive integrals/energy and valid coverage. Unique channel/resolution/interval keys prevent duplicate rollups. |
| `energy_totals` | Optional retained directional Wh/Ah totals, or store these alongside each electrical summary to avoid duplication. Final schema remains open. |
| `events` | Proposed source/collector availability and clock-change records; additional monitor alarms/flags are not selected yet. |

Minutes/hours/days/months identify non-overlapping UTC buckets. The owner selected UTC acquisition timestamps and all aggregation boundaries, including midnight-to-midnight UTC days and calendar months. Chart display may convert timestamps to Europe/London using date-specific GMT/BST rules; this does not change stored interval boundaries. UTC daily totals cover 01:00-to-01:00 UK local time during BST and should remain labelled as UTC daily totals. Month lengths vary; never assume every month has 30 days. Precise calendar versus fixed-day retention cutoffs and incomplete-current-period display remain to define. Commit longer-term summaries before pruning shorter-term rows. Restart-safe checkpoints must prevent duplicate energy accumulation or loss of the in-progress bucket.

Batch bounded writes into transactions; commit cadence and acceptable uncommitted-data loss are still open. Indefinite monthly/environmental history has no automatic expiry. Measure storage growth and range-query performance on the Pi before making resource claims.

## ElectroDacus input

Collector 0.4.0 subscribes to the existing broker's ElectroDacus topic independently of Pico acquisition. It decodes voltage, battery/total current, PV1/PV2 current, external-load current and SOC from the confirmed field contract. Confirm actual reporting cadence and recovery on the Pi before logging/integration. The existing flow maps `soc`, `cellsMV`, `currentMA.battery` and `currentMA.pv1`; its PV2 gauge has no incoming wire. This does not establish that PV2 is unavailable in the firmware, only that the copied flow does not receive/display it.

Establish whether voltage is directly supplied or must be derived from the correct active cell readings, and confirm current signs and the physical location of each current/voltage measurement. See [existing dashboard mappings](../node.red/flows.json). No private runtime/device inventory is included here.

## Received SBMS payload contract — 2026-10-08

The owner supplied one live JSON message. Record the field contract only; do not publish its actual readings, cell/channel inventory or device-clock value.

- `soc`: percentage.
- `cellsMV`: eight cell-voltage slots in millivolts; the supplied message includes unused zero slots. No explicit pack-voltage field is present. Proposed pack voltage is the sum of configured active cells divided by 1000, after confirming the enabled-cell map. Do not infer that a configured active cell reporting zero/missing is merely unused; invalidate derived voltage/power when an expected active input is unavailable.
- `currentMA.battery`: signed battery/net current in milliamps.
- `currentMA.pv1` and `currentMA.pv2`: both keys are present. Their actual charge polarity and wiring/availability still need verification; a zero sample does not prove an installed channel or establish charging direction.
- `currentMA.extLoad`: external-load current is also available, in milliamps. Its role is load; verify its sign and relationship to other load channels before selecting it for totals to avoid double counting.
- `tempInt`, `tempExt`, `ad2`, `ad3`, `ad4`, `heat1`, `heat2` and `flags` exist. These extra values are not automatically added to the requested logging scope. Do not guess the units/roles of the auxiliary fields.
- The device `time` is not aligned with the current date in the supplied example. Use a reliable Pi UTC receipt timestamp for logging; retain source time separately only if useful. Receipt time alone does not prove source freshness, so define retained-message/repetition/stale handling and confirm source reporting cadence before integrating energy.

A single sample establishes field presence and example types, not charge/discharge calibration, reporting rate, timing validity or long-run recovery. Active cells and the example's discharge condition were subsequently confirmed below. Match battery and supply/load readings to known operating conditions before accepting directional totals. The 0.4.0 receiver preserves the source current signs without assigning directional totals.

## Owner confirmations — 2026-10-08

- Use Pi acquisition/receipt time for the logged Pico and ElectroDacus data; do not depend on externally synchronizing the monitor's clock. Store UTC wall time and use monotonic elapsed time for integration. Keep the existing Pico MQTT payload/format unchanged; its formatter already creates the timestamp from the Pi's current time.
- The nonzero cell slots in the supplied example are confirmed active. Keep the actual enabled-cell map as private runtime configuration, not a published inventory. Derive pack voltage from all configured active inputs; invalid/missing active inputs invalidate derived voltage/power.
- The supplied example is confirmed discharging: negative `currentMA.battery` represents battery outflow in that operating condition. Charging-condition and other channel sign checks remain separate.
- Convert `currentMA.pv1` and `currentMA.pv2` from mA to A by dividing by 1000. Battery and external-load current fields also use mA.
- PV2 is presently unconnected but must have a logging channel and follow the agreed electrical summary/retention policy alongside PV1. Preserve explicit valid zero readings; do not confuse them with a missing/null field. Treat present connection state as private runtime configuration. Verify PV2 polarity/measurement point when it is connected rather than asserting that a zero sample proves its sign.
- Avoid persisting retained MQTT snapshots as fresh acquisitions. Pi receipt time timestamps arrival, but freshness/repetition handling is still needed.

## Proposed implementation order and Android access

Sequence, 2026-10-08. ElectroDacus reception is implemented in 0.4.0 following the owner's request to proceed. Remaining logging/API work follows acquisition validation.

1. Obtain representative live Pico and SBMS payloads; map stable channels, SBMS voltage/PV1/PV2 fields, per-shunt voltages and reported current polarity.
2. Extend the Pi collector to subscribe to the existing ElectroDacus MQTT topic. Retain the existing broker destination and Pico MQTT publishing contract. Keep each source's receipt time, validity, freshness and reconnect handling independent.
3. Add persistent SQLite logging, minute/hour/day/month accumulators, selected environmental records, restart-safe checkpoints and the owner's retention rules. Verify calculations on synthetic/replay data before comparing live totals.
4. Add a small authenticated local HTTP API in the same service, using Node.js's built-in HTTP server as the lightweight starting proposal. Android/browser clients request JSON; only the Pi opens the database. No remote SQL or shared database file is needed.
5. Check Pi resource use, query latency, outage/restart/power-loss behaviour, rollup boundaries, pruning and a long unattended run. Keep all owner runtime/resource details private.

Proposed versioned endpoints:

| Endpoint | Response |
| --- | --- |
| `GET /api/v1/metrics` | Available source/channel IDs, labels, units, verified direction, resolutions and available history range. |
| `GET /api/v1/live` | Current Pico/SBMS readings with timestamps and independent stale/connection indicators. |
| `GET /api/v1/history?metric=ID&resolution=hour&from=UTC&to=UTC` | The requested channel/time range at minute/hour/day/month or the selected environmental resolution, with values, interval bounds and missing coverage. |
| `GET /api/v1/totals?metric=ID&from=UTC&to=UTC` | Directional Wh/Ah totals over available retained complete/partial intervals, with coverage and actual returned bounds. Do not imply unavailable detail remains reconstructible after retention. |

Keep query windows/point counts bounded and return labelled fields or an explicit array-field definition. The client polls live values only while viewing them and fetches history when a graph/range changes; an update stream can be considered later if needed. Retention and partial buckets must be visible in responses. Android may cache received history locally for use while disconnected, but the Pi remains the authoritative logger.

Access credentials, network transport/TLS choice, API bind address/port and Pi address discovery are still to be agreed. Preserve API/socket lifecycle cleanup and bounded query execution so history requests do not disrupt collection. Plan a consistent database backup/export method and schema migrations before relying on indefinite records.

References: [SQLite server-side application pattern](https://sqlite.org/whentouse.html), [Node.js HTTP server](https://nodejs.org/api/http.html).

## Remaining verification and API decisions

- Finalize directional Wh/Ah/coverage storage and verification; time-weighted instantaneous watts and last valid device-specific SOC are agreed.
- Implemented hourly environmental choice: last valid reading. Daily pressure is the last valid reading; daily temperature records extrema over all valid samples.
- Implemented retention expires completed intervals by their end timestamp; calendar-month subtraction clamps the day when needed. UTC boundaries and identical electrical retention for ElectroDacus are applied.
- Verify current polarity and live freshness/alignment; the owner has confirmed PV current is battery-side and the initial checkpoint interval is 60 seconds. All Pico load shunts are owner-confirmed on the main battery supply; other battery entries still require their own voltage association.

Reference: [SQLite appropriate uses](https://sqlite.org/whentouse.html).

## Owner decisions and voltage review — 2026-10-09

- Use UTC receipt timestamps and all interval boundaries. Keep optional GMT/BST conversion in chart presentation; no per-record BST flag or system timezone change is needed.
- Use duration-aware averages of paired instantaneous watts, with valid coverage tracked independently from current/voltage coverage. Retain additive integrals for accurate longer-period rollups.
- Store the last valid SOC from the relevant device. Apply the same electrical retention periods to Pico and SBMS0, including both solar channels.
- The owner defines SBMS external-load current as excluding the monitor's own consumption; battery current includes it. Keep the channels distinct and avoid double counting their overlapping measurements.
- The owner prefers SBMS0 pack voltage over the presently displayed Pico voltage readings. Actual readings and private installation inventory are excluded from this plan. A proposed SBMS voltage reference for Pico power must be assigned explicitly to channels confirmed on the same battery supply; do not apply it to unrelated batteries, converter outputs or panel-side current.
- The inspected active/Python/upstream decoders expose voltage sensor entries and battery voltage separately from current shunts. Current sensor metadata reserves two live fields, but the decoder reads current from the first and does not identify the extra field as voltage. Names containing '[' are excluded from the legacy public JSON, even when their raw sensor entries are decoded. Raw configuration/packet evidence is needed to identify any further fields reliably; do not label unknown data as voltage by magnitude alone.
- For a confirmed battery-bus mapping, pair Pico current with fresh valid SBMS pack voltage, retain both timestamps/source identities and describe watts as derived battery-bus power. Voltage drop can make device-terminal power different. Do not silently fall back to a distrusted Pico voltage or continue using stale SBMS voltage; current/SOC records may remain valid while power coverage has a gap.
- This update records decisions and source inspection. It does not implement SQLite logging or an API, prove sensor calibration, or infer PV power from an unverified voltage/current measurement point.

## Channel identity and secondary voltage — 2026-10-09

- The owner confirms every Pico load shunt measures the main battery supply. Use fresh valid SBMS pack voltage as the selected common reference for their derived battery-supply watts. This confirmation applies to load channels; it does not assign the same voltage to another battery or prove PV current is measured on the battery side.
- Plan to log the owner's selected voltage input on the main shunt module as a secondary voltage comparison. Select it by its source/sensor ID in private runtime configuration, preserving the exact configured label, raw voltage and UTC receipt time. Exclude Pico internal voltage from regular selected history and power-reference selection; preserve existing legacy MQTT output and raw diagnostic capture functionality.
- Secondary voltage summaries can follow the selected electrical minute/hour/day/month retention. Keep voltage averages and applicable valid durations; diagnostic paired samples are needed to evaluate a correction. Store any approved correction separately from original readings, with its reference, effective time and calibration version. Do not overwrite historical source measurements or silently use an unverified secondary fallback for watts.
- A shunt measures current from the small differential voltage across its sensing resistor, not by dividing supply voltage by resistance. An external supply-voltage difference does not itself justify changing reported amps. Cable/contact drop varies with current; a constant voltage correction can align a sensor offset but cannot remove load-dependent drop. Compare synchronized raw primary/secondary readings under light/heavy load and charge/discharge conditions before choosing a constant offset, gain or current-dependent model. Matching two sensors is relative alignment, not proof of absolute accuracy.
- Use a permanent database metric ID distinct from the exact source label, dashboard label and live packet position. Bind the metric to a configured Pico source/device identity, configuration sensor ID and measurement kind. Preserve source name and an editable display name separately; record time-effective name/mapping changes so renaming does not split history or rewrite old metadata.
- The Pico decoder currently keys sensor metadata by the ID from configuration and also records live field positions. It does not establish that sensor IDs survive every reconfiguration or firmware update. Verify stability through rename/reboot/configuration refresh; treat changed/reused IDs and physical channel reassignment as explicit mapping changes, not automatic history merges.
- Keep a configured source/device identity independent of its changing LAN IP. Do not use upstream SignalK instance numbers, current configuration ordering or dashboard node IDs as permanent measurement identity. SBMS channels use their distinct field paths under a configured source identity; Pico and SBMS history remain separate.
- The existing Pico 'readings' event exposes name-keyed legacy JSON. The logger will need an internal source-ID-keyed decoded event/snapshot before public-name filtering and duplicate-name overwrites. Include source receipt timestamp and validity, and keep all legacy MQTT fields, naming, rounding and publication behaviour unchanged.
- Current captures already include the Pico configuration, raw live packets with Pi UTC receipt time and normalized SBMS receipt records in one local file. A short capture while loads change can reveal all available configured IDs/names, the hidden secondary voltage and unused raw fields. Unknown fields remain unidentified until supported by configuration or controlled observations; do not infer voltage from magnitude or correlation alone.

Source references: [active sensor mapping](lib/sensor-list.js), [reading decoder and legacy filter](lib/readings.js), [Pico acquisition/capture events](lib/client.js), and [TI current-sensing explanation](https://www.ti.com/document-viewer/lit/html/SSZTA51/GUID-8C507D4D-FB98-422A-AF78-D19B9067D75A).

## Private capture review — 2026-10-09

The owner supplied a local acquisition recording for independent analysis. Keep the recording, actual sensor IDs/names/module identifiers, raw samples, voltage statistics and calibration coefficients private. Publish functional findings only.

- All 54 complete TCP responses and 2,957 live Pico packets passed recorded length/CRC checks; the recorded sensor map matches reconstruction from configuration. There are also 300 normalized SBMS receipt records. Python comparison was not repeated for every packet in this review; these frame checks do not establish that every field's physical meaning is known.
- The selected secondary voltage is a configured voltage sensor already present in raw decoded Pico data, excluded only by the legacy bracketed-name filter. Its raw voltage exactly matches the corresponding virtual battery voltage in every captured packet. Likewise, the main shunt current exactly matches the corresponding virtual battery current. Use one canonical measurement with explicit aliases/SOC association rather than counting duplicate readings as independent supply/load or doubling their totals.
- The primary/secondary voltage difference changes substantially with discharge current. A simple fixed offset is not supported by this recording. A current-dependent fit describes these observations, but receipt timing, shared wiring, sensor gain/offset and operating conditions can confound interpretation; the fit is not proof of wiring resistance or an approved correction. The run covers discharge conditions only and does not establish charge/reversal behaviour or absolute voltage calibration.
- The extra live field following each current reading is a two-word value. Changes in several active channels approximately track time-integrated charge at a candidate scaling. This is evidence of a possible cumulative charge counter, not an additional voltage reading. Other small-current channels did not show corresponding counter increments during the run. Units, deadband, signed representation, rollover and reset behaviour remain unverified; keep the field diagnostic-only and do not replace sample-based Ah/Wh integration with it.
- Other hidden voltage inputs include near-zero, unavailable and high/out-of-scope values. Sensor type alone does not establish a connected valid voltage for the main battery. Select the intended voltage source explicitly and validate it for the associated voltage domain; do not automatically adopt extra inputs because they contain numeric readings.
- Actual configured sensor IDs, exact labels, selected voltage and battery SOC association can now be mapped privately. Configuration also contains shared per-module token fields and channel-number patterns that may strengthen identity checking; their semantics and persistence require verification. Preserve raw metadata and a configuration fingerprint, and keep the permanent database metric ID separate from both sensor IDs and labels. One configuration does not prove identity persistence after reconfiguration.
- The existing Pico collector receives about ten raw packets per second in this recording, while SBMS receipt is about once per second. The logger should use timestamped internal snapshots before legacy output throttling/filtering, with bounded work and appropriate source alignment. Receipt timestamps identify arrival, not necessarily simultaneous physical measurement; preserve this limitation in cross-source calibration/integration.

The source/version remain unchanged. Logging/API implementation is still the next authorized implementation step when requested; this review provides acquisition/mapping evidence rather than a completed database or sensor calibration.


## Authorized logging implementation — 0.5.0

The owner requested code to start logging. The canonical Pico main-battery measurement is its configured battery instance, which provides current and SOC; its separate physical main-shunt display alias is excluded from history. The selection is private, ID-based and generated from the acquisition capture rather than hard-coded into public source. Pico internal voltage is excluded; only the selected raw secondary voltage is logged independently. No constant voltage correction is applied.

`lib/logger.js` accumulates bounded last-valid-reading integrals at UTC boundaries. `lib/history-store.js` persists atomic checkpoints, source/display names and selected metric definitions in SQLite; higher periods accumulate additive integrals independently so pruning child records cannot lose parent totals. Last valid SOC is device-specific. Retained/repeated SBMS messages remain filtered upstream. Freshness defaults are 2 seconds for Pico and 3 for SBMS, with immediate disconnect invalidation. Current, voltage and power coverage are independent, and cross-source Pico watts use fresh SBMS pack voltage only.

Electrical arrays preserve signed source current/net watts; confirmed per-channel polarity separately classifies directional totals. SBMS external-load current is kept distinct from battery current without inferring a gross-load balance. The owner confirms both PV currents are supplied to the battery, so each PV input uses its own fresh SBMS pack voltage for battery-side watts/Wh alongside current/Ah. Missing voltage leaves a power gap; valid zero current produces zero watts. Only unverified directional classification remains null. This confirmation supersedes the earlier PV voltage-domain uncertainty; these values describe battery charging power rather than panel-terminal power.

Hourly environmental values use the last valid sample; daily pressure uses the last sample and outside temperature uses all-sample extrema. The implemented retention and calendar cutoffs are listed in LOGGING.md. A 60-second checkpoint allows up to one uncommitted minute of loss after an abrupt shutdown; graceful stop flushes. SQLite remains outside zram-managed folders, single-writer, and safe to inspect through a separate read-only connection. Source selection/fingerprints and real recordings remain private. Database IDs are stable within the configured mapping; hardware identity persistence after reconfiguration still needs verification.

The local history API, access policy and Android request/chart implementation remain the next stage after live Pi checks. Read-only `bin/history.js` is available now for initial testing; it is not a network API. No Android APK is created.
