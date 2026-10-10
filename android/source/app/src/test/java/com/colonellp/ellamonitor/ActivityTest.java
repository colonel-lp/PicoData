package com.colonellp.ellamonitor;

import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowAlertDialog;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {27, 35}, qualifiers = "w1024dp-h600dp-land-mdpi")
public class ActivityTest {
    @org.junit.Before public void clearSettings(){org.robolectric.RuntimeEnvironment.getApplication().getSharedPreferences("viewer",0).edit().clear().commit();}
    static View text(View view, String value) {
        if (view instanceof TextView && ((TextView)view).getText().toString().equals(value)) return view;
        if (view instanceof ViewGroup) for (int i=0;i<((ViewGroup)view).getChildCount();i++){View match=text(((ViewGroup)view).getChildAt(i),value);if(match!=null)return match;}
        return null;
    }
    static View description(View view,String value){if(value.equals(String.valueOf(view.getContentDescription())))return view;if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){View match=description(((ViewGroup)view).getChildAt(i),value);if(match!=null)return match;}return null;}
    static Object field(MainActivity a,String name)throws Exception{Field f=MainActivity.class.getDeclaredField(name);f.setAccessible(true);return f.get(a);}
    static void set(MainActivity a,String name,Object value)throws Exception{Field f=MainActivity.class.getDeclaredField(name);f.setAccessible(true);f.set(a,value);}
    static void call(MainActivity a,String name)throws Exception{Method m=MainActivity.class.getDeclaredMethod(name);m.setAccessible(true);m.invoke(a);}
    static void fixture(MainActivity a)throws Exception{set(a,"metrics",MonitorData.catalogue(ContractTest.catalogue()));set(a,"live",ContractTest.live());set(a,"receivedMono",android.os.SystemClock.elapsedRealtime());call(a,"updateLive");}
    @Test public void offlineNavigationOpensSettingsPageAndPreservesChartRanges() {
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();View root=a.getWindow().getDecorView();assertNotNull(text(root,"Live data"));assertNotNull(text(root,"Charts"));assertNotNull(description(root,"Keep screen on"));assertNotNull(text(root,"—"));
            description(root,"Settings").performClick();root=a.getWindow().getDecorView();assertNotNull(text(root,"CONNECTION"));assertNotNull(text(root,"DISPLAY & APP"));assertNull(ShadowAlertDialog.getLatestAlertDialog());
            text(root,"Charts").performClick();root=a.getWindow().getDecorView();assertNotNull(text(root,"Month"));assertNotNull(text(root,"Rolling"));text(root,"Month").performClick();
            text(a.getWindow().getDecorView(),"Live data").performClick();assertNotNull(text(a.getWindow().getDecorView(),"Currents:"));
        }
    }
    @Test public void syntheticDataFlagsLockAndExpiryWork()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();fixture(a);((org.json.JSONObject)field(a,"live")).getJSONObject("sbms").getJSONObject("reading").getJSONObject("flags").put("OVLK",true).put("EOC",true);call(a,"updateLive");LiveDashboard dashboard=(LiveDashboard)field(a,"liveDashboard");View gauge=null;int cells=0;
            for(int i=0;i<dashboard.getChildCount();i++)if(dashboard.getChildAt(i) instanceof LiveDashboard.Readout){LiveDashboard.Readout r=(LiveDashboard.Readout)dashboard.getChildAt(i);if(r.id.equals("flag:CFET")){assertEquals("●",r.value.getText().toString());assertEquals(0xff32dc68,r.value.getCurrentTextColor());}if(r.id.equals("flag:OVLK"))assertEquals(0xffff4545,r.value.getCurrentTextColor());if(r.id.equals("flag:EOC"))assertEquals(0xff32dc68,r.value.getCurrentTextColor());if(r.id.startsWith("cell:")&&r.getVisibility()==View.VISIBLE)cells++;if(r.fallback.equals("Battery [Pico]"))gauge=r;}
            assertEquals(4,cells);assertNotNull(gauge);description(a.getWindow().getDecorView(),"Lock gauge popups").performClick();dashboard=(LiveDashboard)field(a,"liveDashboard");for(int i=0;i<dashboard.getChildCount();i++)if(dashboard.getChildAt(i) instanceof LiveDashboard.Readout){LiveDashboard.Readout r=(LiveDashboard.Readout)dashboard.getChildAt(i);if(r.fallback.equals("Battery [Pico]"))r.performClick();}assertNull(ShadowAlertDialog.getLatestAlertDialog());
            c.pause().stop();assertNull(field(a,"live"));
        }
    }
    @Test @Config(qualifiers="w360dp-h760dp-port-mdpi") public void forcesLandscapeAndKeepsReferenceArrangement(){
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();assertEquals(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE,a.getRequestedOrientation());assertNotNull(text(a.getWindow().getDecorView(),"Currents:"));assertNotNull(text(a.getWindow().getDecorView(),"Temps:"));
        }
    }
    @Test public void themeColoursAndPresetSettingsSurviveRecreation()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();if(android.os.Build.VERSION.SDK_INT>=29)ThemeStorageTest.installProvider();PrivateSettings settings=(PrivateSettings)field(a,"settings");settings.prefs.edit().putString("address","https://synthetic.invalid").putString("label:synthetic","Custom label").apply();
            Method m=MainActivity.class.getDeclaredMethod("presetState");m.setAccessible(true);String json=m.invoke(a).toString();assertTrue(json.contains("synthetic.invalid"));assertFalse(json.contains("theme"));assertTrue(json.contains("Custom label"));
            ThemeConfig t=(ThemeConfig)field(a,"theme");t.background=Color.rgb(20,30,40);t.fontFamily="monospace";settings.prefs.edit().putString("fontFamily","monospace").apply();AppearanceStore store=new AppearanceStore(a,settings.prefs);store.save("Test theme",t);store.active(t);assertEquals(t.background,store.active().background);assertFalse(store.load("Test theme").toJson().has("fontFamily"));store.rename("Test theme","Renamed");assertNull(store.load("Test theme"));assertNotNull(store.load("Renamed"));c.recreate();assertEquals(t.background,((ThemeConfig)field(c.get(),"theme")).background);
        }
    }    @Test public void persistenceIsOptInAndHideStatusHidesStatus()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();description(a.getWindow().getDecorView(),"Settings").performClick();View root=a.getWindow().getDecorView();android.widget.CheckBox title=(android.widget.CheckBox)text(root,"Hide connection status");title.setChecked(true);assertEquals(View.GONE,((View)field(a,"status")).getVisibility());
            PrivateSettings settings=(PrivateSettings)field(a,"settings");assertFalse(settings.prefs.getBoolean("persistent",false));settings.prefs.edit().putBoolean("persistent",true).apply();
            org.robolectric.android.controller.ServiceController<PersistentService> service=Robolectric.buildService(PersistentService.class).create();try{
                service.get().onStartCommand(new android.content.Intent().putExtra("visible",false),0,1);android.app.NotificationManager manager=a.getSystemService(android.app.NotificationManager.class);assertEquals(1,manager.getActiveNotifications().length);assertTrue((manager.getActiveNotifications()[0].getNotification().flags&android.app.Notification.FLAG_ONGOING_EVENT)!=0);assertEquals("Ella Monitoring",manager.getActiveNotifications()[0].getNotification().extras.getString(android.app.Notification.EXTRA_TITLE));assertNull(manager.getActiveNotifications()[0].getNotification().extras.getString(android.app.Notification.EXTRA_TEXT));
                service.get().onStartCommand(new android.content.Intent().putExtra("visible",true),0,2);assertEquals(0,manager.getActiveNotifications().length);
                settings.prefs.edit().putBoolean("persistent",false).apply();assertEquals(android.app.Service.START_NOT_STICKY,service.get().onStartCommand(new android.content.Intent(),0,3));
            }finally{service.destroy();}
        }
    }
    @Test @Config(sdk=35) public void fullScreenReleasesSystemBarSpaceAndSettingsKeepsThemeSelector()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();assertNotNull(text(a.getWindow().getDecorView(),"Ella Monitoring"));
            android.view.WindowInsets insets=new android.view.WindowInsets.Builder().setInsets(android.view.WindowInsets.Type.systemBars(),android.graphics.Insets.of(0,24,0,48)).build();
            DesignViewport viewport=(DesignViewport)field(a,"viewport");viewport.dispatchApplyWindowInsets(insets);assertEquals(24,viewport.getPaddingTop());assertEquals(48,viewport.getPaddingBottom());
            description(a.getWindow().getDecorView(),"Full screen").performClick();viewport=(DesignViewport)field(a,"viewport");viewport.dispatchApplyWindowInsets(insets);assertEquals(0,viewport.getPaddingTop());assertEquals(0,viewport.getPaddingBottom());
            description(a.getWindow().getDecorView(),"Settings").performClick();assertNotNull(field(a,"themeAnchor"));assertEquals(View.GONE,((View)field(a,"status")).getVisibility());assertNotNull(text(a.getWindow().getDecorView(),"Changelog / Update"));text(a.getWindow().getDecorView(),"Back").performClick();assertNotNull(text(a.getWindow().getDecorView(),"Currents:"));
        }
    }

}

