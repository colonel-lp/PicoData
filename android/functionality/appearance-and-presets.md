# Appearance and viewer presets

Updated in **0.21 / build 3**.

ThemeConfig, ThemeAppearanceView and ColorWheelView use the owner's current Joying EQ & DSP reference. Monitoring maps positive/negative gauge colours to the equivalent EQ roles. Button backgrounds, On/Off text/borders, panel/border/background, general text, modified indication and five fonts remain themeable. Fixed green/red flag meanings are preserved.

The main-page theme dropdown uses the EQ anchored menu: Save theme as, conditional Update, Manage themes, Edit theme, Edit display labels, Default and saved theme names. Named modifications use the changed-indicator colour. Manage presents a selectable list with Rename/Delete actions enabled only after selection. Compact name entry, theme panel, font dropdown and live HSV/brightness/hex colour picker follow EQ's chrome. Colour cancellation/outside dismissal restores the original value; OK persists the active edit. Settings has no theme controls.

Named JSON theme files are stored in Downloads/ella-monitoring/themes. Android 27–28 uses a legacy storage-permission path; API 29+ uses MediaStore Downloads for files owned by the app. Active/unsaved state remains private preferences. Existing appearance.saved preferences migrate individually and are removed only after a file is written successfully; permission/write failure preserves them. Default cannot be overwritten, and invalid path names are rejected. Rename writes the replacement before deleting the original.

Preset dropdown offers Save settings as, conditional Update, Manage presets, LIVE selection and saved names. Manage includes View values and Copy. A preset stores display aliases, theme, clock/status visibility and chart range/quantity/metric/defined selection. Connection credentials, persistence and screen-global switches are excluded. DSP/audio factory presets are project-specific and are not monitoring presets. Modified named settings show a star/changed indicator.

All popups, including snapshots/retained summaries, inherit the active app theme and EQ panel/text/button/border styling; summary contents and chart calculations remain intact. No vendor/DSP implementation is copied.
