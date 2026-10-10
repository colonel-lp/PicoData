# Build validation

## Android 0.21 — dashboard corrections, Pico runtime and updates, 2026-10-10

Source parent: PicoData b9eec4844e5cc23d35ad46ac4d3dc85324a3a4fd. Read-only EQ reference e1b54f3f58efb92215d99e2e3f2941530fbcbc15. Android build 3, min API 27, compile/target API 37; AGP 9.4.0 / Gradle 9.6.0 / JDK 17. Pi collector 0.7.1 and API/history/MQTT contracts are unchanged.

**assembleDebug, testDebugUnitTest and lintDebug pass. All 37 tests pass, no failures/errors/skips; lint reports zero errors and 49 warnings.** Warnings cover existing intentional LAN/private-CA policies, English strings, legacy guarded APIs, programmatic view constructors/allocations, newer manifest/orientation notices and the EQ-style native alert resource lookup. SDK targets are not lowered and no lint baseline disables errors.

- Fifteen model/history checks include the new runtime charging/discharging capacity/rounding/sign/format cases, zero/missing/invalid/stale inputs and separate raw Pico/starter voltages independent of SBMS reference freshness. Original electrical/history/source/coverage/CSV semantics remain covered.
- Four loopback HTTP checks preserve authentication, bounds, redirect rejection and cancellation behavior.
- Eleven framework activity checks cover API 27/35 navigation, lock/flags/cells/source clearing, forced landscape, credential-excluding presets/theme recreation, persistence/status hiding, fullscreen system-bar inset release and Settings System/Back with no theme controls.
- One filesystem-backed MediaStore stand-in checks legacy theme migration, file load/rename/delete and path rejection on API 35. The API 27 activity path verifies legacy named files. These are synthetic framework/storage checks, not a real Downloads provider/permission test.
- Five update checks cover Ella-only valid repository/newer release selection, all intervals including automatic Off, confirmed-version cleanup and signature comparison/complete bundled notes. Real GitHub download and Android installer/unknown-app prompts are not hardware-tested.
- Native API 35 renders check 1024×600, 1280×720, 800×480 and portrait fallback; a fuller synthetic dashboard verifies nine evenly spaced current rows with 6px gaps, all 13 gauges/readout bounds, and Settings layout. Live and Settings images were inspected privately. Source fixtures contain generic synthetic values, not the owner's photo/installation data.
- The exact complete android/CHANGELOG.md is bundled in assets/changelog.md. ApiClient, HistoryData, ChartView, PrivateSettings and ScreenControl remain unchanged. No collector/database/logging files are changed.
- APK metadata confirms com.colonellp.ellamonitor.preview, versionName 0.21, versionCode 3, minimum 27 and target 37. APK v2/v3 verification succeeds for API 27 onward. The retained certificate SHA256 f092a9f4313643bd9d9da4701afb02d48d0ca5fb3b3234a9b1ef0bfef6d23be9 matches 0.2.0 for in-place updates. Delivered Ella-monitoring-v0.21.apk SHA256: fb6ca00abd7e0d0fbf44f9f3c33c8e5e1513b98d8ee971fa195026c219a974d5.

These are compiled APK, synthetic-network/model/framework/storage and renderer results. Exact phone/Joying fit, actual Pi readings/runtime, storage/LAN/notification permissions, future-release installer handoff, FYT screen behavior and long foreground/background recovery require owner device verification. No physical-device or Android 17 device installation is claimed. The new updater is configured for PicoData releases; committing source alone does not publish an attached APK release.

## Android 0.2.0-preview — companion layout/themes/settings, 2026-10-10

Source parent: PicoData `266b5884e3eeaf3e76c13855b2a6522ace817039`. Read-only UI reference: Joying EQ & DSP `e1b54f3f58efb92215d99e2e3f2941530fbcbc15` (1.54-beta). Android build 2, min API 27, compile/target API 37; pinned AGP 9.4.0 / Gradle 9.6.0 / JDK 17. Collector 0.7.1 and database/API contracts remain unchanged.

