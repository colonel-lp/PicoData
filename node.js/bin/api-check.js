#!/usr/bin/env node
'use strict';
const path = require('node:path');
const { readApiConfig } = require('../lib/api-config');
async function check(args = process.argv.slice(2)) {
  const options = {config:path.resolve(__dirname,'../../api.json'),path:'/api/v1/status'};
  for (let i=0;i<args.length;i++) {
    if (!['--config','--path'].includes(args[i]) || !args[i+1] || args[i+1].startsWith('--')) throw Error('Use [--config FILE] [--path /api/v1/...]');
    options[args[i].slice(2)] = args[++i];
  }
  if (!options.path.startsWith('/api/v1/')) throw Error('Invalid API path');
  const config = readApiConfig(options.config), host = config.host==='0.0.0.0' ? '127.0.0.1' : config.host;
  const response = await fetch(`${config.tls ? 'https' : 'http'}://${host}:${config.port}${options.path}`,{
    headers:{Authorization:'Bearer '+config.token},signal:AbortSignal.timeout(10000),redirect:'error'});
  if (!response.ok) throw Error('API returned HTTP '+response.status);
  return response.json();
}
if (require.main===module) {
  check().then(result=>console.log(JSON.stringify(result,null,2))).catch(()=> {console.error('API check failed. Check the service, private configuration and TLS trust if enabled.');process.exitCode=1;});
}
module.exports = { check };
