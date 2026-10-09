'use strict';
const { sqlite, summary, monthBefore } = require('./history-store');
const { combinedSummary, fieldSources } = require('./battery-history');
const iso = t => new Date(t).toISOString();
class ApiError extends Error {
  constructor(status, code) { super(code); this.status = status; this.code = code; }
}
function timestamp(value) {
  if (typeof value !== 'string' || !/^\d{4}-\d\d-\d\dT\d\d:\d\d:\d\d(?:\.\d{3})?Z$/.test(value)) throw new ApiError(400,'invalid_time');
  const time = Date.parse(value);
  if (!Number.isFinite(time) || iso(time) !== (value.includes('.') ? value : value.replace('Z','.000Z'))) throw new ApiError(400,'invalid_time');
  return time;
}
function range(params, now, kind = 'electrical') {
  for (const key of params.keys()) if (!['from','to','resolution','limit'].includes(key) || params.getAll(key).length !== 1) throw new ApiError(400,'invalid_query');
  const resolution = params.get('resolution') ?? (['temperature','barometer'].includes(kind) ? 'hour' : 'minute');
  if (!['minute','hour','day','month'].includes(resolution) || (['temperature','barometer'].includes(kind) && !['hour','day'].includes(resolution))) throw new ApiError(400,'invalid_resolution');
  const rawLimit = params.get('limit') ?? '500';
  if (!/^[1-9]\d{0,3}$/.test(rawLimit) || Number(rawLimit) > 1000) throw new ApiError(400,'invalid_limit');
  const to = params.has('to') ? timestamp(params.get('to')) : now;
  const defaults = { minute: to-86400000, hour: ['temperature','barometer'].includes(kind) ? monthBefore(to) : to-7*86400000, day: monthBefore(to) };
  const year = new Date(to); year.setUTCFullYear(year.getUTCFullYear()-1);
  const from = params.has('from') ? timestamp(params.get('from')) : defaults[resolution] ?? year.getTime();
  if (from >= to) throw new ApiError(400,'invalid_range');
  return { from, to, resolution, limit:Number(rawLimit) };
}
class HistoryReader {
  constructor(database) {
    this.db = new (sqlite())(database,{readOnly:true});
    try {
      this.db.exec('PRAGMA busy_timeout=250; PRAGMA cache_size=-1024;');
      if (this.db.prepare('PRAGMA user_version').get().user_version !== 2) throw Error('API requires history schema 2');
    } catch (err) { this.close(); throw err; }
  }
  view(fn) {
    this.db.exec('BEGIN');
    try { const result = fn(); this.db.exec('COMMIT'); return result; }
    catch (err) { this.db.exec('ROLLBACK'); throw err; }
  }
  metadata() {
    const metrics = this.db.prepare('SELECT * FROM metrics ORDER BY id').all();
    const group = Object.fromEntries(this.db.prepare('SELECT * FROM battery_fields').all().map(r=>[r.channel,r.metric_id]));
    const meta = Object.fromEntries(this.db.prepare('SELECT * FROM meta').all().map(r=>[r.key,r.value]));
    return { metrics, group, savedAt:meta.savedAt ?? null, clockSteps:Number(meta.clockSteps || 0) };
  }
  catalogue(meta = this.metadata()) {
    const channels = new Map(Object.entries(meta.group).map(([channel,id])=>[id,channel]));
    return { schemaVersion:2, timezone:'UTC', savedAt:meta.savedAt, battery:{sources:fieldSources(meta.group,meta.metrics)}, metrics:meta.metrics.map(m=> {
      const { expectedBinding, ...definition } = JSON.parse(m.definition);
      const electrical = definition.kind === 'electrical', environmental = ['temperature','barometer'].includes(definition.kind);
      return { ...definition, name:m.name, sourceName:m.source_name, batteryChannel:channels.get(m.id) ?? null,
        units:electrical ? {values:definition.role==='battery' ? ['W','A','V','%'] : ['W','A','V'],energy:'Wh',charge:'Ah'} : {value:{voltage:'V',temperature:'°C',barometer:'hPa'}[definition.kind]},
        resolutions:environmental ? ['hour','day'] : ['minute','hour','day','month'],
        retention:environmental ? {hour:'1 calendar month',day:null} : {minute:'24 hours',hour:'7 days',day:'1 calendar month',month:null} };
    }) };
  }
  history(metricId, params, now) {
    return this.view(()=> {
      const meta = this.metadata(), metric = metricId ? meta.metrics.find(m=>m.id===metricId) : null;
      if (metricId && !metric) throw new ApiError(404,'unknown_metric');
      const definition = metric ? JSON.parse(metric.definition) : null, query = range(params,now,definition?.kind);
      let rows;
      const channel = metricId && Object.entries(meta.group).find(([,id])=>id===metricId)?.[0];
      if (!metricId) rows = this.db.prepare('SELECT * FROM battery_history WHERE resolution=? AND start_ms>=? AND start_ms<? ORDER BY start_ms LIMIT ?').all(query.resolution,query.from,query.to,query.limit+1);
      else if (channel) rows = this.db.prepare("SELECT start_ms,end_ms,json_extract(data,?) AS data FROM battery_history WHERE resolution=? AND start_ms>=? AND start_ms<? AND json_type(data,?)='object' ORDER BY start_ms LIMIT ?").all('$.'+channel,query.resolution,query.from,query.to,'$.'+channel,query.limit+1);
      else rows = this.db.prepare('SELECT * FROM history WHERE metric_id=? AND resolution=? AND start_ms>=? AND start_ms<? ORDER BY start_ms LIMIT ?').all(metricId,query.resolution,query.from,query.to,query.limit+1);
      const more = rows.length > query.limit; rows = rows.slice(0,query.limit);
      const saved = meta.savedAt ? Date.parse(meta.savedAt) : -Infinity;
      return { schemaVersion:2, timezone:'UTC', savedAt:meta.savedAt,
        ...(metric ? {metric:this.catalogue(meta).metrics.find(m=>m.id===metricId)} : {sources:fieldSources(meta.group,meta.metrics)}),
        query:{from:iso(query.from),to:iso(query.to),resolution:query.resolution,limit:query.limit},
        next:more ? {from:iso(rows.at(-1).end_ms),to:iso(query.to),resolution:query.resolution,limit:query.limit} : null,
        rows:rows.map(row=>({start:iso(row.start_ms),end:iso(row.end_ms),partial:row.end_ms>saved,
          ...(metric ? summary(definition,JSON.parse(row.data)) : combinedSummary(meta.group,meta.metrics,JSON.parse(row.data))) })) };
    });
  }
  close() { this.db?.close(); this.db = null; }
}
module.exports = { HistoryReader, ApiError, range };
