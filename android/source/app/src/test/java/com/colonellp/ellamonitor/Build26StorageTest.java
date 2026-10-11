package com.colonellp.ellamonitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="w1024dp-h600dp-land-mdpi")
public class Build26StorageTest {
    interface Action {void run()throws Exception;}
    static void rejected(Action action)throws Exception{try{action.run();fail("Malformed file accepted");}catch(IllegalArgumentException expected){}}
    Context context;SharedPreferences prefs;ThemeStorageTest.DownloadsProvider provider;
    @Before public void setup(){context=RuntimeEnvironment.getApplication();prefs=context.getSharedPreferences("viewer",0);prefs.edit().clear().putInt("update.intervalHours",0).commit();provider=new ThemeStorageTest.DownloadsProvider();ThemeStorageTest.installProvider(provider);}
    @Test public void strictDocumentsRejectNonJsonDuplicatesTrailingBytesAndInvalidUtf8()throws Exception{
        assertEquals(1,SavedJson.object("{\"range\":1}").getInt("range"));
        for(String text:new String[]{"[]","{range:1}","{'range':1}","{\"range\":1,}","{\"range\":1} trailing","{\"range\":1,\"range\":2}","{\"range\":01}","{\"range\":NaN}","{\"range\":1;}","{\"range\":1}//comment"})rejected(()->SavedJson.object(text));
        try{SavedJson.read(new ByteArrayInputStream(new byte[]{(byte)0xc3,0x28}));fail();}catch(java.nio.charset.CharacterCodingException expected){}
        rejected(()->SavedJson.read(new ByteArrayInputStream(new byte[65537])));
        String deep="0";for(int i=0;i<40;i++)deep="["+deep+"]";final String nested="{\"range\":"+deep+"}";rejected(()->SavedJson.object(nested));
    }
    @Test public void presetTypesRangesEnumsAndMetadataAreValidated()throws Exception{
        for(Object[] bad:new Object[][]{{"range",5},{"range",1.5},{"range","1"},{"hideStatus","true"},{"persistent",1},{"updateIntervalHours",2},{"quantity","guess"},{"fontFamily","unknown"},{"elementHighlight:test",3},{"elementHidden:test",0},{"label:test",""},{"connection",JSONObject.NULL},{"presetFormat",2}}){JSONObject json=PresetState.defaults().put((String)bad[0],bad[1]);rejected(()->PresetState.normalize(json));}
        rejected(()->PresetState.normalize(new JSONObject()));rejected(()->PresetState.normalize(new JSONObject().put("unrelated",true)));
        JSONObject legacy=PresetState.defaults().put("theme",ThemeConfig.defaults().toJson().put("fontFamily","monospace")).put("keepScreen",true).put("obsoleteSetting",123).put("gaugeHighlight:test",true);legacy.remove("fontFamily");
        JSONObject migrated=PresetState.normalize(legacy);assertEquals("monospace",migrated.getString("fontFamily"));assertEquals(1,migrated.getInt("elementHighlight:test"));assertFalse(migrated.has("theme"));assertFalse(migrated.has("keepScreen"));
    }
    @Test public void themeFieldsAreStrictButOlderMissingRolesStillInherit()throws Exception{
        for(Object[] bad:new Object[][]{{"background","123"},{"controls",true},{"titleText",1.2},{"format",18},{"spectrumColours",1},{"name","../escape"}}){JSONObject json=ThemeConfig.defaults().toJson().put("name","Test").put((String)bad[0],bad[1]);rejected(()->ThemeConfig.validatedSaved(json));}
        rejected(()->ThemeConfig.validatedSaved(new JSONObject().put("name","Test")));
        ThemeConfig old=ThemeConfig.validatedSaved(new JSONObject().put("name","Old").put("background",0xff123456).put("controls",0xff334455));assertEquals(0xff123456,old.background);assertEquals(old.controls,old.buttonBorder);
        assertEquals(-1,ThemeConfig.validatedSaved(new JSONObject().put("background",0xffffffffL)).background);
    }
    @Test public void presetFilesMigrateWithoutOverwritingConflictsAndTrackSelectedName()throws Exception{
        PresetStore store=new PresetStore(context,prefs);store.save("Trip",PresetState.defaults().put("label:test","File label"));prefs.edit().putString("viewer.preset:Trip",PresetState.defaults().put("label:test","Private label").toString()).putString("currentPreset","Trip").commit();
        assertTrue(store.names().containsAll(Arrays.asList("Trip","Trip (migrated 1)")));assertEquals("Trip (migrated 1)",prefs.getString("currentPreset",""));assertFalse(prefs.contains("viewer.preset:Trip"));assertEquals("File label",store.load("Trip").getString("label:test"));assertEquals("Private label",store.load("Trip (migrated 1)").getString("label:test"));
        assertTrue(new DownloadsFiles(context,"presets").files().containsKey("Trip.json"));store.rename("Trip","Renamed");assertFalse(store.names().contains("Trip"));store.delete("Renamed");assertFalse(store.names().contains("Renamed"));
        for(String name:new String[]{"../bad","bad\\path","bad\nname","System default","tail.","Default "})rejected(()->store.save(name,PresetState.defaults()));
        String max=new String(new char[80]).replace('\0','x');store.save(max,PresetState.defaults());assertNotNull(store.load(max));
    }
    @Test public void malformedFileIsNotAppliedOrSilentlyLoadedAsDefaults()throws Exception{
        PresetStore store=new PresetStore(context,prefs);store.save("Valid",PresetState.defaults().put("label:test","Saved"));DownloadsFiles files=new DownloadsFiles(context,"presets");files.write("Bad.json","application/json","{\"range\":99}".getBytes(StandardCharsets.UTF_8));assertFalse(store.names().contains("Bad"));assertNotNull(store.takeProblem());rejected(()->store.load("Bad"));assertEquals("Saved",store.load("Valid").getString("label:test"));
        AppearanceStore themes=new AppearanceStore(context,prefs);ThemeConfig active=ThemeConfig.defaults();active.name="Current";active.background=0xff123456;themes.active(active);new DownloadsFiles(context,"themes").write("Broken.json","application/json","{\"name\":\"Broken\",\"background\":\"nonsense\"}".getBytes(StandardCharsets.UTF_8));assertNull(themes.load("Broken"));assertNotNull(themes.takeProblem());assertEquals(active.background,themes.active().background);
    }
    static class FailingProvider extends ThemeStorageTest.DownloadsProvider {
        boolean failWrite;
        @Override public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException{
            if(mode.contains("w")&&failWrite){failWrite=false;try(FileOutputStream out=new FileOutputStream(files.get(Long.parseLong(uri.getLastPathSegment())))){out.write('X');}catch(IOException e){throw new FileNotFoundException();}throw new FileNotFoundException("Synthetic write failure");}return super.openFile(uri,mode);
        }
    }
    @Test public void failedOverwriteRestoresFileAndFailedMigrationKeepsPrivateOriginal()throws Exception{
        FailingProvider failing=new FailingProvider();ThemeStorageTest.installProvider(failing);PresetStore store=new PresetStore(context,prefs);store.save("Saved",PresetState.defaults().put("label:test","Original"));failing.failWrite=true;try{store.save("Saved",PresetState.defaults().put("label:test","Replacement"));fail();}catch(IOException expected){}assertEquals("Original",store.load("Saved").getString("label:test"));
        prefs.edit().putString("viewer.preset:Legacy",PresetState.defaults().toString()).commit();failing.failWrite=true;store.names();assertTrue(prefs.contains("viewer.preset:Legacy"));assertFalse(new DownloadsFiles(context,"presets").files().containsKey("Legacy.json"));assertTrue(store.names().contains("Legacy"));assertFalse(prefs.contains("viewer.preset:Legacy"));
    }
    @Test @Config(sdk=27) public void legacyDownloadsUsesFilesWithoutPicker()throws Exception{
        PresetStore store=new PresetStore(context,prefs);store.save("Legacy storage",PresetState.defaults());Map<String,Uri> files=new DownloadsFiles(context,"presets").files();assertEquals("file",files.get("Legacy storage.json").getScheme());assertNotNull(store.load("Legacy storage"));store.rename("Legacy storage","Renamed legacy");store.delete("Renamed legacy");assertFalse(store.names().contains("Renamed legacy"));
    }
    @Test public void wrongKeyAndMalformedPresetsKeepAllCurrentSettings()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();PrivateSettings settings=PresetAndColourTest.cryptoSettings(a);ActivityTest.set(a,"settings",settings);String secret=new String(new char[64]).replace('\0','c');settings.saveConnection("https://saved.example.invalid",secret);PresetAndColourTest.save(a,"Encrypted");PresetStore store=(PresetStore)ActivityTest.field(a,"presets");JSONObject saved=store.load("Encrypted");assertFalse(saved.toString().contains(secret));
            String token=saved.getJSONObject("connection").getString("token");byte[] bytes=android.util.Base64.decode(token,android.util.Base64.NO_WRAP);bytes[0]^=1;saved.getJSONObject("connection").put("token",android.util.Base64.encodeToString(bytes,android.util.Base64.NO_WRAP));saved.put("label:test","Unwanted label");store.save("Wrong key",saved);
            settings.setLabel("test","Current label");PresetAndColourTest.load(a,"Wrong key");assertEquals(secret,settings.token());assertEquals("Current label",settings.label("test",""));assertEquals("Encrypted",ActivityTest.field(a,"currentPreset"));
            new DownloadsFiles(a,"presets").write("Bad.json","application/json","{\"range\":99}".getBytes(StandardCharsets.UTF_8));PresetAndColourTest.load(a,"Bad");assertEquals("Current label",settings.label("test",""));assertEquals("Encrypted",ActivityTest.field(a,"currentPreset"));
        }
    }
    @org.robolectric.annotation.Implements(android.app.Activity.class)
    public static class PickerFailureShadow extends org.robolectric.shadows.ShadowActivity {
        static boolean security;
        @org.robolectric.annotation.Implementation protected void startActivityForResult(Intent intent,int request,android.os.Bundle options){if(request==100){if(security)throw new SecurityException("Synthetic launch denied");throw new android.content.ActivityNotFoundException("Synthetic no handler");}}
    }
    @Test @Config(shadows=PickerFailureShadow.class) public void pickerLaunchFailuresExportExactCsvToDownloadsAndCancelDoesNot()throws Exception{
        for(boolean security:new boolean[]{false,true}){
            PickerFailureShadow.security=security;
            try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
                MainActivity a=c.get();MonitorData.Metric metric=MonitorData.catalogue(ContractTest.catalogue()).get(0);HistoryData.Window window=HistoryData.window(java.time.Instant.parse("2026-01-01T00:00:00Z"),0,false,0,metric.kind);HistoryData.Result result=new HistoryData.Result(metric,window,Collections.emptyList(),null);ActivityTest.set(a,"history",result);
                ActivityTest.call(a,"exportHistory");((ExecutorService)ActivityTest.field(a,"historyWorker")).submit(()->{}).get(5,TimeUnit.SECONDS);PresetAndColourTest.idle();Map<String,Uri> files=new DownloadsFiles(a,"exports").files();assertFalse(files.isEmpty());assertNull(ActivityTest.field(a,"exportBytes"));
                for(Uri uri:files.values())try(InputStream in=new DownloadsFiles(a,"exports").read(uri)){String csv=new String(readAll(in),StandardCharsets.UTF_8);assertEquals(HistoryData.csv(result,(String)CompanionLayoutTest.invoke(a,"metricLabel",new Class<?>[]{MonitorData.Metric.class},metric)),csv);}
                int before=files.size();ActivityTest.set(a,"exportBytes","cancelled".getBytes(StandardCharsets.UTF_8));CompanionLayoutTest.invoke(a,"onActivityResult",new Class<?>[]{int.class,int.class,Intent.class},100,android.app.Activity.RESULT_CANCELED,null);assertEquals(before,new DownloadsFiles(a,"exports").files().size());assertNull(ActivityTest.field(a,"exportBytes"));
                ActivityTest.description(a.getWindow().getDecorView(),"Settings").performClick();assertNull(ActivityTest.text(a.getWindow().getDecorView(),"Export current chart range"));
            }
        }
    }
    static byte[] readAll(InputStream in)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[1024];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return out.toByteArray();}
}
