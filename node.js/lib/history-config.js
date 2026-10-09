'use strict';
const fs = require('node:fs');
const path = require('node:path');
const { resolveBatteryGroup } = require('./battery-history');
const TYPES = { electrical: ['battery', 'current'], voltage: ['volt'], temperature: ['thermometer'], barometer: ['barometer'] };
function validateConfig(input, filename = path.resolve('logging.json')) {
  if (!input || !Array.isArray(input.metrics) || !input.metrics.length || input.metrics.length > 64) throw Error('Logging needs 1–64 selected metrics');
  const config = { ...input, database: path.resolve(path.dirname(filename), input.database || 'history/history.sqlite'),
    commitSeconds: input.commitSeconds ?? 60, maxGapSeconds: { pico: 2, sbms: 3, ...input.maxGapSeconds } };
  if (!Number.isFinite(config.commitSeconds) || config.commitSeconds < 1 || config.commitSeconds > 60) throw Error('commitSeconds must be 1–60');
  for (const source of ['pico', 'sbms']) if (!Number.isFinite(config.maxGapSeconds[source]) || config.maxGapSeconds[source] <= 0 || config.maxGapSeconds[source] > 60) throw Error('Invalid freshness timeout');
  const ids = new Set(), bindings = new Set();
  config.metrics = input.metrics.map(m => {
    if (!m || !/^[a-zA-Z0-9_-]{1,80}$/.test(m.id) || ids.has(m.id)) throw Error('Invalid or duplicate metric id');
    ids.add(m.id);
    if (!['pico', 'sbms'].includes(m.source) || !Object.hasOwn(TYPES, m.kind)) throw Error('Invalid metric source/kind');
    if (m.source === 'pico' && (!Number.isInteger(m.sensorId) || m.sensorId < 0 || !TYPES[m.kind].includes(m.sensorType))) throw Error('Invalid Pico sensor selection/type');
    if (m.source === 'sbms' && (m.kind !== 'electrical' || !['battery', 'pv1', 'pv2', 'externalLoad'].includes(m.field))) throw Error('Invalid SBMS selection');
    if (m.kind === 'electrical' && (!['battery', 'load', 'supply', 'unknown'].includes(m.role) || ![null, 1, -1].includes(m.polarity) || ![null, 'sbms', 'self'].includes(m.voltage))) throw Error('Electrical role, polarity and voltage must be explicit');
    if (m.source === 'pico' && m.kind === 'electrical' && m.voltage === 'self' && m.sensorType !== 'battery') throw Error('Shunts have no verified individual voltage');
    if (m.expectedBinding !== undefined && (typeof m.expectedBinding !== 'string' || m.expectedBinding.length > 1000)) throw Error('Invalid configuration binding');
    const binding = m.source + ':' + (m.source === 'pico' ? m.sensorId : m.field);
    if (bindings.has(binding)) throw Error('Duplicate source selection');
    bindings.add(binding); return { ...m };
  });
  config.batteryGroup = resolveBatteryGroup(config.metrics, input.batteryGroup);
  return config;
}
function readLoggingConfig(filename) { return validateConfig(JSON.parse(fs.readFileSync(filename, 'utf8')), filename); }
// These are configuration fingerprints, not a claimed permanent hardware ID.
// Battery references and physical current/voltage inputs must not silently change.
function picoBinding(config, sensorId, sensorType) {
  const entry = Object.values(config).find(e => e[0]?.[1] === sensorId);
  if (!entry) return null;
  const fields = sensorType === 'battery' ? [4, 10, 11] : ['current', 'volt'].includes(sensorType) ? [4, 5] : [];
  return JSON.stringify(fields.map(k => entry[k] ?? null));
}
module.exports = { validateConfig, readLoggingConfig, picoBinding };
