package com.colonellp.ellamonitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/** Vector artwork and state borders adapted from the owner's EQ & DSP DashboardView. */
final class EqIconButton extends View {
    private final ThemeConfig t;
    private final String kind;
    private final boolean selected, fullscreen;
    private boolean changed;
    void changed(boolean value){changed=value;invalidate();}
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    EqIconButton(Context c, ThemeConfig theme, String kind, String label, boolean selected, boolean fullscreen, Runnable action) {
        super(c); t = theme; this.kind = kind; this.selected = selected; this.fullscreen = fullscreen;
        setContentDescription(label); setFocusable(true); setClickable(true); setOnClickListener(v -> action.run());
    }
    @Override protected void onDraw(Canvas c) {
        RectF r = new RectF(.8f, .8f, getWidth() - .8f, getHeight() - .8f);
        p.setStyle(Paint.Style.FILL); p.setColor(t.buttonBackground); c.drawRoundRect(r, 5, 5, p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(selected ? 1.45f : 1.1f); p.setColor(changed?t.changedIndicator:selected || kind.equals("settings") ? t.controls : t.offButtonBorder); c.drawRoundRect(r, 5, 5, p);
        int icon = changed?t.changedIndicator:selected ? t.controls : ThemeConfig.blend(t.controls, t.buttonBackground, .48f);
        float cx = r.centerX(), cy = r.centerY();
        if (kind.equals("screen")) {
            p.setColor(icon); p.setStrokeWidth(2.1f); p.setStrokeCap(Paint.Cap.SQUARE); float d = 9, a = 5;
            for (int x : new int[]{-1, 1}) for (int y : new int[]{-1, 1}) { c.drawLine(cx+x*d,cy+y*d,cx+x*(d-a),cy+y*d,p); c.drawLine(cx+x*d,cy+y*d,cx+x*d,cy+y*(d-a),p); }
        } else if (kind.equals("keep")) {
            p.setColor(icon); p.setStyle(selected ? Paint.Style.FILL : Paint.Style.STROKE); p.setStrokeWidth(2.1f); c.drawCircle(cx, cy, selected ? 9 : 8, p);
        } else if (kind.equals("lock")) {
            float s = 1; cy += 2*s; p.setStyle(Paint.Style.FILL); p.setColor(icon);
            c.drawRoundRect(new RectF(cx-9.5f*s,cy-1.5f*s,cx+9.5f*s,cy+11.5f*s),3.3f,3.3f,p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeWidth(4*s);
            c.drawArc(new RectF(cx-7*s,cy-14.5f*s,cx+7*s,cy+.5f*s),180,selected?180:143,false,p);
            c.drawLine(cx-7*s,cy-7*s,cx-7*s,cy-s,p); if(selected)c.drawLine(cx+7*s,cy-7*s,cx+7*s,cy-s,p);
            p.setStyle(Paint.Style.FILL); p.setColor(t.buttonBackground);c.drawCircle(cx,cy+3.4f*s,2.1f*s,p);c.drawRoundRect(new RectF(cx-1.05f*s,cy+3.3f*s,cx+1.05f*s,cy+8.4f*s),1,1,p);
        } else {
            float s = 1; Path gear = new Path();
            for(int i=0;i<40;i++){double a=-Math.PI/2+i*Math.PI*2/40;int phase=i&3;float radius=(phase==1||phase==2?15.5f:12.3f)*s;float x=cx+(float)Math.cos(a)*radius,y=cy+(float)Math.sin(a)*radius;if(i==0)gear.moveTo(x,y);else gear.lineTo(x,y);}gear.close();
            p.setStyle(Paint.Style.FILL);p.setColor(t.controls);c.drawPath(gear,p);c.drawCircle(cx,cy,10.7f*s,p);p.setColor(t.buttonBackground);c.drawCircle(cx,cy,6*s,p);
        }
        p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.ROUND);
    }
}
