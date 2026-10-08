# Ella Monitoring project instructions

These instructions apply throughout `colonel-lp/PicoData`, branch `main`. Read this file before working on the project.

## Communication and scope

- Keep answers concise and specific. Follow the owner's latest instructions and corrections.
- Ask when the meaning or scope is unclear. Do not assume target Android versions, devices, screen layouts or background requirements from the Joying project.
- Preserve all existing functionality, output fields, calculations, units, names and signs unless the owner explicitly requests a change. A shorter rewrite is not evidence of functional equivalence.
- Record feature requests, fixes and proposed application changes in `android/NEXT-BUILD-CHANGES.md` once development starts. Keep corrections and exclusions current.
- Only change application source when the owner explicitly requests implementation or a build. Questions, investigations and planning requests authorize investigation/documentation, not an app build.
- Explicitly requested documentation changes may proceed independently. Do not increment versions, build APKs or publish releases during planning alone.
- Complete authorized work without repeatedly asking for the same permission. Do not expand its scope.

## Repository map and evidence

- Main project and future Android work: `colonel-lp/PicoData/main`; application and related documentation belong under `android/`, apart from root project guidance.
- Current Pi baseline: `python/pico-mqtt.py`, with CRC helper `python/brainsmoke.py`.
- Earlier reference code: `_old/`. Do not treat these variants or `python/scratch` as the running baseline.
- Updated connectivity reference: `node.js/`, copied from `colonel-lp/pico2signalk/master` (0.0.21). Use the in-repository copy for this project and compare the separate fork when reviewing upstream updates.
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
- The Python baseline adds type-13 inclinometer support (pitch/roll), tank remaining capacity and percentage, named battery/voltage output, and structured JSON sections. The reviewed Node.js sensor list/live decoder omit type-13 support; it must be retained.
- Python reports Celsius, pressure divided by 100, percentage SOC, and current polarity opposite to the reviewed Node.js decoder. Node.js uses Kelvin, undivided pressure and fractional SOC. Battery remaining-capacity and tank-volume calculations also differ. Document and compare these against actual readings; do not silently substitute SignalK conventions.
- Preserve names exactly, including spaces: the dashboard accesses `battery["Ella  "]` with two trailing spaces. Keep stable sensor IDs separate from display labels; handle duplicate labels explicitly.
- Python's output includes `time`, `barometer`, `inclinometer`, `voltage`, `current`, `temperature`, `tank` and `battery`; battery voltage is also copied into `voltage`. Sensors with `[` in their names are excluded by the current output filter. Do not remove these behaviours inadvertently.
- Node-RED topics are `/Ella/Pico/` and `/Ella/sbms`. Its SBMS mappings include `soc`, `cellsMV`, `tempInt`, `tempExt`, `currentMA` and `flags`; Node-RED adds conversions, sums, rounding and sign changes. Trace complete paths to the displayed values, including disabled nodes, before reproducing them.
- Capture real Pico configuration/packets and SBMS JSON to establish the full data contract. Dashboard mappings alone do not define every SBMS field or missing-value rule.
- The reviewed Python MQTT setup has no network-loop/reconnect management. The newer Node.js configuration retries do not establish complete live-stream recovery: TCP reads finish on the first data chunk, discovery accepts the first sender, and plugin stop does not close the live socket. Do not carry these limitations forward as approved Android behaviour.

## Android design and validation

- Goal: direct Pico Wi-Fi readings plus ElectroDacus MQTT display. Keep Pico and MQTT connection state independent.
- Evaluate an embedded MQTT broker early because it is the owner's preferred route. An MQTT client alone cannot accept the ElectroDacus publisher. Confirm that its firmware can target the Android device's reachable Wi-Fi address/port and credentials.
- Retain external-broker support using the Pi. Do not change the working ElectroDacus destination during investigation or silently select a permanent architecture.
- Separate transport/discovery, binary decoding, sensor metadata/unit conversion, shared data state and UI. Keep broker hosting separate from MQTT client operation so both can use the same SBMS decoder.
- Confirm target hardware, Android range, required readings, orientation, history and screen-off/background behaviour before selecting the SDKs, libraries and layout.
- Consult current official Android/network/library documentation when needed. Account for Wi-Fi routing, UDP reception, applicable local-network permissions, foreground-service rules and power management. Do not lower SDK targets merely to bypass restrictions.
- Validate sender identity, packet bounds, unknown fields, invalid/sentinel values and TCP fragmentation. Missing or stale readings must not appear as fresh zero values.
- Recover from Pico reboot, Wi-Fi loss, changed IPs, MQTT disconnect and app lifecycle changes. Close sockets, listeners, retry jobs and any Wi-Fi locks on shutdown.
- Use replay fixtures to compare the port with the existing Python and Node-RED outputs. Test negative values, pitch/roll, battery/tank calculations and malformed/truncated messages.
- Distinguish source inspection, replay/syntax tests, Android compilation and hardware verification. Never claim a working connection, APK or device test without evidence.

## Documentation and delivery

- Keep the process outline, pending changes, functionality notes, user guidance and build validation under `android/`; create those development records when they become needed.
- For authorized builds, read the agreed pending list first, keep version identifiers consistent and update the complete changelog. Respect explicit instructions to retain a version for a correction.
- Preserve signing identity once established. Never commit credentials, signing keys, local SDK paths or generated build output.
- Preserve upstream attribution and applicable licence notices when reusing code. The fork has an MIT LICENSE but `package.json` says UNLICENSED; resolve the discrepancy before distributing reused code. Vendor APK/decompiled reference is not blanket permission to copy implementation or assets.
- Report changed files, validation and remaining device checks concisely. Do not generate source ZIPs unless requested.
- The owner authorizes publishing functional project/test results but excludes system details. Do not publish runtime/process/resource snapshots, account details, actual device/network addresses or private inventory. Use generic account/path placeholders in service templates; provide owner-specific commands only in chat.
