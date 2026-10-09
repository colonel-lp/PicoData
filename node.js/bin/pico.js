#!/usr/bin/env node
'use strict';

const fs = require('node:fs');
const path = require('node:path');
const { PicoClient } = require('../lib/client');
const { readMqttConfig, MqttPublisher } = require('../lib/mqtt-publisher');
const { parseSbmsOptions, SbmsReceiver } = require('../lib/sbms');
const { readLoggingConfig } = require('../lib/history-config');
const { HistoryLogger } = require('../lib/logger');
const args = process.argv.slice(2);
const options = {}, allowed = new Set(['--ip', '--record', '--duration', '--udp-port', '--tcp-port', '--mqtt-config', '--logging-config']);
let recordPath, duration, mqttConfigPath = path.resolve(__dirname, '../../mqtt');
let mqttDisabled = false, mqttConfigProvided = false, printReadings = false;
let sbmsDisabled = false, printSbms = false;
let loggingConfigPath = path.resolve(__dirname, '../../logging.json'), loggingProvided = false, loggingDisabled = false;
function usage() {
  console.error('Usage: node bin/pico.js [--ip ADDRESS] [--record FILE.jsonl] [--duration SECONDS] [--udp-port PORT] [--tcp-port PORT] [--mqtt-config FILE | --no-mqtt] [--stdout] [--no-sbms | --sbms-stdout] [--logging-config FILE | --no-logging]');
  console.error('MQTT defaults to PicoData/mqtt beside node.js. Readings are silent unless --stdout is supplied. Status goes to stderr.');
  console.error('SBMS reception defaults to /Ella/sbms; --sbms-stdout shows decoded SBMS readings. Optional mqtt keys: sbms_topic, sbms_cells (1-based), sbms_stale_seconds.');
  console.error('Logging starts when parent PicoData/logging.json exists. Use --logging-config FILE or --no-logging. SQLite needs Node 22.13+.');
}
for (let i = 0; i < args.length; i++) {
  if (args[i] === '--help' || args[i] === '-h') { usage(); process.exit(0); }
  if (args[i] === '--no-mqtt') { mqttDisabled = true; continue; }
  if (args[i] === '--stdout') { printReadings = true; continue; }
  if (args[i] === '--no-sbms') { sbmsDisabled = true; continue; }
  if (args[i] === '--sbms-stdout') { printSbms = true; continue; }
  if (args[i] === '--no-logging') { loggingDisabled = true; continue; }
  if (!allowed.has(args[i]) || !args[i + 1] || args[i + 1].startsWith('--')) { usage(); process.exit(2); }
  const flag = args[i++], value = args[i];
  if (flag === '--ip') options.picoIp = value;
  if (flag === '--record') recordPath = value;
  if (flag === '--duration') duration = Number(value);
  if (flag === '--udp-port') options.udpPort = Number(value);
  if (flag === '--tcp-port') options.port = Number(value);
  if (flag === '--mqtt-config') { mqttConfigPath = value; mqttConfigProvided = true; }
  if (flag === '--logging-config') { loggingConfigPath = value; loggingProvided = true; }
}
if ((loggingDisabled && loggingProvided) || (mqttDisabled && mqttConfigProvided) || (printSbms && (mqttDisabled || sbmsDisabled)) ||
  (duration !== undefined && (!Number.isFinite(duration) || duration <= 0)) ||
  ['port', 'udpPort'].some(k => options[k] !== undefined && (!Number.isInteger(options[k]) || options[k] < 1 || options[k] > 65535))) {
  usage(); process.exit(2);
}
let publisher, sbms, logger;
if (!loggingDisabled && (loggingProvided || fs.existsSync(loggingConfigPath))) {
  try {
    logger = new HistoryLogger(readLoggingConfig(loggingConfigPath));
    logger.on('status', status => console.error(new Date().toISOString(), JSON.stringify(status)));
    logger.status('started');
  } catch (err) {
    console.error('Logging setup failed. Check logging.json, the writable database location, a second writer, and Node SQLite support.');
    if (process.env.DEBUG === 'pico') console.error(err.message);
    process.exit(1);
  }
}
if (!mqttDisabled) {
  try {
    const config = readMqttConfig(mqttConfigPath);
    if (!sbmsDisabled) sbms = new SbmsReceiver(parseSbmsOptions(config));
    publisher = new MqttPublisher(config, { resubscribe: !sbms });
    publisher.on('status', status => console.error(new Date().toISOString(), JSON.stringify(status)));
    publisher.start();
    sbms?.on('status', status => {
      console.error(new Date().toISOString(), JSON.stringify(status));
      if (status.state !== 'connected') logger?.unavailable('sbms');
    });
    sbms?.start(publisher.client);
    options.legacyPythonOutput = true;
  } catch {
    console.error('MQTT setup failed. Check PicoData/mqtt (or --mqtt-config FILE), optional SBMS topic/cell/timeout settings, and run npm ci; use --no-mqtt for a reader-only test. Credentials are not printed.');
    process.exit(1);
  }
}
// Recordings are local evidence, never an automatic upload. Append allows
// reconnects/config refreshes to remain together in one capture.
const recording = recordPath ? fs.createWriteStream(recordPath, { flags: 'a', mode: 0o600 }) : null;
const client = new PicoClient(options);
const writeRecord = (kind, value) => recording?.write(JSON.stringify({ kind, ...value }) + '\n');
sbms?.on('readings', reading => {
  logger?.acceptSbms(reading);
  if (printSbms) process.stdout.write(JSON.stringify(reading) + '\n');
  writeRecord('sbms', { reading });
});
if (process.env.DEBUG === 'pico') sbms?.on('diagnostic', text => console.error('SBMS:', text));
writeRecord('session', { version: require('../package.json').version, startedAt: new Date().toISOString(), nodeVersion: process.version });
client.on('tcp', data => writeRecord('tcp', data));
client.on('config', data => writeRecord('config', data));
if (logger) {
  client.on('config', data => logger.configurePico(data));
  client.on('snapshot', data => logger.acceptPico(data));
}
client.on('packet', data => writeRecord('packet', data));
client.on('readings', data => {
  if (printReadings) process.stdout.write(JSON.stringify(data) + '\n');
  publisher?.publish(data);
});
client.on('status', status => {
  console.error(new Date().toISOString(), JSON.stringify(status));
  if (status.state !== 'connected') logger?.unavailable('pico');
});
if (process.env.DEBUG === 'pico') client.on('diagnostic', text => console.error(text));
let timer, stopping = false;
async function stop() {
  if (stopping) return;
  stopping = true; clearTimeout(timer);
  await client.stop();
  sbms?.stop();
  await publisher?.stop();
  logger?.close();
  if (recording) recording.end();
}
process.on('SIGINT', stop); process.on('SIGTERM', stop);
recording?.on('error', err => { console.error('Recording failed:', err.message); process.exitCode = 1; stop(); });
if (duration !== undefined) timer = setTimeout(stop, duration * 1000);
client.start().catch(err => { console.error(err.message); process.exitCode = 1; stop(); });
