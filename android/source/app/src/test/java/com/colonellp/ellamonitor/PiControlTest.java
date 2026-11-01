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
    @Test public void apiControlRejectsOtherOperationsBeforeOpeningAnyConnection()throws Exception{
        ApiClient client=new ApiClient("http://pi.example.invalid:8080",new String(new char[64]).replace('\0','a'),url->{throw new AssertionError("Must not connect");});
        try{client.mode("reboot");fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void selectedConnectionSnapshotKeepsItsOwnTokenAfterAnotherPresetIsLoaded()throws Exception{
        PrivateSettings s=new PrivateSettings(RuntimeEnvironment.getApplication(),()->new javax.crypto.spec.SecretKeySpec(new byte[32],"AES"));
        String first=ConnectionStabilityTest.repeat('a'),second=ConnectionStabilityTest.repeat('b');
        s.saveConnection("http://first.example.invalid:8080",first);JSONObject selected=s.connectionSnapshot();
        s.saveConnection("http://second.example.invalid:8080",second);
        assertEquals(first,s.token(selected));assertEquals(second,s.token());
        assertEquals("http://first.example.invalid:8080",selected.getString("address"));
        assertNotEquals(selected.toString(),s.connectionSnapshot().toString());
    }
    @Test public void networkEditsBelongToPresetsButSshAndRuntimeSelectionDoNot()throws Exception{
        PrivateSettings s=new PrivateSettings(RuntimeEnvironment.getApplication());JSONObject before=s.connectionSnapshot();JSONArray networks=PiNetworkConfig.empty();networks.getJSONObject(1).put("ssid","Test home").put("host","pi.example.invalid");s.savePiNetworks(networks);
        assertTrue(s.connectionDifferences(before).contains("connection.network.1"));JSONObject saved=s.connectionSnapshot();s.prefs.edit().putString("pi.ssh.user","test-user").putString("pi.ssh.fingerprint","test-pin").apply();assertTrue(s.connectionDifferences(saved).isEmpty());
        networks.getJSONObject(2).put("ssid","Test home");try{s.savePiNetworks(networks);fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void missingAndStaleApiResourcesNeverBecomeFreshZero()throws Exception {
        JSONObject pi=new JSONObject().put("ageSeconds",0).put("cpu",new JSONObject().put("percent",20));
        PiStats next=new PiStats(new JSONObject().put("apiVersion",1).put("pi",pi),"pi.example.invalid",1);
        assertEquals("20%",next.cpu());assertEquals("—",next.temperature());assertEquals("—",next.memory());
        pi.getJSONObject("cpu").put("percent",JSONObject.NULL);assertEquals("—",next.cpu());
        try{new PiStats(new JSONObject(),"pi.example.invalid",0);fail();}catch(IllegalArgumentException expected){}
        pi.put("ageSeconds",60);try{new PiStats(new JSONObject().put("apiVersion",1).put("pi",pi),"pi.example.invalid",0);fail();}catch(IllegalArgumentException expected){}
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
