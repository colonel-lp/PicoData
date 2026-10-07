package com.google.crypto.tink.streamingaead;

import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.RegistryConfiguration;
import com.google.crypto.tink.StreamingAead;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
@Deprecated
public final class StreamingAeadFactory {
    public static StreamingAead getPrimitive(KeysetHandle keysetHandle) throws GeneralSecurityException {
        StreamingAeadWrapper.register();
        return (StreamingAead) keysetHandle.getPrimitive(RegistryConfiguration.get(), StreamingAead.class);
    }

    private StreamingAeadFactory() {
    }
}
