package com.microsoft.maui;

import android.content.Context;
import android.graphics.Paint;
import android.text.style.LineHeightSpan;

/* JADX INFO: loaded from: classes2.dex */
public class PlatformLineHeightSpan implements LineHeightSpan {
    private final float relativeLineHeight;
    private final Float top;

    public PlatformLineHeightSpan(Context context, float f, float f2) {
        this.relativeLineHeight = f;
        Paint.FontMetrics fontMetrics = PlatformInterop.getFontMetrics(context, f2);
        this.top = fontMetrics != null ? Float.valueOf(fontMetrics.top) : null;
    }

    @Override // android.text.style.LineHeightSpan
    public void chooseHeight(CharSequence charSequence, int i, int i2, int i3, int i4, Paint.FontMetricsInt fontMetricsInt) {
        if (fontMetricsInt != null) {
            Float f = this.top;
            fontMetricsInt.ascent = (int) ((f != null ? f.floatValue() : fontMetricsInt.top) * this.relativeLineHeight);
        }
    }
}
