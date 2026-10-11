'use strict';

const { EventEmitter } = require('node:events');
const { performance } = require('node:perf_hooks');
const { isDeepStrictEqual } = require('node:util');

const DEFAULT_TOPIC = '/Ella/sbms';
const MAX_PAYLOAD_BYTES = 65536;
const BOOLEAN_FLAGS = ['OV', 'OVLK', 'UV', 'UVLK', 'IOT', 'COC', 'DOC', 'DSC', 'CELF', 'OPEN', 'LVC', 'ECCF', 'CFET', 'EOC', 'DFET'];
function parseSbmsOptions(config) {
  const topic = config.sbms_topic ?? DEFAULT_TOPIC;
  if (!topic || /[\0+#]/.test(topic) || Buffer.byteLength(topic) > 65535 || topic === config.prefix) {
    throw new Error('Invalid SBMS topic');
  }
  let activeCells = null;
  if (config.sbms_cells !== undefined) {
    const values = config.sbms_cells.split(',').map(value => value.trim());
    if (!values.length || values.some(value => !/^[1-8]$/.test(value)) || new Set(values).size !== values.length) {
      throw new Error('Invalid SBMS active cells: use unique channel numbers 1 through 8');
    }
    activeCells = values.map(value => Number(value) - 1);
  }
  const seconds = config.sbms_stale_seconds === undefined ? 30 : Number(config.sbms_stale_seconds);
  if (!Number.isFinite(seconds) || seconds <= 0 || seconds > 86400) throw new Error('Invalid SBMS stale interval');
  return { topic, activeCells, staleTimeoutMs: seconds * 1000 };
}

function decodeSbms(payload, { activeCells = null, receivedAt = new Date(), monotonicMs = performance.now() } = {}) {
  if (!Buffer.isBuffer(payload) || !payload.length || payload.length > MAX_PAYLOAD_BYTES) throw new Error('Invalid SBMS payload size');
  const data = JSON.parse(payload.toString('utf8'));
  if (!data || typeof data !== 'object' || Array.isArray(data)) throw new Error('Invalid SBMS object');
  if (typeof data.soc !== 'number' || !Number.isFinite(data.soc) || data.soc < 0 || data.soc > 100) throw new Error('Invalid SBMS SOC');
  if (!Array.isArray(data.cellsMV) || data.cellsMV.length !== 8 ||
      data.cellsMV.some(value => !Number.isInteger(value) || value < 0 || value > 65535)) throw new Error('Invalid SBMS cells');
  const currents = data.currentMA;
  if (!currents || typeof currents !== 'object' || Array.isArray(currents) ||
      ['battery', 'pv1', 'pv2', 'extLoad'].some(key => !Number.isSafeInteger(currents[key]))) throw new Error('Invalid SBMS currents');
  const limits = { year: [0, 99], month: [1, 12], day: [1, 31], hour: [0, 23], minute: [0, 59], second: [0, 59] };
  if (!data.time || typeof data.time !== 'object' || Array.isArray(data.time) ||
      Object.entries(limits).some(([key, [min, max]]) => !Number.isInteger(data.time[key]) || data.time[key] < min || data.time[key] > max)) {
    throw new Error('Invalid SBMS source time');
  }
  if (activeCells !== null && (!Array.isArray(activeCells) || !activeCells.length ||
      activeCells.some(index => !Number.isInteger(index) || index < 0 || index > 7) || new Set(activeCells).size !== activeCells.length)) {
    throw new Error('Invalid SBMS active-cell map');
  }
  // Never learn the enabled-cell map by discarding zero values: an active
  // cell can become unavailable. Preserve the configured identity instead.
  const voltageValid = activeCells !== null && activeCells.every(index => data.cellsMV[index] > 0 && data.cellsMV[index] <= 10000);
  const sourceTime = Object.fromEntries(Object.keys(limits).map(key => [key, data.time[key]]));
  return {
    source: 'sbms', receivedAt: receivedAt.toISOString(), receivedMonotonicMs: monotonicMs,
    sourceTime, voltage: voltageValid ? activeCells.reduce((sum, index) => sum + data.cellsMV[index], 0) / 1000 : null,
    voltageStatus: activeCells === null ? 'unconfigured' : voltageValid ? 'valid' : 'unavailable',
    stateOfCharge: data.soc,
    current: { battery: currents.battery / 1000, pv1: currents.pv1 / 1000,
      pv2: currents.pv2 / 1000, externalLoad: currents.extLoad / 1000 },
    // Keep every original field and unit for live consumers. Invalid/missing
    // boolean flags are unavailable, never coerced into an Off state.
    broadcast: data,
    flags: Object.fromEntries(BOOLEAN_FLAGS.map(key => [key,
      typeof data.flags?.[key] === 'boolean' ? data.flags[key] : null])),
  };
}

class SbmsReceiver extends EventEmitter {
  constructor({ topic = DEFAULT_TOPIC, activeCells = null, staleTimeoutMs = 30000,
    retryDelayMs = 5000, now = () => new Date(), monotonic = () => performance.now() } = {}) {
    super();
    this.topic = topic; this.activeCells = activeCells; this.staleTimeoutMs = staleTimeoutMs;
    this.retryDelayMs = retryDelayMs; this.now = now; this.monotonic = monotonic;
    this.client = null; this.latest = null; this.state = null; this.stopped = false;
    this.generation = 0; this.sourceIdentity = null; this.staleTimer = null; this.retryTimer = null;
    this.onConnect = () => this.connected();
    this.onClose = () => this.disconnected();
    this.onMessage = (topic, payload, packet) => this.message(topic, payload, packet);
  }
  status(state) {
    if (state === this.state) return;
    this.state = state; this.emit('status', { source: 'sbms', state });
  }
  start(client) {
    if (this.client || this.stopped) return;
    this.client = client;
    client.on('connect', this.onConnect); client.on('close', this.onClose); client.on('message', this.onMessage);
    this.status('waiting-broker');
    if (client.connected) this.connected();
  }
  clearTimers() { clearTimeout(this.staleTimer); clearTimeout(this.retryTimer); }
  connected() {
    if (this.stopped) return;
    this.clearTimers(); this.latest = null;
    const generation = ++this.generation;
    this.status('subscribing'); this.subscribe(generation);
  }
  subscribe(generation) {
    if (this.stopped || generation !== this.generation || !this.client?.connected) return;
    try {
      this.client.subscribe(this.topic, { qos: 0 }, (error, granted) => {
        if (this.stopped || generation !== this.generation || !this.client.connected) return;
        if (error || !granted?.some(value => value.topic === this.topic && value.qos >= 0 && value.qos <= 2)) {
          this.status('subscription-error');
          this.retryTimer = setTimeout(() => this.subscribe(generation), this.retryDelayMs);
          return;
        }
        if (!this.latest) { this.status('waiting-data'); this.armStale(); }
      });
    } catch {
      this.status('subscription-error');
      this.retryTimer = setTimeout(() => this.subscribe(generation), this.retryDelayMs);
    }
  }
  disconnected() {
    if (this.stopped) return;
    ++this.generation; this.clearTimers(); this.latest = null;
    this.status('disconnected');
  }
  armStale() {
    clearTimeout(this.staleTimer);
    this.staleTimer = setTimeout(() => { this.latest = null; this.status('stale'); }, this.staleTimeoutMs);
  }
  message(topic, payload, packet) {
    if (this.stopped || !this.client?.connected || topic !== this.topic) return;
    // A retained snapshot is not a new acquisition, even if its date looks valid.
    if (packet?.retain) { this.emit('diagnostic', 'retained-ignored'); return; }
    let reading;
    try { reading = decodeSbms(payload, { activeCells: this.activeCells, receivedAt: this.now(), monotonicMs: this.monotonic() }); }
    catch {
      // A rejected payload is not a new acquisition and cannot renew freshness.
      // Keep the previous accepted display until its original stale deadline.
      this.emit('diagnostic', 'invalid-message'); this.emit('rejected');
      if (!this.latest) this.status('invalid-message');
      return;
    }
    const identity = reading.broadcast;
    // Include live flags/auxiliary values so changes within one source-clock
    // second are delivered. Reordered object keys are still an exact repeat.
    if (isDeepStrictEqual(identity, this.sourceIdentity)) return;
    this.sourceIdentity = identity; this.latest = reading;
    this.armStale(); this.status('connected'); this.emit('readings', reading);
  }
  stop() {
    if (this.stopped) return;
    this.stopped = true; ++this.generation; this.clearTimers(); this.latest = null;
    if (this.client) {
      this.client.removeListener('connect', this.onConnect);
      this.client.removeListener('close', this.onClose);
      this.client.removeListener('message', this.onMessage);
      if (this.client.connected) { try { this.client.unsubscribe(this.topic, () => {}); } catch {} }
    }
    this.status('stopped');
  }
}
module.exports = { DEFAULT_TOPIC, MAX_PAYLOAD_BYTES, parseSbmsOptions, decodeSbms, SbmsReceiver };
