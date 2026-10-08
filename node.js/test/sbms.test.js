'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { EventEmitter } = require('node:events');
const { spawn, spawnSync } = require('node:child_process');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const dgram = require('node:dgram');
const { MAX_PAYLOAD_BYTES, parseSbmsOptions, decodeSbms, SbmsReceiver } = require('../lib/sbms');
const { MqttPublisher } = require('../lib/mqtt-publisher');
const { mqttBroker } = require('./mqtt-broker');

// Invented fixture, not the owner's readings or enabled-cell inventory.
const fixture = (second = 1) => ({ time: { year: 1, month: 2, day: 3, hour: 4, minute: 5, second },
  soc: 63, cellsMV: [3200, 3300, 3400, 3500, 0, 0, 0, 0],
  currentMA: { battery: -4200, pv1: 1700, pv2: 0, extLoad: 4500 },
  flags: { CFET: true }, ad2: 0 });
const bytes = data => Buffer.from(JSON.stringify(data));
const configFor = broker => ({ server: '127.0.0.1', port: broker.port, prefix: '/Ella/Pico/', username: 'test-user', password: 'test-pass=extra' });
function event(emitter, name, predicate = () => true, timeoutMs = 3000) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => { cleanup(); reject(new Error('SBMS event timeout: ' + name)); }, timeoutMs);
    const listener = value => { if (predicate(value)) { cleanup(); resolve(value); } };
    function cleanup() { clearTimeout(timer); emitter.removeListener(name, listener); }
    emitter.on(name, listener);
  });
}
const status = (receiver, state) => event(receiver, 'status', value => value.state === state);
function fakeClient() {
  const client = new EventEmitter(); client.connected = true;
  client.subscribe = (topic, options, callback) => callback(null, [{ topic, qos: 0 }]);
  client.unsubscribe = (topic, callback) => callback();
  return client;
}

test('SBMS config validates exact topic, private cell map and stale interval', () => {
  assert.deepEqual(parseSbmsOptions({ prefix: '/Ella/Pico/' }), { topic: '/Ella/sbms', activeCells: null, staleTimeoutMs: 30000 });
  const config = { prefix: '/Ella/Pico/', sbms_topic: '/custom/sbms', sbms_cells: '1, 2,3,4', sbms_stale_seconds: '4' };
  assert.deepEqual(parseSbmsOptions(config), { topic: '/custom/sbms', activeCells: [0, 1, 2, 3], staleTimeoutMs: 4000 });
  for (const value of ['', '0', '9', '1,1', '1,', '1.5', '1 2']) assert.throws(() => parseSbmsOptions({ ...config, sbms_cells: value }));
  for (const value of ['', '/Ella/Pico/', 'some/+', '#']) assert.throws(() => parseSbmsOptions({ ...config, sbms_topic: value }));
  for (const value of ['0', '-1', 'NaN', 'Infinity', '86401']) assert.throws(() => parseSbmsOptions({ ...config, sbms_stale_seconds: value }));
});

test('SBMS converts all mA channels, preserves signs/zero PV2 and uses Pi receipt time', () => {
  const output = decodeSbms(bytes(fixture()), { activeCells: [0, 1, 2, 3],
    receivedAt: new Date('2025-02-03T04:05:06Z'), monotonicMs: 123 });
  assert.equal(output.receivedAt, '2025-02-03T04:05:06.000Z');
  assert.equal(output.receivedMonotonicMs, 123); assert.equal(output.sourceTime.year, 1);
  assert.equal(output.stateOfCharge, 63); assert.equal(output.voltage, 13.4); assert.equal(output.voltageStatus, 'valid');
  assert.deepEqual(output.current, { battery: -4.2, pv1: 1.7, pv2: 0, externalLoad: 4.5 });
  const charging = fixture(); charging.currentMA.battery = 2800; charging.currentMA.pv2 = 750;
  assert.equal(decodeSbms(bytes(charging)).current.battery, 2.8);
  assert.equal(decodeSbms(bytes(charging)).current.pv2, 0.75);
  assert.equal(Object.hasOwn(output, 'flags'), false);
});

