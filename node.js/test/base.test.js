'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const path = require('node:path');
const net = require('node:net');
const dgram = require('node:dgram');
const { EventEmitter } = require('node:events');
const { spawnSync, spawn } = require('node:child_process');
const fs = require('node:fs');
const os = require('node:os');
const { addCrc, hexdump, frameLength, parseResponse, openTcp, sendReceive } = require('../lib/pico-protocol');
const { createSensorList } = require('../lib/sensor-list');
const { decodeReadings, formatEllaJson, roundEven } = require('../lib/readings');
const { PicoClient, waitForBroadcast, createDiscoverySocket } = require('../lib/client');
const { fields, frame, fixture } = require('./fixtures');
const { verifyCapture } = require('../bin/verify-capture');
const { mqttBroker } = require('./mqtt-broker');
const baseline = path.join(__dirname, 'reference/pico-mqtt.py');
const normalized = data => JSON.parse(JSON.stringify(data));
function awaitEvent(emitter, name, predicate = () => true, timeoutMs = 3000) {
  return new Promise((resolve, reject) => {
    const timeout = setTimeout(() => { cleanup(); reject(new Error('Timed out: ' + name)); }, timeoutMs);
    const listener = value => { if (predicate(value)) { cleanup(); resolve(value); } };
    function cleanup() { clearTimeout(timeout); emitter.removeListener(name, listener); }
    emitter.on(name, listener);
  });
}

test('upstream CRC request golden vectors remain unchanged', () => {
  for (const [number, expected] of [[0xa8c0, 'a8 c0'], [0xcf, '00 cf'], [0x0ce7, '0c e7'], [0, '00 00']]) {
    assert.equal(hexdump(number), expected);
  }
  const request = addCrc('00 00 00 00 00 ff 41 04 8c 55 4b 00 16 ff 00 01 00 00 00 1a ff 01 03 00 00 00 00 ff 00 00 00 00 ff');
  assert.ok(request.endsWith('0c e7'));
  const buffer = Buffer.from(request.replaceAll(' ', ''), 'hex');
  assert.equal(frameLength(buffer), buffer.length);
});

test('sensor positions, decoded values and Ella JSON agree with actual Python source', () => {
  for (const seed of [0, 1, 2, 5, 12, 30]) {
    const data = fixture(seed);
    const oracle = spawnSync('python3', [path.join(__dirname, 'python-oracle.py'), baseline], {
      input: JSON.stringify(data), encoding: 'utf8',
    });
    assert.equal(oracle.status, 0, oracle.stderr);
    const original = JSON.parse(oracle.stdout);
    const sensors = createSensorList(data.config);
    // New type-14 name is intentionally retained from the upstream fork.
    assert.equal(sensors[19].type, 'XX');
    const oldSensors = normalized(sensors); oldSensors[19].type = 14;
    assert.deepEqual(oldSensors, original.sensorList);
    const decoded = decodeReadings(sensors, parseResponse(data.packet));
    const oldDecoded = normalized(decoded); oldDecoded[19].type = 14;
    assert.deepEqual(normalized(oldDecoded), normalized(original.readings));
    assert.deepEqual(normalized(formatEllaJson(decoded, new Date(2026, 9, 7, 12, 34, 56))), normalized(original.output));
  }
});

test('Python ties-to-even rounding, exact labels and signed temperature edge cases', () => {
  assert.deepEqual([44.5, 45.5, -2.5, -3.5].map(roundEven), [44, 46, -2, -4]);
  const data = fixture(); const sensors = createSensorList(data.config);
  data.element[sensors[11].pos] = [0, 65535]; // -0.1 C, not an invalid sentinel
  const output = formatEllaJson(decodeReadings(sensors, data.element));
  assert.equal(output.temperature.Outside, -0.1);
  assert.ok(Object.hasOwn(output.battery, 'Ella  '));
  assert.equal(output.temperature.Duplicate, 19);
  assert.equal(output.voltage['[hidden]'], undefined);
});

