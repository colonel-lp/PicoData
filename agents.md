# Ella Monitoring project instructions

These instructions apply throughout `colonel-lp/PicoData`, branch `main`. Read this file before working on the project.

## Communication and scope

- Keep answers short and focused.
- Reference the latest JavaScript and Android documentation when needed. Prefer official documentation and distinguish confirmed facts from assumptions.
- Follow requests carefully. Ask if the meaning or scope is unclear; do not assume something that was not said.
- Always add listed feature requests, bug fixes and proposed code changes to `android/NEXT-BUILD-CHANGES.md` for the next build.
- Only change application source code when the owner explicitly asks to prepare the next source version or create a build from the current list of changes. A question, investigation, feature request or request to fix an issue alone is not authorization to implement application changes.
- The owner builds the APK in Android Studio. By default, requests to build mean prepare, validate as appropriate, commit and push the agreed source changes; do not assemble, sign, deliver or publish an APK unless the owner explicitly requests it. Report source checks separately from compilation and device verification.
- Before an authorized build, read the current pending list and the owner's latest corrections and exclusions.
- Read-only investigations and updates to the pending list may proceed without a build request. Explicitly requested documentation or instruction-file changes may also proceed; they do not authorize application changes.
- Do not implement unrelated changes, increment the app version, or publish a build while only recording or investigating requests.

## Repository workflow

- Application source, Pi collector and project documents: `colonel-lp/PicoData`, branch `main`. Android project: `android/source/`. Android documents: `android/`. Pi collector and operational documents: `node.js/`.
- The owner selects PicoData for Android updates; source remains android/source and APK update checks use PicoData releases. Retain the existing preview package/signing key. Do not introduce a separate production identity or EQ private-source/public-release split without a request.
- Use the latest GitHub source. Verify the branch head before editing and before pushing; do not rely on an old workspace or overwrite newer owner changes.
- Use the connected GitHub tools when available. When a build is authorized, commit and push the agreed changes and verify the resulting GitHub files.
- For each authorized new Android source version, configure Android Studio/Gradle to produce the APK named exactly `Ella-monitoring-vX.XX.apk` (case-sensitive, two decimal places) and advance that version by exactly **0.01 per build**. Keep Android versionName consistent with the filename version, advance the integer versionCode beyond the previous build, and update the README changelog. Do not reuse a previous build's version or add preview/build suffixes to the APK filename. Configure the actual build output filename rather than only renaming an APK after compilation. Apply this rule on future authorized builds; recording it alone does not authorize rebuilding or changing the current app version. For an authorized Pi collector release, update its package version and corresponding documentation; keep collector and Android versions separate.
- Maintain the complete Android changelog in `android/CHANGELOG.md`; when an in-app changelog and production release repository are established, publish identical complete notes there. Preserve existing entries. Release titles or empty descriptions must not replace full notes. Keep Pi collector changes in `node.js/CHANGELOG.md`.
- When Android updates are implemented, keep Download & Install as the update action, with the agreed installer and confirmed-install cleanup behavior.
- Update the build validation file: `android/BUILD-VALIDATION.md`.
- Do not modify related repositories or relocate their contents without a request.

## Documentation

- Where changes are made in core app functionality, keep files in `android/functionality/` updated with the latest changes, or create a new file where needed. Update each file with the build version where changes were made. Do not create files with the build number in the name.
- Where changes affect how to use the Android app, keep `android/README.md` up to date. For Pi collector changes, update the relevant guides under `node.js/`.
- Do not generate source ZIPs unless requested.

## Implementation and validation

- When the owner asks to carry functionality/code over from Joying-EQ-DSP, use the working implementation as the starting point. Preserve its relevant behavior, UI and interaction flow; remove only irrelevant EQ/DSP-specific code and adapt monitoring-specific names, state, views and bindings. Do not replace it with a different implementation or change what it does without an explicit instruction. Inspect the current EQ code and verify equivalent observable behavior. For the reported colour-picker live-preview/return issues after 0.23, the owner explicitly allows the best suitable solution, guided by the working EQ implementation.
- Preserve agreed dashboard layouts, indicator appearance, themes, data calculations, source bindings and state behavior unless changes are included in the authorized list.
- Processing edits must work consistently with live display, independent source freshness, history, summaries, graph ranges, saved labels and exports. Preserve existing MQTT/Node-RED behavior and the agreed logging schema/retention; do not change source signs, units or calculations without an authorized request.
- Account for Android 8.1 through current Android versions and the declared minimum SDK. Guard newer APIs, retain supported legacy paths and detected FYT screen handling. Do not change SDK targets merely to silence warnings.
- Run checks appropriate to the actual changes. Distinguish source tests, stand-ins and syntax checks from Android compilation and real phone/head-unit/Pi verification.
- Never claim an APK was built, installed or hardware-tested unless it actually was. If SDK/Gradle is unavailable, report that limitation and identify the source version delivered.
- Preserve the existing signing key for update compatibility. Do not commit signing keys, credentials, local SDK paths or generated build output.
- Preserve upstream attribution and applicable licence notices when reusing code. The archived fork has an MIT LICENSE but `package.json` says UNLICENSED; resolve the discrepancy before distributing reused code. Vendor APK/decompiled reference is not blanket permission to copy implementation or assets.
- The owner authorizes publishing functional project/test results but excludes system details. Do not publish runtime/process/resource snapshots, account details, actual device/network addresses, private inventory, captures or measurements. Use generic account/path placeholders in service templates; provide owner-specific commands only in chat.

