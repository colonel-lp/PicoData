# Pi source updates and deployment evidence

Collector 0.7.8 restores the pre-0.7.6 collection/decoding files while retaining
independent API/diagnostic improvements. An already installed monitor 1.4 requires
no reinstallation for this collector recovery. Check `/api/v1/status` after updating;
the current source version alone does not prove fresh sensor data.

## Existing updater

The owner supplied `~/update-pico.sh` in the earlier monitoring chat. Its recovered
behavior is versioned in [tools/update-pico.sh](tools/update-pico.sh): temporary
sparse clone of this repository's `node.js`, copy into `$HOME/PicoData/node.js`,
`npm ci --omit=dev`, then restart `pico-mqtt.service`. Continue to invoke the existing
installed updater:

```bash
~/update-pico.sh
node ~/PicoData/node.js/bin/deployment-status.js
node ~/PicoData/node.js/bin/api-check.js --path /api/v1/status
```

No Git checkout is required at the destination. Parent `mqtt`, `api.json`,
`logging.json` and `history/` are outside its copy target. The versioned copy does
not overwrite the installed updater. Its established copy operation is not a
transactional release swap: files may already have changed if dependency install
fails, and removed upstream files can remain locally. The script stops on errors;
inspect the failure and rerun rather than assuming a completed update.

`SOURCE-MANIFEST.json` records SHA-256 for the shipped runtime source/package files.
`deployment-status.js` checks these and separately reports installed monitor,
zram-guard, updater and pi-wifi hashes where readable. It is a source-comparison
receipt, not a signature, process-memory verification, dependency audit or exact
inventory of extra files. No private configs or addresses are output. Keep receipts
private; helper hashes do not establish that an installed service is running that
source. A mismatch should be reviewed before restarting or replacing anything.

Monitor 1.4 must be installed separately after the source update:

```bash
sudo python3 ~/PicoData/node.js/tools/ap-watch.py install
sudo ella-ap-watch status
```

With no topology arguments the installer preserves the existing STA-only/observe
configuration. This does not enable the old AP or switcher. Updating the zram guard
or Pi control rules is not required by this collector update.

## Historical pi-wifi reference

[tools/reference/pi-wifi.sh](tools/reference/pi-wifi.sh) preserves the switching
logic recovered from the 2026-10-30 chat: initially `ella-uplink`, then renamed
`pi-wifi`, with connection profiles subsequently renamed locally. Formatting and
status text are normalised, and private SSID/profile values are replaced with
`REPLACE_...` placeholders. This is a historical reference, **not a ready-to-install
replacement** or a byte-for-byte copy of the installed private helper.

Its manual commands set auto/home/m1 policy and NetworkManager autoconnect flags.
Its periodic `check` targets **wlan1**, waits for two scans at signal >=40 before
switching home, and has a monotonic 300-second retry gate with router fallback.
The historical oneshot unit ran `/usr/local/sbin/pi-wifi check` after
NetworkManager, with `TimeoutStartSec=100`. Its timer used `OnBootSec=30`,
`OnUnitInactiveSec=60`, `AccuracySec=5` and `timers.target`.

Current operation uses the built-in STA radio, with the USB radio removed, AP
disabled and automatic switching timer disabled. **Do not install or enable this
historical helper/timer in that setup.** Keep the installed private originals and
unit files in the local backup. The collector updater does not touch them.

Before any future adaptation, make interface/profile/SSID values explicit in a
private configuration, validate the device/profile bindings, handle escaped SSID
fields correctly, and update recorded mode only after a confirmed successful
manual switch (the original records requested policy before activation). These
are dormant-helper limitations, not a demonstrated cause of the monitoring glitch.
Replacing or enabling the network switcher is outside this collector release.
