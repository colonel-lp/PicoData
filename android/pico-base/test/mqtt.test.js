'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const os = require('node:os');
const { spawnSync } = require('node:child_process');
const { EventEmitter } = require('node:events');
const { readMqttConfig, MqttPublisher } = require('../lib/mqtt-publisher');
const { decodeReadings, formatEllaJson } = require('../lib/readings');
const { createSensorList } = require('../lib/sensor-list');
const { fixture } = require('./fixtures');
const { mqttBroker } = require('./mqtt-broker');

function waitStatus(publisher, state) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => { cleanup(); reject(new Error('MQTT status timeout: ' + state)); }, 3000);
    const listener = status => { if (status.state === state) { cleanup(); resolve(); } };
    function cleanup() { clearTimeout(timer); publisher.removeListener('status', listener); }
    publisher.on('status', listener);
  });
}
function publishReceived(publisher, broker, data) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => { cleanup(); reject(new Error('MQTT publish timeout')); }, 3000);
    const listener = packet => { cleanup(); resolve(packet); };
    function cleanup() { clearTimeout(timer); broker.events.removeListener('publish', listener); }
    broker.events.on('publish', listener);
    if (!publisher.publish(data)) { cleanup(); reject(new Error('MQTT publish rejected')); }
  });
}
const configFor = broker => ({ server: '127.0.0.1', port: broker.port, prefix: '/Ella/Pico/', username: 'test-user', password: 'test-pass=extra' });

test('existing Python MQTT configuration preserves topic, credentials and equals signs', t => {
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'ella-mqtt-config-'));
  t.after(() => fs.rmSync(directory, { recursive: true, force: true }));
  const file = path.join(directory, 'mqtt');
  fs.writeFileSync(file, 'server=localhost\r\nport=1883\r\nprefix=/Ella/Pico/\r\nusername=test-user\r\npassword=test-pass=extra\r\n');
  const config = readMqttConfig(file);
  assert.equal(config.prefix, '/Ella/Pico/'); assert.equal(config.password, 'test-pass=extra');
  assert.equal(config.port, 1883); assert.equal(config.username, 'test-user');
  fs.writeFileSync(file, 'server=localhost\n');
  assert.throws(() => readMqttConfig(file), /Missing MQTT configuration key: port/);
  for (const port of ['NaN', '0', '65536', '1883.5']) {
    fs.writeFileSync(file, `server=localhost\nport=${port}\nprefix=/Ella/Pico/\nusername=u\npassword=p\n`);
    assert.throws(() => readMqttConfig(file), /Invalid MQTT port/);
  }
});

test('MQTT compatibility output matches original Python including raw 65535 values and labels', () => {
  for (const sentinel of [false, true]) {
    const data = fixture(), sensors = createSensorList(data.config);
    data.config[4][3] = 'Intérieur é  ';
    if (sentinel) {
      data.element[sensors[15].pos][0] = 65535;
      data.element[sensors[15].pos + 2][1] = 65535;
      data.element[sensors[21].pos][1] = 65535;
      data.element[sensors[11].pos][1] = 65535;
    }
    const oracle = spawnSync('python3', [path.join(__dirname, 'python-oracle.py'),
      path.join(__dirname, 'reference/pico-mqtt.py')], { input: JSON.stringify(data), encoding: 'utf8' });
    assert.equal(oracle.status, 0, oracle.stderr);
    const output = formatEllaJson(decodeReadings(createSensorList(data.config), data.element,
      { legacyPython: true }), new Date(2026, 9, 7, 12, 34, 56));
    assert.deepEqual(JSON.parse(JSON.stringify(output)), JSON.parse(oracle.stdout).output);
    assert.ok(Object.hasOwn(output.battery, 'Ella  '));
    assert.ok(Object.hasOwn(output.temperature, 'Intérieur é  '));
    if (sentinel) { assert.equal(output.voltage.Standalone, 65.535); assert.equal(output.battery['Ella  '].state_of_charge, 65535 / 160); }
  }
});

