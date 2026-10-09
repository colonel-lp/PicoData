'use strict';
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { spawnSync } = require('node:child_process');
const { validateConfig } = require('../lib/history-config');
const { HistoryStore, sqlite } = require('../lib/history-store');
const { empty } = require('../lib/logger');
const { inspect } = require('../bin/history');
const BASE = Date.parse('2026-01-15T12:00:00Z');
function setup(t) {
  const directory=fs.mkdtempSync(path.join(os.tmpdir(),'battery-history-'));
  t.after(()=>fs.rmSync(directory,{recursive:true,force:true}));
  return validateConfig({database:path.join(directory,'history.sqlite'),metrics:[
    {id:'pico-battery',source:'pico',sensorId:101,sensorType:'battery',kind:'electrical',role:'battery',polarity:1,voltage:'sbms'},
    {id:'secondary-voltage',source:'pico',sensorId:103,sensorType:'volt',kind:'voltage'},
    {id:'load',source:'pico',sensorId:102,sensorType:'current',kind:'electrical',role:'load',polarity:-1,voltage:'sbms'},
    {id:'outside',source:'pico',sensorId:104,sensorType:'thermometer',kind:'temperature'},
    {id:'pressure',source:'pico',sensorId:105,sensorType:'barometer',kind:'barometer'},
    ...['battery','pv1','pv2','externalLoad'].map(field=>({id:'sbms-'+field,source:'sbms',field,kind:'electrical',role:field==='battery'?'battery':field.startsWith('pv')?'supply':'unknown',polarity:field==='battery'?1:null,voltage:'self'})),
  ]});
}
function data(amp,volt,soc=null,seconds=30) {
  return {...empty(),ampSeconds:amp*seconds,voltSeconds:volt*seconds,wattSeconds:amp*volt*seconds,
    ampCoverageSeconds:seconds,voltCoverageSeconds:seconds,powerCoverageSeconds:seconds,
    forwardAmpSeconds:Math.max(0,amp)*seconds,reverseAmpSeconds:Math.max(0,-amp)*seconds,
    forwardWattSeconds:Math.max(0,amp*volt)*seconds,reverseWattSeconds:Math.max(0,-amp*volt)*seconds,
    soc,socAt:soc===null?null:BASE+25000,currentLastAt:BASE+25000,voltageLastAt:BASE+25000,samples:30};
}
const rec=(id,value,start=BASE,resolution='minute',length=60000)=>({id,resolution,start,end:start+length,data:value});
function createV1(config,records,{keepOpen=false}={}) {
  const DatabaseSync=sqlite(), db=new DatabaseSync(config.database);
  db.exec(`PRAGMA journal_mode=WAL; PRAGMA synchronous=FULL;
    CREATE TABLE metrics(id TEXT PRIMARY KEY,definition TEXT NOT NULL,name TEXT NOT NULL,source_name TEXT NOT NULL);
    CREATE TABLE names(metric_id TEXT NOT NULL,changed_ms INTEGER NOT NULL,name TEXT NOT NULL,source_name TEXT NOT NULL,PRIMARY KEY(metric_id,changed_ms));
    CREATE TABLE history(metric_id TEXT NOT NULL,resolution TEXT NOT NULL,start_ms INTEGER NOT NULL,end_ms INTEGER NOT NULL,data TEXT NOT NULL,PRIMARY KEY(metric_id,resolution,start_ms));
    CREATE TABLE meta(key TEXT PRIMARY KEY,value TEXT NOT NULL); PRAGMA user_version=1;`);
  for(const m of config.metrics) {const {name,...definition}=m;db.prepare('INSERT INTO metrics VALUES(?,?,?,?)').run(m.id,JSON.stringify(definition),name||m.id,name||m.id);}
  db.prepare('INSERT INTO names VALUES(?,?,?,?)').run('pico-battery',BASE,'Original label','Original source');
  db.prepare('INSERT INTO meta VALUES(?,?)').run('savedAt',new Date(BASE).toISOString());
  db.prepare('INSERT INTO meta VALUES(?,?)').run('clockSteps','2');
  for(const row of records) db.prepare('INSERT INTO history VALUES(?,?,?,?,?)').run(row.id,row.resolution,row.start,row.end,typeof row.data==='string'?row.data:JSON.stringify(row.data));
  if(keepOpen)return db;
  db.close();
}