**`:app:assembleDebug :app:testDebugUnitTest :app:lintDebug` succeeds. All 28 tests pass, zero failures; lint has zero errors and 31 warnings.** Warnings include the intentional LAN HTTP/private-CA preview policies, English-only text, programmatic custom-view constructors/draw allocations, and newer manifest/orientation policy notices. They are not disabled by a baseline. Native Back uses the current API 33+ callback with a documented lint suppression only on the retained legacy API 27–32 override.

- The original 13 model/history contract checks and four real loopback HTTP tests remain intact and pass. Original freshness, source identities, signs, history bucket/coverage logic and exports are unchanged.
- Ten Robolectric activity checks (five cases on API 27 and API 35) verify offline navigation, Settings as a page, retained chart range controls, synthetic readings, green/red/Off flags, dynamically available cells, locked popups, source clearing on stop, forced landscape, saved appearance/preset credential exclusion and recreation, hide-title, and opt-in ongoing-notification show/hide/disable behaviour.
- One API 35 native-renderer test checks 1024×600, 1280×720, 800×480 and a portrait window, retaining the logical landscape arrangement, readout bounds/text layout, one-line alignment and all 13 instruments. Synthetic live and Settings renders were visually inspected. A label/right-alignment issue was corrected with bounded canvas text rendering; no private reference image/readings/inventory is included in source or fixtures.
- Exclusion checks confirm `buildHistory`, `selectMetric`, `loadHistory`, `showHistory`, `showDetail`, `loadSummary` and `summary` methods are unchanged from 0.1.0. ChartView, HistoryData, MonitorData, ApiClient, PrivateSettings and ScreenControl implementations are unchanged. Only the existing range navigation is relocated into a header outside chart contents; original chart/summary colours remain separate from the new global theme.
- APK metadata confirms `com.colonellp.ellamonitor.preview`, versionName `0.2.0-preview`, versionCode `2`, min SDK 27 and target SDK 37. Signature verification succeeds for API 27 and newer. Delivered APK SHA-256: `79cdb84e69d50c876059f85df3dd3c080c88837558de2ac2bad8881e7c6df49e`.

**Signing limitation:** the original 0.1.0 development key is unavailable in the retained build files/artifacts. This build uses a new retained private development key, whose certificate differs from 0.1.0; it cannot update the previous installation in place. Reinstalling clears connection/display settings. No key is committed; production signing and updates remain unsettled.

These are compilation, synthetic-network/framework and renderer checks, not physical-device or live Pi verification. The owner reports API connectivity on the first preview; this new APK still needs phone/Joying comparison with the supplied image, theme/font/preset/long-label interaction, real notification/LAN permissions, portrait/fullscreen scaling, FYT keep-screen/restoration, background/resume and firmware task/battery handling. No Android 17 device or head-unit test was performed by the agent.

## Android 0.1.0-preview — first API viewer, 2026-10-09

Source starts from PicoData `8069a778da17ebb1bec7dfe9846ab836ebc60174`. Collector 0.7.1 and history schema 2 are unchanged. App build 1 has minimum API 27 and compile/target API 37; AGP 9.4.0 / Gradle 9.6.0 / JDK 17.

**Development APK compilation and all 24 tests pass, with zero test failures.** `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug` completes successfully. Lint reports zero errors with 13 warnings: intentionally supported manual LAN HTTP/user-installed CA trust and English-only preview text/localisation work. These warnings are not silently disabled. APK metadata confirms the preview package/version/minimum/target, and APK signature verification succeeds for API 27 and newer using development v2 signing.

- 13 model/contract tests cover original signed currents/power, distinct SOC sources, PV2 valid zero, three-state flags, no string coercion, independent monotonic staleness and cross-source voltage expiry, mapping rejection, verified per-channel load orientation/unknown signs, raw alias metadata, UTC/BST-independent/calendar ranges, environmental extrema, coverage/whole-bucket statistics and CSV escaping/UTC receipt times.
- Four loopback HTTP tests exercise real authenticated GET requests with header-only credentials, rejected redirects, size/version rejection, complete 1,440-row 24-hour pagination over two pages and malformed continuation rejection. Fixtures use invented readings/identifiers, not owner data.
- Six Robolectric activity checks exercise Android 8.1/API 27 and Android 15/API 35: offline launch, bottom controls/theme/history navigation, dynamic synthetic data/flags, clearing current readings on stop and narrower portrait grouping.
- One native-renderer smoke check on API 35 checks label/text-layout dimensions and navigation outside the scrollable dashboard. A temporary synthetic render was visually inspected; a text-alignment issue was corrected. The reference photo and actual owner readings/inventory are not published.

