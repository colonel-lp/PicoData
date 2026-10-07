#!/usr/bin/env node
'use strict';

const fs = require('node:fs');
const readline = require('node:readline');
const path = require('node:path');
const { spawnSync } = require('node:child_process');
const { frameLength, parseResponse } = require('../lib/pico-protocol');
const { calcRevCrc16 } = require('../lib/crc16');
const { createSensorList } = require('../lib/sensor-list');
const { decodeReadings, formatEllaJson } = require('../lib/readings');

function crcMatches(buffer) {
  return buffer.length >= 16 && buffer.readUInt16BE(buffer.length - 2) ===
    calcRevCrc16(buffer.subarray(1, buffer.length - 3));
}
async function verifyCapture(filename, { comparePython = false, replay = false } = {}) {
  const summary = { configurations: 0, packets: 0, tcpResponses: 0, tcpLengthMatches: 0,
    tcpCrcMatches: 0, udpLengthMatches: 0, udpCrcMatches: 0, pythonMatches: 0, pythonDifferences: 0 };
  let config, sensorList;
  const stream = readline.createInterface({ input: fs.createReadStream(filename), crlfDelay: Infinity });
  for await (const line of stream) {
    if (!line.trim()) continue;
    const record = JSON.parse(line);
    if (record.kind === 'tcp' && record.direction === 'complete' && !record.error) {
      const b = Buffer.from(record.hex, 'hex'); summary.tcpResponses++;
      if (frameLength(b) === b.length) summary.tcpLengthMatches++;
      if (crcMatches(b)) summary.tcpCrcMatches++;
    }
    if (record.kind === 'config') {
      config = record.config; sensorList = createSensorList(config); summary.configurations++;
    }
    if (record.kind !== 'packet') continue;
    if (!sensorList) throw new Error('Packet precedes sensor configuration');
    const b = Buffer.from(record.hex, 'hex'), element = parseResponse(b);
    summary.packets++;
    if (frameLength(b) === b.length) summary.udpLengthMatches++;
    if (crcMatches(b)) summary.udpCrcMatches++;
    const now = new Date(record.receivedAt);
    if (Number.isNaN(now.valueOf())) throw new Error('Invalid capture timestamp');
    const output = formatEllaJson(decodeReadings(sensorList, element), now);
    if (replay) process.stdout.write(JSON.stringify(output) + '\n');
    if (comparePython) {
      const time = { year: now.getFullYear(), month: now.getMonth() + 1, day: now.getDate(),
        hour: now.getHours(), minute: now.getMinutes(), second: now.getSeconds() };
      const oracle = spawnSync('python3', [path.join(__dirname, '../test/python-oracle.py'),
        path.resolve(__dirname, '../../../python/pico-mqtt.py')], {
        input: JSON.stringify({ config, element, time }), encoding: 'utf8',
      });
      if (oracle.status !== 0) throw new Error('Python reference could not decode capture: ' + oracle.stderr.trim());
      const expected = JSON.parse(oracle.stdout).output;
      const actual = JSON.parse(JSON.stringify(output));
      // Key ordering and numeric spelling are not part of the MQTT JSON contract.
      const { isDeepStrictEqual } = require('node:util');
      if (isDeepStrictEqual(actual, JSON.parse(JSON.stringify(expected)))) summary.pythonMatches++;
      else summary.pythonDifferences++;
    }
  }
  summary.ok = summary.configurations > 0 && summary.packets > 0 && summary.tcpResponses > 0 &&
    summary.tcpLengthMatches === summary.tcpResponses && summary.tcpCrcMatches === summary.tcpResponses &&
    summary.udpLengthMatches === summary.packets && summary.udpCrcMatches === summary.packets &&
    (!comparePython || summary.pythonMatches === summary.packets);
  return summary;
}
if (require.main === module) {
  const args = process.argv.slice(2);
  if (!args[0] || args[0].startsWith('--') || args.slice(1).some(x => !['--compare-python', '--replay'].includes(x))) {
    console.error('Usage: node bin/verify-capture.js FILE.jsonl [--compare-python] [--replay]'); process.exit(2);
  }
  verifyCapture(args[0], { comparePython: args.includes('--compare-python'), replay: args.includes('--replay') })
    .then(summary => {
      (args.includes('--replay') ? console.error : console.log)(JSON.stringify(summary));
      if (!summary.ok) process.exitCode = 1;
    }).catch(err => { console.error(err.message); process.exitCode = 1; });
}
module.exports = { crcMatches, verifyCapture };
