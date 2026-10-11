package com.colonellp.ellamonitor;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/** Density-independent logical checkbox geometry; CheckBox retains its native semantics. */
final class CheckboxDrawable extends Drawable {
    private final ThemeConfig theme;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int size;
    CheckboxDrawable(ThemeConfig theme) { this(theme,24); }
    CheckboxDrawable(ThemeConfig theme,int size) { this.theme = theme;this.size=size; }
    @Override public int getIntrinsicWidth() { return size; }
    @Override public int getIntrinsicHeight() { return size; }
    @Override public boolean isStateful() { return true; }
    @Override protected boolean onStateChange(int[] state) { invalidateSelf(); return true; }
    @Override public void draw(Canvas canvas) {
        canvas.save();canvas.translate(getBounds().left,getBounds().top);
        canvas.scale(getBounds().width()/24f,getBounds().height()/24f);
        boolean checked=false;for(int state:getState())if(state==android.R.attr.state_checked)checked=true;
        RectF box=new RectF(3,3,21,21);
        paint.setStyle(Paint.Style.FILL);paint.setColor(theme.buttonBackground);canvas.drawRoundRect(box,3,3,paint);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.5f);paint.setColor(checked?theme.controls:theme.offButtonBorder);canvas.drawRoundRect(box,3,3,paint);
        if(checked){paint.setStrokeWidth(2);paint.setStrokeCap(Paint.Cap.ROUND);canvas.drawLine(7,12,10.5f,16,paint);canvas.drawLine(10.5f,16,17,8,paint);}
        canvas.restore();
    }
    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha);invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter);invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
