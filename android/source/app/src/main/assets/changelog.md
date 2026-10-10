# Android changelog

## 0.24 — 2026-10-10

Android source version 0.24 / versionCode 6. Owner-built APK output: Ella-monitoring-v0.24.apk. Package/signing identity, collector/API/database/MQTT contracts and SDK targets are unchanged. This delivery prepares source only.

- Keep screen awake, Full screen and Lock are independent app-wide states. Exclude them from preset save/load and modification checks; loading older presets or System default preserves their current values.
- Restore live colour preview by repainting existing dashboard views. OK/Cancel returns directly to the retained colour list without rebuilding the page; cancellation rolls back. Adapt the working EQ picker flow, retaining RGB validation and undimmed dialogs.
- Replace Gauge highlight with Highlight 1 and add Highlight 2. Show both colours in the editor and all element popups. Each element can select one highlight or neither. Preserve older highlights as Highlight 1.
- Add Hide contents to readout and heading popups. Hide labels/values/gauges while retaining boxes, spacing and popup access. Save visibility/highlight choices in presets; themes store colours only. Changed settings use the existing difference indication.
- Restore 6px inside the summary box after V[P]/V[S]. Use 10px text side padding for cell voltages, delta and both V[P]/V[S] cells; preserve centred lower voltage/Ah/runtime values.
- Add Inverter as the final live current row: Battery [Pico] minus the displayed Load [Pico]. Preserve signed inputs; unavailable inputs show a dash. No new logging or history is introduced.
- Trust the collector's independent Pico/SBMS freshness decision instead of applying a second short source-age limit. Expire cached HTTP snapshots after three seconds including request latency; actual stale/disconnected sources still show dashes.
- Isolate catalogue/status requests from live polling. Metadata failures do not clear readings. Retain stable dashboard views through missing snapshots/bindings and avoid repainting unchanged values, addressing periodic whole-dashboard redraws.
- Move Changed indicator beside Title outline. Add Indicator background for the flags box and Voltages background for both lower summary/cell-voltage boxes. Preserve fixed green/red flag semantics and compatible older theme/preset imports.

## 0.23 — 2026-10-10

Android source version 0.23 / versionCode 5. Owner-built APK output: Ella-monitoring-v0.23.apk. No APK is assembled or published in this source delivery. Collector/API/database/MQTT contracts and signing identity remain unchanged.

- Separate colour-only themes from viewer presets. Font moves to Settings and belongs to the preset. Theme edits/selection no longer mark presets UNSAVED; presets do not replace theme colours.
- Save labels and adjustable app/chart/screen/highlight settings, including connection address and encrypted private token, in presets. Restore connection through the normal connection lifecycle. Compare decrypted token values so re-encrypting the same token does not create a false change. Keep tokens and private addresses out of copied preset summaries.
- Migrate older presets and active font preferences without clearing connection settings. System default resets viewer settings while keeping the current connection. Existing theme colour files remain compatible.
- Keep automatic chart fallback separate from the saved selection. Refresh change indication immediately for chart/settings edits; live readings never count as preset changes.
- Apply Changed indicator colour to changed labels/headings, Settings fields, chart setting controls, screen/lock icons and gauge-highlight settings. Save or restore matching settings clears it; fixed flag status colours remain unchanged.
- Keep the theme colour list alive while the picker is open and return after OK/Cancel/outside/Back dismissal without added dimming. Reject invalid RGB entry without closing; accept six hex digits with optional # and surrounding spaces. Cancel restores the original colour.
- Add Title text alongside Title background and Title outline for Currents:, Ella Monitoring and Temps:, with backwards-compatible saved-theme fallbacks.
- Retain the 0.22 spacing/menu corrections and loaded-name selectors on all pages. Prepare source and regression checks for the owner to compile; device appearance, Android Keystore and installer verification remain device checks.

## 0.22 — 2026-10-10

Android source version 0.22 / versionCode 4. Prepared for the owner to compile in Android Studio; no APK is assembled, signed or published by this source update. Package, collector 0.7.1, API, MQTT and database contracts remain unchanged.

