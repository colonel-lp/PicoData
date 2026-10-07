package com.microsoft.maui.glide.stream;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;
import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: loaded from: classes2.dex */
public class GlideInputStreamModelLoader implements ModelLoader<InputStream, InputStream> {
    @Override // com.bumptech.glide.load.model.ModelLoader
    public boolean handles(InputStream inputStream) {
        return true;
    }

    @Override // com.bumptech.glide.load.model.ModelLoader
    public ModelLoader.LoadData<InputStream> buildLoadData(final InputStream inputStream, int i, int i2, Options options) {
        return new ModelLoader.LoadData<>(new ObjectKey(inputStream), new DataFetcher<InputStream>() { // from class: com.microsoft.maui.glide.stream.GlideInputStreamModelLoader.1
            @Override // com.bumptech.glide.load.data.DataFetcher
            public void cancel() {
            }

            @Override // com.bumptech.glide.load.data.DataFetcher
            public void loadData(Priority priority, DataFetcher.DataCallback<? super InputStream> dataCallback) {
                dataCallback.onDataReady(inputStream);
            }

            @Override // com.bumptech.glide.load.data.DataFetcher
            public void cleanup() {
                try {
                    inputStream.close();
                } catch (IOException unused) {
                }
            }

            @Override // com.bumptech.glide.load.data.DataFetcher
            public Class<InputStream> getDataClass() {
                return InputStream.class;
            }

            @Override // com.bumptech.glide.load.data.DataFetcher
            public DataSource getDataSource() {
                return DataSource.LOCAL;
            }
        });
    }
}
