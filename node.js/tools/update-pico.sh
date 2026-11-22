#!/bin/bash
set -euo pipefail
pico_update_dir=$(mktemp -d)
trap 'rm -rf -- "$pico_update_dir"' EXIT
git clone --depth 1 --filter=blob:none --sparse \
https://github.com/colonel-lp/PicoData.git "$pico_update_dir"
git -C "$pico_update_dir" sparse-checkout set node.js
cp -a "$pico_update_dir/node.js/." "$HOME/PicoData/node.js/"
cd "$HOME/PicoData/node.js"
npm ci --omit=dev
sudo systemctl restart pico-mqtt.service
echo "Update complete; service restarted."