## Repository map and evidence

- Main project and future Android work: `colonel-lp/PicoData/main`; Android application and related documentation belong under `android/`; the active Pi collector and its operational documentation belong under root `node.js/`, with root project guidance alongside.
- Current tested Pi collector: `node.js/bin/pico.js` (0.7.1); use `node.js/README.md` for install/update/service commands. Default MQTT config is the parent `PicoData/mqtt`. It publishes unchanged Pico JSON and receives SBMS on the same broker connection. Optional history uses Node 22.13+ built-in SQLite and persistent `PicoData/history/`; preserve the battery-instance-only selection and never automatically log its physical-shunt alias. Normal readings are silent; `--stdout` shows Pico and `--sbms-stdout` shows decoded SBMS.
- Android viewer starts at `android/source/app/src/main/java/com/colonellp/ellamonitor/MainActivity.java` (0.23/build 5); use `android/README.md` for setup/build and explicit preview defaults. Minimum API 27, compile/target API 37. The preview uses native API/UI/plotting with no third-party runtime library, a separate development package, foreground-only polling and private encrypted credentials. Preserve source freshness, IDs/units/signs, per-metric history compatibility and coverage; never silently add background alerts or claim hardware tests. Preserve the retained signing key and PicoData update feed; production identity changes require a request.
- Python behaviour baseline: `python/pico-mqtt.py`, with CRC helper `python/brainsmoke.py`; the collector bundles an unchanged comparison snapshot under `node.js/test/reference/`.
- Earlier reference code: `_old/`. Do not treat these variants or `python/scratch` as the running baseline.
- Upstream connectivity reference: `_old/pico2signalk/`, copied from `colonel-lp/pico2signalk/master` (0.0.21). This archived SignalK implementation is separate from the active `node.js/` collector. Compare it and the separate fork when reviewing upstream updates.
- Existing dashboard: [`node.red/flows.json`](node.red/flows.json) in this repository. Use this copy as the project reference, with `node.red/package.json` for project metadata.
- Vendor reference: `simarine/simarine.apk` and `simarine/simarine jadx/`. The inspected manifest and activity identify a .NET MAUI/Mono app; JADX Java wrappers may not expose managed protocol logic. State this limitation rather than claiming a complete decompilation.
- Use the current GitHub source. Verify branch heads before editing and again before pushing. Preserve concurrent owner changes; never force-push over them.
- Commit requested project documents to `PicoData/main`. Do not modify the related repositories or relocate their contents without a request.

Initial review on 2026-10-07 used:

- PicoData: `cee91e7f08bed1a8151a1a778aeafe1f4a31b14d`.
- pico2signalk: `49beac3c42110c0bf82dafa97102852f258d68e7` (package version 0.0.21).
- Node-RED flow blob: `c46d025991b3ba25aee052a65faf78a5ce0e7813`; the same reviewed flow is now available at [`node.red/flows.json`](node.red/flows.json) (copy verified in PicoData commit `3ef9d741da44ecbfd55fb6075440167294a76489`).

These identify the reviewed baseline, not permanently pinned development versions.

## Behaviour to preserve and reconcile

