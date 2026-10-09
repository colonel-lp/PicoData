'use strict';
const fs = require('node:fs');
const path = require('node:path');
const net = require('node:net');
function readApiConfig(filename) {
  const input = JSON.parse(fs.readFileSync(filename, 'utf8'));
  if (!input || typeof input !== 'object' || Array.isArray(input)) throw Error('Invalid API configuration');
  const host = input.host ?? '127.0.0.1', port = input.port ?? 8080;
  if (net.isIP(host) !== 4 || !Number.isInteger(port) || port < 1 || port > 65535 ||
      typeof input.token !== 'string' || !/^[a-f0-9]{64}$/.test(input.token)) throw Error('Invalid API host, port or token');
  let tls;
  if (input.tls !== undefined) {
    if (!input.tls || typeof input.tls.cert !== 'string' || typeof input.tls.key !== 'string') throw Error('Invalid API TLS files');
    tls = { cert: fs.readFileSync(path.resolve(path.dirname(filename), input.tls.cert)),
      key: fs.readFileSync(path.resolve(path.dirname(filename), input.tls.key)), minVersion: 'TLSv1.2' };
  }
  return { host, port, token: input.token, tls };
}
module.exports = { readApiConfig };
