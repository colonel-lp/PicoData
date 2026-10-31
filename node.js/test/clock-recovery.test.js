'use strict';
const test=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),os=require('node:os'),path=require('node:path');
const {HistoryLogger}=require('../lib/logger');
const BASE=Date.parse('2026-01-15T12:00:00Z');
function setup(t){
  const dir=fs.mkdtempSync(path.join(os.tmpdir(),'clock-recovery-'));
  const logger=new HistoryLogger({database:path.join(dir,'history.sqlite'),commitSeconds:60,metrics:[{id:'load',source:'sbms',field:'externalLoad',kind:'electrical',role:'load',polarity:1,voltage:'self'}]},{automatic:false});
  const states=[];logger.on('status',s=>states.push(s.state));logger.tick(BASE,0);
  t.after(()=>{logger.close({advance:false});fs.rmSync(dir,{recursive:true,force:true});});
  return {logger,states,count:()=>Number(logger.store.db.prepare("SELECT value FROM meta WHERE key='clockSteps'").get()?.value??0)};
}
test('forward clock warning clears only after normal progression and a successful save',t=>{
  const {logger,states,count}=setup(t);logger.tick(BASE+10000,1000);logger.flush();assert.equal(logger.state,'clock-step');
  logger.tick(BASE+11000,2000);assert.equal(logger.state,'clock-step');logger.flush();assert.equal(logger.state,'started');assert.equal(count(),1);assert.deepEqual(states,['clock-step','started']);
  logger.tick(BASE+22000,3000);assert.equal(count(),2);
});
test('backward hold never recovers just because savedAt advances',t=>{
  const {logger,count}=setup(t);logger.tick(BASE+1000,1000);logger.tick(BASE-3000,2000);assert.equal(logger.state,'clock-step');
  for(let n=3;n<=6;n++){logger.tick(BASE-5000+n*1000,n*1000);logger.flush();assert.equal(logger.state,'clock-step');}
  logger.tick(BASE+2000,7000);logger.flush();assert.equal(logger.state,'started');assert.equal(count(),1);
});
test('failed save and unrelated diagnostics are not cleared',t=>{
  const {logger,states}=setup(t);logger.tick(BASE+10000,1000);logger.tick(BASE+11000,2000);logger.status('mapping-error');logger.flush();assert.equal(logger.state,'mapping-error');
  logger.status('clock-step');const save=logger.store.save;logger.store.save=()=>{throw Error('storage unavailable');};logger.tick(BASE+71000,62000);assert.equal(logger.failed,true);assert.equal(logger.state,'failed');assert.equal(states.at(-1),'failed');logger.store.save=save;
});
test('recovery preserves the persistent counter after reopening',t=>{
  const {logger,count}=setup(t);logger.tick(BASE+10000,1000);logger.tick(BASE+11000,2000);logger.flush();assert.equal(count(),1);
  const database=logger.config.database;logger.close({advance:false});const reopened=new HistoryLogger({...logger.config,database},{automatic:false});assert.equal(Number(reopened.store.db.prepare("SELECT value FROM meta WHERE key='clockSteps'").get().value),1);reopened.close({advance:false});
});
