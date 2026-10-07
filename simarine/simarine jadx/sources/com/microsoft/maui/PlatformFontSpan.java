package com.microsoft.maui;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.util.TypedValue;

/* JADX INFO: loaded from: classes2.dex */
public class PlatformFontSpan extends MetricAffectingSpan {
    private Float letterSpacing;
    private Float textSize;
    private Typeface typeface;

    public PlatformFontSpan(float f) {
        this.letterSpacing = Float.valueOf(f);
    }

    public PlatformFontSpan(Context context, Typeface typeface, boolean z, float f) {
        this.typeface = typeface;
        this.textSize = Float.valueOf(TypedValue.applyDimension(z ? 2 : 1, f, context.getResources().getDisplayMetrics()));
    }

    @Override // android.text.style.CharacterStyle
    public void updateDrawState(TextPaint textPaint) {
        if (textPaint != null) {
            apply(textPaint);
        }
    }

    @Override // android.text.style.MetricAffectingSpan
    public void updateMeasureState(TextPaint textPaint) {
        apply(textPaint);
    }

    void apply(TextPaint textPaint) {
        Typeface typeface = this.typeface;
        if (typeface != null) {
            textPaint.setTypeface(typeface);
        }
        Float f = this.textSize;
        if (f != null) {
            textPaint.setTextSize(f.floatValue());
        }
        Float f2 = this.letterSpacing;
        if (f2 != null) {
            textPaint.setLetterSpacing(f2.floatValue());
        }
    }
}