These are compilation, synthetic network/model checks and simulated Android framework/rendering tests. They do **not** establish physical-device Wi-Fi connectivity, Android 17 runtime permission behaviour, Keystore support on the FYT firmware, certificate trust, file-picker delivery, head-unit toolkit compatibility or unattended performance. No live Pi/phone/head-unit connection was performed by the agent.

**Next owner checks:** install the preview; enter private Pi origin/token; compare live values/source signs/flags with Node-RED; verify stale/recovery and foreground/background/resume; test theme/full-screen/keep-screen/rotation/long labels; verify FYT timeout restoration and EQ app interaction; compare retained chart points/coverage with the API and export a CSV. Android 17 permission grant/denial and optional HTTPS need suitable devices/configuration. Existing Pi overnight UTC retention/midnight and controlled charging checks remain pending.

No production signing/release or self-updater is established. Background alerts, remote relay/cache, direct Pico/broker mode, backups and monitor settings writes are not included in this preview. See [README.md](README.md) and [CHANGELOG.md](CHANGELOG.md).

## Pi collector 0.7.1 — live SBMS flags/full broadcast, 2026-10-09

Source parent: `686ffb7279dcaf4b4dd1c591d52c2f627a2a9e64`. Package metadata/documentation are 0.7.1; schema 2 and npm dependencies remain unchanged.

**76 tests passed, 0 failed under Node 22**, including 23 targeted SBMS/API checks and the existing collector/MQTT/Python/logging regression suite. Syntax checks pass.

- Preserve the full accepted JSON broadcast, original names/units and optional/future fields alongside existing normalized values. Check all eight requested flags as true/false and missing/nonboolean values as null.
- Deliver flag/auxiliary-only changes within the same source-clock second; reject retained changes and complete repeats with reordered keys. Keep reconnect/stale behaviour and default silent output.
- Exercise authenticated live HTTP responses with SBMS decoder and logger: On/Off states change, stale/disconnected values disappear, existing normalized currents remain correct, and no flags/auxiliary fields or metrics enter history.
- Keep public fixtures synthetic and all owner readings, network details and system inventory private.

The owner has confirmed local Pi API status/live/history requests and working live flags in 0.7.1. Supplied history passes private interval/coverage/arithmetic/source-reference review. A short local live/history API load check completes without failed requests while collection/logging remains healthy; numerical timing/resource snapshots and actual system/network details remain private. **Remaining device checks:** LAN/TLS access, restart/recovery and unattended midnight/retention/charging checks. This is short local validation, not evidence of indefinite memory stability or client LAN performance. Android/automatic backups are not included.

## Pi collector 0.7.0 — authenticated local API, 2026-10-09

Source parent: `3976bc202f47a326be779f59b50300b81bbb08f0`. Package metadata/documentation are 0.7.0; database schema remains 2. No new npm dependencies or MQTT/collector calculation changes.

**74 tests passed, 0 failed under Node 22**, including nine API tests and actual collector/API/MQTT integration. Syntax checks pass. Public API fixtures/certificates/credentials are synthetic; TLS test certificates are generated temporarily through OpenSSL.

- Check private random-token generation, owner-only files, no overwrite, host/port/TLS validation, default/disable/conflicting collector configuration and authenticated local checks without URL tokens.
- Exercise real HTTP and HTTPS requests: all routes require header authentication, non-GET operations are rejected, no wildcard CORS/cache exposure, no credentials/database paths in status/errors, read-only SQLite enforces no writes and the collector writer continues operating.
- Check source references, selected units/exact labels, range validation/calendar cutoffs, indexed pagination without duplicate rows, missing periods/null members, valid zero PV2, partial checkpoints, environmental values/extrema and battery-member compatibility views.
- Check monotonic independent source freshness, disconnected/stale snapshot suppression, safe mapping selection, fresh configured cross-source voltage for live watts and current/SOC preservation when voltage is unavailable.
- Check global request-rate and response-size caps, generic storage failures, logging health and shutdown cleanup. Listener/header/request/socket/connection bounds are configured; their real Pi resource effect remains unmeasured.
- Run the actual collector CLI with simulated Pico and MQTT transports while issuing authenticated/denied requests and reading checkpointed history. Normal stdout stays silent and every published Pico JSON is still compared with the Python oracle; capture checks remain valid.
- Privately serve the owner's uploaded schema-2 history through the read-only API reader. Existing combined statistics and field-level source references match inspection; uploaded files are not changed or published.

