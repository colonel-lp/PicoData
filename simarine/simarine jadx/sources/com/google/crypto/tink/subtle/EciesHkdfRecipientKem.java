package com.google.crypto.tink.subtle;

import java.security.GeneralSecurityException;
import java.security.interfaces.ECPrivateKey;

/* JADX INFO: loaded from: classes2.dex */
public final class EciesHkdfRecipientKem {
    private ECPrivateKey recipientPrivateKey;

    public EciesHkdfRecipientKem(final ECPrivateKey recipientPrivateKey) {
        this.recipientPrivateKey = recipientPrivateKey;
    }

    public byte[] generateKey(byte[] kemBytes, String hmacAlgo, final byte[] hkdfSalt, final byte[] hkdfInfo, int keySizeInBytes, EllipticCurves.PointFormatType pointFormat) throws GeneralSecurityException {
        return Hkdf.computeEciesHkdfSymmetricKey(kemBytes, EllipticCurves.computeSharedSecret(this.recipientPrivateKey, EllipticCurves.getEcPublicKey(this.recipientPrivateKey.getParams(), pointFormat, kemBytes)), hmacAlgo, hkdfSalt, hkdfInfo, keySizeInBytes);
    }
}
