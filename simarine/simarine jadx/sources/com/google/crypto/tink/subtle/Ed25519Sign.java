package com.google.crypto.tink.subtle;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.PublicKeySign;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.Ed25519;
import com.google.crypto.tink.signature.Ed25519Parameters;
import com.google.crypto.tink.signature.Ed25519PrivateKey;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
public final class Ed25519Sign implements PublicKeySign {
    public static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_NOT_FIPS;
    public static final int SECRET_KEY_LEN = 32;
    private final byte[] hashedPrivateKey;
    private final byte[] messageSuffix;
    private final byte[] outputPrefix;
    private final byte[] publicKey;

    public static PublicKeySign create(Ed25519PrivateKey key) throws GeneralSecurityException {
        byte[] bArr;
        byte[] byteArray = key.getPrivateKeyBytes().toByteArray(InsecureSecretKeyAccess.get());
        byte[] byteArray2 = key.getOutputPrefix().toByteArray();
        if (key.getParameters().getVariant().equals(Ed25519Parameters.Variant.LEGACY)) {
            bArr = new byte[]{0};
        } else {
            bArr = new byte[0];
        }
        return new Ed25519Sign(byteArray, byteArray2, bArr);
    }

    private Ed25519Sign(final byte[] privateKey, final byte[] outputPrefix, final byte[] messageSuffix) throws GeneralSecurityException {
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Can not use Ed25519 in FIPS-mode.");
        }
        if (privateKey.length != 32) {
            throw new IllegalArgumentException(String.format("Given private key's length is not %s", 32));
        }
        byte[] hashedScalar = Ed25519.getHashedScalar(privateKey);
        this.hashedPrivateKey = hashedScalar;
        this.publicKey = Ed25519.scalarMultWithBaseToBytes(hashedScalar);
        this.outputPrefix = outputPrefix;
        this.messageSuffix = messageSuffix;
    }

    public Ed25519Sign(final byte[] privateKey) throws GeneralSecurityException {
        this(privateKey, new byte[0], new byte[0]);
    }

    private byte[] noPrefixSign(final byte[] data) throws GeneralSecurityException {
        return Ed25519.sign(data, this.publicKey, this.hashedPrivateKey);
    }

    @Override // com.google.crypto.tink.PublicKeySign
    public byte[] sign(final byte[] data) throws GeneralSecurityException {
        byte[] bArrNoPrefixSign;
        byte[] bArr = this.messageSuffix;
        if (bArr.length == 0) {
            bArrNoPrefixSign = noPrefixSign(data);
        } else {
            bArrNoPrefixSign = noPrefixSign(Bytes.concat(data, bArr));
        }
        byte[] bArr2 = this.outputPrefix;
        return bArr2.length == 0 ? bArrNoPrefixSign : Bytes.concat(bArr2, bArrNoPrefixSign);
    }

    public static final class KeyPair {
        private final byte[] privateKey;
        private final byte[] publicKey;

        private KeyPair(final byte[] publicKey, final byte[] privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        public byte[] getPublicKey() {
            byte[] bArr = this.publicKey;
            return Arrays.copyOf(bArr, bArr.length);
        }

        public byte[] getPrivateKey() {
            byte[] bArr = this.privateKey;
            return Arrays.copyOf(bArr, bArr.length);
        }

        public static KeyPair newKeyPair() throws GeneralSecurityException {
            return newKeyPairFromSeed(Random.randBytes(32));
        }

        public static KeyPair newKeyPairFromSeed(byte[] secretSeed) throws GeneralSecurityException {
            if (secretSeed.length != 32) {
                throw new IllegalArgumentException(String.format("Given secret seed length is not %s", 32));
            }
            return new KeyPair(Ed25519.scalarMultWithBaseToBytes(Ed25519.getHashedScalar(secretSeed)), secretSeed);
        }
    }
}
