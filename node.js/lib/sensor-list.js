'use strict';

function createSensorList(config) {
  if (!config || typeof config !== 'object' || !Object.keys(config).length) throw new Error('Empty Pico configuration');
  const sensorList = {};
  // Type 10 was accepted by the working Python/Node collectors as an opaque
  // single-slot entry. Preserve that layout without inventing a measurement
  // decoder; rejecting it prevents all Pico readings on existing installations.
  const supported = new Set([0, 1, 2, 3, 5, 6, 8, 9, 10, 13, 14]);
  const pair = value => Array.isArray(value) && value.length === 2 &&
    value.every(n => Number.isInteger(n) && n >= 0 && n <= 65535);
  const fluid = ['Unknown', 'freshWater', 'fuel', 'wasteWater'];
  const fluidType = ['Unknown', 'fresh water', 'diesel', 'blackwater'];
  let elementPos = 0;

  for (const entry of Object.keys(config)) {
    const data = config[entry];
    if (!data || !pair(data[0]) || !pair(data[1])) throw new Error('Invalid Pico configuration identity');
    const id = data[0][1];
    const code = data[1][1];
    if (Object.hasOwn(sensorList, id)) throw new Error('Duplicate Pico sensor ID');
    if (!supported.has(code)) throw new Error('Unsupported Pico sensor type ' + code);
    if ([1, 2, 3, 5, 6, 8, 9].includes(code) && typeof data[3] !== 'string') throw new Error('Invalid Pico sensor name');
    if (code === 8 && (!pair(data[6]) || !pair(data[7]))) throw new Error('Invalid Pico tank configuration');
    if (code === 9 && !pair(data[5])) throw new Error('Invalid Pico battery configuration');
    if (code === 13 && !pair(data[3])) throw new Error('Invalid Pico inclinometer configuration');
    let type = config[entry][1][1];
    let elementSize = 1;

    sensorList[id] = {};

    if (type === 0) {
      type = 'null';
      elementSize = 0;
    }
    if (type === 1) {
      type = 'volt';
      sensorList[id].name = config[entry][3];
      if (config[entry][3] === 'PICO INTERNAL') {
        elementSize = 6;
      }
    }
    if (type === 2) {
      type = 'current';
      sensorList[id].name = config[entry][3];
      elementSize = 2;
    }
    if (type === 3) {
      type = 'thermometer';
      sensorList[id].name = config[entry][3];
    }
    if (type === 5) {
      type = 'barometer';
      sensorList[id].name = config[entry][3];
      elementSize = 2;
    }
    if (type === 6) {
      type = 'ohm';
      sensorList[id].name = config[entry][3];
    }
    if (type === 8) {
      type = 'tank';
      sensorList[id].name = config[entry][3];
      sensorList[id].capacity = config[entry][7][1] / 10;
      sensorList[id].fluid_type = fluidType[config[entry][6][1]];
      sensorList[id].fluid = fluid[config[entry][6][1]];
    }
    if (type === 9) {
      type = 'battery';
      sensorList[id].name = config[entry][3];
      sensorList[id]['capacity.nominal'] = config[entry][5][1] * 36 * 12;
      elementSize = 5;
    }
    // Owner's Python type-13 mapping; it still consumes one live field.
    if (type === 13) {
      type = 'inclinometer';
      const axis = config[entry][3][1];
      sensorList[id].inclinometer_type = axis;
      if (axis === 1) sensorList[id].name = 'pitch';
      if (axis === 2) sensorList[id].name = 'roll';
    }
    if (type === 14) {
      type = 'XX';
      elementSize = 1;
    }

    sensorList[id].type = type;
    sensorList[id].pos = elementPos;
    elementPos += elementSize;
  }

  return sensorList;
}

module.exports = {
  createSensorList,
};
