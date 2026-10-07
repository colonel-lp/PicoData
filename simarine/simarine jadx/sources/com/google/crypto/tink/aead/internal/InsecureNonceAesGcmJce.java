package com.google.crypto.tink.aead.internal;

import com.google.crypto.tink.config.internal.TinkFipsUtil;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

/* JADX INFO: loaded from: classes2.dex */
public final class InsecureNonceAesGcmJce {
    public static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_REQUIRES_BORINGCRYPTO;
    public static final int IV_SIZE_IN_BYTES = 12;
    public static final int TAG_SIZE_IN_BYTES = 16;
    private final SecretKey keySpec;

    public InsecureNonceAesGcmJce(final byte[] key) throws GeneralSecurityException {
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Can not use AES-GCM in FIPS-mode, as BoringCrypto module is not available.");
        }
        this.keySpec = AesGcmJceUtil.getSecretKey(key);
    }

    public byte[] encrypt(final byte[] iv, final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
        return encrypt(iv, plaintext, 0, associatedData);
    }

    public byte[] encrypt(final byte[] iv, final byte[] plaintext, int ciphertextOffset, final byte[] associatedData) throws GeneralSecurityException {
        if (iv.length != 12) {
            throw new GeneralSecurityException("iv is wrong size");
        }
        AlgorithmParameterSpec params = AesGcmJceUtil.getParams(iv);
        Cipher threadLocalCipher = AesGcmJceUtil.getThreadLocalCipher();
        threadLocalCipher.init(1, this.keySpec, params);
        if (associatedData != null && associatedData.length != 0) {
            threadLocalCipher.updateAAD(associatedData);
        }
        int outputSize = threadLocalCipher.getOutputSize(plaintext.length);
        if (outputSize > Integer.MAX_VALUE - ciphertextOffset) {
            throw new GeneralSecurityException("plaintext too long");
        }
        byte[] bArr = new byte[ciphertextOffset + outputSize];
        if (threadLocalCipher.doFinal(plaintext, 0, plaintext.length, bArr, ciphertextOffset) == outputSize) {
            return bArr;
        }
        throw new GeneralSecurityException("not enough data written");
    }

    public byte[] decrypt(final byte[] iv, final byte[] ciphertext, final byte[] associatedData) throws GeneralSecurityException {
        return decrypt(iv, ciphertext, 0, associatedData);
    }

    public byte[] decrypt(final byte[] iv, final byte[] ciphertextWithPrefix, int ciphertextOffset, final byte[] associatedData) throws GeneralSecurityException {
        if (iv.length != 12) {
            throw new GeneralSecurityException("iv is wrong size");
        }
        if (ciphertextWithPrefix.length < ciphertextOffset + 16) {
            throw new GeneralSecurityException("ciphertext too short");
        }
        AlgorithmParameterSpec params = AesGcmJceUtil.getParams(iv);
        Cipher threadLocalCipher = AesGcmJceUtil.getThreadLocalCipher();
        threadLocalCipher.init(2, this.keySpec, params);
        if (associatedData != null && associatedData.length != 0) {
            threadLocalCipher.updateAAD(associatedData);
        }
        return threadLocalCipher.doFinal(ciphertextWithPrefix, ciphertextOffset, ciphertextWithPrefix.length - ciphertextOffset);
    }
}
