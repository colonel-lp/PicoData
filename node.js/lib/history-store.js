'use strict';
const fs = require('node:fs');
const path = require('node:path');
function sqlite() {
  try { return require('node:sqlite').DatabaseSync; }
  catch { throw Error('Logging requires Node 22.13+ with built-in SQLite support'); }
}
function monthBefore(time) {
  const d = new Date(time), day = d.getUTCDate();
  d.setUTCDate(1); d.setUTCMonth(d.getUTCMonth() - 1);
  const last = new Date(Date.UTC(d.getUTCFullYear(), d.getUTCMonth() + 1, 0)).getUTCDate();
  d.setUTCDate(Math.min(day, last)); return d.getTime();
}
class HistoryStore {
  constructor(config) {
    const DatabaseSync = sqlite(); this.config = config;
    fs.mkdirSync(path.dirname(config.database), { recursive: true, mode: 0o700 });
    try {
      // SQLite's OS lock is released even after a crash. Readers use the main DB.
      this.lock = new DatabaseSync(config.database + '.writer-lock');
      fs.chmodSync(config.database + '.writer-lock', 0o600);
      this.lock.exec('PRAGMA busy_timeout=0; BEGIN EXCLUSIVE;');
      this.db = new DatabaseSync(config.database);
      fs.chmodSync(config.database, 0o600);
      const version = this.db.prepare('PRAGMA user_version').get().user_version;
      if (![0, 1].includes(version)) throw Error('Unsupported history schema');
      this.db.exec(`PRAGMA journal_mode=WAL; PRAGMA synchronous=FULL; PRAGMA busy_timeout=1000; PRAGMA cache_size=-2048;
        CREATE TABLE IF NOT EXISTS metrics(id TEXT PRIMARY KEY, definition TEXT NOT NULL, name TEXT NOT NULL, source_name TEXT NOT NULL);
        CREATE TABLE IF NOT EXISTS names(metric_id TEXT NOT NULL, changed_ms INTEGER NOT NULL, name TEXT NOT NULL, source_name TEXT NOT NULL, PRIMARY KEY(metric_id,changed_ms));
        CREATE TABLE IF NOT EXISTS history(metric_id TEXT NOT NULL, resolution TEXT NOT NULL, start_ms INTEGER NOT NULL, end_ms INTEGER NOT NULL, data TEXT NOT NULL, PRIMARY KEY(metric_id,resolution,start_ms));
        CREATE INDEX IF NOT EXISTS history_expiry ON history(resolution,end_ms);
        CREATE TABLE IF NOT EXISTS meta(key TEXT PRIMARY KEY, value TEXT NOT NULL);
        PRAGMA user_version=1;`);
      for (const m of config.metrics) {
        const { name, ...definition } = m;
        const encoded = JSON.stringify(definition);
        const old = this.db.prepare('SELECT definition FROM metrics WHERE id=?').get(m.id);
        if (old && old.definition !== encoded) throw Error('Existing metric binding changed; migrate history or choose a new metric id');
        this.db.prepare('INSERT OR IGNORE INTO metrics VALUES(?,?,?,?)').run(m.id, encoded, name || m.id, m.source === 'sbms' ? m.field : m.id);
        if (name) this.name(m.id, name, Date.now(), this.db.prepare('SELECT source_name FROM metrics WHERE id=?').get(m.id).source_name);
      }
      this.getRow = this.db.prepare('SELECT data FROM history WHERE metric_id=? AND resolution=? AND start_ms=?');
      this.putRow = this.db.prepare('INSERT INTO history VALUES(?,?,?,?,?) ON CONFLICT(metric_id,resolution,start_ms) DO UPDATE SET end_ms=excluded.end_ms,data=excluded.data');
    } catch (err) { this.close(); throw err; }
  }
  name(id, name, time, sourceName = name) {
    const old = this.db.prepare('SELECT name,source_name FROM metrics WHERE id=?').get(id);
    if (!old || (old.name === name && old.source_name === sourceName)) return;
    this.db.exec('BEGIN IMMEDIATE');
    try {
      this.db.prepare('UPDATE metrics SET name=?,source_name=? WHERE id=?').run(name, sourceName, id);
      this.db.prepare('INSERT OR REPLACE INTO names VALUES(?,?,?,?)').run(id, time, name, sourceName);
      this.db.exec('COMMIT');
    } catch (err) { this.db.exec('ROLLBACK'); throw err; }
  }
  read(id, resolution, start) {
    const row = this.getRow.get(id, resolution, start); return row ? JSON.parse(row.data) : null;
  }
  save(rows, time) {
    this.db.exec('BEGIN IMMEDIATE');
    try {
      for (const row of rows) this.putRow.run(row.id, row.resolution, row.start, row.end, JSON.stringify(row.data));
      const cutoff = monthBefore(time);
      this.db.prepare(`DELETE FROM history WHERE
        (resolution='minute' AND end_ms<=?) OR
        (resolution='hour' AND end_ms<=CASE WHEN metric_id IN (SELECT id FROM metrics WHERE json_extract(definition,'$.kind') IN ('temperature','barometer')) THEN ? ELSE ? END) OR
        (resolution='day' AND end_ms<=? AND metric_id IN (SELECT id FROM metrics WHERE json_extract(definition,'$.kind') NOT IN ('temperature','barometer')))`)
        .run(time - 86400000, cutoff, time - 7 * 86400000, cutoff);
      this.db.prepare("INSERT OR REPLACE INTO meta VALUES('savedAt',?)").run(new Date(time).toISOString());
      this.db.exec('COMMIT');
    } catch (err) { this.db.exec('ROLLBACK'); throw err; }
  }
  close() {
    try { this.db?.close(); } finally { this.db = null; this.lock?.close(); this.lock = null; }
  }
}
function summary(metric, row) {
  const a = row.ampCoverageSeconds ? row.ampSeconds / row.ampCoverageSeconds : null;
  const v = row.voltCoverageSeconds ? row.voltSeconds / row.voltCoverageSeconds : null;
  const w = row.powerCoverageSeconds ? row.wattSeconds / row.powerCoverageSeconds : null;
  const array = metric.kind === 'electrical' ? [w, a, v] : null;
  if (array && metric.role === 'battery') array.push(row.soc ?? null);
  return { values: array, value: metric.kind === 'voltage' ? v : row.last ?? null,
    netWh: row.powerCoverageSeconds ? row.wattSeconds / 3600 : null,
    netAh: row.ampCoverageSeconds ? row.ampSeconds / 3600 : null,
    forwardWh: metric.polarity === null || !row.powerCoverageSeconds ? null : row.forwardWattSeconds / 3600,
    reverseWh: metric.polarity === null || !row.powerCoverageSeconds ? null : row.reverseWattSeconds / 3600,
    forwardAh: metric.polarity === null || !row.ampCoverageSeconds ? null : row.forwardAmpSeconds / 3600,
    reverseAh: metric.polarity === null || !row.ampCoverageSeconds ? null : row.reverseAmpSeconds / 3600,
    ...row };
}
module.exports = { HistoryStore, monthBefore, sqlite, summary };
