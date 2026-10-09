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
      if (![0, 1, 2].includes(version)) throw Error('Unsupported history schema');
      this.db.exec(`PRAGMA journal_mode=WAL; PRAGMA synchronous=FULL; PRAGMA busy_timeout=1000; PRAGMA cache_size=-2048;
        CREATE TABLE IF NOT EXISTS metrics(id TEXT PRIMARY KEY, definition TEXT NOT NULL, name TEXT NOT NULL, source_name TEXT NOT NULL);
        CREATE TABLE IF NOT EXISTS names(metric_id TEXT NOT NULL, changed_ms INTEGER NOT NULL, name TEXT NOT NULL, source_name TEXT NOT NULL, PRIMARY KEY(metric_id,changed_ms));
        CREATE TABLE IF NOT EXISTS history(metric_id TEXT NOT NULL, resolution TEXT NOT NULL, start_ms INTEGER NOT NULL, end_ms INTEGER NOT NULL, data TEXT NOT NULL, PRIMARY KEY(metric_id,resolution,start_ms));
        CREATE INDEX IF NOT EXISTS history_expiry ON history(resolution,end_ms);
        CREATE TABLE IF NOT EXISTS meta(key TEXT PRIMARY KEY, value TEXT NOT NULL);
        `);
      for (const m of config.metrics) {
        const { name, ...definition } = m;
        const encoded = JSON.stringify(definition);
        const old = this.db.prepare('SELECT definition FROM metrics WHERE id=?').get(m.id);
        if (old && old.definition !== encoded) throw Error('Existing metric binding changed; migrate history or choose a new metric id');
        this.db.prepare('INSERT OR IGNORE INTO metrics VALUES(?,?,?,?)').run(m.id, encoded, name || m.id, m.source === 'sbms' ? m.field : m.id);
        if (name) this.name(m.id, name, Date.now(), this.db.prepare('SELECT source_name FROM metrics WHERE id=?').get(m.id).source_name);
      }
      this.prepareBattery(version);
      this.getRow = this.db.prepare('SELECT data FROM history WHERE metric_id=? AND resolution=? AND start_ms=?');
      this.putRow = this.db.prepare('INSERT INTO history VALUES(?,?,?,?,?) ON CONFLICT(metric_id,resolution,start_ms) DO UPDATE SET end_ms=excluded.end_ms,data=excluded.data');
      this.getBattery = this.db.prepare('SELECT data FROM battery_history WHERE resolution=? AND start_ms=?');
      this.putBattery = this.db.prepare('INSERT INTO battery_history VALUES(?,?,?,?) ON CONFLICT(resolution,start_ms) DO UPDATE SET end_ms=excluded.end_ms,data=excluded.data');
    } catch (err) { this.close(); throw err; }
  }
  prepareBattery(version) {
    // VACUUM INTO snapshots committed WAL data before upgrading schema 1.
    const backup = this.config.database + '.v1-backup.sqlite';
    if (version === 1 && !fs.existsSync(backup)) {
      const pending = backup + '.pending';
      if (fs.existsSync(pending)) fs.unlinkSync(pending);
      this.db.prepare('VACUUM INTO ?').run(pending);
      fs.chmodSync(pending, 0o600);
      const fd = fs.openSync(pending, 'r');
      try { fs.fsyncSync(fd); } finally { fs.closeSync(fd); }
      fs.renameSync(pending,backup);
    }
    this.db.exec('BEGIN IMMEDIATE');
    try {
      this.db.exec(`CREATE TABLE IF NOT EXISTS battery_fields(channel TEXT PRIMARY KEY, metric_id TEXT UNIQUE NOT NULL);
        CREATE TABLE IF NOT EXISTS battery_history(resolution TEXT NOT NULL, start_ms INTEGER NOT NULL, end_ms INTEGER NOT NULL, data TEXT NOT NULL, PRIMARY KEY(resolution,start_ms));
        CREATE INDEX IF NOT EXISTS battery_expiry ON battery_history(resolution,end_ms);`);
      for (const [channel, id] of Object.entries(this.config.batteryGroup)) {
        const old = this.db.prepare('SELECT metric_id FROM battery_fields WHERE channel=?').get(channel);
        if (old && old.metric_id !== id) throw Error('Existing battery group binding changed; migrate explicitly');
        this.db.prepare('INSERT OR IGNORE INTO battery_fields VALUES(?,?)').run(channel, id);
        const assigned = this.db.prepare('SELECT channel FROM battery_fields WHERE metric_id=?').get(id);
        if (assigned.channel !== channel) throw Error('Metric already belongs to another battery group field');
      }
      this.batteryGroup = Object.fromEntries(this.db.prepare('SELECT * FROM battery_fields').all().map(r => [r.channel, r.metric_id]));
      this.batteryByMetric = new Map(Object.entries(this.batteryGroup).map(([channel,id]) => [id,channel]));
      const find = this.db.prepare('SELECT data FROM battery_history WHERE resolution=? AND start_ms=?');
      const put = this.db.prepare('INSERT INTO battery_history VALUES(?,?,?,?) ON CONFLICT(resolution,start_ms) DO UPDATE SET end_ms=excluded.end_ms,data=excluded.data');
      // Bounded batches keep upgrade memory use small on the Pi.
      const batch = this.db.prepare('SELECT * FROM history WHERE metric_id=? ORDER BY resolution,start_ms LIMIT 256');
      const remove = this.db.prepare('DELETE FROM history WHERE metric_id=? AND resolution=? AND start_ms=?');
      for (const [channel,id] of Object.entries(this.batteryGroup)) {
        let rows;
        while ((rows = batch.all(id)).length) {
          for (const row of rows) {
            const old = find.get(row.resolution, row.start_ms);
            const channels = old ? JSON.parse(old.data) : {};
            if (Object.hasOwn(channels,channel)) throw Error('Duplicate battery interval during migration');
            channels[channel] = JSON.parse(row.data);
            put.run(row.resolution,row.start_ms,row.end_ms,JSON.stringify(channels));
            remove.run(id,row.resolution,row.start_ms);
          }
        }
      }
      this.db.exec('PRAGMA user_version=2; COMMIT;');
    } catch (err) { this.db.exec('ROLLBACK'); throw err; }
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
    const channel = this.batteryByMetric.get(id);
    if (channel) {
      const row = this.getBattery.get(resolution,start);
      return row ? JSON.parse(row.data)[channel] ?? null : null;
    }
    const row = this.getRow.get(id, resolution, start); return row ? JSON.parse(row.data) : null;
  }
  save(rows, time) {
    this.db.exec('BEGIN IMMEDIATE');
    try {
      const combined = new Map();
      for (const row of rows) {
        const channel = this.batteryByMetric.get(row.id);
        if (!channel) { this.putRow.run(row.id,row.resolution,row.start,row.end,JSON.stringify(row.data)); continue; }
        const key = `${row.resolution}:${row.start}`;
        let bucket = combined.get(key);
        if (!bucket) {
          const old = this.getBattery.get(row.resolution,row.start);
          bucket = { resolution:row.resolution,start:row.start,end:row.end,channels:old ? JSON.parse(old.data) : {} };
          combined.set(key,bucket);
        }
        if (bucket.end !== row.end) throw Error('Conflicting battery interval bounds');
        bucket.channels[channel] = row.data;
      }
      for (const row of combined.values()) this.putBattery.run(row.resolution,row.start,row.end,JSON.stringify(row.channels));
      const cutoff = monthBefore(time);
      this.db.prepare(`DELETE FROM battery_history WHERE (resolution='minute' AND end_ms<=?) OR
        (resolution='hour' AND end_ms<=?) OR (resolution='day' AND end_ms<=?)`).run(time-86400000,time-7*86400000,cutoff);
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
