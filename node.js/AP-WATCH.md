# AP and upstream Wi-Fi diagnostics

Standalone helper **1.2**, for the existing stock-hostapd installation. Collector
0.7.5 and Android version 0.32 are separate. The normal collector updater does
not install or activate this helper. Source checks do not mean it is installed:
the last owner-confirmed installation is 1.1.

## Update an existing monitor to 1.2

Use working SSH through the independent upstream connection, with the AP and both
sensors working:

```bash
curl -fL https://raw.githubusercontent.com/colonel-lp/PicoData/main/node.js/tools/ap-watch.py -o ~/ap-watch.py
sudo python3 ~/ap-watch.py install
sudo ella-ap-watch status
sudo journalctl -u ella-ap-watch.service -n 15 --no-pager
```

No-target install preserves private targets, AP paths, mode, recovery state and
original diagnostic settings. It restarts **only the monitor**, retaining the
hotspot and collector. Check version **1.2**, recent Wi-Fi diagnostic samples,
the correct AP/STA interfaces, local API probe configuration and independent
round-trip results. The existing two-minute recovery startup grace still applies.
Same-boot/config learned recovery and diagnostic baselines survive monitor-only
restarts; new boots/configurations require fresh healthy observations.

First installation requires Python 3.9+, systemd, `iw`, `ip`, `ping` and an active,
responsive hostapd service. No pip packages or driver replacement is needed.
With two to four always-on sensors connected, substitute their reserved hotspot
IPv4 addresses:

```bash
sudo python3 ~/ap-watch.py install --target SENSOR_ONE_IP --target SENSOR_TWO_IP
```

Targets must be distinct and directly routed through the AP, with known MACs in
its DHCP lease file/neighbour table. Default AP is `wlan0`, service
`ella-hotspot.service`, control path `/run/hostapd-ella`. The actual upstream
wireless interface is discovered from local interfaces/default routes; optional
`install --uplink INTERFACE` pins it when automatic identification is ambiguous.
No scan or internet speed test is performed.

## What is recorded and why

| Evidence | Target cadence | Diagnostic purpose |
| --- | --- | --- |
| AP and STA presence, carrier/state, byte/packet/error/drop counters | 1 second | Distinguish interface disappearance and traffic direction/progress |
| AP firmware `fc_xoff`, `fc_xon`, `f2txdata`, resolved through the AP's current phy | 1 second, if exposed | Correlate a possible firmware transmit/flow-control stall; a counter alone is not a diagnosis |
| Driver and USB runtime-power/autosuspend state | 1 second | Correlate USB device/power state changes without changing them |
| Local collector API listener response, latency and error/errno | 1 second | Separate local HTTP/listener trouble from remote radio reachability |
| AP station counters and independent sensor round trips | 10 seconds | Check both directions even when inbound sensor traffic continues |
| STA association, signal, rates, station retries/failures/beacon/drop counters | 10 seconds | Trace upstream-link degradation independently of the AP |
| STA default route and interface-bound local gateway probe/RTT | 10 seconds | Separate uplink/local-router reachability from internet service |
| Collector service state/PID/restart count | 10 seconds | Correlate process loss/replacement with connection failures |
| Relevant kernel/USB/SDIO, hostapd, NetworkManager/supplicant, DHCP, collector, broker and selection-helper journal events | As emitted, bounded | Preserve reported errors, disassociation, resets and source-state transitions around a fault |
| New Mosquitto file-log messages, when `/var/log/mosquitto/mosquitto.log` is readable | 1 second, bounded | Preserve broker socket/malformed-packet/timeout evidence when it is absent from journald |
| Kernel/service journal, addresses, routes and hotspot firewall snapshots | At fault and during its tail | Preserve configuration and event context before recovery changes the scene |

Cadences are targets, not deadlines guaranteed by Linux. Slow commands run in a
separate worker and have time/output bounds. Samples include actual times,
query outcomes, slow-sample age and the largest health-sampling gap. Unsupported
counters are explicitly unavailable, never invented as zero. The recovery-policy
loop remains separate at about 30 seconds.

The installer discovers only the collector's public bind/port/TLS properties from
its existing local configuration; tokens and TLS keys are never copied or used.
For HTTP, an unauthenticated `/api/v1/status` response normally returns **401**:
that proves HTTP listener/event-loop availability, **not source freshness**. TLS
installations use TCP-only reachability, without bypassing certificate checks.
If automatic discovery is unavailable, status says not configured. Set the known
local HTTP port explicitly with `install --api-port PORT --api-http`, or omit
`--api-http` for TCP-only. Source freshness comes from collector state events and
the usual authenticated API check, not this credential-free availability probe.

The startup hook retains the existing AP power-saving-off and hostapd DEBUG
settings, without `-K`, password reads or a hotspot restart. Version 1.2 also adds
brcmfmac's firmware-console debug bit `0x100000` when supported. This enables
firmware messages; it does not replace firmware, reload a driver or change radio
security. The previous flag is saved for restoration. Status reports flag
availability. To restore the previous firmware verbosity while retaining other
recording, re-run `install --no-firmware-console`. DEBUG/firmware messages can
increase journal traffic; actual hardware resource/volume measurements remain
required. Journald's storage policy is unchanged.

## Automatic private fault windows

A time-based RAM buffer holds up to 180 seconds of context, with separate bounded
health/event queues so journal noise cannot evict health samples. After learning
healthy behavior, repeated local API, interface, sensor round-trip, STA
association/route/gateway or collector-service failures trigger a saved incident.
High latency after a learned low-latency baseline, process replacement, relevant
kernel radio/USB errors, collector stale/retrying events and critical monitor
errors also capture diagnostics. A client which has never answered ping is not
silently treated as ping-capable. These triggers save evidence; they do not add
new radio-reset criteria.

