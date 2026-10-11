package com.colonellp.ellamonitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import java.util.Locale;

final class GaugeView extends View {
    private final Palette colors;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arc = new RectF();
    private android.graphics.Typeface face;
    private Double value;
    private final String unit;
    final GaugeScale.Kind kind;
    GaugeView(Context context, Palette colors, String unit) { this(context, colors, unit, GaugeScale.Kind.DEFAULT); }
    GaugeView(Context context, Palette colors, String unit, GaugeScale.Kind kind) {
        super(context); this.colors = colors; this.unit = unit; this.kind = kind;
        this.face = android.graphics.Typeface.create(colors.fontFamily, android.graphics.Typeface.NORMAL);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }
    public GaugeView(Context context) { this(context, new Palette(false), "A"); }
    static GaugeScale.Kind kind(String slot, String title) { return GaugeScale.kind(slot, title); }
    void refreshTheme() { face=android.graphics.Typeface.create(colors.fontFamily,android.graphics.Typeface.NORMAL);invalidate(); }
    void value(Double value) { if (java.util.Objects.equals(this.value, value)) return; this.value = value; invalidate(); }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w=getWidth(),h=getHeight();
        boolean soc=kind==GaugeScale.Kind.SOC,angle=unit.equals("°"),battery=kind==GaugeScale.Kind.BATTERY;
        float diameter=Math.max(1,Math.min(w-22,soc?h-18:(h-16)*1.18f));
        float left=(w-diameter)/2,top=Math.max(8,(h-diameter)/2);
        if(angle)top=Math.max(10,(h-diameter*.7f)/2);
        arc.set(left,top,left+diameter,top+diameter);
        paint.setTypeface(face);paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(5,diameter*.07f));paint.setStrokeCap(Paint.Cap.ROUND);paint.setColor(colors.border);
        canvas.drawArc(arc,angle?180:soc?270:145,angle?180:soc?-360:250,false,paint);
        if(value!=null){
            if(soc){
                // SOC grows anticlockwise: 0=12, 25=9, 50=6, 100=12 o'clock.
                int[] bandColors={0xffff4545,0xffffbf45,0xff32dc68};float[] starts={270,180,90};
                paint.setStrokeCap(Paint.Cap.BUTT);
                for(int i=0;i<3;i++){paint.setColor(bandColors[i]);float sweep=GaugeScale.socBandSweep(value,i);if(sweep!=0)canvas.drawArc(arc,starts[i],sweep,false,paint);}
                paint.setStrokeCap(Paint.Cap.ROUND);
            }else{
                float sweep=angle?(float)Math.max(-90,Math.min(90,value*18)):GaugeScale.sweep(kind,unit,value);
                paint.setColor(kind==GaugeScale.Kind.LOAD||value<0?colors.negative:colors.positive);
                float start=angle||battery?270:145;
                if(sweep!=0)canvas.drawArc(arc,start,sweep,false,paint);
                if(angle||battery){
                    double radians=Math.toRadians(start+sweep);paint.setColor(colors.text);paint.setStrokeWidth(2);
                    canvas.drawLine(arc.centerX(),arc.centerY(),arc.centerX()+(float)Math.cos(radians)*diameter*.46f,arc.centerY()+(float)Math.sin(radians)*diameter*.46f,paint);
                }
            }
        }
        if(angle||battery||kind==GaugeScale.Kind.LOAD||kind==GaugeScale.Kind.PV){
            paint.setStyle(Paint.Style.FILL);paint.setTextSize(10);paint.setColor(colors.muted);
            float baseline=angle?top+diameter*.68f:Math.min(h-3,top+diameter*.91f);
            paint.setTextAlign(Paint.Align.LEFT);canvas.drawText(angle?"-5":battery?"-10":"0",left,baseline,paint);
            paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText(angle?"+5":battery?"+10":kind==GaugeScale.Kind.PV?"20":"10",left+diameter,baseline,paint);
        }
        paint.setStyle(Paint.Style.FILL);paint.setColor(value==null?colors.muted:colors.text);paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(face);paint.setTextSize(Math.min(30*getResources().getDisplayMetrics().scaledDensity,diameter*.24f));
        String text=value==null?"—":String.format(Locale.UK,unit.equals("hPa")||unit.equals("%")?"%.0f":"%.2f",value);
        canvas.drawText(text,w/2,top+diameter*(angle?.83f:soc?.55f:.62f),paint);
        paint.setTypeface(android.graphics.Typeface.DEFAULT);paint.setTextSize(Math.min(14*getResources().getDisplayMetrics().scaledDensity,diameter*.14f));paint.setColor(colors.muted);
        canvas.drawText(value==null?"":unit,w/2,top+diameter*(angle?.98f:soc?.73f:.80f),paint);
    }
}
