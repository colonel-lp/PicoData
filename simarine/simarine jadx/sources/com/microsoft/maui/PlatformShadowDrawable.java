package com.microsoft.maui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/* JADX INFO: loaded from: classes2.dex */
public interface PlatformShadowDrawable {
    boolean canDrawShadow();

    void drawShadow(Canvas canvas, Paint paint, Path path);
}