The owner's main/history-backup pair passes integrity and migration-preservation review. Completed intervals remain unchanged; active intervals continue accumulating consistently, selected sensors/zero values remain present and larger electrical totals match minute sums. This verifies the supplied recording, not physical sensor calibration or long-run durability. Keep raw measurements, actual inventory and system details private.

**Remaining device checks:** enable API access on the Pi, issue LAN requests from a client, check private credentials/TLS trust if selected, restart with collection/logging, privately measure memory/latency under history queries and finish long-run midnight/retention/charging verification. No Android application, automated backup or Google Drive authorization/upload is implemented.

## Pi collector 0.6.0 — combined battery intervals, 2026-10-09

Source parent: `7b492024ad2f645acf482815b284961b83dba3f3`. Package metadata/documentation are 0.6.0; npm dependencies and MQTT acquisition/publishing remain unchanged.

**64 tests passed, 0 failed under Node 22**, including seven combined-history tests and the existing logger/MQTT/Python checks. Syntax checks also pass. All new public fixtures are synthetic.

- Assert a single physical battery row contains all selected measurements with field-level source/unit references, distinct SOC/current, valid zero PV2, independent coverage and directional/net energy; other shunts/environmental rows remain separate.
- Upgrade synthetic schema-1 databases across more than one migration batch, including committed data still in WAL. Compare every saved statistic, preserve name/checkpoint/clock metadata and verify a private schema-1 backup with SQLite integrity checks.
- Force a migration error after transfer begins; verify original rows/schema version remain intact, retry succeeds and later opens do not repeat migration or overwrite the first backup.
- Check partial updates/restarts do not overwrite other members, missing-device fields remain null, UTC retention applies to the combined row and implicit group rebinding is rejected.
- Check read-only `--battery`, existing per-metric compatibility views and simultaneous real-CLI Pico/SBMS logging with exact MQTT/Python payload parity and silent normal readings.
- Privately migrate the earlier capture-derived database and compare every original statistic against its destination. Database integrity passes; no separate battery-channel history rows remain. Actual readings/identities, raw files and system details remain excluded from publication.

Owner has inspected an initial live minute record from 0.5.0. The owner subsequently supplied live 0.6.0 history and its pre-upgrade backup; both pass integrity/preservation checks as recorded above. Live energy calibration, storage performance and long-run recovery remain pending. No network API or Android app is added.

## Pi collector 0.5.0 — SQLite history, 2026-10-09

Source parent: `6511c297dbed0bbb4fd1415e4cb7758c4d944c3b`. No npm database dependency is added; SQLite support is loaded only when history is used.

The complete **57-test suite passes under Node 22, with no failures**, including 17 logger tests. Syntax, private generator/replay and read-only inspection checks pass. New fixtures use synthetic configuration/measurements. Version identifiers are 0.5.0 in package metadata and documentation.

