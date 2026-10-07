package com.microsoft.maui.glide.font;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.microsoft.maui.PlatformLogger;

/* JADX INFO: loaded from: classes2.dex */
public class FontModelResourceDecoder implements ResourceDecoder<FontModel, Bitmap> {
    private static final PlatformLogger logger = new PlatformLogger("FontModelResDecoder");
    private final BitmapPool bitmapPool = new BitmapPoolAdapter();

    @Override // com.bumptech.glide.load.ResourceDecoder
    public boolean handles(FontModel fontModel, Options options) {
        return true;
    }

    @Override // com.bumptech.glide.load.ResourceDecoder
    public Resource<Bitmap> decode(FontModel fontModel, int i, int i2, Options options) {
        Paint paint = new Paint();
        paint.setTextSize(fontModel.getTextSize());
        paint.setAntiAlias(true);
        paint.setColor(fontModel.getColor());
        paint.setTextAlign(Paint.Align.LEFT);
        Typeface typeface = fontModel.getTypeface();
        if (typeface != null) {
            paint.setTypeface(typeface);
        }
        int iMeasureText = (int) (paint.measureText(fontModel.getGlyph()) + 0.5f);
        float f = (int) ((-paint.ascent()) + 0.5f);
        Bitmap bitmap = this.bitmapPool.get(iMeasureText, (int) (paint.descent() + f + 0.5f), Bitmap.Config.ARGB_8888);
        new Canvas(bitmap).drawText(fontModel.getGlyph(), 0.0f, f, paint);
        PlatformLogger platformLogger = logger;
        if (platformLogger.isVerboseLoggable) {
            platformLogger.v(fontModel.toString());
        }
        return new BitmapResource(bitmap, this.bitmapPool);
    }
}
