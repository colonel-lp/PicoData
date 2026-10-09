'use strict';
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { spawnSync, spawn } = require('node:child_process');
const { HistoryLogger, bounds } = require('../lib/logger');
const { HistoryStore, monthBefore } = require('../lib/history-store');
const { validateConfig } = require('../lib/history-config');
const { inspect } = require('../bin/history');
const { replay } = require('../bin/replay-logging');
const { fixture } = require('./fixtures');
const BASE = Date.parse('2026-01-15T12:00:00Z');
const metrics = [
  { id: 'battery', source: 'pico', sensorId: 101, sensorType: 'battery', kind: 'electrical', role: 'battery', polarity: 1, voltage: 'sbms' },
  { id: 'load', source: 'pico', sensorId: 102, sensorType: 'current', kind: 'electrical', role: 'load', polarity: -1, voltage: 'sbms' },
  { id: 'secondary', source: 'pico', sensorId: 103, sensorType: 'volt', kind: 'voltage' },
  { id: 'outside', source: 'pico', sensorId: 104, sensorType: 'thermometer', kind: 'temperature' },
  { id: 'pressure', source: 'pico', sensorId: 105, sensorType: 'barometer', kind: 'barometer' },
  { id: 'sbms-battery', source: 'sbms', field: 'battery', kind: 'electrical', role: 'battery', polarity: 1, voltage: 'self' },
  { id: 'pv2', source: 'sbms', field: 'pv2', kind: 'electrical', role: 'supply', polarity: null, voltage: null },
];
const sensors = Object.fromEntries([[101,'battery','House battery'],[102,'current','Cabin load'],[103,'volt','Module [example]'],[104,'thermometer','Outdoor'],[105,'barometer','Pressure']].map(([id,type,name],pos)=>[id,{type,name,pos}]));
function setup(t, extra = {}) {
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'pico-history-'));
  const config = validateConfig({ database: path.join(directory, 'history.sqlite'), commitSeconds: 60,
    maxGapSeconds: { pico: 60, sbms: 60 }, metrics: structuredClone(metrics), ...extra });
  const logger = new HistoryLogger(config, { automatic: false });
  logger.configurePico({ sensorList: sensors, config: {} }, BASE);
  t.after(() => { logger.close({ advance: false }); fs.rmSync(directory, { recursive: true, force: true }); });
  return { logger, config, directory };
}
function pico(logger, seconds, values = {}, base = BASE) {
  const readings = Object.fromEntries(Object.entries(values).map(([id,v])=>[id,{...sensors[id],...v}]));
  logger.acceptPico({ receivedAt: new Date(base + seconds * 1000).toISOString(), receivedMonotonicMs: seconds * 1000, readings });
}
function sbms(logger, seconds, voltage = 12, battery = 0, soc = 70, pv2 = 0, base = BASE) {
  logger.acceptSbms({ receivedAt: new Date(base + seconds * 1000).toISOString(), receivedMonotonicMs: seconds * 1000,
    voltage, voltageStatus: voltage === null ? 'unavailable' : 'valid', stateOfCharge: soc, current: { battery, pv2 } });
}
const tick = (logger, seconds, base = BASE) => logger.tick(base + seconds * 1000, seconds * 1000);
const row = (config, id, resolution = 'minute') => inspect(config, { metric: id, resolution }).metrics[0].rows[0];
function approx(v, e) { assert.ok(Math.abs(v - e) < 1e-9, `${v} != ${e}`); }

test('instantaneous watts are duration weighted; load/battery directions and device SOC remain separate', t => {
  const { logger, config } = setup(t);
  sbms(logger, 0, 10, -2, 60); pico(logger, 0, { 101: { current: -2, stateOfCharge: 80 }, 102: { current: -3 } });
  sbms(logger, 10, 20, 4, 65); pico(logger, 10, { 101: { current: 4, stateOfCharge: 81 }, 102: { current: 2 } });
  tick(logger, 30); logger.flush();
  const r = row(config, 'battery'); approx(r.values[0], 1400 / 30); approx(r.values[1], 2); approx(r.values[2], 500 / 30);
  assert.notEqual(r.values[0], r.values[1] * r.values[2]); assert.equal(r.values[3], 81);
  approx(r.forwardWh, 1600 / 3600); approx(r.reverseWh, 200 / 3600);
  const load = row(config, 'load'); approx(load.values[1], 1/3); approx(load.forwardWh, 300 / 3600); approx(load.reverseWh, 800 / 3600);
  assert.equal(row(config, 'sbms-battery').soc, 65);
});

