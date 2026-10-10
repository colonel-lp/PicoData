## Next source version after 0.21 — 2026-10-10

- [ ] Persistent notification: show one app-authored line saying **Ella Monitoring**, with no body/subtitle text. Remove the existing tap-to-return text; preserve the notification's tap action and ongoing behavior.
- [ ] Configure the actual Android Studio/Gradle APK output filename as **Ella-monitoring-vX.XX.apk**, using the versionName and advancing by 0.01 per authorized version. The current project still outputs `app-debug.apk`; renaming only the delivered APK did not satisfy this requirement. The next authorized version after 0.21 is 0.22.
- The owner will compile the APK in Android Studio. Prepare and push source changes on an explicit implementation request; do not assemble, sign, deliver or publish an APK unless explicitly requested. Record validation accurately and distinguish source checks from Android compilation/device tests.

These requests are recorded for the next authorized source update. No application changes or version increment are made by recording them.

### Dashboard and theme corrections — 2026-10-10

These owner corrections supersede conflicting earlier checked-off items. They are pending for the next authorized source update; the owner compiles the APK.

- [ ] Restore the **Ella Monitoring** dashboard header directly above the indicator box, as in the supplied Node-RED reference. This is a dashboard group title, distinct from the removed app-wide title. Source inspection: the current flags group has an empty title.
- [ ] Add a **Gauge borders** colour picker to the theme editor and apply it to gauge borders; retain a separate role from other panel/button outlines.
- [ ] Centre the lower voltage, amp-hours and time-remaining values within their cells.
- [ ] Change the text padding before and after the values in the cell-voltage and delta-voltage boxes from 6px to **20px**. Retain the global 6px gaps between elements.
- [ ] Remove the user-facing **LIVE** entry identified in the themes list and add a **System default** option. Check the existing theme/preset menu placement rather than silently relabelling an unrelated setting.
- [ ] Reformat the Settings block currently titled **SYSTEM** to match the EQ app's bottom-right block. Remove the SYSTEM heading, arrange controls in two rows with space reserved for a future button, and put the app version at the bottom. Match EQ button proportions and spacing. Retain Changelog/Update and Back.
- [ ] Remove the separate **Check for updates** text and include **Updates:** within the update-frequency control. Retain Off / 1hr / 3hr / 6hr / 12hr / 24hr choices.
- [ ] Keep the bottom bar and its buttons at the same height when fullscreen is toggled, matching the requested EQ style. Fullscreen should expand the available content without changing bottom-control heights. Source inspection: MainActivity uses 44/54 logical pixels for the bar; BottomBar uses 26/40 for its controls, and DesignViewport changes its logical base height. Account for all three when fixing the visible height.
- [ ] Add an option in each gauge's popup to highlight its background colour, with a corresponding colour picker in Themes.
- [ ] The bottom preset and theme boxes must say **Preset** and **Theme** and show a changed-state dot like the EQ app. Replace the current asterisk/whole-label colour treatment with the reference dot behavior.
- [ ] Rename **Current Draw** to **Currents:**.
- [ ] Give **Currents:**, **Temps:** and **Ella Monitoring** title boxes themed outlines and backgrounds. Add theme colour roles named **Title outline** and **Title background**.
- [ ] Both OK and Cancel in a colour picker must return to the theme colour-editor box. Fix the reported unwanted screen dimming; inspect nested-dialog/window dim behavior and match the EQ app.
- [ ] Remove the top explanatory/title text from the rename-display-label popup and resize it to fit. Match comparable EQ popup dimensions and button sizes; the current buttons are too large.

Voltage clarification: the owner confirms that the voltage delivered with the other Pico battery values is the intended source. Preserve the current V[P] raw Pico battery-voltage binding; no voltage-source change is requested.

### Further dashboard and preset clarification — 2026-10-10

- [ ] Put the complete gauge array in its own bounding box, using global 6px padding between elements and around the group.
- [ ] The preset changed indicator must show that current settings differ from the currently saved preset. Include display-label changes and other settings belonging to the preset in this comparison. Unsaved differences trigger the dot/highlight; saving or restoring matching values clears it. Keep theme-specific modification indication consistent with the saved theme.
- [ ] When adding the **Ella Monitoring** header above the indicators, make space by reducing only the height of the gauges. Preserve their widths and the dimensions of the remaining dashboard elements.
- [ ] Increase text size in the **amp-hours** and **time remaining** boxes to use their available space. Preserve centred alignment and fit longer valid values without clipping.
- The requested centring of voltage, amp-hours and time remaining, and **20px** text padding in cell-voltage/delta-voltage boxes, remain pending as listed above. Only the accidental unfinished removal phrase was discarded.

### Separate gauge background theme roles — 2026-10-10

- [ ] Add two independently selectable background colour roles to the theme editor: **Gauge panel 1** for the eight central gauges, and **Gauge panel 2** for the barometer, LPG, water, pitch and roll gauges. Apply each role to the individual gauge panels in its respective group. Keep these two group background colours separate from general panels, gauge borders and the optional background-highlight colour requested above; save/load them with the theme.

## Completed authorized build 0.21 — 2026-10-10

- Use Pico's reported nominal and remaining capacity with the verified Node-RED time-to-full/empty chain and raw battery current. Zero/missing/stale inputs are unavailable.
- Remove the blank gaps from the reference current list; distribute all rows evenly with 6px global element padding.
- Completed the pending dashboard, theme, Settings, changelog and update corrections below. Updates use PicoData releases; source stays in android/source. Retain the installed preview package and signing key.

# Next build changes

## Owner device-review corrections — implemented in 0.21, 2026-10-10

### Runtime source correction and owner preference — 2026-10-10

