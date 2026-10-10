# Settings and persistence

Updated in **0.2.0-preview / build 2**.

Settings is a grouped two-column page reachable from the bottom gear: Connection, Display & App, Theme & Appearance, Labels & Export. It retains manual origin/token setup, encrypted Android Keystore token storage and foreground connection restart. The credentials page prevents screenshots and token view-state saving. Hide-title changes only the title; status/navigation remain. Local clock changes display timezone only. Label editing/export preserve the original APIs and source/history behaviour.

Persistence is opt-in and follows EQ's return-to-app notification behaviour. A low-importance ongoing notification appears while backgrounded and tapping it returns to the app; it is removed while visible or when persistence is disabled. Android 13+ requests notification permission. The non-exported service declares Android's specialUse foreground-service type/subtype for current targets. It does not poll the API, collect data, acquire a background wake lock or deliver alerts. START_STICKY allows system restart, but firmware/user battery/task restrictions still require device validation. Pi collection/logging continues independently.

Foreground keep-screen/fullscreen behaviour and detected FYT service capability are unchanged. The first preview's development signing key could not be recovered: 0.2.0 uses a retained private replacement preview key and requires reinstalling 0.1.0, clearing settings. Production signing and updater/release policy remain pending; keys are never stored in GitHub.
