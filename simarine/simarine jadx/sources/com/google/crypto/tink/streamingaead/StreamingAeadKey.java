package com.google.crypto.tink.streamingaead;

import com.google.crypto.tink.Key;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public abstract class StreamingAeadKey extends Key {
    @Override // com.google.crypto.tink.Key
    @Nullable
    public final Integer getIdRequirementOrNull() {
        return null;
    }

    @Override // com.google.crypto.tink.Key
    public abstract StreamingAeadParameters getParameters();
}
