# Pi controls

Implemented in Android **0.30 / build 12**. This page controls the existing Pi, not logging destinations or backup policy.

## Layout and appearance

Settings has a top-left NETWORK & PI control panel, PI CONNECTION and PI DATA panels below it, and the API CONNECTION editor plus DISPLAY & APP on the right. The existing lower-right update/back/version panel remains. Columns/panels have 6 logical pixel gaps, with a 6px content inset above the unchanged bottom bar. No Pi bottom-bar button is added. All application panels/buttons/labels/editors/confirmations use the current theme and font. Android-owned Wi-Fi settings/panels follow the Android system appearance.

## Device networks and endpoints

Hotspot, Home and Mobile are roles, not hardcoded installation SSIDs. Long-press a button to enter the exact saved SSID and Pi IP/hostname. Hosts accept no scheme, path, port, credentials, query or fragment; the configured API scheme/port/token/TLS checks are retained. Three entries are bounded/type-checked, duplicate nonempty SSIDs are rejected, and Cancel changes nothing.

The highlight follows the current device Wi-Fi SSID, not the requested network or Pi upstream. Android may redact SSID without location/Wi-Fi permissions and enabled location services; Wi-Fi access provides the user-controlled permission/settings route. Unknown SSID has no guessed highlight. Pressing a different button asks for confirmation; Android 8.1 tries an already saved network and newer platforms open system Wi-Fi settings/panel. A request is not reported as a successful connection. The app neither stores Wi-Fi passwords nor changes the Android target SDK to bypass restrictions.

An Android network change cancels old HTTP/SSH requests and invalidates their callbacks. The selected network's configured Pi endpoint is probed through that Android Wi-Fi Network using pinned SSH identity before using the existing API token at the new origin. The selected runtime API origin does not overwrite the saved origin or produce a modified-preset indication. Network SSIDs/hosts themselves are preset-owned under connection.networks; older presets have empty network definitions. SSH setup, pin and runtime Pi mode stay outside presets/themes.

## SSH and Pi controls

Use the existing SSH service and `/usr/local/sbin/pi-wifi` helper. The application does not install/change the helper, Pi sudo rules, collector configuration or network profiles. Status reads use fixed unprivileged Linux/NetworkManager commands. Auto/Home/M1 are the only remote mutations and require separate confirmation. They use `sudo -S` with the password on stdin, never in a shell command or command-line argument. A failed/disconnected mutation is unconfirmed until a new status read succeeds; it is not automatically repeated.

JSch 2.28.8 is pinned as the maintained SSH dependency, with its licence in the app assets. Android 8.1 platform crypto is used with ECDSA/RSA SHA-2 SSH host keys, avoiding legacy SHA-1 host keys. Pasted private keys need a format/algorithm supported by that platform/library combination; unsupported keys require conversion or password authentication. Do not silently downgrade cryptography. Credentials, private keys and passphrases are AES-GCM encrypted with the existing Android Keystore key, excluded from view-state saving/presets/exports; the SSH editor is screenshot-protected. Save is atomic and Clear saved credentials is staged until Save.

First use requires comparing and explicitly trusting the displayed SHA256 host-key fingerprint (or entering the verified fingerprint in SSH setup). For the selected ECDSA algorithm, compare with `ssh-keygen -lf /etc/ssh/ssh_host_ecdsa_key.pub -E sha256` on the Pi. An existing mismatched pin is rejected without an automatic replacement button. The same verified identity is used across all three network endpoints. No API token is sent to a newly selected endpoint until the SSH identity check succeeds. Explicit existing API-editor connections retain their prior behavior.

## Status and lifecycle

The Pi's actual upstream SSID comes from `iw dev wlan1 link`, separately from its NetworkManager profile, IPv4 address and `/etc/pi-wifi-mode`. Missing fields show dashes. RAM uses MemTotal/MemAvailable; CPU is the change in non-idle counters between same-boot samples, not the process lifetime average. The first/reset CPU sample is unavailable. Temperature uses the available CPU thermal reading. Collector/source/logging readouts continue to come from the authenticated read-only API.

SSH refresh is foreground-only: 15 seconds on Settings, 30 seconds elsewhere. The existing main status bar includes the confirmed Pi SSID, following its normal theme/font/visibility setting. Readouts expire after 45 seconds or a failed request. Work has bounded connection/read/command deadlines and a 16KiB output limit. Network changes/background/destroy disconnect transports, reject old callbacks and cancel refresh work. Application-owned credential/address editors dismiss when backgrounded. No new background notification/service polling is added.

Reachability still depends on the network topology. If the Pi leaves the upstream used by the Android device, join the monitoring hotspot to regain a common path. Address editing/hostnames do not bridge isolated networks.

## Validation limits

See BUILD-VALIDATION.md for this update's actual checks. Java parsing/JVM core tests do not establish Android SDK type/resource compilation, JSch packaging, runtime permissions, FYT saved-network switching, router behavior or SSH/sudo success on hardware. Those remain owner Android Studio/device checks.