- Same-version selector correction: Preset/Theme show the loaded name in the EQ format, use UNSAVED and the modification colour/dot for changes, and remain available on all pages. Long names stay on one line with ellipsis. Version remains 0.22 / build 4.
- Same-version correction: restore 6px gaps around the central gauge box and side insets for indicators/gauges; align the environmental and cell-voltage boxes. Keep Theme on the Settings bottom bar and open Updates frequency choices above their button. Version remains 0.22 / build 4.
- Restore the Ella Monitoring header above the indicators, reduce central gauge heights to make room, and add the padded gauge bounding box. Rename Current Draw to Currents:; give all three headers themeable backgrounds/outlines.
- Centre lower voltage, Ah and runtime values; enlarge Ah/runtime text; apply 20px text padding in cell-voltage and delta boxes. Unavailable measurement values use a dash.
- Add independent Gauge panel 1/2 background and outline colours, plus optional per-gauge background highlighting and its theme colour. Group theme colour pickers logically; preserve older themes with sensible inherited colours.
- Order bottom controls Preset, Theme, Live data, Charts. Keep bar, button and icon dimensions constant when fullscreen toggles; only available content height changes. Preset/Theme use modification dots; labels and unsaved preset settings count as changes. System default replaces the old LIVE selector.
- Use density-independent Settings checkbox geometry. Place the untitled two-row update/back block at bottom right, reserve a future button position and put the version below. Label the frequency selector Updates:.
- Return to the theme editor after colour OK/Cancel without added dimming. Compact label-renaming dialogs omit their title/explanation; dialog buttons use companion proportions.
- Persistent notification contains only Ella Monitoring. Match launcher/status icon intrinsic size and artwork padding to the EQ reference.
- Android Studio/Gradle debug and release outputs are configured as Ella-monitoring-v0.22.apk using the public AGP VariantOutput filename API. Retain the signing key used for the installed app when compiling updates.

## 0.21 — 2026-10-10

Android build 3. Retains the existing preview package and the retained 0.2.0 signing key for an in-place update. Pi collector 0.7.1, MQTT, database and API contracts remain unchanged.

- Calculate time to full/empty with the verified Node-RED chain using Pico's reported nominal capacity, rounded remaining Ah and raw battery current. Preserve the charging minus sign and days/hours or hours/minutes format; zero, missing, invalid or stale inputs are unavailable. Stop displaying the collector's fabricated seven-day fallback as a native Simarine estimate.
- Remove the application title. Give every current, temperature, indicator, gauge and battery/cell readout its own themed button background, text and highlight/on/off border. Space current rows evenly with 6px gaps and no reference-layout blank rows. Order temperatures by their stable source roles. Place cell voltages in their own box; bind V[P] to raw Pico battery voltage and V[S] to raw starter-battery voltage.
- Centre gauge faces within their buttons and correct environment/lower-summary padding. Pitch/roll show a fixed -5 to +5 scale with a needle and colour only from zero to the needle; retain an out-of-range numeric value. Apply 6px padding above bottom buttons in either screen mode. Fullscreen uses the space released by the Android bars while respecting cutouts/keyboard space.
- Bring the EQ theme selector to the main page with Save as, conditional Update, Manage, Edit, Default and saved themes; move display-label editing there. Adapt the EQ anchored dropdowns, selected states, compact name editor, selectable Manage list, HSV/brightness/hex live colour editor with cancel rollback and font selector. Save named JSON themes in Downloads/ella-monitoring/themes and migrate existing saved preferences without loss. Android 8.1/9 asks for legacy storage permission; newer versions use owned Downloads files.
- Apply the current app theme and EQ dialog chrome to all popups, including retained summaries and changelog/update dialogs. Retain chart calculations and summary contents. Add preset View values/Copy and LIVE selection; presets continue to exclude credentials.
- Rework Settings: remove theme controls and connection-status text; add Hide connection status. Group version, Changelog / Update and Back in System. Retain persistence, local clock, connection configuration and chart export.
- Adapt the EQ updater to PicoData releases, accepting Ella-monitoring-vX.XX.apk assets only. Include the complete maintained changelog, manual checking, Download & Install, size/checksum/package/newer-version/signature checks, Android installer permission/handoff and cleanup after confirmed installation. Save automatic check frequency Off, 1hr, 3hr, 6hr, 12hr or 24hr; Off disables automatic checks and manual checking stays available.
- Deliver Ella-monitoring-v0.21.apk; future builds advance by 0.01. Compile, run synthetic regression/render tests and Android lint; hardware installation, exact head-unit fit and future-release installer handoff require device verification.

