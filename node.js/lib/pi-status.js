'use strict';
const fs = require('node:fs/promises');
const os = require('node:os');
const { execFile } = require('node:child_process');
const { performance } = require('node:perf_hooks');
const MODES = new Set(['auto','home','m1']);
const HELPER = '/usr/local/sbin/pi-wifi';
const validInterface = name => typeof name==='string' && /^[a-zA-Z0-9_-]{1,15}$/.test(name);
const command = (file,args,timeout=1500) => new Promise(resolve=> {
  execFile(file,args,{timeout,maxBuffer:8192,encoding:'utf8',env:{PATH:'/usr/sbin:/usr/bin:/sbin:/bin',LC_ALL:'C'}},
    (error,stdout)=>resolve({ok:!error,text:error ? '' : stdout}));
});
function memory(text) {
  const read = key=> {const m=text.match(new RegExp('^'+key+':\\s+(\\d+) kB','m'));return m ? Number(m[1])*1024 : null;};
  const total=read('MemTotal'),available=read('MemAvailable');
  return {totalBytes:total>0 ? total : null,availableBytes:total>0 && available!==null && available<=total ? available : null,
    usedBytes:total>0 && available!==null && available<=total ? total-available : null};
}
function counters(text) {
  const line=text.match(/^cpu\s+(.+)$/m);if(!line)return null;
  const values=line[1].trim().split(/\s+/).slice(0,8).map(Number);
  return values.length===8 && values.every(v=>Number.isSafeInteger(v)&&v>=0) ? values : null;
}
function cpuPercent(before,after) {
  if(!before||!after||after.some((v,i)=>v<before[i]))return null;
  const total=after.reduce((n,v,i)=>n+v-before[i],0),idle=after[3]-before[3]+after[4]-before[4];
  return total>0 ? Math.max(0,Math.min(100,100*(total-idle)/total)) : null;
}
function defaultInterface(text) {
  const rows=text.trim().split('\n').slice(1).map(l=>l.trim().split(/\s+/)).filter(a=>a[1]==='00000000' && (parseInt(a[3],16)&1) && validInterface(a[0]));
  rows.sort((a,b)=>Number(a[6])-Number(b[6]));return rows[0]?.[0] ?? null;
}
class PiStatus {
  constructor({allowWifiControl=false,uplinkInterface=null,read=fs.readFile,stat=fs.stat,run=command,
    interfaces=os.networkInterfaces,loadavg=os.loadavg,now=Date.now,monotonic=()=>performance.now()}={}) {
    Object.assign(this,{allowWifiControl,uplinkInterface,read,stat,run,interfaces,loadavg,now,monotonic});
    this.cached=null;this.pending=null;this.previous=null;this.cacheAt=-Infinity;this.controlState='idle';
    this.lastAction=-Infinity;this.actionTimer=null;this.closed=false;
  }
  async text(file) {try{const value=await this.read(file,'utf8');return value.length<=65536 ? value : '';}catch{return '';}}
  async refresh() {
    const [mem,cpu,temperature,uptime,routes,modeText]=await Promise.all([
      this.text('/proc/meminfo'),this.text('/proc/stat'),this.text('/sys/class/thermal/thermal_zone0/temp'),
      this.text('/proc/uptime'),this.text('/proc/net/route'),this.text('/etc/pi-wifi-mode')]);
    const iface=this.uplinkInterface ?? defaultInterface(routes);
    const [link,profile]=iface ? await Promise.all([
      this.run('/usr/sbin/iw',['dev',iface,'link']),
      this.run('/usr/bin/nmcli',['--escape','no','--get-values','GENERAL.CONNECTION','device','show',iface])
    ]) : [{ok:false,text:''},{ok:false,text:''}];
    const sample=counters(cpu),at=this.monotonic();
    const percent=at>this.cacheAt ? cpuPercent(this.previous,sample) : null;
    this.previous=sample;this.cacheAt=at;
    const temp=temperature.trim()!=='' ? Number(temperature)/1000 : NaN;
    const up=uptime.trim()!=='' ? Number(uptime.split(/\s+/)[0]) : NaN;
    const addresses=iface ? (this.interfaces()[iface]||[]).filter(a=>!a.internal).slice(0,8) : [];
    const ip=addresses.find(a=>a.family==='IPv4') || addresses.find(a=>a.family==='IPv6'&&!a.address.startsWith('fe80:'));
    const ssid=link.ok ? link.text.match(/^\s*SSID: (.*)$/m)?.[1] : null;
    // A missing mode file means auto in the existing helper; an unreadable/invalid
    // existing value is unknown, not a successful requested mode.
    let mode=modeText.trim();if(!mode) {try{await this.stat('/etc/pi-wifi-mode');mode=null;}catch(e){mode=e.code==='ENOENT'?'auto':null;}}
    this.cached={sampledAt:new Date(this.now()).toISOString(),ram:memory(mem),cpu:{percent,load:this.loadavg().map(x=>Number.isFinite(x)?x:null)},
      temperatureC:Number.isFinite(temp)&&temp>=-50&&temp<=150 ? temp : null,
      uptimeSeconds:Number.isFinite(up)&&up>=0 ? up : null,
      network:{interface:iface,ssid:ssid||null,profile:profile.ok ? profile.text.trim()||null : null,address:ip?.address??null},
      wifi:{mode:MODES.has(mode)?mode:null}};
  }
  async status() {
    if(this.closed)throw Error('Pi status closed');
    if(!this.cached||this.monotonic()-this.cacheAt>=10000) {
      if(!this.pending)this.pending=this.refresh().finally(()=>{this.pending=null;});
      await this.pending;
    }
    return {...this.cached,ageSeconds:Math.max(0,(this.monotonic()-this.cacheAt)/1000),
      wifi:{...this.cached.wifi,controlEnabled:this.allowWifiControl,controlState:this.controlState}};
  }
  async requestMode(mode) {
    const fail=(status,code)=>{const e=Error(code);e.status=status;e.code=code;throw e;};
    if(!MODES.has(mode))fail(400,'invalid_wifi_mode');
    if(!this.allowWifiControl)fail(403,'pi_control_disabled');
    if(this.closed)fail(503,'pi_control_unavailable');
    if(this.controlState==='pending')fail(409,'pi_control_busy');
    if(this.monotonic()-this.lastAction<30000)fail(429,'pi_control_rate_limited');
    this.controlState='pending';
    try {
      const helper=await this.stat(HELPER);
      if(!helper.isFile()||helper.uid!==0||(helper.mode&0o022))throw Error();
      const allowed=await this.run('/usr/bin/sudo',['-n','-l','--',HELPER,mode]);
      if(!allowed.ok)throw Error();
    } catch {this.controlState='unavailable';fail(403,'pi_control_setup_required');}
    if(this.closed){this.controlState='unavailable';fail(503,'pi_control_unavailable');}
    this.lastAction=this.monotonic();
    // Return 202 before a mode change can remove this client's upstream route.
    // This acknowledges queueing only; actual mode is re-read on later status.
    this.actionTimer=setTimeout(async()=> {
      this.actionTimer=null;
      try{const result=await this.run('/usr/bin/sudo',['-n','--',HELPER,mode],40000);this.controlState=result.ok?'succeeded':'failed';}
      catch{this.controlState='failed';}
      this.cacheAt=-Infinity;
    },300);
    this.actionTimer.unref?.();
    return {requestedMode:mode,accepted:true,confirmed:false};
  }
  close(){this.closed=true;if(this.actionTimer)clearTimeout(this.actionTimer);this.actionTimer=null;}
}
module.exports={PiStatus,memory,counters,cpuPercent,defaultInterface};
