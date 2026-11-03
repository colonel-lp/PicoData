# Live connection and rendering

Updated in **0.33 / build 15**; earlier live-rendering behavior below is retained.

The app polls /api/v1/live in the foreground, scheduling the next request one second after completion. Source freshness is determined independently by the collector's fresh/state fields. The app validates source age but no longer adds HTTP elapsed time to it or imposes its own Pico/SBMS 2/3-second source limits. This removes a duplicate cutoff that could blank SBMS despite a fresh API response. It does not establish an MQTT fault or change collector stale/repeat handling.

An additional three-second HTTP snapshot bound starts when response headers arrive. Body transfer, parsing and queued UI delivery consume this bound; pre-header connection/recovery wait does not. Header receipt is a local timing boundary, not proof of exact server-to-client network transit time. The app does not require synchronized phone/Pi wall clocks. Genuine collector-stale/disconnected sources and expired HTTP snapshots show dashes. A failed live request retries after one second and keeps the preceding snapshot only until this bound. The app does not invent zeroes or retain readings indefinitely.

Catalogue/status fetches use a separate cancellable worker and client, initially and roughly every 30 successful live polls (or when catalogue is missing). Their latency/errors do not block live HTTP requests or clear live values. Lifecycle/connection generations reject obsolete callbacks and cancel both clients.

Missing source fields retain existing view bindings as unavailable; only genuinely new bindings rebuild the dashboard. Removed raw configuration fingerprints are replaced when that sensor has a new binding. Receipt timestamps update even when the numeric value is unchanged. Unchanged values/flags reuse their existing background and avoid unnecessary text/gauge redraws; explicit theme/preset changes repaint the existing views.

Synthetic checks cover collector freshness near the former SBMS cutoff, independent source staleness, cached snapshot expiry, unchanged view identity and delayed/failing catalogue requests alongside live updates. Actual MQTT cadence, Pi freshness configuration, prolonged foreground/background operation and reported head-unit dropouts still require device verification.

In 0.25 Pico temperature/pressure instruments keep their raw configuration fingerprint when catalogue/history bindings arrive. Retention compares physical source/sensor/quantity, rather than treating raw and catalogue IDs as different devices. It cannot append an obsolete unavailable Outside/barometer copy after a valid mapped one. Saved raw element preferences retain their IDs; existing catalogue-based preferences also remain usable. A changed raw configuration fingerprint does not inherit the old raw settings. Stale retention keeps metadata only, never old values, and the temperature sorter is unchanged.

Collector 0.7.2 uses each receiver's live stale deadline (default Pico 15 seconds, SBMS 30 seconds; SBMS follows sbms_stale_seconds) independently of maxGapSeconds integration limits. Rejected SBMS JSON cannot renew the last accepted reading, but one rejection does not clear a still-unexpired display. It immediately interrupts logging coverage. Actual source disconnects/stale states and the app's three-second cached HTTP bound still clear values. This corrects demonstrated software blanking paths; it does not prove or fix the separately reported device/Wi-Fi connection loss.


## Pi/network selection — 0.31 / build 13

The configured network role selects a runtime API endpoint directly, preserving the saved scheme/port/token and normal TLS verification. No SSH probe or credential is needed for viewing. The three buttons and exact SSID/host editing remain. Android network changes cancel old API work and invalidate callbacks and DNS/mDNS caches. Saved origin and preset comparisons do not change merely because the runtime route changes.

NetworkTransport uses network-scoped OkHttp with the app's bounded local-hostname resolver. Android 8.1 `.local` lookup queries only the configured name over mDNS; newer Android first uses its native resolver. The URL hostname remains the TLS identity. Multicast availability and reachability still depend on the actual network.

Pi status has its own foreground worker: GET /api/v1/pi every 15 seconds on Settings or 30 seconds elsewhere. Missing/stale resources become dashes after failure or 45 seconds. The live top bar shows the API-reported Pi upstream SSID, separately from the Android SSID, and follows Hide connection status/current theme. Resources never become sensor history or gate live source freshness. Open terminal separately launches ConnectBot. See [Pi control](pi-control.md).

## Transport and network continuity — 0.32 / build 14

NetworkTransport retains network-scoped DNS/sockets and default TLS hostname/certificate checks. Its GET client enables OkHttp connection-failure recovery within the existing nine-second call deadline. The shared pool/dispatcher is retained, while the POST client keeps recovery disabled and marks command bodies one-shot, so a Pi mode action is not automatically replayed even for an HTTP 503 Retry-After: 0 response. Redirects stay disabled. The existing one-second post-completion poll interval, request-start snapshot timestamp, source freshness rules and three-second snapshot cutoff are unchanged.

NetworkIdentity distinguishes the physical Network handle, local link/address/default-route signature, connection configuration and confirmed Wi-Fi name changes from transient name redaction. The last known name is used internally only to retain an already configured endpoint on the same route. UI highlights/readouts still use the currently reported name. A new route/handle discards that internal role; a known different SSID resets the connection, and newly available metadata reselects only if its resolved API origin differs. Foreground resumes rebuild clients; DNS caches/sockets still clear on lifecycle/network invalidation.