test('SBMS voltage requires configured cells and does not silently discard a failed active cell', () => {
  assert.equal(decodeSbms(bytes(fixture())).voltage, null);
  assert.equal(decodeSbms(bytes(fixture())).voltageStatus, 'unconfigured');
  for (const value of [0, 65535, 10001]) {
    const data = fixture(); data.cellsMV[1] = value;
    const output = decodeSbms(bytes(data), { activeCells: [0, 1, 2, 3] });
    assert.equal(output.voltage, null); assert.equal(output.voltageStatus, 'unavailable');
    assert.equal(output.current.battery, -4.2);
  }
  assert.throws(() => decodeSbms(bytes(fixture()), { activeCells: [] }));
  assert.throws(() => decodeSbms(bytes(fixture()), { activeCells: [0, 0] }));
});

test('SBMS rejects malformed, oversized, incomplete and coerced measurements', () => {
  for (const payload of [Buffer.alloc(0), Buffer.alloc(MAX_PAYLOAD_BYTES + 1), Buffer.from('{bad'), Buffer.from('null'), Buffer.from('[]')]) {
    assert.throws(() => decodeSbms(payload));
  }
  const mutations = [d => { delete d.currentMA.pv2; }, d => { d.currentMA.battery = '12'; },
    d => { d.currentMA.pv1 = null; }, d => { d.currentMA.pv1 = 1.5; }, d => { d.soc = 101; },
    d => { d.soc = true; }, d => { d.cellsMV.pop(); }, d => { d.cellsMV[0] = -1; },
    d => { d.cellsMV[0] = '3200'; }, d => { d.time.second = 60; }, d => { delete d.time; }];
  for (const mutate of mutations) { const data = fixture(); mutate(data); assert.throws(() => decodeSbms(bytes(data))); }
});

test('SBMS ignores unrelated/retained/repeated messages and becomes stale until genuinely fresh data', async t => {
  const client = fakeClient(), receiver = new SbmsReceiver({ staleTimeoutMs: 60, activeCells: [0, 1, 2, 3] });
  t.after(() => receiver.stop()); const readings = []; receiver.on('readings', value => readings.push(value));
  receiver.start(client);
  client.emit('message', '/other', bytes(fixture()), { retain: false });
  client.emit('message', '/Ella/sbms', bytes(fixture()), { retain: true });
  assert.equal(readings.length, 0);
  client.emit('message', '/Ella/sbms', bytes(fixture()), { retain: false });
  assert.equal(readings.length, 1);
  const stale = status(receiver, 'stale');
  const timer = setInterval(() => client.emit('message', '/Ella/sbms', bytes(fixture()), { retain: false }), 10);
  t.after(() => clearInterval(timer)); await stale; clearInterval(timer);
  assert.equal(receiver.latest, null); assert.equal(readings.length, 1);
  client.emit('message', '/Ella/sbms', bytes(fixture()), { retain: false });
  assert.equal(receiver.latest, null);
  client.emit('message', '/Ella/sbms', bytes(fixture(2)), { retain: false });
  assert.equal(readings.length, 2); assert.equal(receiver.state, 'connected');
});

test('SBMS malformed data does not keep the previous reading fresh', async t => {
  const client = fakeClient(), receiver = new SbmsReceiver({ staleTimeoutMs: 40 });
  t.after(() => receiver.stop()); receiver.start(client);
  client.emit('message', '/Ella/sbms', bytes(fixture()), {});
  const stale = status(receiver, 'stale');
  client.emit('message', '/Ella/sbms', Buffer.from('bad-json'), {});
  await stale; assert.equal(receiver.latest, null);
});

test('SBMS reconnect does not turn the previous source-time token into a fresh reading', t => {
  const client = fakeClient(), receiver = new SbmsReceiver();
  t.after(() => receiver.stop()); let count = 0; receiver.on('readings', () => count++);
  receiver.start(client); client.emit('message', '/Ella/sbms', bytes(fixture()), {});
  client.connected = false; client.emit('close');
  client.connected = true; client.emit('connect');
  client.emit('message', '/Ella/sbms', bytes(fixture()), {});
  assert.equal(receiver.latest, null); assert.equal(count, 1);
  client.emit('message', '/Ella/sbms', bytes(fixture(2)), {});
  assert.equal(count, 2); assert.equal(receiver.state, 'connected');
});

