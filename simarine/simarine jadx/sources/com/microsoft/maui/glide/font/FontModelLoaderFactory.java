package com.microsoft.maui.glide.font;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

/* JADX INFO: loaded from: classes2.dex */
public class FontModelLoaderFactory implements ModelLoaderFactory<FontModel, FontModel> {
    @Override // com.bumptech.glide.load.model.ModelLoaderFactory
    public void teardown() {
    }

    @Override // com.bumptech.glide.load.model.ModelLoaderFactory
    public ModelLoader<FontModel, FontModel> build(MultiModelLoaderFactory multiModelLoaderFactory) {
        return new FontModelLoader();
    }
}
