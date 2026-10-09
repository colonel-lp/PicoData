# Android monitoring app plan

Planning draft, 2026-10-09. This records the owner's requirements and proposals to settle before an Android implementation. No app build or collector change is included.

## Agreed foundation

- Support Android 8.1 (API 27) through current Android versions, on phones and the Joying/FYT head unit. Verify current SDK, network, file-export and installer requirements during implementation; minimum supported Android is separate from the target SDK.
- Use the validated Pi collector and authenticated live/history API as the first viewer's data source. Continuous acquisition, UTC logging and retention remain on the Pi while the Android device is off. Direct Pico/Android broker operation remains a future option.
- Preserve current source identities, units, signs, calculations, relevant-device SOC, independent source freshness and existing MQTT/Node-RED compatibility. The same physical battery's Pico/SBMS measurements must not be summed.
- The owner confirmed working live flags and a successful short local API load test with collection/logging remaining healthy. Keep raw readings, account/network/system inventory and numerical resource/timing snapshots private. LAN/TLS, long-run rollover/retention and controlled charging checks remain pending.

## Owner-requested interface and features

- Similar look and feel to the EQ & DSP app: borders enclose related buttons, labels and indicators; retain a theme button.
- Persistent bottom bar: settings, full screen, keep screen on, main monitoring screen and graph-range buttons such as Hour, Day, Week and Month. Final sizing/overflow and labels await layout agreement.
- Main monitoring screen follows the familiar Node-RED grouping, with better use of space and more polished indicators. Preserve relevant readings, including battery/load/solar values, cell values, live SBMS flags, environmental values, tanks and inclinometer where applicable.
- Tapping a relevant indicator opens a summary popup with its current and available hourly/daily/weekly/monthly information. Offer only statistics actually recorded/derivable for that measurement; flags remain live-only and unlogged values have no invented history.
- Graphs can switch between lines and bars where appropriate. Voltage and similar measurements use lines; charging/discharging and energy/charge amounts support period bars. Final chart controls and metrics await agreement.
- Settings include a data export option.
- Check GitHub releases and offer downloading/installing updates from this project; retain a changelog and stable signing identity. Exact release repository/package identity and update policy still need agreement.
- Preserve the special Joying/FYT keep-screen handling and detect compatible devices/services, with ordinary Android handling on phones.
- Investigate Simarine settings backup/restore/editing as a later feature, separate from the first monitoring/history viewer.

## Source references for reuse

Reviewed EQ & DSP source: [`colonel-lp/Joying-EQ-DSP`](https://github.com/colonel-lp/Joying-EQ-DSP/tree/e1b54f3f58efb92215d99e2e3f2941530fbcbc15), version 1.54-beta source commit `e1b54f3f58efb92215d99e2e3f2941530fbcbc15`. This is a read-only reference; that project is not modified here.

- [`MainActivity.java`](https://github.com/colonel-lp/Joying-EQ-DSP/blob/e1b54f3f58efb92215d99e2e3f2941530fbcbc15/source/app/src/main/java/com/eqdsp/controller/MainActivity.java): full-screen/window behaviour, Android screen flags/view handling and foreground wake-lock lifecycle; FYT toolkit/Main module 0, command 13, value 0 disables its automatic screen blanking. The existing implementation saves/restores a timeout with a 30-second fallback/minimum, reapplies on resume and releases Android wake locks on stop. Audit detection, lifecycle and interactions with the separate EQ app during the port; do not bring DSP writes into the monitoring app or apply deprecated head-unit workarounds indiscriminately to phones.
- [`AppUpdateManager.java`](https://github.com/colonel-lp/Joying-EQ-DSP/blob/e1b54f3f58efb92215d99e2e3f2941530fbcbc15/source/app/src/main/java/com/eqdsp/controller/AppUpdateManager.java) and its update helpers: GitHub release selection, changelog, package/version/signature validation and Android installer handoff. Adapt repository/package references for PicoData and verify source-install permission handling across supported Android versions.
- Theme configuration/store/appearance classes in the same source tree provide design references. Agree the monitoring app's colour roles, themes and responsive layout before carrying code across.
- [`node.red/flows.json`](../node.red/flows.json): existing dashboard data/indicator mappings. Reviewed groups include battery/voltage, temperature, load draw, SBMS, tank/inclinometer, cell values/delta, flags and graphs. Trace live/disabled paths and conversions rather than duplicating every legacy group or derived value automatically.

## Suggested additions, not yet approved

- Always-visible Pi/API and independent Pico/SBMS connection/freshness status, reconnect handling and a clear unavailable state. Optional cached readings/history must be labelled with acquisition time and offline/stale state.
- Phone portrait/landscape reflow and head-unit landscape layout with usable touch targets; keep bottom controls reachable without stretching the dashboard or hiding readings.
- Choose the primary battery source for dashboard values, with explicit comparison/source labels for secondary Pico measurements. Preserve both devices' SOC/current in history and avoid counting overlapping channels twice.
- Graph cursor/tap inspection, previous/next range navigation, selected channels, source/units and time labels. Show missing periods as gaps and partial/incomplete buckets with coverage, rather than as zero data.
- Use watts (W) for instantaneous/average power; watt-hours (Wh) or amp-hours (Ah) for amounts per bar period. Show charging/discharging separately where polarity is verified. Main-battery directional bars describe net battery flow and cannot reconstruct simultaneous gross charging and loads. Unverified channel polarity remains explicit.
- Define whether Hour/Day/Week/Month means rolling windows or named calendar periods. Use UTC storage/buckets and optional local clock/BST display; relabelling a UTC daily bucket does not turn it into a local-midnight bucket. Respect existing minute/hour/day/month retention and derive summaries from available coverage without claiming unavailable precision.
- Export selected source/measurement, date range and resolution to CSV with UTC interval timestamps, units, coverage and source references; optional JSON or graph images can be agreed later. Export saved summaries, not nonexistent raw historical samples, and exclude credentials.
- Human-readable SBMS state explanations and optional configurable threshold alerts. Agree whether notifications are foreground-only or must operate when the app is backgrounded/closed; the Pi remains the continuous logger.
- Persist theme/layout/units/connection settings, store API credentials privately, and support manual Pi address as well as agreed discovery. No public-router access is needed for the first local viewer.

## Simarine configuration investigation

The official [PICO manual, section 10](https://simarine.net/wp-content/uploads/2023/07/Simarine-Pico-Manual-8.3.pdf) documents app settings save/restore and compatibility with unchanged physical module/shunt configuration. This establishes vendor-app capability, not a verified backup/write implementation in this project.

The repository's vendor app was identified as .NET MAUI/Mono. JADX Java wrappers may omit the managed protocol code. Future investigation should locate the actual protocol implementation and distinguish the collector's read configuration from a complete restorable backup, then verify backup/compare before any explicitly authorized settings writes. Do not promise backup compatibility, firmware support or a working write path before evidence.

## Before implementation

- Finalise layout/wireframes, primary source, indicator/popup fields, phone orientation, graph ranges/units and theme scope.
- Agree foreground/background/notification behaviour, connection setup, app/package name, signing/release location and export formats.
- Validate app LAN connectivity and Android-version requirements; build a small live-data viewer first, then summary popups, history/charts, export and update handling in agreed stages.
- Continue overnight UTC rollover/retention checks alongside planning. Record compilation, synthetic tests and actual phone/head-unit tests separately.
