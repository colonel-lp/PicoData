# Pi history logging — 0.5.0

The collector writes selected measurements to SQLite when `PicoData/logging.json` exists. MQTT publishing and its existing JSON contract remain unchanged. No separate database server or npm database package is needed. Logging requires Node 22.13+ with built-in `node:sqlite`; reader-only/MQTT operation remains compatible with Node 18+. Node 22 can print one SQLite experimental-feature warning at startup.

## Enable logging

1. Update the root `node.js/` collector using the [existing update commands](README.md#run-on-the-pi), then run `npm ci --omit=dev` in its installed directory.
2. Generate a **private** selection file from a local capture containing the Pico configuration. Choose the main **battery instance**, selected secondary voltage, outside thermometer, barometer and individual load shunts. Exclude the separate alias of the main shunt and Pico internal voltage. The numbers below are invented examples; use your verified sensor IDs.

```bash
cd ~/PicoData/node.js
node bin/init-logging.js --capture capture.jsonl \
  --battery-id 101 --secondary-voltage-id 103 \
  --outside-id 104 --barometer-id 105 \
  --load-ids 102,106 --load-polarity -1
```

The command creates `~/PicoData/logging.json`, refuses to overwrite it, and enables all four SBMS current channels including PV2. It records configuration fingerprints for selected Pico inputs/references. These are safeguards against reassignment, **not proof of permanent hardware identity**. Review the generated selection and signs before starting; the battery default is positive charge/negative discharge. `--load-polarity -1` means negative raw current represents consumption. Positive-consuming installations need `1` instead.

3. Restart the existing service. No change to its `node.js/bin/pico.js` entry point or working directory is necessary.

```bash
sudo systemctl restart pico-mqtt.service
journalctl -u pico-mqtt.service -n 20 --no-pager
```

Look for `{"source":"logging","state":"started"}` and normal source connection statuses. A `mapping-error` identifies a selected metric whose configuration/type no longer matches; resolve its mapping deliberately. A `failed` logging state means history has stopped and the collector must be restarted after the storage/configuration problem is fixed. Pico/MQTT operation continues after a runtime logging failure. Invalid configuration or an existing writer prevents startup.

After two minutes, read the database while the service is running:

```bash
node bin/history.js
node bin/history.js --metric pico-battery --resolution minute --limit 5
node bin/history.js --metric sbms-battery --resolution hour --limit 24
```

`integrity` should be `ok`, `savedAt` should advance and valid electrical rows should show positive coverage. This command is read-only. It reports the most recent checkpoint, so readings can be up to a minute behind live MQTT. `--config FILE` selects another private configuration. The history API and Android charts are a subsequent step.

For a foreground test, stop the service first; the database permits only one writer:

```bash
sudo systemctl stop pico-mqtt.service
node bin/pico.js --duration 120
sudo systemctl start pico-mqtt.service
```

Normal readings remain silent. Use `--stdout`/`--sbms-stdout` only for diagnostics. `--logging-config FILE` explicitly selects history settings; `--no-logging` temporarily disables history without deleting it. Without the default configuration file, the collector operates as before.

## Storage and periods

The default database is `~/PicoData/history/history.sqlite`, resolved relative to `logging.json`. Keep that directory on persistent storage **outside zram-managed filesystems**. The configuration/database have private permissions. Local captures, selections, SQLite databases and their sidecar files are ignored by git; do not upload them. The `.writer-lock` file implements an SQLite OS lock: it may remain after shutdown, but a crash releases its lock automatically. Do not delete it to bypass a running collector.

All boundaries and acquisition timestamps use UTC. The Pi system timezone can remain unchanged. Future charts can convert to `Europe/London`, including GMT/BST. A UTC day is the recorded day; a later local midnight-to-midnight view cannot be reconstructed exactly from these daily summaries once the shorter data has expired.

| Measurements | Resolution | Retention |
| --- | --- | --- |
| Pico/SBMS electrical; secondary voltage | Minute | 24 hours |
| Pico/SBMS electrical; secondary voltage | Hour | 7 days |
| Pico/SBMS electrical; secondary voltage | Day | 1 calendar month |
| Pico/SBMS electrical; secondary voltage | Month | Indefinite |
| Pressure; outside temperature | Hour, last valid sample | 1 calendar month |
| Pressure | Day, last valid sample | Indefinite |
| Outside temperature | Day, minimum/maximum of all valid samples | Indefinite |

Retention expires **completed buckets by their end timestamp**. One month means subtract one UTC calendar month, clamping the day when necessary. Partial current buckets are checkpointed as well as completed buckets. The larger summaries accumulate integrals directly; deleting minute records never removes their already saved contribution to the larger intervals. Empty periods are left empty.

## Values and accuracy

Electrical `values` are `[averageWatts, averageAmps, averageVolts]`; batteries add the last valid SOC from that device as element four. Missing values are `null`; a valid zero remains zero. SOC is not borrowed from another device or carried into an empty period. Raw current and derived net watts retain source signs. The secondary voltage is stored independently, without a calibration offset.

Averages use monotonic elapsed seconds, holding the latest valid reading only until the configured freshness deadline. Defaults are 2 seconds for Pico and 3 seconds for SBMS; source disconnects invalidate inputs immediately. Every validated Pico snapshot is used before legacy name filtering/output throttling. SBMS retained/exact repeated messages remain excluded by its receiver.

`averageWatts = integral(current × voltage) / powerCoverageSeconds`. This is the average of instantaneous power, not average current multiplied by average voltage. `netWh`, `netAh` and additive integrals are stored alongside independent current, voltage and power coverage. Energy is calculated only over observed valid coverage, with no extrapolation across outages or process downtime. Short gaps within the freshness limit are a bounded last-reading estimate. Different sources' receipt times do not prove simultaneous physical sampling.

Pico battery/load watts use **fresh SBMS pack voltage** as derived battery-bus power. Missing SBMS voltage leaves a power gap while Pico current/SOC can still be recorded. There is no fallback to Pico internal or secondary voltage. SBMS battery/external-load currents use their own valid pack voltage. The external-load channel excludes monitor consumption and overlaps battery measurement; do not add it to battery current or label it a verified gross-load balance.

The owner confirms PV1/PV2 current measures charge supplied to the battery. Both channels therefore use fresh SBMS pack voltage for battery-side watts and Wh, with the same summaries/retention as the other electrical channels. These are battery charging power values, not panel-terminal power. Missing pack voltage leaves a power gap while valid PV current/Ah can still be logged. Directional PV/external-load classifications default to `null` until polarity is verified; raw signed current, net Ah and net Wh remain available, including valid zero PV2 power.

With confirmed `polarity`:

| Role | Forward totals | Reverse totals |
| --- | --- | --- |
| Battery | Net charging | Net discharging |
| Load | Consumption | Reverse flow |
| Supply | Generation | Reverse flow |

Directional `Wh`/`Ah` split each observed sample before aggregation, so charging and discharging do not cancel. Battery totals describe **net battery flow**, not simultaneous gross charging/load. Preserve Pico and SBMS battery histories separately for comparison; they describe the same physical battery and must not be summed. An unverified polarity is `null`, never guessed from a zero reading.

Stable metric IDs are separate from sensor IDs, exact source names, display names and packet positions. Configuration refresh updates names without creating a new series; the database keeps time-effective source/display name changes. Changing an existing metric's binding, role, voltage reference or polarity is rejected to avoid mixing incompatible history. Use a deliberate migration or a new metric ID for a verified mapping change; do not discard the database to work around it. Source IDs are not proven permanent across firmware/reconfiguration.

## Checkpoints and recovery

SQLite uses WAL with `synchronous=FULL`, a small cache and one writer. Default `commitSeconds: 60` commits all dirty summaries and applies retention in one transaction. Normal service shutdown flushes the tail. An abrupt power cut/process kill can lose up to the uncommitted minute; SD/storage hardware still determines actual durability. On restart, saved partial summaries resume, but live inputs must be acquired again. There is no integration over downtime.

Clock steps beyond the tolerance or backwards time skip the affected interval and clear inputs. Backwards steps pause integration until the previous UTC position is reached. Inspection reports a persistent `clockSteps` counter. Large clock corrections need investigation before relying on chart timing. Long unattended operation, Pi storage performance and live power comparisons remain hardware test tasks.

## Offline replay

Replay can exercise a private capture without device/network access. It refuses to overwrite a database and does not upload anything:

```bash
node bin/replay-logging.js --capture capture.jsonl \
  --config ../logging.json --database /tmp/history-replay.sqlite
```

The replay reconstructs configuration/sensors from raw Pico records and uses recorded Pi UTC receipt order for elapsed time. Captures with receipt timestamps out of order are rejected. It cannot recover monotonic Pico timestamps absent from older captures or prove sensor calibration.

The schema consists of `metrics` (definitions/latest names), `names` (rename history), `history` (metric/resolution/UTC interval plus additive statistics), and `meta` (checkpoint/clock information), at schema version 1. Statistics are stored as named JSON fields; arrays are a presentation format. Backup after stopping the service by copying the entire history directory; never copy just the main SQLite file from a running WAL database.

Official references: [Node 22.13 SQLite module availability](https://nodejs.org/en/blog/release/v22.13.0), [SQLite WAL](https://sqlite.org/wal.html), [SQLite synchronous settings](https://sqlite.org/pragma.html#pragma_synchronous).
