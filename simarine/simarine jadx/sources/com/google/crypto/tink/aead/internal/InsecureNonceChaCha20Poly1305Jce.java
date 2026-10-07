package com.google.crypto.tink.aead.internal;

import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class InsecureNonceChaCha20Poly1305Jce {
    private static final String CIPHER_NAME = "ChaCha20-Poly1305";
    private static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_NOT_FIPS;
    private static final String KEY_NAME = "ChaCha20";
    private static final int KEY_SIZE_IN_BYTES = 32;
    private static final int NONCE_SIZE_IN_BYTES = 12;
    private static final int TAG_SIZE_IN_BYTES = 16;
    private final SecretKey keySpec;

    private InsecureNonceChaCha20Poly1305Jce(final byte[] key) throws GeneralSecurityException {
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Can not use ChaCha20Poly1305 in FIPS-mode.");
        }
        if (!isSupported()) {
            throw new GeneralSecurityException("JCE does not support algorithm: ChaCha20-Poly1305");
        }
        if (key.length != 32) {
            throw new InvalidKeyException("The key length in bytes must be 32.");
        }
        this.keySpec = new SecretKeySpec(key, KEY_NAME);
    }

    public static InsecureNonceChaCha20Poly1305Jce create(final byte[] key) throws GeneralSecurityException {
        return new InsecureNonceChaCha20Poly1305Jce(key);
    }

    public static boolean isSupported() {
        return ChaCha20Poly1305Jce.getThreadLocalCipherOrNull() != null;
    }

    public byte[] encrypt(final byte[] nonce, final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
        return encrypt(nonce, plaintext, 0, associatedData);
    }

    public byte[] encrypt(final byte[] nonce, final byte[] plaintext, int ciphertextOffset, final byte[] associatedData) throws GeneralSecurityException {
        if (plaintext == null) {
            throw new NullPointerException("plaintext is null");
        }
        if (nonce.length != 12) {
            throw new GeneralSecurityException("nonce length must be 12 bytes.");
        }
        IvParameterSpec ivParameterSpec = new IvParameterSpec(nonce);
        Cipher threadLocalCipherOrNull = ChaCha20Poly1305Jce.getThreadLocalCipherOrNull();
        threadLocalCipherOrNull.init(1, this.keySpec, ivParameterSpec);
        if (associatedData != null && associatedData.length != 0) {
            threadLocalCipherOrNull.updateAAD(associatedData);
        }
        int outputSize = threadLocalCipherOrNull.getOutputSize(plaintext.length);
        if (outputSize > Integer.MAX_VALUE - ciphertextOffset) {
            throw new GeneralSecurityException("plaintext too long");
        }
        byte[] bArr = new byte[ciphertextOffset + outputSize];
        if (threadLocalCipherOrNull.doFinal(plaintext, 0, plaintext.length, bArr, ciphertextOffset) == outputSize) {
            return bArr;
        }
        throw new GeneralSecurityException("not enough data written");
    }

    public byte[] decrypt(final byte[] nonce, final byte[] ciphertext, final byte[] associatedData) throws GeneralSecurityException {
        return decrypt(nonce, ciphertext, 0, associatedData);
    }

    public byte[] decrypt(final byte[] nonce, final byte[] ciphertextWithPrefix, int ciphertextOffset, final byte[] associatedData) throws GeneralSecurityException {
        if (ciphertextWithPrefix == null) {
            throw new NullPointerException("ciphertext is null");
        }
        if (nonce.length != 12) {
            throw new GeneralSecurityException("nonce length must be 12 bytes.");
        }
        if (ciphertextWithPrefix.length < ciphertextOffset + 16) {
            throw new GeneralSecurityException("ciphertext too short");
        }
        IvParameterSpec ivParameterSpec = new IvParameterSpec(nonce);
        Cipher threadLocalCipherOrNull = ChaCha20Poly1305Jce.getThreadLocalCipherOrNull();
        threadLocalCipherOrNull.init(2, this.keySpec, ivParameterSpec);
        if (associatedData != null && associatedData.length != 0) {
            threadLocalCipherOrNull.updateAAD(associatedData);
        }
        return threadLocalCipherOrNull.doFinal(ciphertextWithPrefix, ciphertextOffset, ciphertextWithPrefix.length - ciphertextOffset);
    }
}
