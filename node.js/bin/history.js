#!/usr/bin/env node
'use strict';
const path = require('node:path');
const { readLoggingConfig } = require('../lib/history-config');
const { sqlite, summary } = require('../lib/history-store');
function inspect(config, { metric, resolution = 'minute', limit = 1 } = {}) {
  if (!['minute', 'hour', 'day', 'month'].includes(resolution) || !Number.isInteger(limit) || limit < 1 || limit > 1000) throw Error('Invalid resolution/limit');
  const DatabaseSync = sqlite(), db = new DatabaseSync(config.database, { readOnly: true });
  try {
    db.exec('PRAGMA busy_timeout=1000;');
    const metrics = db.prepare('SELECT * FROM metrics ORDER BY id').all();
    if (metric && !metrics.some(m => m.id === metric)) throw Error('Unknown metric');
    return { savedAt: db.prepare("SELECT value FROM meta WHERE key='savedAt'").get()?.value ?? null,
      integrity: db.prepare('PRAGMA quick_check').get().quick_check,
      clockSteps: Number(db.prepare("SELECT value FROM meta WHERE key='clockSteps'").get()?.value || 0),
      counts: db.prepare('SELECT resolution,COUNT(*) AS rows FROM history GROUP BY resolution').all(),
      metrics: metrics.filter(m => !metric || m.id === metric).map(m => ({ id: m.id, name: m.name, sourceName: m.source_name,
        definition: JSON.parse(m.definition), rows: db.prepare('SELECT * FROM history WHERE metric_id=? AND resolution=? ORDER BY start_ms DESC LIMIT ?').all(m.id, resolution, limit)
          .map(row => ({ start: new Date(row.start_ms).toISOString(), end: new Date(row.end_ms).toISOString(),
            ...summary(JSON.parse(m.definition), JSON.parse(row.data)) })) })) };
  } finally { db.close(); }
}
if (require.main === module) {
  try {
    const args = process.argv.slice(2), options = {}, allowed = new Set(['--config', '--metric', '--resolution', '--limit']);
    for (let i = 0; i < args.length; i += 2) {
      if (!allowed.has(args[i]) || !args[i + 1]) throw Error('Use [--config FILE] [--metric ID] [--resolution minute|hour|day|month] [--limit 1–1000]');
      options[args[i].slice(2)] = args[i + 1];
    }
    if (options.limit !== undefined) options.limit = Number(options.limit);
    const config = readLoggingConfig(options.config || path.resolve(__dirname, '../../logging.json'));
    console.log(JSON.stringify(inspect(config, options), null, 2));
  } catch (err) { console.error('History inspection failed:', err.message); process.exitCode = 1; }
}
module.exports = { inspect };