## 0.2.0-preview — 2026-10-10

Authorized build from the owner's companion-app correction list. Android build 2; collector 0.7.1, API contract and history schema 2 remain unchanged.

- Rebuild Live data around the supplied Node-RED arrangement: single-line current and temperature lists; one two-row flag box; central 2×4 current/SOC gauges; barometer above LPG/water and pitch/roll; compact bottom voltage, delta, capacity, runtime and available-cell readouts. Remove added current suffixes while retaining saved aliases. CFET/DFET/EOC are green when On; the other flags are red when On. Off and unavailable remain distinct.
- Use the owner's current Joying EQ & DSP source as the UI reference: 1024×600 fullscreen / 1024×510 minimum windowed logical scaling, 6px gaps, matching default colours, compact boxed bottom controls and vector keep-screen/fullscreen/lock/settings icons. Force sensor landscape and retain a rotated landscape canvas where Android ignores the orientation request.
- Add the EQ theme engine, HSV colour wheel, brightness control, hex editor and five font choices. Theme the live page, navigation, Settings, appearance editor and in-scope dialogs. Add saved themes with Default, Save as, conditional Update and Manage/rename/delete options.
- Add the requested Preset → Live data → Charts group and Keep screen → Full screen → Lock → Settings group. Viewer presets provide Save settings as, conditional Update, Manage/rename/delete/View values and saved preset loading. They save display labels, theme, clock/title and chart selection; connection credentials and monitor configuration are excluded. DSP/audio factory presets do not apply to the monitoring app.
- Replace the Settings popup with a grouped settings page. Add hide-title and optional persistence, matching EQ's ongoing return-to-app notification while backgrounded. Live polling still stops in the background; logging continues independently on the Pi. Keep existing detected FYT screen handling.
- Preserve existing chart drawing/contents, recorded summaries and their preview palette. Move existing range navigation to the Charts page header to retain every range under the new bottom bar. Preserve data identities, signs, freshness, calculations, storage and exports; the SC302T secondary-voltage channel supplies the distinct Pico voltage readout.
- Validate compilation, 28 synthetic/framework/render tests and lint (zero errors). Inspect synthetic live/Settings renders. Physical phone/head-unit fit, permissions, FYT behaviour and background/resume still need owner verification.

The previous preview's development signing key could not be recovered. This build uses a new retained private development key, so it cannot update 0.1.0 in place: uninstalling the old preview clears its connection, labels and appearance settings. Re-enter them after installing 0.2.0. Production signing/releases and automatic updates remain unsettled.

## 0.1.0-preview — 2026-10-09

First authorized Android viewer build. Collector/database/API versions remain unchanged.

- Add a native API viewer targeting Android 8.1 through Android 17, with bounded authenticated GET requests over the local Wi-Fi/Ethernet connection, Android 17 LAN permission, encrypted private token storage and normal HTTPS trust checks.
- Preserve the reference dashboard grouping and both Pico/SBMS current/SOC comparisons, with flags, load list, pressure, tanks, inclination, temperatures, primary/secondary voltage, capacity/runtime and cell details supplied dynamically by the API. Preserve units/signs, valid zero and independent source freshness; exclude the duplicated physical main-shunt from logging/sums.
- Add dark/light themes, full screen, foreground keep-screen controls and detected FYT Main-module display handling; responsive stacked phone layout and a persistent scrollable bottom control bar.
- Add editable element/group aliases with original restoration, snapshot/details popups and retained hourly/daily/weekly/monthly summaries.
- Add rolling 6/12/24-hour, Week and 30-day Month charts; explicit defined UTC periods, range navigation, line and verified-direction Wh/Ah bars, temperature extrema, pagination, gaps, partial/coverage inspection and labelled whole-bucket boundary precision.
- Add selected-range CSV export with source identifiers, display/original names, UTC intervals, stored statistics/units, coverage and safe escaping through the system file picker.
- Add synthetic API/model/network and Android activity tests, pinned build tooling and validation records. No private runtime readings/network/system inventory is included.

Development preview identity/signing is separate from the future production app. Hardware/LAN/UI fit, FYT timeout restoration, production identity/releases/updater, background alerts and future remote/direct-device features remain pending; no cloud uploads or monitor settings writes are enabled.