- Collector formula review: with `C` as decoded nominal Ah and `f = SOC/100`, its calculation is approximately **`3600 × C / (currentA × f + 0.001)` seconds**. Discharge current is negative, so normal discharging selects the fixed seven-day substitute. It also puts SOC in the denominator instead of using remaining capacity in the numerator, and does not use the missing capacity for charging. This is not a correct time-to-empty/time-to-full estimate.
- The owner's Node-RED arithmetic is the better fit: discharge uses `remainingAh / abs(currentA)` hours; charge uses `(fullCapacityAh − remainingAh) / currentA` hours and the flow prepends the established negative sign. It assumes the present net current continues; it is not necessarily identical to Simarine's own estimate.
- Recommendation for a future authorized implementation: keep the Node-RED calculation/formatting but use the battery's valid decoded nominal Ah for full capacity rather than a fixed installation capacity. Treat zero/missing/stale current as unavailable; do not keep the old fabricated seven-day result. This recommendation does not authorize altering collector compatibility or application code now.
- [x] **Prefer Simarine/Pico's own displayed time estimate if it is available in the received protocol data**, as requested by the owner. Investigate the actual packet/configuration field and its units, sign, unavailable sentinels and freshness before using it. The native estimate has not yet been identified in the existing decoder; the Node-RED calculation below is the confirmed alternative, not evidence that Pico transmits its own estimate.
- [x] Correct the misleading **Runtime · Pico estimate** description. The currently consumed `capacity.timeRemaining` is computed in `node.js/lib/readings.js`, inherited from `python/pico-mqtt.py` and the archived upstream code; it is **not a decoded native time value**. It explicitly changes negative calculated results to **604800 seconds (seven days)**, which the Android formatter renders as `7d:00h`. Do not present that fabricated fallback as Simarine's estimate.
- [x] Remove that seven-day substitute from the app's runtime source selection. Preserve the collector's existing MQTT compatibility contract during the investigation; any collector/protocol change must be explicitly included in an authorized build/release. If a valid native estimate cannot be obtained, use the verified owner Node-RED calculation below with correct unavailable handling.
- The owner reports that Simarine displays an estimate while the app remains at seven days. Source inspection explains the app's fallback; agreement with Simarine's displayed estimate has not been tested on hardware.


These corrections supersede conflicting earlier implementation claims and exclusions. The owner has supplied photos comparing the actual app with the Node-RED display. The previous checked-off layout/theme items do not establish acceptance. This records the next-build requirements; no new build is authorized by this review.

### Dashboard elements, layout and readings

- [x] Each displayed element is a button: apply the EQ app's actual themed button background, text and highlight/on/off borders to individual readouts and gauges, rather than plain text rows or generic panel borders. Preserve the lock behavior for popup interactions.
- [x] Match the reference photo's arrangement and temperature ordering using stable source bindings; do not use API arrival order or alphabetical order. Preserve the reference's intended grouping/spacing, without publishing the private photo, inventory or readings.
- [x] Remove the excessive unused space below gauge faces; fit the dashboard elements and lower readouts to the available screen while retaining the requested layout and 6px spacing.
- [x] Pitch and roll: fixed **−5 to +5** scale, zero in the centre, a needle, and colour **only between zero and the needle**. At zero there is no coloured sweep. Keep the actual numeric reading even when it exceeds the visible scale; do not substitute the current generic gauge appearance.
- [x] Correct days/time remaining using the now-supplied owner export **flows(1).json** (2026-10-10), following **Pico → Delay → Total → Divide → Battery [Pico] and Set message topic → Join 2 → Set message payload → Divide → Split into time → Time to Full 2**; and **Pico → Delay → SOC AH → Nearest integer → SOC Ah and Set message topic → the same Join 2**. This source requirement is now resolved; do not use raw Pico `capacity.timeRemaining`.
  - Total supplies raw signed Pico total/battery current. The first Divide node divides by 1 and rounds to one decimal, but its output field is **topic**, so **payload remains the raw current**; the next Change node then assigns topic `batteryPico`. Do not mistakenly use the rounded topic value as the calculation input.
  - SOC AH supplies battery remaining capacity in Ah, rounded with nearest-integer/JavaScript Math.round behavior, and is assigned topic `SOC`. Join 2 builds a payload object keyed by topic and emits after two messages.
  - The payload expression selects `[fullCapacityAh − roundedRemainingAh, currentA]` when current is positive; otherwise `[roundedRemainingAh, currentA]`. Use the configured full-capacity constant from the owner's private export; preserve it without publishing the installation's capacity/inventory.
  - The second Divide reduces that array and the appended constant **−1**, then rounds to **two decimal places**: `hours = −selectedAh / currentA`. Negative current therefore gives positive discharge runtime; positive current gives a negative charge-to-full time. Retain that sign behavior instead of silently taking an absolute value for the calculation.
  - Split into time takes the absolute rounded hours for formatting and prepends `-` for negative hours. At 24 hours or more, display `Dd:HHh`; below 24 hours, display `Hh:MMm`. Days, leftover hours and minutes are **floored**, and HH/MM are zero-padded. Do not convert the input from seconds or always format days/hours.
  - Time to Full 2 centres the resulting string vertically/horizontally and uses bold text. Apply the app's current theme/button styling while preserving the calculation/formatting.
  - The calculator rejects a zero denominator and emits no updated result; it does not return zero runtime. The Android display must treat zero-current, missing or stale inputs as an unavailable estimate rather than a fresh fabricated result. Preserve the independently fresh Ah/current readings.
