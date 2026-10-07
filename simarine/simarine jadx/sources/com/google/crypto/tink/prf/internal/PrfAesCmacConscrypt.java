package com.google.crypto.tink.prf.internal;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.ConscryptUtil;
import com.google.crypto.tink.prf.AesCmacPrfKey;
import com.google.crypto.tink.prf.Prf;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.Provider;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class PrfAesCmacConscrypt implements Prf {
    private static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_NOT_FIPS;
    private final Provider conscrypt;
    private final Key key;

    public static Prf create(AesCmacPrfKey key) throws GeneralSecurityException {
        Provider providerProviderOrNull = ConscryptUtil.providerOrNull();
        if (providerProviderOrNull == null) {
            throw new GeneralSecurityException("Conscrypt not available");
        }
        Mac.getInstance("AESCMAC", providerProviderOrNull);
        return new PrfAesCmacConscrypt(key.getKeyBytes().toByteArray(InsecureSecretKeyAccess.get()), providerProviderOrNull);
    }

    private PrfAesCmacConscrypt(byte[] keyBytes, Provider conscrypt) throws GeneralSecurityException {
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Cannot use AES-CMAC in FIPS-mode, as BoringCrypto module is not available");
        }
        this.key = new SecretKeySpec(keyBytes, "AES");
        this.conscrypt = conscrypt;
    }

    @Override // com.google.crypto.tink.prf.Prf
    public byte[] compute(byte[] data, int outputLength) throws GeneralSecurityException {
        if (outputLength > 16) {
            throw new InvalidAlgorithmParameterException("outputLength must not be larger than 16");
        }
        Mac mac = Mac.getInstance("AESCMAC", this.conscrypt);
        mac.init(this.key);
        byte[] bArrDoFinal = mac.doFinal(data);
        return outputLength == bArrDoFinal.length ? bArrDoFinal : Arrays.copyOf(bArrDoFinal, outputLength);
    }
}
