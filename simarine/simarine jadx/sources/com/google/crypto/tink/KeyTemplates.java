package com.google.crypto.tink;

import com.google.crypto.tink.internal.MutableParametersRegistry;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public final class KeyTemplates {
    public static KeyTemplate get(String name) throws GeneralSecurityException {
        return KeyTemplate.createFrom(MutableParametersRegistry.globalInstance().get(name));
    }

    private KeyTemplates() {
    }
}
