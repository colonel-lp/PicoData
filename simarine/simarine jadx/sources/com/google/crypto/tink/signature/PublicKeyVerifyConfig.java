package com.google.crypto.tink.signature;

import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
@Deprecated
public final class PublicKeyVerifyConfig {
    @Deprecated
    public static void registerStandardKeyTypes() throws GeneralSecurityException {
        SignatureConfig.register();
    }

    private PublicKeyVerifyConfig() {
    }
}