test('one battery interval contains all measurements and source references; other sensors stay separate', t=>{
  const config=setup(t), store=new HistoryStore(config);
  try {
    store.save([rec('pico-battery',data(-2,12,80)),rec('sbms-battery',data(-3,12,65)),
      rec('secondary-voltage',data(0,11.9)),rec('sbms-pv1',data(2,12)),rec('sbms-pv2',data(0,12)),
      rec('sbms-externalLoad',data(2.9,12)),rec('load',data(-1,12)),
      rec('outside',{...empty(),last:8,min:4,max:9},BASE,'hour',3600000),
      rec('pressure',{...empty(),last:1000},BASE,'hour',3600000)],BASE+30000);
    assert.equal(store.db.prepare('SELECT COUNT(*) AS n FROM battery_history').get().n,1);
    assert.equal(store.db.prepare('SELECT COUNT(*) AS n FROM history').get().n,3);
    const result=inspect(config), b=result.battery.rows[0];
    assert.deepEqual(b.values,{voltage:12,secondaryVoltage:11.9,picoCurrent:-2,picoWatts:-24,picoSoc:80,sbmsCurrent:-3,sbmsWatts:-36,sbmsSoc:65,
      pv1Current:2,pv1Watts:24,pv2Current:0,pv2Watts:0,externalLoadCurrent:2.9,externalLoadWatts:34.8});
    assert.equal(result.battery.sources.picoCurrent.sensorId,101); assert.equal(result.battery.sources.pv1Current.source,'sbms');
    assert.equal(b.coverageSeconds.pv2Watts,30); assert.equal(b.energy.picoBattery.netWh,-0.2); assert.equal(b.energy.pv1.forwardWh,null);
    assert.deepEqual(result.metrics.map(m=>m.id),['load','outside','pressure']);
    assert.equal(inspect(config,{metric:'sbms-pv2'}).metrics[0].rows[0].values[0],0);
    assert.equal(inspect(config,{metric:'outside',resolution:'hour'}).metrics[0].rows[0].min,4);
  } finally {store.close();}
});

test('schema-1 migration is lossless, handles bounded batches and backs up committed WAL data', t=>{
  const config=setup(t), records=[rec('pico-battery',data(-2,12,80)),rec('sbms-battery',data(-3,12,65)),
    rec('secondary-voltage',data(0,11.9)),rec('sbms-pv2',data(0,12)),rec('load',data(-1,12)),rec('pressure',{...empty(),last:1000},BASE,'hour',3600000)];
  for(let i=1;i<=300;i++) records.push(rec('pico-battery',data(-1,12,79),BASE+i*60000));
  const old=createV1(config,records,{keepOpen:true}), store=new HistoryStore(config);
  try {
    assert.equal(store.db.prepare('PRAGMA user_version').get().user_version,2);
    assert.equal(store.db.prepare('SELECT COUNT(*) AS n FROM battery_history').get().n,301);
    assert.equal(store.db.prepare('SELECT COUNT(*) AS n FROM history').get().n,2);
    for(const r of records) assert.deepEqual(store.read(r.id,r.resolution,r.start),r.data);
    assert.equal(store.db.prepare('SELECT COUNT(*) AS n FROM names').get().n,1);
    assert.equal(inspect(config).clockSteps,2);
    const backup=new (sqlite())(config.database+'.v1-backup.sqlite',{readOnly:true});
    try {assert.equal(backup.prepare('PRAGMA user_version').get().user_version,1);assert.equal(backup.prepare('SELECT COUNT(*) AS n FROM history').get().n,records.length);assert.equal(backup.prepare('PRAGMA integrity_check').get().integrity_check,'ok');}
    finally {backup.close();}
    assert.equal(fs.statSync(config.database+'.v1-backup.sqlite').mode&0o777,0o600);
  } finally {store.close();old.close();}
});

test('a failed schema migration rolls back the whole transfer and retains original rows/version', t=>{
  const config=setup(t), records=[rec('pico-battery',data(-2,12,80)),rec('sbms-battery','broken-json')];
  createV1(config,records); assert.throws(()=>new HistoryStore(config),/JSON|Unexpected token/);
  const old=new (sqlite())(config.database);
  try {
    assert.equal(old.prepare('PRAGMA user_version').get().user_version,1);
    assert.equal(old.prepare('SELECT COUNT(*) AS n FROM history').get().n,2);
    assert.equal(old.prepare("SELECT COUNT(*) AS n FROM sqlite_master WHERE name='battery_history'").get().n,0);
    assert.deepEqual(JSON.parse(old.prepare('SELECT data FROM history WHERE metric_id=?').get('pico-battery').data),records[0].data);
    old.prepare('UPDATE history SET data=? WHERE metric_id=?').run(JSON.stringify(data(-3,12,65)),'sbms-battery');
  } finally {old.close();}
  const retry=new HistoryStore(config); retry.close();
});

