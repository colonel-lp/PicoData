package com.colonellp.ellamonitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

/** The former Settings theme panel, presented as a dashboard popup. */
final class ThemeAppearanceView extends View {
    static final float BASE_W = 544f;
    static final float BASE_H = 560f;

    private final MainActivity host;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float uiScaleX = 1f, uiScaleY = 1f;
    private final RectF panel = new RectF(0, 0, BASE_W, BASE_H);
    private final String[] colourFields = {
        ThemeConfig.BACKGROUND, ThemeConfig.TEXT,
        ThemeConfig.PANEL, ThemeConfig.BORDER,
        ThemeConfig.TITLE_TEXT, ThemeConfig.TITLE_BACKGROUND,
        ThemeConfig.TITLE_OUTLINE, ThemeConfig.CHANGED_INDICATOR,
        ThemeConfig.INDICATOR_BACKGROUND, ThemeConfig.VOLTAGES_BACKGROUND,
        ThemeConfig.GAUGE_PANEL_1, ThemeConfig.GAUGE_OUTLINE_1,
        ThemeConfig.GAUGE_PANEL_2, ThemeConfig.GAUGE_OUTLINE_2,
        ThemeConfig.FRONT_EQ, ThemeConfig.REAR_EQ,
        ThemeConfig.HIGHLIGHT_1, ThemeConfig.HIGHLIGHT_2,
        ThemeConfig.BUTTON_BACKGROUND, null,
        ThemeConfig.BUTTON_TEXT_ON, ThemeConfig.CONTROLS,
        ThemeConfig.BUTTON_TEXT_OFF, ThemeConfig.OFF_BUTTON_BORDER
    };
    private final String[] colourLabels = {
        "BACKGROUND", "TEXT", "PANELS", "BORDERS", "TITLE TEXT", "TITLE BACKGROUND",
        "TITLE OUTLINE", "CHANGED INDICATOR", "INDICATOR BACKGROUND", "VOLTAGES BACKGROUND",
        "GAUGE PANEL 1", "GAUGE PANEL 1 OUTLINE", "GAUGE PANEL 2", "GAUGE PANEL 2 OUTLINE",
        "GAUGE POSITIVE", "GAUGE NEGATIVE", "HIGHLIGHT 1", "HIGHLIGHT 2", "BUTTON BACKGROUND", "",
        "BUTTON ON TEXT", "BUTTON ON BORDER", "BUTTON OFF TEXT", "BUTTON OFF BORDER"
    };
    private final RectF[] colourButtons = new RectF[colourFields.length];
    private final RectF backButton = new RectF(20, 508, 518, 548);

    ThemeAppearanceView(Context context, MainActivity host) {
        super(context);
        this.host = host;
        stroke.setStyle(Paint.Style.STROKE);
        float left = 20, top = 40, w = 240, h = 33, gapX = 18, gapY = 6;
        for (int i = 0; i < colourButtons.length; i++) {
            int col = i % 2, row = i / 2;
            float x = left + col * (w + gapX), y = top + row * (h + gapY);
            colourButtons[i] = new RectF(x, y, x + w, y + h);
        }
        setFocusable(true);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = Math.max(1, getResources().getDisplayMetrics().widthPixels - dp(30));
        int desiredWidth = Math.min(dp(560), maxWidth);
        int measuredWidth = resolveSize(desiredWidth, widthMeasureSpec);
        int desiredHeight = Math.round(measuredWidth * BASE_H / BASE_W);
        setMeasuredDimension(measuredWidth, resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        uiScaleX = uiScaleY = Math.max(0.0001f, Math.min(w / BASE_W, h / BASE_H));
    }

    float getUiScaleX() { return uiScaleX; }
    float getUiScaleY() { return uiScaleY; }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        ThemeConfig t = host.getThemeConfig();
        canvas.save();
        canvas.scale(uiScaleX, uiScaleY);
        drawPanel(canvas, panel, t);
        text(canvas, "THEME COLOURS", BASE_W * 0.5f, 28, 14.5f, t.buttonTextOn, Paint.Align.CENTER);
        for (int i = 0; i < colourButtons.length; i++) {
            if (colourFields[i] == null) drawEmptyButton(canvas, colourButtons[i], t);
            else drawColourButton(canvas, colourButtons[i], colourLabels[i], t.get(colourFields[i]), t);
        }
        drawSelector(canvas, backButton, "BACK", t, Paint.Align.CENTER);
        canvas.restore();
    }

