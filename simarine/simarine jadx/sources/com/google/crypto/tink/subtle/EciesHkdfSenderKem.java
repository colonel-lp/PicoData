package com.google.crypto.tink.subtle;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

/* JADX INFO: loaded from: classes2.dex */
public final class EciesHkdfSenderKem {
    private final ECPublicKey recipientPublicKey;

    public static final class KemKey {
        private final com.google.crypto.tink.util.Bytes kemBytes;
        private final com.google.crypto.tink.util.Bytes symmetricKey;

        public KemKey(final byte[] kemBytes, final byte[] symmetricKey) {
            if (kemBytes == null) {
                throw new NullPointerException("KemBytes must be non-null");
            }
            if (symmetricKey == null) {
                throw new NullPointerException("symmetricKey must be non-null");
            }
            this.kemBytes = com.google.crypto.tink.util.Bytes.copyFrom(kemBytes);
            this.symmetricKey = com.google.crypto.tink.util.Bytes.copyFrom(symmetricKey);
        }

        public byte[] getKemBytes() {
            return this.kemBytes.toByteArray();
        }

        public byte[] getSymmetricKey() {
            return this.symmetricKey.toByteArray();
        }
    }

    public EciesHkdfSenderKem(final ECPublicKey recipientPublicKey) {
        this.recipientPublicKey = recipientPublicKey;
    }

    public KemKey generateKey(String hmacAlgo, final byte[] hkdfSalt, final byte[] hkdfInfo, int keySizeInBytes, EllipticCurves.PointFormatType pointFormat) throws GeneralSecurityException {
        KeyPair keyPairGenerateKeyPair = EllipticCurves.generateKeyPair(this.recipientPublicKey.getParams());
        ECPublicKey eCPublicKey = (ECPublicKey) keyPairGenerateKeyPair.getPublic();
        byte[] bArrComputeSharedSecret = EllipticCurves.computeSharedSecret((ECPrivateKey) keyPairGenerateKeyPair.getPrivate(), this.recipientPublicKey);
        byte[] bArrPointEncode = EllipticCurves.pointEncode(eCPublicKey.getParams().getCurve(), pointFormat, eCPublicKey.getW());
        return new KemKey(bArrPointEncode, Hkdf.computeEciesHkdfSymmetricKey(bArrPointEncode, bArrComputeSharedSecret, hmacAlgo, hkdfSalt, hkdfInfo, keySizeInBytes));
    }
}
