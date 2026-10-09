#!/usr/bin/env node
'use strict';
const fs = require('node:fs');
const path = require('node:path');
const readline = require('node:readline');
const { createSensorList } = require('../lib/sensor-list');
const { validateConfig, picoBinding } = require('../lib/history-config');
async function main() {
  const args = process.argv.slice(2), options = {};
  const allowed = new Set(['--capture', '--battery-id', '--secondary-voltage-id', '--outside-id', '--barometer-id', '--load-ids', '--load-polarity', '--output']);
  for (let i = 0; i < args.length; i += 2) {
    if (!allowed.has(args[i]) || !args[i + 1]) throw Error('Use --capture FILE --battery-id ID --secondary-voltage-id ID --outside-id ID --barometer-id ID --load-ids ID,ID --load-polarity -1|1 [--output FILE]');
    options[args[i].slice(2)] = args[i + 1];
  }
  const filename = path.resolve(options.output || path.resolve(__dirname, '../../logging.json'));
  if (fs.existsSync(filename)) throw Error('Configuration already exists; edit it deliberately rather than overwriting it');
  if (!options.capture || !['1', '-1'].includes(options['load-polarity'])) throw Error('Supply a capture and the verified load polarity');
  let config;
  const input = fs.createReadStream(options.capture), lines = readline.createInterface({ input, crlfDelay: Infinity });
  try {
    for await (const line of lines) {
      if (!line.trim()) continue;
      const record = JSON.parse(line);
      if (record.kind === 'config') { config = record.config; break; }
    }
  } finally { lines.close(); input.destroy(); }
  if (!config) throw Error('Capture contains no Pico configuration');
  const sensors = createSensorList(config), metrics = [];
  function select(flag, kind, type, extra = {}) {
    const text = options[flag];
    if (!text || !/^\d+$/.test(text)) throw Error('Missing or invalid ' + flag);
    const sensorId = Number(text), sensor = sensors[sensorId];
    if (!sensor || sensor.type !== type) throw Error('Selected sensor has the wrong type: ' + flag);
    const id = flag === 'battery-id' ? 'pico-battery' : flag === 'secondary-voltage-id' ? 'secondary-voltage' : flag === 'outside-id' ? 'outside-temperature' : flag === 'barometer-id' ? 'barometer' : 'pico-load-' + sensorId;
    metrics.push({ id, source: 'pico', sensorId, sensorType: type, kind, expectedBinding: picoBinding(config, sensorId, type), ...extra });
  }
  select('battery-id', 'electrical', 'battery', { role: 'battery', polarity: 1, voltage: 'sbms' });
  select('secondary-voltage-id', 'voltage', 'volt');
  select('outside-id', 'temperature', 'thermometer');
  select('barometer-id', 'barometer', 'barometer');
  if (!options['load-ids']) throw Error('Select load shunts explicitly');
  for (const id of options['load-ids'].split(',')) {
    options['load-id'] = id;
    select('load-id', 'electrical', 'current', { role: 'load', polarity: Number(options['load-polarity']), voltage: 'sbms' });
  }
  for (const field of ['battery', 'pv1', 'pv2', 'externalLoad']) metrics.push({
    id: 'sbms-' + field, source: 'sbms', field, kind: 'electrical',
    role: field === 'battery' ? 'battery' : field.startsWith('pv') ? 'supply' : 'unknown',
    // Positive battery current = charge; PV/load direction is left unconfirmed.
    polarity: field === 'battery' ? 1 : null,
    voltage: 'self',
  });
  const result = { database: 'history/history.sqlite', commitSeconds: 60, maxGapSeconds: { pico: 2, sbms: 3 }, metrics };
  validateConfig(result, filename);
  fs.writeFileSync(filename, JSON.stringify(result, null, 2) + '\n', { flag: 'wx', mode: 0o600 });
  console.log('Private logging configuration created. Review selected IDs and polarity before starting the service.');
}
if (require.main === module) main().catch(err => { console.error(err.message); process.exitCode = 1; });
module.exports = { main };
