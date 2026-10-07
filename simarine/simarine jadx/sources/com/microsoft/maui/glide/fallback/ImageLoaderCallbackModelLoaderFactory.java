package com.microsoft.maui.glide.fallback;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.microsoft.maui.ImageLoaderCallback;

/* JADX INFO: loaded from: classes2.dex */
public class ImageLoaderCallbackModelLoaderFactory implements ModelLoaderFactory<ImageLoaderCallback, ImageLoaderCallback> {
    @Override // com.bumptech.glide.load.model.ModelLoaderFactory
    public void teardown() {
    }

    @Override // com.bumptech.glide.load.model.ModelLoaderFactory
    public ModelLoader<ImageLoaderCallback, ImageLoaderCallback> build(MultiModelLoaderFactory multiModelLoaderFactory) {
        return new ImageLoaderCallbackModelLoader();
    }
}
