# Settings and persistence

Updated in **0.22 / build 4**.

Settings is the grouped two-column Connection, Display & App, Export page with an untitled bottom-right control block. Connection retains encrypted private-token/manual-origin setup and foreground reconnection. Credentials disable screenshots and token view-state saving. No connection-status line or theme controls appear here. Hide connection status applies to monitoring pages; the app-wide title is removed. Local-clock display preserves UTC history boundaries. The bottom-right block has Updates: plus a reserved position in its top row, Changelog / Update and Back in its lower row, and the version at the bottom.

Persistence remains opt-in, following EQ's ongoing return-to-app notification while backgrounded, with Android 13+ notification permission and the guarded specialUse service. It never adds background live polling, data logging, wake locks or alerts. Pi logging continues independently. Foreground keep-screen and detected FYT handling remain intact; fullscreen recalculates the canvas using the released bar space. Device/firmware restrictions and EQ interaction still require hardware validation.

The installed preview package and retained 0.2.0 development certificate are preserved for compatible updates when the owner compiles with the same installed signing key. A separate production identity is not introduced. Keys and local SDK/build outputs are excluded from GitHub.

See [updates.md](updates.md) for changelog/release/installer handling.

0.22 supplies a 24px logical checkbox drawable inside 38px rows rather than density-sized native glyphs. Labels and touch behavior retain CheckBox accessibility/state semantics. The opted-in persistent notification contains only Ella Monitoring with no body text, keeping its tap action. The launcher/notification icon uses the EQ reference's 108dp intrinsic size and padded artwork bounds. No APK is assembled by this source-only update; device appearance remains for owner verification.
