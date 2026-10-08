'use strict';

// Synthetic packets, not recordings from the owner's Pico.
const { calcRevCrc16 } = require('../lib/crc16');
function pairField(id, pair) {
  const b = Buffer.alloc(7); b[0] = Number(id); b[1] = 1;
  b.writeUInt16BE(pair[0], 2); b.writeUInt16BE(pair[1], 4); b[6] = 0xff; return b;
}
function textField(id, text) {
  return Buffer.concat([Buffer.from([Number(id), 4, 0, 0, 0, 0, 0]), Buffer.from(text, 'latin1'), Buffer.from([0, 0xff])]);
}
function fields(data) {
  return Buffer.concat(Object.entries(data).map(([id, value]) => typeof value === 'string' ? textField(id, value) : pairField(id, value)));
}
function frame(payload, command = 0xb0) {
  const header = Buffer.from('0000000000ffb0048c554b0000ff', 'hex');
  header[6] = command; header.writeUInt16BE(payload.length + 3, 11);
  const result = Buffer.concat([header, payload, Buffer.alloc(2)]);
  result.writeUInt16BE(calcRevCrc16(result.subarray(1, result.length - 3)), result.length - 2);
  return result;
}
function fixture(seed = 0) {
  const config = {}, element = {};
  let pos = 0;
  function sensor(id, type, name, width = 1, extra = {}) {
    const entry = { 0: [0, id], 1: [0, type], ...extra };
    if (name !== undefined) entry[3] = name;
    config[Object.keys(config).length] = entry;
    for (let i = 0; i < width; i++) element[pos + i] = [0, 0];
    const start = pos; pos += width; return start;
  }
  sensor(1, 0, undefined, 0);
  let p = sensor(2, 1, 'PICO INTERNAL', 6); element[p] = [0, 12000];
  p = sensor(80, 2, 'Fridge', 2); element[p] = [0, 201 + seed];
  p = sensor(90, 2, 'Starter Charger', 2); element[p] = [0, 65535 - 145 - seed];
  p = sensor(10, 3, 'Inside'); element[p] = [0, 231 + seed];
  p = sensor(11, 3, 'Outside'); element[p] = [0, 65536 - 42 - seed];
  p = sensor(12, 5, 'Barometer', 2); element[p] = [0, 35000 + seed];
  p = sensor(13, 6, 'Ohm'); element[p] = [0, 4321];
  p = sensor(14, 8, 'LPG', 1, { 6: [0, 2], 7: [0, 1000] }); element[p] = [450, 445 + seed];
  p = sensor(15, 9, 'Ella  ', 5, { 5: [0, 28000] });
  element[p] = [13280 + seed, 12345]; element[p + 1] = [0, 65535 - 876 - seed]; element[p + 2] = [0, 13240 + seed];
  p = sensor(16, 9, 'Starter', 5, { 5: [0, 10000] });
  element[p] = [16000, 1234]; element[p + 1] = [0, 200 + seed]; element[p + 2] = [0, 12500];
  p = sensor(17, 13, [0, 1]); element[p] = [0, 23 + seed];
  p = sensor(18, 13, [0, 2]); element[p] = [0, 65535 - 36 - seed];
  sensor(19, 14, undefined);
  p = sensor(20, 1, '[hidden]'); element[p] = [0, 9999];
  p = sensor(21, 1, 'Standalone'); element[p] = [0, 12222];
  // Non-monotonic IDs with duplicate labels expose ordering differences.
  p = sensor(200, 3, 'Duplicate'); element[p] = [0, 180];
  p = sensor(22, 3, 'Duplicate'); element[p] = [0, 190];
  return { config, element, packet: frame(fields(element)) };
}
module.exports = { pairField, textField, fields, frame, fixture };
