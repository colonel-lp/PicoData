#!/usr/bin/env node
'use strict';
const path = require('node:path');
const { readLoggingConfig } = require('../lib/history-config');
const { sqlite, summary } = require('../lib/history-store');
const { fieldSources, combinedSummary } = require('../lib/battery-history');
function inspect(config, { metric, resolution = 'minute', limit = 1, batteryOnly = false } = {}) {
  if (!['minute', 'hour', 'day', 'month'].includes(resolution) || !Number.isInteger(limit) || limit < 1 || limit > 1000 || (metric && batteryOnly)) throw Error('Invalid history options');
  const DatabaseSync = sqlite(), db = new DatabaseSync(config.database, { readOnly: true });
  try {
    db.exec('PRAGMA busy_timeout=1000;');
    const schemaVersion = db.prepare('PRAGMA user_version').get().user_version;
    if (![1,2].includes(schemaVersion)) throw Error('Unsupported history schema');
    if (batteryOnly && schemaVersion === 1) throw Error('Start the updated collector to migrate battery history first');
    const metrics = db.prepare('SELECT * FROM metrics ORDER BY id').all();
    if (metric && !metrics.some(m => m.id === metric)) throw Error('Unknown metric');
    const group = schemaVersion === 2 ? Object.fromEntries(db.prepare('SELECT * FROM battery_fields').all().map(r => [r.channel,r.metric_id])) : {};
    const channelById = new Map(Object.entries(group).map(([channel,id]) => [id,channel]));
    const selected = metric ? metrics.filter(m => m.id === metric) : batteryOnly ? [] : metrics.filter(m => !channelById.has(m.id));
    const counts = db.prepare("SELECT 'metrics' AS collection,resolution,COUNT(*) AS rows FROM history GROUP BY resolution").all();
    if (schemaVersion === 2) counts.push(...db.prepare("SELECT 'battery' AS collection,resolution,COUNT(*) AS rows FROM battery_history GROUP BY resolution").all());
    return { schemaVersion, savedAt: db.prepare("SELECT value FROM meta WHERE key='savedAt'").get()?.value ?? null,
      integrity: db.prepare('PRAGMA quick_check').get().quick_check,
      clockSteps: Number(db.prepare("SELECT value FROM meta WHERE key='clockSteps'").get()?.value || 0), counts,
      battery: !metric && Object.keys(group).length ? { sources: fieldSources(group,metrics), rows:
        db.prepare('SELECT * FROM battery_history WHERE resolution=? ORDER BY start_ms DESC LIMIT ?').all(resolution,limit)
          .map(row => ({ start:new Date(row.start_ms).toISOString(),end:new Date(row.end_ms).toISOString(),
            ...combinedSummary(group,metrics,JSON.parse(row.data)) })) } : null,
      metrics: selected.map(m => {
        const channel = channelById.get(m.id);
        // Older --metric commands are views into the single combined record.
        const rows = channel ? db.prepare('SELECT start_ms,end_ms,json_extract(data,?) AS data FROM battery_history WHERE resolution=? AND json_type(data,?)=\'object\' ORDER BY start_ms DESC LIMIT ?')
          .all('$.'+channel,resolution,'$.'+channel,limit) :
          db.prepare('SELECT * FROM history WHERE metric_id=? AND resolution=? ORDER BY start_ms DESC LIMIT ?').all(m.id,resolution,limit);
        return { id:m.id,name:m.name,sourceName:m.source_name,definition:JSON.parse(m.definition),rows:
          rows.map(row => ({ start:new Date(row.start_ms).toISOString(),end:new Date(row.end_ms).toISOString(),
            ...summary(JSON.parse(m.definition),JSON.parse(row.data)) })) };
      }) };
  } finally { db.close(); }
}
if (require.main === module) {
  try {
    const args = process.argv.slice(2), options = {}, allowed = new Set(['--config','--metric','--resolution','--limit']);
    for (let i = 0; i < args.length; i++) {
      if (args[i] === '--battery') { options.batteryOnly = true; continue; }
      if (!allowed.has(args[i]) || !args[i+1] || args[i+1].startsWith('--')) throw Error('Use [--config FILE] [--battery | --metric ID] [--resolution minute|hour|day|month] [--limit 1–1000]');
      options[args[i].slice(2)] = args[++i];
    }
    if (options.limit !== undefined) options.limit = Number(options.limit);
    const config = readLoggingConfig(options.config || path.resolve(__dirname,'../../logging.json'));
    console.log(JSON.stringify(inspect(config,options),null,2));
  } catch (err) { console.error('History inspection failed:',err.message); process.exitCode = 1; }
}
module.exports = { inspect };
