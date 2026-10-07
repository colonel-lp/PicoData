package com.google.crypto.tink.keyderivation.internal;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.internal.MutableKeyDerivationRegistry;
import com.google.crypto.tink.internal.MutablePrimitiveRegistry;
import com.google.crypto.tink.keyderivation.PrfBasedKeyDerivationKey;
import com.google.crypto.tink.subtle.prf.StreamingPrf;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class PrfBasedKeyDeriver implements KeyDeriver {
    final PrfBasedKeyDerivationKey key;
    final StreamingPrf prf;

    private PrfBasedKeyDeriver(StreamingPrf prf, PrfBasedKeyDerivationKey key) {
        this.prf = prf;
        this.key = key;
    }

    public static KeyDeriver create(PrfBasedKeyDerivationKey key) throws GeneralSecurityException {
        PrfBasedKeyDeriver prfBasedKeyDeriver = new PrfBasedKeyDeriver((StreamingPrf) MutablePrimitiveRegistry.globalInstance().getPrimitive(key.getPrfKey(), StreamingPrf.class), key);
        prfBasedKeyDeriver.deriveKey(new byte[]{1});
        return prfBasedKeyDeriver;
    }

    @Override // com.google.crypto.tink.keyderivation.internal.KeyDeriver
    public Key deriveKey(byte[] salt) throws GeneralSecurityException {
        return MutableKeyDerivationRegistry.globalInstance().createKeyFromRandomness(this.key.getParameters().getDerivedKeyParameters(), this.prf.computePrf(salt), this.key.getIdRequirementOrNull(), InsecureSecretKeyAccess.get());
    }
}
