# Settings and persistence

Updated in **0.21 / build 3**.

Settings is the grouped two-column Connection, Display & App, Export and System page. Connection retains encrypted private-token/manual-origin setup and foreground reconnection. Credentials disable screenshots and token view-state saving. No connection-status line or theme controls appear here. Hide connection status applies to monitoring pages; the title is permanently removed. Local-clock display preserves UTC history boundaries. System groups version, Changelog / Update, update frequency and an explicit Back button returning to Live data.

Persistence remains opt-in, following EQ's ongoing return-to-app notification while backgrounded, with Android 13+ notification permission and the guarded specialUse service. It never adds background live polling, data logging, wake locks or alerts. Pi logging continues independently. Foreground keep-screen and detected FYT handling remain intact; fullscreen recalculates the canvas using the released bar space. Device/firmware restrictions and EQ interaction still require hardware validation.

The installed preview package and retained 0.2.0 development certificate are preserved so 0.21 installs as an update without clearing settings. A separate production identity is not introduced. Keys and local SDK/build outputs are excluded from GitHub.

See [updates.md](updates.md) for changelog/release/installer handling.
