package com.colonellp.ellamonitor;

import android.widget.*;
import android.view.*;
import java.lang.reflect.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(org.robolectric.RobolectricTestRunner.class)
@Config(sdk=27)
public class PiControlTest {
    @Before public void clear(){RuntimeEnvironment.getApplication().getSharedPreferences("viewer",0).edit().clear().commit();}
    @Test public void hostsAreStrictAndApiSchemeAndPortArePreserved()throws Exception{
        assertEquals("pi.example.invalid",PiNetworkConfig.host("pi.example.invalid"));
        assertEquals("::1",PiNetworkConfig.host("[::1]"));
        assertEquals("https://second.example.invalid:8443",PiNetworkConfig.replaceApiHost("https://first.example.invalid:8443","second.example.invalid"));
        for(String bad:new String[]{"http://pi.example.invalid","user@pi.example.invalid","pi.example.invalid/path","pi.example.invalid:22","pi.example.invalid?x","pi.\nexample.invalid"}){
            try{PiNetworkConfig.host(bad);fail(bad);}catch(IllegalArgumentException expected){}
        }
    }
    @Test public void sshOnlyAllowsTheFixedOperationsBeforeOpeningAnySocket()throws Exception{
        PiSshClient client=new PiSshClient((host,port)->{throw new AssertionError("Must not connect");});
        try{client.run("pi.example.invalid",new PiSshClient.Credentials("test-user",22,"","","",""),"reboot");fail();}catch(IllegalArgumentException expected){}
        assertTrue(PiSshClient.fingerprint(new byte[]{1,2,3}).matches("SHA256:[A-Za-z0-9+/]{43}"));
    }
    @Test public void networkEditsBelongToPresetsButSshAndRuntimeSelectionDoNot()throws Exception{
        PrivateSettings s=new PrivateSettings(RuntimeEnvironment.getApplication());JSONObject before=s.connectionSnapshot();JSONArray networks=PiNetworkConfig.empty();networks.getJSONObject(1).put("ssid","Test home").put("host","pi.example.invalid");s.savePiNetworks(networks);
        assertTrue(s.connectionDifferences(before).contains("connection.network.1"));JSONObject saved=s.connectionSnapshot();s.prefs.edit().putString("pi.ssh.user","test-user").putString("pi.ssh.fingerprint","test-pin").apply();assertTrue(s.connectionDifferences(saved).isEmpty());
        networks.getJSONObject(2).put("ssid","Test home");try{s.savePiNetworks(networks);fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void missingAndResetCpuCountersNeverBecomeFreshZero() {
        PiStats first=new PiStats("protocol\tella-pi-control-1\nboot\ttest-boot\ncpu\t10 0 10 80 0 0 0 0\nmemory\t2048 1024\n", "pi.example.invalid",0);
        PiStats next=new PiStats("protocol\tella-pi-control-1\nboot\ttest-boot\ncpu\t20 0 20 160 0 0 0 0\n", "pi.example.invalid",1);
        assertEquals("20%",next.cpu(first));assertEquals("—",next.cpu(null));assertEquals("—",next.temperature());assertEquals("—",next.memory());assertEquals("—",first.cpu(next));
        try{new PiStats("arbitrary response","pi.example.invalid",0);fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void settingsPanelsUseRequestedColumnsAndBottomGap()throws Exception{
        try(org.robolectric.android.controller.ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).create()){
            MainActivity a=c.get();Method method=MainActivity.class.getDeclaredMethod("showSettings");method.setAccessible(true);method.invoke(a);
            View root=a.getWindow().getDecorView();View network=text(root,"NETWORK & PI"),connection=text(root,"CONNECTION"),data=text(root,"PI DATA");assertNotNull(network);assertNotNull(connection);assertNotNull(data);
            View left=(View)network.getParent().getParent(),right=(View)connection.getParent().getParent();assertNotSame(left,right);assertSame(left,data.getParent().getParent());
            assertEquals(6,((LinearLayout.LayoutParams)left.getLayoutParams()).rightMargin);assertEquals(6,((View)left.getParent()).getPaddingBottom());
            assertNotNull(text(root,"Hotspot"));assertNotNull(text(root,"Home"));assertNotNull(text(root,"Mobile"));
            a.viewerPreferences().edit().putString("address","http://first.example.invalid:8080").apply();a.selectPiEndpoint("second.example.invalid");assertEquals("http://first.example.invalid:8080",a.viewerPreferences().getString("address",""));
        }
    }
    private static View text(View v,String value){if(v instanceof TextView&&value.contentEquals(((TextView)v).getText()))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View found=text(((ViewGroup)v).getChildAt(i),value);if(found!=null)return found;}return null;}
}
