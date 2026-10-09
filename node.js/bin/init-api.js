#!/usr/bin/env node
'use strict';
const fs = require('node:fs');
const path = require('node:path');
const net = require('node:net');
const { randomBytes } = require('node:crypto');
function init(args = process.argv.slice(2)) {
  const options = { host: '127.0.0.1', port: 8080, output: path.resolve(__dirname, '../../api.json') };
  for (let i = 0; i < args.length; i++) {
    if (!['--host','--port','--output'].includes(args[i]) || !args[i+1] || args[i+1].startsWith('--')) throw Error('Use [--host IPv4] [--port PORT] [--output FILE]');
    options[args[i].slice(2)] = args[++i];
  }
  options.port = Number(options.port);
  if (net.isIP(options.host) !== 4 || !Number.isInteger(options.port) || options.port < 1 || options.port > 65535) throw Error('Invalid host or port');
  fs.writeFileSync(options.output, JSON.stringify({ host: options.host, port: options.port, token: randomBytes(32).toString('hex') },null,2)+'\n', { flag:'wx',mode:0o600 });
  return options.output;
}
if (require.main === module) {
  try { console.log('Created private API configuration:', init()); }
  catch { console.error('API configuration creation failed. Check options, parent directory and existing file; existing tokens are never overwritten.'); process.exitCode = 1; }
}
module.exports = { init };
