'use strict';
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const https = require('node:https');
const { spawnSync } = require('node:child_process');
const { ReadingApi } = require('../lib/api');
const { HistoryLogger, empty } = require('../lib/logger');
const { validateConfig } = require('../lib/history-config');
const { readApiConfig } = require('../lib/api-config');
const { init } = require('../bin/init-api');
const { check } = require('../bin/api-check');
const { range } = require('../lib/history-reader');
const { decodeSbms } = require('../lib/sbms');
const BASE = Date.parse('2026-01-15T12:00:00Z'), TOKEN = 'a'.repeat(64);
async function setup(t, {tls}={}) {
  const directory = fs.mkdtempSync(path.join(os.tmpdir(),'pico-api-'));
  const config = validateConfig({database:path.join(directory,'history.sqlite'), metrics:[
    {id:'pico-battery',source:'pico',sensorId:101,sensorType:'battery',kind:'electrical',role:'battery',polarity:1,voltage:'sbms'},
    {id:'secondary-voltage',source:'pico',sensorId:103,sensorType:'volt',kind:'voltage'},
    {id:'load',source:'pico',sensorId:102,sensorType:'current',kind:'electrical',role:'load',polarity:-1,voltage:'sbms'},
    {id:'outside',source:'pico',sensorId:104,sensorType:'thermometer',kind:'temperature'},
    {id:'pressure',source:'pico',sensorId:105,sensorType:'barometer',kind:'barometer'},
    ...['battery','pv1','pv2','externalLoad'].map(field=>({id:'sbms-'+field,source:'sbms',field,kind:'electrical',role:field==='battery'?'battery':'supply',polarity:field==='battery'?1:null,voltage:'self'})),
  ]});
  const logger = new HistoryLogger(config,{automatic:false});
  const sensors = {101:{type:'battery',name:'House  '},102:{type:'current',name:'Load'},103:{type:'volt',name:'Module [test]'},104:{type:'thermometer',name:'Outdoor'},105:{type:'barometer',name:'Pressure'}};
  logger.configurePico({sensorList:sensors,config:{}},BASE);
  const clock = {time:BASE+30000,mono:0};
  const api = new ReadingApi({config:{host:'127.0.0.1',port:0,token:TOKEN,tls},logger,now:()=>clock.time,monotonic:()=>clock.mono});
  api.configurePico(sensors); await api.start();
  t.after(async()=> {await api.close();logger.close({advance:false});fs.rmSync(directory,{recursive:true,force:true});});
  const url = 'http://127.0.0.1:'+api.server.address().port;
  const get = async (route,options={})=> {
    const response=await fetch(url+route,{headers:{Authorization:'Bearer '+TOKEN},...options});
    return {status:response.status,headers:response.headers,body:await response.json()};
  };
  return {api,logger,config,directory,clock,get};
}
function stats(amps=-2,volts=12,soc=80,seconds=30) {
  return {...empty(),ampSeconds:amps*seconds,voltSeconds:volts*seconds,wattSeconds:amps*volts*seconds,
    ampCoverageSeconds:seconds,voltCoverageSeconds:seconds,powerCoverageSeconds:seconds,soc,socAt:BASE+25000,samples:1,
    forwardAmpSeconds:Math.max(amps,0)*seconds,reverseAmpSeconds:Math.max(-amps,0)*seconds,
    forwardWattSeconds:Math.max(amps*volts,0)*seconds,reverseWattSeconds:Math.max(-amps*volts,0)*seconds};
}
const rec=(id,data,start=BASE,resolution='minute',size=60000)=>({id,data,start,resolution,end:start+size});

