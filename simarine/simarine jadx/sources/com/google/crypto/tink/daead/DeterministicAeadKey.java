package com.google.crypto.tink.daead;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.util.Bytes;

/* JADX INFO: loaded from: classes2.dex */
public abstract class DeterministicAeadKey extends Key {
    public abstract Bytes getOutputPrefix();

    @Override // com.google.crypto.tink.Key
    public abstract DeterministicAeadParameters getParameters();
}