test('SBMS accepts changed electrical readings within the same source-clock second', t => {
  const client = fakeClient(), receiver = new SbmsReceiver(); t.after(() => receiver.stop());
  let count = 0; receiver.on('readings', () => count++); receiver.start(client);
  client.emit('message', '/Ella/sbms', bytes(fixture()), {});
  const changed = fixture(); changed.currentMA.battery = -4300;
  client.emit('message', '/Ella/sbms', bytes(changed), {});
  client.emit('message', '/Ella/sbms', bytes(changed), {});
  assert.equal(count, 2); assert.equal(receiver.latest.current.battery, -4.3);
});

test('SBMS closes listeners/timers and ignores late subscription callbacks', () => {
  const client = fakeClient(), callbacks = [];
  client.subscribe = (topic, options, callback) => callbacks.push(callback);
  const receiver = new SbmsReceiver(); receiver.start(client);
  client.connected = false; client.emit('close');
  client.connected = true; client.emit('connect');
  callbacks[0](null, [{ topic: '/Ella/sbms', qos: 0 }]);
  assert.equal(receiver.state, 'subscribing');
  receiver.stop(); receiver.stop();
  callbacks[1](new Error('private broker error'));
  client.emit('message', '/Ella/sbms', bytes(fixture()), {});
  assert.equal(receiver.state, 'stopped'); assert.equal(receiver.latest, null);
  assert.equal(client.listenerCount('message'), 0); assert.equal(client.listenerCount('connect'), 0); assert.equal(client.listenerCount('close'), 0);
});

test('SBMS wire subscription shares Pico publishing, retries refusal and resubscribes after reconnect', async t => {
  const broker = await mqttBroker(t), publisher = new MqttPublisher(configFor(broker), { reconnectPeriod: 40, resubscribe: false });
  const receiver = new SbmsReceiver({ activeCells: [0, 1, 2, 3], retryDelayMs: 40 });
  t.after(async () => { receiver.stop(); await publisher.stop(); });
  broker.denySubscriptions(); const failed = status(receiver, 'subscription-error');
  const subscriptions = []; broker.events.on('subscribe', packet => subscriptions.push(packet));
  publisher.start(); receiver.start(publisher.client); await failed;
  const published = event(broker.events, 'publish'); assert.equal(publisher.publish({ originalPico: true }), true);
  assert.equal((await published).topic, '/Ella/Pico/');
  const waiting = status(receiver, 'waiting-data'); broker.allowSubscriptions(); await waiting;
  assert.ok(subscriptions.length >= 2);
  assert.deepEqual(subscriptions[0].subscriptions, [{ topic: '/Ella/sbms', qos: 0 }]);
  const first = event(receiver, 'readings'); broker.send('/Ella/sbms', fixture());
  assert.equal((await first).voltage, 13.4);
  const disconnected = status(receiver, 'disconnected'); broker.drop(); await disconnected;
  assert.equal(receiver.latest, null);
  await status(receiver, 'waiting-data');
  const second = event(receiver, 'readings'); broker.send('/Ella/sbms', fixture(2));
  assert.equal((await second).current.pv1, 1.7);
  assert.ok(subscriptions.length >= 3);
  assert.ok(broker.connections >= 2);
});