test('freshness is independent for current/voltage; missing differs from zero and invalid SOC', t => {
  const { logger, config } = setup(t, { maxGapSeconds: { pico: 2, sbms: 3 } });
  sbms(logger, 0); pico(logger, 0, { 101: { current: -2, stateOfCharge: 80 } });
  for (let s = 1; s <= 5; s++) pico(logger, s, { 101: { current: -2, stateOfCharge: 80 } });
  tick(logger, 10); logger.flush();
  const r = row(config, 'battery'); approx(r.ampCoverageSeconds, 7); approx(r.powerCoverageSeconds, 3); approx(r.netWh, -72/3600);
  const zero = row(config, 'pv2'); assert.equal(zero.values[1], 0); assert.equal(zero.values[0], null); assert.equal(zero.forwardAh, null);
  pico(logger, 11, { 101: { stateOfCharge: 101 } }); logger.flush(); assert.equal(row(config, 'battery').soc, 80);
});

test('disconnect cuts only the affected source and reconnect does not bridge downtime', t => {
  const { logger, config } = setup(t);
  sbms(logger, 0); pico(logger, 0, { 101: { current: -2 } }); logger.unavailable('sbms', BASE + 2000, 2000);
  pico(logger, 10, { 101: { current: -2 } }); sbms(logger, 20); tick(logger, 25); logger.flush();
  const r = row(config, 'battery'); assert.equal(r.ampCoverageSeconds, 25); assert.equal(r.powerCoverageSeconds, 7);
});

test('UTC month/day/hour/minute boundary splitting keeps additive parent energy and no carried SOC', t => {
  const base = Date.parse('2026-01-31T23:59:55Z'), { logger, config } = setup(t);
  sbms(logger, 0, 10, 0, 70, 0, base); pico(logger, 0, { 101: { current: -2, stateOfCharge: 80 } }, base); tick(logger, 10, base); logger.flush();
  const r = inspect(config, { metric: 'battery', limit: 2 }).metrics[0].rows;
  assert.deepEqual(r.map(v=>v.powerCoverageSeconds), [5,5]); assert.equal(r[0].soc, null); assert.equal(r[1].soc,80);
  const month = inspect(config, { metric: 'battery', resolution: 'month', limit: 2 }).metrics[0].rows;
  assert.deepEqual(month.map(v=>v.powerCoverageSeconds),[5,5]); approx(month.reduce((s,v)=>s+v.netWh,0),-200/3600);
  assert.deepEqual(bounds(Date.parse('2024-02-29T12:00:00Z'),'month').map(v=>new Date(v).toISOString()),['2024-02-01T00:00:00.000Z','2024-03-01T00:00:00.000Z']);
});

test('environmental hourly last and daily extrema use all fresh samples without empty-day records', t => {
  const { logger, config } = setup(t);
  pico(logger, 0, {104:{temperature:4},105:{pressure:1000}}); pico(logger,10,{104:{temperature:-3},105:{pressure:1002}}); pico(logger,20,{104:{temperature:9},105:{pressure:999}});
  tick(logger,60); logger.flush();
  const r = row(config,'outside','day'); assert.equal(r.min,-3); assert.equal(r.max,9); assert.equal(r.last,9);
  assert.equal(row(config,'pressure','day').value,999); assert.equal(row(config,'outside','hour').value,9); assert.equal(row(config,'outside'),undefined);
  tick(logger,2*86400); logger.flush(); assert.equal(inspect(config,{metric:'outside',resolution:'day',limit:10}).metrics[0].rows.length,1);
});

test('restart recovers partial parent sums without integrating downtime or double counting', t => {
  const { logger, config } = setup(t); sbms(logger,0); pico(logger,0,{101:{current:-2,stateOfCharge:80}}); tick(logger,10); logger.close({advance:false});
  const next = new HistoryLogger(config,{automatic:false});
  try {
    next.configurePico({sensorList:sensors,config:{}},BASE+20000); sbms(next,20); pico(next,20,{101:{current:-4,stateOfCharge:79}}); tick(next,30); next.flush();
    const r=row(config,'battery','hour'); assert.equal(r.ampCoverageSeconds,20); assert.equal(r.ampSeconds,-60); assert.equal(r.soc,79); assert.equal(inspect(config).integrity,'ok');
  } finally {next.close({advance:false});}
});

test('writer lease prevents overlap while read-only inspection and private file permissions work', t => {
  const { logger, config }=setup(t); assert.throws(()=>new HistoryLogger(config,{automatic:false}),/locked/);
  sbms(logger,0); pico(logger,0,{101:{current:0}}); tick(logger,1); logger.flush(); assert.equal(inspect(config).integrity,'ok');
  assert.equal(fs.statSync(config.database).mode&0o777,0o600); logger.close({advance:false}); new HistoryLogger(config,{automatic:false}).close({advance:false});
});