test('missing readings and confirmed invalid voltage/SOC are not displayed as zeros or 65.535 V', () => {
  const data = fixture(), sensors = createSensorList(data.config);
  delete data.element[sensors[10].pos];
  data.element[sensors[15].pos][0] = 65535;
  data.element[sensors[15].pos + 2][1] = 65535;
  data.element[sensors[21].pos][1] = 65535;
  const output = formatEllaJson(decodeReadings(sensors, data.element));
  assert.equal(output.temperature.Inside, undefined);
  assert.equal(output.voltage.Standalone, undefined);
  assert.equal(output.voltage['Ella  '], undefined);
  assert.equal(output.battery['Ella  '].voltage, null);
  assert.equal(output.battery['Ella  '].state_of_charge, null);
});

test('parser accepts type-3 config fields and rejects truncated/unknown fields without hanging', () => {
  const typed = Buffer.from([5, 3, 0, 0, 0, 0, 0, 0, 1, 0, 2, 255]);
  assert.deepEqual(parseResponse(frame(typed)), { 5: [1, 2] });
  typed.set([127, 255, 255, 255], 7);
  assert.deepEqual(parseResponse(frame(typed)), { 5: '' });
  assert.throws(() => parseResponse(frame(Buffer.from([0, 99, 255]))), /Unknown/);
  assert.throws(() => parseResponse(frame(Buffer.from([0, 1, 0]))), /Truncated/);
  assert.throws(() => parseResponse(frame(Buffer.from([0, 4, 0, 0, 0, 0, 0, 65, 65]))), /Unterminated/);
});

test('literal sensor names cannot change object prototypes', () => {
  const output = formatEllaJson({ 1: { name: '__proto__', pos: 0, type: 'volt', voltage: 12 } });
  assert.ok(Object.hasOwn(output.voltage, '__proto__'));
  assert.equal(output.voltage.__proto__, 12);
  assert.equal(Object.getPrototypeOf(output.voltage), Object.prototype);
});

test('TCP reads collect fragmented frames, reject truncation, and release listeners', async () => {
  const socket = new EventEmitter(); socket.destroyed = false;
  socket.write = () => {};
  const packet = frame(fields({ 0: [0, 123] }));
  const pending = sendReceive(socket, '00', { responseTimeoutMs: 200 });
  socket.emit('data', packet.subarray(0, 10));
  socket.emit('data', packet.subarray(10, 18));
  socket.emit('data', packet.subarray(18));
  assert.deepEqual(await pending, packet);
  assert.equal(socket.listenerCount('data'), 0);
  const incomplete = sendReceive(socket, '00', { responseTimeoutMs: 200 });
  socket.emit('data', packet.subarray(0, 18)); socket.emit('end');
  await assert.rejects(incomplete, /complete response/);
  assert.equal(socket.listenerCount('close'), 0);
  await assert.rejects(sendReceive(socket, '00', { responseTimeoutMs: 10 }), /timeout/);
});

test('discovery ignores unrelated datagrams and supports cancellation', async () => {
  const socket = new EventEmitter(), controller = new AbortController();
  const pending = waitForBroadcast(socket, { signal: controller.signal, discoveryTimeoutMs: 300 });
  socket.emit('message', Buffer.from('unrelated'), { address: '192.0.2.1' });
  socket.emit('message', fixture().packet, { address: '192.0.2.2' });
  assert.equal(await pending, '192.0.2.2');
  const cancelled = waitForBroadcast(socket, { signal: controller.signal }); controller.abort();
  await assert.rejects(cancelled, { name: 'AbortError' });
  assert.equal(socket.listenerCount('message'), 0);
});

