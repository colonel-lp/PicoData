package com.google.crypto.tink.keyderivation;

import com.google.crypto.tink.config.TinkFips;
import com.google.crypto.tink.keyderivation.internal.PrfBasedDeriverKeyManager;
import com.google.crypto.tink.prf.HkdfPrfKeyManager;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public final class KeyDerivationConfig {
    public static void register() throws GeneralSecurityException {
        com.google.crypto.tink.keyderivation.internal.KeysetDeriverWrapper.register();
        if (TinkFips.useOnlyFips()) {
            return;
        }
        HkdfPrfKeyManager.register(true);
        PrfBasedDeriverKeyManager.register(true);
    }

    private KeyDerivationConfig() {
    }
}