test('retention prunes short summaries but retains electrical months and environmental days', t => {
  const {logger,config}=setup(t), time=Date.parse('2026-03-31T12:00:00Z'), rows=[];
  for(const id of ['battery','outside']) for(const resolution of ['minute','hour','day','month']) {
    const [start,end]=bounds(Date.parse('2026-01-01T00:00:00Z'),resolution); rows.push({id,resolution,start,end,data:{}});
  }
  logger.store.save(rows,time);
  assert.deepEqual(logger.store.db.prepare('SELECT metric_id,resolution FROM history ORDER BY metric_id,resolution').all().map(r=>[r.metric_id,r.resolution]),[['battery','month'],['outside','day'],['outside','month']]);
  assert.equal(new Date(monthBefore(time)).toISOString(),'2026-02-28T12:00:00.000Z'); assert.equal(new Date(monthBefore(Date.parse('2024-03-31T12:00:00Z'))).toISOString(),'2024-02-29T12:00:00.000Z');
  const [start,end]=bounds(time-10*86400000,'hour'); logger.store.save(['battery','outside'].map(id=>({id,resolution:'hour',start,end,data:{}})),time);
  assert.equal(row(config,'battery','hour'),undefined); assert.ok(row(config,'outside','hour'));
});

test('clock adjustments skip untrusted intervals and do not invent coverage', t => {
  const {logger,config}=setup(t), states=[]; logger.on('status',s=>states.push(s)); sbms(logger,0); pico(logger,0,{101:{current:-2}});
  logger.tick(BASE+600000,1000); logger.flush(); assert.equal(row(config,'battery').ampCoverageSeconds,0); assert.ok(states.some(s=>s.state==='clock-step'));
  pico(logger,2,{101:{current:-2}},BASE+600000); tick(logger,3,BASE+600000); logger.flush();
  assert.equal(row(config,'battery').ampCoverageSeconds,1); assert.equal(row(config,'battery').powerCoverageSeconds,0);
});

test('label rename preserves identity; type/binding changes are not silently merged', t => {
  const {logger,config}=setup(t); logger.configurePico({sensorList:{...sensors,101:{...sensors[101],name:'Renamed battery'}},config:{}},BASE);
  assert.equal(inspect(config,{metric:'battery'}).metrics[0].name,'Renamed battery');
  logger.configurePico({sensorList:{...sensors,101:{...sensors[101],type:'current'}},config:{}},BASE); sbms(logger,0); pico(logger,0,{101:{current:-2}}); tick(logger,1); logger.flush(); assert.equal(row(config,'battery'),undefined);
  logger.close({advance:false}); const changed=structuredClone(config); changed.metrics[0].sensorId=999;
  assert.throws(()=>new HistoryLogger(changed,{automatic:false}),/binding changed/);
});

test('storage failure stops logging visibly with bounded buffers', t => {
  const {logger}=setup(t), statuses=[]; logger.on('status',s=>statuses.push(s)); sbms(logger,0); pico(logger,0,{101:{current:-2}});
  logger.store.save=()=>{throw Error('Disk full');}; tick(logger,60); assert.equal(logger.failed,true); assert.ok(statuses.some(s=>s.state==='failed')); assert.equal(logger.rows.size,0);
  for(let i=0;i<1000;i++) sbms(logger,61+i); assert.equal(logger.rows.size,0);
});

test('configuration rejects duplicate selections, ambiguous polarity and unverified shunt voltage', () => {
  const config={metrics:structuredClone(metrics)}; config.metrics.push({...config.metrics[0],id:'duplicate'}); assert.throws(()=>validateConfig(config),/Duplicate source/);
  config.metrics.pop(); config.metrics[0].polarity=0; assert.throws(()=>validateConfig(config),/polarity/); config.metrics[0].polarity=1; config.metrics[1].voltage='self'; assert.throws(()=>validateConfig(config),/Shunts/);
});

test('private generator/replay select IDs, preserve hidden voltage and refuse overwrites', async t => {
  const {directory}=setup(t), data=fixture(), capture=path.join(directory,'capture.jsonl'), filename=path.join(directory,'logging.json');
  fs.writeFileSync(capture,JSON.stringify({kind:'config',config:data.config})+'\n'+[0,1000].map(ms=>JSON.stringify({kind:'packet',receivedAt:new Date(BASE+ms).toISOString(),hex:data.packet.toString('hex')})).join('\n')+'\n');
  const args=[path.join(__dirname,'../bin/init-logging.js'),'--capture',capture,'--output',filename,'--battery-id','15','--secondary-voltage-id','20','--outside-id','11','--barometer-id','12','--load-ids','90','--load-polarity','-1'];
  const init=spawnSync(process.execPath,args,{encoding:'utf8'}); assert.equal(init.status,0,init.stderr); assert.equal(fs.statSync(filename).mode&0o777,0o600); assert.equal(spawnSync(process.execPath,args).status,1);
  const config=validateConfig(JSON.parse(fs.readFileSync(filename)),filename);
  assert.ok(config.metrics.filter(m => m.source === 'sbms').every(m => m.voltage === 'self'));
  config.database=path.join(directory,'replay.sqlite');
  assert.deepEqual(await replay(capture,config),{packets:2,sbms:0,ok:true}); assert.equal(row(config,'secondary-voltage').value,9.999); assert.equal(row(config,'pico-battery').values[0],null);
  await assert.rejects(replay(capture,config),/already exists/);
});

