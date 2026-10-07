package com.google.crypto.tink.jwt;

import com.google.crypto.tink.Key;
import java.util.Optional;

/* JADX INFO: loaded from: classes2.dex */
public abstract class JwtMacKey extends Key {
    public abstract Optional<String> getKid();

    @Override // com.google.crypto.tink.Key
    public abstract JwtMacParameters getParameters();
}