test('MQTT compatibility mode rejects incomplete snapshots instead of inventing values', () => {
  const data = fixture(), sensors = createSensorList(data.config);
  delete data.element[sensors[15].pos + 1];
  assert.throws(() => decodeReadings(sensors, data.element, { legacyPython: true }), /Incomplete/);
});

test('MQTT wire authentication, original publish settings, reconnect and shutdown', async t => {
  const broker = await mqttBroker(t), publisher = new MqttPublisher(configFor(broker), { reconnectPeriod: 40 });
  t.after(() => publisher.stop());
  const messages = [], connections = [];
  broker.events.on('publish', p => messages.push(JSON.parse(p.payload.toString())));
  broker.events.on('connect', p => connections.push(p));
  const connected = waitStatus(publisher, 'connected'); publisher.start(); await connected;
  assert.equal(connections[0].username, 'test-user'); assert.equal(connections[0].password.toString(), 'test-pass=extra');
  assert.equal(connections[0].protocolVersion, 4); assert.equal(connections[0].keepalive, 60);
  assert.equal(connections[0].will, undefined);
  const first = await publishReceived(publisher, broker, { time: { second: 1 }, voltage: { 'Ella  ': 13.24 } });
  assert.equal(first.topic, '/Ella/Pico/'); assert.equal(first.qos, 0); assert.equal(first.retain, false);
  const closed = waitStatus(publisher, 'disconnected'), reconnected = waitStatus(publisher, 'connected');
  broker.drop(); await closed;
  assert.equal(publisher.publish({ stale: true }), false);
  await reconnected;
  await publishReceived(publisher, broker, { fresh: true });
  assert.deepEqual(messages, [{ time: { second: 1 }, voltage: { 'Ella  ': 13.24 } }, { fresh: true }]);
  const count = broker.connections;
  await publisher.stop(); await publisher.stop();
  assert.equal(publisher.publish({ afterStop: true }), false);
  assert.equal(publisher.client.reconnecting, false);
  assert.equal(broker.connections, count);
});

test('MQTT retries denied authentication without terminating the reader or exposing credentials', async t => {
  const broker = await mqttBroker(t); broker.deny();
  const publisher = new MqttPublisher(configFor(broker), { reconnectPeriod: 40 });
  t.after(() => publisher.stop());
  const statuses = []; publisher.on('status', status => statuses.push(status));
  const error = waitStatus(publisher, 'connection-error'); publisher.start(); await error;
  assert.equal(publisher.publish({ offline: true }), false);
  const connected = waitStatus(publisher, 'connected'); broker.allow(); await connected;
  assert.ok(broker.connections >= 2);
  assert.equal(JSON.stringify(statuses).includes('test-pass'), false);
});

test('MQTT bounds pending writes and does not let old callbacks clear a newer write', async () => {
  const fake = new EventEmitter(), callbacks = [], calls = [];
  fake.connected = true;
  fake.publish = (...args) => { calls.push(args); callbacks.push(args[3]); };
  fake.endAsync = async () => { fake.connected = false; fake.emit('close'); };
  const publisher = new MqttPublisher(configFor({ port: 1883 }), { connect: () => fake });
  publisher.start();
  assert.equal(publisher.options.queueQoSZero, false);
  assert.equal(publisher.publish({ first: true }), true);
  for (let i = 0; i < 1000; i++) assert.equal(publisher.publish({ dropped: i }), false);
  assert.equal(calls.length, 1);
  fake.connected = false; fake.emit('close');
  assert.equal(publisher.publish({ offline: true }), false);
  fake.connected = true; fake.emit('connect');
  assert.equal(publisher.publish({ fresh: true }), true);
  callbacks[0]();
  assert.equal(publisher.publish({ stillBusy: true }), false);
  callbacks[1]();
  assert.equal(publisher.publish({ next: true }), true);
  assert.equal(calls.length, 3);
  await publisher.stop();
});
