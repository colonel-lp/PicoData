package com.google.crypto.tink.keyderivation;

import com.google.crypto.tink.Key;

/* JADX INFO: loaded from: classes2.dex */
public abstract class KeyDerivationKey extends Key {
    @Override // com.google.crypto.tink.Key
    public abstract KeyDerivationParameters getParameters();
}
