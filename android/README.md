# Ella Monitoring Android preview

Version **0.1.0-preview** (build 1). Native Java viewer for the existing [Pi collector 0.7.1 API](../node.js/API.md). Minimum Android 8.1 / API 27; compile/target API 37. The Pi remains the always-on collector and logger. This build does not require a collector update.

## First connection

1. Install the development preview APK. It is a separate app named **Ella Monitoring Preview**, package `com.colonellp.ellamonitor.preview`; production identity/signing are still to be established.
2. Join the same Wi-Fi as the Pi. In **Settings**, enter `http://PI_HOST:8080` (replace the placeholder with the Pi's LAN address) and the private 64-character token from `PicoData/api.json`. On older Android, use a numeric address if `.local` resolution is unavailable. The Pi API must listen on its LAN interface.
3. Tap **Save & connect**. Android 17 asks for local-network access; grant it. The app opens connections through an available Wi-Fi/Ethernet network so a phone's mobile-data default route does not take over a local request.
4. Confirm independent **Pico live / SBMS live** status and compare readings with the existing dashboard. Check source signs/units before relying on directional charts.

The API token is encrypted with an Android Keystore AES-GCM key in app-private preferences. It is not placed in URLs/logs/exports, and connection settings are excluded from backup/device transfer. Clearing app data requires re-entering credentials. HTTP sends the token in plaintext on the LAN; HTTPS preserves normal certificate/hostname checks and supports a user-installed private CA. There is no trust-all certificate mode or redirect forwarding. No private endpoint, token, actual readings or inventory is included in source.

## Interface

- Landscape at 800dp or wider preserves the reference grouping: load list at left; flags and the two-row load/PV/battery/SOC comparisons in the centre; environment/tanks at right; temperatures at far right; battery details/cells below. Narrower screens stack the same groups vertically. Scroll as needed; bottom controls remain available in a horizontal strip.
- Tap an indicator for a snapshot and available recorded summaries; **Hour / Day / Week / Month** summaries use retained data and coverage. **Chart** opens its history. Flags and unlogged elements remain live-only. The Pico load sum covers selected load metrics only, using each verified polarity to orient consumption consistently; it is unavailable if a constituent/current/sign is unknown. Individual readings retain original signs. No physical main-shunt alias is added or summed into the battery comparison.
- **Settings → Edit labels** renames elements/headings; long-press is a shortcut. Restore original labels from the editor. Aliases do not rename a device, change units/source bindings or rewrite history. Selected metric IDs retain their database binding; raw Pico aliases include available configuration metadata, not sensor position alone. This is a conservative binding check, not proof of permanent hardware identity.
- **Theme** switches dark/light palettes. **Full screen** hides system bars; a swipe reveals them. **Keep screen** applies only while the app is foregrounded. FYT detection uses the advertised toolkit and binder descriptors, with Main module 0 / command 13 / update 36. The legacy wake-lock workaround is restricted to detected FYT devices on Android 8.1/9; phones use normal Android screen flags. Timeout restoration follows the EQ app's 30-second minimum/fallback and still needs head-unit testing, including interaction with the EQ app.

## History and exports

- **6h / 12h / 24h / Week / Month** are rolling views by default; Month means **30 days** in this preview. Electrical resolutions are minute / hour / day respectively; pressure/temperature use available hour/day data. History refreshes approximately every 30 successful live polls while its screen is open, or with **Refresh**. Previous/Now/Next navigation is available.
- **Defined UTC** uses midnight-aligned 6/12-hour blocks, calendar days, Monday-based weeks and calendar months. These are explicit preview defaults for review. Optional local clock display applies the device timezone/BST rules to labels, without changing UTC buckets or reconstructing local-midnight totals.
- Quantities keep their units: mean W/A/V, last recorded SOC, and recorded Wh/Ah per period. Voltage/SOC use lines. **Bars** selects Wh/Ah, with green in/amber out only when channel polarity is verified. Load channels reverse the forward/reverse legend appropriately; battery bars describe **net** battery flow, not simultaneous gross charging and consumption. Unknown polarity shows unclassified signed net values. Temperature offers last/low/high where those statistics exist.
- Missing periods are gaps; null is not zero. Tap a recorded interval for its source, value, partial state and coverage. Rolling cutoffs select intersecting **whole UTC buckets**, explicitly labelled; no proportional energy estimate is presented as exact. Short-data retention, outages and incomplete checkpoints limit available summaries. Environmental points and SOC are recorded readings, not continuous coverage guarantees.
- **Settings → Export current chart range** or the chart's export button writes CSV through Android's file picker. Load a chart first. It exports the selected metric/range's saved statistics, source IDs/original names, edited quantity label, UTC boundaries, units, extrema, signed/directional energy, coverage and partial/edge markers. Credentials are excluded. User labels are escaped for spreadsheet safety. It does not export a database backup or nonexistent raw historical samples.

The app polls live data only in the foreground, about one second after each completed request. API failures clear current values; client monotonic ageing independently expires Pico/SBMS at the collector's default 2/3-second limits. A privately configured longer freshness threshold on the Pi does not extend the app's conservative limits. Background/closed devices continue logging on the Pi; this preview includes no alerts, offline cache or remote access.

## Build

Source is in [source/](source/). Install JDK 17 and the Android SDK platform `platforms;android-37.0` plus Build Tools 36.0.0 using Android Studio/SDK Manager. Set your local `ANDROID_HOME` or private `source/local.properties`; do not commit SDK paths. The Gradle wrapper pins 9.6.0 with its distribution checksum, and AGP is pinned to 9.4.0.

```bash
cd android/source
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

The development APK is `app/build/outputs/apk/debug/app-debug.apk`. Keep development signing material private. This preview uses development signing; it does not establish a production signing identity, publish a GitHub APK release or enable self-updates. Rebuilding elsewhere with another debug key will require uninstalling the existing preview (which clears its settings), so final signing must be settled before regular updates.

The app adds no third-party runtime dependency: HTTP/JSON, UI, storage encryption and plotting use Android/Java APIs. JUnit, a test-only JSON implementation and Robolectric are test dependencies. The standard Gradle wrapper retains its generated Apache 2.0 notices. FYT screen behaviour is adapted from the owner's [EQ app reference](APP-PLAN.md#source-references-for-reuse); no audio/DSP operations or vendor implementation/assets are copied. No SignalK/Pico protocol port is distributed inside this APK.

See [CHANGELOG.md](CHANGELOG.md), [BUILD-VALIDATION.md](BUILD-VALIDATION.md) and [NEXT-BUILD-CHANGES.md](NEXT-BUILD-CHANGES.md). Actual phone/head-unit/Pi LAN, certificate trust, Android 17 permission prompts, recovery, screen handling and visual fit still require device verification. GitHub update download/install, production signing/releases, background alerts, cloud relay, direct Pico/MQTT mode and Simarine settings are subsequent stages.

References: [Android 17 SDK setup](https://developer.android.com/about/versions/17/setup-sdk), [AGP 9.4 compatibility](https://developer.android.com/build/releases/agp-9-4-0-release-notes), [local-network permission](https://developer.android.com/privacy-and-security/local-network-permission), [Android Keystore](https://developer.android.com/privacy-and-security/keystore), [network security configuration](https://developer.android.com/privacy-and-security/security-config), [document creation/export](https://developer.android.com/training/data-storage/shared/documents-files).
