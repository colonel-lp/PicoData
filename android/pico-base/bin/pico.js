#!/usr/bin/env node
'use strict';

const fs = require('node:fs');
const { PicoClient } = require('../lib/client');
const { readMqttConfig, MqttPublisher } = require('../lib/mqtt-publisher');
const args = process.argv.slice(2);
const options = {}, allowed = new Set(['--ip', '--record', '--duration', '--udp-port', '--tcp-port', '--mqtt-config']);
let recordPath, duration, mqttConfigPath;
function usage() {
  console.error('Usage: node bin/pico.js [--ip ADDRESS] [--record FILE.jsonl] [--duration SECONDS] [--udp-port PORT] [--tcp-port PORT] [--mqtt-config FILE]');
  console.error('Ella JSON goes to stdout; connection status goes to stderr. DEBUG=pico adds diagnostics.');
}
for (let i = 0; i < args.length; i++) {
  if (args[i] === '--help' || args[i] === '-h') { usage(); process.exit(0); }
  if (!allowed.has(args[i]) || !args[i + 1] || args[i + 1].startsWith('--')) { usage(); process.exit(2); }
  const flag = args[i++], value = args[i];
  if (flag === '--ip') options.picoIp = value;
  if (flag === '--record') recordPath = value;
  if (flag === '--duration') duration = Number(value);
  if (flag === '--udp-port') options.udpPort = Number(value);
  if (flag === '--tcp-port') options.port = Number(value);
  if (flag === '--mqtt-config') mqttConfigPath = value;
}
if ((duration !== undefined && (!Number.isFinite(duration) || duration <= 0)) ||
  ['port', 'udpPort'].some(k => options[k] !== undefined && (!Number.isInteger(options[k]) || options[k] < 1 || options[k] > 65535))) {
  usage(); process.exit(2);
}
let publisher;
if (mqttConfigPath) {
  try {
    publisher = new MqttPublisher(readMqttConfig(mqttConfigPath));
    publisher.on('status', status => console.error(new Date().toISOString(), JSON.stringify(status)));
    publisher.start();
    options.legacyPythonOutput = true;
  } catch {
    console.error('MQTT setup failed. Check the configuration file and run npm ci; credentials are not printed.');
    process.exit(1);
  }
}
// Recordings are local evidence, never an automatic upload. Append allows
// reconnects/config refreshes to remain together in one capture.
const recording = recordPath ? fs.createWriteStream(recordPath, { flags: 'a', mode: 0o600 }) : null;
const client = new PicoClient(options);
const writeRecord = (kind, value) => recording?.write(JSON.stringify({ kind, ...value }) + '\n');
writeRecord('session', { version: require('../package.json').version, startedAt: new Date().toISOString(), nodeVersion: process.version });
client.on('tcp', data => writeRecord('tcp', data));
client.on('config', data => writeRecord('config', data));
client.on('packet', data => writeRecord('packet', data));
client.on('readings', data => {
  process.stdout.write(JSON.stringify(data) + '\n');
  publisher?.publish(data);
});
client.on('status', status => console.error(new Date().toISOString(), JSON.stringify(status)));
if (process.env.DEBUG === 'pico') client.on('diagnostic', text => console.error(text));
let timer, stopping = false;
async function stop() {
  if (stopping) return;
  stopping = true; clearTimeout(timer);
  await client.stop();
  await publisher?.stop();
  if (recording) recording.end();
}
process.on('SIGINT', stop); process.on('SIGTERM', stop);
recording?.on('error', err => { console.error('Recording failed:', err.message); process.exitCode = 1; stop(); });
if (duration !== undefined) timer = setTimeout(stop, duration * 1000);
client.start().catch(err => { console.error(err.message); process.exitCode = 1; stop(); });
