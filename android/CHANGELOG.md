# Android changelog

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
