package com.google.crypto.tink.aead.internal;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.aead.XChaCha20Poly1305Key;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.subtle.Random;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class XChaCha20Poly1305Jce implements Aead {
    private static final String CIPHER_NAME = "ChaCha20-Poly1305";
    private static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_NOT_FIPS;
    private static final String KEY_NAME = "ChaCha20";
    private static final int KEY_SIZE_IN_BYTES = 32;
    private static final int NONCE_SIZE_IN_BYTES = 24;
    private static final int TAG_SIZE_IN_BYTES = 16;
    private final byte[] key;
    private final byte[] outputPrefix;

    private XChaCha20Poly1305Jce(final byte[] key, final byte[] outputPrefix) throws GeneralSecurityException {
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Can not use ChaCha20Poly1305 in FIPS-mode.");
        }
        if (!isSupported()) {
            throw new GeneralSecurityException("JCE does not support algorithm: ChaCha20-Poly1305");
        }
        if (key.length != 32) {
            throw new InvalidKeyException("The key length in bytes must be 32.");
        }
        this.key = key;
        this.outputPrefix = outputPrefix;
    }

    public static Aead create(XChaCha20Poly1305Key key) throws GeneralSecurityException {
        return new XChaCha20Poly1305Jce(key.getKeyBytes().toByteArray(InsecureSecretKeyAccess.get()), key.getOutputPrefix().toByteArray());
    }

    public static boolean isSupported() {
        return ChaCha20Poly1305Jce.getThreadLocalCipherOrNull() != null;
    }

    @Override // com.google.crypto.tink.Aead
    public byte[] encrypt(final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
        if (plaintext == null) {
            throw new NullPointerException("plaintext is null");
        }
        byte[] bArrRandBytes = Random.randBytes(24);
        SecretKeySpec secretKeySpec = new SecretKeySpec(ChaCha20Util.hChaCha20(this.key, bArrRandBytes), KEY_NAME);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(getChaCha20Nonce(bArrRandBytes));
        Cipher threadLocalCipherOrNull = ChaCha20Poly1305Jce.getThreadLocalCipherOrNull();
        threadLocalCipherOrNull.init(1, secretKeySpec, ivParameterSpec);
        if (associatedData != null && associatedData.length != 0) {
            threadLocalCipherOrNull.updateAAD(associatedData);
        }
        int outputSize = threadLocalCipherOrNull.getOutputSize(plaintext.length);
        byte[] bArr = this.outputPrefix;
        if (outputSize > 2147483623 - bArr.length) {
            throw new GeneralSecurityException("plaintext too long");
        }
        byte[] bArrCopyOf = Arrays.copyOf(bArr, bArr.length + 24 + outputSize);
        System.arraycopy(bArrRandBytes, 0, bArrCopyOf, this.outputPrefix.length, 24);
        if (threadLocalCipherOrNull.doFinal(plaintext, 0, plaintext.length, bArrCopyOf, this.outputPrefix.length + 24) == outputSize) {
            return bArrCopyOf;
        }
        throw new GeneralSecurityException("not enough data written");
    }

    @Override // com.google.crypto.tink.Aead
    public byte[] decrypt(final byte[] ciphertext, final byte[] associatedData) throws GeneralSecurityException {
        if (ciphertext == null) {
            throw new NullPointerException("ciphertext is null");
        }
        int length = ciphertext.length;
        byte[] bArr = this.outputPrefix;
        if (length < bArr.length + 40) {
            throw new GeneralSecurityException("ciphertext too short");
        }
        if (!Util.isPrefix(bArr, ciphertext)) {
            throw new GeneralSecurityException("Decryption failed (OutputPrefix mismatch).");
        }
        byte[] bArr2 = new byte[24];
        System.arraycopy(ciphertext, this.outputPrefix.length, bArr2, 0, 24);
        SecretKeySpec secretKeySpec = new SecretKeySpec(ChaCha20Util.hChaCha20(this.key, bArr2), KEY_NAME);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(getChaCha20Nonce(bArr2));
        Cipher threadLocalCipherOrNull = ChaCha20Poly1305Jce.getThreadLocalCipherOrNull();
        threadLocalCipherOrNull.init(2, secretKeySpec, ivParameterSpec);
        if (associatedData != null && associatedData.length != 0) {
            threadLocalCipherOrNull.updateAAD(associatedData);
        }
        byte[] bArr3 = this.outputPrefix;
        return threadLocalCipherOrNull.doFinal(ciphertext, bArr3.length + 24, (ciphertext.length - bArr3.length) - 24);
    }

    static byte[] getChaCha20Nonce(byte[] nonce) {
        byte[] bArr = new byte[12];
        System.arraycopy(nonce, 16, bArr, 4, 8);
        return bArr;
    }
}
