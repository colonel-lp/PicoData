# Pi system status and fixed mode controls

Collector 0.7.5; Ella Monitoring source 0.31. The existing authenticated API now
serves whole-Pi resources. SSH is not needed for monitoring. Sensor collection,
SQLite history/retention, MQTT, AP services and the normal updater are unchanged.

## Update and check status

Use the existing Pi-local updater:

```bash
~/update-pico.sh
node ~/PicoData/node.js/bin/api-check.js --path /api/v1/pi
```

The installed PicoData folder does not need to be a Git checkout. The updater's
copy target remains only node.js; parent api.json, logging.json, mqtt and history
are preserved. The API must already be configured. No new credential is needed.
Do not publish actual returned network/resource values or the private API file.

GET /api/v1/pi returns apiVersion 1 and a pi object:

| Field | Meaning |
| --- | --- |
| sampledAt / ageSeconds | UTC sample time and monotonic age of the cached sample. |
| ram.totalBytes / availableBytes / usedBytes | Whole-system MemTotal / MemAvailable, and their difference. |
| cpu.percent / load | CPU utilization between aggregate counter samples; 1/5/15-minute load averages. |
| temperatureC / uptimeSeconds | Available Linux CPU temperature and uptime. |
| network.interface / ssid / profile / address | Selected upstream interface, actual Wi-Fi SSID, NM profile and IP. |
| wifi.mode | Observed auto/home/m1 policy from the existing mode file, or null. |
| wifi.controlEnabled / controlState | Optional mode capability and idle/pending/succeeded/failed/unavailable state. |

The first/reset CPU sample is null, not zero. Missing/unreadable Linux fields stay
null. No resource values are inserted into the history database. One shared
10-second cache coalesces concurrent requests. Linux reads and two fixed bounded
network-status commands refresh on demand; they are not spawned per sensor frame.
The app refreshes in the foreground about every 15 seconds in Settings, otherwise
30 seconds, and expires old samples. Read-only status works without sudo or SSH.
The collector must be able to read the helper's existing policy-mode file to show
that mode; missing file uses the helper's auto default, unreadable/invalid is null.

Default-interface selection uses the current IPv4 default route. If the upstream
is IPv6-only or another interface should be observed, optionally put a validated
interface name in private api.json under pi.uplinkInterface. This selects a readout
interface; it does not alter routing, Wi-Fi or IP addresses.

## Optional Pi Auto/Home/M1 buttons

Mode changes use one fixed POST route, disabled unless explicitly enabled locally:

```json
{"pi":{"allowWifiControl":true}}
```

Merge this into the existing private api.json; never replace its token, bind/TLS
configuration or other settings with this small fragment. Prefer the installer
below, which retains the existing file and creates a private original backup.

The existing /usr/local/sbin/pi-wifi helper must already be installed and working.
Run this one-time setup with the account running pico-mqtt.service, not an
invented account. For a service installed under your current login:

```bash
sudo python3 ~/PicoData/node.js/tools/pi-api-controls.py install \
  --user "$USER" --api-config ~/PicoData/api.json
sudo systemctl restart pico-mqtt.service
```

The installer validates the existing root-owned helper and sudoers syntax, then
permits that service account to run exactly pi-wifi auto, pi-wifi home and pi-wifi
m1 without prompting. It enables the private API flag without printing credentials
and preserves API file ownership/private mode. It does not change a network or
restart a service itself. The ordinary collector updater never installs this rule.
Its private backup is api.json.before-pi-controls; keep that file private too.

POST /api/v1/pi/wifi requires the same Bearer header as other routes, JSON content
type, a bounded Content-Length and exactly one mode field:

```json
{"mode":"auto"}
```

Only auto, home and m1 are accepted. Extra fields, duplicate fields, arbitrary
commands and other writes are rejected. No shell is used. Before accepting, the
API checks noninteractive privilege for the exact command and a root-owned,
non-group/world-writable helper. There is one pending action and a 30-second
request cooldown. Execution/output are bounded; failure stays in controlState
without failing sensor acquisition. There is no reboot, terminal or generic remote
execution endpoint. History routes remain GET-only; no SQLite writes are added.

HTTP 202 means accepted/queued, with confirmed false. The command starts after
that response. A later GET provides actual network/mode and command status;
acceptance is never reported as successful reconnection. A mode switch can remove
an upstream client's route; join the independent monitoring hotspot if necessary.
Queued actions are cancelled on collector shutdown; completed commands are not
replayed. Pending/result state is transient, not stored as sensor history.

To disable the API action and remove its exact managed rule:

```bash
sudo python3 ~/PicoData/node.js/tools/pi-api-controls.py remove \
  --user "$USER" --api-config ~/PicoData/api.json
sudo systemctl restart pico-mqtt.service
```

## Android terminal and hostname use

Open terminal launches the separately installed ConnectBot app. Long-press to
edit the SSH username and port. Authentication, keys and SSH host verification
belong to ConnectBot. No SSH password/key is sent by Ella Monitoring; old encrypted
SSH preferences are not reused for API access. Existing API credentials stay as
configured and monitoring does not wait for an SSH connection.

Keep the three Android network buttons and their individual host configuration.
They may all use the same configured .local hostname. Modern Android uses native
lookup where available. Android 8.1 has a bounded one-shot mDNS fallback scoped to
the physical local network, without requiring an API DNS-SD service advertisement.
Queries/caches are cancelled/cleared when the foreground network changes. The
logical URL hostname is retained for HTTPS certificate checks; DNS is not proof
of server identity. ConnectBot receives a resolved address for that network so
it need not provide the head unit's missing hostname resolver itself.

A hostname does not route between isolated home/mobile LANs. Multicast filtering,
local-network permissions and a stalled AP can still prevent discovery. Existing
numeric host configuration remains available. No Wi-Fi, DHCP, Avahi, router,
firmware or logging reconfiguration is performed by the app's resolver.

## Verification limits

Offline tests cover API authentication/body/action limits, cache/coalescing,
resource parsing/reset/null handling, fixed command policy and private installer
backup/permissions. Source/JVM checks are separate from Android SDK compilation,
Android 8.1 multicast, ConnectBot handoff, actual resource overhead and real Pi
mode-switch tests. The owner performs those live checks; none are implied by
source publication.