test('private API configuration generates a random token, refuses overwrite and validates bind/TLS settings', t=> {
  const directory=fs.mkdtempSync(path.join(os.tmpdir(),'api-config-'));t.after(()=>fs.rmSync(directory,{recursive:true,force:true}));
  const file=path.join(directory,'api.json');init(['--output',file]);const config=readApiConfig(file);
  assert.equal(config.host,'127.0.0.1');assert.equal(config.port,8080);assert.match(config.token,/^[a-f0-9]{64}$/);
  assert.equal(fs.statSync(file).mode&0o777,0o600);assert.throws(()=>init(['--output',file]),/EEXIST/);
  assert.equal(readApiConfig(file).token,config.token);
  for(const bad of [{host:'example.invalid'},{port:0},{token:'weak'},{tls:{key:'missing',cert:'missing'}}]) {
    fs.writeFileSync(file,JSON.stringify({...config,...bad}));assert.throws(()=>readApiConfig(file));
  }
  const other=path.join(directory,'other.json');init(['--host','0.0.0.0','--port','8081','--output',other]);
  assert.notEqual(readApiConfig(other).token,config.token);
});

test('HTTP requires header authentication on every route and rejects writes without changing SQLite', async t=> {
  const {get,logger,api}=await setup(t);const before=logger.store.db.prepare('SELECT COUNT(*) AS n FROM history').get().n;
  for(const route of ['/api/v1/status','/api/v1/live','/api/v1/metrics','/api/v1/history/battery','/api/v1/history/metrics/load']) {
    assert.equal((await get(route,{headers:{}})).status,401);
    assert.equal((await get(route,{headers:{Authorization:'Bearer '+'b'.repeat(64)}})).status,401);
  }
  assert.equal((await get('/api/v1/status?token='+TOKEN,{headers:{}})).status,401);
  const write=await get('/api/v1/status',{method:'POST',body:'delete'});assert.equal(write.status,405);assert.equal(write.headers.get('allow'),'GET');
  const status=await get('/api/v1/status');assert.equal(status.status,200);assert.equal(status.body.apiVersion,1);assert.equal(status.headers.get('cache-control'),'no-store');
  assert.equal(status.headers.get('access-control-allow-origin'),null);
  assert.equal(JSON.stringify(status.body).includes(TOKEN),false);assert.equal(JSON.stringify(status.body).includes(logger.config.database),false);
  assert.equal(logger.store.db.prepare('SELECT COUNT(*) AS n FROM history').get().n,before);
  assert.throws(()=>api.reader.db.exec('DELETE FROM history'),/readonly/);
  logger.store.save([rec('load',stats(-1))],BASE+30000);
  assert.equal((await get('/api/v1/history/metrics/load')).body.rows[0].values[1],-1);
});

test('combined history pagination preserves gaps, source references, zero PV2 and partial intervals', async t=> {
  const {get,logger,clock}=await setup(t);
  logger.store.save([rec('pico-battery',stats()),rec('sbms-battery',stats(-3,12,65)),rec('sbms-pv2',stats(0,12,null)),
    rec('pico-battery',stats(-4,12,79),BASE+120000),rec('sbms-pv2',stats(0,12,null),BASE+120000)],BASE+150000);
  clock.time=BASE+180000;
  const query='?from=2026-01-15T12:00:00Z&to=2026-01-15T12:03:00Z&limit=1';
  const first=await get('/api/v1/history/battery'+query);assert.equal(first.status,200);
  const b=first.body.rows[0];assert.equal(b.values.picoCurrent,-2);assert.equal(b.values.picoSoc,80);assert.equal(b.values.sbmsSoc,65);
  assert.equal(b.values.pv2Watts,0);assert.equal(b.coverageSeconds.pv2Watts,30);assert.equal(b.partial,false);
  assert.equal(first.body.sources.picoCurrent.sensorId,101);assert.equal(first.body.sources.picoWatts.voltageSource.source,'sbms');
  const second=await get('/api/v1/history/battery?'+new URLSearchParams(first.body.next));
  assert.equal(second.body.rows.length,1);assert.equal(second.body.rows[0].start,'2026-01-15T12:02:00.000Z');assert.equal(second.body.rows[0].partial,true);
  assert.equal(second.body.rows[0].values.sbmsCurrent,null);assert.equal(second.body.rows[0].values.sbmsSoc,null);assert.equal(second.body.next,null);
  const member=await get('/api/v1/history/metrics/pico-battery'+query);assert.deepEqual(member.body.rows[0].values,[-24,-2,12,80]);
  assert.equal(logger.store.db.prepare('SELECT COUNT(*) AS n FROM history').get().n,0);
});