- [x] **Remove the app title altogether**, including the title currently above the indicators. This supersedes the earlier optional hide-title feature; do not add another Ella Monitoring heading.
- [x] Put cell voltages in **their own bounding box**, separate from the other battery summary readouts.
- [x] Show both **V[P]** and **V[S]**, as live readings: V[P] is the actual raw Pico Ella battery voltage; V[S] is the actual raw Pico starter-battery voltage. The reviewed Node-RED change nodes bind these to their respective battery voltage fields. The current secondary logged voltage selected for V[P] is not a verified equivalent; do not replace either raw live source with SBMS voltage or a derived/reference voltage.
- [x] Ensure **6px padding above the buttons inside the bottom panels**, alongside the agreed reference spacing.
- [x] Full screen must let the app **expand into the area freed by hiding Android system bars**, rather than leaving the old inset/empty space. Recalculate the usable viewport and scale on both entry and exit, preserving the reference layout.

### EQ theme engine, menus and every popup

- [x] Replace the approximate theme/menu/dialog implementation with the actual EQ app's behavior and styling, adapted only for monitoring-specific elements. Inspect its current DashboardView, MainActivity menu/dialog helpers, ThemeAppearanceView, ThemeConfig and ThemeStore; reuse the established interaction/formatting rather than generic Android menus recoloured afterward.
- [x] Provide the **theme dropdown on the main page**, with the EQ options: Save theme as, conditional Update for a modified named theme, Manage themes, Edit theme, Default and all saved themes. Match the EQ anchored dropdown, selection/modified indication and popup behavior. Restore the reference's relevant missing main-page preset options as well.
- [x] Restore saved themes and relevant options throughout the theme workflows; manage/load/save/update/rename/delete must work as in EQ, with its colour picker and theme roles.
- [x] **Every popup/dialog must use the current app theme and look like the EQ app's popups**, including its panels, borders, text, controls, sizing, spacing and selected states. This explicitly supersedes the previous exclusion of summary-popup styling. Chart/summary content redesign is not otherwise inferred from this styling correction.
- [x] **No theme controls/options on the Settings page.** Theme editing belongs in the main-page theme dropdown, as in the current EQ app.
- [x] Move **Edit display labels** into the theme dropdown.
- [x] Save named theme files in **Downloads/ella-monitoring/themes**, with EQ-equivalent file-based save/load/manage behavior. Preserve existing saved themes during migration; adapt storage access for the supported Android versions.

### Settings page

- [x] Do not display connection-status text on the Settings page.
- [x] Change **Hide app title** to **Hide connection status**; the title is removed permanently. Apply the connection-status preference to the monitoring display.
- [x] Add an explicit **Back** button that returns from Settings to the dashboard.
- [x] Group the **app version, Changelog/Update and Back button in one bounding box**, following the EQ Settings page's System grouping and themed appearance.
- [x] Retain the already-recorded update-check dropdown: **Off, 1hr, 3hr, 6hr, 12hr, 24hr**, alongside the previously requested update download/install and full changelog functionality.

### Review evidence (prior to this authorized build)

- EQ source reviewed at `e1b54f3f58efb92215d99e2e3f2941530fbcbc15`: the dashboard has separate preset/theme selectors; themes use anchored menus and files in Downloads; Settings explicitly places theme editing on the dashboard and groups Back/Changelog/Update/version in System.
- Ella source reviewed at `22dff908431f97356a07c54af198e506e3701d5f`: readout rows lack individual button backgrounds/borders, temperature rows follow incoming data order, the raw starter voltage is absent from LiveDashboard, and generic menu/theme handling differs from EQ.
- Runtime source resolved from the owner's supplied `flows(1).json`: it contains Time to Full 2 and the additional topic/join/calculation nodes absent from the older GitHub copies. The export remains private; only the functional calculation contract is recorded here. That review changed no application source. The later authorized 0.21 build implements the confirmed Node-RED alternative; a native protocol estimate remains unidentified.

## Missing EQ companion functionality — implemented in 0.21, 2026-10-10

- [x] Carry over the Joying EQ & DSP **Changelog/Update** button, its themed dialog and complete changelog (newest entries first), adapting project references for Ella Monitoring. The 0.2.0-preview settings page currently has no such button; the repository changelog alone does not fulfil this requirement.
- [x] Carry over the EQ app's update checking, **Download & Install** action, validated APK download, Android installer handoff and confirmed-install cleanup. Inspect the current EQ implementation before adapting it; use Ella's own package, version, signing identity and release source rather than EQ's.
- [x] Add a themed **update-check frequency dropdown in Settings**, with options in this order: **Off, 1hr, 3hr, 6hr, 12hr, 24hr**. Save the selection and use it for automatic update-check frequency; **Off** disables automatic checks. Keep manual checking available through Changelog/Update. This selection supersedes a fixed automatic-check interval copied from EQ.
- [x] Resolve the still-unsettled Ella release/package/signing configuration needed to make updates functional. This does not block displaying the complete in-app changelog. Updates were requested in APP-PLAN.md but remain unimplemented in 0.2.0-preview; do not describe the companion functionality as complete while these are missing.
- Originally recorded without source changes; implemented after the owner authorized 0.21.

## Future APK naming and versioning — 2026-10-10

- [x] On every future authorized Android build, deliver `Ella-monitoring-vX.XX.apk`, preserving this exact capitalization and two decimal places, with the version increasing by **0.01 per build**. Keep versionName consistent with the filename and increment versionCode. Do not append preview/build suffixes to the filename.
- This is a standing requirement for future builds, also recorded in `agents.md`; it does not request a new build or change the current delivered version.

## Owner-requested next-build list — 2026-10-10

The owner explicitly authorized this build with **"Ok. Do that build"**. Implemented in **0.2.0-preview / build 2**. Later additions/corrections take precedence. The requirements below supersede conflicting 0.1.0 layout/theme defaults. Device review remains pending; compilation/synthetic rendering does not establish exact physical-device fit.