test('actual CLI silently receives SBMS with no Pico and offers opt-in SBMS diagnostics/capture', async t => {
  const broker = await mqttBroker(t), directory = fs.mkdtempSync(path.join(os.tmpdir(), 'ella-sbms-cli-'));
  t.after(() => fs.rmSync(directory, { recursive: true, force: true }));
  const config = path.join(directory, 'mqtt');
  fs.writeFileSync(config, `server=127.0.0.1\nport=${broker.port}\nprefix=/Ella/Pico/\nusername=test-user\npassword=test-pass=extra\nsbms_cells=1,2,3,4\n`);
  for (const print of [false, true]) {
    const socket = dgram.createSocket('udp4');
    await new Promise(resolve => socket.bind(0, '127.0.0.1', resolve));
    const port = socket.address().port; await new Promise(resolve => socket.close(resolve));
    const capture = path.join(directory, `capture-${print}.jsonl`);
    const child = spawn(process.execPath, [path.join(__dirname, '../bin/pico.js'), '--mqtt-config', config,
      '--ip', '127.0.0.1', '--tcp-port', '1', '--udp-port', String(port), '--duration', '0.8', '--record', capture,
      ...(print ? ['--sbms-stdout'] : [])]);
    t.after(() => { if (child.exitCode === null) child.kill(); });
    let stdout = '', stderr = '', second = 1;
    child.stdout.on('data', data => { stdout += data; }); child.stderr.on('data', data => { stderr += data; });
    const timer = setInterval(() => broker.send('/Ella/sbms', fixture(second++ % 60)), 50);
    t.after(() => clearInterval(timer));
    const code = await new Promise((resolve, reject) => { child.on('error', reject); child.on('close', resolve); });
    clearInterval(timer);
    assert.equal(code, 0, stderr);
    assert.ok(stderr.includes('"source":"sbms","state":"connected"'), stderr);
    assert.ok(stderr.includes('"source":"sbms","state":"stopped"'));
    assert.equal(stderr.includes('test-pass'), false);
    const records = fs.readFileSync(capture, 'utf8').trim().split('\n').map(line => JSON.parse(line));
    const received = records.filter(record => record.kind === 'sbms').map(record => record.reading);
    assert.ok(received.length > 0); assert.equal(received[0].voltage, 13.4);
    assert.equal(fs.statSync(capture).mode & 0o777, 0o600);
    if (print) {
      const lines = stdout.trim().split('\n').map(line => JSON.parse(line));
      assert.deepEqual(lines, received);
    } else assert.equal(stdout, '');
  }
});

test('CLI rejects SBMS flag/config mistakes without credentials and can disable SBMS independently', async t => {
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'ella-sbms-options-'));
  t.after(() => fs.rmSync(directory, { recursive: true, force: true }));
  const broker = await mqttBroker(t), config = path.join(directory, 'mqtt'), script = path.join(__dirname, '../bin/pico.js');
  fs.writeFileSync(config, `server=127.0.0.1\nport=${broker.port}\nprefix=/Ella/Pico/\nusername=test-user\npassword=test-pass=extra\nsbms_cells=0,9\n`);
  const invalid = spawnSync(process.execPath, [script, '--mqtt-config', config], { encoding: 'utf8' });
  assert.equal(invalid.status, 1); assert.equal(invalid.stdout, ''); assert.equal(invalid.stderr.includes('test-pass'), false);
  for (const flags of [['--no-mqtt', '--sbms-stdout'], ['--no-sbms', '--sbms-stdout']]) {
    const result = spawnSync(process.execPath, [script, ...flags], { encoding: 'utf8' });
    assert.equal(result.status, 2); assert.equal(result.stdout, '');
  }
  const socket = dgram.createSocket('udp4'); await new Promise(resolve => socket.bind(0, '127.0.0.1', resolve));
  const port = socket.address().port; await new Promise(resolve => socket.close(resolve));
  let subscribed = false; broker.events.on('subscribe', () => { subscribed = true; });
  const child = spawn(process.execPath, [script, '--mqtt-config', config, '--no-sbms', '--ip', '127.0.0.1',
    '--tcp-port', '1', '--udp-port', String(port), '--duration', '0.5']);
  t.after(() => { if (child.exitCode === null) child.kill(); }); let stderr = '', stdout = '';
  child.stderr.on('data', data => { stderr += data; }); child.stdout.on('data', data => { stdout += data; });
  const code = await new Promise((resolve, reject) => { child.on('error', reject); child.on('close', resolve); });
  assert.equal(code, 0, stderr); assert.equal(subscribed, false); assert.equal(stdout, '');
  assert.ok(stderr.includes('"source":"mqtt","state":"connected"')); assert.equal(stderr.includes('"source":"sbms"'), false);
});
