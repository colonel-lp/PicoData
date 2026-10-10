# Appearance and viewer presets

Updated in **0.2.0-preview / build 2**.

The theme configuration/defaults, HSV wheel with brightness control and appearance-editor/button artwork derive from the owner's Joying EQ & DSP source at the inspected 1.54-beta reference. No DSP operations or vendor assets are reused. Live readouts, navigation, Settings and their dialogs use the active theme. Existing summary popups and chart contents retain the original preview palette, as requested.

Theme roles cover gauge positive/negative, text, panel/border/background, button background, On/Off text/borders and changed-preset indication. Five font choices match EQ. Theme menus provide Save theme as, Update when changed, Manage/rename/delete, Edit theme, Default and saved themes. Apply/cancel the wheel or enter hex colour. Default cannot be overwritten. Active/saved themes persist in app-private preferences.

The left bottom dropdown retains EQ's Preset terminology and Save settings as / conditional Update / Manage / saved-name choices. Manage offers Rename, Delete and View values. A viewer preset records display aliases, active theme, local clock, title visibility and chart range/quantity/metric/defined selection. Existing names require overwrite confirmation. Connection credentials, API origin, persistence/screen global switches and monitor writes are excluded. DSP factory presets are not monitoring presets. A modified named preset has a star/changed-theme colour. Loading/deleting does not rewrite Pi history.
