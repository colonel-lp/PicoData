'use strict';

const fs = require('node:fs');
const { EventEmitter } = require('node:events');

function readMqttConfig(filename) {
  const config = Object.create(null);
  // Match Python's line.strip().split('=', 1), including '=' in passwords.
  for (const line of fs.readFileSync(filename, 'utf8').split(/\r?\n/)) {
    const text = line.trim(), index = text.indexOf('=');
    if (index !== -1) config[text.slice(0, index)] = text.slice(index + 1);
  }
  for (const key of ['server', 'port', 'prefix', 'username', 'password']) {
    if (!Object.hasOwn(config, key)) throw new Error('Missing MQTT configuration key: ' + key);
  }
  if (!config.server || /[\s/\0]/.test(config.server)) throw new Error('Invalid MQTT server: use a hostname or IP address');
  if (!/^\s*\+?\d+\s*$/.test(config.port)) throw new Error('Invalid MQTT port');
  config.port = Number(config.port);
  if (config.port < 1 || config.port > 65535) throw new Error('Invalid MQTT port');
  if (!config.prefix || /[\0+#]/.test(config.prefix) || Buffer.byteLength(config.prefix) > 65535) {
    throw new Error('Invalid MQTT prefix: use the exact publishing topic');
  }
  return config;
}

class MqttPublisher extends EventEmitter {
  constructor(config, { connect, reconnectPeriod = 5000, connectTimeout = 10000, resubscribe = true } = {}) {
    super();
    this.config = config;
    this.connect = connect || (options => require('mqtt').connect(options));
    this.options = { host: config.server, port: config.port, protocol: 'mqtt',
      username: config.username, password: config.password, protocolVersion: 4,
      keepalive: 60, clean: true, reconnectPeriod, connectTimeout,
      reconnectOnConnackError: true, queueQoSZero: false, resubscribe };
    this.client = null; this.stopped = false; this.pending = null; this.stopTask = null;
  }
  status(state) { this.emit('status', { source: 'mqtt', state }); }
  start() {
    if (this.client || this.stopped) return;
    this.client = this.connect(this.options);
    this.client.on('connect', () => this.status('connected'));
    this.client.on('reconnect', () => this.status('reconnecting'));
    this.client.on('close', () => { this.pending = null; if (!this.stopped) this.status('disconnected'); });
    // Never print connection options or broker errors containing credentials.
    this.client.on('error', () => { if (!this.stopped) this.status('connection-error'); });
    this.status('connecting');
  }
  publish(output) {
    // No offline backlog, retained snapshot, startup resync or stale replay.
    // One pending write bounds memory if the broker stops consuming data.
    if (this.stopped || !this.client?.connected || this.pending) return false;
    const pending = {};
    this.pending = pending;
    try {
      this.client.publish(this.config.prefix, JSON.stringify(output), { qos: 0, retain: false }, err => {
        if (this.pending === pending) this.pending = null;
        if (err && !this.stopped) this.status('publish-error');
      });
    } catch {
      if (this.pending === pending) this.pending = null;
      if (!this.stopped) this.status('publish-error');
      return false;
    }
    return true;
  }
  stop() {
    if (this.stopTask) return this.stopTask;
    this.stopped = true;
    this.stopTask = (async () => {
      if (this.client) await this.client.endAsync(true);
      this.pending = null;
      this.status('stopped');
    })();
    return this.stopTask;
  }
}
module.exports = { readMqttConfig, MqttPublisher };
