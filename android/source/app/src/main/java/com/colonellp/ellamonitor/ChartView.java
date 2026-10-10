package com.colonellp.ellamonitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class ChartView extends View {
    interface Inspect { void show(HistoryData.Record row); }
    private final Palette colors;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private String forwardKey = "forwardWh", reverseKey = "reverseWh";
    private HistoryData.Result data;
    private String quantity = "watts";
    private boolean bars, local;
    private Inspect inspect;
    ChartView(Context context, Palette colors) { super(context); this.colors = colors; setContentDescription("History chart. Tap a period to inspect its values and coverage."); setFocusable(true); }
    public ChartView(Context context) { this(context, new Palette(false)); }
    void show(HistoryData.Result data, String quantity, boolean bars, boolean local, Inspect inspect) {
        this.data = data; this.quantity = quantity; this.bars = bars; this.local = local; this.inspect = inspect; forwardKey = "forward" + quantity; reverseKey = "reverse" + quantity; invalidate();
    }
    private double milliseconds(java.time.Instant value) { return value.toEpochMilli(); }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas); float density = getResources().getDisplayMetrics().density;
        float left = 65 * density, right = getWidth() - 18 * density, top = 24 * density, bottom = getHeight() - 38 * density;
        paint.setTextSize(12 * getResources().getDisplayMetrics().scaledDensity); paint.setStrokeWidth(density); paint.setStyle(Paint.Style.FILL);
        if (data == null || data.rows.isEmpty()) { paint.setColor(colors.muted); paint.setTextAlign(Paint.Align.CENTER); canvas.drawText("No recorded data in this period", getWidth() / 2f, getHeight() / 2f, paint); return; }
        double min = 0, max = 0; boolean found = false;
        for (HistoryData.Record row : data.rows) {
            Double v = row.value(quantity, data.metric.kind); if (v != null) { min = found ? Math.min(min, v) : v; max = found ? Math.max(max, v) : v; found = true; }
            if (bars) {
                Double forward = MonitorData.number(row.raw, forwardKey), reverse = MonitorData.number(row.raw, reverseKey);
                if (forward != null) { min = Math.min(min, -forward); max = Math.max(max, forward); }
                if (reverse != null) { min = Math.min(min, -reverse); max = Math.max(max, reverse); }
            }
        }
        if (bars) { min = Math.min(0, min); max = Math.max(0, max); }
        if (!found) { paint.setColor(colors.muted); paint.setTextAlign(Paint.Align.CENTER); canvas.drawText("—", getWidth() / 2f, getHeight() / 2f, paint); return; }
        double padding = Math.max(.1, (max - min) * .1); min -= padding; max += padding;
        double start = milliseconds(data.window.from), span = milliseconds(data.window.to) - start;
        for (int i = 0; i <= 4; i++) {
            float y = top + (bottom - top) * i / 4; paint.setColor(colors.border); canvas.drawLine(left, y, right, y, paint);
            paint.setColor(colors.muted); paint.setTextAlign(Paint.Align.RIGHT); canvas.drawText(String.format(Locale.UK, "%.1f", max - (max - min) * i / 4), left - 8 * density, y + 4 * density, paint);
        }
        canvas.save(); canvas.clipRect(left, top, right, bottom);
        path.reset(); boolean connected = false; java.time.Instant previousEnd = null;
        for (HistoryData.Record row : data.rows) {
            Double value = row.value(quantity, data.metric.kind), coverage = row.coverage(quantity, data.metric.kind);
            float x = left + (float)((milliseconds(row.start) + milliseconds(row.end)) / 2 - start) / (float)span * (right - left);
            if (value == null || (coverage != null && coverage <= 0)) { connected = false; previousEnd = null; continue; }
            float y = bottom - (float)((value - min) / (max - min)) * (bottom - top);
            if (bars) {
                float a = left + (float)(milliseconds(row.start) - start) / (float)span * (right - left), b = left + (float)(milliseconds(row.end) - start) / (float)span * (right - left);
                float zero = bottom - (float)((0 - min) / (max - min)) * (bottom - top);
                Double forward = MonitorData.number(row.raw, forwardKey), reverse = MonitorData.number(row.raw, reverseKey);
                if (forward != null && reverse != null) {
                    boolean load = data.metric.role.equals("load"); double incoming = load ? reverse : forward, outgoing = load ? forward : reverse;
                    paint.setColor(colors.positive); canvas.drawRect(a + density, bottom - (float)((incoming - min) / (max - min)) * (bottom - top), Math.max(a + 2 * density, b - density), zero, paint);
                    paint.setColor(colors.negative); canvas.drawRect(a + density, zero, Math.max(a + 2 * density, b - density), bottom - (float)((-outgoing - min) / (max - min)) * (bottom - top), paint);
                } else {
                    // Net-only bars remain explicitly unclassified, retaining the original sign.
                    paint.setColor(colors.muted); canvas.drawRect(a + density, Math.min(y, zero), Math.max(a + 2 * density, b - density), Math.max(y, zero), paint);
                }
            } else {
                boolean complete = coverage == null || coverage >= (milliseconds(row.end) - milliseconds(row.start)) / 1000 - .01;
                if (!connected || previousEnd == null || !previousEnd.equals(row.start) || !complete) path.moveTo(x, y); else path.lineTo(x, y);
                paint.setColor(row.partial || !complete ? colors.negative : colors.accent); canvas.drawCircle(x, y, 2 * density, paint);
                connected = complete; previousEnd = row.end;
            }
        }
        if (!bars) { paint.setColor(colors.accent); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(2 * density); canvas.drawPath(path, paint); paint.setStyle(Paint.Style.FILL); }
        canvas.restore();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd MMM HH:mm").withZone(local ? ZoneId.systemDefault() : ZoneOffset.UTC);
        paint.setColor(colors.muted); paint.setTextAlign(Paint.Align.LEFT); canvas.drawText(format.format(data.window.from), left, bottom + 25 * density, paint);
        paint.setTextAlign(Paint.Align.RIGHT); canvas.drawText(format.format(data.window.to) + (local ? " local" : " UTC"), right, bottom + 25 * density, paint);
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            performClick(); if (data == null || inspect == null || data.rows.isEmpty()) return true;
            float d = getResources().getDisplayMetrics().density;
            double fraction = Math.max(0, Math.min(1, (event.getX() - 65 * d) / Math.max(1, getWidth() - 83 * d)));
            double time = data.window.from.toEpochMilli() + fraction * (data.window.to.toEpochMilli() - data.window.from.toEpochMilli());
            // A tap in an omitted interval must not pretend the nearest bucket covers the gap.
            for (HistoryData.Record row : data.rows) if (time >= row.start.toEpochMilli() && time < row.end.toEpochMilli()) { inspect.show(row); break; }
            return true;
        }
        return true;
    }
    @Override public boolean performClick() { super.performClick(); return true; }
}
