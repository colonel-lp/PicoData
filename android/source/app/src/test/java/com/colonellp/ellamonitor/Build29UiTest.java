package com.colonellp.ellamonitor;

import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;

@RunWith(org.robolectric.RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="w1024dp-h600dp-land-mdpi")
public class Build29UiTest {
    @Before public void clear(){RuntimeEnvironment.getApplication().getSharedPreferences("viewer",0).edit().clear().putInt("update.intervalHours",0).commit();RuntimeEnvironment.getApplication().getSharedPreferences("chart-display",0).edit().clear().commit();ThemeStorageTest.installProvider();}
    static Object field(Object object,String name)throws Exception{java.lang.reflect.Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
    @Test public void compactButtonsHaveSeparatePaddedBoxesAndNoRowLabels()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();RenderTest.renderFixture(a);CompanionLayoutTest.layout(a,1024,600);Dialog dialog=DisplayRevisionTest.popup(DisplayRevisionTest.readout(a,"Battery [Pico]"));PresetAndColourTest.idle();
            View root=dialog.getWindow().getDecorView();assertNull(ActivityTest.text(root,"Rolling data"));assertNull(ActivityTest.text(root,"Fixed data"));TextView close=(TextView)ActivityTest.text(root,"Close"),line=(TextView)ActivityTest.text(root,"Line"),sum=(TextView)ActivityTest.text(root,"Sum");assertEquals(a.dialogDp(28),close.getLayoutParams().height);assertEquals(close.getTextSize(),line.getTextSize(),0);assertEquals(close.getTextSize(),sum.getTextSize(),0);
            ViewGroup footer=(ViewGroup)close.getParent(),footerBox=(ViewGroup)footer.getParent();assertEquals(5,footer.getChildCount());assertEquals(a.dialogDp(6),footerBox.getPaddingLeft());assertEquals(a.dialogDp(6),footerBox.getPaddingBottom());assertSame(line.getParent(),sum.getParent());ViewGroup modes=(ViewGroup)line.getParent();assertEquals(a.dialogDp(6),modes.getPaddingRight());assertEquals(a.dialogDp(6),((LinearLayout.LayoutParams)sum.getLayoutParams()).topMargin);dialog.dismiss();
        }
    }
    @Test public void valuesAndWhAreTallHeaderButtonsWithSavedIndependentState()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();RenderTest.renderFixture(a);CompanionLayoutTest.layout(a,1024,600);LiveDashboard.Readout r=DisplayRevisionTest.readout(a,"Battery [Pico]");Dialog dialog=DisplayRevisionTest.popup(r);PresetAndColourTest.idle();TextView values=(TextView)ActivityTest.text(dialog.getWindow().getDecorView(),"Values"),quantity=(TextView)ActivityTest.text(dialog.getWindow().getDecorView(),"Ah");assertEquals(a.dialogDp(40),values.getLayoutParams().height);assertEquals(quantity.getLayoutParams().height,values.getLayoutParams().height);assertTrue(values.isActivated());values.performClick();assertFalse(values.isActivated());quantity.performClick();assertEquals("Wh",quantity.getText().toString());dialog.dismiss();
            dialog=DisplayRevisionTest.popup(r);PresetAndColourTest.idle();assertFalse(ActivityTest.text(dialog.getWindow().getDecorView(),"Values").isActivated());assertNotNull(ActivityTest.text(dialog.getWindow().getDecorView(),"Wh"));assertFalse(a.viewerPreferences().contains("values"));dialog.dismiss();
        }
    }
    @Test public void monthFetchFailureRetainsARetryControlAndCloseCancelsRefresh()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();RenderTest.renderFixture(a);CompanionLayoutTest.layout(a,1024,600);Dialog dialog=DisplayRevisionTest.popup(DisplayRevisionTest.readout(a,"Battery [Pico]"));PresetAndColourTest.idle();Object popup=ActivityTest.field(a,"elementPopup");java.lang.reflect.Field error=popup.getClass().getDeclaredField("monthsError");error.setAccessible(true);error.setBoolean(popup,true);java.lang.reflect.Method update=popup.getClass().getDeclaredMethod("updateButtons");update.setAccessible(true);update.invoke(popup);TextView retry=(TextView)ActivityTest.text(dialog.getWindow().getDecorView(),"Retry months");assertNotNull(retry);assertTrue(retry.isEnabled());dialog.dismiss();PresetAndColourTest.idle();assertNull(ActivityTest.field(a,"elementPopup"));assertFalse((Boolean)field(popup,"open"));
        }
    }
    static class Capture extends android.graphics.Canvas {
        final java.util.List<String> annotations=new java.util.ArrayList<>();int labelColour;
        @Override public void drawText(String text,float x,float y,android.graphics.Paint paint){if(paint.getColor()==labelColour)annotations.add(text);}
    }
    @Test public void valuesToggleHidesOnlyBarAnnotationsAndAxesKeepTheirRole(){
        ThemeConfig theme=ThemeConfig.defaults();theme.graphAxes=0xff123456;PopupChartView graph=new PopupChartView(RuntimeEnvironment.getApplication(),theme,1);graph.layout(0,0,500,300);
        PopupSelection selection=new PopupSelection();selection.period=1;selection.fixed=true;PopupHistory.Plan plan=PopupHistory.plan(java.time.Instant.parse("2026-10-13T12:00:00Z"),selection,java.time.ZoneOffset.UTC);PopupHistory.Series data=new PopupHistory.Series();data.unit="Ah";data.points.add(new PopupHistory.Point(plan.query.from,plan.query.from.plusSeconds(3600),3.7,true));
        Capture visible=new Capture();visible.labelColour=theme.text;graph.show(plan,data,true,true,false);graph.onDraw(visible);assertEquals(java.util.Collections.singletonList("4"),visible.annotations);
        Capture hidden=new Capture();hidden.labelColour=theme.text;graph.show(plan,data,true,false,false);graph.onDraw(hidden);assertTrue(hidden.annotations.isEmpty());
    }

}
