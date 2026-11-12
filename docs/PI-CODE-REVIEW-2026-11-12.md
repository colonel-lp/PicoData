# Pi code review — 12 November 2026

Reviewed repository `colonel-lp/PicoData`, branch `main`, commit `6731317a7fad2d5b6edd8660e2f081cc8d733a66`. Collector 0.7.5 and diagnostic helper 1.3.

## Compatibility correction — collector 0.7.7

Owner deployment evidence revealed a regression introduced by R6's implementation:
the 0.7.6 supported-type list omitted type 10, previously accepted by the working
collector. Rejecting the whole configuration stopped Pico acquisition. Version
0.7.7 restores that type's previous single-slot opaque handling and adds it to the
shared regression fixture, including Python parity and later-sensor alignment.
The original test fixture did not cover this installed type. This preserves known
installation compatibility; it does not claim to decode type 10 or establish its
meaning on every firmware. Other unknown types remain rejected.

## Implementation follow-up — collector 0.7.6 / monitor 1.4

The owner subsequently authorized the fixes. R1–R8 are addressed in source,
with startup/buffer/clock/CRC/configuration, history failure isolation, health
capture and installer rollback regression checks. Bounded LAN request correlation,
API bind retry, reduced copying/capture/monitor work, logger error categories and
explicit IPv6 support are included. Original findings below describe the reviewed
0.7.5/1.3 baseline, not remaining known failures in 0.7.6/1.4.

The previous chat supplied the copy-based updater and historical USB pi-wifi
script. They are now versioned as documented in [deployment](../node.js/DEPLOYMENT.md);
private profile/SSID values are removed from the historical reference. The installed
Pi files/hashes are still unverified. Source changes do not identify the original
monitoring glitch, establish Pi resource savings, enable the old network switcher
or update Android. See [validation](../android/BUILD-VALIDATION.md).

## Scope and deployment evidence

The repository quick reference confirms built-in Wi-Fi in STA-only mode, sensors on the mobile router, the USB adapter removed, and hotspot recovery and automatic network switching disabled. This review follows that configuration.

The review covers the repository implementation of the active collector and diagnostic helper, all seven collector command-line scripts, all three operational Python tools, the service template, dependencies and operational documentation. The exact files installed on the Pi have not been retrieved or compared. The updater uses a sparse temporary clone and copies files, so the installed directory need not contain Git metadata. The findings apply to this source revision wherever its files are installed.

| Area | Reviewed files and limits |
|---|---|
| Acquisition and MQTT | `lib/client.js`, `pico-protocol.js`, `crc16.js`, `sensor-list.js`, `readings.js`, `mqtt-publisher.js`, `sbms.js`, `bin/pico.js` |
| History | `logger.js`, `history-store.js`, `history-config.js`, `history-reader.js`, `battery-history.js` |
| API and Pi controls | `api.js`, `api-config.js`, `pi-status.js`, `tools/pi-api-controls.py` |
| Operational tools | `tools/ap-watch.py`, including dormant AP paths, and `tools/zram-start-guard.py`, whose installation is still marked pending |
| Command-line utilities | Initialisers, history inspection, API check, capture verification and replay |
| Deployment | Service template, package files, README, logging/API/monitor guides and Pi quick reference |
| Outside the available source | Actual `~/update-pico.sh`, `/usr/local/sbin/pi-wifi`, installed service overrides, NetworkManager profiles, Mosquitto and zram configuration. These need a private copy or read-only Pi inspection to finish deployment verification. |

The archived Python/SignalK implementations and Node-RED dashboard are historical references. The current collector operates independently of Node-RED.

## Findings

Priorities indicate suggested repair order. P1 affects measurement correctness. P2 affects reliability or diagnosis. Conditional findings include the necessary trigger.

### R1 — P1 — Buffered Pico packets acquire a false fresh timestamp

During configuration, `cycle()` retains the latest live packet as `{msg, info}`. After TCP configuration completes it passes that packet into `live()`, which assigns the current wall and monotonic time and resets the stale timer. If broadcasts stop while configuration is still completing, an old measurement becomes fresh again and can enter history with the wrong receipt time. The configuration sequence can include retries and multiple response waits.

A synthetic call through `live()` confirmed that the buffered packet is retimestamped. The packet cache has no original receipt timestamp or age check.

Record receipt timestamps at the socket listener and reject buffered packets older than the applicable freshness limit. Alternatively, wait for a new matching packet after configuration. Add a regression covering a packet received before a delayed configuration response and subsequent silence.

