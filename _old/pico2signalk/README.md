# Pico2SignalK

Archived upstream reference (0.0.21), preserved when the active collector moved to the repository root `node.js/`. Use [the collector](../../node.js/README.md) for current Pi testing. The commands below refer to this archive; run them from `_old/pico2signalk/` at the repository root.

Reads Simarine Pico config and values and inserts them into SignalK.

This plugin is **Node.js only** — no Python runtime required. See [CHANGELOG.md](./CHANGELOG.md)
for migration notes and command-line testing instructions.

## Test from the command line

To verify Pico connectivity and dump the sensor config without SignalK:

```bash
node bin/dump-pico-config.js --pretty
```

With debug logging:

```bash
DEBUG=pico node bin/dump-pico-config.js --raw
```

Set `PICO_IP=x.x.x.x` to skip UDP discovery. Full options and environment
variables are documented in [CHANGELOG.md](./CHANGELOG.md).

## Plugin options

You can set the start instances of batteries, tanks, etc. in the SignalK plugin
config UI.
