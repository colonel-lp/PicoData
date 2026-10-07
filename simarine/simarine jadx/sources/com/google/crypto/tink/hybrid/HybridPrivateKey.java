package com.google.crypto.tink.hybrid;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.PrivateKey;
import com.google.crypto.tink.util.Bytes;
import com.google.errorprone.annotations.Immutable;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public abstract class HybridPrivateKey extends Key implements PrivateKey {
    @Override // com.google.crypto.tink.PrivateKey
    public abstract HybridPublicKey getPublicKey();

    public final Bytes getOutputPrefix() {
        return getPublicKey().getOutputPrefix();
    }

    @Override // com.google.crypto.tink.Key
    @Nullable
    public Integer getIdRequirementOrNull() {
        return getPublicKey().getIdRequirementOrNull();
    }

    @Override // com.google.crypto.tink.Key
    public HybridParameters getParameters() {
        return getPublicKey().getParameters();
    }
}
