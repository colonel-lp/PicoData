'use strict';
const { EventEmitter } = require('node:events');
const { performance } = require('node:perf_hooks');
const { validateConfig, picoBinding } = require('./history-config');
const { HistoryStore } = require('./history-store');
function bounds(time, resolution) {
  const d = new Date(time);
  if (resolution === 'month') {
    const start = Date.UTC(d.getUTCFullYear(), d.getUTCMonth(), 1);
    return [start, Date.UTC(d.getUTCFullYear(), d.getUTCMonth() + 1, 1)];
  }
  const size = { minute: 60000, hour: 3600000, day: 86400000 }[resolution];
  const start = Math.floor(time / size) * size; return [start, start + size];
}
function empty() {
  return { ampSeconds: 0, voltSeconds: 0, wattSeconds: 0, ampCoverageSeconds: 0, voltCoverageSeconds: 0,
    powerCoverageSeconds: 0, forwardAmpSeconds: 0, reverseAmpSeconds: 0, forwardWattSeconds: 0,
    reverseWattSeconds: 0, samples: 0, last: null, lastAt: null, min: null, max: null, soc: null, socAt: null };
}
function finite(x) { return typeof x === 'number' && Number.isFinite(x); }
class HistoryLogger extends EventEmitter {
  constructor(config, { automatic = true } = {}) {
    super(); this.config = validateConfig(config); this.store = new HistoryStore(this.config);
    this.rows = new Map(); this.latest = new Map(); this.active = new Set(); this.sbmsVoltage = null;
    this.cursor = null; this.lastCommitMono = null; this.failed = false; this.closed = false;
    if (automatic) this.timer = setInterval(() => this.tick(), 1000);
  }
  status(state, extra = {}) { this.emit('status', { source: 'logging', state, ...extra }); }
  guard(fn) {
    if (this.failed || this.closed) return;
    try { fn(); }
    catch {
      this.failed = true; clearInterval(this.timer); this.rows.clear(); this.latest.clear();
      this.status('failed', { reason: 'History write failed; check storage and configuration, then restart the collector' });
    }
  }
  resolutions(m) { return ['temperature', 'barometer'].includes(m.kind) ? ['hour', 'day'] : ['minute', 'hour', 'day', 'month']; }
  row(m, resolution, time) {
    const [start, end] = bounds(time, resolution), key = `${m.id}:${resolution}:${start}`;
    let row = this.rows.get(key);
    if (!row) {
      row = { id: m.id, resolution, start, end, data: this.store.read(m.id, resolution, start) || empty(), dirty: false };
      this.rows.set(key, row);
    }
    row.dirty = true; return row.data;
  }
  configurePico({ config, sensorList }, time = Date.now()) {
    this.guard(() => {
      // A refreshed configuration invalidates previous Pico inputs immediately.
      for (const m of this.config.metrics.filter(m => m.source === 'pico')) {
        this.latest.delete(m.id); this.active.delete(m.id);
        const sensor = sensorList[m.sensorId];
        if (!sensor || sensor.type !== m.sensorType || (m.expectedBinding !== undefined && m.expectedBinding !== picoBinding(config, m.sensorId, m.sensorType))) {
          this.status('mapping-error', { metric: m.id }); continue;
        }
        this.active.add(m.id); this.store.name(m.id, m.name || sensor.name || m.id, time, sensor.name || m.id);
      }
    });
  }
  input(value, source, mono, valid = finite) { return valid(value) ? { value, source, mono, time: this.cursor.time } : null; }
  acceptPico({ receivedAt, receivedMonotonicMs, readings }) {
    this.accept('pico', receivedAt, receivedMonotonicMs, readings);
  }
  acceptSbms(reading) { this.accept('sbms', reading.receivedAt, reading.receivedMonotonicMs, reading); }
  accept(source, receivedAt, mono, readings) {
    this.guard(() => {
      const time = Date.parse(receivedAt);
      if (!finite(time) || !finite(mono) || (this.cursor && mono < this.cursor.mono)) return;
      this.advance(time, mono);
      if (this.clockHoldUntil && time < this.clockHoldUntil) return;
      if (source === 'sbms') this.sbmsVoltage = this.input(readings.voltageStatus === 'valid' ? readings.voltage : null, source, mono, v => finite(v) && v > 0);
      for (const m of this.config.metrics.filter(m => m.source === source)) {
        if (source === 'pico' && !this.active.has(m.id)) continue;
        const s = source === 'pico' ? readings[m.sensorId] || {} : readings;
        if (source === 'pico' && s.type !== m.sensorType) { this.latest.delete(m.id); continue; }
        const current = source === 'pico' ? s.current : s.current?.[m.field];
        const voltage = m.kind === 'voltage' ? s.voltage : m.voltage === 'self' ? (source === 'sbms' ? (s.voltageStatus === 'valid' ? s.voltage : null) : s.voltage) : null;
        this.latest.set(m.id, { amp: this.input(current, source, mono),
          volt: this.input(voltage, source, mono, v => finite(v) && v > 0) });
        const soc = m.role === 'battery' ? s.stateOfCharge : null;
        const scalar = m.kind === 'barometer' ? s.pressure : m.kind === 'temperature' ? s.temperature : null;
        const validSoc = finite(soc) && soc >= 0 && soc <= 100;
        const validScalar = finite(scalar);
        if (!finite(current) && !finite(voltage) && !validSoc && !validScalar) continue;
        for (const resolution of this.resolutions(m)) {
          const row = this.row(m, resolution, time); row.samples++;
          const latest = this.latest.get(m.id), reference = m.voltage === 'sbms' ? this.sbmsVoltage : latest.volt;
          if (latest.amp) row.currentLastAt = latest.amp.time;
          if (reference) row.voltageLastAt = reference.time;
          if (validSoc && (row.socAt === null || time >= row.socAt)) { row.soc = soc; row.socAt = time; }
          if (validScalar) {
            row.min = row.min === null ? scalar : Math.min(row.min, scalar);
            row.max = row.max === null ? scalar : Math.max(row.max, scalar);
            if (row.lastAt === null || time >= row.lastAt) { row.last = scalar; row.lastAt = time; }
          }
        }
      }
    });
  }
  deadline(component) { return component ? component.mono + this.config.maxGapSeconds[component.source] * 1000 : -Infinity; }
  integrate(m, from, to, seconds, amp, volt) {
    for (const resolution of this.resolutions(m)) {
      let t = from;
      // Allocation uses UTC boundaries; elapsed energy uses monotonic seconds.
      do {
        const [, boundary] = bounds(t, resolution), end = Math.min(to, boundary);
        const duration = to === from ? seconds : seconds * (end - t) / (to - from);
        const row = this.row(m, resolution, t);
        if (amp !== null) {
          row.ampSeconds += amp * duration; row.ampCoverageSeconds += duration;
          if (m.polarity !== null) {
            const signed = amp * m.polarity;
            row.forwardAmpSeconds += Math.max(0, signed) * duration;
            row.reverseAmpSeconds += Math.max(0, -signed) * duration;
          }
        }
        if (volt !== null) { row.voltSeconds += volt * duration; row.voltCoverageSeconds += duration; }
        if (amp !== null && volt !== null && m.kind === 'electrical') {
          const watts = amp * volt; row.wattSeconds += watts * duration; row.powerCoverageSeconds += duration;
          if (m.polarity !== null) {
            row.forwardWattSeconds += Math.max(0, watts * m.polarity) * duration;
            row.reverseWattSeconds += Math.max(0, -watts * m.polarity) * duration;
          }
        }
        t = end;
      } while (t < to);
    }
  }
  advance(time, mono) {
    if (!this.cursor) { this.cursor = { time, mono }; this.lastCommitMono = mono; return; }
    const { time: previousTime, mono: previousMono } = this.cursor;
    const dt = mono - previousMono, wallDt = time - previousTime;
    if (dt < 0) return;
    if (wallDt < 0 || Math.abs(wallDt - dt) > 2000) {
      this.latest.clear(); this.sbmsVoltage = null; this.status('clock-step', { reason: 'Skipped interval after a system clock adjustment' });
      if (wallDt < 0) this.clockHoldUntil = Math.max(this.clockHoldUntil || 0, previousTime);
      this.store.db.prepare("INSERT INTO meta VALUES('clockSteps','1') ON CONFLICT(key) DO UPDATE SET value=CAST(value AS INTEGER)+1").run();
    } else if (this.clockHoldUntil && previousTime < this.clockHoldUntil) {
      this.latest.clear(); this.sbmsVoltage = null;
    } else if (dt > 0) {
      for (const m of this.config.metrics) {
        if (!['electrical', 'voltage'].includes(m.kind)) continue;
        const value = this.latest.get(m.id); if (!value) continue;
        const amp = m.kind === 'electrical' ? value.amp : null;
        const volt = m.voltage === 'sbms' ? this.sbmsVoltage : value.volt;
        const cutoffs = [...new Set([previousMono, Math.min(mono, Math.max(previousMono, this.deadline(amp))),
          Math.min(mono, Math.max(previousMono, this.deadline(volt))), mono])].sort((a, b) => a - b);
        for (let i = 1; i < cutoffs.length; i++) {
          const a = cutoffs[i - 1], b = cutoffs[i]; if (a === b) continue;
          const av = this.deadline(amp) > a ? amp.value : null, vv = this.deadline(volt) > a ? volt.value : null;
          if (av === null && vv === null) continue;
          this.integrate(m, previousTime + wallDt * (a - previousMono) / dt,
            previousTime + wallDt * (b - previousMono) / dt, (b - a) / 1000, av, vv);
        }
      }
    }
    this.cursor = { time, mono };
    if (mono - this.lastCommitMono >= this.config.commitSeconds * 1000) this.flush(time, mono);
  }
  unavailable(source, time = Date.now(), mono = performance.now()) {
    this.guard(() => {
      this.advance(time, mono);
      for (const m of this.config.metrics) if (m.source === source) this.latest.delete(m.id);
      if (source === 'sbms') this.sbmsVoltage = null;
    });
  }
  tick(time = Date.now(), mono = performance.now()) { this.guard(() => this.advance(time, mono)); }
  flush(time = this.cursor?.time ?? Date.now(), mono = this.cursor?.mono ?? performance.now()) {
    this.store.save([...this.rows.values()].filter(r => r.dirty), time);
    for (const [key, row] of this.rows) { row.dirty = false; if (row.end <= time) this.rows.delete(key); }
    this.lastCommitMono = mono;
  }
  close({ time = Date.now(), mono = performance.now(), advance = true } = {}) {
    if (this.closed) return;
    clearInterval(this.timer);
    this.guard(() => { if (advance) this.advance(time, mono); this.flush(this.cursor?.time ?? time); });
    this.store.close(); this.closed = true;
  }
}
module.exports = { HistoryLogger, bounds, empty };