test('environmental history and catalogue expose agreed units, periods and retained daily extrema', async t=> {
  const {get,logger,clock}=await setup(t);clock.time=BASE+3600000;
  logger.store.save([rec('outside',{...empty(),last:8,min:4,max:9,lastAt:BASE,samples:1},BASE,'hour',3600000),
    rec('pressure',{...empty(),last:1001,min:1000,max:1001,lastAt:BASE,samples:1},BASE,'hour',3600000)],clock.time);
  const outside=await get('/api/v1/history/metrics/outside');assert.equal(outside.body.rows[0].value,8);assert.equal(outside.body.rows[0].min,4);assert.equal(outside.body.rows[0].max,9);
  assert.equal((await get('/api/v1/history/metrics/outside?resolution=minute')).status,400);
  const catalogue=(await get('/api/v1/metrics')).body;assert.equal(catalogue.timezone,'UTC');
  assert.equal(catalogue.metrics.find(m=>m.id==='outside').units.value,'°C');assert.equal(catalogue.metrics.find(m=>m.id==='pressure').units.value,'hPa');
  assert.equal(catalogue.metrics.find(m=>m.id==='pico-battery').name,'House  ');assert.equal(catalogue.metrics.find(m=>m.id==='outside').retention.day,null);
  assert.equal(catalogue.picoSensors.find(s=>s.sensorId===103).name,'Module [test]');
});

test('live freshness is monotonic and source-specific; derived watts require fresh valid voltage and safe mappings', async t=> {
  const {get,api,logger,clock}=await setup(t);
  const receivedAt=new Date(BASE).toISOString();
  api.status('pico','connected');api.status('sbms','connected');
  api.accept('pico',{receivedAt,receivedMonotonicMs:0,readings:{101:{type:'battery',current:-2,voltage:10,stateOfCharge:80},102:{type:'current',current:-1},103:{type:'volt',voltage:11.9}}});
  api.accept('sbms',{receivedAt,receivedMonotonicMs:0,voltage:12,voltageStatus:'valid',stateOfCharge:65,current:{battery:-3,pv1:1,pv2:0,externalLoad:2.9}});
  let live=(await get('/api/v1/live')).body;assert.equal(live.pico.fresh,true);assert.equal(live.sbms.fresh,true);
  assert.equal(live.measurements.find(m=>m.id==='pico-battery').values.watts,-24);assert.equal(live.measurements.find(m=>m.id==='sbms-pv2').values.watts,0);
  clock.mono=15100;clock.time=BASE-86400000;live=(await get('/api/v1/live')).body;
  assert.equal(live.pico.fresh,false);assert.equal(live.pico.readings,null);assert.equal(live.sbms.fresh,true);
  assert.equal(live.measurements.find(m=>m.id==='pico-battery').values.current,null);
  assert.equal(live.measurements.find(m=>m.id==='pico-battery').values.voltage,12);
  clock.mono=30100;api.accept('pico',{receivedAt,receivedMonotonicMs:30100,readings:{101:{type:'battery',current:-2,stateOfCharge:80}}});
  live=(await get('/api/v1/live')).body;assert.equal(live.pico.fresh,true);assert.equal(live.sbms.fresh,false);
  assert.equal(live.measurements.find(m=>m.id==='pico-battery').values.current,-2);assert.equal(live.measurements.find(m=>m.id==='pico-battery').values.watts,null);
  logger.active.delete('pico-battery');live=(await get('/api/v1/live')).body;assert.equal(live.measurements.find(m=>m.id==='pico-battery').mappingValid,false);
  assert.equal(live.measurements.find(m=>m.id==='pico-battery').values.current,null);
  api.status('pico','retrying');assert.equal((await get('/api/v1/live')).body.pico.readings,null);
});

