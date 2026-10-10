package com.colonellp.ellamonitor;

import android.app.Dialog;
import android.graphics.Color;
import android.view.View;
import android.widget.CheckBox;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="w1024dp-h600dp-land-mdpi")
public class CompanionLayoutTest {
    @Before public void clear(){org.robolectric.RuntimeEnvironment.getApplication().getSharedPreferences("viewer",0).edit().clear().commit();}
    static Object invoke(MainActivity a,String name,Class<?>[] types,Object... args)throws Exception{Method m=MainActivity.class.getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(a,args);}
    static void layout(MainActivity a,int w,int h)throws Exception{View v=(View)ActivityTest.field(a,"viewport");v.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));v.layout(0,0,w,h);}
    @Test public void labelsAndHighlightCompareAgainstSavedPresetAndDefault()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();assertEquals(false,invoke(a,"presetModified",new Class<?>[]{}));
            PrivateSettings settings=(PrivateSettings)ActivityTest.field(a,"settings");settings.setLabel("test-label","Edited");assertEquals(true,invoke(a,"presetModified",new Class<?>[]{}));
            invoke(a,"writePreset",new Class<?>[]{String.class},"Test");assertEquals(false,invoke(a,"presetModified",new Class<?>[]{}));
            settings.setLabel("test-label","Unsaved");assertEquals(true,invoke(a,"presetModified",new Class<?>[]{}));
            invoke(a,"loadPreset",new Class<?>[]{String.class},"Test");assertEquals("Edited",settings.label("test-label","Original"));assertEquals(false,invoke(a,"presetModified",new Class<?>[]{}));
            settings.prefs.edit().putBoolean("gaugeHighlight:test",true).apply();assertEquals(true,invoke(a,"presetModified",new Class<?>[]{}));
            invoke(a,"loadPreset",new Class<?>[]{String.class},"System default");assertFalse(settings.prefs.contains("gaugeHighlight:test"));assertEquals(false,invoke(a,"presetModified",new Class<?>[]{}));
        }
    }
    @Test public void themeRolesRoundTripIndependentlyAndOldThemesInheritColours()throws Exception{
        ThemeConfig t=ThemeConfig.defaults();t.gaugePanel1=Color.RED;t.gaugeOutline1=Color.GREEN;t.gaugePanel2=Color.BLUE;t.gaugeOutline2=Color.YELLOW;t.gaugeHighlight=Color.MAGENTA;t.titleBackground=Color.GRAY;t.titleOutline=Color.CYAN;
        ThemeConfig restored=ThemeConfig.fromJson(t.toJson());assertTrue(t.sameColours(restored));restored.gaugeOutline2=Color.WHITE;assertFalse(t.sameColours(restored));assertEquals(Color.RED,restored.gaugePanel1);
        ThemeConfig old=ThemeConfig.fromJson(new JSONObject().put("buttonBackground",Color.RED).put("controls",Color.GREEN).put("panel",Color.BLUE).put("border",Color.YELLOW));assertEquals(Color.RED,old.gaugePanel1);assertEquals(Color.RED,old.gaugePanel2);assertEquals(Color.GREEN,old.gaugeOutline1);assertEquals(Color.GREEN,old.gaugeOutline2);assertEquals(Color.BLUE,old.titleBackground);assertEquals(Color.YELLOW,old.titleOutline);
    }
    @Test public void fullscreenKeepsBarAndControlsSameHeightAndDropdownOrder()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();layout(a,1024,510);android.widget.LinearLayout root=(android.widget.LinearLayout)ActivityTest.field(a,"root");View bar=root.getChildAt(root.getChildCount()-1);int height=bar.getHeight(),button=((View)ActivityTest.field(a,"presetAnchor")).getHeight();
            View preset=(View)ActivityTest.field(a,"presetAnchor"),theme=(View)ActivityTest.field(a,"themeAnchor"),live=ActivityTest.text(root,"Live data");assertEquals(6,theme.getLeft()-preset.getRight());assertEquals(6,live.getLeft()-theme.getRight());
            ActivityTest.description(root,"Full screen").performClick();layout(a,1024,600);root=(android.widget.LinearLayout)ActivityTest.field(a,"root");bar=root.getChildAt(root.getChildCount()-1);assertEquals(height,bar.getHeight());assertEquals(button,((View)ActivityTest.field(a,"presetAnchor")).getHeight());
        }
    }
    @Test @Config(qualifiers="w1024dp-h600dp-land-xxhdpi") public void checkboxFitsPhoneSettingsAndFooterUsesTwoRows()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();ActivityTest.description(a.getWindow().getDecorView(),"Settings").performClick();layout(a,1920,1080);View root=a.getWindow().getDecorView();CheckBox check=(CheckBox)ActivityTest.text(root,"Hide connection status");assertEquals(24,check.getButtonDrawable().getIntrinsicHeight());assertTrue(check.getButtonDrawable().getIntrinsicHeight()<check.getHeight());assertTrue(check.getWidth()>check.getButtonDrawable().getIntrinsicWidth());assertNotNull(ActivityTest.text(root,"Updates: 24hr  ▾"));assertNull(ActivityTest.text(root,"SYSTEM"));check.performClick();assertTrue(check.isChecked());
        }
    }
    @Test public void colourCancelAndOkReturnToEditorWithoutDim()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();layout(a,1024,600);ActivityTest.call(a,"showThemeEditor");Dialog first=(Dialog)ActivityTest.field(a,"appearanceDialog");assertEquals(0,first.getWindow().getAttributes().flags&android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            a.editThemeAppearanceColour(ThemeConfig.GAUGE_PANEL_1,"GAUGE PANEL 1");Dialog picker=org.robolectric.shadows.ShadowDialog.getLatestDialog();assertEquals(0,picker.getWindow().getAttributes().flags&android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);ActivityTest.text(picker.getWindow().getDecorView(),"CANCEL").performClick();assertNotNull(ActivityTest.field(a,"appearanceDialog"));
            a.editThemeAppearanceColour(ThemeConfig.GAUGE_PANEL_2,"GAUGE PANEL 2");picker=org.robolectric.shadows.ShadowDialog.getLatestDialog();ActivityTest.text(picker.getWindow().getDecorView(),"OK").performClick();assertNotNull(ActivityTest.field(a,"appearanceDialog"));
        }
    }
    @Test public void gaugePopupHighlightPersistsThroughRefresh()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();RenderTest.renderFixture(a);layout(a,1024,600);LiveDashboard dashboard=(LiveDashboard)ActivityTest.field(a,"liveDashboard");LiveDashboard.Readout gauge=null;
            for(int i=0;i<dashboard.getChildCount();i++)if(dashboard.getChildAt(i) instanceof LiveDashboard.Readout){LiveDashboard.Readout r=(LiveDashboard.Readout)dashboard.getChildAt(i);if(r.fallback.equals("Battery [Pico]"))gauge=r;}
            assertNotNull(gauge);assertEquals(109,gauge.getWidth(),1);gauge.performClick();android.app.AlertDialog popup=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();CheckBox choice=(CheckBox)ActivityTest.text(popup.getWindow().getDecorView(),"Highlight background");assertNotNull(choice);choice.performClick();assertTrue(a.viewerPreferences().getBoolean("gaugeHighlight:"+gauge.id,false));ActivityTest.call(a,"updateLive");
            android.graphics.drawable.GradientDrawable background=(android.graphics.drawable.GradientDrawable)gauge.getBackground().getCurrent();assertEquals(((ThemeConfig)ActivityTest.field(a,"theme")).gaugeHighlight,background.getColor().getDefaultColor());popup.dismiss();
        }
    }

}
