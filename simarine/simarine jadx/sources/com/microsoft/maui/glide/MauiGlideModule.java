package com.microsoft.maui.glide;

import android.content.Context;
import android.graphics.Bitmap;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.module.AppGlideModule;
import com.microsoft.maui.ImageLoaderCallback;
import com.microsoft.maui.PlatformLogger;
import com.microsoft.maui.glide.fallback.ImageLoaderCallbackModelLoaderFactory;
import com.microsoft.maui.glide.font.FontModel;
import com.microsoft.maui.glide.font.FontModelLoaderFactory;
import com.microsoft.maui.glide.font.FontModelResourceDecoder;
import com.microsoft.maui.glide.stream.GlideInputStreamModelLoaderFactory;
import java.io.InputStream;

/* JADX INFO: loaded from: classes2.dex */
public class MauiGlideModule extends AppGlideModule {
    private static final PlatformLogger logger = new PlatformLogger("Glide");

    @Override // com.bumptech.glide.module.AppGlideModule
    public boolean isManifestParsingEnabled() {
        return false;
    }

    @Override // com.bumptech.glide.module.LibraryGlideModule, com.bumptech.glide.module.RegistersComponents
    public void registerComponents(Context context, Glide glide, Registry registry) {
        registry.prepend(FontModel.class, FontModel.class, new FontModelLoaderFactory());
        registry.prepend(FontModel.class, Bitmap.class, new FontModelResourceDecoder());
        registry.prepend(InputStream.class, InputStream.class, new GlideInputStreamModelLoaderFactory());
        registry.prepend(ImageLoaderCallback.class, ImageLoaderCallback.class, new ImageLoaderCallbackModelLoaderFactory());
    }

    @Override // com.bumptech.glide.module.AppGlideModule, com.bumptech.glide.module.AppliesOptions
    public void applyOptions(Context context, GlideBuilder glideBuilder) {
        if (logger.isVerboseLoggable) {
            glideBuilder.setLogLevel(2);
        }
    }
}
