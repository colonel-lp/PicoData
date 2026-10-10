package com.colonellp.ellamonitor;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import javax.crypto.spec.SecretKeySpec;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="w1024dp-h600dp-land-mdpi")
public class PresetAndColourTest {
    @Before public void clear(){RuntimeEnvironment.getApplication().getSharedPreferences("viewer",0).edit().clear().commit();}
    static Object call(MainActivity a,String method,Class<?>[] types,Object... args)throws Exception{return CompanionLayoutTest.invoke(a,method,types,args);}
    static boolean modified(MainActivity a)throws Exception{return (Boolean)call(a,"presetModified",new Class<?>[]{});}
    static void save(MainActivity a,String name)throws Exception{call(a,"writePreset",new Class<?>[]{String.class},name);}
    static void load(MainActivity a,String name)throws Exception{call(a,"loadPreset",new Class<?>[]{String.class},name);}
    static PrivateSettings cryptoSettings(android.content.Context context){return new PrivateSettings(context,()->new SecretKeySpec(new byte[32],"AES"));}
    static EditText input(View view){if(view instanceof EditText)return (EditText)view;if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){EditText found=input(((ViewGroup)view).getChildAt(i));if(found!=null)return found;}return null;}
    static Dialog picker(){return org.robolectric.shadows.ShadowDialog.getLatestDialog();}
    static void idle(){((org.robolectric.shadows.ShadowLooper)org.robolectric.shadow.api.Shadow.extract(Looper.getMainLooper())).idle();}
    static void assertEditor(MainActivity a)throws Exception{Dialog d=(Dialog)ActivityTest.field(a,"appearanceDialog");assertNotNull(d);assertTrue(d.isShowing());assertEquals(View.VISIBLE,d.getWindow().getDecorView().getVisibility());assertEquals(0,d.getWindow().getAttributes().flags&android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);}

    @Test public void rgbValidationAcceptsOptionalHashAndRejectsPartialOrInvalid(){
        assertEquals(Integer.valueOf(0xffabcdef),MainActivity.parseHexColour("  AbCdEf  "));
        assertEquals(Integer.valueOf(0xff012345),MainActivity.parseHexColour(" #012345 "));
        for(String value:new String[]{"","#123","12345","1234567","GG1234","++1234","##123456","#12345678"})assertNull(MainActivity.parseHexColour(value));
        assertNull(MainActivity.parseHexColour(null));
    }
    @Test public void pickerReturnsVisibleEditorAfterQueuedCallbacksAndInvalidOkStaysOpen()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();CompanionLayoutTest.layout(a,1024,600);ActivityTest.call(a,"showThemeEditor");ThemeConfig theme=(ThemeConfig)ActivityTest.field(a,"theme");int original=theme.titleText;
            a.editThemeAppearanceColour(ThemeConfig.TITLE_TEXT,"TITLE TEXT");Dialog colour=picker();input(colour.getWindow().getDecorView()).setText("#123");ActivityTest.text(colour.getWindow().getDecorView(),"OK").performClick();idle();assertTrue(colour.isShowing());assertNotNull(input(colour.getWindow().getDecorView()).getError());assertEquals(original,theme.titleText);
            input(colour.getWindow().getDecorView()).setText(" 123abc ");ActivityTest.text(colour.getWindow().getDecorView(),"OK").performClick();idle();assertFalse(colour.isShowing());assertEquals(0xff123abc,theme.titleText);assertEditor(a);((org.robolectric.shadows.ShadowLooper)org.robolectric.shadow.api.Shadow.extract(Looper.getMainLooper())).idleFor(java.time.Duration.ofSeconds(1));assertEditor(a);
            a.editThemeAppearanceColour(ThemeConfig.TITLE_TEXT,"TITLE TEXT");colour=picker();input(colour.getWindow().getDecorView()).setText("#ffffff");ActivityTest.text(colour.getWindow().getDecorView(),"CANCEL").performClick();idle();assertEquals(0xff123abc,theme.titleText);assertEditor(a);
            a.editThemeAppearanceColour(ThemeConfig.TITLE_TEXT,"TITLE TEXT");colour=picker();input(colour.getWindow().getDecorView()).setText("#000000");colour.cancel();idle();assertEquals(0xff123abc,theme.titleText);assertEditor(a);
        }
    }
    @Test public void themeChangesDoNotDirtyPresetAndFontBelongsToPreset()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();save(a,"Saved");ThemeConfig theme=(ThemeConfig)ActivityTest.field(a,"theme");theme.background=Color.RED;theme.name="Other";assertFalse(modified(a));
            theme.fontFamily="monospace";assertTrue(modified(a));assertFalse(theme.toJson().has("fontFamily"));assertFalse(theme.toJson().has(ThemeConfig.LINE_GLOW_STRENGTH_100));
            load(a,"Saved");assertFalse(modified(a));assertEquals(Color.RED,((ThemeConfig)ActivityTest.field(a,"theme")).background);assertEquals("Other",((ThemeConfig)ActivityTest.field(a,"theme")).name);
        }
    }
    @Test public void chartFallbackIsReadOnlyAndUserChartEditsRefreshImmediately()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();RenderTest.renderFixture(a);CompanionLayoutTest.layout(a,1024,600);save(a,"Saved");assertEquals("",ActivityTest.field(a,"selectedMetric"));call(a,"selected",new Class<?>[]{});assertEquals("",ActivityTest.field(a,"selectedMetric"));assertFalse(modified(a));
            ActivityTest.text(a.getWindow().getDecorView(),"Charts").performClick();assertFalse(modified(a));TextView rolling=(TextView)ActivityTest.text(a.getWindow().getDecorView(),"Rolling");assertNotNull(rolling);rolling.performClick();assertTrue(modified(a));assertTrue(((TextView)ActivityTest.field(a,"presetAnchor")).getText().toString().contains("UNSAVED"));
            assertEquals(((ThemeConfig)ActivityTest.field(a,"theme")).changedIndicator,((TextView)ActivityTest.text(a.getWindow().getDecorView(),"Defined UTC")).getCurrentTextColor());
        }
    }
    @Test public void labelsSettingsAndTitlesUseTheirRespectiveColoursAndClearOnSave()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();RenderTest.renderFixture(a);save(a,"Saved");PrivateSettings settings=(PrivateSettings)ActivityTest.field(a,"settings");ThemeConfig theme=(ThemeConfig)ActivityTest.field(a,"theme");theme.titleText=Color.BLUE;theme.changedIndicator=Color.MAGENTA;
            settings.setLabel("group:loads","Edited currents");ActivityTest.call(a,"build");assertEquals(Color.MAGENTA,((TextView)ActivityTest.text(a.getWindow().getDecorView(),"Edited currents")).getCurrentTextColor());assertEquals(Color.BLUE,((TextView)ActivityTest.text(a.getWindow().getDecorView(),"Temps:")).getCurrentTextColor());
            save(a,"Saved");assertEquals(Color.BLUE,((TextView)ActivityTest.text(a.getWindow().getDecorView(),"Edited currents")).getCurrentTextColor());
            ActivityTest.description(a.getWindow().getDecorView(),"Settings").performClick();TextView clock=(TextView)ActivityTest.text(a.getWindow().getDecorView(),"Display local clock");clock.performClick();assertEquals(Color.MAGENTA,clock.getCurrentTextColor());save(a,"Saved");assertEquals(theme.text,((TextView)ActivityTest.text(a.getWindow().getDecorView(),"Display local clock")).getCurrentTextColor());
        }
    }
    @Test public void encryptedConnectionSnapshotComparesValuesAndRejectsTamperingBeforeCommit()throws Exception{
        PrivateSettings settings=cryptoSettings(RuntimeEnvironment.getApplication());String first=new String(new char[64]).replace('\0','a'),second=new String(new char[64]).replace('\0','b');
        settings.saveConnection("https://first.example.invalid",first);JSONObject saved=settings.connectionSnapshot();assertFalse(saved.toString().contains(first));
        settings.saveConnection("https://first.example.invalid",first);assertNotEquals(saved.getString("token"),settings.connectionSnapshot().getString("token"));assertTrue(settings.connectionDifferences(saved).isEmpty());
        settings.saveConnection("https://second.example.invalid",second);assertEquals(2,settings.connectionDifferences(saved).size());android.content.SharedPreferences.Editor edit=settings.prefs.edit();settings.stageConnection(edit,saved);assertTrue(edit.commit());assertEquals(first,settings.token());
        JSONObject damaged=new JSONObject(saved.toString()).put("token",saved.getString("token").substring(4));edit=settings.prefs.edit();try{settings.stageConnection(edit,damaged);fail();}catch(Exception expected){}assertEquals(first,settings.token());assertEquals("https://first.example.invalid",settings.prefs.getString("address",""));
    }
    @Test public void presetRestoresEncryptedConnectionAndKeepsTokenOutOfCopiedValues()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();PrivateSettings settings=cryptoSettings(a);ActivityTest.set(a,"settings",settings);String token=new String(new char[64]).replace('\0','c');settings.saveConnection("https://saved.example.invalid",token);save(a,"Connection");String stored=settings.prefs.getString("viewer.preset:Connection","");assertFalse(stored.contains(token));assertTrue(new JSONObject(stored).has("connection"));
            settings.saveConnection("https://other.example.invalid",new String(new char[64]).replace('\0','d'));assertTrue(modified(a));load(a,"Connection");assertEquals(token,settings.token());assertFalse(modified(a));
            call(a,"showPresetValues",new Class<?>[]{String.class},"Connection");android.app.AlertDialog values=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();assertEquals("COPY",values.getButton(android.app.AlertDialog.BUTTON_POSITIVE).getText().toString());values.getButton(android.app.AlertDialog.BUTTON_POSITIVE).performClick();idle();android.content.ClipboardManager clipboard=a.getSystemService(android.content.ClipboardManager.class);assertNotNull(clipboard.getPrimaryClip());String copied=clipboard.getPrimaryClip().getItemAt(0).coerceToText(a).toString();assertTrue(copied.contains("token hidden"));assertFalse(copied.contains(token));assertFalse(copied.contains("saved.example.invalid"));
        }
    }
    @Test public void olderPresetsMigrateLabelsFontAndKeepExistingConnection()throws Exception{
        android.content.SharedPreferences prefs=RuntimeEnvironment.getApplication().getSharedPreferences("viewer",0);JSONObject oldTheme=ThemeConfig.defaults().toJson().put("fontFamily","monospace");JSONObject old=new JSONObject().put("label:test","Old label").put("theme",oldTheme);
        prefs.edit().putString("viewer.preset:Legacy",old.toString()).putString("address","https://legacy.example.invalid").putString("appearance.active",oldTheme.toString()).commit();
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();JSONObject migrated=new JSONObject(prefs.getString("viewer.preset:Legacy",""));assertFalse(migrated.has("theme"));assertEquals("monospace",migrated.getString("fontFamily"));assertTrue(migrated.has("connection"));load(a,"Legacy");assertEquals("Old label",prefs.getString("label:test",""));assertEquals("https://legacy.example.invalid",prefs.getString("address",""));assertFalse(modified(a));
        }
    }
}
