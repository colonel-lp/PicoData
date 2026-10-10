package com.colonellp.ellamonitor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

final class ColorWheelView extends View {
    interface Listener { void onColorChanged(int color); }

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap wheel;
    private final float[] fullValueHsv = new float[3];
    private final RectF thumbRect = new RectF();
    private final RectF valueHitRect = new RectF();
    private final float[] hsv = new float[]{180f, 1f, 1f};
    private Listener listener;
    private int activeDrag = 0;
    private RectF wheelRect = new RectF();
    private RectF valueRect = new RectF();

    ColorWheelView(Context context, int initialColor) {
        super(context);
        Color.colorToHSV(initialColor, hsv);
        stroke.setStyle(Paint.Style.STROKE);
        setFocusable(true);
    }

    void setListener(Listener l) { listener = l; }
    void setColor(int color) {
        Color.colorToHSV(color, hsv);
        invalidate();
        if (listener != null) listener.onColorChanged(getColor());
    }
    int getColor() { return Color.HSVToColor(hsv); }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rebuildWheel();
    }

    private void rebuildWheel() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;
        float margin = dp(8);
        float barH = dp(32);
        float gap = dp(10);
        float size = Math.min(w - margin * 2f, h - margin * 2f - barH - gap);
        size = Math.max(dp(80), size);
        float left = (w - size) / 2f;
        wheelRect.set(left, margin, left + size, margin + size);
        valueRect.set(margin, wheelRect.bottom + gap, w - margin, wheelRect.bottom + gap + barH);

        int bw = Math.max(1, Math.round(size));
        int bh = bw;
        int[] pixels = new int[bw * bh];
        float cx = (bw - 1) / 2f;
        float cy = (bh - 1) / 2f;
        float r = Math.min(cx, cy);
        float[] local = new float[3];
        local[2] = 1f;
        for (int y = 0; y < bh; y++) {
            float dy = y - cy;
            for (int x = 0; x < bw; x++) {
                float dx = x - cx;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                int index = y * bw + x;
                if (dist > r) {
                    pixels[index] = Color.TRANSPARENT;
                    continue;
                }
                float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
                if (angle < 0) angle += 360f;
                local[0] = angle;
                local[1] = Math.min(1f, dist / r);
                pixels[index] = Color.HSVToColor(local);
            }
        }
        wheel = Bitmap.createBitmap(pixels, bw, bh, Bitmap.Config.ARGB_8888);
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (wheel == null) rebuildWheel();
        if (wheel != null) c.drawBitmap(wheel, null, wheelRect, p);

        float rad = wheelRect.width() / 2f;
        float angle = (float) Math.toRadians(hsv[0]);
        float rr = rad * hsv[1];
        float mx = wheelRect.centerX() + (float) Math.cos(angle) * rr;
        float my = wheelRect.centerY() + (float) Math.sin(angle) * rr;
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);
        c.drawCircle(mx, my, dp(7), p);
        fullValueHsv[0]=hsv[0]; fullValueHsv[1]=hsv[1]; fullValueHsv[2]=1f;
        p.setColor(Color.HSVToColor(fullValueHsv));
        c.drawCircle(mx, my, dp(4.5f), p);

        int full = Color.HSVToColor(fullValueHsv);
        p.setShader(new LinearGradient(valueRect.left, 0, valueRect.right, 0,
                Color.BLACK, full, Shader.TileMode.CLAMP));
        c.drawRoundRect(valueRect, dp(5), dp(5), p);
        p.setShader(null);
        stroke.setStrokeWidth(dp(1.0f));
        stroke.setColor(Color.rgb(170, 190, 200));
        c.drawRoundRect(valueRect, dp(5), dp(5), stroke);
        float vx = valueRect.left + hsv[2] * valueRect.width();
        p.setColor(Color.WHITE);
        thumbRect.set(vx - dp(6), valueRect.top - dp(5), vx + dp(6), valueRect.bottom + dp(5));
        c.drawRoundRect(thumbRect, dp(2), dp(2), p);
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (wheelRect.contains(x, y)) activeDrag = 1;
                else if (expandedValueHit().contains(x, y)) activeDrag = 2;
                else return true;
                update(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (activeDrag != 0) update(x, y);
                return true;
            case MotionEvent.ACTION_UP:
                if (activeDrag != 0) update(x, y);
                activeDrag = 0;
                performClick();
                return true;
            case MotionEvent.ACTION_CANCEL:
                activeDrag = 0;
                return true;
        }
        return super.onTouchEvent(e);
    }

    @Override public boolean performClick() {
        super.performClick();
        return true;
    }

    private RectF expandedValueHit() {
        RectF hit = valueHitRect; hit.set(valueRect);
        hit.inset(0, -dp(14));
        return hit;
    }

    private void update(float x, float y) {
        if (activeDrag == 1) {
            float dx = x - wheelRect.centerX();
            float dy = y - wheelRect.centerY();
            float rad = wheelRect.width() / 2f;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
            if (angle < 0) angle += 360f;
            hsv[0] = angle;
            hsv[1] = Math.max(0f, Math.min(1f, dist / rad));
        } else if (activeDrag == 2) {
            hsv[2] = Math.max(0f, Math.min(1f, (x - valueRect.left) / Math.max(1f, valueRect.width())));
        }
        invalidate();
        if (listener != null) listener.onColorChanged(getColor());
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
