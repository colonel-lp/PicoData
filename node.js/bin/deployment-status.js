#!/usr/bin/env node
'use strict';
const fs = require('node:fs');
const path = require('node:path');
const { createHash } = require('node:crypto');
const root = path.resolve(__dirname, '..');
function digest(file) { return createHash('sha256').update(fs.readFileSync(file)).digest('hex'); }
function inspect() {
  const manifest = JSON.parse(fs.readFileSync(path.join(root, 'SOURCE-MANIFEST.json'), 'utf8'));
  const mismatches = [];
  for (const [name, expected] of Object.entries(manifest.files)) {
    if (path.isAbsolute(name) || name.split('/').includes('..')) throw Error('Invalid source manifest path');
    try { if (digest(path.join(root, name)) !== expected) mismatches.push(name); }
    catch { mismatches.push(name); }
  }
  const helpers = {};
  for (const [name, installed, source] of [
    ['monitor', '/usr/local/sbin/ella-ap-watch', 'tools/ap-watch.py'],
    ['zramGuard', '/usr/local/sbin/ella-zram-start-guard', 'tools/zram-start-guard.py'],
    ['piWifi', '/usr/local/sbin/pi-wifi', null],
    ['updater', path.join(require('node:os').homedir(), 'update-pico.sh'), 'tools/update-pico.sh'],
  ]) {
    try {
      const sha256 = digest(installed);
      helpers[name] = { sha256, matchesSource: source ? sha256 === manifest.files[source] : null };
    } catch { helpers[name] = { available: false }; }
  }
  return { version: require('../package.json').version, manifestVersion: manifest.version,
    sourceMatches: mismatches.length === 0, mismatches, helpers };
}
if (require.main === module) {
  try {
    const result = inspect();
    console.log(JSON.stringify(result, null, 2));
    if (!result.sourceMatches) process.exitCode = 1;
  }
  catch { console.error('Unable to verify installed source manifest.'); process.exitCode = 1; }
}
module.exports = { inspect };