test('live receiver rejects other senders and does not refresh freshness for malformed packets', async () => {
  const client = new PicoClient({ staleAfterMs: 50, updateIntervalMs: 0 });
  const socket = new EventEmitter(), controller = new AbortController(), data = fixture();
  let count = 0; client.on('readings', () => count++);
  const pending = client.live(socket, '192.0.2.3', createSensorList(data.config), controller.signal);
  socket.emit('message', data.packet, { address: '192.0.2.4' });
  socket.emit('message', frame(Buffer.from([0, 99, 255])), { address: '192.0.2.3' });
  await assert.rejects(pending, /timed out/);
  assert.equal(count, 0);
  assert.equal(socket.listenerCount('message'), 0);
});

async function simulator(t, { failFirst = false } = {}) {
  const data = fixture(), sockets = new Set(); let sessions = 0, sending = true;
  const server = net.createServer(socket => {
    sessions++; sockets.add(socket); socket.on('close', () => sockets.delete(socket));
    socket.on('error', () => {});
    let buffer = Buffer.alloc(0);
    socket.on('data', chunk => {
      buffer = Buffer.concat([buffer, chunk]);
      while (buffer.length >= 14) {
        const size = frameLength(buffer); if (buffer.length < size) break;
        const request = buffer.subarray(0, size); buffer = buffer.subarray(size);
        const reply = request[6] === 2 ? frame(fields({ 0: [0, Object.keys(data.config).length - 1] }), 3) :
          frame(fields(data.config[request[19]]), 0x42);
        if (failFirst && sessions === 1) { socket.end(reply.subarray(0, 18)); return; }
        socket.write(reply.subarray(0, 9));
        setTimeout(() => { if (!socket.destroyed) socket.write(reply.subarray(9)); }, 2);
      }
    });
  });
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  const port = server.address().port, udp = dgram.createSocket('udp4');
  const timer = setInterval(() => { if (sending) udp.send(data.packet, port, '127.0.0.1'); }, 10);
  t.after(async () => {
    clearInterval(timer); for (const socket of sockets) socket.destroy();
    try { udp.close(); } catch {}
    await new Promise(resolve => server.close(resolve));
  });
  return { data, port, get sessions() { return sessions; }, pause() { sending = false; }, resume() { sending = true; } };
}

for (const fixedIp of [false, true]) test(`loopback Pico simulator: ${fixedIp ? 'fixed IP' : 'discovery'}, config retries, stale recovery and shutdown`, async t => {
  const sim = await simulator(t, { failFirst: true });
  const client = new PicoClient({ ...(fixedIp ? { picoIp: '127.0.0.1' } : {}),
    port: sim.port, udpPort: sim.port, bindAddress: '127.0.0.1', configRetryMs: 10,
    maxRetries: 1, responseTimeoutMs: 300, discoveryTimeoutMs: 500, staleAfterMs: 80, updateIntervalMs: 0 });
  t.after(() => client.stop());
  const reading = awaitEvent(client, 'readings'); client.start();
  const result = await reading;
  assert.equal(result.inclinometer.pitch, 2.3); assert.equal(result.inclinometer.roll, -3.6);
  assert.equal(result.battery['Ella  '].voltage, 13.24);
  assert.ok(sim.sessions >= 2);
  const stale = awaitEvent(client, 'status', s => s.state === 'stale'); sim.pause(); await stale;
  const recovered = awaitEvent(client, 'readings'); sim.resume(); await recovered;
  assert.ok(sim.sessions >= 3);
  await client.stop(); assert.equal(client.sensorList, null);
  // The same instance can start again, and its previous socket was released.
  const again = awaitEvent(client, 'readings'); client.start(); await again; await client.stop();
});

test('stop cancels TCP retry waits promptly', async () => {
  const controller = new AbortController();
  // Reserve and close a local TCP port to induce a connection refusal.
  const server = net.createServer(); await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  const port = server.address().port; await new Promise(resolve => server.close(resolve));
  const pending = openTcp('127.0.0.1', { port, retryDelayMs: 60000, signal: controller.signal });
  const timer = setTimeout(() => controller.abort(), 20);
  await assert.rejects(pending, { name: 'AbortError' }); clearTimeout(timer);
});

