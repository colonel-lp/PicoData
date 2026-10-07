package com.microsoft.maui.glide;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;

/* JADX INFO: loaded from: classes2.dex */
public class ShadowBitmapPool extends LruBitmapPool implements ComponentCallbacks2 {
    private static ShadowBitmapPool bitmapPool;

    @Override // android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
    }

    public static BitmapPool get(Context context) {
        if (bitmapPool == null) {
            synchronized (ShadowBitmapPool.class) {
                if (bitmapPool == null) {
                    Context applicationContext = context.getApplicationContext();
                    ShadowBitmapPool shadowBitmapPoolCreateBitmapPool = createBitmapPool(applicationContext);
                    bitmapPool = shadowBitmapPoolCreateBitmapPool;
                    applicationContext.registerComponentCallbacks(shadowBitmapPoolCreateBitmapPool);
                }
            }
        }
        return bitmapPool;
    }

    private static ShadowBitmapPool createBitmapPool(Context context) {
        return new ShadowBitmapPool(new MemorySizeCalculator.Builder(context).build().getBitmapPoolSize());
    }

    private ShadowBitmapPool(int i) {
        super(i);
    }

    @Override // android.content.ComponentCallbacks2
    public void onTrimMemory(int i) {
        trimMemory(i);
    }

    @Override // android.content.ComponentCallbacks
    public void onLowMemory() {
        trimMemory(20);
    }
}
