'use strict';

// Owner's python/pico-mqtt.py calculations and JSON layout, without MQTT.
function roundEven(value) {
  const floor = Math.floor(value), part = value - floor;
  return part === 0.5 ? (floor % 2 === 0 ? floor : floor + 1) : Math.round(value);
}
function currentAmps(raw) { return raw > 25000 ? (65535 - raw) / -100 : raw / 100; }
function celsius(raw) { return (raw > 32768 ? raw - 65536 : raw) / 10; }
function set(obj, key, value) {
  // Keep exact names, including trailing spaces and literal '__proto__'.
  Object.defineProperty(obj, key, { value, enumerable: true, writable: true, configurable: true });
}
function decodeReadings(sensorList, element, { legacyPython = false } = {}) {
  const result = JSON.parse(JSON.stringify(sensorList));
  function pair(pos) {
    const p = element[pos];
    if (!Array.isArray(p) || p.length !== 2 || !p.every(v => Number.isInteger(v) && v >= 0 && v <= 65535)) return null;
    return p;
  }
  for (const [id, metadata] of Object.entries(sensorList)) {
    const out = result[id], pos = metadata.pos, p = pair(pos);
    if (!p) {
      if (legacyPython && ['barometer', 'thermometer', 'tank', 'battery', 'volt', 'ohm', 'current', 'inclinometer'].includes(metadata.type)) {
        throw new Error('Incomplete Python-compatible sensor snapshot');
      }
      continue; // Missing readings never become fresh zeros.
    }
    switch (metadata.type) {
      case 'barometer': out.pressure = (p[1] + 65536) / 100; break;
      case 'thermometer': out.temperature = celsius(p[1]); break;
      case 'tank':
        {
          out.currentLevel = p[0] / 1000;
          out.remainingCapacity = p[1] / 10;
          out.percentage = metadata.capacity ? out.remainingCapacity / metadata.capacity * 100 : 0;
        }
        break;
      case 'battery': {
        const v = pair(pos + 2), i = pair(pos + 1);
        if (legacyPython && (!v || !i)) throw new Error('Incomplete Python-compatible battery snapshot');
        out['capacity.nominal'] = metadata['capacity.nominal'] / 43200;
        if (v && (legacyPython || v[1] !== 65535)) out.voltage = v[1] / 1000;
        if (i) out.current = currentAmps(i[1]);
        if (legacyPython || p[0] !== 65535) {
          out.stateOfCharge = p[0] / 160;
          out['capacity.remaining'] = metadata['capacity.nominal'] * out.stateOfCharge / 4320000;
          if (out.current !== undefined && p[0] !== 65535) {
            let remaining = roundEven(metadata['capacity.nominal'] / 12 /
              (out.current * out.stateOfCharge / 100 + 0.001));
            if (remaining < 0) remaining = 604800;
            out['capacity.timeRemaining'] = remaining;
          }
        }
        break;
      }
      case 'volt': if (legacyPython || p[1] !== 65535) out.voltage = p[1] / 1000; break;
      case 'ohm': out.ohm = p[1]; break;
      case 'current': out.current = currentAmps(p[1]); break;
      case 'inclinometer':
        out.degree = p[1] > 600 ? (p[1] - 65535) / 10 : p[1] / 10;
        break;
    }
  }
  return result;
}

function formatEllaJson(readings, now = new Date()) {
  const output = {
    time: { year: now.getFullYear() % 100, month: now.getMonth() + 1, day: now.getDate(),
      hour: now.getHours(), minute: now.getMinutes(), second: now.getSeconds() },
    barometer: {}, inclinometer: { pitch: null, roll: null }, voltage: {}, current: {},
    temperature: {}, tank: {}, battery: {},
  };
  for (const value of Object.values(readings).sort((a, b) => a.pos - b.pos)) {
    const name = value.name;
    if (!name || name.includes('[')) continue;
    switch (value.type) {
      case 'barometer': if (value.pressure !== undefined) output.barometer = value.pressure; break;
      case 'inclinometer': if (value.degree !== undefined && [1, 2].includes(value.inclinometer_type)) set(output.inclinometer, name, value.degree); break;
      case 'volt': if (value.voltage !== undefined) set(output.voltage, name, value.voltage); break;
      case 'current': if (value.current !== undefined) set(output.current, name, value.current); break;
      case 'thermometer': if (value.temperature !== undefined) set(output.temperature, name, value.temperature); break;
      case 'tank': if (value.percentage !== undefined) set(output.tank, name, {
        capacity_nominal: value.capacity, capacity_remaining: value.remainingCapacity,
        percentage: roundEven(value.percentage),
      }); break;
      case 'battery': if (value.voltage !== undefined || value.current !== undefined || value.stateOfCharge !== undefined) {
        set(output.battery, name, {
          capacity_nominal: value['capacity.nominal'], capacity_remaining: value['capacity.remaining'] ?? null,
          state_of_charge: value.stateOfCharge ?? null, current: value.current ?? null, voltage: value.voltage ?? null,
        });
        if (value.voltage !== undefined) set(output.voltage, name, value.voltage);
      } break;
    }
  }
  return output;
}
module.exports = { roundEven, currentAmps, celsius, decodeReadings, formatEllaJson };
