# Pi controls

Updated in Android **0.32 / build 14**, with collector **0.7.5**.

Settings keeps NETWORK & PI at the top left, PI CONNECTION and PI DATA below, and the API CONNECTION editor plus DISPLAY & APP on the right. The update/back/version area remains bottom right. The columns measure their complete contents, fill a taller viewport, and scroll when the footer cannot fit. Existing themes, button sizes, 6px column/panel gaps and bottom clearance remain. The API address and masked token editors have explicit vertical alignment/minimum-height handling within their existing rows; blank token input keeps the saved token. Settings screenshot protection remains.

## Three network buttons

Hotspot, Home and Mobile are editable roles. Long-press to edit the exact Wi-Fi SSID and Pi IP/hostname. Hosts contain no scheme, path, port or username; API scheme/port/token and normal TLS checks come from the Connection editor. Duplicate configured SSIDs are rejected. The highlight follows the Android device's actual SSID, separately from the Pi's upstream network. Unknown/redacted SSIDs have no guessed highlight.

Pressing another network asks for confirmation. Android 8.1 tries an already saved network; newer Android opens its Wi-Fi panel/settings. A request is not reported as a successful connection. Wi-Fi access requests identification permissions and opens Wi-Fi settings when ready. When Location is off, themed guidance offers Location or Wi-Fi settings directly; returning rereads the actual network. No Wi-Fi passwords are stored by the app.

Network changes cancel old HTTP work and invalidate replies, sockets and address caches. The selected runtime endpoint retains the saved API scheme/port and does not overwrite presets or create a changed-preset indication. Network SSID/host definitions remain preset-owned. A reachable common LAN/hotspot is still required; hostnames do not bridge isolated networks.

## Local hostnames

Enter a hostname such as `pi-name.local` in each appropriate network button's host field, or use `http://pi-name.local:8080` as the API address. Do not put `username@` in an API hostname. SSH username is separate.

Modern Android first uses network-specific system DNS. Legacy Android uses a bounded, foreground one-shot mDNS A/AAAA query for configured `.local` names, with a short per-network cache, multicast lock and on-link answer checks. Network/background changes close sockets and clear the cache. Only the configured hostname is queried; no service advertisement is required. OkHttp 5.5.1 supplies custom DNS while preserving the URL hostname and standard TLS certificate/hostname verification. Local discovery is not server authentication; the existing private token and HTTPS policy remain.

## Pi readouts and controls

Normal monitoring needs the collector API, with **no SSH setup or tunnel**. GET `/api/v1/pi` supplies whole-system RAM, first/reset-aware CPU utilization, load, available temperature, Pi upstream SSID/profile/address and Wi-Fi mode. Missing values show dashes. The collector coalesces reads into one ten-second cache with bounded fixed status commands. It never logs these resources to history.

The app refreshes Pi status every 15 seconds on Settings and 30 seconds elsewhere while foregrounded. Readouts expire after 45 seconds or a failed request. The main-page status bar includes the confirmed Pi SSID, following the existing theme/font/hide setting. Sensor/source/history polling remains independent of resource reads. Background/network changes cancel pending work and reject old callbacks.

Pi Auto / Home / M1 remain separate from Android network selection. They require confirmation and the optional fixed API action setup described in [PI-STATUS.md](../../node.js/PI-STATUS.md). The ordinary collector update enables readouts but does not install privileges or change modes. The separate local installer grants only those three existing `pi-wifi` helper commands and enables private API opt-in. An accepted request is queued, not proof of a completed connection; later status confirms the resulting mode/network. There is no generic remote command endpoint.

Update the Pi with the existing `~/update-pico.sh`, preserving parent private configuration/history. See the Pi guide for optional control install/removal and API checks.

## Open terminal

Open terminal launches the separately installed [ConnectBot](https://connectbot.org/download/) app. First use asks for SSH username/port; long-press edits these later. The current configured Pi host is resolved on the current network and passed in an explicit ConnectBot SSH intent. ConnectBot handles passwords, keys and host-key verification. No password, key or API token is sent through this intent. Missing ConnectBot produces a themed download prompt.

The former embedded JSch client/runtime dependency is removed. Previously saved private SSH credentials are retained privately but unused, not exported or imported into ConnectBot. SSH username/port and runtime Pi mode stay outside presets/themes.

## Verification

See [BUILD-VALIDATION.md](../BUILD-VALIDATION.md) for actual Node/Python/JVM/parser checks. Android SDK compilation, dependency packaging, Settings measurements, legacy multicast/routing/permissions, ConnectBot handoff and real Pi controls still require Android Studio and phone/Joying/Pi verification. Source tests do not establish radio stability or measured hardware overhead.

In 0.32, transient SSID redaction retains an endpoint only when the physical network, local route and saved connection remain the same. The visible network name/highlight is not inferred. Actual route/confirmed identity/endpoint/settings changes reconnect; private connection snapshots keep the selected origin and token together. GET recovery is enabled within its deadline, while mode POST replay remains disabled. Long-press Refresh opens the current-themed, RAM-only connection diagnostics with COPY / CLOSE. Normal Refresh and approved panel/button layout stay unchanged. See [live-connection.md](live-connection.md) for safeguards and validation limits.
