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
const baseline = path.resolve(__dirname, '../../../python/pico-mqtt.py');
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

test('real CLI path writes live JSON and a verifiable capture without MQTT or SignalK', async t => {
  const sim = await simulator(t);
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'ella-cli-'));
  t.after(() => fs.rmSync(directory, { recursive: true, force: true }));
  const capture = path.join(directory, 'capture.jsonl');
  const child = spawn(process.execPath, [path.join(__dirname, '../bin/pico.js'),
    '--ip', '127.0.0.1', '--udp-port', String(sim.port), '--tcp-port', String(sim.port),
    '--duration', '1.5', '--record', capture]);
  t.after(() => { if (child.exitCode === null) child.kill(); });
  let stdout = '', stderr = '';
  child.stdout.on('data', b => { stdout += b; }); child.stderr.on('data', b => { stderr += b; });
  const code = await new Promise((resolve, reject) => { child.on('error', reject); child.on('close', resolve); });
  assert.equal(code, 0, stderr);
  const lines = stdout.trim().split('\n').map(line => JSON.parse(line));
  assert.ok(lines.length > 0); assert.equal(lines[0].inclinometer.pitch, 2.3);
  assert.equal(lines[0].battery['Ella  '].voltage, 13.24);
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
