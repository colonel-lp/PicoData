package com.colonellp.ellamonitor;

import android.Manifest;
import android.app.Dialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

/** Foreground Pi operations and Android network selection are deliberately separate. */
final class PiControls {
    private final MainActivity a;private final PrivateSettings settings;
    private final Handler ui=new Handler(Looper.getMainLooper());
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private volatile PiSshClient client;private volatile int generation;private volatile boolean active;private boolean busy,visible,awaitingEndpoint;
    private String networkKey="",ssid="",deviceIp="—",error="Set up SSH to read Pi status.",pendingSsid="";
    private volatile String verifiedBase="";
    private long lastAttempt,pendingUntil;private Network network;private PiStats stats,previous;
    private TextView connectionReadout,resourceReadout;private Button[] networks,modes;
    private final Set<Dialog> editors=new HashSet<>();
    private final Map<Network,WifiInfo> wifiInfo=new HashMap<>();
    private ConnectivityManager.NetworkCallback networkCallback;
    private final Runnable tick=this::tick;
    PiControls(MainActivity a,PrivateSettings settings){this.a=a;this.settings=settings;}
    void start(){active=true;networkKey="";lastAttempt=0;registerNetworkInfo();inspectNetwork();ui.removeCallbacks(tick);ui.post(tick);}
    void stop(){active=false;generation++;busy=false;ui.removeCallbacks(tick);unregisterNetworkInfo();PiSshClient c=client;if(c!=null)c.cancel();client=null;stats=null;previous=null;for(Dialog d:new ArrayList<>(editors))d.dismiss();editors.clear();}
    void destroy(){stop();worker.shutdownNow();}
    Network network(){return network;}
    boolean awaitingIdentity(){return configured()&&awaitingEndpoint;}
    String apiBase(){return verifiedBase.isEmpty()?settings.prefs.getString("address",""):verifiedBase;}
    void useEndpoint(String host){verifiedBase=PiNetworkConfig.replaceApiHost(settings.prefs.getString("address",""),host);}
    void settingsVisible(boolean value){visible=value;if(!value){connectionReadout=null;resourceReadout=null;networks=null;modes=null;}render();}
    private boolean permitted(){return Build.VERSION.SDK_INT<37||a.checkSelfPermission("android.permission.ACCESS_LOCAL_NETWORK")==PackageManager.PERMISSION_GRANTED;}
    private JSONArray configuration(){try{return settings.piNetworks();}catch(Exception e){return new JSONArray();}}
    private JSONObject entry(int i){JSONObject o=configuration().optJSONObject(i);return o==null?new JSONObject():o;}
    private int role(){for(int i=0;i<3;i++)if(!ssid.isEmpty()&&ssid.equals(entry(i).optString("ssid")))return i;return -1;}
    private String endpoint(){
        int role=role();if(role>=0&&!entry(role).optString("host").isEmpty())return entry(role).optString("host");
        try{return new URI(settings.prefs.getString("address","")).getHost();}catch(Exception e){return null;}
    }
    private boolean configured(){return !settings.prefs.getString("pi.ssh.user","").isEmpty();}
    void permissionsChanged(){if(!active)return;unregisterNetworkInfo();registerNetworkInfo();networkKey="";lastAttempt=0;ui.removeCallbacks(tick);ui.post(tick);}
    private void registerNetworkInfo(){
        if(Build.VERSION.SDK_INT<31||networkCallback!=null)return;
        ConnectivityManager cm=(ConnectivityManager)a.getSystemService(Context.CONNECTIVITY_SERVICE);if(cm==null)return;
        networkCallback=new ConnectivityManager.NetworkCallback(ConnectivityManager.NetworkCallback.FLAG_INCLUDE_LOCATION_INFO){
            @Override public void onCapabilitiesChanged(Network n,NetworkCapabilities c){if(active&&c.getTransportInfo() instanceof WifiInfo)wifiInfo.put(n,(WifiInfo)c.getTransportInfo());}
            @Override public void onLost(Network n){wifiInfo.remove(n);}
        };
        try{cm.registerNetworkCallback(new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build(),networkCallback,ui);}catch(SecurityException e){networkCallback=null;}
    }
    private void unregisterNetworkInfo(){ConnectivityManager cm=(ConnectivityManager)a.getSystemService(Context.CONNECTIVITY_SERVICE);if(cm!=null&&networkCallback!=null)try{cm.unregisterNetworkCallback(networkCallback);}catch(IllegalArgumentException ignored){}networkCallback=null;wifiInfo.clear();}
    @SuppressWarnings("deprecation") private void inspectNetwork(){
        Network found=null;String name="",ip="—";
        ConnectivityManager cm=(ConnectivityManager)a.getSystemService(Context.CONNECTIVITY_SERVICE);
        try{
            if(cm!=null)for(Network n:cm.getAllNetworks()){
                NetworkCapabilities caps=cm.getNetworkCapabilities(n);
                if(caps!=null&&caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                    found=n;WifiInfo info=wifiInfo.get(n);
                    if(info==null&&Build.VERSION.SDK_INT>=31&&caps.getTransportInfo() instanceof WifiInfo)info=(WifiInfo)caps.getTransportInfo();
                    if(info==null||info.getSSID()==null||WifiManager.UNKNOWN_SSID.equals(info.getSSID())){WifiManager wm=(WifiManager)a.getApplicationContext().getSystemService(Context.WIFI_SERVICE);if(wm!=null)info=wm.getConnectionInfo();}
                    if(info!=null){name=info.getSSID();if(name==null||name.equals(WifiManager.UNKNOWN_SSID))name="";else if(name.startsWith("\"")&&name.endsWith("\""))name=name.substring(1,name.length()-1);}
                    LinkProperties link=cm.getLinkProperties(n);if(link!=null)for(LinkAddress address:link.getLinkAddresses())if(address.getAddress() instanceof Inet4Address){ip=address.getAddress().getHostAddress();break;}
                    break;
                }
            }
            if(found==null&&cm!=null)for(Network n:cm.getAllNetworks()){NetworkCapabilities caps=cm.getNetworkCapabilities(n);if(caps!=null&&caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){found=n;LinkProperties link=cm.getLinkProperties(n);if(link!=null)for(LinkAddress address:link.getLinkAddresses())if(address.getAddress() instanceof Inet4Address){ip=address.getAddress().getHostAddress();break;}break;}}
        }catch(SecurityException ignored){name="";}
        String key=(found==null?"none":Long.toString(found.getNetworkHandle()))+"/"+name+"/"+settings.prefs.getString("pi.networks","")+"/"+settings.prefs.getString("address","");
        network=found;ssid=name;deviceIp=ip;
        if(!key.equals(networkKey)){
            networkKey=key;invalidate();error=found==null?"Join a configured Wi-Fi network.":configured()?"Checking Pi identity…":"Set up SSH to read Pi status.";
            // Pause old-endpoint HTTP work before changing routes. A verified SSH probe
            // selects the new endpoint before the existing API token is sent there.
            if(configured()){awaitingEndpoint=true;a.pauseForPiNetwork();}
        }
        if(!pendingSsid.isEmpty()){
            if(ssid.equals(pendingSsid)){pendingSsid="";lastAttempt=0;}
            else if(SystemClock.elapsedRealtime()>pendingUntil){pendingSsid="";error="Requested Wi-Fi network was not connected. Open Wi-Fi settings and retry.";}
        }
    }
    private void invalidate(){generation++;busy=false;lastAttempt=0;stats=null;previous=null;verifiedBase="";PiSshClient c=client;if(c!=null)c.cancel();client=null;}
    private void tick(){
        if(!active)return;inspectNetwork();render();
        if(!busy&&network!=null&&configured()&&permitted()&&(lastAttempt==0||SystemClock.elapsedRealtime()-lastAttempt>=(visible?15000:30000)))request("status",false);
        ui.postDelayed(tick,2000);
    }
    String statusBar(){return stats!=null&&SystemClock.elapsedRealtime()-stats.at<45000?"Pi Wi-Fi: "+stats.get("ssid"):"Pi Wi-Fi: —";}
    void build(LinearLayout parent){
        visible=true;
        LinearLayout controls=a.settingsGroup(parent,"NETWORK & PI");
        LinearLayout row=a.row();networks=new Button[3];
        for(int i=0;i<3;i++){final int index=i;String name=entry(i).optString("ssid");Button b=a.uiButton(name.isEmpty()?PiNetworkConfig.ROLES[i]:name,false,()->connectAndroid(index));b.setOnLongClickListener(v->{editNetwork(index);return true;});a.trackChange(b,"connection.network."+i);networks[i]=b;add(row,b,i==0?0:6,38);}
        controls.addView(row);
        LinearLayout policy=a.row();modes=new Button[3];String[] names={"Pi Auto","Pi Home","Pi M1"},actions={"auto","home","m1"};
        for(int i=0;i<3;i++){final String action=actions[i];modes[i]=a.uiButton(names[i],false,()->a.themedAlertBuilder().setMessage("Set the Pi upstream to "+action+"? This changes the Pi, not this device's Wi-Fi.").setPositiveButton("Apply",(d,w)->request(action,true)).setNegativeButton("Cancel",null).show());add(policy,modes[i],i==0?0:6,35);}
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,6,0,0);controls.addView(policy,p);
        LinearLayout actionsRow=a.row();add(actionsRow,a.uiButton("Refresh",false,()->request("status",true)),0,35);add(actionsRow,a.uiButton("SSH setup",false,this::editSsh),6,35);add(actionsRow,a.uiButton("Wi-Fi access",false,this::wifiAccess),6,35);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,-2);ap.setMargins(0,6,0,0);controls.addView(actionsRow,ap);
        LinearLayout connection=a.settingsGroup(parent,"PI CONNECTION");connectionReadout=a.uiText("",12,a.getThemeConfig().text);connection.addView(connectionReadout);
        LinearLayout resources=a.settingsGroup(parent,"PI DATA");resourceReadout=a.uiText("",12,a.getThemeConfig().text);resources.addView(resourceReadout);render();
    }
    private void add(LinearLayout row,View child,int gap,int height){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,height,1);p.setMargins(gap,0,0,0);row.addView(child,p);}
    private void render(){
        boolean fresh=stats!=null&&SystemClock.elapsedRealtime()-stats.at<45000;
        if(connectionReadout!=null)connectionReadout.setText("Device Wi-Fi: "+(ssid.isEmpty()?"— (Wi-Fi access/location may be needed)":ssid)+"\nDevice IP: "+deviceIp+"\nPi endpoint: "+(endpoint()==null?"—":endpoint())+"\n"+(fresh?"Pi Wi-Fi: "+stats.get("ssid")+"\nPi profile: "+stats.get("profile")+"\nPi IP: "+stats.get("ip")+"\nPi mode: "+stats.get("mode"):error));
        if(resourceReadout!=null)resourceReadout.setText((fresh?"RAM: "+stats.memory()+"\nCPU: "+stats.cpu(previous)+"   Load: "+stats.get("load")+"\nTemperature: "+stats.temperature():"RAM: —\nCPU: —   Load: —\nTemperature: —")+"\n"+a.collectorReadout());
        if(networks!=null)for(int i=0;i<3;i++){String name=entry(i).optString("ssid");networks[i].setText(name.isEmpty()?PiNetworkConfig.ROLES[i]:name);style(networks[i],!ssid.isEmpty()&&ssid.equals(name));if(a.piNetworkModified(i))networks[i].setTextColor(a.getThemeConfig().changedIndicator);}
        if(modes!=null)for(int i=0;i<3;i++){String mode=new String[]{"auto","home","m1"}[i];style(modes[i],fresh&&mode.equals(stats.get("mode")));modes[i].setEnabled(!busy);}
    }
    private void style(Button b,boolean selected){ThemeConfig t=a.getThemeConfig();b.setActivated(selected);b.setTextColor(t.buttonTextOn);b.setBackground(a.uiBackground(t.buttonBackground,selected?t.controls:t.offButtonBorder));}
    private void request(String action,boolean explicit){
        if(!active||busy)return;
        if(!permitted()){a.requestPermissions(new String[]{"android.permission.ACCESS_LOCAL_NETWORK"},37);return;}
        if(!configured()){if(explicit)editSsh();return;}
        String target=endpoint();if(network==null||target==null||target.isEmpty()){error="Join/configure a network and Pi address first.";render();return;}
        final int version=generation;final Network route=network;final String expectedTarget=target;
        busy=true;lastAttempt=SystemClock.elapsedRealtime();render();
        worker.execute(()->{
            PiSshClient c=new PiSshClient((host,port)->{InetAddress[] addresses=route.getAllByName(host);if(addresses.length==0)throw new java.io.IOException("Pi address unavailable.");java.net.Socket s=route.getSocketFactory().createSocket();try{s.connect(new InetSocketAddress(addresses[0],port),5000);s.setSoTimeout(6000);return s;}catch(Exception e){s.close();throw e;}});
            if(!active||version!=generation)return;client=c;
            try{
                PiSshClient.Credentials auth=new PiSshClient.Credentials(settings.prefs.getString("pi.ssh.user",""),settings.prefs.getInt("pi.ssh.port",22),settings.secret("pi.ssh.password"),settings.prefs.getBoolean("pi.ssh.useKey",false)?settings.secret("pi.ssh.key"):"",settings.secret("pi.ssh.passphrase"),settings.prefs.getString("pi.ssh.fingerprint",""));
                String text=c.run(expectedTarget,auth,action);PiStats result=action.equals("status")?new PiStats(text,expectedTarget,SystemClock.elapsedRealtime()):null;
                ui.post(()->{if(!active||version!=generation)return;busy=false;client=null;
                    if(result!=null){previous=stats;stats=result;error="";if(awaitingEndpoint||!expectedTarget.equals(apiHost())){awaitingEndpoint=false;a.selectPiEndpoint(expectedTarget);}}
                    else {stats=null;error="Pi command sent; checking its resulting connection…";lastAttempt=0;awaitingEndpoint=true;}
                    render();a.refreshPiStatusBar();});
            }catch(Exception e){ui.post(()->{if(!active||version!=generation)return;busy=false;client=null;stats=null;previous=null;
                error=e instanceof PiSshClient.UntrustedKey?e.getMessage():action.equals("status")?"Pi unavailable. Check address/SSH credentials, or join the monitoring hotspot.":"Pi command not confirmed. The upstream may have changed; refresh or join the monitoring hotspot.";
                if(e instanceof PiSshClient.UntrustedKey&&explicit){PiSshClient.UntrustedKey k=(PiSshClient.UntrustedKey)e;if(!k.changed)verifyKey(k.fingerprint,version);else a.toast(error);}
                else if(explicit)a.toast(error);render();a.refreshPiStatusBar();});}finally{c.close();}
        });
    }
    private String apiHost(){try{return new URI(apiBase()).getHost();}catch(Exception e){return "";}}
    private void verifyKey(String fingerprint,int version){a.themedAlertBuilder().setTitle("Verify Pi SSH identity").setMessage("Compare this fingerprint with the Pi's ECDSA SSH host key before trusting it:\n\n"+fingerprint).setPositiveButton("Trust key",(d,w)->{if(version!=generation||!active)return;settings.prefs.edit().putString("pi.ssh.fingerprint",fingerprint).apply();lastAttempt=0;request("status",true);}).setNegativeButton("Cancel",null).show();}
    private EditText editor(String value,String hint,boolean secret){EditText e=new EditText(a);e.setSingleLine();e.setText(value);e.setHint(hint);e.setSaveEnabled(!secret);e.setInputType(InputType.TYPE_CLASS_TEXT|(secret?InputType.TYPE_TEXT_VARIATION_PASSWORD:InputType.TYPE_TEXT_VARIATION_NORMAL));a.styleEditor(e);e.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,a.dialogTextSize(13));e.setPadding(a.dialogDp(6),0,a.dialogDp(6),0);return e;}
    private Dialog remember(Dialog d){editors.add(d);d.setOnDismissListener(v->editors.remove(d));return d;}
    private void editNetwork(int index){
        Dialog d=remember(new Dialog(a));LinearLayout panel=a.styledDialogRoot();panel.addView(a.styledTitle(PiNetworkConfig.ROLES[index]+" connection"));JSONObject original=entry(index);
        EditText name=editor(original.optString("ssid"),"Exact Wi-Fi network name",false),host=editor(original.optString("host"),"Pi IP address / hostname",false);panel.addView(name,new LinearLayout.LayoutParams(-1,a.dialogDp(38)));panel.addView(host,new LinearLayout.LayoutParams(-1,a.dialogDp(38)));
        LinearLayout row=a.row();TextView cancel=a.dialogButton("CANCEL",a.getThemeConfig().border),save=a.dialogButton("SAVE",a.getThemeConfig().controls);add(row,cancel,0,a.dialogDp(35));add(row,save,a.dialogDp(6),a.dialogDp(35));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,a.dialogDp(6),0,0);panel.addView(row,p);
        cancel.setOnClickListener(v->d.dismiss());save.setOnClickListener(v->{try{JSONArray entries=settings.piNetworks();entries.put(index,new JSONObject().put("ssid",PiNetworkConfig.ssid(name.getText().toString())).put("host",PiNetworkConfig.host(host.getText().toString())));settings.savePiNetworks(entries);invalidate();awaitingEndpoint=true;a.refreshPiPresetChanges();d.dismiss();render();}catch(Exception e){host.setError(e.getMessage());}});a.showStyledDialog(d,panel,460);
    }
    @SuppressWarnings("deprecation") private void connectAndroid(int index){
        JSONObject saved=entry(index);String wanted=saved.optString("ssid");if(wanted.isEmpty()||saved.optString("host").isEmpty()){editNetwork(index);return;}
        if(wanted.equals(ssid)){awaitingEndpoint=true;request("status",true);return;}
        a.themedAlertBuilder().setMessage("Connect this device to \""+wanted+"\"?").setPositiveButton("Connect",(d,w)->{
            pendingSsid=wanted;pendingUntil=SystemClock.elapsedRealtime()+45000;
            if(Build.VERSION.SDK_INT<29)try{WifiManager wm=(WifiManager)a.getApplicationContext().getSystemService(Context.WIFI_SERVICE);List<WifiConfiguration> savedNetworks=wm==null?null:wm.getConfiguredNetworks();if(savedNetworks!=null)for(WifiConfiguration c:savedNetworks)if(("\""+wanted+"\"").equals(c.SSID)&&wm.enableNetwork(c.networkId,true))return;}catch(SecurityException ignored){}
            openWifi();
        }).setNegativeButton("Cancel",null).show();
    }
    private void openWifi(){try{a.startActivity(new Intent(Build.VERSION.SDK_INT>=29?Settings.Panel.ACTION_WIFI:Settings.ACTION_WIFI_SETTINGS));}catch(RuntimeException e){try{a.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));}catch(RuntimeException ignored){a.toast("Open Android Wi-Fi settings to choose the network.");}}}
    private void wifiAccess(){ArrayList<String> permissions=new ArrayList<>();for(String permission:new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION})if(a.checkSelfPermission(permission)!=PackageManager.PERMISSION_GRANTED)permissions.add(permission);if(Build.VERSION.SDK_INT>=33&&a.checkSelfPermission("android.permission.NEARBY_WIFI_DEVICES")!=PackageManager.PERMISSION_GRANTED)permissions.add("android.permission.NEARBY_WIFI_DEVICES");if(!permissions.isEmpty())a.requestPermissions(permissions.toArray(new String[0]),39);else try{a.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));}catch(RuntimeException e){openWifi();}}
    private void editSsh(){
        Dialog d=remember(new Dialog(a));if(d.getWindow()!=null)d.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);LinearLayout panel=a.styledDialogRoot();panel.addView(a.styledTitle("Pi SSH setup"));
        ScrollView scroll=new ScrollView(a);LinearLayout fields=a.column();scroll.addView(fields);panel.addView(scroll,new LinearLayout.LayoutParams(-1,a.dialogDp(260)));
        EditText user=editor(settings.prefs.getString("pi.ssh.user",""),"SSH username",false),port=editor(Integer.toString(settings.prefs.getInt("pi.ssh.port",22)),"SSH port",false),password=editor("","Password / sudo password (blank keeps saved)",true),key=editor("","Private key (blank keeps saved)",true),passphrase=editor("","Key passphrase (blank keeps saved)",true),fingerprint=editor(settings.prefs.getString("pi.ssh.fingerprint",""),"SHA256 host fingerprint (or verify on Refresh)",false);
        port.setInputType(InputType.TYPE_CLASS_NUMBER);key.setSingleLine(false);key.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        final boolean[] useKey={settings.prefs.getBoolean("pi.ssh.useKey",false)};Button method=a.uiButton(useKey[0]?"Authentication: private key":"Authentication: password",useKey[0],()->{});method.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,a.dialogTextSize(11.5f));method.setOnClickListener(v->{useKey[0]=!useKey[0];method.setText(useKey[0]?"Authentication: private key":"Authentication: password");style(method,useKey[0]);});
        for(View v:new View[]{user,port,password,method,key,passphrase,fingerprint}){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,a.dialogDp(v==key?70:38));p.setMargins(0,0,0,a.dialogDp(6));fields.addView(v,p);}
        final boolean[] clearAuth={false};Button clear=a.uiButton("Clear saved credentials",false,()->{});clear.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,a.dialogTextSize(11.5f));clear.setOnClickListener(v->{clearAuth[0]=true;password.setText("");key.setText("");passphrase.setText("");clear.setText("Credentials will be cleared on Save");});fields.addView(clear,new LinearLayout.LayoutParams(-1,a.dialogDp(35)));
        LinearLayout row=a.row();TextView cancel=a.dialogButton("CANCEL",a.getThemeConfig().border),save=a.dialogButton("SAVE",a.getThemeConfig().controls);add(row,cancel,0,a.dialogDp(35));add(row,save,a.dialogDp(6),a.dialogDp(35));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,a.dialogDp(6),0,0);panel.addView(row,p);
        cancel.setOnClickListener(v->d.dismiss());save.setOnClickListener(v->{try{
            String username=user.getText().toString().trim(),fp=fingerprint.getText().toString().trim();int number=Integer.parseInt(port.getText().toString());
            if(!username.matches("[a-zA-Z_][a-zA-Z0-9_.-]{0,63}")||number<1||number>65535)throw new IllegalArgumentException("Enter a valid SSH username and port.");
            if(!fp.isEmpty()&&!fp.matches("SHA256:[A-Za-z0-9+/]{43}"))throw new IllegalArgumentException("Enter a SHA256 SSH host fingerprint.");
            if(useKey[0]&&key.getText().toString().isEmpty()&&(clearAuth[0]||settings.secret("pi.ssh.key").isEmpty()))throw new IllegalArgumentException("Paste a supported private key.");
            android.content.SharedPreferences.Editor edit=settings.prefs.edit();
            if(clearAuth[0])for(String name:new String[]{"pi.ssh.password","pi.ssh.key","pi.ssh.passphrase"})edit.remove(name).remove(name+".iv");
            if(!password.getText().toString().isEmpty())settings.stageSecret(edit,"pi.ssh.password",password.getText().toString());if(!key.getText().toString().isEmpty())settings.stageSecret(edit,"pi.ssh.key",key.getText().toString());if(!passphrase.getText().toString().isEmpty())settings.stageSecret(edit,"pi.ssh.passphrase",passphrase.getText().toString());
            if(!edit.putString("pi.ssh.user",username).putInt("pi.ssh.port",number).putBoolean("pi.ssh.useKey",useKey[0]).putString("pi.ssh.fingerprint",fp).commit())throw new java.io.IOException("Could not save SSH settings.");
            password.setText("");key.setText("");passphrase.setText("");invalidate();awaitingEndpoint=true;d.dismiss();request("status",true);
        }catch(Exception e){user.setError(e instanceof IllegalArgumentException?e.getMessage():"Could not save SSH credentials.");}});a.showStyledDialog(d,panel,520);
    }
}
