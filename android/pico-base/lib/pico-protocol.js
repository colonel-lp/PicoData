'use strict';

// Based on node.js/lib/pico-protocol.js: same requests, CRC and field layouts.
const net = require('node:net');
const { calcRevCrc16 } = require('./crc16');

function abortError() { const e = new Error('Stopped'); e.name = 'AbortError'; return e; }
function delay(ms, signal) {
  return new Promise((resolve, reject) => {
    if (signal?.aborted) return reject(abortError());
    const finish = (err) => {
      clearTimeout(timer); signal?.removeEventListener('abort', onAbort);
      err ? reject(err) : resolve();
    };
    const onAbort = () => finish(abortError());
    const timer = setTimeout(finish, ms);
    signal?.addEventListener('abort', onAbort, { once: true });
  });
}
function hexdump(value) { return value.toString(16).padStart(4, '0').match(/../g).join(' '); }
function addCrc(message) {
  const fields = message.trim().split(/\s+/).map(x => parseInt(x, 16));
  return message + ' ' + hexdump(calcRevCrc16(fields.slice(1, -1)));
}
function toBuffer(message) {
  if (Buffer.isBuffer(message)) return message;
  const hex = message.replace(/\s+/g, '');
  if (!hex || hex.length % 2 || /[^0-9a-f]/i.test(hex)) throw new Error('Invalid hexadecimal packet');
  return Buffer.from(hex, 'hex');
}
function bufferToHex(buffer) { return Array.from(buffer, b => b.toString(16).padStart(2, '0')).join(' '); }

// The upstream requests encode bytes-after-offset-12 as a big-endian length
// at offsets 11..12. Receive framing uses that layout; confirm on real hardware.
function frameLength(buffer) {
  if (buffer.length < 14) return null;
  if (buffer[5] !== 0xff || buffer[13] !== 0xff) throw new Error('Unrecognised Pico header');
  const size = buffer.readUInt16BE(11) + 13;
  if (size < 16) throw new Error('Invalid Pico frame length');
  return size;
}
function isPicoPacket(buffer) {
  return buffer.length >= 16 && buffer[5] === 0xff && buffer[13] === 0xff;
}
function isLivePacket(buffer) { return isPicoPacket(buffer) && (buffer[6] & 0xf0) === 0xb0; }

function parseResponse(message) {
  const b = toBuffer(message);
  if (!isPicoPacket(b)) throw new Error('Invalid Pico packet header');
  const end = b.length - 2; // checksum bytes; incoming CRC is not yet enforced
  const result = {};
  let pos = 14;
  const need = n => { if (pos + n > end) throw new Error('Truncated Pico field'); };
  while (pos < end) {
    need(2);
    const id = b[pos], type = b[pos + 1];
    if (Object.hasOwn(result, id)) throw new Error('Duplicate Pico field ' + id);
    if (type === 1) {
      need(7);
      if (b[pos + 6] !== 0xff) throw new Error('Missing field separator');
      result[id] = [b.readUInt16BE(pos + 2), b.readUInt16BE(pos + 4)];
      pos += 7;
    } else if (type === 3) {
      need(12);
      if (b[pos + 11] !== 0xff) throw new Error('Missing field separator');
      result[id] = b.readUInt32BE(pos + 7) === 0x7fffffff ? '' :
        [b.readUInt16BE(pos + 7), b.readUInt16BE(pos + 9)];
      pos += 12;
    } else if (type === 4) {
      need(9);
      const start = pos + 7;
      const nul = b.indexOf(0, start);
      if (nul < start || nul + 1 >= end || b[nul + 1] !== 0xff) throw new Error('Unterminated Pico string');
      // Latin-1 preserves Python HexToByte behaviour; do not trim sensor names.
      result[id] = b.subarray(start, nul).toString('latin1');
      pos = nul + 2;
    } else throw new Error('Unknown Pico field type ' + type);
  }
  return result;
}