- Check duration-weighted instantaneous watts versus the product of averages, raw signed current and independent directional energy/coverage. Keep Pico and SBMS SOC separate, preserve valid zeros, and derive battery-side PV watts/Wh from fresh SBMS pack voltage while keeping unverified directional classifications null.
- Check independent current/voltage freshness, immediate source disconnect invalidation, no fallback voltage, no integration across outages/restarts, and invalid SOC handling.
- Split electrical integrals over UTC minute/hour/day/month boundaries, including a calendar-month transition and leap-February boundaries. Preserve parent totals after short-record pruning; verify calendar-month expiry and indefinite environmental days/electrical months.
- Check last hourly/daily pressure and all-sample outside-temperature extrema, empty-period behaviour, partial checkpoint resume, exclusive writer/read-only access, private permissions and rollback/lock release after SIGKILL.
- Verify name changes preserve series identity, physical-binding/type mismatches disable only the affected measurement, existing series cannot be silently rebound, and forward/backward clock corrections skip unsafe intervals and remain visible in inspection.
- Simultaneously receive Pico/SBMS and log through the actual collector CLI while checking silence, valid coverage, hidden ID-based secondary voltage, relevant SOC, PV2 zero and exact MQTT/Python payload agreement. The wire broker is a loopback fixture, not a production Mosquitto hardware test.
- Privately replay the reviewed capture: all 2,957 Pico packets and 300 SBMS receipt records are accepted, database integrity passes, the canonical battery-instance selection excludes its physical-shunt alias, and Pico internal voltage is not selected. PV1/PV2 use the owner-confirmed battery-voltage reference; PV2 zero power remains covered and no voltage correction is introduced. Raw data, IDs/names and system details are excluded from publication.

**Remaining Pi checks:** create the private selection, compare saved averages/energy/coverage with controlled loads and source displays, verify charge/PV signs and compare the confirmed battery-side voltage/power readings, confirm checkpoints/reboots/network/broker recovery, and assess storage growth and long unattended performance. Automated/replay checks do not prove live SD-card durability or calibration. The API/Android viewer is not implemented in this version.

## Independently reviewed private Pico/SBMS capture — 2026-10-09

The owner supplied a collector 0.4.0 local recording. Independent `verify-capture.js` execution returned **54/54 TCP length matches and CRC matches**, **2,957/2,957 UDP length matches and CRC matches**, one configuration and `ok: true`. The recording also includes 300 normalized SBMS receipt records. The configured sensor map reconstructed from raw configuration exactly matches the recorder's map.

This run verifies raw framing and concurrent acquisition evidence, not SBMS electrical calibration, per-field Python parity or restart recovery. The private sensor mapping supports selecting the secondary voltage and detecting duplicate source/virtual-battery voltage/current measurements. Extra current fields exhibit counter-like behaviour whose units/deadband/reset rules remain unverified. Cross-source voltage differences vary with load, so no fixed correction is approved. Only receipt timing is available for cross-source pairing, and a single configuration does not establish ID persistence.

The raw recording, runtime/session details, actual network/device/channel identities, readings, fitted coefficients and inventory are excluded from the repository. No runtime code, application version or API/database was changed for this investigation.

## Pi collector 0.4.0 — ElectroDacus reception, 2026-10-08

Source parent: `6eb9c5fab6f1fcf6a8f39b70c152a4d344961fcd`. MQTT dependencies are unchanged.

`npm test`: **39 passed, 0 failed** in the development environment. Existing Pico/MQTT tests remain included. CLI syntax and help checks pass. The test fixtures contain invented measurements and cell maps, not the owner's runtime sample or installation inventory.

- Verify exact-topic/config validation, all four mA-to-A conversions, unchanged signs, valid zero PV2, SOC and Pi UTC receipt time independent of the device date.
- Verify configured pack voltage, no guessed enabled-cell map, null voltage on failed selected cells and rejection of malformed, oversized, incomplete or coerced input.
- Verify retained/duplicate samples cannot refresh freshness, changed measurements within a source-clock second are accepted, malformed input cannot keep readings alive and disconnect/stale timeout clears current state.
- Exercise actual MQTT 3.1.1 subscription packets, shared Pico publishing, denied subscription/retry and broker reconnect/resubscribe. Verify stale callbacks and shutdown cleanup.
- Run the real CLI against the loopback MQTT fixture while the simulated Pico is unavailable: silent default, opt-in SBMS output, capture records and independent receive/disable controls work. Invalid config/flags fail without printing credentials.
- Run simultaneous Pico/SBMS CLI acquisition: Pico stdout and published JSON still match the original Python oracle; Pico capture verification still succeeds while ignoring the added SBMS records.

The full suite initially exposed an empty-output timeout in an older 1.5-second CLI test during concurrent startup. That test passed in isolation; its startup duration was increased to three seconds with an explicit no-output assertion. The complete suite then passed. No runtime Pico timeout or protocol change was made for this test adjustment.

