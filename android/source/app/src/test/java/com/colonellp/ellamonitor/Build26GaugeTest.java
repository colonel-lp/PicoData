package com.colonellp.ellamonitor;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35)
public class Build26GaugeTest {
    static class Text {String value;float x,y,size;Text(String text,float x,float y,Paint paint){value=text;this.x=x;this.y=y;size=paint.getTextSize();}}
    static class Arc {float start,sweep;int colour;Arc(float start,float sweep,Paint paint){this.start=start;this.sweep=sweep;colour=paint.getColor();}}
    static class Capture extends Canvas {
        List<Text> text=new ArrayList<>();List<Arc> arcs=new ArrayList<>();int lines;
        @Override public void drawText(String value,float x,float y,Paint p){text.add(new Text(value,x,y,p));}
        @Override public void drawArc(RectF rect,float start,float sweep,boolean centre,Paint p){arcs.add(new Arc(start,sweep,p));}
        @Override public void drawLine(float a,float b,float c,float d,Paint p){lines++;}
        Text named(String label){return text.stream().filter(t->label.equals(t.value)).findFirst().get();}
    }
    static Capture draw(GaugeScale.Kind kind,String unit,Double value){GaugeView gauge=new GaugeView(RuntimeEnvironment.getApplication(),new Palette(false),unit,kind);gauge.layout(0,0,150,140);gauge.value(value);Capture canvas=new Capture();gauge.onDraw(canvas);return canvas;}
    @Test public void batteryHasNoPointerPitchRetainsItAndAmpsShareLimitBaseline(){
        for(GaugeScale.Kind kind:new GaugeScale.Kind[]{GaugeScale.Kind.LOAD,GaugeScale.Kind.PV,GaugeScale.Kind.BATTERY}){
            Capture c=draw(kind,"A",1d);assertEquals(0,c.lines);assertEquals(75,c.named("amps").x,0);assertEquals(c.text.get(0).y,c.named("amps").y,0);assertEquals(c.text.get(1).y,c.named("amps").y,0);assertEquals(130.48,c.named("amps").y,.01);assertEquals(81.36,c.named("1.00").y,.01);
        }
        Capture pitch=draw(GaugeScale.Kind.DEFAULT,"°",1d);assertEquals(1,pitch.lines);assertTrue(pitch.named("1.00").size<draw(GaugeScale.Kind.BATTERY,"A",1d).named("1.00").size);
        assertEquals(pitch.named("-5").y,pitch.named("+5").y,0);
    }
    @Test public void socUsesOneAnticlockwiseRemainingArcAtExactUnroundedThresholds(){
        for(double value:new double[]{100,50,49.9,25,24.9}){
            Capture c=draw(GaugeScale.Kind.SOC,"%",value);assertEquals(2,c.arcs.size());Arc remaining=c.arcs.get(1);assertEquals(270,remaining.start,0);assertEquals(-value*3.6,remaining.sweep,.001);assertEquals(value>=50?0xff32dc68:value>=25?0xffffbf45:0xffff4545,remaining.colour);
        }
        assertEquals(1,draw(GaugeScale.Kind.SOC,"%",0d).arcs.size());assertEquals(1,draw(GaugeScale.Kind.SOC,"%",null).arcs.size());
    }
    @Test public void environmentalValuesMoveUpWithoutChangingTheirUnitsOrUnavailableDashes(){
        for(String unit:new String[]{"hPa","%","l"}){Capture c=draw(GaugeScale.Kind.DEFAULT,unit,10d);Text number=c.named(unit.equals("hPa")||unit.equals("%")?"10":"10.00");assertEquals(81.36,number.y,.01);assertEquals(110.4,c.named(unit).y,.01);}
        assertEquals("—",draw(GaugeScale.Kind.DEFAULT,"hPa",null).text.get(0).value);
    }
}