test('discovery socket closes on abort during initial bind', async () => {
  const controller = new AbortController();
  const pending = createDiscoverySocket({ udpPort: 0, bindAddress: '127.0.0.1', signal: controller.signal });
  controller.abort(); await assert.rejects(pending, { name: 'AbortError' });
});

for (const mqttMode of ['disabled', 'online', 'denied']) test(`real CLI publishes live JSON and records a capture: MQTT ${mqttMode}`, async t => {
  const sim = await simulator(t);
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'ella-cli-'));
  t.after(() => fs.rmSync(directory, { recursive: true, force: true }));
  const capture = path.join(directory, 'capture.jsonl');
  const mqttArgs = mqttMode === 'disabled' ? ['--no-mqtt'] : [], messages = [];
  if (mqttMode !== 'disabled') {
    const broker = await mqttBroker(t), config = path.join(directory, 'mqtt');
    if (mqttMode === 'denied') broker.deny();
    fs.writeFileSync(config, `server=127.0.0.1\nport=${broker.port}\nprefix=/Ella/Pico/\nusername=test-user\npassword=test-pass=extra\n`);
    broker.events.on('publish', packet => messages.push(packet));
    mqttArgs.push('--mqtt-config', config);
  }
  const child = spawn(process.execPath, [path.join(__dirname, '../bin/pico.js'),
    '--ip', '127.0.0.1', '--udp-port', String(sim.port), '--tcp-port', String(sim.port),
    '--duration', '3', '--record', capture, '--stdout', ...mqttArgs]);
  t.after(() => { if (child.exitCode === null) child.kill(); });
  let stdout = '', stderr = '';
  child.stdout.on('data', b => { stdout += b; }); child.stderr.on('data', b => { stderr += b; });
  const code = await new Promise((resolve, reject) => { child.on('error', reject); child.on('close', resolve); });
  assert.equal(code, 0, stderr);
  assert.ok(stdout.trim(), 'No Pico output before test duration expired: ' + stderr);
  const lines = stdout.trim().split('\n').map(line => JSON.parse(line));
  assert.ok(lines.length > 0); assert.equal(lines[0].inclinometer.pitch, 2.3);
  assert.equal(lines[0].battery['Ella  '].voltage, 13.24);
  if (mqttMode === 'online') {
    assert.ok(messages.length > 0);
    for (const packet of messages) {
      assert.equal(packet.topic, '/Ella/Pico/');
      assert.equal(packet.qos, 0); assert.equal(packet.retain, false);
      const received = JSON.parse(packet.payload.toString());
      assert.ok(lines.some(line => JSON.stringify(line) === JSON.stringify(received)));
      const now = received.time;
      const oracle = spawnSync('python3', [path.join(__dirname, 'python-oracle.py'), baseline], {
        input: JSON.stringify({ ...sim.data, time: { ...now, year: now.year + 2000 } }), encoding: 'utf8',
      });
      assert.equal(oracle.status, 0, oracle.stderr);
      assert.deepEqual(received, JSON.parse(oracle.stdout).output);
    }
    assert.equal(stderr.includes('test-pass'), false);
  }
  if (mqttMode === 'denied') {
    assert.equal(messages.length, 0);
    assert.ok(stderr.includes('connection-error'));
    assert.equal(stderr.includes('test-pass'), false);
  }
  const summary = await verifyCapture(capture, { comparePython: true });
  assert.equal(summary.ok, true, JSON.stringify(summary));
  assert.ok(summary.tcpResponses > 1); assert.ok(summary.pythonMatches > 0);
});

