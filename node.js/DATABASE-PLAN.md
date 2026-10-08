# Database and logging proposal

Draft for discussion, 2026-10-08. No database, logger or history API is implemented by this document. Values, sampling and retention remain to be agreed.

## Storage and structure

Use a local SQLite database on persistent Pi storage outside zram-managed directories (proposed `/var/lib/ella/history.sqlite`). The existing collector would write it; a future local API would serve live/history data to a browser or Android viewer. Keep existing MQTT payloads unchanged. SQLite is appropriate for embedded local storage with one writer; this is a design recommendation, not a measured Pi benchmark.

| Table | Proposed contents |
| --- | --- |
| `metrics` | Numeric metric ID, source/device identifier, stable sensor ID or MQTT field path, metric key, exact display label, unit and measured/derived provenance. Keep nominal capacity/configuration metadata here rather than repeating it in every sample. Preserve sensor identity across label changes and distinguish sources. |
| `history` | Metric ID, UTC interval start, resolution, mean/minimum/maximum/last value, valid sample count and valid coverage duration. Unique key `(metric_id, resolution, interval_start)` supports range queries and prevents duplicate summaries. Reject unavailable/sentinel measurements without changing legacy MQTT output. |
| `energy_totals` | Per circuit/source/day charge and discharge Ah, generated and consumed Wh, and coverage duration. Keep generation/consumption separate rather than just a net balance; define the reporting timezone before daily grouping. Derived totals retain their input provenance. |
| `events` | UTC timestamp, source, event type and state: connection loss/recovery, monitor flags/alarms, collector restarts and clock discontinuities. Record changes, rather than repeating unchanged flags every second. |

UTC timestamps are independent of the legacy MQTT local-time components. Use monotonic elapsed time for energy integration within a collector run and do not bridge restarts, stale readings, missing inputs or clock jumps. Keep Pico and ElectroDacus history distinct; choose an explicit preferred source for combined reports instead of adding both monitors' measurements of the same battery.

## Candidate readings

- Electrical: battery voltage/current/SOC/remaining Ah from each monitor; starter voltage; each available circuit current; directly reported solar currents; active cell voltages, cell imbalance and monitor temperatures.
- Environment: each available temperature, tank percentage/remaining capacity, barometric pressure, pitch and roll. Decide whether tilt needs continuous history or live display only.
- Events: charge/discharge permission flags, cell/voltage/temperature-related flags, end-of-charge and source availability, after confirming actual SBMS field semantics.
- Derived: circuit/battery power, separately accumulated charge/discharge Ah and generated/consumed Wh, and daily totals. Derive watts only with the correct voltage for the measurement point and sufficiently fresh inputs. Do not treat a battery-side estimate as measured panel-side solar power.

The Pico implementation is `lib/readings.js`; existing dashboard mappings are in [`../node.red/flows.json`](../node.red/flows.json). The flow maps SBMS `soc`, `cellsMV`, `tempInt`, `tempExt`, `currentMA.battery`, `currentMA.pv1` and `flags`. `currentMA.extLoad` has no downstream display connection and the PV2 gauge has no incoming wire; their presence in the flow does not establish a working extra measurement. Confirm active cell indices, solar channels, scaling, signs and flags from a live SBMS payload before implementation. No private sensor/device inventory is included here.

## Proposed sampling and retention

Process fresh incoming readings at their available rate for power/energy calculations, independently of history resolution. Suggested starting policy, subject to agreement and storage measurement:

- Keep 10-second summaries for 7 days.
- Keep 1-minute summaries for 90 days.
- Keep hourly summaries and daily energy totals for 2 years.
- Keep status/alarm events on change.

Min/max preserve peaks in a graph, but do not preserve their exact shape/timing. Weight aggregates by valid elapsed coverage, combine summaries without averaging averages blindly, and do not infer energy from a sparse snapshot or integrate an outage as zero. Graphs should show missing coverage. Apply retention only after the longer-term summaries are committed, and avoid counting multiple resolutions in energy totals.

Batch bounded writes into transactions (proposed every 30 seconds) to reduce commit frequency. Abrupt power loss may lose the uncommitted batch; the durability policy must be explicit. Benchmark representative selected-metric volumes, range queries, retention and interruption recovery on the Pi before accepting storage/performance claims.

## Decisions still needed

- Record every useful measurement or only selected electrical/environmental channels?
- Required graph detail and history duration; acceptable power-loss window.
- Preferred battery/voltage sources, correct voltage points for power calculations, actual SBMS payload and current polarity.
- Whether daily reporting follows UTC or a configured local timezone.

Reference: [SQLite appropriate uses](https://sqlite.org/whentouse.html).
