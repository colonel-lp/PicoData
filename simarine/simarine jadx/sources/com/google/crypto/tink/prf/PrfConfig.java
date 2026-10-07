package com.google.crypto.tink.prf;

import com.google.crypto.tink.config.TinkFips;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public final class PrfConfig {
    public static final String PRF_TYPE_URL = HkdfPrfKeyManager.getKeyType();
    public static final String HMAC_PRF_TYPE_URL = HmacPrfKeyManager.getKeyType();

    public static void register() throws GeneralSecurityException {
        PrfSetWrapper.register();
        HmacPrfKeyManager.register(true);
        if (TinkFips.useOnlyFips()) {
            return;
        }
        AesCmacPrfKeyManager.register(true);
        HkdfPrfKeyManager.register(true);
    }

    private PrfConfig() {
    }
}
