# Android changelog

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
