'use strict';

// One physical battery interval, with separate measurements and provenance.
const CHANNELS = ['picoBattery', 'sbmsBattery', 'secondaryVoltage', 'pv1', 'pv2', 'externalLoad'];
function resolveBatteryGroup(metrics, selection) {
  if (selection === undefined) {
    selection = {};
    const pico = metrics.filter(m => m.source === 'pico' && m.kind === 'electrical' && m.role === 'battery');
    if (pico.length > 1) throw Error('Select the main battery explicitly with batteryGroup');
    if (pico[0]) selection.picoBattery = pico[0].id;
    const secondary = metrics.find(m => m.id === 'secondary-voltage' && m.kind === 'voltage');
    if (secondary) selection.secondaryVoltage = secondary.id;
    for (const m of metrics.filter(m => m.source === 'sbms')) selection[m.field === 'battery' ? 'sbmsBattery' : m.field] = m.id;
  }
  if (!selection || typeof selection !== 'object' || Array.isArray(selection)) throw Error('Invalid batteryGroup');
  const used = new Set(), result = {};
  for (const [channel, id] of Object.entries(selection)) {
    const m = metrics.find(m => m.id === id);
    if (!CHANNELS.includes(channel) || !m || used.has(id)) throw Error('Invalid or duplicate batteryGroup member');
    const valid = channel === 'picoBattery' ? m.source === 'pico' && m.kind === 'electrical' && m.role === 'battery' :
      channel === 'secondaryVoltage' ? m.source === 'pico' && m.kind === 'voltage' :
      m.source === 'sbms' && m.kind === 'electrical' && m.field === (channel === 'sbmsBattery' ? 'battery' : channel);
    if (!valid) throw Error('Battery group member has the wrong measurement type');
    used.add(id); result[channel] = id;
  }
  return result;
}
const FIELDS = {
  voltage: ['sbmsBattery', 'voltage', 'V'], secondaryVoltage: ['secondaryVoltage', 'voltage', 'V'],
  picoCurrent: ['picoBattery', 'current', 'A'], picoWatts: ['picoBattery', 'power', 'W'], picoSoc: ['picoBattery', 'soc', '%'],
  sbmsCurrent: ['sbmsBattery', 'current', 'A'], sbmsWatts: ['sbmsBattery', 'power', 'W'], sbmsSoc: ['sbmsBattery', 'soc', '%'],
  pv1Current: ['pv1', 'current', 'A'], pv1Watts: ['pv1', 'power', 'W'],
  pv2Current: ['pv2', 'current', 'A'], pv2Watts: ['pv2', 'power', 'W'],
  externalLoadCurrent: ['externalLoad', 'current', 'A'], externalLoadWatts: ['externalLoad', 'power', 'W'],
};
function fieldSources(group, metrics) {
  const byId = new Map(metrics.map(m => [m.id, m]));
  const sources = {};
  for (const [field, [channel, quantity, unit]] of Object.entries(FIELDS)) {
    const m = byId.get(group[channel]);
    if (!m) continue;
    const definition = JSON.parse(m.definition);
    sources[field] = { metricId: m.id, source: definition.source, name: m.name, sourceName: m.source_name,
      quantity, unit, ...(definition.sensorId !== undefined ? { sensorId: definition.sensorId } : { field: quantity === 'voltage' ? 'voltage' : quantity === 'soc' ? 'stateOfCharge' : 'current.'+definition.field }) };
    if (quantity === 'power') sources[field].voltageSource = definition.voltage === null ? null : {source:definition.voltage === 'sbms' ? 'sbms' : definition.source,field:'voltage'};
  }
  return sources;
}
function combinedSummary(group, metrics, channels) {
  const byId = new Map(metrics.map(m => [m.id, JSON.parse(m.definition)]));
  const values = {}, coverageSeconds = {}, lastReceivedAt = {}, energy = {}, samples = {};
  for (const [field, [channel, quantity]] of Object.entries(FIELDS)) {
    if (!group[channel]) continue;
    const r = channels[channel];
    const denominator = { current: 'ampCoverageSeconds', voltage: 'voltCoverageSeconds', power: 'powerCoverageSeconds' }[quantity];
    const numerator = { current: 'ampSeconds', voltage: 'voltSeconds', power: 'wattSeconds' }[quantity];
    values[field] = quantity === 'soc' ? r?.soc ?? null : r?.[denominator] ? r[numerator] / r[denominator] : null;
    if (quantity !== 'soc') coverageSeconds[field] = r?.[denominator] ?? 0;
    const iso = at => at === null || at === undefined ? null : new Date(at).toISOString();
    lastReceivedAt[field] = quantity === 'power' ? {current:iso(r?.currentLastAt),voltage:iso(r?.voltageLastAt)} :
      iso(r?.[{current:'currentLastAt',voltage:'voltageLastAt',soc:'socAt'}[quantity]]);
  }
  for (const [channel, id] of Object.entries(group)) {
    const m = byId.get(id), r = channels[channel];
    samples[channel] = r?.samples ?? 0;
    if (m.kind !== 'electrical') continue;
    energy[channel] = {
      netWh: r?.powerCoverageSeconds ? r.wattSeconds / 3600 : null,
      netAh: r?.ampCoverageSeconds ? r.ampSeconds / 3600 : null,
      forwardWh: m.polarity === null || !r?.powerCoverageSeconds ? null : r.forwardWattSeconds / 3600,
      reverseWh: m.polarity === null || !r?.powerCoverageSeconds ? null : r.reverseWattSeconds / 3600,
      forwardAh: m.polarity === null || !r?.ampCoverageSeconds ? null : r.forwardAmpSeconds / 3600,
      reverseAh: m.polarity === null || !r?.ampCoverageSeconds ? null : r.reverseAmpSeconds / 3600,
    };
  }
  return { values, coverageSeconds, energy, lastReceivedAt, samples };
}
module.exports = { CHANNELS, resolveBatteryGroup, fieldSources, combinedSummary };