test('live SBMS exposes complete broadcast and boolean flags without adding flag history', async t=> {
  const {get,api,logger,clock}=await setup(t);
  const data={time:{year:1,month:2,day:3,hour:4,minute:5,second:6},soc:65,
    cellsMV:[3000,3000,3000,3000,0,0,0,0],currentMA:{battery:-3000,pv1:1000,pv2:0,extLoad:2900},
    tempInt:21,tempExt:18,ad2:0,ad3:12,ad4:34,heat1:0,heat2:567,
    flags:{CFET:true,DFET:false,OVLK:false,UVLK:true,EOC:false,IOT:true,LVC:false,CELF:true,delta:7},
    futureField:{available:true}};
  const accept=()=> {
    const reading=decodeSbms(Buffer.from(JSON.stringify(data)),{activeCells:[0,1,2,3],receivedAt:new Date(clock.time),monotonicMs:clock.mono});
    logger.acceptSbms(reading);api.accept('sbms',reading);api.status('sbms','connected');
  };
  accept();
  let live=(await get('/api/v1/live')).body;
  assert.deepEqual(live.sbms.reading.broadcast,data);
  for(const key of ['CFET','DFET','OVLK','UVLK','EOC','IOT','LVC','CELF']) assert.equal(live.sbms.reading.flags[key],data.flags[key]);
  assert.equal(live.sbms.reading.flags.OV,null);assert.equal(live.sbms.reading.current.battery,-3);
  clock.time+=1000;clock.mono+=1000;data.flags.CFET=false;data.flags.DFET=true;accept();
  live=(await get('/api/v1/live')).body;
  assert.equal(live.sbms.reading.flags.CFET,false);assert.equal(live.sbms.reading.flags.DFET,true);
  logger.flush(clock.time,clock.mono);
  const history=(await get('/api/v1/history/battery')).body;
  assert.ok(history.rows.length>0);
  for(const key of ['flags','broadcast','CFET','tempInt','futureField']) assert.equal(JSON.stringify(history).includes('"'+key+'"'),false);
  assert.equal((await get('/api/v1/metrics')).body.metrics.length,9);
  assert.equal((await get('/api/v1/history/metrics/CFET')).status,404);
  clock.mono+=30100;assert.equal((await get('/api/v1/live')).body.sbms.reading,null);
  accept();api.status('sbms','disconnected');assert.equal((await get('/api/v1/live')).body.sbms.reading,null);
});

test('invalid dates, duplicate parameters, large limits, unknown metrics and SQL/path input are rejected', async t=> {
  const {get}=await setup(t);
  for(const query of ['resolution=week','limit=1001','limit=-1','limit=0','limit=1&limit=2','from=2026-02-30T00:00:00Z','from=2026-01-15T12:00:00%2B00:00','from=2026-01-16T12:00:00Z&to=2026-01-15T12:00:00Z','sql=DROP%20TABLE%20history','limit=']) assert.equal((await get('/api/v1/history/battery?'+query)).status,400,query);
  assert.equal((await get('/api/v1/history/metrics/unknown')).status,404);assert.equal((await get('/api/v1/history/metrics/load%27')).status,404);
  assert.equal((await get('/api/v1/status?token='+TOKEN)).status,400);
  const q=range(new URLSearchParams('resolution=day&to=2026-03-31T00:00:00Z'),BASE);assert.equal(new Date(q.from).toISOString(),'2026-02-28T00:00:00.000Z');
});

test('request rate and response size are bounded; storage failures report generic errors and logging failure is visible', async t=> {
  const {get,api,logger}=await setup(t);
  api.budget=0;const limited=await get('/api/v1/live');assert.equal(limited.status,429);assert.equal(limited.headers.get('retry-after'),'1');api.budget=60;
  logger.failed=true;assert.equal((await get('/api/v1/status')).body.logging.state,'failed');
  api.reader.history=()=>({rows:['x'.repeat(4*1024*1024)]});assert.equal((await get('/api/v1/history/battery')).status,413);
  api.reader.history=()=> {throw Error('secret path /private/example token '+TOKEN);};
  const error=await get('/api/v1/history/battery');assert.equal(error.status,503);assert.deepEqual(error.body,{apiVersion:1,error:'history_unavailable'});
});