The MQTT endpoint is a loopback wire fixture, not Mosquitto. New SBMS reception has not yet been tested on the real Pi. Confirm the broker account's subscription permissions, private active-cell settings, decoded values, publishing cadence, retained/repeated-message behaviour and actual broker restart. Verify each charging/load polarity and voltage association before directional integration. No database, logger, history API or Android APK is implemented in 0.4.0.

## Repository relocation — 2026-10-08

Moved the complete collector to root `node.js/` and preserved the original upstream reference under `_old/pico2signalk/`. Version remains 0.3.0. All **26 automated tests pass** from the new location; the CLI syntax check also passes. Documentation links and install/update examples use the new layout. The runtime MQTT config resolver and systemd entry point are unchanged. These are local checks, not a new Pi hardware test.

## Pico base 0.3.0 — service operation, 2026-10-08

Source parent: `3148bd0544a56621d16a32292ad01f169a6ad1b3`.

`npm test`: **26 passed, 0 failed** in the development environment. Existing Pico/MQTT tests remain included. New checks run the CLI in an isolated flat `PicoData/node.js` installation with the config in `PicoData/mqtt` and a different working directory: MQTT publishes the same parsed Python output, stdout is empty, status remains available, and shutdown completes normally. Missing default config exits with an actionable error without leaking credentials; conflicting MQTT flags are rejected. Existing CLI stdout tests now request `--stdout` explicitly.

The generic systemd unit passed `systemd-analyze verify` locally with its account and executable placeholders replaced by available development values. This is unit validation, not an actual Pi service installation. The owner subsequently reported clean shutdown/restart and successful MQTT/Pico connections with the updated service.

The owner reports successful Pico reboot/Wi-Fi recovery and correct-looking MQTT output in the existing Node-RED webpage. Some reboots recover after the normal stale/retry path. This is functional evidence supplied by the owner; no timed recovery/per-field audit, changed-IP test, actual broker-restart test or long unattended soak is recorded yet. System inventories, resource/process snapshots, account details and actual network addresses are excluded from published validation notes.

## Pico base 0.2.0 — optional MQTT addition, 2026-10-08

Source parent: `4e786e95ed70fa6b07ae67537b04dfca80915e7e`. MQTT.js is pinned to 5.16.0 with a committed npm lockfile; the MQTT wire fixture uses mqtt-packet 9.0.2. No version or APK change was made to an Android application; this remains the Node.js base.

`npm test`: **24 passed, 0 failed**, on Node.js 24.19.0 and Python 3.12.14. The existing acquisition/parser/recovery/comparison tests remain included.

New checks cover:

- Existing five-key configuration, exact topic/slashes, authentication and '=' in passwords; invalid/missing configuration is rejected.
- Legacy MQTT JSON compared with the original Python output, including raw 65535 voltage/SOC and signed temperature, accented labels and trailing spaces.
- Incomplete legacy snapshots are rejected rather than filled with invented values.
- Actual loopback MQTT 3.1.1 CONNECT/PUBLISH packets: credentials, keepalive 60, QoS 0, retain=false, exact topic and no will.
- Connection loss/reconnect, denied authentication/retry, no offline replay and stop without a retry loop.
- Bounded pending writes and delayed callbacks across reconnection.
- Full Pico CLI execution with MQTT disabled, available, and access denied. With MQTT available, received payloads equal stdout and the Python oracle; with access denied, Pico stdout/capture still work and the CLI exits normally. Diagnostics exclude credentials.

The automated MQTT endpoint is a loopback protocol fixture, not Mosquitto. Later owner-reported MQTT/Node-RED and reboot/Wi-Fi outcomes are recorded above. No collector/logging/API/UI, ElectroDacus subscription or Android APK was added. The owner's 0.1.0 Pico capture below remains the recorded real-hardware evidence.

## Pico base 0.1.0 — 2026-10-07

Source baseline: `PicoData/main` at `2846a9d74fb6bdf4d09f11e43eb9f0746d83a3da`. Acquisition reference: pico2signalk 0.0.21, now preserved in `_old/pico2signalk/`. Sensor/output reference: `python/pico-mqtt.py` (blob `eb99d3dfc57c7c9d6ca721754015a296fb086741`). Dashboard reference remains `node.red/flows.json`.

This is a standalone Node.js reference implementation in the repository root `node.js/`, not an Android application or APK.

