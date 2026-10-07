package com.microsoft.maui.glide.font;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;

/* JADX INFO: loaded from: classes2.dex */
public class FontModelLoader implements ModelLoader<FontModel, FontModel> {
    @Override // com.bumptech.glide.load.model.ModelLoader
    public boolean handles(FontModel fontModel) {
        return true;
    }

    @Override // com.bumptech.glide.load.model.ModelLoader
    public ModelLoader.LoadData<FontModel> buildLoadData(FontModel fontModel, int i, int i2, Options options) {
        return new ModelLoader.LoadData<>(fontModel.getCacheKey(), new FontModelDataFetcher(fontModel));
    }
}