test('crash releases the writer lock and rolls back the uncommitted tail', async t => {
  const {logger,config,directory}=setup(t); logger.close({advance:false}); const script=path.join(directory,'crash.cjs');
  fs.writeFileSync(script,`const {HistoryLogger}=require(${JSON.stringify(path.join(__dirname,'../lib/logger'))}); const l=new HistoryLogger(${JSON.stringify(config)},{automatic:false});
    l.store.save([{id:'battery',resolution:'hour',start:${BASE},end:${BASE+3600000},data:{ampSeconds:5}}],${BASE});
    l.store.db.exec('BEGIN IMMEDIATE');l.store.db.prepare('UPDATE history SET data=?').run('{"ampSeconds":99}');process.stdout.write('ready\\n');setInterval(()=>{},1000);`);
  const child=spawn(process.execPath,[script]); t.after(()=>{if(child.exitCode===null)child.kill('SIGKILL');});
  await new Promise((resolve,reject)=>{child.stdout.once('data',resolve);child.once('error',reject);child.once('exit',code=>{if(code!==null)reject(Error('Child exited before ready'));});});
  const closed=new Promise(resolve=>child.once('close',resolve)); child.kill('SIGKILL'); await closed;
  const store=new HistoryStore(config); try {assert.equal(store.read('battery','hour',BASE).ampSeconds,5);} finally {store.close();}
});

test('backward clock step leaves a gap until the previous UTC position is reached', t => {
  const {logger,config}=setup(t); sbms(logger,0); pico(logger,0,{101:{current:-2}}); tick(logger,10);
  logger.tick(BASE+5000,11000); pico(logger,12,{101:{current:-2}},BASE-6000);
  logger.tick(BASE+10000,16000); sbms(logger,17,12,0,70,0,BASE-6000); pico(logger,17,{101:{current:-2}},BASE-6000);
  tick(logger,18,BASE-6000); logger.flush(); assert.equal(row(config,'battery').ampCoverageSeconds,11); assert.equal(inspect(config).clockSteps,1);
});

test('physical binding mismatch disables only the selected channel, not other sensors', t => {
  const selected=structuredClone(metrics); selected[0].expectedBinding='[[0,103],[0,102],[0,102]]';
  const {logger,config}=setup(t,{metrics:selected}), states=[]; logger.on('status',s=>states.push(s));
  const cfg={0:{0:[0,101],4:[0,999],10:[0,102],11:[0,102]}};
  logger.configurePico({sensorList:sensors,config:cfg},BASE); sbms(logger,0); pico(logger,0,{101:{current:-2},102:{current:-1},103:{voltage:12}}); tick(logger,1); logger.flush();
  assert.equal(row(config,'battery'),undefined); assert.equal(row(config,'load').values[1],-1); assert.ok(states.some(s=>s.metric==='battery'));
});


test('battery-side PV channels use fresh SBMS pack voltage and preserve zero, signs and coverage', t => {
  const selected=structuredClone(metrics); selected.find(m=>m.id==='pv2').voltage='self';
  selected.push({id:'pv1',source:'sbms',field:'pv1',kind:'electrical',role:'supply',polarity:null,voltage:'self'});
  const {logger,config}=setup(t,{metrics:selected,maxGapSeconds:{pico:2,sbms:3}});
  const sample=(seconds,voltage,pv1,pv2)=>logger.acceptSbms({receivedAt:new Date(BASE+seconds*1000).toISOString(),receivedMonotonicMs:seconds*1000,
    voltage,voltageStatus:voltage===null?'unavailable':'valid',stateOfCharge:70,current:{battery:0,pv1,pv2}});
  sample(0,12,2,0); sample(1,14,3,0); sample(2,null,4,-1); tick(logger,6); logger.flush();
  const pv1=row(config,'pv1'), pv2=row(config,'pv2');
  approx(pv1.netWh,66/3600); approx(pv1.values[0],33); assert.equal(pv1.powerCoverageSeconds,2); assert.equal(pv1.ampCoverageSeconds,5);
  assert.equal(pv2.netWh,0); assert.equal(pv2.values[0],0); approx(pv2.netAh,-3/3600);
  assert.equal(pv1.forwardWh,null); // Polarity remains unverified; signed totals are still valid.
});
