# Live connection and rendering

Updated in **0.31 / build 13**; earlier live-rendering behavior below is retained.

The app polls /api/v1/live in the foreground, scheduling the next request one second after completion. Source freshness is determined independently by the collector's fresh/state fields. The app validates source age but no longer adds HTTP elapsed time to it or imposes its own Pico/SBMS 2/3-second source limits. This removes a duplicate cutoff that could blank SBMS despite a fresh API response. It does not establish an MQTT fault or change collector stale/repeat handling.

An additional three-second HTTP snapshot bound starts before the live request, including request latency. Genuine collector-stale/disconnected sources and expired HTTP snapshots show dashes. A failed live request retries after one second and keeps the preceding snapshot only until this bound. The app does not invent zeroes or retain readings indefinitely.

Catalogue/status fetches use a separate cancellable worker and client, initially and roughly every 30 successful live polls (or when catalogue is missing). Their latency/errors do not block live HTTP requests or clear live values. Lifecycle/connection generations reject obsolete callbacks and cancel both clients.

Missing source fields retain existing view bindings as unavailable; only genuinely new bindings rebuild the dashboard. Removed raw configuration fingerprints are replaced when that sensor has a new binding. Receipt timestamps update even when the numeric value is unchanged. Unchanged values/flags reuse their existing background and avoid unnecessary text/gauge redraws; explicit theme/preset changes repaint the existing views.

Synthetic checks cover collector freshness near the former SBMS cutoff, independent source staleness, cached snapshot expiry, unchanged view identity and delayed/failing catalogue requests alongside live updates. Actual MQTT cadence, Pi freshness configuration, prolonged foreground/background operation and reported head-unit dropouts still require device verification.

In 0.25 Pico temperature/pressure instruments keep their raw configuration fingerprint when catalogue/history bindings arrive. Retention compares physical source/sensor/quantity, rather than treating raw and catalogue IDs as different devices. It cannot append an obsolete unavailable Outside/barometer copy after a valid mapped one. Saved raw element preferences retain their IDs; existing catalogue-based preferences also remain usable. A changed raw configuration fingerprint does not inherit the old raw settings. Stale retention keeps metadata only, never old values, and the temperature sorter is unchanged.

Collector 0.7.2 uses each receiver's live stale deadline (default Pico 15 seconds, SBMS 30 seconds; SBMS follows sbms_stale_seconds) independently of maxGapSeconds integration limits. Rejected SBMS JSON cannot renew the last accepted reading, but one rejection does not clear a still-unexpired display. It immediately interrupts logging coverage. Actual source disconnects/stale states and the app's three-second cached HTTP bound still clear values. This corrects demonstrated software blanking paths; it does not prove or fix the separately reported device/Wi-Fi connection loss.


## Pi/network selection — 0.31 / build 13

The configured network role selects a runtime API endpoint directly, preserving the saved scheme/port/token and normal TLS verification. No SSH probe or credential is needed for viewing. The three buttons and exact SSID/host editing remain. Android network changes cancel old API work and invalidate callbacks and DNS/mDNS caches. Saved origin and preset comparisons do not change merely because the runtime route changes.

NetworkTransport uses network-scoped OkHttp with the app's bounded local-hostname resolver. Android 8.1 `.local` lookup queries only the configured name over mDNS; newer Android first uses its native resolver. The URL hostname remains the TLS identity. Multicast availability and reachability still depend on the actual network.

Pi status has its own foreground worker: GET /api/v1/pi every 15 seconds on Settings or 30 seconds elsewhere. Missing/stale resources become dashes after failure or 45 seconds. The live top bar shows the API-reported Pi upstream SSID, separately from the Android SSID, and follows Hide connection status/current theme. Resources never become sensor history or gate live source freshness. Open terminal separately launches ConnectBot. See [Pi control](pi-control.md).
