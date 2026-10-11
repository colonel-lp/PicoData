# Settings and persistence

Updated in **0.25 / build 7**.

Settings is the grouped two-column Connection, Display & App, Export page with an untitled bottom-right control block. Connection retains encrypted private-token/manual-origin setup and foreground reconnection. Credentials disable screenshots and token view-state saving. The page content has no connection-status line or embedded theme controls. The bottom bar retains the Theme dropdown beside Preset, including while Settings is open. Hide connection status applies to monitoring pages; the app-wide title is removed. Local-clock display preserves UTC history boundaries. The bottom-right block has Updates: plus a reserved position in its top row, Changelog / Update and Back in its lower row, and the version at the bottom. Updates frequency choices open above their button.

Persistence remains opt-in, following EQ's ongoing return-to-app notification while backgrounded, with Android 13+ notification permission and the guarded specialUse service. It never adds background live polling, data logging, wake locks or alerts. Pi logging continues independently. Foreground keep-screen and detected FYT handling remain intact; fullscreen recalculates the canvas using the released bar space. Device/firmware restrictions and EQ interaction still require hardware validation.

The installed preview package and retained 0.2.0 development certificate are preserved for compatible updates when the owner compiles with the same installed signing key. A separate production identity is not introduced. Keys and local SDK/build outputs are excluded from GitHub.

See [updates.md](updates.md) for changelog/release/installer handling.

0.22 supplies a 24px logical checkbox drawable inside 38px rows rather than density-sized native glyphs. Labels and touch behavior retain CheckBox accessibility/state semantics. The opted-in persistent notification contains only Ella Monitoring with no body text, keeping its tap action. The launcher/notification icon uses the EQ reference's 108dp intrinsic size and padded artwork bounds. No APK is assembled by this source-only update; device appearance remains for owner verification.

Font remains a preset setting; in 0.25 its control moves from Display & App to Edit theme. Connection address/private token now belong to presets: snapshots remain encrypted/app-private and loading validates them before applying/reconnecting. Older presets and System default keep the existing connection. Changed editable Settings fields use Changed indicator colour and clear on matching save/restore. Theme colours remain separate.

In 0.24 element highlight/hidden-content preferences are preset state, including compatible conversion of older gauge-highlight choices. Theme edits still change only the Theme indication. Foreground live and catalogue/status work use separate cancellable workers; backgrounding/reconnecting cancels both and rejects old callbacks. No background polling is added.

Keep screen awake, Full screen and Lock retain their current app-wide states when loading any preset or System default. They are excluded from preset storage and changed-item indication; ordinary preference persistence remains.
