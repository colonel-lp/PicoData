package com.microsoft.maui.glide.stream;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import java.io.InputStream;

/* JADX INFO: loaded from: classes2.dex */
public class GlideInputStreamModelLoaderFactory implements ModelLoaderFactory<InputStream, InputStream> {
    @Override // com.bumptech.glide.load.model.ModelLoaderFactory
    public void teardown() {
    }

    @Override // com.bumptech.glide.load.model.ModelLoaderFactory
    public ModelLoader<InputStream, InputStream> build(MultiModelLoaderFactory multiModelLoaderFactory) {
        return new GlideInputStreamModelLoader();
    }
}
