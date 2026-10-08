# Database and logging plan

Updated discussion draft, 2026-10-08. The owner's logging/retention requests below supersede the initial broad candidate list and retention proposal. No database, logger, ElectroDacus subscriber or history API is implemented by this document. Calculation/storage refinements and the remaining decisions require agreement.

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
| ElectroDacus | Voltage, total/battery current, PV1/PV2 charge currents and SOC; analogous electrical summaries with the extra values | Confirm whether the same electrical retention rules apply |

Do not silently include other temperatures, tanks, tilt, cell voltages or flags in regular history. They were initial candidates, not selections in the owner's latest request. Connection/coverage tracking supports correct calculations; user-facing alarm/event history is a separate decision.

The owner's proposed aggregation sums received current and voltage over each minute and divides each by 60, then multiplies their averages. The following refinements are recommendations for accurate power/energy reporting, not changes already agreed or implemented.

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

A compact electrical representation can remain `[average_W, average_A, average_V]`, with the main battery adding SOC. Recommended timestamp/coverage and Wh/Ah totals belong in named database columns alongside that array. SOC aggregation is still to be chosen; last valid SOC is recommended for an end-of-period battery state. Per-shunt voltage association must be explicit and correct for the measurement point. Do not present battery-side estimated solar power as directly measured panel-side power.

## Storage proposal

Use a local SQLite database on persistent Pi storage outside zram-managed directories (proposed `/var/lib/ella/history.sqlite`). The collector writes it; a future local API serves graphs to Android or a browser. Existing MQTT payloads remain unchanged.

| Table | Proposed contents |
| --- | --- |
| `metrics` | Source/channel identity, exact label, unit, stable sensor ID or MQTT field path, voltage association and measured/derived provenance. |
| `history` | UTC interval bounds, resolution, electrical averages or environmental readings/extrema, optional SOC, additive integrals/energy and valid coverage. Unique channel/resolution/interval keys prevent duplicate rollups. |
| `energy_totals` | Optional retained directional Wh/Ah totals, or store these alongside each electrical summary to avoid duplication. Final schema remains open. |
| `events` | Proposed source/collector availability and clock-change records; additional monitor alarms/flags are not selected yet. |

Minutes/hours/days/months identify non-overlapping buckets. Confirm the reporting timezone, calendar versus fixed-day retention, and incomplete-current-period display. Day lengths and calendar month lengths vary; never assume every day has 24 elapsed hours or every month has 30 days. Commit longer-term summaries before pruning shorter-term rows. Restart-safe checkpoints must prevent duplicate energy accumulation or loss of the in-progress bucket.

Batch bounded writes into transactions; commit cadence and acceptable uncommitted-data loss are still open. Indefinite monthly/environmental history has no automatic expiry. Measure storage growth and range-query performance on the Pi before making resource claims.

## ElectroDacus input

Subscribe to the existing broker's ElectroDacus topic independently of Pico acquisition. Confirm a live payload and source timestamps/reporting cadence before implementing voltage, battery/total current, PV1/PV2 current and SOC decoding. The existing flow maps `soc`, `cellsMV`, `currentMA.battery` and `currentMA.pv1`; its PV2 gauge has no incoming wire. This does not establish that PV2 is unavailable in the firmware, only that the copied flow does not receive/display it.

Establish whether voltage is directly supplied or must be derived from the correct active cell readings, and confirm current signs and the physical location of each current/voltage measurement. See [existing dashboard mappings](../node.red/flows.json). No private runtime/device inventory is included here.

## Decisions still needed

- Agree averaging instantaneous power, duration-aware rollups and preservation of directional Wh/Ah/coverage alongside the requested arrays.
- SOC: last valid value, average, or another representation?
- Hourly pressure/outside temperature: last valid reading or an average? Daily pressure is explicitly the last valid reading; daily temperature is explicitly minimum/maximum.
- Reporting timezone and precise retention cutoffs; same electrical retention for ElectroDacus?
- Correct per-shunt voltages, SBMS payload/PV2 mapping and accepted power-loss window.

Reference: [SQLite appropriate uses](https://sqlite.org/whentouse.html).
