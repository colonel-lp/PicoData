package com.microsoft.maui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.ViewGroup;

/* JADX INFO: loaded from: classes2.dex */
public abstract class PlatformContentViewGroup extends ViewGroup {
    private boolean hasClip;

    protected abstract Path getClipPath(int i, int i2);

    public PlatformContentViewGroup(Context context) {
        super(context);
        this.hasClip = false;
    }

    public PlatformContentViewGroup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.hasClip = false;
    }

    public PlatformContentViewGroup(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.hasClip = false;
    }

    public PlatformContentViewGroup(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.hasClip = false;
    }

    protected void setHasClip(boolean z) {
        this.hasClip = z;
        invalidate();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        Path clipPath;
        if (this.hasClip && (clipPath = getClipPath(canvas.getWidth(), canvas.getHeight())) != null) {
            canvas.clipPath(clipPath);
        }
        super.dispatchDraw(canvas);
    }

    protected final void viewGroupDispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }
}
