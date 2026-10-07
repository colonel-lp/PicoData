package com.microsoft.maui.glide.fallback;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.microsoft.maui.ImageLoaderCallback;

/* JADX INFO: loaded from: classes2.dex */
public class ImageLoaderCallbackModelLoader implements ModelLoader<ImageLoaderCallback, ImageLoaderCallback> {
    @Override // com.bumptech.glide.load.model.ModelLoader
    public ModelLoader.LoadData<ImageLoaderCallback> buildLoadData(ImageLoaderCallback imageLoaderCallback, int i, int i2, Options options) {
        return null;
    }

    @Override // com.bumptech.glide.load.model.ModelLoader
    public boolean handles(ImageLoaderCallback imageLoaderCallback) {
        return true;
    }
}