Each incident records first observed onset separately from confirmation. It
freezes the available preceding **60 seconds**, immediately writes a durable
private prefix, then collects through **60 seconds after onset**. The pending
file is checkpointed every ten seconds during that incident and finalized as
compressed JSON. Recovery does not wait for that tail. Repeated symptoms are
grouped into one episode; the diagnostic latch re-arms after a quiet minute.
The original prefix is retained even if the live RAM ring rotates.

Completed incident archives live in `/var/lib/ella-ap-watch`, with root-only
permissions. Retention is **latest five completed incidents**, each at most
**2 MiB uncompressed**, plus one pending incident and temporary atomic-write
space. It is count/size bounded, not a promised number of hours. Routine health
samples remain in RAM; no per-sample SD-card logging is added. The older two-file
RAM event ring and bounded monitor-error records remain available (one persistent
error record, at most 16 KiB, updated at most once per five minutes per boot).

Archives include UTC, monotonic time and boot identity, explicit short/truncated
context, sampling gaps and event-drop counts. A monitor restart recovers the
already-saved prefix as **interrupted**, without inventing post-crash data. A
cold start cannot supply preceding context it never collected. Sudden power
loss/kernel freeze before confirmation can lose the RAM prefix; no crashed
machine can record the following minute. Continuous durable checkpoints or an
independent receiver would be needed for that stronger guarantee, with extra
writes/resources. Storage failure is reported best-effort; no logger can
promise saved evidence on inaccessible storage.

Private Pi reports contain network names, MACs, addresses and diagnostic messages.
Credential-like fields in journal messages are redacted; configuration secrets,
HTTP headers/bodies and sensor readings are not deliberately copied. Review
reports privately rather than posting them to the repository. Firmware may still
fail silently: this evidence narrows the fault boundary but cannot guarantee a
root cause.

## Recovery policy remains conservative

After two minutes of startup grace, each always-on sensor must have been observed
alive on this boot/configuration. A validated same-boot baseline survives a
monitor restart. Existing inbound traffic **or** a valid interface-bound ping
counts for recovery safety; independent failed round trips are separately
reported as `rx-present-roundtrip-failed` rather than `traffic-ok`.

Only the existing global-stall/control-failure policy can restart the existing
hotspot service: no confirmed client traffic for at least 90 seconds, correct
routes, no responsive sensor, saved restart accounting and the established
cooldown/latch. One responsive sensor suppresses a whole-AP restart. An isolated
sensor, app, MQTT or internet failure does not reset a radio. Inactive hotspot
services pause recovery. Deliberately powering off all sensors can resemble a
stall; use observe mode for maintenance.

At most two AP recoveries per rolling 30 minutes, at least ten minutes apart; an
unresolved incident stays latched until 90 seconds of healthy observations.
Invalid recovery state inhibits action until explicit reset. The helper never
restarts the collector, upstream interface, NetworkManager or Pi, changes
addresses/NAT/profiles, reloads drivers or migrates sensors. Missed sensor samples
cannot be reconstructed by diagnostic logging.

## Retrieve evidence and operate

```bash
sudo ella-ap-watch status
sudo ella-ap-watch faults
sudo ella-ap-watch report
sudo ella-ap-watch export --output "$HOME/ella-wifi-fault.json"
sudo ella-ap-watch errors
sudo ella-ap-watch capture
sudo ella-ap-watch observe
sudo ella-ap-watch recover
```

`faults` lists saved and collecting/partial files. `report` prints the newest
incident. `export` writes the newest incident to a **new**, private file owned by
the invoking sudo user and refuses overwrites; choose a new filename for another
export. Retrieve that file through the SSH client's file transfer. A pending
report contains only the tail collected so far. `capture` requests the same
60-second tail while monitoring is active; with an inactive monitor it reports
an explicitly instantaneous snapshot. Commands do not upload files.

Mode changes take effect within a policy cycle; they do not cancel an already
started action. For uninterrupted maintenance:

```bash
sudo systemctl disable --now ella-ap-watch.service
```

After investigating/repairing an inhibited or latched incident,
`sudo ella-ap-watch reset` resets recovery state and requires a fresh baseline;
it does not restart the hotspot. `sudo ella-ap-watch uninstall` removes the
monitor/startup hook and restores saved diagnostic settings, including firmware
verbosity where available, without restarting AP/collector. Incident files remain.
The hotspot installation/rollback helper is separate.

## Validation and limits

From `node.js`, run `python3 test/ap-watch.test.py` and
`python3 test/wifi-flight.test.py`. Version 1.2 has **40 existing recovery/I/O/
installer tests plus 36 new focused diagnostic tests**. These use synthetic
interfaces/addresses, isolated files, mocked radio/service actions and a real
systemd unit parser. They cover independent pings, current-phy mapping, loopback
401/TCP-only probes, learned faults, slow-worker isolation, frozen prefixes,
complete/partial tails, restart recovery, bounds, redaction, firmware debug-bit
restoration, bounded broker-file following/rotation and private non-overwriting export. No live Pi/network action runs.

Android's separate same-version 0.32 source correction adds call IDs/address
families and private automatic app incident files. Rebuild it in Android Studio;
installing this Pi helper does not update the app.

Actual RAM/CPU cost, firmware output, device/service integration and behavior
during a real stall remain owner verification. Do not disconnect live sensors or
force a stall merely to demonstrate these tests.