## Completed checks

`npm test`: **16 passed, 0 failed**, using Node.js 24.19.0 and Python 3.12.14 in the development environment. Re-run after correcting support for a flat installation in `~/PicoData/node.js`.

- Golden CRC/request vectors from the upstream test.
- Six synthetic sensor/value sets compared with the actual Python sensor-list functions, reading functions and output block. They cover all handled types, battery/tank calculations, current signs, negative temperatures, pitch/roll, hidden names, trailing spaces and duplicate labels.
- Python ties-to-even rounding and signed temperature edge behaviour.
- Missing data, unavailable SOC and the voltage sentinel; deliberate differences are documented separately.
- Binary type-3 fields, truncated packets, unknown field types and unterminated strings.
- Literal labels that resemble object prototype keys.
- Fragmented TCP replies, incomplete closure, timeout and listener cleanup.
- Discovery filtering/cancellation and live sender/type filtering.
- Loopback TCP/UDP Pico simulators for both discovery and fixed-IP operation, including failed configuration, stale/reconnect recovery, stop and restart.
- Actual CLI execution against the simulator, JSON stdout, capture recording and Python comparison of the recorded samples.
- Capture verification rejects empty evidence and bad CRCs.
- An isolated copy containing only the base's files in `PicoData/node.js` verifies a synthetic capture against Python successfully, without the original `android/` or `python/` folders. The bundled Python reference has the same Git blob hash as the original baseline (`eb99d3dfc57c7c9d6ca721754015a296fb086741`).

The automated tests use synthetic packets. Loopback tests do not establish Wi-Fi broadcast delivery, firmware compatibility, changed-IP recovery on the real LAN or Android behaviour.

## Owner-reported real Pico capture — 2026-10-07

The owner ran the base from `~/PicoData/node.js` on the Pi and supplied this terminal result for `node bin/verify-capture.js pico-capture.jsonl --compare-python`:

```json
{"configurations":2,"packets":1096,"tcpResponses":108,"tcpLengthMatches":108,"tcpCrcMatches":108,"udpLengthMatches":1096,"udpCrcMatches":1096,"pythonMatches":1096,"pythonDifferences":0,"ok":true}
```

All 108 complete TCP responses and 1,096 live UDP packets passed length and CRC checks. All 1,096 decoded output objects matched the original Python calculations/formatting, with zero differences. The same capture passed without Python comparison in 1.432 seconds; Python comparison completed after a longer silent wait because the current verifier launches Python once per packet.

This is owner-reported hardware evidence; the raw capture has not been supplied to the agent for independent inspection. Two configurations alone do not establish recovery after a reboot or Wi-Fi interruption. Discovery mode and a detailed visual per-field comparison remain unconfirmed. Later functional recovery/dashboard outcomes are recorded above; runtime/device inventory is excluded from publication.

## Pending real Pi checks

- [ ] Keep any required device/runtime inventory private unless publication is explicitly requested.
- [x] Retrieve configuration and live packets from the owner's Pico (owner-reported capture result).
- [ ] Confirm automatic discovery mode on the common LAN.
- [x] Pass `verify-capture.js ... --compare-python`: 1,096 matches, zero differences.
- [ ] Verify all expected names/values against the current dashboard and Pico display, especially charge/discharge current signs, tank percentage, battery capacity and both inclinometer axes.
- [x] Owner reports successful Pico reboot and Wi-Fi disconnect/reconnect recovery.
- [ ] Test automatic discovery after an address change and an actual broker restart.
- [ ] Check UDP binding while the existing Python reader runs; restore the original service after any temporary test stop.
- [ ] Confirm Ctrl+C exits and a second start binds/connects normally.

Incoming framing/CRC interpretation was derived from upstream request layouts and passed every recorded response/packet in the owner's reported capture. This validates that capture, not every firmware or failure condition. The recorder includes raw TCP data to diagnose failures. The live receiver currently bounds-checks fields but does not enforce receive CRCs.

No real Pico, SBMS0, Pi or Android hardware test has been performed by the agent. The original 0.1.0 step built no MQTT transport; 0.2.0 adds the optional publisher described above. No APK, production broker or SignalK integration has been built.


