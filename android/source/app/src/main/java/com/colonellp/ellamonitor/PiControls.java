package com.colonellp.ellamonitor;

import android.Manifest;
import android.app.Dialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.LocationManager;
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

/** Pi reads/actions use the API; interactive SSH belongs to ConnectBot. */
final class PiControls {
    private final MainActivity a;private final PrivateSettings settings;
    private final Handler ui=new Handler(Looper.getMainLooper());
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private volatile ApiClient client;private volatile int generation;private volatile boolean active;
    private boolean busy,visible,permissionRequest,locationReturn;
    private String networkKey="",ssid="",deviceIp="—",error="Set the Pi API address/token in Settings.",pendingSsid="";
    private volatile String selectedBase="";
    private long lastAttempt,pendingUntil;private Network network;private PiStats stats;
    private TextView connectionReadout,resourceReadout;private Button[] networks,modes;
    private final Set<Dialog> editors=new HashSet<>();
    private final Map<Network,WifiInfo> wifiInfo=new HashMap<>();
    private ConnectivityManager.NetworkCallback networkCallback;
    final LocalNameResolver resolver;
    private final Runnable tick=this::tick;
    PiControls(MainActivity a,PrivateSettings settings){this.a=a;this.settings=settings;resolver=new LocalNameResolver(a);}
    void start(){active=true;networkKey="";lastAttempt=0;registerNetworkInfo();inspectNetwork();ui.removeCallbacks(tick);ui.post(tick);}
    void stop(){active=false;invalidate();ui.removeCallbacks(tick);unregisterNetworkInfo();for(Dialog d:new ArrayList<>(editors))d.dismiss();editors.clear();}
    void destroy(){stop();worker.shutdownNow();}
    Network network(){return network;}
    String apiBase(){return selectedBase.isEmpty()?settings.prefs.getString("address",""):selectedBase;}
    void useEndpoint(String host){selectedBase=PiNetworkConfig.replaceApiHost(settings.prefs.getString("address",""),host);}
    void settingsVisible(boolean value){visible=value;if(!value){connectionReadout=null;resourceReadout=null;networks=null;modes=null;}render();}
    private boolean permitted(){return Build.VERSION.SDK_INT<37||a.checkSelfPermission("android.permission.ACCESS_LOCAL_NETWORK")==PackageManager.PERMISSION_GRANTED;}
    private JSONArray configuration(){try{return settings.piNetworks();}catch(Exception e){return new JSONArray();}}
    private JSONObject entry(int i){JSONObject o=configuration().optJSONObject(i);return o==null?new JSONObject():o;}
    private int role(){for(int i=0;i<3;i++)if(!ssid.isEmpty()&&ssid.equals(entry(i).optString("ssid")))return i;return -1;}
    private String endpoint(){int role=role();if(role>=0&&!entry(role).optString("host").isEmpty())return entry(role).optString("host");try{return new URI(settings.prefs.getString("address","")).getHost();}catch(Exception e){return null;}}
    private boolean configured(){return !settings.prefs.getString("address","").isEmpty()&&settings.prefs.contains("token");}
    void connectionEdited(){networkKey="";inspectNetwork();}
    void permissionsChanged(){if(!active)return;unregisterNetworkInfo();registerNetworkInfo();networkKey="";lastAttempt=0;ui.removeCallbacks(tick);ui.post(tick);if(permissionRequest){permissionRequest=false;if(requiredPermissions().isEmpty())wifiAccess();else a.toast("Wi-Fi identification permission was not granted.");}}
    void resumed(){if(!active)return;if(locationReturn){locationReturn=false;if(locationEnabled())openWifi();}inspectNetwork();}
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
                if(caps!=null&&caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)&&!caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)){
                    found=n;WifiInfo info=wifiInfo.get(n);
                    if(info==null&&Build.VERSION.SDK_INT>=31&&caps.getTransportInfo() instanceof WifiInfo)info=(WifiInfo)caps.getTransportInfo();
                    if(info==null||info.getSSID()==null||WifiManager.UNKNOWN_SSID.equals(info.getSSID())){WifiManager wm=(WifiManager)a.getApplicationContext().getSystemService(Context.WIFI_SERVICE);if(wm!=null)info=wm.getConnectionInfo();}
                    if(info!=null){name=info.getSSID();if(name==null||name.equals(WifiManager.UNKNOWN_SSID))name="";else if(name.startsWith("\"")&&name.endsWith("\""))name=name.substring(1,name.length()-1);}
                    LinkProperties link=cm.getLinkProperties(n);if(link!=null)for(LinkAddress address:link.getLinkAddresses())if(address.getAddress() instanceof Inet4Address){ip=address.getAddress().getHostAddress();break;}
                    break;
                }
            }
            if(found==null&&cm!=null)for(Network n:cm.getAllNetworks()){NetworkCapabilities caps=cm.getNetworkCapabilities(n);if(caps!=null&&caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)&&!caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)){found=n;LinkProperties link=cm.getLinkProperties(n);if(link!=null)for(LinkAddress address:link.getLinkAddresses())if(address.getAddress() instanceof Inet4Address){ip=address.getAddress().getHostAddress();break;}break;}}
        }catch(SecurityException ignored){name="";}
        String key=(found==null?"none":Long.toString(found.getNetworkHandle()))+"/"+name+"/"+settings.prefs.getString("pi.networks","")+"/"+settings.prefs.getString("address","");
        network=found;ssid=name;deviceIp=ip;
        if(!key.equals(networkKey)){
            networkKey=key;invalidate();error=found==null?"Join a configured Wi-Fi network.":configured()?"Reading Pi API status…":"Set the Pi API address/token in Settings.";
            a.pauseForPiNetwork();
            String target=endpoint();if(found!=null&&configured()&&target!=null)try{a.selectPiEndpoint(target);}catch(IllegalArgumentException e){error=e.getMessage();}
        }
        if(!pendingSsid.isEmpty()){if(ssid.equals(pendingSsid)){pendingSsid="";lastAttempt=0;}else if(SystemClock.elapsedRealtime()>pendingUntil){pendingSsid="";error="Requested Wi-Fi network was not connected. Open Wi-Fi settings and retry.";}}
    }
    private void invalidate(){generation++;busy=false;lastAttempt=0;stats=null;selectedBase="";resolver.reset();ApiClient c=client;if(c!=null)c.cancel();client=null;}
    private void tick(){
        if(!active)return;inspectNetwork();render();
        if(!busy&&network!=null&&configured()&&permitted()&&(lastAttempt==0||SystemClock.elapsedRealtime()-lastAttempt>=(visible?15000:30000)))request("status",false);
        ui.postDelayed(tick,2000);
    }
    String statusBar(){return stats!=null&&SystemClock.elapsedRealtime()-stats.at<45000?"Pi Wi-Fi: "+stats.get("ssid"):"Pi Wi-Fi: —";}
    void build(LinearLayout parent){
        visible=true;LinearLayout controls=a.settingsGroup(parent,"NETWORK & PI");LinearLayout row=a.row();networks=new Button[3];
        for(int i=0;i<3;i++){final int index=i;String name=entry(i).optString("ssid");Button b=a.uiButton(name.isEmpty()?PiNetworkConfig.ROLES[i]:name,false,()->connectAndroid(index));b.setOnLongClickListener(v->{editNetwork(index);return true;});a.trackChange(b,"connection.network."+i);networks[i]=b;add(row,b,i==0?0:6,38);}controls.addView(row);
        LinearLayout policy=a.row();modes=new Button[3];String[] names={"Pi Auto","Pi Home","Pi M1"},actions={"auto","home","m1"};
        for(int i=0;i<3;i++){final String action=actions[i];modes[i]=a.uiButton(names[i],false,()->a.themedAlertBuilder().setMessage("Set the Pi upstream to "+action+"? This changes the Pi, not this device's Wi-Fi.").setPositiveButton("Apply",(d,w)->request(action,true)).setNegativeButton("Cancel",null).show());add(policy,modes[i],i==0?0:6,35);}
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,6,0,0);controls.addView(policy,p);
        LinearLayout actionsRow=a.row();add(actionsRow,a.uiButton("Refresh",false,()->request("status",true)),0,35);Button terminal=a.uiButton("Open terminal",false,this::openTerminal);terminal.setOnLongClickListener(v->{editSsh();return true;});add(actionsRow,terminal,6,35);add(actionsRow,a.uiButton("Wi-Fi access",false,this::wifiAccess),6,35);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,-2);ap.setMargins(0,6,0,0);controls.addView(actionsRow,ap);
        LinearLayout connection=a.settingsGroup(parent,"PI CONNECTION");connectionReadout=a.uiText("",12,a.getThemeConfig().text);connection.addView(connectionReadout);
        LinearLayout resources=a.settingsGroup(parent,"PI DATA");resourceReadout=a.uiText("",12,a.getThemeConfig().text);resources.addView(resourceReadout);render();
    }
    private void add(LinearLayout row,View child,int gap,int height){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,height,1);p.setMargins(gap,0,0,0);row.addView(child,p);}
    private void render(){
        boolean fresh=stats!=null&&SystemClock.elapsedRealtime()-stats.at<45000;
        if(connectionReadout!=null)connectionReadout.setText("Device Wi-Fi: "+(ssid.isEmpty()?"— (Wi-Fi access may be needed)":ssid)+"\nDevice IP: "+deviceIp+"\nPi endpoint: "+(endpoint()==null?"—":endpoint())+"\n"+(fresh?"Pi Wi-Fi: "+stats.get("ssid")+"\nPi profile: "+stats.get("profile")+"\nPi IP: "+stats.get("ip")+"\nPi mode: "+stats.get("mode")+"\nMode controls: "+(stats.controlEnabled()?stats.get("controlState"):"enable API controls on Pi"):error));
        if(resourceReadout!=null)resourceReadout.setText((fresh?"RAM: "+stats.memory()+"\nCPU: "+stats.cpu()+"   Load: "+stats.load()+"\nTemperature: "+stats.temperature():"RAM: —\nCPU: —   Load: —\nTemperature: —")+"\n"+a.collectorReadout());
        if(networks!=null)for(int i=0;i<3;i++){String name=entry(i).optString("ssid");networks[i].setText(name.isEmpty()?PiNetworkConfig.ROLES[i]:name);style(networks[i],!ssid.isEmpty()&&ssid.equals(name));if(a.piNetworkModified(i))networks[i].setTextColor(a.getThemeConfig().changedIndicator);}
        if(modes!=null)for(int i=0;i<3;i++){String mode=new String[]{"auto","home","m1"}[i];style(modes[i],fresh&&mode.equals(stats.get("mode")));modes[i].setEnabled(!busy&&fresh&&stats.controlEnabled()&&!stats.get("controlState").equals("pending"));}
    }
    private void style(Button b,boolean selected){ThemeConfig t=a.getThemeConfig();b.setActivated(selected);b.setTextColor(t.buttonTextOn);b.setBackground(a.uiBackground(t.buttonBackground,selected?t.controls:t.offButtonBorder));}
    private void request(String action,boolean explicit){
        if(!active||busy)return;if(!permitted()){a.requestPermissions(new String[]{"android.permission.ACCESS_LOCAL_NETWORK"},37);return;}
        if(!configured()||network==null){if(explicit)a.toast("Save the Pi API address/token and join its network first.");return;}
        final int version=generation;final Network route=network;final String base=apiBase();final String host=endpoint();
        busy=true;lastAttempt=SystemClock.elapsedRealtime();render();
        worker.execute(()->{
            ApiClient c=null;
            try{
                if(!active||version!=generation)return;
                c=new ApiClient(base,settings.token(),new NetworkTransport(route,resolver));client=c;
                if(!active||version!=generation){c.cancel();return;}
                JSONObject response=action.equals("status")?c.get("/api/v1/pi"):c.mode(action);
                final PiStats result=action.equals("status")?new PiStats(response,host,SystemClock.elapsedRealtime()):null;
                ui.post(()->{if(!active||version!=generation)return;busy=false;client=null;
                    if(result!=null){stats=result;error="";}else{stats=null;error="Pi mode request accepted; checking its resulting connection…";lastAttempt=0;}
                    render();a.refreshPiStatusBar();});
            }catch(Exception e){final String message=e instanceof ApiClient.Failure?e.getMessage():action.equals("status")?"Pi status unavailable. Check the API address/token or join the monitoring hotspot.":"Pi mode request not confirmed. Refresh or join the monitoring hotspot.";
                ui.post(()->{if(!active||version!=generation)return;busy=false;client=null;stats=null;error=message;if(explicit)a.toast(message);render();a.refreshPiStatusBar();});
            }finally{if(c!=null)c.cancel();}
        });
    }
    private EditText editor(String value,String hint,boolean secret){EditText e=new EditText(a);e.setSingleLine();e.setText(value);e.setHint(hint);e.setSaveEnabled(!secret);e.setInputType(InputType.TYPE_CLASS_TEXT|(secret?InputType.TYPE_TEXT_VARIATION_PASSWORD:InputType.TYPE_TEXT_VARIATION_NORMAL));a.styleEditor(e);e.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,a.dialogTextSize(13));e.setMinHeight(0);e.setMinimumHeight(0);e.setIncludeFontPadding(false);e.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);e.setPadding(a.dialogDp(6),0,a.dialogDp(6),0);return e;}
    private Dialog remember(Dialog d){editors.add(d);d.setOnDismissListener(v->editors.remove(d));return d;}
    private void editNetwork(int index){
        Dialog d=remember(new Dialog(a));LinearLayout panel=a.styledDialogRoot();panel.addView(a.styledTitle(PiNetworkConfig.ROLES[index]+" connection"));JSONObject original=entry(index);
        EditText name=editor(original.optString("ssid"),"Exact Wi-Fi network name",false),host=editor(original.optString("host"),"Pi IP address / hostname",false);panel.addView(name,new LinearLayout.LayoutParams(-1,a.dialogDp(38)));panel.addView(host,new LinearLayout.LayoutParams(-1,a.dialogDp(38)));
        LinearLayout row=a.row();TextView cancel=a.dialogButton("CANCEL",a.getThemeConfig().border),save=a.dialogButton("SAVE",a.getThemeConfig().controls);add(row,cancel,0,a.dialogDp(35));add(row,save,a.dialogDp(6),a.dialogDp(35));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,a.dialogDp(6),0,0);panel.addView(row,p);
        cancel.setOnClickListener(v->d.dismiss());save.setOnClickListener(v->{try{JSONArray entries=settings.piNetworks();entries.put(index,new JSONObject().put("ssid",PiNetworkConfig.ssid(name.getText().toString())).put("host",PiNetworkConfig.host(host.getText().toString())));settings.savePiNetworks(entries);networkKey="";invalidate();a.refreshPiPresetChanges();d.dismiss();render();}catch(Exception e){host.setError(e.getMessage());}});a.showStyledDialog(d,panel,460);
    }
    @SuppressWarnings("deprecation") private void connectAndroid(int index){
        JSONObject saved=entry(index);String wanted=saved.optString("ssid");if(wanted.isEmpty()||saved.optString("host").isEmpty()){editNetwork(index);return;}
        if(wanted.equals(ssid)){request("status",true);return;}
        a.themedAlertBuilder().setMessage("Connect this device to \""+wanted+"\"?").setPositiveButton("Connect",(d,w)->{
            pendingSsid=wanted;pendingUntil=SystemClock.elapsedRealtime()+45000;
            if(Build.VERSION.SDK_INT<29)try{WifiManager wm=(WifiManager)a.getApplicationContext().getSystemService(Context.WIFI_SERVICE);List<WifiConfiguration> savedNetworks=wm==null?null:wm.getConfiguredNetworks();if(savedNetworks!=null)for(WifiConfiguration c:savedNetworks)if(("\""+wanted+"\"").equals(c.SSID)&&wm.enableNetwork(c.networkId,true))return;}catch(SecurityException ignored){}
            openWifi();
        }).setNegativeButton("Cancel",null).show();
    }
    private void openWifi(){try{a.startActivity(new Intent(Build.VERSION.SDK_INT>=29?Settings.Panel.ACTION_WIFI:Settings.ACTION_WIFI_SETTINGS));}catch(RuntimeException e){try{a.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));}catch(RuntimeException ignored){a.toast("Open Android Wi-Fi settings to choose the network.");}}}
    private ArrayList<String> requiredPermissions(){
        ArrayList<String> list=new ArrayList<>();for(String p:new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION})if(a.checkSelfPermission(p)!=PackageManager.PERMISSION_GRANTED)list.add(p);
        if(Build.VERSION.SDK_INT>=33&&a.checkSelfPermission("android.permission.NEARBY_WIFI_DEVICES")!=PackageManager.PERMISSION_GRANTED)list.add("android.permission.NEARBY_WIFI_DEVICES");return list;
    }
    @SuppressWarnings("deprecation") private boolean locationEnabled(){try{LocationManager lm=(LocationManager)a.getSystemService(Context.LOCATION_SERVICE);return lm!=null&&(Build.VERSION.SDK_INT>=28?lm.isLocationEnabled():lm.isProviderEnabled(LocationManager.GPS_PROVIDER)||lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));}catch(Exception e){return false;}}
    private void wifiAccess(){
        ArrayList<String> permissions=requiredPermissions();if(!permissions.isEmpty()){permissionRequest=true;a.requestPermissions(permissions.toArray(new String[0]),39);return;}
        if(!locationEnabled()){
            a.themedAlertBuilder().setMessage("Android may require Location to identify the connected Wi-Fi network. Enable it to show network names, or open Wi-Fi settings directly.")
                .setPositiveButton("Location",(d,w)->{try{locationReturn=true;a.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));}catch(RuntimeException e){locationReturn=false;openWifi();}})
                .setNeutralButton("Wi-Fi settings",(d,w)->openWifi()).setNegativeButton("Cancel",null).show();return;
        }
        openWifi();
    }
    private void openTerminal(){
        final String user=settings.prefs.getString("pi.ssh.user","");if(user.isEmpty()){editSsh();return;}
        final Network route=network;final String host=endpoint();final int version=generation,port=settings.prefs.getInt("pi.ssh.port",22);
        if(route==null||host==null){a.toast("Join/configure the Pi network first.");return;}
        worker.execute(()->{try{
            List<InetAddress> addresses=resolver.lookup(route,host);String target=addresses.get(0).getHostAddress();
            String uri=new URI("ssh",user,target,port,"/",null,"Ella Pi").toASCIIString();
            ui.post(()->{if(!active||version!=generation)return;try{a.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(uri)).setPackage("org.connectbot"));}catch(ActivityNotFoundException e){a.themedAlertBuilder().setMessage("Install ConnectBot to open the Pi terminal. SSH passwords and keys are managed inside ConnectBot.").setPositiveButton("Download",(d,w)->{try{a.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://connectbot.org/download/")));}catch(RuntimeException ignored){a.toast("Download ConnectBot from connectbot.org.");}}).setNegativeButton("Close",null).show();}});
        }catch(Exception e){ui.post(()->{if(active&&version==generation)a.toast("Pi hostname unavailable on this network. Check the configured address.");});}});
    }
    private void editSsh(){
        Dialog d=remember(new Dialog(a));LinearLayout panel=a.styledDialogRoot();panel.addView(a.styledTitle("ConnectBot connection"));
        EditText user=editor(settings.prefs.getString("pi.ssh.user",""),"SSH username",false),port=editor(Integer.toString(settings.prefs.getInt("pi.ssh.port",22)),"SSH port",false);port.setInputType(InputType.TYPE_CLASS_NUMBER);
        panel.addView(user,new LinearLayout.LayoutParams(-1,a.dialogDp(38)));LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,a.dialogDp(38));pp.setMargins(0,a.dialogDp(6),0,0);panel.addView(port,pp);
        TextView help=a.uiText("Passwords, keys and host verification are managed in ConnectBot.",a.dialogTextSize(12),a.getThemeConfig().text);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.setMargins(0,a.dialogDp(6),0,0);panel.addView(help,hp);
        LinearLayout row=a.row();TextView cancel=a.dialogButton("CANCEL",a.getThemeConfig().border),save=a.dialogButton("SAVE",a.getThemeConfig().controls);add(row,cancel,0,a.dialogDp(35));add(row,save,a.dialogDp(6),a.dialogDp(35));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.setMargins(0,a.dialogDp(6),0,0);panel.addView(row,rp);
        cancel.setOnClickListener(v->d.dismiss());save.setOnClickListener(v->{try{String name=user.getText().toString().trim();int number=Integer.parseInt(port.getText().toString());if(!name.matches("[a-zA-Z_][a-zA-Z0-9_.-]{0,63}")||number<1||number>65535)throw new IllegalArgumentException();settings.prefs.edit().putString("pi.ssh.user",name).putInt("pi.ssh.port",number).apply();d.dismiss();openTerminal();}catch(Exception e){user.setError("Enter a valid SSH username and port.");}});a.showStyledDialog(d,panel,460);
    }
}
