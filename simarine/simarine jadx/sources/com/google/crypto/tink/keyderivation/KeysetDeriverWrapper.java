package com.google.crypto.tink.keyderivation;

import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public final class KeysetDeriverWrapper {
    @Deprecated
    public static void register() throws GeneralSecurityException {
        com.google.crypto.tink.keyderivation.internal.KeysetDeriverWrapper.register();
    }

    private KeysetDeriverWrapper() {
    }
}