- Pico discovery/live readings use UDP 43210; configuration uses TCP 5001. Retrieve configuration before decoding values by sensor position.
- Reuse the newer discovery, retry, timeout and configuration ideas, with the owner's Python changes added. Port the required behaviour into Android; do not assume the app must run Node.js.
- Exclude SignalK plugin registration, instance/path routing, metadata and delta publication from the Android implementation. Keep protocol decoding and required sensor metadata.
- The Python baseline adds type-13 inclinometer support (pitch/roll), tank remaining capacity and percentage, named battery/voltage output, and structured JSON sections. The archived upstream Node.js sensor list/live decoder omit type-13 support; the active collector restores it and it must be retained.
- Python reports Celsius, pressure divided by 100, percentage SOC, and current polarity opposite to the reviewed Node.js decoder. The archived upstream Node.js code uses Kelvin, undivided pressure and fractional SOC; the active collector preserves Python conversions. Battery remaining-capacity and tank-volume calculations also differ. Document and compare these against actual readings; do not silently substitute SignalK conventions.
- Preserve names exactly, including spaces: the dashboard accesses `battery["Ella  "]` with two trailing spaces. Keep stable sensor IDs separate from display labels; handle duplicate labels explicitly.
- Python's output includes `time`, `barometer`, `inclinometer`, `voltage`, `current`, `temperature`, `tank` and `battery`; battery voltage is also copied into `voltage`. Sensors with `[` in their names are excluded by the current output filter. Do not remove these behaviours inadvertently.
- Node-RED topics are `/Ella/Pico/` and `/Ella/sbms`. Its SBMS mappings include `soc`, `cellsMV`, `tempInt`, `tempExt`, `currentMA` and `flags`; Node-RED adds conversions, sums, rounding and sign changes. Trace complete paths to the displayed values, including disabled nodes, before reproducing them.
- Capture real Pico configuration/packets and SBMS JSON to establish the full data contract. Dashboard mappings alone do not define every SBMS field or missing-value rule.
- SBMS reception is implemented in `node.js/lib/sbms.js`. Collector 0.7.1 exposes the complete broadcast and normalized live boolean flags through the API; missing/invalid flags are null and stale states unavailable. Flags/auxiliary fields are not added to history. Exact-repeat detection includes the full broadcast and ignores object-key ordering. Preserve Pi UTC receipt time, all four mA-to-A conversions, separate source time, explicit private active-cell mapping and null voltage on unavailable selected cells. Ignore retained snapshots and exact repeated samples; clear current state on staleness/disconnection. Do not publish the owner's cell map or fixtures copied from actual runtime measurements.
- SQLite logging introduced in 0.5.0 now stores one combined battery record per interval in 0.6.0, with distinct source-referenced fields and an atomic backed-up history migration. Other shunts/environmental records remain separate. Use `history.js --battery`; do not regenerate/delete existing history on update. Configuration is the private parent `PicoData/logging.json`; see `node.js/LOGGING.md` for selected metrics, signs, coverage and retention. The optional authenticated read-only API is implemented in 0.7.0; use `node.js/API.md` for parent `api.json`, live freshness, bounded history, HTTP/HTTPS and independent credentials. No API opens without configuration; preserve SQLite/MQTT behaviour and keep tokens/TLS files private. The Android API viewer is now in android/source/; backup destination/implementation remains a later stage. Follow `node.js/DATABASE-PLAN.md`; do not silently add unselected monitor fields to history or infer current polarity from zero readings.
- The reviewed Python MQTT setup has no network-loop/reconnect management. The archived upstream Node.js configuration retries do not establish complete live-stream recovery: TCP reads finish on the first data chunk, discovery accepts the first sender, and plugin stop does not close the live socket. Do not carry these limitations forward as approved Android behaviour.

## Android design and validation

- Goal: direct Pico Wi-Fi readings plus ElectroDacus MQTT display. Keep Pico and MQTT connection state independent.
- Evaluate an embedded MQTT broker early because it is the owner's preferred route. An MQTT client alone cannot accept the ElectroDacus publisher. Confirm that its firmware can target the Android device's reachable Wi-Fi address/port and credentials.
- Retain external-broker support using the Pi. Do not change the working ElectroDacus destination during investigation or silently select a permanent architecture.
- Separate transport/discovery, binary decoding, sensor metadata/unit conversion, shared data state and UI. Keep broker hosting separate from MQTT client operation so both can use the same SBMS decoder.
- The owner confirms phones plus Joying/FYT head units, supporting Android 8.1 (API 27) through current Android versions. The first viewer uses the validated Pi API while continuous logging stays on the Pi; direct Pico/embedded-broker mode remains future investigation. Follow `android/APP-PLAN.md` for requested grouped EQ-style UI, Node-RED reference, bottom controls, summary popups, graphs, export and updates. Preserve the owner's supplied dashboard arrangement, including both monitor comparisons; do not publish the private reference photo or its readings/inventory. Graph ranges are rolling 6/12/24 hours, Week and Month with a Rolling / Defined toggle. Monitoring labels/group headings must be renameable through saved display aliases, without changing source bindings, history, units/calculations or monitor settings. Keep the visual style consistent with the EQ app; manufacturer interfaces are inspiration, not permission to replace the requested layout or copy branded assets. If alerts are included, background operation is required; Alert rule/delivery implementation remains to settle. The owner authorized a first preview with documented orientation/reflow, source selection, graph ranges and foreground screen defaults; review those on devices before production. Establish production release identity/signing before regular updates. Preserve the separate EQ app's FYT keep-screen capability through detected compatible services and foreground lifecycle; do not port DSP writes or apply head-unit workarounds to phones.
- Consult current official Android/network/library documentation when needed. Account for Wi-Fi routing, UDP reception, applicable local-network permissions, foreground-service rules and power management. Do not lower SDK targets merely to bypass restrictions.
- Validate sender identity, packet bounds, unknown fields, invalid/sentinel values and TCP fragmentation. Missing or stale readings must not appear as fresh zero values.
- Recover from Pico reboot, Wi-Fi loss, changed IPs, MQTT disconnect and app lifecycle changes. Close sockets, listeners, retry jobs and any Wi-Fi locks on shutdown.
- Use replay fixtures to compare the port with the existing Python and Node-RED outputs. Test negative values, pitch/roll, battery/tank calculations and malformed/truncated messages.
- Distinguish source inspection, replay/syntax tests, Android compilation and hardware verification. Never claim a working connection, APK or device test without evidence.


