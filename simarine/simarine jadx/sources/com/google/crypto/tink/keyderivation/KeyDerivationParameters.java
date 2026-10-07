package com.google.crypto.tink.keyderivation;

import com.google.crypto.tink.Parameters;

/* JADX INFO: loaded from: classes2.dex */
public abstract class KeyDerivationParameters extends Parameters {
    public abstract Parameters getDerivedKeyParameters();

    @Override // com.google.crypto.tink.Parameters
    public boolean hasIdRequirement() {
        return getDerivedKeyParameters().hasIdRequirement();
    }
}
