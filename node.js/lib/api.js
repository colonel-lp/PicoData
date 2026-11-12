'use strict';
const http = require('node:http');
const https = require('node:https');
const net = require('node:net');
const { createHash, timingSafeEqual } = require('node:crypto');
const { performance } = require('node:perf_hooks');
const { HistoryReader, ApiError } = require('./history-reader');
const { PiStatus } = require('./pi-status');
function isLoopback(address) {
  return address === '::1' || /^(?:::ffff:)?127\./.test(address || '');
}
function lanAddress(address) {
  if (typeof address !== 'string') return null;
  const value = address.replace(/^::ffff:/i, '');
  if (net.isIP(value) === 4) {
    const [a, b] = value.split('.').map(Number);
    return a === 10 || a === 127 || (a === 172 && b >= 16 && b <= 31) ||
      (a === 192 && b === 168) || (a === 169 && b === 254) ? address : null;
  }
  return net.isIP(value) === 6 && (value === '::1' || /^(?:f[cd]|fe[89ab])/i.test(value)) ? address : null;
}
const finite = value => typeof value === 'number' && Number.isFinite(value) ? value : null;
class ReadingApi {
  constructor({ config, logger, now = Date.now, monotonic = ()=>performance.now(), piStatus }) {
    this.config = config; this.logger = logger; this.now = now; this.monotonic = monotonic;
    this.reader = new HistoryReader(logger.config.database);
    this.catalogue = this.reader.view(() => this.reader.catalogue());
    this.requests = [];
    this.requestSequence = 0;
    this.tokenHash = createHash('sha256').update(config.token).digest();
    this.startedAt = new Date(now()).toISOString();
    this.sources = Object.fromEntries(['pico','sbms'].map(source=>[source,{state:'waiting-data',latest:null,receivedAt:null}]));
    this.liveTimeoutMs = {pico:15000,sbms:30000};
    this.loggingState = 'started'; this.budget = 60; this.budgetAt = monotonic();
    this.pi=piStatus??new PiStatus({...config.pi,now,monotonic});
  }
  configurePico(sensorList) {
    this.sensorList = sensorList;
    this.sources.pico.latest = null;
    // Refresh names when configuration changes, while retaining the last usable catalogue on storage failure.
    try { this.catalogue = this.reader.view(() => this.reader.catalogue()); } catch {}
  }
  health() {
    return {
      collectorVersion: require('../package.json').version,
      startedAt: this.startedAt,
      now: new Date(this.now()).toISOString(),
      sources: Object.fromEntries(['pico', 'sbms'].map(source => {
        const { state, fresh, receivedAt, ageSeconds } = this.source(source);
        return [source, { state, fresh, receivedAt, ageSeconds }];
      })),
      logging: { state: this.logger.failed ? 'failed' : this.logger.state,
        savedAt: this.logger.store.savedAt, clockSteps: this.logger.store.clockSteps,
        failure: this.logger.failure ?? null },
      schemaVersion: 2,
    };
  }
  diagnostics() { return { requests: this.requests.slice(-32) }; }
  recordRequest(req, res) {
    const started = this.monotonic();
    const supplied = req.headers['x-request-id'];
    const id = typeof supplied === 'string' && /^[a-zA-Z0-9_-]{1,64}$/.test(supplied)
      ? supplied : String(++this.requestSequence);
    res.setHeader('X-Request-ID', id);
    const path = req.url.split('?')[0];
    const route = ['/api/v1/live', '/api/v1/status', '/api/v1/pi', '/api/v1/pi/wifi', '/api/v1/metrics'].includes(path)
      ? path : path.startsWith('/api/v1/history/') ? 'history' : 'other';
    const localAddress = req.socket.localAddress;
    const peerAddress = req.socket.remoteAddress;
    const localPort = req.socket.localPort ?? null;
    let recorded = false;
    const finish = () => {
      if (recorded) return;
      recorded = true;
      this.requests.push({ id, utc: new Date(this.now()).toISOString(), route,
        localAddress: lanAddress(localAddress), localPort,
        peerAddress: lanAddress(peerAddress), status: res.writableFinished ? res.statusCode : 0,
        elapsedMs: Math.max(0, Math.round(this.monotonic() - started)), aborted: !res.writableFinished });
      if (this.requests.length > 128) this.requests.shift();
    };
    res.once('finish', finish);
    res.once('close', finish);
  }
  status(source, state) {
    if (source === 'logging') { this.loggingState = state; return; }
    if (!this.sources[source]) return;
    this.sources[source].state = state;
    if (state !== 'connected') this.sources[source].latest = null;
  }
  accept(source, reading) {
    if (!this.sources[source]) return;
    this.sources[source].latest = reading; this.sources[source].receivedAt = reading.receivedAt;
  }
  setLiveTimeout(source, milliseconds) {
    if (!Object.hasOwn(this.liveTimeoutMs,source) || !Number.isFinite(milliseconds) || milliseconds<=0) throw Error('Invalid live timeout');
    this.liveTimeoutMs[source] = milliseconds;
  }
  source(source) {
    const s = this.sources[source], age = s.latest ? (this.monotonic()-s.latest.receivedMonotonicMs)/1000 : null;
    const fresh = s.state==='connected' && age !== null && age>=0 && age<=this.liveTimeoutMs[source]/1000;
    return {state:s.state==='connected' && !fresh ? 'stale' : s.state,fresh,receivedAt:s.receivedAt,ageSeconds:age===null ? null : Math.max(0,age),
      ...(source==='pico' ? {readings:fresh ? s.latest.readings : null} : {reading:fresh ? {
        voltage:s.latest.voltage,voltageStatus:s.latest.voltageStatus,stateOfCharge:s.latest.stateOfCharge,current:s.latest.current,sourceTime:s.latest.sourceTime,
        flags:s.latest.flags ?? null,broadcast:s.latest.broadcast ?? null
      } : null})};
  }
  live() {
    const pico = this.source('pico'), sbms = this.source('sbms');
    const catalogue = this.catalogue;
    const measurements = catalogue.metrics.map(m=> {
      const source = m.source==='pico' ? pico : sbms;
      const validMapping = m.source!=='pico' || this.logger.active.has(m.id);
      const reading = validMapping ? (m.source==='pico' ? pico.readings?.[m.sensorId] : sbms.reading) : null;
      let values, voltageReceivedAt = null;
      if (m.kind==='electrical') {
        const current = finite(m.source==='pico' ? reading?.current : reading?.current?.[m.field]);
        const reference = m.voltage==='sbms' ? sbms.reading : m.voltage==='self' ? reading : null;
        const voltage = reference && (m.voltage!=='sbms' && m.source!=='sbms' || reference.voltageStatus==='valid') && finite(reference.voltage)>0 ? reference.voltage : null;
        if (voltage !== null) voltageReceivedAt = m.voltage==='sbms' ? sbms.receivedAt : source.receivedAt;
        values = {current,voltage,watts:current===null || voltage===null ? null : current*voltage};
        if (m.role==='battery') values.stateOfCharge = finite(reading?.stateOfCharge);
      } else {
        const key = {voltage:'voltage',temperature:'temperature',barometer:'pressure'}[m.kind];
        const value = finite(reading?.[key]);
        values = {[key]:m.kind==='voltage' && !(value>0) ? null : value};
      }
      return {id:m.id,fresh:source.fresh && !!reading,mappingValid:validMapping,receivedAt:source.receivedAt,voltageReceivedAt,values};
    });
    return {now:new Date(this.now()).toISOString(),pico,sbms,measurements};
  }

