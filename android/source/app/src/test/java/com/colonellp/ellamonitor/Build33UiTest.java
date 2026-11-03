package com.colonellp.ellamonitor;

import android.app.Dialog;
import android.graphics.*;
import java.time.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE,qualifiers="w1024dp-h600dp-land-mdpi")
public class Build33UiTest {
    static class Capture extends Canvas {
        final Map<Integer,Integer> lines=new HashMap<>(),points=new HashMap<>();
        @Override public void drawLine(float x,float y,float x2,float y2,Paint p){lines.put(p.getColor(),lines.getOrDefault(p.getColor(),0)+1);}
        @Override public void drawCircle(float x,float y,float radius,Paint p){points.put(p.getColor(),points.getOrDefault(p.getColor(),0)+1);}
    }
    private PopupHistory.Plan plan(){PopupSelection s=new PopupSelection();s.period=1;s.fixed=true;return PopupHistory.plan(Instant.parse("2026-10-14T12:00:00Z"),s,ZoneOffset.UTC);}
    @Test public void disconnectedAndPartialPointsAreJoinedAndMarkersUseTheirTraceColours(){
        PopupHistory.Plan p=plan();ThemeConfig t=ThemeConfig.defaults();t.frontEq=0xff12ab34;t.rearEq=0xffba3421;t.graphAxes=0xff112233;
        PopupHistory.Series charge=new PopupHistory.Series(),draw=new PopupHistory.Series();charge.unit="Ah";charge.batterySplit=true;charge.companion=draw;
        for(int hour:new int[]{0,3,7}){Instant start=p.query.from.plusSeconds(3600L*hour);charge.points.add(new PopupHistory.Point(start,start.plusSeconds(1800),2,false));draw.points.add(new PopupHistory.Point(start,start.plusSeconds(1800),-3,false));}
        PopupChartView graph=new PopupChartView(RuntimeEnvironment.getApplication(),t,1);graph.layout(0,0,1008,280);graph.show(p,charge,false);Capture canvas=new Capture();graph.onDraw(canvas);
        assertEquals(Integer.valueOf(2),canvas.lines.get(t.frontEq));assertEquals(Integer.valueOf(2),canvas.lines.get(t.rearEq));assertEquals(Integer.valueOf(3),canvas.points.get(t.frontEq));assertEquals(Integer.valueOf(3),canvas.points.get(t.rearEq));assertNull(canvas.points.get(t.graphAxes));
    }
    @Test public void temperatureExtremaJoinTheirOwnPreviousValidPoint(){
        PopupHistory.Plan p=plan();ThemeConfig t=ThemeConfig.defaults();PopupHistory.Series s=new PopupHistory.Series();s.unit="°C";s.temperature=true;
        for(int i=0;i<3;i++){Instant start=p.query.from.plusSeconds(3600L*i);s.points.add(new PopupHistory.Point(start,start.plusSeconds(3600),start.plusSeconds(3600),5,false,i==1?null:-2d,5d));}
        PopupChartView graph=new PopupChartView(RuntimeEnvironment.getApplication(),t,1);graph.layout(0,0,1008,280);graph.show(p,s,false);Capture canvas=new Capture();graph.onDraw(canvas);assertEquals(Integer.valueOf(1),canvas.lines.get(t.rearEq));assertEquals(Integer.valueOf(2),canvas.lines.get(t.frontEq));
    }
    @Test public void chartPopupUsesDashboardWidthAndOtherDialogsKeepTheirWidth()throws Exception{
        RuntimeEnvironment.getApplication().getSharedPreferences("viewer",0).edit().clear().putInt("update.intervalHours",0).commit();ThemeStorageTest.installProvider();
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();RenderTest.renderFixture(a);CompanionLayoutTest.layout(a,1024,600);Dialog popup=DisplayRevisionTest.popup(DisplayRevisionTest.readout(a,"Battery [Pico]"));PresetAndColourTest.idle();assertEquals(1008,popup.getWindow().getAttributes().width);popup.dismiss();
            Dialog other=new Dialog(a);a.showStyledDialog(other,a.styledDialogRoot(),350);assertEquals(350,other.getWindow().getAttributes().width);other.dismiss();
        }
    }
    @Test @Config(qualifiers="w480dp-h270dp-land-mdpi") public void phonePopupFitsAsymmetricInsets()throws Exception{
        RuntimeEnvironment.getApplication().getSharedPreferences("viewer",0).edit().clear().putInt("update.intervalHours",0).commit();ThemeStorageTest.installProvider();
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();RenderTest.renderFixture(a);android.view.View viewport=(android.view.View)ActivityTest.field(a,"viewport");viewport.setPadding(12,8,20,14);CompanionLayoutTest.layout(a,480,270);
            Dialog popup=DisplayRevisionTest.popup(DisplayRevisionTest.readout(a,"Battery [Pico]"));PresetAndColourTest.idle();assertEquals(Math.min(a.dialogDp(1008),480-32-a.dialogDp(16)),popup.getWindow().getAttributes().width);assertEquals(-4,popup.getWindow().getAttributes().x);assertEquals(-3,popup.getWindow().getAttributes().y);assertTrue(popup.getWindow().getAttributes().height<=270-22-a.dialogDp(24));popup.dismiss();
        }
    }

}