test('optional HTTPS uses configured certificate/key and still requires authentication', async t=> {
  const directory=fs.mkdtempSync(path.join(os.tmpdir(),'api-tls-'));t.after(()=>fs.rmSync(directory,{recursive:true,force:true}));
  const key=path.join(directory,'key.pem'),cert=path.join(directory,'cert.pem');
  const generated=spawnSync('openssl',['req','-x509','-newkey','rsa:2048','-nodes','-keyout',key,'-out',cert,'-days','1','-subj','/CN=localhost'],{encoding:'utf8'});
  assert.equal(generated.status,0,generated.stderr);
  const file=path.join(directory,'api.json');fs.writeFileSync(file,JSON.stringify({host:'127.0.0.1',port:8080,token:TOKEN,tls:{key:'key.pem',cert:'cert.pem'}}));
  const tls=readApiConfig(file).tls;const {api}=await setup(t,{tls});
  const result=await new Promise((resolve,reject)=> {
    https.get({hostname:'127.0.0.1',port:api.server.address().port,path:'/api/v1/status',rejectUnauthorized:false,headers:{Authorization:'Bearer '+TOKEN}},res=> {let text='';res.on('data',d=>text+=d);res.on('end',()=>resolve({status:res.statusCode,body:JSON.parse(text)}));}).on('error',reject);
  });
  assert.equal(result.status,200);assert.equal(result.body.schemaVersion,2);
});

test('API check reads private credentials without URL tokens; disabled/conflicting or invalid collector setup is explicit', async t=> {
  const {api,directory}=await setup(t),file=path.join(directory,'api.json');
  fs.writeFileSync(file,JSON.stringify({host:'127.0.0.1',port:api.server.address().port,token:TOKEN}));
  assert.equal((await check(['--config',file])).apiVersion,1);
  await assert.rejects(check(['--config',file,'--path','https://example.invalid/']),/path/);
  const cli=path.join(__dirname,'../bin/pico.js');
  const conflict=spawnSync(process.execPath,[cli,'--api-config',file,'--no-api'],{encoding:'utf8'});assert.equal(conflict.status,2);
  const noLogging=spawnSync(process.execPath,[cli,'--api-config',file,'--no-logging','--no-mqtt'],{encoding:'utf8'});assert.equal(noLogging.status,1);assert.match(noLogging.stderr,/API setup failed/);
  assert.equal(noLogging.stderr.includes(TOKEN),false);
});


test('live SBMS gaps use the receiver timeout, not the shorter integration cutoff', async t=> {
  const {api,logger,clock,get}=await setup(t);
  const reading={receivedAt:new Date(BASE).toISOString(),receivedMonotonicMs:0,voltage:12,voltageStatus:'valid',stateOfCharge:65,current:{battery:-3,pv1:1,pv2:0,externalLoad:2}};
  api.status('sbms','connected');api.accept('sbms',reading);
  assert.equal(logger.config.maxGapSeconds.sbms,3);
  clock.mono=5000;
  let result=(await get('/api/v1/live')).body;
  assert.equal(result.sbms.fresh,true);assert.equal(result.sbms.ageSeconds,5);assert.equal(result.sbms.reading.current.battery,-3);
  api.setLiveTimeout('sbms',6000);clock.mono=6001;
  result=(await get('/api/v1/live')).body;assert.equal(result.sbms.state,'stale');assert.equal(result.sbms.fresh,false);assert.equal(result.sbms.reading,null);
  api.accept('sbms',{...reading,receivedMonotonicMs:clock.mono});assert.equal(api.source('sbms').fresh,true);
  api.status('sbms','disconnected');assert.equal(api.source('sbms').reading,null);
  assert.equal(logger.config.maxGapSeconds.sbms,3);
  assert.throws(()=>api.setLiveTimeout('sbms',0));assert.throws(()=>api.setLiveTimeout('unknown',6000));
});