  authorize(req) {
    const value = req.headers.authorization;
    return typeof value==='string' && /^Bearer [a-f0-9]{64}$/.test(value) &&
      timingSafeEqual(this.tokenHash,createHash('sha256').update(value.slice(7)).digest());
  }
  send(res,status,value) {
    const body = JSON.stringify({apiVersion:1,...value});
    if (Buffer.byteLength(body)>4*1024*1024) throw new ApiError(413,'response_too_large');
    res.writeHead(status,{'Content-Type':'application/json; charset=utf-8','Cache-Control':'no-store','X-Content-Type-Options':'nosniff'});
    res.end(body);
  }
  async handle(req,res) {
    // Only local processes may read this limited health view without an API credential.
    // It deliberately bypasses the public request budget and does not record itself.
    if (req.method === 'GET' && req.url === '/api/v1/health' && isLoopback(req.socket.remoteAddress)) {
      return this.send(res, 200, { health: this.health(), ...this.diagnostics() });
    }
    this.recordRequest(req, res);
    try {
      const at = this.monotonic(); this.budget = Math.min(60,this.budget+Math.max(0,at-this.budgetAt)/100); this.budgetAt = at;
      if (this.budget<1) { res.setHeader('Retry-After','1'); throw new ApiError(429,'rate_limited'); }
      this.budget--;
      if (!this.authorize(req)) { res.setHeader('WWW-Authenticate','Bearer'); throw new ApiError(401,'unauthorized'); }
      if(req.method==='POST' && req.url==='/api/v1/pi/wifi') {
        if(!this.config.pi?.allowWifiControl)throw new ApiError(403,'pi_control_disabled');
        if(req.headers['transfer-encoding'] || !/^application\/json(?:\s*;.*)?$/i.test(req.headers['content-type']||'') ||
            !/^\d+$/.test(req.headers['content-length']||'') || Number(req.headers['content-length'])>64)throw new ApiError(400,'invalid_body');
        const text=await new Promise((resolve,reject)=> {
          let value='';const timer=setTimeout(()=>{cleanup();req.resume();reject(new ApiError(408,'body_timeout'));},2000);
          const cleanup=()=>{clearTimeout(timer);req.removeListener('data',data);req.removeListener('end',end);req.removeListener('error',error);req.removeListener('aborted',error);};
          const data=b=>{value+=b.toString('utf8');if(Buffer.byteLength(value)>64){cleanup();req.resume();reject(new ApiError(400,'invalid_body'));}};
          const end=()=>{cleanup();resolve(value);};const error=()=>{cleanup();reject(new ApiError(400,'invalid_body'));};
          req.on('data',data);req.once('end',end);req.once('error',error);req.once('aborted',error);
        });
        const match=text.match(/^\s*\{\s*"mode"\s*:\s*"(auto|home|m1)"\s*\}\s*$/);
        if(!match)throw new ApiError(400,'invalid_wifi_mode');
        try{return this.send(res,202,await this.pi.requestMode(match[1]));}
        catch(e){throw new ApiError(e.status??503,e.code??'pi_control_unavailable');}
      }
      if (req.method!=='GET') { res.setHeader('Allow','GET'); throw new ApiError(405,'read_only'); }
      if (req.headers['transfer-encoding'] || Number(req.headers['content-length'])>0) { res.setHeader('Connection','close'); req.resume(); throw new ApiError(400,'unexpected_body'); }
      if (!req.url.startsWith('/') || req.url.length>2048) throw new ApiError(400,'invalid_url');
      const url = new URL(req.url,'http://local'), now = this.now(); let result;
      if (url.pathname==='/api/v1/history/battery') result = this.reader.history(null,url.searchParams,now);
      else if (/^\/api\/v1\/history\/metrics\/[a-zA-Z0-9_-]{1,80}$/.test(url.pathname)) result = this.reader.history(url.pathname.split('/').at(-1),url.searchParams,now);
      else {
        if (url.search) throw new ApiError(400,'invalid_query');
        if(url.pathname==='/api/v1/pi')result={pi:await this.pi.status()};
        else if (url.pathname==='/api/v1/live') result = this.live();
        else if (url.pathname==='/api/v1/metrics') result = this.reader.view(()=>({ ...this.reader.catalogue(),picoSensors:Object.entries(this.sensorList || {}).map(([sensorId,s])=>({sensorId:Number(sensorId),name:s.name,type:s.type})) }));
        else if (url.pathname==='/api/v1/status') result = this.health();
        else if (url.pathname==='/api/v1/diagnostics') result = this.diagnostics();
        else throw new ApiError(404,'not_found');
      }
      this.send(res,200,result);
    } catch (err) { this.send(res,err instanceof ApiError ? err.status : 503,{error:err instanceof ApiError ? err.code : 'history_unavailable'}); }
  }
  async start() {
    const limits = {maxHeaderSize:8192,connectionsCheckingInterval:1000};
    this.server = this.config.tls ? https.createServer({...limits,...this.config.tls},(req,res)=>this.handle(req,res)) : http.createServer(limits,(req,res)=>this.handle(req,res));
    this.server.maxConnections = 16; this.server.maxRequestsPerSocket = 100;
    this.server.headersTimeout = 5000; this.server.requestTimeout = 5000; this.server.keepAliveTimeout = 2000;
    this.server.setTimeout(5000,socket=>socket.destroy());
    await new Promise((resolve,reject)=> {
      const fail = err=> {this.server.removeListener('listening',ready);reject(err);};
      const ready = ()=> {this.server.removeListener('error',fail);resolve();};
      this.server.once('error',fail);this.server.once('listening',ready);this.server.listen(this.config.port,this.config.host);
    });
    this.server.on('error',()=>console.error('API listener failed. Check the private configuration and restart the collector.'));
  }
  async close() {
    this.pi.close();
    if (this.server?.listening) await new Promise(resolve=> {this.server.close(resolve);this.server.closeAllConnections();});
    this.reader.close();
  }
}
module.exports = { ReadingApi };