PrivateSettings captures connection fields from one SharedPreferences map snapshot. The API selection carries a network, origin and immutable encrypted connection snapshot together; clients reject a selection that no longer matches settings and decrypt that captured snapshot, preventing a newly loaded token being paired with an old runtime origin. Preset/connection/network edits trigger selection before reconnecting.

The original 0.32 release kept only 256 RAM diagnostic events. The same-version source correction below supersedes that storage behavior; transport and freshness rules above are retained.

## Diagnostic source correction — 0.32 / build 14 — 2026-11-02

The owner authorized immediate logging changes, without requesting another APK version. Rebuild the updated source in Android Studio to obtain them; previously installed 0.32 binaries keep their original diagnostic behavior.

Each request has a local call ID. Transport-attempt failures record address family (IPv4/IPv6/unresolved), stage, duration, error class and numeric errno when exposed. Final API request outcomes are separate and carry the same ID. An attempt followed by HTTP 200 is recovered within the call; it is not automatically an outage. No extra wire header or network/GET retry/POST replay behavior is introduced.

Keep the latest 256 visible events and an additional time-based RAM history (180 seconds, maximum 2,048 entries). Two consecutive final live failures or a real display-snapshot expiry automatically save a private fault window: available 60 seconds before the first observed failure/expiry through 60 seconds after it. Isolated recovered attempts remain context and do not create incident files. Repeated symptoms coalesce; re-arm follows a healthy minute. Foreground exit or process/activity loss produces an interrupted record, not a fabricated complete tail. Short pre-context and byte truncation are explicit.

A worker writes the prefix immediately, checkpoints every ten seconds only during an active incident and finalizes it. Retain the latest five completed files (256 KiB maximum each) plus one pending capture under the app-private files directory, independent of presets/themes/Downloads. Already-written pending records survive activity/process restart as interrupted evidence. No fixed-hours retention guarantee or continuous per-request disk logging is claimed. Sudden termination before a prefix is written can lose RAM context. Storage errors are reported without breaking live monitoring.

Long-press Settings → NETWORK & PI → Refresh, then **FAULT LOGS**, select a record and **COPY**. The short live trace remains available with Copy/Close. Files contain UTC/monotonic timing, call IDs, final/attempt results, snapshot/network transitions and availability/truncation flags, with no addresses, SSIDs, tokens, paths/query/metric identifiers, headers, bodies or exception messages. No background network polling or automatic upload is added. Source/API freshness, three-second snapshot cutoff, acquisition/history and all approved page layouts remain unchanged. Pi helper 1.2 separately correlates both radios and local collector availability; app traces alone cannot distinguish every remote listener, routing and radio failure.

Pure JVM checks cover identity, diagnostics and the ApiClient wrapper; Android Studio tests additionally exercise actual OkHttp recovery of a primed pooled socket and non-replay of a dropped POST. Those transport tests and real Android networking remain unrun here. The owner-reported Joying flicker and separate full AP stall have not been reproduced or assigned a proven cause by these source changes.


## Cancellation and snapshot correction — 0.33 / build 15

This supersedes the request-start timestamp and response-disconnect behavior in the historical 0.32 description. NetworkTransport.disconnect only marks cancellation and calls OkHttp Call.cancel. ApiClient's request worker releases its response in finally on every success/failure path; UI cancellation never drains or closes the body concurrently with a read. Short-lived popup, month, catalogue/status and full-history clients close their own transport after their work. The persistent live client remains separate. Pool/dispatcher close is idempotent, network/TLS/authentication remain scoped, safe GET recovery remains bounded, and POST command bodies remain one-shot/non-retrying.

ApiClient exposes elapsed time since headers using monotonic time. The live callback carries that boundary through body parsing and UI scheduling; a transfer already over three seconds expires on arrival. A failed request never refreshes the preceding snapshot. Collector-stale/disconnected data stays unavailable independently. This corrects a demonstrated display-expiry amplifier without lengthening the cutoff or claiming the underlying transport delays are solved.

History HTTP/JSON failures retain request stage/call identity; subsequent popup, solar-combination and month-list processing failures record separate redacted stage names. Cancelled operations are labelled cancelled and do not count toward repeated live-request failure captures. A successful month-availability scan may be reused in RAM for 60 seconds, at most 32 metric/field lists, within the same connection/calendar month; readings/failures are not cached there.

An uncaught crash records its exception class and up to twelve application stack locations, excluding messages, bodies, endpoints and credentials. It saves available context in the existing private fault store, labels the unavailable post-crash tail, waits at most 500 ms for the queued write, and delegates to the original Android handler. Recreation does not chain old activity handlers. A sudden kill, storage failure or exhausted wait still cannot guarantee a crash file; routine traffic remains in RAM with the existing count/size limits. Retrieval is unchanged: long-press Settings Refresh → FAULT LOGS → record → COPY.
