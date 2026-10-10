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
    GaugeView(Context context, Palette colors, String unit) { super(context); this.colors = colors; this.unit = unit; this.face = android.graphics.Typeface.create(colors.fontFamily, android.graphics.Typeface.NORMAL); setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO); }
    public GaugeView(Context context) { this(context, new Palette(false), "A"); }
    void refreshTheme() { face=android.graphics.Typeface.create(colors.fontFamily,android.graphics.Typeface.NORMAL);invalidate(); }
    void value(Double value) { if (java.util.Objects.equals(this.value, value)) return; this.value = value; invalidate(); }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight(), diameter = Math.max(1, Math.min(w - 22, (h - 16) * 1.18f)), left = (w - diameter) / 2, top = Math.max(8, (h - diameter) / 2);
        arc.set(left, top, left + diameter, top + diameter);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(Math.max(5, diameter * .07f)); paint.setStrokeCap(Paint.Cap.ROUND);
        boolean angle=unit.equals("°");
        if(angle){
            arc.set(left, Math.max(10,(h-diameter*.7f)/2),left+diameter,Math.max(10,(h-diameter*.7f)/2)+diameter);
            top=arc.top;paint.setColor(colors.border);canvas.drawArc(arc,180,180,false,paint);
            if(value!=null){float sweep=(float)Math.max(-90,Math.min(90,value*18));paint.setColor(value<0?colors.negative:colors.positive);if(sweep!=0)canvas.drawArc(arc,270,sweep,false,paint);
                double radians=Math.toRadians(270+sweep);float cx=arc.centerX(),cy=arc.centerY();paint.setColor(colors.text);paint.setStrokeWidth(2);canvas.drawLine(cx,cy,cx+(float)Math.cos(radians)*diameter*.46f,cy+(float)Math.sin(radians)*diameter*.46f,paint);
            }
            paint.setStyle(Paint.Style.FILL);paint.setTextSize(10);paint.setColor(colors.muted);paint.setTextAlign(Paint.Align.LEFT);canvas.drawText("-5",left,top+diameter*.68f,paint);paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText("+5",left+diameter,top+diameter*.68f,paint);
        }else{paint.setColor(colors.border); canvas.drawArc(arc, 145, 250, false, paint);}
        if (value != null && !angle) {
            double minimum = unit.equals("hPa") ? 950 : unit.equals("%") ? 0 : unit.equals("°") ? -15 : -Math.max(20, Math.ceil(Math.abs(value) / 10) * 10);
            double maximum = unit.equals("hPa") ? 1050 : unit.equals("%") ? 100 : unit.equals("°") ? 15 : -minimum;
            float fraction = (float)Math.max(0, Math.min(1, (value - minimum) / (maximum - minimum)));
            paint.setColor(value < 0 ? colors.negative : colors.positive); canvas.drawArc(arc, 145, Math.max(1, fraction * 250), false, paint);
        }
        paint.setStyle(Paint.Style.FILL); paint.setColor(value == null ? colors.muted : colors.text); paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(face); paint.setTextSize(Math.min(30 * getResources().getDisplayMetrics().scaledDensity, diameter * .24f));
        String text = value == null ? "—" : String.format(Locale.UK, unit.equals("hPa") || unit.equals("%") ? "%.0f" : "%.2f", value);
        canvas.drawText(text, w / 2, top + diameter * (angle?.83f:.62f), paint);
        paint.setTypeface(android.graphics.Typeface.DEFAULT); paint.setTextSize(Math.min(14 * getResources().getDisplayMetrics().scaledDensity, diameter * .14f)); paint.setColor(colors.muted);
        canvas.drawText(value == null ? "" : unit, w / 2, top + diameter * (angle?.98f:.80f), paint);
    }
}