### Companion-app reference and theming

- [x] Treat the owner's **Joying EQ & DSP app as the companion-app reference** for screen size, appearance and the specified functionality. Inspect its current source and actual controls when implementing; do not approximate it with a generic Android dashboard.
- [x] Match the EQ & DSP button styles, icons, theme engine and colour picker, with the same ability to theme most elements. Respect **6px padding between elements where relevant**, using the same coordinate/scaling approach as the reference.
- [x] All dialogue/popup windows must follow the application's current theme and the reference app's styling. **For this next build, do not touch the existing summary popups or charts:** they require drastic changes separately. Apply global theming to the in-scope surfaces while respecting this exclusion; do not redesign, alter or restyle the excluded summary/chart surfaces as part of this list.

### Screen size and orientation

- [x] Build pages around the **same reference screen resolution and scaling as EQ & DSP** (the owner's Joying is 1024×600); use the reference source to establish its actual layout/scaling behavior for other screen sizes.
- [x] **Force landscape on portrait screens.** This supersedes the preview's portrait/stacked dashboard behavior; do not use the old 800dp threshold to replace the reference layout with a portrait arrangement.

### Bottom bar

- [x] Match the EQ & DSP bottom bar's **height, style, button/icon sizes, spacing and theme behavior**.
- [x] In the first bounding box, left to right: **preset dropdown button → Live data → Charts**. The dropdown must have the same look and options as the reference app; preserve the owner's term "preset dropdown" rather than silently choosing different functionality. Live data opens the main monitoring page; Charts opens the charts page. Adding its navigation button does not authorize changes to chart contents.
- [x] In another bounding box, use the same reference controls, left to right: **Keep screen on → Full screen → Lock → Settings**, matching their icons, size and style.
- [x] **Lock** disables gauge-click popups. Preserve current collection/display behavior while locked.
- [x] Theme these controls using the same type of colour picker/theme engine as EQ & DSP.
- [x] **Settings opens a settings page, not a popup.** Use the EQ & DSP settings page as the reference for separation, grouping and display of settings.

### Main monitoring layout

- [x] Use the owner's supplied **Node-RED dashboard image** as the layout reference. Keep its arrangement and formatting; do not publish the private image or its readings/inventory.
- [x] **Current list at left:** each label and its value share one line. Remove the extra "current" text added to the displayed titles; preserve editable user labels.
- [x] **CFET, DFET and all other On/Off indicators:** put them together in **one bounding box**, each as a single-line indicator arranged as in the reference image. **CFET, DFET and EOC are green when On; all the others are red when On.** Preserve the distinction between Off and unavailable.
- [x] **Central gauges below the flags:** retain their current drawing for now; correct their padding and positioning to fit the reference arrangement.
- [x] **Barometer/LPG/water/pitch/roll box:** correct its layout to the reference image: barometer above, LPG/water paired below, pitch/roll paired below those.
- [x] **Temperature box at far right:** each label/value shares one line, following the same compact layout principle as the current list at left.
- [x] Fit and format the remaining **battery voltages, delta, SOC, remaining capacity and time remaining** according to the image/previous Node-RED display, preserving the requested central/bottom arrangement, units and source identities.

### Settings-page additions

- [x] Add an option **not to display the app title**.
- [x] Add an option for the app to be **persistent**, matching the EQ & DSP app's existing persistence option and behavior.

### Build outcome

0.2.0-preview implements this authorized list; the existing summary popups/chart contents and all Pi collector/database behaviour remain unchanged. Viewer presets adapt the EQ Save/Update/Manage options to display settings; audio factory presets are project-specific to EQ. The first preview signing key is unavailable, so this APK requires reinstalling the preview and re-entering its settings. See [CHANGELOG.md](CHANGELOG.md) and [BUILD-VALIDATION.md](BUILD-VALIDATION.md).

- [ ] Owner device review: compare the new screen with the reference image, try themes/labels/presets/lock, test forced landscape/fullscreen/keep-screen, notification permission and persistence/background/resume on a phone and the Joying head unit.

## Earlier clarification — context only, 2026-10-09

The owner clarified that the earlier questions were to establish the data, not requests to record application changes. Do not treat those questions or assistant assumptions as extra next-build tasks.

- Owner reports phone API connectivity: Pico readings arrive and SBMS readings eventually arrive. This is not full device/recovery validation.
- The owner confirms the SC302T shunt is assigned to the Pico battery. The complete Pico block under the exact `Ella  ` label supplies the battery information; preserve exact source bindings, units and signs.
- The combined battery history contains Pico current, SC302T voltage and Pico SOC, alongside SBMS battery current, PV1/PV2 current, voltage, SOC and external-load current. Sources remain distinct within the same battery record. Pico derived battery power uses the assigned Pico battery current multiplied by fresh SBMS voltage.
- The preview's Load Σ · Pico display currently sums individual shunts. The owner did not request a calculated total or new aggregate summaries/graphs; those were assistant assumptions. No new logging/schema or popup/chart work is authorized by this clarification.

## Authorized first Android preview — 0.1.0-preview, 2026-10-09

- [x] Owner explicitly requests starting implementation and will monitor allowance warnings.
- [x] Add native Java source under `android/source/`, minimum API 27 / target API 37, with pinned AGP/Gradle tooling. Keep Pi collector 0.7.1 and schema 2 unchanged.
- [x] Add bounded GET-only local API transport, Wi-Fi/Ethernet routing, Android 17 LAN permission, encrypted private token storage and regular TLS validation. Independent source expiry clears values rather than substituting zero; polling stops in the background.
- [x] Add the reference dashboard grouping and both source comparisons, live flags/environment/temperature/cell/capacity details, themes, bottom controls and detected FYT foreground screen handling. Use verified per-load signs for a selected-load consumption sum; leave it unavailable on unknown signs/values.
- [x] Add persistent element/group label overrides and restoration, source-referenced details/retained summaries, paginated rolling/defined charts, explicit coverage/gaps/net direction, temperature extrema and selected-range CSV export.
- [x] Record preview defaults for review: landscape grouping at 800dp or wider, stacked narrower screens; rolling Month 30 days; defined midnight-aligned UTC 6/12-hour blocks, calendar days, Monday weeks and calendar months; dark/light themes; manual Pi origin/token; development-only preview package/signing. Production identity/releases/updater remain unsettled.
- [ ] Test actual phone/head-unit installation, Pi LAN/HTTPS, Android 17 permission grant/denial, source/API/Wi-Fi restart, rotation/visual fit/long labels, background/resume, FYT timeout/wake handling and interaction with the EQ app, chart values and CSV file-picker output.
- [ ] Agree production app identity and persistent private signing/release location, then implement validated GitHub update checking/download/installer handoff.
- [ ] Agree alert rules/delivery and implement them with background support in a subsequent authorized stage; no foreground-only alerts are introduced here.
- [ ] Future remote outbound relay, offline cache, direct Pico/MQTT modes, database backups and Simarine settings remain separate stages.

See [README.md](README.md), [CHANGELOG.md](CHANGELOG.md) and [BUILD-VALIDATION.md](BUILD-VALIDATION.md). This preview is a concrete first review/test build; compilation/synthetic tests do not establish hardware connectivity or production signing.

## Current planning: Android viewer requirements — 2026-10-09

- [x] Owner targets both phones and the Joying/FYT head unit, supporting Android 8.1 (API 27) through current Android versions.
- [x] Record the owner's requested EQ/DSP visual grouping, Node-RED dashboard reference, bottom bar, full-screen/keep-screen/theme controls, relevant summary popups, line/period bar graphs, data export and GitHub updates in [APP-PLAN.md](APP-PLAN.md).
- [x] Inspect the separate EQ app's current screen/window/update source as a read-only reference; preserve FYT Main module 0 command 13 handling with Android foreground lifecycle and timeout restoration. Detect supported capability before head-unit-specific operations.
- [x] Owner confirms live flag changes working; short local API load test completed without failed requests and collection/logging remained healthy. Publish only functional outcomes, excluding system/network details and numerical resource/timing snapshots.
- [x] Owner selects rolling Last 6 hours, Last 12 hours, Last 24 hours, Week and Month, with a Rolling / Defined period toggle.
- [x] Owner provides the existing dashboard photo and asks to preserve its layout/grouping and both monitor comparisons. Record the arrangement without publishing the photo, readings or inventory.
- [x] If alerts are included, they must work while the Android UI is backgrounded; implementation, thresholds/rules and delivery policy are not yet selected.
- [x] Owner requests stylish design informed by other manufacturers while staying consistent with the EQ app and supplied dashboard arrangement. Record Victron GX and Mastervolt display guidance as design references; visual styling/wireframes remain to review.
- [x] Make each monitoring-element label/group heading renameable, with persistent display aliases and original/default restoration. Keep stable source/metric bindings, units, calculations, history and monitor settings unchanged; apply labels consistently to related graphs/popups/exports.
- [ ] Settle phone adaptation, indicator/summary fields, defined-period boundaries/rolling Month convention, graph interaction/edge coverage, offline/background alert delivery, export formats and package/signing/release location before an app build.
- [ ] Future option: internet-relayed alerts and remote data display using Pi-initiated outbound uploads and an authenticated phone read-only relay. No exposed Pi API/ports, incoming commands or remote monitor/settings changes. Investigate notification/provider choices later; no cloud service or private-data upload is enabled.
- [ ] Investigate Simarine settings backup/restore/editing later; vendor-app capability does not prove our collector has a complete restorable backup or settings write path.
- [x] Owner subsequently authorizes starting implementation; the first Android preview is recorded above. This older planning entry is retained as history, not an outstanding implementation block.

## Previous authorized correction: live SBMS states in collector 0.7.1

- [x] Expose the complete accepted SBMS broadcast through the live API; preserve existing normalized battery values and original raw field names/units.
- [x] Provide live boolean CFET, DFET, OVLK, UVLK, EOC, IOT, LVC and CELF states for On/Off display. Missing/invalid flags and stale/disconnected readings are unavailable, never silently Off.
- [x] Deliver flag/auxiliary-only changes within a source-clock second while continuing to ignore retained and complete repeated broadcasts, including reordered object keys.
- [x] Keep flags/auxiliary values out of database history; preserve schema, selected measurements, calculations, retention and Pico MQTT output.
- [x] Owner updated the Pi and confirms the live flag changes work. Android display remains pending.

## Previous authorized step: local API in collector 0.7.0

- [x] Owner requests the API before Android implementation. Add authenticated, read-only status, measurement catalogue, live Pico/SBMS freshness and bounded combined/per-metric history.
- [x] Reuse the existing service and schema 2 without changing collection, selected statistics, retention, source references, units, signs or MQTT output. Add independent private credentials, optional HTTPS, pagination and explicit partial/gap semantics.
- [x] Generate/check private parent API settings, document setup/routes/limits, test denied access, safe errors, read-only database access and simultaneous collector/API/MQTT operation.
- [x] Inspect the owner's live schema-2 database and migration backup privately: integrity passes, completed history/metadata preserved, active buckets extended consistently. Other shunts/environmental history and primary/secondary voltage references are present.
- [x] Owner enabled the API and confirmed local status/live/history requests; supplied responses pass private freshness/calculation/source-reference review.
- [x] Owner completed a short local API load check with no request failures and healthy collection/logging; raw timing/resource data remains private.
- [ ] Test LAN/TLS requests and restarts. Finish long-run retention/midnight and charge-polarity checks.
- [x] Agree Android target range/device categories; draft screen/chart requirements are recorded in APP-PLAN.md.
- [ ] Finalise layout, discovery, credential storage and offline/background behaviour before the app build.
- [ ] Keep history on its current persistent storage for now. Plan USB migration with consistent backups; SD/Android/Google Drive destinations, backup schedule and retention remain undecided. No automatic backup/cloud access is included in this API build.

## Previous authorized step: combined battery history in collector 0.6.0

- [x] Owner requests one combined battery record per interval with field-level source references, instead of separate battery/SBMS history rows.
- [x] Include primary/secondary voltage, distinct Pico/SBMS current and SOC, PV1/PV2 and external load, retaining independent coverage and energy/statistics.
- [x] Back up and migrate existing history atomically without recalculating/discarding saved values; keep other shunts, pressure and outside temperature in their current records and periods.
- [x] Add combined inspection and preserve existing per-metric commands as views; support existing private configuration without regeneration.
- [x] Owner updated the Pi and supplied live history plus pre-upgrade backup; read-only review confirms integrity, preserved completed records, source bindings, valid zeros and consistent rollups. Long-run hardware checks remain pending.
- [x] History API is implemented in 0.7.0 above; Android/browser display remains a subsequent stage.

## Previous authorized step: SQLite logging in collector 0.5.0

- [x] Implement optional SQLite history and private sensor selection alongside unchanged Pico MQTT publishing and SBMS reception.
- [x] Log the main battery through its selected battery instance; exclude the duplicate physical main-shunt alias. Keep selected secondary voltage raw and exclude Pico internal voltage from history.
- [x] Use fresh SBMS voltage for derived Pico battery/load watts; retain raw signed current, source/display names, relevant-device SOC and stable database IDs. Check configuration fingerprints before using a selected sensor.
- [x] Implement UTC duration-aware electrical summaries, additive energy/directional totals, coverage, environmental summaries and requested retention. Use fresh SBMS pack voltage for PV1/PV2 battery-side power; leave unverified directional classifications unavailable.
- [x] Save partial summaries every minute, flush graceful stops, resume saved sums without extending over downtime, prevent concurrent writers and report storage failures/clock steps.
- [x] Add configuration generation, read-only inspection and private offline replay. Keep capture/configuration/database/system details out of published source.
- [ ] Install private configuration on the Pi; confirm saved rows, live value/energy comparisons, sign/voltage settings, reboot/network/broker recovery, storage use and long unattended operation.
- [ ] Implement the local history API after live logging checks, then build the Android/browser history display. Neither is included in 0.5.0.

## Previous authorized step: ElectroDacus reception in collector 0.4.0

- [x] Receive `/Ella/sbms` on the existing MQTT connection independently of Pico availability; resubscribe after reconnection and retry denied subscription without stopping publishing.
- [x] Decode all four mA current fields to amps, SOC and pack voltage from a private configured cell map; include PV2 and preserve valid zeros/current signs.
- [x] Use Pi UTC receipt time and keep monitor time separately. Reject invalid payloads, ignore retained/exact repeated samples, and clear current readings on stale timeout or broker disconnect.
- [x] Preserve original Pico MQTT output and quiet service operation; add `--sbms-stdout`, `--no-sbms` and local SBMS capture records for diagnostics.
- [x] Owner supplied a short real Pi run showing simultaneous Pico/SBMS connection, approximately one SBMS sample per second, valid derived pack voltage and normal timed shutdown. Keep raw readings/timestamps/runtime details private.
- [ ] Check actual broker restart, stale/repeated/retained behaviour and long unattended SBMS collection. Confirm charging polarity under known conditions before directional integration.
- [x] Selected SQLite history, retention and restart recovery are added in 0.5.0 above. No database/API was part of 0.4.0; the API remains pending.

## Previous Pico base and service work

- [x] Start with updated Node.js discovery/configuration and TCP request methods.
- [x] Carry over the owner's sensor mappings, pitch/roll and Ella JSON calculations/formatting from Python.
- [x] Exclude SignalK integration. The initial MQTT exclusion was superseded by the owner's 2026-10-08 request for publishing to the existing dashboard.
- [x] Add fragmented TCP reads, error/retry handling, sender checks and clean shutdown.
- [x] Compare normal readings with the original Python functions and test simulated connection failures/recovery.
- [x] Provide recording, verification and replay commands for a live Pi test.
- [x] Support installation directly in `~/PicoData/node.js`, including a bundled Python comparison reference so deleted original folders are not needed.
- [x] Receive configurations and live readings from the owner's actual Pico; 1,096 captured outputs match the original Python, with zero differences (owner-reported verification).
- [x] Confirm real receive frame lengths/checksums for the reported capture: 108 TCP responses and 1,096 UDP packets all pass.
- [ ] Confirm automatic discovery mode; retain device identity/runtime inventory privately unless the owner requests publication.
- [x] Owner reports successful Pico reboot/Wi-Fi recovery and correct-looking MQTT output in the existing Node-RED webpage.
- [ ] Audit expected values against the Pico display, test changed IP and actual broker restart, and run a long unattended stability check.

## MQTT addition — 2026-10-08

- [x] Add optional publishing using the existing five-key Python `mqtt` configuration file.
- [x] Preserve the exact configured topic, parsed JSON structure/values/labels/units, QoS 0 and non-retained messages.
- [x] Restore original raw-65535 value handling in MQTT compatibility mode; reject incomplete output snapshots.
- [x] Add independent reconnect, no offline backlog/resync, bounded pending writes and clean shutdown.
- [x] Test actual MQTT wire settings and received CLI payloads against the Python source; verify Pico output continues with broker access denied.
- [x] Owner reports successful publishing/display through the existing Mosquitto/Node-RED setup.

## Service changes — 2026-10-08

- [x] Use the parent `PicoData/mqtt` configuration by default for files installed directly in `PicoData/node.js`, independent of the working directory.
- [x] Remove normal readings from terminal/journal output by default; retain connection/error status. `--stdout` enables diagnostic JSON and `--no-mqtt` enables reader-only testing.
- [x] Add a generic systemd service template with the correct `bin/pico.js` path, network/broker ordering and a 30-second process restart interval.
- [x] Confirm default config resolution and silent MQTT payload parity in an isolated installation; missing config fails visibly.
- [x] Owner reports clean shutdown/restart and successful MQTT/Pico live connections with the updated service.
- Publish functional test outcomes only; exclude owner runtime/process snapshots, device inventory, addresses, credentials and account details.

## Repository layout — 2026-10-08

- [x] Move the standalone collector to the repository root `node.js/` and remove its former folder.
- [x] Preserve the original SignalK reference under `_old/pico2signalk/` to avoid mixing it with the collector.
- [x] Update documentation, links, code provenance and checkout/update examples. The parent MQTT config and deployed service path are unchanged.

## Proposed Pi logging direction

- Keep collection/logging on the always-on, headless Pi Zero 2 W; the head unit is not continuously powered.
- Prefer one focused service over an expanding dashboard/database stack. Measure actual memory/CPU before replacing working services.
- SQLite plus a small history API is the preferred proposal; the owner's selected logging/retention rules are in `node.js/DATABASE-PLAN.md`, with calculation details and final display/app architecture still to be agreed. No logging/API/UI implementation is included in this MQTT addition.
- Keep history on persistent storage outside zram-managed directories (proposed `/var/lib/ella/`), with batched durable commits; reserve `/var/log` for diagnostics.
- ElectroDacus MQTT decoding is now implemented in 0.4.0, preserving the existing Mosquitto destination during validation.

## Later Android work

- Start the Android port after reviewing the live Pi evidence; confirm target hardware, Android range, layout and background requirements first.
- Review Android's role as a viewer/client of the Pi logger; direct Pico/embedded-broker operation can remain a separate future option. ElectroDacus reception is implemented on the Pi; Android source is not yet implemented.
- Keep SignalK functionality outside the Android scope.

## Database planning — 2026-10-08

- [x] Review existing Pico output and dashboard SBMS mappings; record the discussion draft in [`node.js/DATABASE-PLAN.md`](../node.js/DATABASE-PLAN.md).
- [ ] Agree logged measurements, power/energy inputs and signs, sampling/retention, daily timezone and acceptable uncommitted-data window.
- [x] Confirm a live SBMS payload and active cell channels privately; solar current fields are present. Solar voltage/measurement-point verification remains pending.
- [ ] Implement and benchmark the agreed persistent SQLite history only after implementation is requested. Keep legacy MQTT output unchanged, separate sources, and preserve gaps/validity in summaries and energy totals.

## Owner logging specification — 2026-10-08

- [x] Record the owner's selected measurements and retention in [`node.js/DATABASE-PLAN.md`](../node.js/DATABASE-PLAN.md), superseding the initial broad candidates and retention tiers.
- [ ] Barometer: hourly for 1 month; last valid daily reading indefinitely.
- [ ] All Pico current shunts: `[W, A, V]` minute/hour/day/month summaries retained for 1 day / 1 week / 1 month / indefinitely; add main-battery SOC.
- [ ] Outside temperature: hourly for 1 month; daily minimum/maximum indefinitely.
- [x] Add independent ElectroDacus MQTT subscription/decoding for voltage, total/battery current, PV1/PV2 current and SOC. Pi operation and solar voltage/current associations remain to verify.
- [ ] Agree instantaneous-power averaging, duration-aware rollups and directional energy/charge totals so short-history deletion does not remove information needed for later calculations.
- [ ] Confirm SOC/hourly environmental aggregation, calendar timezone/cutoffs, ElectroDacus retention and durable in-progress bucket recovery.
- This records planning requirements only; no logging implementation or version change is authorized by the calculation question.

## Current polarity requirement — 2026-10-08

- [x] Owner defines all non-battery Pico shunts as load/current-draw channels; battery channels are net balance and ElectroDacus PV channels are charging supply.
- [ ] Verify voltage association and reported sign separately for every Pico shunt and ElectroDacus current field using known charging/load conditions; current draw is not assumed negative on every channel.
- [ ] Preserve source readings/MQTT output and use explicitly verified channel-specific direction mappings for logging calculations. Unverified channels remain unclassified for directional totals.
- [ ] Keep net main-battery inflow/outflow separate from gross supply/load totals; PV1 is a charging-only supply channel as described by the owner, with its reported polarity still to verify.

## Proposed implementation sequence and history API — 2026-10-08

- [ ] Confirm live SBMS fields/reporting cadence, per-channel voltage/sign mapping and remaining aggregation/timezone choices.
- [x] Add ElectroDacus subscription using the existing Mosquitto destination without altering Pico MQTT output.
- [ ] Implement persistent selected history/rollups/retention after Pi reception validation.
- [ ] Add an authenticated local JSON API: metric catalogue, live readings/freshness, bounded per-channel history queries and retained Wh/Ah totals. Only the Pi opens SQLite; Android/browser clients request data through the API.
- [ ] Define stable API IDs/units/array field meanings, credentials/transport/port/address discovery, partial/gap responses, backups/export and schema migration.
- [ ] Test restart recovery, power interruption, clock/calendar boundaries, pruning, denied MQTT/API access and long unattended collection; privately measure Pi performance.
- Reception is now authorized and implemented; the remaining sequence is planned Pi logging/API work, not an Android build.

## Received ElectroDacus example — 2026-10-08

- [x] Owner supplied a live MQTT JSON payload containing SOC, cell-voltage slots and battery/PV1/PV2/external-load current fields. Publish the field contract only, not the sample readings or device inventory.
- [x] Confirm configured active cells and the example's discharge condition privately. Implement Pi UTC receipt time and retained/stale handling.
- [ ] Verify remaining channel polarity, actual reporting cadence and retained/repeated behaviour on the Pi before integration.
- [ ] Decide whether external-load current is required in the logged channel set; keep it distinct from battery balance and overlapping load-shunt sums.

## Owner-confirmed acquisition details — 2026-10-08

- [x] Confirm Pi acquisition/receipt timestamps for both Pico and ElectroDacus logging; preserve existing MQTT output.
- [x] Confirm the active cells in the supplied example and that its negative battery current is discharge. Keep the actual cell map and present installation inventory private.
- [x] Convert PV1/PV2 mA to A with division by 1000 in reception, preserving valid zero readings.
- [ ] Include both solar channels in electrical history; log valid zeros distinctly from unavailable readings.
- [x] Include PV2 for future use, with its directional verification performed when connected. This confirmation does not establish its charging polarity from the current zero sample.

## Agreed logging choices and voltage review — 2026-10-09

- [x] Select UTC timestamps and minute/hour/day/month aggregation boundaries; optional Europe/London chart display does not change UTC daily totals.
- [x] Select the more accurate duration-aware average of instantaneous paired A × V measurements, with coverage and additive rollup integrals.
- [x] Store last valid SOC from each relevant device and use identical electrical retention periods for Pico and SBMS0.
- [x] Record owner-described SBMS battery/external-load consumption distinction without adding overlapping currents to totals.
- [x] Inspect current sensor mappings: voltage sensors/battery voltage are decoded separately; extra current-record fields have no verified voltage interpretation, and bracketed sensor names are filtered from legacy public JSON.
- [x] Owner confirms all Pico load shunts are on the main battery supply; use SBMS pack voltage as their preferred reference. Preserve original Pico MQTT output and label derived voltage provenance. Other battery/PV measurement points remain separate.
- [ ] Pair only fresh valid current/voltage measurements; retain current/SOC during voltage gaps and avoid an automatic fallback to untrusted voltage.
- [ ] Implement persistent logging and the authenticated read-only API when requested; this decision/source-inspection update changes no application version or source.

## Secondary voltage and stable names — 2026-10-09

- [x] Record selected secondary shunt-module voltage for comparison and exclude Pico internal voltage from regular history/reference selection; keep private sensor selection outside published source/documents.
- [x] Choose database identity separate from name, packet position and dashboard instance: permanent metric ID with source/device, sensor ID and measurement kind; preserve exact source name plus editable display name and time-effective mapping/name history.
- [x] Independently inspect the owner's supplied raw Pico/SBMS capture, validate framing and map actual configured IDs/names privately. Secondary voltage is present; main virtual-battery voltage/current duplicate configured source measurements.
- [x] Identify load-dependent primary/secondary voltage differences; avoid a fixed correction. Extra current fields appear counter-like rather than voltage, with meaning/units still unverified.
- [ ] Verify sensor-ID persistence through rename/reboot/reconfiguration and handle ID reuse/physical channel changes explicitly.
- [ ] Add an internal timestamped, ID-keyed decoded Pico snapshot for the future logger, before legacy filtering/name collisions; preserve all current MQTT output.
- [ ] Log the selected secondary voltage with original readings preserved; evaluate paired voltage/current differences before approving any correction or fallback.
- [ ] Implement the agreed SQLite history/API when requested. This planning/source review changes no application version or runtime code.

## Capture findings for the logger — 2026-10-09

- [x] Pass length/CRC checks for all recorded complete TCP replies and live packets; reconstruct the recorded sensor metadata from configuration. Raw capture/statistics/actual ID-to-name map remain private.
- [ ] Bind the private confirmed sensor map to permanent database metric IDs and editable names; retain versioned mapping metadata and check identity across later configuration changes.
- [ ] Treat duplicate main-battery current/voltage paths as aliases of one measurement; attach the relevant battery SOC without double counting currents/energy.
- [ ] Record the selected secondary voltage unchanged. Any later calibration/fallback requires explicit validation under different operating conditions, not a fixed offset inferred from one sample.
- [ ] Keep extra current-field counter candidates diagnostic-only until scale, deadband, signs, rollover and resets are established. Continue with sample/time-based integration for primary history.
- [ ] Consume raw decoded ID-keyed Pico snapshots before public filtering/throttling, while preserving the original MQTT contract and keeping cross-source timing/freshness explicit.


## PV voltage confirmation — 2026-10-09

- [x] Owner confirms both PV currents are charge supplied to the battery; use fresh SBMS pack voltage for their watts/Wh, not an assumed panel voltage.
- [x] Log current, battery-side power/energy, coverage and valid zeros for both PV channels with the agreed electrical periods/retention. Preserve raw signs; polarity verification remains separate.

## Owner-reported initial history check — 2026-10-09

- [x] Owner inspected an actual saved minute electrical record and could identify voltage/SOC. Keep the posted values/timestamps private. This verifies initial live history visibility, not all channels, energy accuracy, retention or long-run recovery.

