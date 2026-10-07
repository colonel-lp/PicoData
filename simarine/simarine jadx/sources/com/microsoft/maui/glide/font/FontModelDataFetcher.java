package com.microsoft.maui.glide.font;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

/* JADX INFO: loaded from: classes2.dex */
public class FontModelDataFetcher implements DataFetcher<FontModel> {
    private final FontModel model;

    @Override // com.bumptech.glide.load.data.DataFetcher
    public void cancel() {
    }

    @Override // com.bumptech.glide.load.data.DataFetcher
    public void cleanup() {
    }

    FontModelDataFetcher(FontModel fontModel) {
        this.model = fontModel;
    }

    @Override // com.bumptech.glide.load.data.DataFetcher
    public void loadData(Priority priority, DataFetcher.DataCallback<? super FontModel> dataCallback) {
        dataCallback.onDataReady(this.model);
    }

    @Override // com.bumptech.glide.load.data.DataFetcher
    public Class<FontModel> getDataClass() {
        return FontModel.class;
    }

    @Override // com.bumptech.glide.load.data.DataFetcher
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }
}