[Source](https://github.com/colonel-lp/PicoData/blob/6731317a7fad2d5b6edd8660e2f081cc8d733a66/node.js/lib/client.js#L90)

### R2 — P2 — A backward clock adjustment pauses Pico MQTT output

The live freshness logic uses monotonic time, but public output throttling compares `Date.now()` against `lastOutput`. After a backward system-clock step, the difference stays negative until wall time catches up. Valid snapshots continue reaching the logger/API while MQTT publication can stop for the duration of the clock correction.

A synthetic one-hour backward step produced two snapshots but only one public readings event. Use monotonic elapsed time for the output interval, retaining wall time only for displayed dates. This fault would affect MQTT consumers and does not by itself explain an Android API interruption.

[Source](https://github.com/colonel-lp/PicoData/blob/6731317a7fad2d5b6edd8660e2f081cc8d733a66/node.js/lib/client.js#L140)

### R3 — P2 — Live decoding accepts bad checksums and ignores UDP declared length

`parseResponse()` checks field structure but explicitly skips CRC validation. UDP reception also does not compare `frameLength()` with the datagram length. The capture verifier already implements both checks, and the README records that they matched the previously supplied capture.

Flipping a synthetic packet's final CRC byte made `crcMatches()` fail while `parseResponse()` still accepted it. A malformed or incompatible packet with plausible fields can therefore refresh source state and reach history. This establishes an acceptance gap, without establishing that corrupt packets caused the reported glitch.

Share validated framing/checksum logic between capture verification and live reception. Verify compatibility against the retained firmware captures before enforcing it. Rejected packets must retain the previous sample's original age and produce a bounded diagnostic reason.

[Source](https://github.com/colonel-lp/PicoData/blob/6731317a7fad2d5b6edd8660e2f081cc8d733a66/node.js/lib/pico-protocol.js#L45)

### R4 — P2 — Some failures present at monitor startup never create an incident

`LearnedChecks.check()` requires two successful observations before it can trigger a failure. That is sensible for optional ICMP peers, but the same gate applies to a configured local API listener and the collector service. If the listener is unavailable from a fresh monitor start, repeated failures alone never arm the check. A direct check with 120 consecutive failures produced zero triggers.

Use a startup grace period followed by failure detection for required configured components. Keep capability learning for optional peers. Record an explicit unestablished baseline so an empty archive cannot be mistaken for evidence of health.

[Source](https://github.com/colonel-lp/PicoData/blob/6731317a7fad2d5b6edd8660e2f081cc8d733a66/node.js/tools/ap-watch.py#L801)

### R5 — P2 — Existing source/logger faults can be invisible after the monitor restarts

The loopback probe deliberately sends no credentials and accepts the normal 401 response. In `api.js`, authentication happens before source or database inspection. That probe can therefore succeed while the logger has failed or a sensor is stale. The monitor learns source/logger failures from journal transitions, but starts its journal follower at `-n 0`. A failure already present when monitoring begins can have no new transition to capture.

Expose or read a narrowly scoped local health snapshot containing source state, logger state and last successful save. Establish current health when the monitor starts, alongside its existing transition capture. Give the startup snapshot its actual observation time.

This is a concrete reason why a responding listener and no matching Pi incident are insufficient to rule out every collector-side fault. It does not identify the cause of the November 11 event.

[Source](https://github.com/colonel-lp/PicoData/blob/6731317a7fad2d5b6edd8660e2f081cc8d733a66/node.js/tools/ap-watch.py#L759)

### R6 — P2, conditional — Unknown sensor types can shift subsequent decoded positions

`createSensorList()` begins each entry with `elementSize = 1`. Unrecognised types retain that assumption and parsing continues. Duplicate sensor IDs also overwrite earlier entries. If a new firmware type consumes more than one live position, later known sensors can read the wrong fields. The logger's binding check validates configuration references but cannot establish that the guessed packet offset is correct.

Validate required config fields and unique IDs. For unsupported types whose width is unknown, reject the configuration with a clear diagnostic or use a verified width table. This is a firmware/configuration compatibility risk; the currently reported sensor set has not been shown to trigger it.

[Source](https://github.com/colonel-lp/PicoData/blob/6731317a7fad2d5b6edd8660e2f081cc8d733a66/node.js/lib/sensor-list.js#L9)

### R7 — P2, conditional — Live API delivery depends on successful history database reads

Every `/api/v1/live` response opens a history read transaction and rebuilds the catalogue from SQLite, despite live samples already being in memory. A database read error produces `503 history_unavailable` for live monitoring too. History reads and writes use synchronous SQLite on the collector event loop, so storage latency also delays acquisition callbacks and HTTP responses.

Cache the validated active catalogue in memory and refresh it on relevant configuration/name changes. Permit live data and explicit logging-failure status to remain available when history reads fail. Preserve mapping checks and source freshness. Measure event-loop delays on the Pi before considering a database worker, which would be a larger change.

[Source](https://github.com/colonel-lp/PicoData/blob/6731317a7fad2d5b6edd8660e2f081cc8d733a66/node.js/lib/api.js#L44)

### R8 — P2, optional installation path — Pi control setup can leave a partial installation

`pi-api-controls.py` writes or removes the sudoers rule before creating the configuration backup and replacing `api.json`. A later filesystem failure can leave the privilege rule and API setting inconsistent. The error path reports setup failure without restoring the earlier rule.

Prepare and validate both outputs first, preserve prior state, and restore it if the second replacement fails. Test failures during backup creation and API replacement. This concerns setup/remove operations. Current STA-only instructions already require the old switching helper to remain unused until its bindings are adapted.

[Source](https://github.com/colonel-lp/PicoData/blob/6731317a7fad2d5b6edd8660e2f081cc8d733a66/node.js/tools/pi-api-controls.py#L71)

## Improvements and optimisations

1. **Correlate app requests with Pi events.** Add bounded diagnostic records for route category, request identifier, local destination/peer LAN address, result code and elapsed time. Record rare failures and recovery transitions, with a short RAM history for fault capture. The current API handler emits no per-request diagnostic evidence, so requests that reach it may still leave no useful trace. The requested Android LAN-address logging belongs in the same diagnostic plan.
2. **Reduce repeated work in the hot path.** `decodeReadings()` deep-clones the sensor metadata through JSON. With logging/API and MQTT enabled it normally runs once for snapshots and again for public output. `bin/pico.js` also installs packet/TCP recording listeners when capture is disabled, causing avoidable hex conversion. Cache immutable metadata and enable capture listeners only while recording, preserving the separate legacy MQTT conversion rules. Benchmark before assigning a performance benefit.
3. **Make source refresh and collector startup resilient.** An API bind failure currently stops the entire collector, including MQTT and logging. Evaluate a degraded state with explicit API retry while acquisition continues. Capture startup failures distinctly so a port conflict cannot masquerade as a sensor problem. This is a proposed operating-policy change.
4. **Improve retained failure reasons.** The logger's catch block discards the exception and sets a generic permanent failed state. Retain a bounded error code/category such as SQLite busy, I/O failure or invalid data, plus the phase. This would help distinguish storage failure from a calculation/configuration fault without requiring the original process to remain running.
5. **Reduce monitor overhead.** The STA daemon rereads the same configuration every half second, while the journal follower streams the whole system journal and filters in Python. Use an appropriate slower config check and journal-side filters that preserve the relevant kernel/service entries. Measure CPU and process activity on the Pi before and after.
6. **Address IPv4-only API binding explicitly.** `api-config.js` accepts only IPv4 and the listener binds that address. A `.local` hostname can have a different address-family choice at the client. Check the actual resolution and attempted family during the next fault. Either keep a verified IPv4 client selection or deliberately add/test dual-stack support. The repository alone cannot establish what address Android selected during the incident.
7. **Make deployment reproducible.** Track sanitised source for the existing updater and `pi-wifi`, together with installed-source hashes/version receipts. This allows an exact installed-versus-repository comparison without requiring private configuration in Git. A deployment inventory should identify copied helpers separately from the collector version.

## Verification and limitations

- All **90 Node tests passed**, using Node 24.22.1 in the review environment. They include synthetic/loopback Pico and MQTT tests, SQLite/history, API and Pi-status tests. This is a newer Node runtime than the Pi's previously reported Node 22 installation.
- Four focused synthetic probes confirmed R1 retimestamping, R2 wall-clock throttling, R3 checksum acceptance and R4 unarmed startup checks. These probes were temporary review tools.
- All Python files under `node.js` parsed successfully.
- Python suites were attempted. AP-watch ran 40 tests with 15 skipped, Pi API controls ran 2 with 1 skipped, and zram guard passed 9. STA-watch reported 6 errors among 8 tests and Wi-Fi flight reported 30 errors among 36, caused by the root-owned-directory requirement in the non-root test environment. These suites need a suitable privileged test environment before certification.
- No direct Pi filesystem comparison, installed service inspection, actual radio test, SD latency measurement or replay of the November 11 incident was available in this review. Findings cannot be claimed as the demonstrated cause of that event.

## Suggested repair order

First address R1 and R2, then validate/enforce packet integrity and configuration structure (R3/R6). Improve startup and current-health diagnostics (R4/R5), and decouple live delivery from history reads (R7). Handle the optional installer rollback issue (R8) before using that installer again. Measure performance before adopting the larger optimisations.
