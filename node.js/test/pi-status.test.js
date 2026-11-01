'use strict';
const test=require('node:test');
const assert=require('node:assert/strict');
const {PiStatus,memory,counters,cpuPercent,defaultInterface}=require('../lib/pi-status');
test('whole-system memory, CPU deltas and default route keep unknown values distinct from zero',()=>{
  assert.deepEqual(memory('MemTotal: 2048 kB\nMemAvailable: 1024 kB'),{totalBytes:2097152,availableBytes:1048576,usedBytes:1048576});
  assert.equal(memory('MemTotal: 2048 kB').availableBytes,null);
  assert.equal(memory('MemTotal: 2048 kB\nMemAvailable: 4096 kB').usedBytes,null);
  assert.equal(cpuPercent(null,counters('cpu 10 0 10 80 0 0 0 0')),null);
  assert.equal(cpuPercent([10,0,10,80,0,0,0,0],[20,0,20,160,0,0,0,0]),20);
  assert.equal(cpuPercent([20,0,20,160,0,0,0,0],[10,0,10,80,0,0,0,0]),null);
  assert.equal(defaultInterface('Iface Destination Gateway Flags RefCnt Use Metric\nwan0 00000000 01000000 0003 0 0 50\nwan1 00000000 01000000 0003 0 0 10'),'wan1');
});
test('concurrent API clients share a ten-second cache; no snapshots go to history',async()=>{
  let at=0,reads=0,runs=0;
  const values={'/proc/meminfo':'MemTotal: 2048 kB\nMemAvailable: 1024 kB','/proc/stat':'cpu 10 0 10 80 0 0 0 0',
    '/proc/net/route':'Iface Destination Gateway Flags RefCnt Use Metric\nwan0 00000000 01000000 0003 0 0 10',
    '/sys/class/thermal/thermal_zone0/temp':'25000','/proc/uptime':'300.0 200.0','/etc/pi-wifi-mode':'auto'};
  const pi=new PiStatus({read:async p=>{reads++;return values[p]||'';},interfaces:()=>({wan0:[{family:'IPv4',address:'192.0.2.10',internal:false}]}),loadavg:()=>[0,0,0],
    run:async(file)=>{runs++;return {ok:true,text:file.endsWith('iw')?'Connected\n\tSSID: Test network':'Test profile'};},now:()=>0,monotonic:()=>at});
  const [a,b]=await Promise.all([pi.status(),pi.status()]);assert.deepEqual(a,b);assert.equal(reads,6);assert.equal(runs,2);
  assert.equal(a.cpu.percent,null);assert.equal(a.temperatureC,25);assert.equal(a.network.ssid,'Test network');assert.equal(a.wifi.controlEnabled,false);
  at=5000;assert.equal((await pi.status()).ageSeconds,5);assert.equal(reads,6);
  at=10000;values['/proc/stat']='cpu 20 0 20 160 0 0 0 0';assert.equal((await pi.status()).cpu.percent,20);assert.equal(reads,12);pi.close();
});
test('missing Linux fields remain unavailable and do not prevent status',async()=>{
  const pi=new PiStatus({read:async()=>{throw Error();},stat:async()=>{throw Object.assign(Error(),{code:'EACCES'});},interfaces:()=>({}),loadavg:()=>[0,0,0]});
  const s=await pi.status();assert.equal(s.temperatureC,null);assert.equal(s.ram.totalBytes,null);assert.equal(s.cpu.percent,null);assert.equal(s.network.ssid,null);assert.equal(s.wifi.mode,null);pi.close();
});
test('Wi-Fi action is fixed, opt-in, throttled and queued without a shell',async()=>{
  const denied=new PiStatus();await assert.rejects(denied.requestMode('auto'),e=>e.status===403);await assert.rejects(denied.requestMode('reboot'),e=>e.status===400);denied.close();
  const calls=[];const pi=new PiStatus({allowWifiControl:true,stat:async()=>({isFile:()=>true,uid:0,mode:0o100755}),
    run:async(file,args)=>{calls.push({file,args});return {ok:true,text:''};}});
  assert.deepEqual(await pi.requestMode('home'),{requestedMode:'home',accepted:true,confirmed:false});
  await assert.rejects(pi.requestMode('m1'),e=>e.status===409);
  await new Promise(r=>setTimeout(r,350));assert.equal(pi.controlState,'succeeded');
  assert.deepEqual(calls.at(-1),{file:'/usr/bin/sudo',args:['-n','--','/usr/local/sbin/pi-wifi','home']});
  await assert.rejects(pi.requestMode('m1'),e=>e.status===429);pi.close();
});
test('unsafe helpers and missing noninteractive permission never queue an action',async()=>{
  for(const stat of [{isFile:()=>true,uid:1,mode:0o100755},{isFile:()=>true,uid:0,mode:0o100777}]){
    const pi=new PiStatus({allowWifiControl:true,stat:async()=>stat,run:()=>{throw Error('must not run');}});
    await assert.rejects(pi.requestMode('auto'),e=>e.code==='pi_control_setup_required');assert.equal(pi.actionTimer,null);pi.close();
  }
  const pi=new PiStatus({allowWifiControl:true,stat:async()=>({isFile:()=>true,uid:0,mode:0o100755}),run:async()=>({ok:false,text:''})});
  await assert.rejects(pi.requestMode('auto'),e=>e.status===403);pi.close();
});