    private void drawPanel(Canvas c, RectF r, ThemeConfig t) {
        p.setStyle(Paint.Style.FILL); p.setColor(t.panel); c.drawRoundRect(r, 8, 8, p);
        outlineRoundRect(c, r, 8f, 1.2f, t.border);
    }

    private void drawColourButton(Canvas c, RectF r, String label, int color, ThemeConfig t) {
        p.setStyle(Paint.Style.FILL); p.setColor(t.buttonBackground); c.drawRoundRect(r, 6, 6, p);
        outlineRoundRect(c, r, 6f, 1.15f, t.border);
        RectF swatch = new RectF(r.left + 9, r.top + 6, r.left + 39, r.bottom - 6);
        p.setColor(color); c.drawRoundRect(swatch, 5, 5, p);
        stroke.setColor(ThemeConfig.blend(t.text, t.background, 0.35f)); stroke.setStrokeWidth(0.9f);
        c.drawRoundRect(swatch, 5, 5, stroke);
        centredText(c, label, r.left + 48, r.centerY(), 10.8f, t.buttonTextOn, Paint.Align.LEFT);
        centredText(c, hex(color), r.right - 8, r.centerY(), 10.8f, t.buttonTextOn, Paint.Align.RIGHT);
    }

    private void drawEmptyButton(Canvas c, RectF r, ThemeConfig t) {
        p.setStyle(Paint.Style.FILL); p.setColor(t.buttonBackground); c.drawRoundRect(r, 6, 6, p);
        outlineRoundRect(c, r, 6f, 1.15f, t.border);
    }

    private void drawSelector(Canvas c, RectF r, String label, ThemeConfig t, Paint.Align align) {
        p.setStyle(Paint.Style.FILL); p.setColor(t.buttonBackground); c.drawRoundRect(r, 6, 6, p);
        outlineRoundRect(c, r, 6f, 1.2f, t.controls);
        float x = align == Paint.Align.LEFT ? r.left + 12f : r.centerX();
        centredText(c, label, x, r.centerY(), 12.8f, t.buttonTextOn, align);
    }

    private void outlineRoundRect(Canvas c, RectF r, float radius, float width, int color) {
        RectF rr = new RectF(r); float inset = Math.max(0.5f, width * 0.5f); rr.inset(inset, inset);
        stroke.setStyle(Paint.Style.STROKE); stroke.setStrokeWidth(width); stroke.setColor(color);
        c.drawRoundRect(rr, Math.max(0f, radius - inset), Math.max(0f, radius - inset), stroke);
    }

    private void text(Canvas c, String s, float x, float y, float size, int color, Paint.Align align) {
        float readableSize = size * (host.isFullscreenMode() ? 1f : 1.08f);
        p.setStyle(Paint.Style.FILL); p.setTypeface(host.getUiTypeface()); p.setTextSize(readableSize);
        p.setTextAlign(align); p.setColor(color); c.drawText(s == null ? "" : s, x, y, p);
    }

    private void centredText(Canvas c, String s, float x, float centreY,
                             float size, int color, Paint.Align align) {
        float readableSize = size * (host.isFullscreenMode() ? 1f : 1.08f);
        p.setStyle(Paint.Style.FILL); p.setTypeface(host.getUiTypeface()); p.setTextSize(readableSize);
        p.setTextAlign(align); p.setColor(color);
        Paint.FontMetrics fm = p.getFontMetrics();
        c.drawText(s == null ? "" : s, x, centreY - (fm.ascent + fm.descent) * 0.5f, p);
    }

    @Override public boolean performClick() { super.performClick(); return true; }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_UP) return true;
        performClick();
        float x = event.getX() / Math.max(0.0001f, uiScaleX);
        float y = event.getY() / Math.max(0.0001f, uiScaleY);
        for (int i = 0; i < colourButtons.length; i++) {
            if (colourFields[i] != null && colourButtons[i].contains(x, y)) {
                host.editThemeAppearanceColour(colourFields[i], colourLabels[i]);
                return true;
            }
        }
        if (backButton.contains(x, y)) { host.dismissThemeAppearanceDialog(); return true; }
        return true;
    }

    private int dp(float value) {
        return host.dialogDp(value);
    }

    private static String hex(int c) {
        return String.format(java.util.Locale.US, "#%02X%02X%02X", Color.red(c), Color.green(c), Color.blue(c));
    }
}
