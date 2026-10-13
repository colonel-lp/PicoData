# Settings and persistence

Updated in **0.29 / build 11**.

Settings is the grouped two-column Connection and Display & App page with an untitled bottom-right control block. Connection retains encrypted private-token/manual-origin setup and foreground reconnection. Credentials disable screenshots and token view-state saving. The page content has no connection-status line or embedded theme controls. The bottom bar retains the Theme dropdown beside Preset, including while Settings is open. Hide connection status applies to monitoring pages; the app-wide title is removed. Local-clock display preserves UTC chart history boundaries; calendar summaries use the device timezone in 0.27. The bottom-right block has Updates: plus a reserved position in its top row, Changelog / Update and Back in its lower row, and the version at the bottom. Updates frequency choices open above their button.

Persistence remains opt-in, following EQ's ongoing return-to-app notification while backgrounded, with Android 13+ notification permission and the guarded specialUse service. It never adds background live polling, data logging, wake locks or alerts. Pi logging continues independently. Foreground keep-screen and detected FYT handling remain intact; fullscreen recalculates the canvas using the released bar space. Device/firmware restrictions and EQ interaction still require hardware validation.

The installed preview package and retained 0.2.0 development certificate are preserved for compatible updates when the owner compiles with the same installed signing key. A separate production identity is not introduced. Keys and local SDK/build outputs are excluded from GitHub.

See [updates.md](updates.md) for changelog/release/installer handling.

0.22 supplies a 24px logical checkbox drawable inside 38px rows rather than density-sized native glyphs. Labels and touch behavior retain CheckBox accessibility/state semantics. The opted-in persistent notification contains only Ella Monitoring with no body text, keeping its tap action. The launcher/notification icon uses the EQ reference's 108dp intrinsic size and padded artwork bounds. No APK is assembled by this source-only update; device appearance remains for owner verification.

Font remains a preset setting; in 0.25 its control moves from Display & App to Edit theme. Connection address/private token now belong to presets: snapshots remain encrypted; named files move to Downloads/ella-monitoring/presets in 0.26 and loading validates them before applying/reconnecting. Older presets and System default keep the existing connection. Changed editable Settings fields use Changed indicator colour and clear on matching save/restore. Theme colours remain separate.

In 0.24 element highlight/hidden-content preferences are preset state, including compatible conversion of older gauge-highlight choices. Theme edits still change only the Theme indication. Foreground live and catalogue/status work use separate cancellable workers; backgrounding/reconnecting cancels both and rejects old callbacks. No background polling is added.

Keep screen awake, Full screen and Lock retain their current app-wide states when loading any preset or System default. They are excluded from preset storage and changed-item indication; ordinary preference persistence remains.

In 0.26 Settings no longer contains an Export group/button. Chart CSV export still opens ACTION_CREATE_DOCUMENT; a missing handler or another runtime launch failure uses Downloads/ella-monitoring/exports with the same CSV bytes and a timestamped filename. Feedback distinguishes the launch exception type without exposing credentials/runtime details. Ordinary picker cancellation performs no fallback write. The shared Downloads permission gate covers preset/theme saves and fallback exports on API 27/28; API 29+ uses owned MediaStore Downloads entries. No document picker is required to save preset/theme files.


In 0.28 popup/future chart display choices have separate global ownership: period, Rolling/Fixed, recorded month, A/W, Line/Bar and Sum are individually keyed in Downloads/ella-monitoring/display-settings.json. The chart-display private preferences cache stores only those choices and pending IDs, outside viewer preferences/preset comparison. Highlight/Hide remains in presets; colours remain in themes. Named presets and theme files/format are unchanged.

The global file uses format 1 with a maximum 128 identities and the shared 64 KB strict JSON/UTF-8 limits. Known selection fields/types/ranges/months are validated; unknown fields, credentials and malformed entries are rejected. Malformed existing files are not overwritten by autosave. Choices are cached immediately, written on a separate worker after a short debounce and flushed on popup close/background when storage access is granted. Pending cached choices survive restarts and merge with the valid file before saving. IO does not hold the UI selection lock; choices changed during a write remain pending. Existing Downloads permission, rollback and picker-free paths are reused. Actual provider/device behavior remains to verify.


In 0.29 the global display file also stores each popup's Values boolean. Older entries default on. It hides only bar annotations; header values, Samples/period and axes remain visible. This flag remains outside viewer preferences/presets, alongside the existing popup period/Ah-Wh/Line-Bar/Sum choices. File format 1, bounds, pending-cache/retry and malformed-file preservation remain.

Open popup refresh requests/timers are foreground-only and do not change logging or poll the owner devices directly. Closing/backgrounding/reconnecting cancels them; foreground recovery reloads a still-visible popup with its saved choices. A selected older month is not silently advanced; its month list can still update at calendar boundaries. Failed month loading is distinct from a successful empty history list and leaves an enabled Retry months action. Main app notification/screen/preset/connection policies are preserved.
