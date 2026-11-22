# Wi-Fi fault diagnostics

Standalone helper **1.4** supports `sta-only` and `ap-sta` topologies. Collector
and Android versions are independent. The owner confirmed 1.4 installed and active in STA-only observe mode on November 12, with its journal follower running and the new local health endpoint responding. Collector 0.7.6 introduced a separate configuration regression and 0.7.7 still acquired no Pico data. The owner subsequently confirmed collector 0.7.8 restored fresh Pico and SBMS readings. Sustained stability and subsequent checkpoint advancement remain to verify. Monitor 1.4 needs no reinstall. The owner confirmed 1.3 installed and active
in STA-only observe mode after returning to built-in STA operation: AP checks and
recovery disabled, firmware-console diagnostic flag disabled, local API listener
responding. Sensor peer discovery, incident capture and unattended stability
remain to verify; an empty initial peer list does not establish sensor failure.

## Current STA-only setup

The owner has returned the built-in radio to NetworkManager client mode, removed
the USB client radio, and moved both sensors back to the upstream router. AP
services/recovery and automatic uplink switching are disabled. Sensor logging
continues independently. Do not re-enable the former AP profile or run the
hotspot migration helper's `rollback`: it restores a NetworkManager AP, not STA.

Download only the monitor source; the collector updater is unnecessary here:

```bash
curl -fL https://raw.githubusercontent.com/colonel-lp/PicoData/main/node.js/tools/ap-watch.py -o ~/ap-watch.py
sudo python3 ~/ap-watch.py install --topology sta-only --uplink STA_INTERFACE
sudo ella-ap-watch status
```

Substitute the actual wireless client interface. This preserves existing private
AP configuration/target MACs and all incident archives, removes the monitor's AP
startup hook, installs/starts only the monitor and forces `observe` mode. It does
not change NetworkManager, routes, power saving, sensor settings, broker settings,
collector service or history. Existing verbose Broadcom console logging is
restored to its saved pre-monitor value when available; normal kernel errors
remain recorded. System journal/broker log configuration is not changed.

STA-only never queries hostapd, checks AP clients/firewall, constructs an AP
recovery policy or restarts a network service. The `recover` command is rejected.
`observe` alone on an old installation is **not** a substitute for STA-only.

For sensor reachability, known target MACs from the old AP installation are
matched against the current STA neighbour cache; only current, directly routed
IPv4 addresses are probed. Old AP IPs are never reused. Discovery is passive and
may be incomplete, especially with router client isolation. Explicit optional
addresses can be configured during install:

```bash
sudo python3 ~/ap-watch.py install --peer-target SENSOR_ONE_IP --peer-target SENSOR_TWO_IP
```

This replaces the explicit peer list (maximum four). Peer pings are supplementary:
a never-responsive client is not declared faulty simply because it blocks ICMP.
Collector `stale`/`retrying` events and broker disconnects still trigger capture.
No MQTT payload subscription, credentials, authenticated API read or sensor
firmware access is added. A refused/timed-out connection is evidence, not proof
of an internal SBMS firmware fault. Connection/disconnection/timeout/duplicate
client events can locate a transport problem; they cannot diagnose battery
protection flags or cell faults.

Routine health is in RAM (target 1 second); bounded radio/gateway/peer checks run
about every 10 seconds. Relevant journal/broker events are read continuously.
Faults retain available **60 seconds before and after**, with immediate prefix
save and bounded checkpoints; the latest **five compressed archives** survive
reboots. The first minute after startup cannot have a full pre-event window.
Normal `/var/log` persistence remains subject to the existing zram sync policy;
incident archives are stored separately under `/var/lib/ella-ap-watch/`.

```bash
sudo ella-ap-watch status
sudo ella-ap-watch faults
sudo ella-ap-watch capture
sudo ella-ap-watch export --output "$HOME/wifi-fault.json"
sudo journalctl -u ella-ap-watch.service -n 20 --no-pager
```

The export is private and contains the latest incident only; use a new filename.
No automatic STA recovery, internet polling or speed tests are performed.
With collector 0.7.6, loopback HTTP health also reports current source freshness,
logger state/checkpoint and recent LAN request diagnostics. Required API/service/
source/logger failures can trigger after 60 seconds even without prior success;
an unchanged checkpoint triggers after 120 seconds plus confirmation. Disabled
sources are excluded. Old collectors returning 401 and TLS TCP-only probes still
prove availability only; check their freshness separately with `api-check.js`.

## Future USB AP adapter

Keep this helper rather than install another monitor. After the new hardware is
configured and the AP/sensors are working, explicitly select AP topology and
its actual interfaces/paths:

```bash
sudo python3 ~/ap-watch.py install --topology ap-sta \
  --interface AP_INTERFACE --uplink STA_INTERFACE \
  --unit HOTSPOT_SERVICE.service --control /run/HOSTAPD_CONTROL \
  --target SENSOR_ONE_AP_IP --target SENSOR_TWO_AP_IP
sudo ella-ap-watch observe
```

The installer verifies a responsive AP before switching topology. Existing
observe mode is preserved; automatic recovery requires the separate deliberate
`recover` command once the new installation is verified. Reinstallation without
topology/interface arguments preserves the current setup, so a source update
cannot silently turn a STA-only installation back into an AP monitor. AP/STA
policy learning is separated by topology/interface/target configuration.
This does not configure the adapter or migrate the network itself.

## AP topology: update an existing monitor

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
hotspot and collector. Check version **1.3**, recent Wi-Fi diagnostic samples,
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

## Evidence reference (AP entries are skipped in STA-only)

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

## AP-only recovery policy remains conservative

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

From `node.js`, run:

```bash
python3 test/ap-watch.test.py
python3 test/wifi-flight.test.py
python3 test/sta-watch.test.py
```

Version 1.3 passes **84 offline tests**: 40 AP recovery/I/O/installer regressions,
36 recorder/window tests and 8 STA topology tests. They use synthetic addresses,
isolated files, mocked radio/service actions and a real systemd unit parser.
STA tests cover migration without radio changes, fresh install, retained archives,
no AP event/policy construction, firmware verbosity restoration, current neighbour
IP discovery, gateway/peer fault captures, broker disconnect/SBMS stale triggers,
forbidden recovery and switching back to a differently named USB AP interface.
No APK, collector release or live hardware test is part of this helper update.

Actual RAM/CPU cost, firmware output, device/service integration and behavior
during a real stall remain owner verification. Do not disconnect live sensors or
force a stall merely to demonstrate these tests.



## Updating an already configured monitor to 1.4

After the normal collector source update:

```bash
sudo python3 ~/PicoData/node.js/tools/ap-watch.py install
sudo ella-ap-watch status
```

Omitting topology/interface options preserves the existing validated STA-only
configuration and observe policy. The collector updater itself does not install
standalone helpers. New source/logger checks require collector 0.7.6 and a
configured loopback HTTP probe; existing TLS probes remain TCP-only. This release
records current health when the monitor starts, including an already-failed
logger, rather than relying on a future journal transition. It does not recover
an earlier event's missing pre-start history or identify the previous glitch's
cause. Journal input is filtered to relevant units/kernel events; config rereads
are limited to once every ten seconds.