test('partial checkpoints and restart preserve untouched fields; missing fields stay null', t=>{
  const config=setup(t); let store=new HistoryStore(config);
  store.save([rec('pico-battery',data(-2,12,80)),rec('sbms-battery',data(-3,12,65))],BASE+30000);store.close();
  store=new HistoryStore(config);
  try {
    store.save([rec('sbms-battery',data(-4,12,64,45))],BASE+45000);
    assert.deepEqual(store.read('pico-battery','minute',BASE),data(-2,12,80));
    const b=inspect(config,{batteryOnly:true}).battery.rows[0]; assert.equal(b.values.picoSoc,80);assert.equal(b.values.sbmsSoc,64);assert.equal(b.values.pv2Current,null);
    store.save([rec('sbms-battery',data(-1,12,63),BASE+60000)],BASE+90000);
    const next=inspect(config,{batteryOnly:true}).battery.rows[0];assert.equal(next.values.picoCurrent,null);assert.equal(next.values.picoSoc,null);assert.equal(next.coverageSeconds.picoCurrent,0);
    assert.equal(inspect(config,{batteryOnly:true}).metrics.length,0);
  } finally {store.close();}
});

test('combined minute/hour/day expiry preserves monthly history and forbids implicit group rebinding', t=>{
  const config=setup(t), store=new HistoryStore(config), now=BASE+40*86400000;
  try {
    store.save([rec('pico-battery',data(-2,12,80)),rec('pico-battery',data(-2,12,80),BASE,'hour',3600000),
      rec('pico-battery',data(-2,12,80),BASE,'day',86400000),rec('pico-battery',data(-2,12,80),Date.parse('2026-01-01T00:00:00Z'),'month',31*86400000)],now);
    assert.deepEqual(store.db.prepare('SELECT resolution FROM battery_history').all().map(r=>r.resolution),['month']);
  } finally {store.close();}
  const changed=structuredClone(config);changed.metrics.push({...changed.metrics[0],id:'another-battery',sensorId:201});changed.batteryGroup.picoBattery='another-battery';
  assert.throws(()=>new HistoryStore(validateConfig(changed)),/group binding changed/);
  const reopened=new HistoryStore(config);reopened.close();
});

test('battery CLI is read-only and old member commands remain views rather than duplicate storage', t=>{
  const config=setup(t), file=path.join(path.dirname(config.database),'logging.json'), store=new HistoryStore(config);
  try {
    store.save([rec('pico-battery',data(-2,12,80))],BASE+30000);fs.writeFileSync(file,JSON.stringify(config));
    const cli=path.join(__dirname,'../bin/history.js');
    const result=spawnSync(process.execPath,[cli,'--config',file,'--battery','--limit','3'],{encoding:'utf8'});assert.equal(result.status,0,result.stderr);
    assert.equal(JSON.parse(result.stdout).battery.rows[0].values.picoCurrent,-2);
    const conflict=spawnSync(process.execPath,[cli,'--config',file,'--battery','--metric','pico-battery']);assert.equal(conflict.status,1);
    assert.equal(store.db.prepare('SELECT COUNT(*) AS n FROM history').get().n,0);
  } finally {store.close();}
});

test('migration is idempotent and legacy read-only inspection never changes a schema-1 database', t=>{
  const config=setup(t), records=[rec('pico-battery',data(-2,12,80)),rec('sbms-battery',data(-3,12,65))];createV1(config,records);
  assert.equal(inspect(config,{metric:'pico-battery'}).schemaVersion,1);
  assert.throws(()=>inspect(config,{batteryOnly:true}),/migrate/);
  const first=new HistoryStore(config);first.close();
  const before=fs.readFileSync(config.database+'.v1-backup.sqlite');
  const again=new HistoryStore(config);
  try {assert.equal(again.db.prepare('SELECT COUNT(*) AS n FROM battery_history').get().n,1);assert.deepEqual(again.read('pico-battery','minute',BASE),records[0].data);}
  finally {again.close();}
  assert.deepEqual(fs.readFileSync(config.database+'.v1-backup.sqlite'),before);
});
