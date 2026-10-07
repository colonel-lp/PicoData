package com.google.crypto.tink.jwt;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.PrivateKey;
import com.google.errorprone.annotations.Immutable;
import java.util.Optional;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public abstract class JwtSignaturePrivateKey extends Key implements PrivateKey {
    @Override // com.google.crypto.tink.Key
    public abstract JwtSignatureParameters getParameters();

    @Override // com.google.crypto.tink.PrivateKey
    public abstract JwtSignaturePublicKey getPublicKey();

    public Optional<String> getKid() {
        return getPublicKey().getKid();
    }

    @Override // com.google.crypto.tink.Key
    @Nullable
    public Integer getIdRequirementOrNull() {
        return getPublicKey().getIdRequirementOrNull();
    }
}
