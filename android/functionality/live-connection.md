# Live connection and rendering

Updated in **0.24 / build 6**.

The app polls /api/v1/live in the foreground, scheduling the next request one second after completion. Source freshness is determined independently by the collector's fresh/state fields. The app validates source age but no longer adds HTTP elapsed time to it or imposes its own Pico/SBMS 2/3-second source limits. This removes a duplicate cutoff that could blank SBMS despite a fresh API response. It does not establish an MQTT fault or change collector stale/repeat handling.

An additional three-second HTTP snapshot bound starts before the live request, including request latency. Genuine collector-stale/disconnected sources and expired HTTP snapshots show dashes. A failed live request retries after one second and keeps the preceding snapshot only until this bound. The app does not invent zeroes or retain readings indefinitely.

Catalogue/status fetches use a separate cancellable worker and client, initially and roughly every 30 successful live polls (or when catalogue is missing). Their latency/errors do not block live HTTP requests or clear live values. Lifecycle/connection generations reject obsolete callbacks and cancel both clients.

Missing source fields retain existing view bindings as unavailable; only genuinely new bindings rebuild the dashboard. Removed raw configuration fingerprints are replaced when that sensor has a new binding. Receipt timestamps update even when the numeric value is unchanged. Unchanged values/flags reuse their existing background and avoid unnecessary text/gauge redraws; explicit theme/preset changes repaint the existing views.

Synthetic checks cover collector freshness near the former SBMS cutoff, independent source staleness, cached snapshot expiry, unchanged view identity and delayed/failing catalogue requests alongside live updates. Actual MQTT cadence, Pi freshness configuration, prolonged foreground/background operation and reported head-unit dropouts still require device verification.
