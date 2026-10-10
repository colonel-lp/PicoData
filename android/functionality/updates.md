# Changelog and updates

Introduced in **0.21 / build 3** using the owner's EQ updater components.

Settings System opens the complete themed changelog, newest first, bundled from the exact android/CHANGELOG.md and refreshed from PicoData/main/android/CHANGELOG.md. Release checks use colonel-lp/PicoData/releases and accept only Ella-monitoring-vX.XX.apk assets from that repository. A commit/source folder is not an APK release; publication requires an attached signed APK. The development package/certificate remain the installed app's identity.

The saved automatic frequency is Off, 1hr, 3hr, 6hr, 12hr or 24hr, default 24hr. Off suppresses all automatic/startup checks; manual Changelog / Update stays available. Scheduling pauses while backgrounded and resumes with the retained last-check timestamp. Checks are bounded and failures leave the bundled/cached complete notes available.

Download & Install bounds the response/download, compares published size and SHA256 when supplied, requires the installed package, a newer versionCode and a compatible signing certificate, stages privately and exposes only the verified APK through a read-only URI provider. Android's unknown-app permission screen precedes installer handoff when needed. The final install remains Android-controlled. Pending downloads survive cancellation for retry; files/preferences are cleaned only after installation is confirmed by the installed version, or after a failed download requires cleanup. Interrupted staged downloads are not installed unverified.

Release titles/notes should carry the new version and Android versionCode. Full changelog entries remain maintained in android/CHANGELOG.md and bundled identically. Future artifacts increase by 0.01 and retain package/key. Synthetic tests cover selection, interval/Off, certificate comparison and confirmed cleanup; actual installer/permissions/future-release behavior still needs device verification.
