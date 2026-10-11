package com.colonellp.ellamonitor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="w1024dp-h600dp-land-mdpi")
public class DisplayRevisionTest {
    @Before public void clear(){RuntimeEnvironment.getApplication().getSharedPreferences("viewer",0).edit().clear().commit();}
    static LiveDashboard.Readout readout(MainActivity a,String fallback)throws Exception{
        LiveDashboard d=(LiveDashboard)ActivityTest.field(a,"liveDashboard");
        for(int i=0;i<d.getChildCount();i++)if(d.getChildAt(i) instanceof LiveDashboard.Readout){LiveDashboard.Readout r=(LiveDashboard.Readout)d.getChildAt(i);if(r.fallback.equals(fallback))return r;}
        throw new AssertionError(fallback);
    }
    static int fill(View v){return ((GradientDrawable)v.getBackground().getCurrent()).getColor().getDefaultColor();}
    static AlertDialog popup(View v){v.performClick();return org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();}
    static CheckBox choice(AlertDialog d,String label){return (CheckBox)ActivityTest.text(d.getWindow().getDecorView(),label);}

    @Test public void wheelAndHexPreviewRepaintWithoutReplacingPageAndCancelRestores()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();RenderTest.renderFixture(a);CompanionLayoutTest.layout(a,1024,600);PresetAndColourTest.save(a,"Saved");
            Object viewport=ActivityTest.field(a,"viewport"),dashboard=ActivityTest.field(a,"liveDashboard");ThemeConfig theme=(ThemeConfig)ActivityTest.field(a,"theme");int before=theme.gaugePanel1;
            ActivityTest.call(a,"showThemeEditor");a.editThemeAppearanceColour(ThemeConfig.GAUGE_PANEL_1,"GAUGE PANEL 1");Dialog picker=PresetAndColourTest.picker();
            PresetAndColourTest.input(picker.getWindow().getDecorView()).setText("#123ABC");assertEquals(0xff123abc,fill(readout(a,"Battery [Pico]")));assertSame(viewport,ActivityTest.field(a,"viewport"));assertSame(dashboard,ActivityTest.field(a,"liveDashboard"));assertFalse(PresetAndColourTest.modified(a));
            ActivityTest.text(picker.getWindow().getDecorView(),"CANCEL").performClick();PresetAndColourTest.idle();PresetAndColourTest.assertEditor(a);assertEquals(before,fill(readout(a,"Battery [Pico]")));assertSame(viewport,ActivityTest.field(a,"viewport"));
            a.editThemeAppearanceColour(ThemeConfig.TITLE_BACKGROUND,"TITLE BACKGROUND");picker=PresetAndColourTest.picker();PresetAndColourTest.input(picker.getWindow().getDecorView()).setText("#234BCD");assertEquals(0xff234bcd,fill(ActivityTest.text(a.getWindow().getDecorView(),"Temps:")));ActivityTest.text(picker.getWindow().getDecorView(),"OK").performClick();PresetAndColourTest.idle();assertSame(viewport,ActivityTest.field(a,"viewport"));PresetAndColourTest.assertEditor(a);
        }
    }
    @Test public void allElementKindsHaveExclusiveHighlightsSwatchesAndHideContents()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();RenderTest.renderFixture(a);CompanionLayoutTest.layout(a,1024,600);ThemeConfig t=(ThemeConfig)ActivityTest.field(a,"theme");
            for(String name:new String[]{"Battery [Pico]","CFET","V [P]","Inverter","[1]"}){
                LiveDashboard.Readout r=readout(a,name);AlertDialog d=popup(r);CheckBox one=choice(d,"Highlight 1"),two=choice(d,"Highlight 2");assertNotNull(one);assertNotNull(two);assertNotNull(one.getCompoundDrawables()[2]);assertEquals(t.gaugeHighlight,((GradientDrawable)one.getCompoundDrawables()[2]).getColor().getDefaultColor());assertEquals(t.highlight2,((GradientDrawable)two.getCompoundDrawables()[2]).getColor().getDefaultColor());
                one.performClick();assertEquals(t.gaugeHighlight,fill(r));two.performClick();assertFalse(one.isChecked());assertTrue(two.isChecked());assertEquals(2,a.viewerPreferences().getInt("elementHighlight:"+r.id,0));assertEquals(t.highlight2,fill(r));
                int width=r.getWidth(),height=r.getHeight();choice(d,"Hide contents").performClick();assertEquals(View.INVISIBLE,r.name.getVisibility());if(r.gauge!=null)assertEquals(View.INVISIBLE,r.gauge.getVisibility());if(r.value!=null)assertEquals(View.INVISIBLE,r.value.getVisibility());assertEquals(width,r.getWidth());assertEquals(height,r.getHeight());d.dismiss();
                d=popup(r);assertTrue(choice(d,"Hide contents").isChecked());choice(d,"Hide contents").performClick();assertEquals(View.VISIBLE,r.name.getVisibility());d.dismiss();
            }
            View title=ActivityTest.text(a.getWindow().getDecorView(),"Currents:");AlertDialog d=popup(title);choice(d,"Highlight 2").performClick();assertEquals(t.highlight2,fill(title));choice(d,"Hide contents").performClick();assertEquals("",((TextView)title).getText().toString());d.dismiss();d=popup(title);assertTrue(choice(d,"Hide contents").isChecked());d.dismiss();
        }
    }
    @Test public void elementOptionsSurvivePresetAndLegacySelectionsMigrate()throws Exception{
        android.content.SharedPreferences prefs=RuntimeEnvironment.getApplication().getSharedPreferences("viewer",0);
        JSONObject legacy=PresetState.defaults().put("gaugeHighlight:test",true);prefs.edit().putInt("viewer.settingsFormat",2).putBoolean("gaugeHighlight:test",true).putString("viewer.preset:Old",legacy.toString()).commit();
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();PrivateSettings s=(PrivateSettings)ActivityTest.field(a,"settings");assertEquals(1,s.highlight("test"));assertFalse(prefs.contains("gaugeHighlight:test"));assertEquals(1,new JSONObject(prefs.getString("viewer.preset:Old","{}")).getInt("elementHighlight:test"));
            s.setHighlight("test",2);prefs.edit().putBoolean("elementHidden:test",true).commit();PresetAndColourTest.save(a,"New");s.setHighlight("test",0);prefs.edit().remove("elementHidden:test").commit();assertTrue(PresetAndColourTest.modified(a));PresetAndColourTest.load(a,"New");assertEquals(2,s.highlight("test"));assertTrue(s.hidden("test"));assertFalse(PresetAndColourTest.modified(a));
            PresetAndColourTest.load(a,"Old");assertEquals(1,s.highlight("test"));assertFalse(s.hidden("test"));assertFalse(PresetAndColourTest.modified(a));
        }
    }
    @Test public void voltageEdgesAndAllRequestedTextInsetsHaveTenPixels()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();RenderTest.renderFixture(a);CompanionLayoutTest.layout(a,1024,600);
            for(String name:new String[]{"V [P]","V [S]","Δ","[1]"}){LiveDashboard.Readout r=readout(a,name);assertEquals(10,r.getPaddingLeft());assertEquals(10,r.getPaddingRight());}
            LiveDashboard.Readout p=readout(a,"V [P]"),s=readout(a,"V [S]");assertEquals(624,p.getRight());assertEquals(p.getRight(),s.getRight());assertEquals(6,630-p.getRight());assertEquals(18,readout(a,"[1]").getLeft()-p.getRight());
            LiveDashboard.Readout inverter=readout(a,"Inverter");assertEquals("loads",inverter.slot);assertTrue(inverter.getBottom()>readout(a,"Load 9").getBottom());
        }
    }
    @Test public void transientSnapshotsKeepTheSameViewsAndOnlyExpireValues()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();RenderTest.renderFixture(a);LiveDashboard dashboard=(LiveDashboard)ActivityTest.field(a,"liveDashboard");LiveDashboard.Readout battery=readout(a,"Battery [Pico]");Object background=battery.getBackground();ActivityTest.call(a,"updateLive");assertSame(background,battery.getBackground());
            JSONObject live=(JSONObject)ActivityTest.field(a,"live");live.getJSONObject("pico").put("fresh",false).put("readings",JSONObject.NULL);ActivityTest.call(a,"updateLive");assertSame(dashboard,ActivityTest.field(a,"liveDashboard"));assertSame(battery,readout(a,"Battery [Pico]"));assertNotNull(readout(a,"Battery [BMS]").datum.value);assertNull(battery.datum.value);
            RenderTest.renderFixture(a);assertSame(dashboard,ActivityTest.field(a,"liveDashboard"));ActivityTest.set(a,"receivedMono",SystemClock.elapsedRealtime()-4000);ActivityTest.call(a,"updateLive");assertSame(dashboard,ActivityTest.field(a,"liveDashboard"));assertNull(readout(a,"Battery [BMS]").datum.value);assertNull(readout(a,"Battery [Pico]").datum.value);
        }
    }
    @Test public void collectorFreshnessDoesNotGetASecondShortAgeCutoffAndInverterKeepsSigns()throws Exception{
        JSONObject l=ContractTest.live();l.getJSONObject("sbms").put("ageSeconds",2.9);
        Map<String,MonitorData.Datum> data=MonitorData.display(MonitorData.catalogue(ContractTest.catalogue()),l,.8);assertNotNull(ContractTest.find(data,"sbmsBattery","current").value);assertEquals(0,data.get("pico:inverter").value,0);
        l.getJSONObject("sbms").put("fresh",false);data=MonitorData.display(MonitorData.catalogue(ContractTest.catalogue()),l,0);assertNull(ContractTest.find(data,"sbmsBattery","current").value);assertEquals(0,data.get("pico:inverter").value,0);
        l.getJSONObject("pico").put("fresh",false);assertNull(MonitorData.display(MonitorData.catalogue(ContractTest.catalogue()),l,0).get("pico:inverter").value);assertFalse(MonitorData.fresh(ContractTest.source(new JSONObject(),"reading",.1),3.1));
    }
    @Test public void addedColoursRoundTripAndEditorOrdersChangedBesideTitleOutline()throws Exception{
        ThemeConfig t=ThemeConfig.defaults();t.highlight2=Color.RED;t.indicatorBackground=Color.GREEN;t.voltagesBackground=Color.BLUE;t.gaugeHighlight=Color.CYAN;ThemeConfig copy=ThemeConfig.fromJson(t.toJson());assertTrue(copy.sameColours(t));copy.voltagesBackground=Color.YELLOW;assertFalse(copy.sameColours(t));assertEquals(Color.CYAN,ThemeConfig.fromJson(new JSONObject().put("gaugeHighlight",Color.CYAN)).gaugeHighlight);
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            ThemeAppearanceView editor=new ThemeAppearanceView(c.get(),c.get());java.lang.reflect.Field f=ThemeAppearanceView.class.getDeclaredField("colourFields");f.setAccessible(true);String[] fields=(String[])f.get(editor);assertEquals(ThemeConfig.TITLE_OUTLINE,fields[6]);assertEquals(ThemeConfig.CHANGED_INDICATOR,fields[7]);assertEquals(ThemeConfig.HIGHLIGHT_1,fields[16]);assertEquals(ThemeConfig.HIGHLIGHT_2,fields[17]);
        }
    }
    @Test public void delayedFailingMetadataDoesNotBlockOrClearLivePolling()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();ActivityTest.fixture(a);CountDownLatch started=new CountDownLatch(1),release=new CountDownLatch(1);String token=new String(new char[64]).replace('\0','a');
            ApiClient metadata=new ApiClient("https://fixture.invalid",token,url->new FixtureConnection(url,"{}",started,release));
            try{
                CompanionLayoutTest.invoke(a,"refreshMetadata",new Class<?>[]{int.class,ApiClient.class},ActivityTest.field(a,"epoch"),metadata);assertTrue(started.await(2,TimeUnit.SECONDS));
                JSONObject body=ContractTest.live();body.getJSONArray("measurements").getJSONObject(1).getJSONObject("values").put("current",-7);
                ActivityTest.set(a,"liveClient",new ApiClient("https://fixture.invalid",token,url->new FixtureConnection(url,body.toString(),null,null)));ActivityTest.call(a,"poll");((ExecutorService)ActivityTest.field(a,"liveWorker")).submit(()->{}).get(2,TimeUnit.SECONDS);PresetAndColourTest.idle();
                assertEquals(-7,readout(a,"Battery [BMS]").datum.value,0);assertNotNull(ActivityTest.field(a,"live"));
            }finally{release.countDown();}
            ((ExecutorService)ActivityTest.field(a,"metadataWorker")).submit(()->{}).get(2,TimeUnit.SECONDS);PresetAndColourTest.idle();assertEquals(-7,readout(a,"Battery [BMS]").datum.value,0);assertNotNull(ActivityTest.field(a,"live"));
        }
    }
    @Test public void screenAndLockStatesAreIndependentOfCurrentAndLegacyPresets()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();PresetAndColourTest.save(a,"Saved");
            JSONObject saved=new JSONObject(a.viewerPreferences().getString("viewer.preset:Saved","{}"));
            for(String key:new String[]{"keepScreen","fullscreen","locked"})assertFalse(saved.has(key));
            for(String label:new String[]{"Keep screen on","Full screen","Lock gauge popups"})ActivityTest.description(a.getWindow().getDecorView(),label).performClick();
            assertFalse(PresetAndColourTest.modified(a));
            saved.put("keepScreen",false).put("fullscreen",false).put("locked",false);
            a.viewerPreferences().edit().putString("viewer.preset:Legacy",saved.toString()).commit();
            for(String preset:new String[]{"Saved","Legacy","System default"}){
                PresetAndColourTest.load(a,preset);
                for(String key:new String[]{"keepScreen","fullscreen","locked"}){assertEquals(true,ActivityTest.field(a,key));assertTrue(a.viewerPreferences().getBoolean(key,false));}
                assertFalse(PresetAndColourTest.modified(a));
            }
        }
    }
    private static final class FixtureConnection extends HttpURLConnection {
        final byte[] body;final CountDownLatch started,release;
        FixtureConnection(URL url,String body,CountDownLatch started,CountDownLatch release){super(url);this.body=body.getBytes(java.nio.charset.StandardCharsets.UTF_8);this.started=started;this.release=release;}
        public int getResponseCode()throws java.io.IOException{if(started!=null){started.countDown();try{if(!release.await(4,TimeUnit.SECONDS))throw new java.io.IOException("Fixture timeout");}catch(InterruptedException e){throw new java.io.IOException(e);}return 503;}return 200;}
        public InputStream getInputStream(){return new ByteArrayInputStream(body);}
        public long getContentLengthLong(){return body.length;}
        public void disconnect(){}public boolean usingProxy(){return false;}public void connect(){}
    }
}