test('capture verifier does not certify bad CRCs or empty captures', async t => {
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'ella-capture-'));
  t.after(() => fs.rmSync(directory, { recursive: true, force: true }));
  const capture = path.join(directory, 'capture.jsonl');
  fs.writeFileSync(capture, ''); assert.equal((await verifyCapture(capture)).ok, false);
  const data = fixture(), packet = Buffer.from(data.packet); packet[packet.length - 1] ^= 1;
  fs.writeFileSync(capture, [
    { kind: 'config', config: data.config },
    { kind: 'tcp', direction: 'complete', hex: frame(fields(data.config[0]), 0x42).toString('hex') },
    { kind: 'packet', receivedAt: '2026-10-07T12:34:56Z', hex: packet.toString('hex') },
  ].map(r => JSON.stringify(r)).join('\n'));
  const summary = await verifyCapture(capture, { comparePython: true });
  assert.equal(summary.udpCrcMatches, 0); assert.equal(summary.pythonMatches, 1);
  assert.equal(summary.ok, false);
});

test('flat installation verifies captures without the original repository folders', t => {
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'ella-flat-'));
  t.after(() => fs.rmSync(directory, { recursive: true, force: true }));
  const installed = path.join(directory, 'PicoData/node.js');
  fs.cpSync(path.join(__dirname, '..'), installed, { recursive: true,
    filter: source => path.basename(source) !== 'node_modules' });
  assert.equal(fs.existsSync(path.join(directory, 'PicoData/python')), false);
  assert.equal(fs.existsSync(path.join(directory, 'PicoData/android')), false);
  const data = fixture(), capture = path.join(installed, 'capture.jsonl');
  fs.writeFileSync(capture, [
    { kind: 'config', config: data.config },
    { kind: 'tcp', direction: 'complete', hex: frame(fields(data.config[0]), 0x42).toString('hex') },
    { kind: 'packet', receivedAt: '2026-10-07T12:34:56Z', hex: data.packet.toString('hex') },
  ].map(record => JSON.stringify(record)).join('\n'));
  const result = spawnSync(process.execPath, ['bin/verify-capture.js', 'capture.jsonl', '--compare-python'], {
    cwd: installed, encoding: 'utf8',
  });
  assert.equal(result.status, 0, result.stderr);
  const summary = JSON.parse(result.stdout);
  assert.equal(summary.ok, true);
  assert.equal(summary.pythonMatches, 1);
});

test('deployed CLI finds parent mqtt config and publishes silently from another working directory', async t => {
  const sim = await simulator(t), broker = await mqttBroker(t);
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'ella-service-'));
  t.after(() => fs.rmSync(directory, { recursive: true, force: true }));
  const root = path.join(directory, 'PicoData'), installed = path.join(root, 'node.js');
  fs.cpSync(path.join(__dirname, '..'), installed, { recursive: true,
    filter: source => path.basename(source) !== 'node_modules' });
  fs.mkdirSync(path.join(root, 'python'));
  fs.writeFileSync(path.join(root, 'mqtt'), `server=127.0.0.1\nport=${broker.port}\nprefix=/Ella/Pico/\nusername=test-user\npassword=test-pass=extra\n`);
  const messages = []; broker.events.on('publish', packet => messages.push(packet));
  const child = spawn(process.execPath, [path.join(installed, 'bin/pico.js'),
    '--ip', '127.0.0.1', '--udp-port', String(sim.port), '--tcp-port', String(sim.port), '--duration', '1.5'], {
    cwd: directory, env: { ...process.env, NODE_PATH: path.join(__dirname, '../node_modules') },
  });
  t.after(() => { if (child.exitCode === null) child.kill(); });
  let stdout = '', stderr = '';
  child.stdout.on('data', b => { stdout += b; }); child.stderr.on('data', b => { stderr += b; });
  const code = await new Promise((resolve, reject) => { child.on('error', reject); child.on('close', resolve); });
  assert.equal(code, 0, stderr); assert.equal(stdout, '');
  assert.ok(stderr.includes('connected')); assert.ok(stderr.includes('stopped'));
  assert.ok(messages.length > 0);
  for (const packet of messages) {
    assert.equal(packet.topic, '/Ella/Pico/'); assert.equal(packet.qos, 0); assert.equal(packet.retain, false);
    const received = JSON.parse(packet.payload.toString()), now = received.time;
    const oracle = spawnSync('python3', [path.join(__dirname, 'python-oracle.py'), baseline], {
      input: JSON.stringify({ ...sim.data, time: { ...now, year: now.year + 2000 } }), encoding: 'utf8',
    });
    assert.equal(oracle.status, 0, oracle.stderr);
    assert.deepEqual(received, JSON.parse(oracle.stdout).output);
  }
  assert.equal(stderr.includes('test-pass'), false);
});

