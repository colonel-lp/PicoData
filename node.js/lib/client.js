'use strict';

// Discovery/configuration sequence derives from _old/pico2signalk/lib/get-pico-config.js.
const dgram = require('node:dgram');
const { EventEmitter } = require('node:events');
const { abortError, delay, getPicoConfigTcp, isPicoPacket, isLivePacket, parseResponse } = require('./pico-protocol');
const { createSensorList } = require('./sensor-list');
const { decodeReadings, formatEllaJson } = require('./readings');

function createDiscoverySocket({ udpPort = 43210, bindAddress = '0.0.0.0', signal } = {}) {
  return new Promise((resolve, reject) => {
    if (signal?.aborted) return reject(abortError());
    const socket = dgram.createSocket({ type: 'udp4', reuseAddr: true });
    const onAbort = () => fail(abortError());
    function cleanup() { socket.removeListener('error', fail); signal?.removeEventListener('abort', onAbort); }
    function fail(err) { cleanup(); try { socket.close(); } catch {} reject(err); }
    socket.on('error', fail);
    signal?.addEventListener('abort', onAbort, { once: true });
    socket.bind(udpPort, bindAddress, () => {
      if (signal?.aborted) return fail(abortError());
      socket.setBroadcast(true); cleanup();
      socket.on('error', () => {}); // Runtime handler is attached by PicoClient.
      resolve(socket);
    });
  });
}
function waitForBroadcast(socket, { signal, discoveryTimeoutMs = 60000 } = {}) {
  return new Promise((resolve, reject) => {
    if (signal?.aborted) return reject(abortError());
    function finish(err, ip) {
      clearTimeout(timer); socket.removeListener('message', onMessage);
      socket.removeListener('error', onError); signal?.removeEventListener('abort', onAbort);
      err ? reject(err) : resolve(ip);
    }
    const onMessage = (msg, info) => { if (isPicoPacket(msg)) finish(null, info.address); };
    const onError = err => finish(err);
    const onAbort = () => finish(abortError());
    const timer = setTimeout(() => finish(new Error('No Pico broadcast received')), discoveryTimeoutMs);
    socket.on('message', onMessage); socket.once('error', onError);
    signal?.addEventListener('abort', onAbort, { once: true });
  });
}

class PicoClient extends EventEmitter {
  constructor(options = {}) {
    super();
    this.options = { udpPort: 43210, port: 5001, configRetryMs: 30000,
      staleAfterMs: 15000, updateIntervalMs: 1000, ...options };
    for (const key of ['udpPort', 'port', 'configRetryMs', 'staleAfterMs', 'updateIntervalMs']) {
      if (!Number.isFinite(this.options[key]) || this.options[key] < 0) throw new Error('Invalid option ' + key);
    }
    this.controller = null; this.task = null; this.sensorList = null;
  }
  start() {
    if (this.task) return this.task;
    this.controller = new AbortController();
    this.task = this.run(this.controller.signal).finally(() => { this.task = null; });
    return this.task;
  }
  async stop() {
    this.controller?.abort();
    await this.task;
  }
  status(state, extra = {}) { this.emit('status', { state, ...extra }); }
  async run(signal) {
    while (!signal.aborted) {
      try { await this.cycle(signal); }
      catch (err) {
        if (signal.aborted) break;
        this.sensorList = null;
        this.status('retrying', { reason: err.message });
        try { await delay(this.options.configRetryMs, signal); }
        catch { break; }
      }
    }
    this.sensorList = null;
    this.status('stopped');
  }
  async cycle(parentSignal) {
    const controller = new AbortController(), signal = controller.signal;
    const onParentAbort = () => controller.abort();
    parentSignal.addEventListener('abort', onParentAbort, { once: true });
    if (parentSignal.aborted) controller.abort();
    let socket, latest;
    try {
      this.status('discovering');
      socket = await createDiscoverySocket({ ...this.options, signal });
      socket.on('error', err => controller.abort(err));
      socket.on('message', (msg, info) => { if (isLivePacket(msg)) latest = { msg, info }; });
      const ip = this.options.picoIp || await waitForBroadcast(socket, { ...this.options, signal });
      this.status('configuring', { ip });
      const config = await getPicoConfigTcp(ip, { ...this.options, signal,
        debug: text => this.emit('diagnostic', text), onTcp: data => this.emit('tcp', data) });
      const sensorList = createSensorList(config);
      this.sensorList = sensorList;
      this.emit('config', { picoIp: ip, config, sensorList });
      this.status('waiting-data', { ip });
      await this.live(socket, ip, sensorList, signal, latest);
    } finally {
      controller.abort(); parentSignal.removeEventListener('abort', onParentAbort);
      if (socket) { socket.removeAllListeners(); try { socket.close(); } catch {} }
    }
  }
  live(socket, ip, sensorList, signal, latest) {
    return new Promise((resolve, reject) => {
      if (signal.aborted) return reject(signal.reason || abortError());
      let timer, lastOutput = -Infinity, active = false, settled = false;
      function finish(err) {
        if (settled) return;
        settled = true; clearTimeout(timer);
        socket.removeListener('message', onMessage); socket.removeListener('error', onError);
        signal.removeEventListener('abort', onAbort);
        err ? reject(err) : resolve();
      }
      const onAbort = () => finish(signal.reason || abortError());
      const onError = err => finish(err);
      const resetTimer = () => {
        clearTimeout(timer);
        timer = setTimeout(() => {
          this.status('stale', { ip }); finish(new Error('Pico live data timed out'));
        }, this.options.staleAfterMs);
      };
      const onMessage = (msg, info) => {
        if (info.address !== ip || !isLivePacket(msg)) return;
        let element;
        try { element = parseResponse(msg); }
        catch (err) { this.emit('diagnostic', 'Ignored live packet: ' + err.message); return; }
        // An unrelated/empty message cannot keep the connection looking fresh.
        if (!Object.values(sensorList).some(s => Array.isArray(element[s.pos]))) return;
        resetTimer();
        this.emit('packet', { picoIp: ip, receivedAt: new Date().toISOString(), hex: msg.toString('hex') });
        if (!active) { active = true; this.status('connected', { ip }); }
        if (Date.now() - lastOutput < this.options.updateIntervalMs) return;
        let readings;
        try { readings = decodeReadings(sensorList, element, { legacyPython: this.options.legacyPythonOutput }); }
        catch (err) { this.emit('diagnostic', 'Skipped output: ' + err.message); return; }
        this.emit('readings', formatEllaJson(readings));
        lastOutput = Date.now();
      };
      socket.on('message', onMessage); socket.once('error', onError);
      signal.addEventListener('abort', onAbort, { once: true });
      resetTimer();
      if (latest) onMessage(latest.msg, latest.info);
    });
  }
}
module.exports = { createDiscoverySocket, waitForBroadcast, PicoClient };
