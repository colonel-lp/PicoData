package com.colonellp.ellamonitor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;

@RunWith(org.robolectric.RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="w1280dp-h720dp-land-mdpi")
public class Build25UiTest {
    private static Object editorField(ThemeAppearanceView editor,String name)throws Exception{java.lang.reflect.Field field=ThemeAppearanceView.class.getDeclaredField(name);field.setAccessible(true);return field.get(editor);}
    @Before public void clear(){RuntimeEnvironment.getApplication().getSharedPreferences("viewer",0).edit().clear().commit();}
    @Test public void rawToCatalogueKeepsElementChoicesAndDoesNotDirtyPreset() throws Exception {
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity a=c.get();ActivityTest.set(a,"metrics",MonitorData.catalogue(ContractTest.catalogue()));ActivityTest.set(a,"live",BindingAndGaugeTest.sample());ActivityTest.set(a,"receivedMono",android.os.SystemClock.elapsedRealtime());ActivityTest.call(a,"updateLive");
            LiveDashboard.Readout outside=DisplayRevisionTest.readout(a,"Outside");String id=outside.id;
            a.viewerPreferences().edit().putString("label:"+id,"My outside").putInt("elementHighlight:"+id,2).putBoolean("elementHidden:"+id,true).commit();PresetAndColourTest.save(a,"Saved");outside=DisplayRevisionTest.readout(a,"Outside");
            ActivityTest.set(a,"metrics",MonitorData.catalogue(BindingAndGaugeTest.instruments()));ActivityTest.call(a,"updateLive");
            LiveDashboard.Readout updated=DisplayRevisionTest.readout(a,"Outside");assertSame(outside,updated);assertEquals(id,updated.id);assertNotNull(updated.datum.metric);assertFalse(PresetAndColourTest.modified(a));
            updated=DisplayRevisionTest.readout(a,"Outside");assertEquals("My outside",updated.name.getText().toString());assertEquals(View.INVISIBLE,updated.name.getVisibility());
            assertEquals(1007,DisplayRevisionTest.readout(a,"Barometer").datum.value,0);
            JSONObject live=(JSONObject)ActivityTest.field(a,"live");live.getJSONObject("sbms").put("fresh",false);ActivityTest.call(a,"updateLive");assertEquals(1007,DisplayRevisionTest.readout(a,"Barometer").datum.value,0);
            AlertDialog popup=DisplayRevisionTest.popup(updated);assertTrue(DisplayRevisionTest.choice(popup,"Highlight 2").isChecked());assertTrue(DisplayRevisionTest.choice(popup,"Hide contents").isChecked());popup.dismiss();
        }
    }
    @Test public void existingCatalogueSettingsStillApplyToTheSameInstrument() throws Exception {
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity a=c.get();java.util.List<MonitorData.Metric> metrics=MonitorData.catalogue(BindingAndGaugeTest.instruments());MonitorData.Metric m=metrics.stream().filter(x->x.id.equals("outside-test")).findFirst().get();String id=m.alias+":temperature";
            a.viewerPreferences().edit().putString("label:"+id,"Saved outside").putInt("elementHighlight:"+id,1).putBoolean("elementHidden:"+id,true).commit();
            ActivityTest.set(a,"metrics",metrics);ActivityTest.set(a,"live",BindingAndGaugeTest.sample());ActivityTest.set(a,"receivedMono",android.os.SystemClock.elapsedRealtime());ActivityTest.call(a,"updateLive");
            LiveDashboard.Readout r=DisplayRevisionTest.readout(a,"Outside");assertEquals(id,r.id);assertEquals("Saved outside",r.name.getText().toString());assertEquals(View.INVISIBLE,r.name.getVisibility());assertNotNull(r.datum.metric);
        }
    }
    @Test public void footerHasFourControlsAndVoltagesUseTheCellTextSize() throws Exception {
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity a=c.get();RenderTest.renderFixture(a);CompanionLayoutTest.layout(a,1024,600);
            for(String label:new String[]{"V [P]","V [S]"}){LiveDashboard.Readout r=DisplayRevisionTest.readout(a,label),cell=DisplayRevisionTest.readout(a,"[1]");assertEquals(cell.value.getTextSize(),r.value.getTextSize(),0);assertTrue(r.centred());assertTrue(r.value.getText().toString().contains(label.replace(" ","")));assertEquals(10,r.getPaddingLeft());assertEquals(10,r.getPaddingRight());}
            for(int[] size:new int[][]{{1024,600},{360,760}}){CompanionLayoutTest.layout(a,size[0],size[1]);AlertDialog popup=DisplayRevisionTest.popup(DisplayRevisionTest.readout(a,"Battery [Pico]"));PresetAndColourTest.idle();
                ViewGroup footer=(ViewGroup)DisplayRevisionTest.choice(popup,"Highlight 1").getParent();assertEquals(4,footer.getChildCount());assertSame(footer,DisplayRevisionTest.choice(popup,"Highlight 2").getParent());assertSame(footer,DisplayRevisionTest.choice(popup,"Hide contents").getParent());assertEquals("CLOSE",((TextView)footer.getChildAt(3)).getText().toString());
                footer.measure(View.MeasureSpec.makeMeasureSpec(a.dialogDp(412),View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(a.dialogDp(38),View.MeasureSpec.EXACTLY));footer.layout(0,0,footer.getMeasuredWidth(),footer.getMeasuredHeight());
                for(int i=0;i<4;i++){assertTrue(footer.getChildAt(i).getWidth()>0);assertEquals(footer.getChildAt(0).getTop(),footer.getChildAt(i).getTop());assertEquals(footer.getChildAt(0).getHeight(),footer.getChildAt(i).getHeight());assertTrue(footer.getChildAt(i).getBottom()<=footer.getHeight());assertTrue(footer.getChildAt(i).getRight()<=footer.getWidth());}
                footer.getChildAt(3).performClick();assertFalse(popup.isShowing());
            }
        }
    }
    @Test public void fontMovesToEditorAndOnlySaveConfirmationsAdoptNameDialogPadding() throws Exception {
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity a=c.get();RenderTest.renderFixture(a);CompanionLayoutTest.layout(a,1024,600);ActivityTest.description(a.getWindow().getDecorView(),"Settings").performClick();PresetAndColourTest.idle();
            assertNull(ActivityTest.text(a.getWindow().getDecorView(),"Font: MEDIUM  ▾"));ActivityTest.call(a,"showThemeEditor");ThemeAppearanceView editor=(ThemeAppearanceView)ActivityTest.field(a,"appearanceView");RectF font=(RectF)editorField(editor,"fontButton"),back=(RectF)editorField(editor,"backButton");assertEquals(font.top,back.top,0);assertTrue(font.right<back.left);assertEquals(20,font.left,0);assertEquals(518,back.right,0);
            a.dismissThemeAppearanceDialog();final int[] saved={0};CompanionLayoutTest.invoke(a,"confirmOverwrite",new Class<?>[]{String.class,Runnable.class},"Test preset",(Runnable)()->saved[0]++);Dialog confirm=PresetAndColourTest.picker();TextView cancel=(TextView)ActivityTest.text(confirm.getWindow().getDecorView(),"CANCEL"),save=(TextView)ActivityTest.text(confirm.getWindow().getDecorView(),"SAVE CHANGES");ViewGroup buttons=(ViewGroup)cancel.getParent(),panel=(ViewGroup)buttons.getParent();assertSame(buttons,save.getParent());assertEquals(a.dialogDp(10),panel.getPaddingLeft());assertEquals(a.dialogDp(7),panel.getPaddingTop());assertEquals(cancel.getLayoutParams().height,save.getLayoutParams().height);assertEquals(1,((LinearLayout.LayoutParams)cancel.getLayoutParams()).weight,0);assertEquals(a.dialogDp(6),((LinearLayout.LayoutParams)save.getLayoutParams()).leftMargin);cancel.performClick();assertEquals(0,saved[0]);
            CompanionLayoutTest.invoke(a,"confirmOverwrite",new Class<?>[]{String.class,Runnable.class},"Test theme",(Runnable)()->saved[0]++);confirm=PresetAndColourTest.picker();ActivityTest.text(confirm.getWindow().getDecorView(),"SAVE CHANGES").performClick();assertEquals(1,saved[0]);assertFalse(confirm.isShowing());
        }
    }
    @Test public void mainBorderRoleRoundTripsAndIsSeparateFromBottomBarColours() throws Exception {
        ThemeConfig t=ThemeConfig.defaults();int bottom=t.controls,off=t.offButtonBorder;t.set(ThemeConfig.BUTTON_BORDER,Color.MAGENTA);assertEquals(bottom,t.controls);assertEquals(off,t.offButtonBorder);assertEquals(Color.MAGENTA,t.copy().buttonBorder);assertEquals(Color.MAGENTA,ThemeConfig.fromJson(t.toJson()).buttonBorder);assertFalse(t.sameColours(ThemeConfig.defaults()));assertEquals(Color.BLUE,ThemeConfig.fromJson(new JSONObject().put(ThemeConfig.CONTROLS,Color.BLUE)).buttonBorder);
    }
}
