# Next build changes

## Current authorized step: standalone Pico base 0.1.0

- [x] Start with updated Node.js discovery/configuration and TCP request methods.
- [x] Carry over the owner's sensor mappings, pitch/roll and Ella JSON calculations/formatting from Python.
- [x] Exclude MQTT transport and SignalK integration from this base.
- [x] Add fragmented TCP reads, error/retry handling, sender checks and clean shutdown.
- [x] Compare normal readings with the original Python functions and test simulated connection failures/recovery.
- [x] Provide recording, verification and replay commands for a live Pi test.
- [x] Support installation directly in `~/PicoData/node.js`, including a bundled Python comparison reference so deleted original folders are not needed.
- [ ] Confirm discovery, configuration and required sensor readings on the owner's actual Pico.
- [ ] Confirm real receive frame lengths/checksums; investigate any differences before the Android port.
- [ ] Confirm live readings against the existing Node-RED/Pico display and exercise Wi-Fi loss/Pico restart.

## Later Android work

- Start the Android port after reviewing the live Pi evidence; confirm target hardware, Android range, layout and background requirements first.
- Add ElectroDacus MQTT reception later. Assess an embedded broker early, retaining the Pi broker as fallback. The current step excludes the old Pico MQTT publisher.
- Keep SignalK functionality outside the Android scope.