async function openTcp(picoIp, options = {}) {
  const { port = 5001, maxRetries = 5, retryDelayMs = 5000,
    connectTimeoutMs = 10000, signal, debug = () => {} } = options;
  let lastError;
  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    if (signal?.aborted) throw abortError();
    try {
      debug(`TCP connect ${attempt}/${maxRetries} to ${picoIp}:${port}`);
      return await new Promise((resolve, reject) => {
        const socket = new net.Socket();
        socket.setNoDelay(true);
        function cleanup() {
          clearTimeout(timer); signal?.removeEventListener('abort', onAbort);
          socket.removeListener('connect', onConnect); socket.removeListener('error', onError);
        }
        function fail(err) { cleanup(); socket.destroy(); reject(err); }
        const onError = err => fail(err);
        const onAbort = () => fail(abortError());
        const onConnect = () => {
          cleanup();
          // Avoid an unhandled error between serial requests; sendReceive also
          // listens for errors and checks a closed socket before each write.
          socket.on('error', () => {});
          resolve(socket);
        };
        const timer = setTimeout(() => fail(new Error('TCP connect timeout')), connectTimeoutMs);
        socket.once('connect', onConnect); socket.once('error', onError);
        signal?.addEventListener('abort', onAbort, { once: true });
        socket.connect(port, picoIp);
      });
    } catch (err) {
      if (err.name === 'AbortError') throw err;
      lastError = err; debug('TCP connect failed: ' + err.message);
      if (attempt < maxRetries) await delay(retryDelayMs, signal);
    }
  }
  throw lastError || new Error('No TCP connection attempts configured');
}

function sendReceive(socket, message, options = {}) {
  const { responseTimeoutMs = 30000, signal, debug = () => {}, label = 'request', onTcp = () => {} } = options;
  return new Promise((resolve, reject) => {
    if (signal?.aborted) return reject(abortError());
    if (socket.destroyed) return reject(new Error('TCP socket is closed'));
    let response = Buffer.alloc(0), settled = false;
    function cleanup() {
      clearTimeout(timer); signal?.removeEventListener('abort', onAbort);
      socket.removeListener('data', onData); socket.removeListener('error', onError);
      socket.removeListener('close', onClose); socket.removeListener('end', onClose);
    }
    function finish(err) {
      if (settled) return;
      settled = true; cleanup();
      onTcp({ direction: 'complete', label, hex: response.toString('hex'), error: err?.message });
      err ? reject(err) : resolve(response);
    }
    function onData(chunk) {
      try {
        onTcp({ direction: 'receive', label, hex: chunk.toString('hex') });
        response = Buffer.concat([response, chunk]);
        const size = frameLength(response);
        debug(`TCP ${label}: ${response.length} bytes collected`);
        if (response.length > 65548) throw new Error('Oversized TCP response');
        if (size !== null && response.length > size) throw new Error('TCP response exceeds declared length');
        if (size !== null && response.length === size) finish();
      } catch (err) { finish(err); }
    }
    const onError = err => finish(err);
    const onClose = () => finish(new Error('TCP closed before complete response'));
    const onAbort = () => finish(abortError());
    const timer = setTimeout(() => finish(new Error(`TCP ${label} timeout (${response.length} bytes)`)), responseTimeoutMs);
    socket.on('data', onData); socket.once('error', onError);
    socket.once('close', onClose); socket.once('end', onClose);
    signal?.addEventListener('abort', onAbort, { once: true });
    try {
      const request = toBuffer(message);
      onTcp({ direction: 'send', label, hex: request.toString('hex') });
      socket.write(request, err => { if (err) finish(err); });
    }
    catch (err) { finish(err); }
  });
}

async function getPicoConfigTcp(picoIp, options = {}) {
  const { signal, debug = () => {} } = options;
  const socket = await openTcp(picoIp, options);
  try {
    const count = await sendReceive(socket, addCrc('00 00 00 00 00 ff 02 04 8c 55 4b 00 03 ff'),
      { ...options, label: 'config-count' });
    if (count.length < 22) throw new Error('Config-count response too short');
    const entries = count[19] + 1; // Same count extraction as the upstream fork.
    const config = {};
    for (let pos = 0; pos < entries; pos++) {
      if (signal?.aborted) throw abortError();
      const request = addCrc('00 00 00 00 00 ff 41 04 8c 55 4b 00 16 ff 00 01 00 00 00 ' +
        pos.toString(16).padStart(2, '0') + ' ff 01 03 00 00 00 00 ff 00 00 00 00 ff');
      config[pos] = parseResponse(await sendReceive(socket, request, { ...options, label: `config-${pos}` }));
      debug(`Config entry ${pos + 1}/${entries}`);
    }
    return config;
  } finally { socket.destroy(); }
}

module.exports = { abortError, delay, hexdump, addCrc, toBuffer, bufferToHex,
  frameLength, isPicoPacket, isLivePacket, parseResponse, openTcp, sendReceive, getPicoConfigTcp };
