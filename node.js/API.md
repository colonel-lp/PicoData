# Local readings and history API — collector 0.7.2

The existing collector can serve authenticated, read-only JSON through Node's built-in HTTP/HTTPS server. No additional npm dependency, database server or service is added. Only the Pi opens SQLite; Android/browser clients request JSON. Existing collection, logging, MQTT JSON and retention are unchanged. The database remains schema 2; this update does not require a history migration or new logging selection.

## Enable and check

Update `node.js/` using [README.md](README.md#update-an-existing-flat-installation), preserving the parent `mqtt`, `logging.json` and history directory. Create a private API configuration once:

```bash
cd ~/PicoData/node.js
node bin/init-api.js --host 0.0.0.0
sudo systemctl restart pico-mqtt.service
node bin/api-check.js
node bin/api-check.js --path /api/v1/live
node bin/api-check.js --path '/api/v1/history/battery?resolution=minute&limit=3'
```

`init-api.js` creates parent `PicoData/api.json` with a cryptographically random 256-bit token and owner-only permissions. It refuses to overwrite an existing file and never prints the token. Its default host, when `--host` is omitted, is `127.0.0.1` (Pi-only). `0.0.0.0` accepts connections on the Pi's IPv4 interfaces; use the Pi's actual LAN address or hostname in clients. The default port is 8080; `--port PORT` selects another. Do not forward this service through the router.

Logging must be configured. No API listener opens unless parent `api.json` exists or `--api-config FILE` is given. `--no-api` disables it; it conflicts with `--api-config`. The existing systemd `ExecStart` needs no change. Invalid API setup/bind/TLS configuration fails visibly at startup; normal sensor output stays silent. Correct the configuration or use `--no-api` to continue without the listener.

The private configuration has `host` (an IPv4 bind address), `port`, `token` (64 lowercase hexadecimal characters), and optionally `tls`. To rotate credentials, replace the token with a newly generated 32-byte hexadecimal secret and restart the service. MQTT/API credentials are independent. Keep `api.json`, TLS keys, selections and measurements out of source control.

Every route requires this request header:

```text
Authorization: Bearer YOUR_PRIVATE_API_TOKEN
```

Tokens in URLs are not accepted. HTTP sends headers and measurements without encryption; use it only on a trusted local network. HTTPS is supported by adding certificate/key paths relative to `api.json`:

```json
"tls": { "cert": "tls/api-cert.pem", "key": "tls/api-key.pem" }
```

Use a certificate valid for the hostname/address requested by clients and configure client trust for a private CA/self-signed certificate. `api-check.js` uses normal certificate/hostname validation; Node's `NODE_EXTRA_CA_CERTS` can supply a private CA. Do not disable verification in a deployed client. TLS requires version 1.2 or newer.

To test from a Mac, supply the private token locally in the header and use the Pi's LAN hostname/address:

```bash
curl -H "Authorization: Bearer $PICO_API_TOKEN" \
  http://PI_HOST:8080/api/v1/status
```

Set `PICO_API_TOKEN` privately on that machine first; do not paste tokens into issues, shared commands or recordings. HTTPS uses the same routes and authentication.

## Routes

All routes accept GET only. JSON includes `apiVersion: 1`. Timestamps/boundaries are UTC ISO 8601; database responses also include `schemaVersion: 2`. Responses are not cached. No wildcard browser CORS access is enabled; native Android and command-line clients can request directly. A future web UI should use the same origin or an explicitly agreed proxy/origin policy.

| Route | Response |
| --- | --- |
| `/api/v1/status` | Collector version/start time, current UTC, separate Pico/SBMS state and freshness/last receipt, logging state/latest saved checkpoint and clock-step count. No credentials, database paths or device addresses. |
| `/api/v1/metrics` | Selected stable metric IDs, exact display/source names, units, kind, role, polarity, voltage reference, available resolutions and retention; combined battery field sources. Also current Pico sensor IDs/names/types for live display. |
| `/api/v1/live` | Fresh normalized Pico/SBMS snapshots and selected metric values. Missing/stale snapshots are null. Selected Pico electrical watts use the configured fresh voltage source, including SBMS pack voltage; raw sensor voltages do not override that selection. |
| `/api/v1/history/battery` | Combined battery intervals with named values, source references, independent coverage, net/directional energy, samples and input timestamps, matching `history.js --battery`. |
| `/api/v1/history/metrics/ID` | One selected metric's intervals, including other loads, temperature and pressure. Battery-member requests are compatibility views into combined storage, not duplicated rows. |

The full normalized live Pico snapshot includes decoded sensors before legacy MQTT name filtering. It can therefore contain aliases/hidden names; the selected `measurements` list and history use the canonical logging selection. Do not add physical-shunt aliases or overlapping monitor/load channels together as independent energy sources. Live API units/invalid handling follow the safe decoder; the legacy MQTT formatter/wire output remains unchanged.

### Live values and freshness

`pico` has `state`, `fresh`, `receivedAt`, `ageSeconds` and `readings` keyed by configuration sensor ID. `sbms` has those status fields plus `reading`, containing normalized `voltage`, `voltageStatus`, `stateOfCharge`, `current` and diagnostic `sourceTime`. `sourceTime` is not used for acquisition timestamps or calculations.

Since collector 0.7.1, `sbms.reading.flags` provides the monitor boolean states, including `CFET`, `DFET`, `OVLK`, `UVLK`, `EOC`, `IOT`, `LVC` and `CELF`. Keys keep the broadcast uppercase spelling. Use `true` for On and `false` for Off; a missing/nonboolean flag is `null` (unavailable), never Off. When SBMS data is stale/disconnected, the whole `reading` is null. These are live-only states: no flags are stored in history.

`sbms.reading.broadcast` preserves the complete accepted MQTT JSON in original names and units, including `time`, `soc`, `cellsMV`, `tempInt`, `tempExt`, `currentMA`, `ad2`, `ad3`, `ad4`, `heat1`, `heat2`, all original `flags` (including numeric `delta`) and additional publisher fields. Raw currents/cells remain mA/mV; the existing normalized `current` and `voltage` remain A/V. Raw optional fields are passed through, so use normalized `flags` for boolean display. No additional broadcast fields are automatically logged. Changed flags/auxiliary values within the same source-clock second are delivered; a complete repeat with reordered keys still does not renew freshness.

Freshness uses monotonic elapsed time and each receiver's live stale deadline (default Pico 15 seconds/SBMS 30 seconds). Logging integration still uses its shorter source-specific maxGapSeconds (default Pico 2 seconds/SBMS 3 seconds); display persistence adds no history coverage. Disconnect/configuration changes invalidate affected snapshots immediately. Retained/exact-repeat SBMS messages remain filtered by the existing receiver. An unavailable source's last receipt can still be reported, but its old values are not served as current.

Each selected `measurements` entry has `id`, `fresh`, `mappingValid`, receipt/voltage receipt timestamps and named `values`. Electrical values are `current` (A), `voltage` (V), `watts` (W), plus relevant-device `stateOfCharge` (%) for batteries. Voltage/temperature/pressure values are `voltage`, `temperature` (°C) or `pressure` (hPa). A live current and SOC can remain available when voltage/watts are null. Valid zero current produces zero watts when voltage is available. The freshness flag concerns the metric's own source; the fields/timestamps independently show cross-source voltage availability. A rejected configured Pico binding cannot supply selected live measurements.

### History queries

| Parameter | Meaning |
| --- | --- |
| `resolution` | `minute`, `hour`, `day` or `month`. Temperature/pressure allow only `hour` and `day`, defaulting to `hour`; other metrics/battery default to `minute`. |
| `from` | Inclusive **bucket start** cutoff. UTC string such as `2026-01-01T00:00:00Z` or with exactly three fractional digits. |
| `to` | Exclusive bucket-start cutoff, default current Pi UTC time. |
| `limit` | 1–1000 rows; default 500. |

Default windows end at `to`: minute 24 hours; electrical hour 7 days; environmental hour/calendar-day one calendar month; month one year. Explicit ranges can request retained older daily environmental/monthly electrical records. From must precede to. Invalid dates, offset/local timestamps, unknown/repeated query parameters and unsupported resolutions are rejected. Windows select whole intervals; integrals are not prorated for an unaligned cutoff.

Rows are oldest first. `next` is null at the end, otherwise a query object (`from`, `to`, `resolution`, `limit`) for the next page. Use it as URL parameters on the same route. It continues after the last returned interval without duplicates. Each response reads metadata/rows within one SQLite read transaction; multiple pages are separate snapshots, so refresh the current partial interval as logging advances.

Empty periods are omitted, not filled with zeros. Missing members inside a combined battery row remain null with zero coverage. `partial` means the interval had not ended at the database's `savedAt` checkpoint; `end` is the scheduled UTC bucket boundary. A nonpartial interval may still have gaps: inspect coverage rather than treating it as a full measurement. Current history can be approximately one commit interval behind live data.

Combined battery named values, source references, independent `coverageSeconds`, `energy`, `samples` and `lastReceivedAt` use [LOGGING.md](LOGGING.md)'s contract. Directional values remain null for unverified polarity. Per-metric electrical `values` remain `[average_W, average_A, average_V]`, with battery SOC fourth. Per-metric rows also expose named integrals/coverage/net and directional Wh/Ah. Environmental `value` is the last valid reading; daily temperature `min`/`max` are the agreed extrema. Historical units/derived power sources are provided by the catalogue/source references.

## Limits and failures

Requests are bounded to 16 concurrent connections, 8KiB headers, 2048-character URLs, five-second header/request/socket inactivity timeouts, 100 requests per connection and a 4MiB JSON response. A global token bucket allows approximately 10 requests/second with a 60-request burst. SQLite reads use a small cache and a 250ms lock timeout; the API never accepts SQL or filesystem paths. Range queries use indexed metric/resolution/start keys with a maximum 1001 fetched rows for pagination. No database integrity scan runs on every request.

| HTTP status | Meaning |
| --- | --- |
| 400 | Invalid time/range/query/body. |
| 401 | Missing/incorrect header token. |
| 404 | Unknown route/metric. |
| 405 | Write/non-GET method rejected. |
| 413 | Response exceeds the size cap; reduce `limit`. |
| 429 | Request rate exceeded; respect `Retry-After`. |
| 503 | History cannot currently be read. Internal database/path/credential details are omitted. |

Logging failures are visible in `/status` while live collection/MQTT continues. Shutdown closes the listener/connections/read handle before the existing collector flushes and closes its writer. The API does not alter database contents or retention.

## Validation and next stages

Automated checks cover authenticated HTTP/HTTPS, read-only SQLite, simultaneous writer/request operation, calendar ranges, pagination/gaps/partial records, valid zero PV2, source references, fresh-voltage power, stale/disconnected sources, configuration/token privacy and bounded/error responses. The actual collector CLI is exercised against simulated Pico/MQTT endpoints while comparing all MQTT payloads with the Python baseline. See [BUILD-VALIDATION.md](../android/BUILD-VALIDATION.md) for the completed suite and remaining Pi checks.

Test LAN requests/restarts on the Pi and privately measure request memory/latency alongside unattended logging before claiming hardware performance. Android implementation remains next; its target versions, screens, graphs, discovery, credentials and offline behaviour still need agreement. Future USB storage should include consistent SQLite backups. Local SD/Android/cloud destination and schedule/retention are undecided; automatic backup or Google Drive access is not implemented here.

References: [Node HTTP server](https://nodejs.org/download/release/v22.13.0/docs/api/http.html), [Node SQLite read-only connections](https://nodejs.org/download/release/v22.13.0/docs/api/sqlite.html), [Node TLS](https://nodejs.org/download/release/v22.13.0/docs/api/tls.html).


### Live deadlines in 0.7.2

API freshness uses the acquisition receiver deadline, independently of logging.maxGapSeconds. Defaults are Pico 15 seconds and SBMS 30 seconds; the existing sbms_stale_seconds option also sets the SBMS API deadline. An expired connected sample is reported as state=stale, fresh=false, with null readings/reading. Monotonic age and last accepted receipt timestamps remain truthful. Actual disconnects immediately clear readings.

One rejected SBMS JSON message neither renews nor immediately discards a still-unexpired accepted live reading. The receiver emits rejected and diagnostic events, and the collector CLI immediately ends that source's logging coverage. The next valid nonduplicate sample recovers normally. Retained messages and complete repeats cannot renew live freshness. Integration continues to use the unchanged configured maxGapSeconds; a longer display deadline adds no history duration/energy coverage. MQTT protocol errors and device/network connection loss are separate unresolved causes, not fixed by this display policy.