test('missing default config fails visibly without readings or credentials', t => {
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'ella-no-config-'));
  t.after(() => fs.rmSync(directory, { recursive: true, force: true }));
  const installed = path.join(directory, 'PicoData/node.js');
  fs.cpSync(path.join(__dirname, '..'), installed, { recursive: true,
    filter: source => path.basename(source) !== 'node_modules' });
  const result = spawnSync(process.execPath, [path.join(installed, 'bin/pico.js')], { cwd: directory, encoding: 'utf8' });
  assert.equal(result.status, 1); assert.equal(result.stdout, '');
  assert.ok(result.stderr.includes('Check PicoData/mqtt'));
  const conflict = spawnSync(process.execPath, [path.join(installed, 'bin/pico.js'), '--no-mqtt', '--mqtt-config', 'mqtt'], {
    cwd: directory, encoding: 'utf8',
  });
  assert.equal(conflict.status, 2); assert.ok(conflict.stderr.includes('Usage:'));
});

for (const logging of [false, true]) test(`simultaneous SBMS/Pico preserves MQTT and capture: logging ${logging}`, async t => {
  const sim = await simulator(t), broker = await mqttBroker(t);
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'ella-combined-'));
  t.after(() => fs.rmSync(directory, { recursive: true, force: true }));
  const config = path.join(directory, 'mqtt'), capture = path.join(directory, 'capture.jsonl');
  fs.writeFileSync(config, `server=127.0.0.1\nport=${broker.port}\nprefix=/Ella/Pico/\nusername=test-user\npassword=test-pass=extra\nsbms_cells=1,2,3,4\n`);
  const packets = []; broker.events.on('publish', packet => packets.push(packet));
  const loggingFile = path.join(directory, 'logging.json');
  if (logging) fs.writeFileSync(loggingFile, JSON.stringify({ database: 'history.sqlite', commitSeconds: 1, batteryGroup: {picoBattery:'battery',sbmsBattery:'sbms-battery',secondaryVoltage:'secondary',pv1:'pv1',pv2:'pv2'}, metrics: [
    { id: 'battery', source: 'pico', sensorId: 15, sensorType: 'battery', kind: 'electrical', role: 'battery', polarity: 1, voltage: 'sbms' },
    { id: 'secondary', source: 'pico', sensorId: 20, sensorType: 'volt', kind: 'voltage' },
    { id: 'sbms-battery', source: 'sbms', field: 'battery', kind: 'electrical', role: 'battery', polarity: 1, voltage: 'self' },
    { id: 'pv2', source: 'sbms', field: 'pv2', kind: 'electrical', role: 'supply', polarity: null, voltage: 'self' },
    { id: 'pv1', source: 'sbms', field: 'pv1', kind: 'electrical', role: 'supply', polarity: null, voltage: 'self' },
  ] }));
  const child = spawn(process.execPath, [path.join(__dirname, '../bin/pico.js'), '--mqtt-config', config,
    '--ip', '127.0.0.1', '--udp-port', String(sim.port), '--tcp-port', String(sim.port),
    '--duration', '1.8', '--record', capture, ...(logging ? ['--logging-config', loggingFile] : ['--stdout', '--sbms-stdout'])]);
  t.after(() => { if (child.exitCode === null) child.kill(); });
  let stdout = '', stderr = '', second = 1;
  child.stdout.on('data', data => { stdout += data; }); child.stderr.on('data', data => { stderr += data; });
  const timer = setInterval(() => broker.send('/Ella/sbms', {
    time: { year: 1, month: 2, day: 3, hour: 4, minute: 5, second: second++ % 60 }, soc: 62,
    cellsMV: [3200, 3300, 3400, 3500, 0, 0, 0, 0], currentMA: { battery: -3000, pv1: 1000, pv2: 0, extLoad: 4000 },
  }), 50);
  t.after(() => clearInterval(timer));
  const code = await new Promise((resolve, reject) => { child.on('error', reject); child.on('close', resolve); });
  clearInterval(timer); assert.equal(code, 0, stderr);
  const lines = logging ? [] : stdout.trim().split('\n').map(line => JSON.parse(line));
  const pico = logging ? packets.map(p => JSON.parse(p.payload.toString())) : lines.filter(line => line.source !== 'sbms');
  const sbms = logging ? fs.readFileSync(capture, 'utf8').trim().split('\n').map(JSON.parse).filter(r => r.kind === 'sbms').map(r => r.reading) : lines.filter(line => line.source === 'sbms');
  if (logging) {
    assert.equal(stdout, ''); assert.ok(stderr.includes('\"source\":\"logging\",\"state\":\"started\"'));
    assert.equal(stderr.includes('\"state\":\"failed\"'), false);
    const { readLoggingConfig } = require('../lib/history-config'), { inspect } = require('../bin/history');
    const history = inspect(readLoggingConfig(loggingFile));
    assert.equal(history.integrity, 'ok'); assert.ok(history.savedAt);
    const battery = history.battery.rows[0];
    assert.equal(history.schemaVersion,2);
    assert.equal(history.metrics.length,0); // The configured channels share one record.
    assert.ok(battery.samples.picoBattery > packets.length); assert.ok(battery.coverageSeconds.picoWatts > 0);
    assert.equal(battery.values.picoSoc,83); assert.equal(battery.values.sbmsSoc,62);
    assert.ok(Math.abs(battery.values.secondaryVoltage - 9.999) < 1e-9);
    assert.equal(battery.values.pv2Current,0); assert.equal(battery.values.pv2Watts,0);
    assert.ok(Math.abs(battery.values.pv1Watts - 13.4) < 1e-9);
    assert.equal(history.battery.sources.picoCurrent.sensorId,15);
    assert.equal(history.battery.sources.sbmsCurrent.source,'sbms');
    const {sqlite}=require('../lib/history-store'), db=new (sqlite())(path.join(directory,'history.sqlite'),{readOnly:true});
    try { assert.equal(db.prepare('SELECT COUNT(*) AS n FROM history').get().n,0); }
    finally {db.close();}
  }
  assert.ok(pico.length > 0); assert.ok(sbms.length > 0); assert.ok(packets.length > 0);
  assert.equal(sbms[0].voltage, 13.4); assert.equal(sbms[0].current.pv2, 0);
  for (const packet of packets) {
    assert.equal(packet.topic, '/Ella/Pico/'); assert.equal(packet.qos, 0); assert.equal(packet.retain, false);
    const output = JSON.parse(packet.payload.toString());
    assert.ok(pico.some(value => JSON.stringify(value) === JSON.stringify(output)));
    const now = output.time;
    const oracle = spawnSync('python3', [path.join(__dirname, 'python-oracle.py'), baseline], {
      input: JSON.stringify({ ...sim.data, time: { ...now, year: now.year + 2000 } }), encoding: 'utf8',
    });
    assert.equal(oracle.status, 0, oracle.stderr); assert.deepEqual(output, JSON.parse(oracle.stdout).output);
  }
  assert.equal((await verifyCapture(capture, { comparePython: true })).ok, true);
  assert.equal(stderr.includes('test-pass'), false);
});
