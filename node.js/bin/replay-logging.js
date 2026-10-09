#!/usr/bin/env node
'use strict';
const fs = require('node:fs');
const path = require('node:path');
const readline = require('node:readline');
const { readLoggingConfig } = require('../lib/history-config');
const { HistoryLogger } = require('../lib/logger');
const { createSensorList } = require('../lib/sensor-list');
const { parseResponse, isLivePacket } = require('../lib/pico-protocol');
const { decodeReadings } = require('../lib/readings');
async function replay(filename, config) {
  // Never replay into live history. Explicit output must be a fresh database.
  for (const suffix of ['', '-wal', '-shm', '.writer-lock']) if (fs.existsSync(config.database + suffix)) throw Error('Replay output already exists');
  const logger = new HistoryLogger(config, { automatic: false });
  let sensorList, packets = 0, sbms = 0, first = null, last = null;
  let pendingConfig;
  logger.on('status', status => { if (status.state === 'failed') throw Error('Replay logging failed'); });
  const lines = readline.createInterface({ input: fs.createReadStream(filename), crlfDelay: Infinity });
  try {
    // Captures append source events in receipt order. No source clock is used.
    for await (const line of lines) {
      if (!line.trim()) continue;
      const record = JSON.parse(line);
      if (record.kind === 'config') { sensorList = createSensorList(record.config); pendingConfig = { config: record.config, sensorList }; continue; }
      if (!['packet', 'sbms'].includes(record.kind)) continue;
      const receivedAt = record.kind === 'sbms' ? record.reading.receivedAt : record.receivedAt;
      const time = Date.parse(receivedAt);
      if (!Number.isFinite(time) || (last !== null && time < last)) throw Error('Capture receipt time is invalid/out of order');
      if (first === null) first = time;
      if (pendingConfig) { logger.configurePico(pendingConfig, time); pendingConfig = null; }
      const receivedMonotonicMs = time - first;
      if (record.kind === 'sbms') { logger.acceptSbms({ ...record.reading, receivedAt, receivedMonotonicMs }); sbms++; }
      else {
        if (!sensorList) throw Error('Packet before configuration');
        const buffer = Buffer.from(record.hex, 'hex');
        if (!isLivePacket(buffer)) throw Error('Invalid live frame');
        const readings = decodeReadings(sensorList, parseResponse(buffer));
        logger.acceptPico({ receivedAt, receivedMonotonicMs, readings }); packets++;
      }
      last = time;
      if (logger.failed) throw Error('Replay logging failed');
    }
    logger.close({ advance: false });
    return { packets, sbms, ok: !logger.failed };
  } finally { lines.close(); lines.input.destroy(); logger.close({ advance: false }); }
}
if (require.main === module) (async () => {
  const options = {}, args = process.argv.slice(2);
  for (let i = 0; i < args.length; i += 2) {
    if (!['--capture', '--config', '--database'].includes(args[i]) || !args[i + 1]) throw Error('Use --capture FILE --config FILE --database NEW_FILE');
    options[args[i].slice(2)] = args[i + 1];
  }
  if (!options.capture || !options.config || !options.database) throw Error('Capture, configuration and a NEW database are required');
  const config = readLoggingConfig(options.config); config.database = path.resolve(options.database);
  console.log(JSON.stringify(await replay(options.capture, config)));
})().catch(err => { console.error('Replay failed:', err.message); process.exitCode = 1; });
module.exports = { replay };
