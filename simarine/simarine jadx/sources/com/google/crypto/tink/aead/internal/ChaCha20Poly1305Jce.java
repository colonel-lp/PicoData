package com.google.crypto.tink.aead.internal;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.aead.ChaCha20Poly1305Key;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.subtle.EngineFactory;
import com.google.crypto.tink.subtle.Hex;
import com.google.crypto.tink.subtle.Random;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class ChaCha20Poly1305Jce implements Aead {
    private static final String CIPHER_NAME = "ChaCha20-Poly1305";
    private static final String KEY_NAME = "ChaCha20";
    private static final int KEY_SIZE_IN_BYTES = 32;
    private static final int NONCE_SIZE_IN_BYTES = 12;
    private static final int TAG_SIZE_IN_BYTES = 16;
    private final SecretKey keySpec;
    private final byte[] outputPrefix;
    private static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_NOT_FIPS;
    private static final byte[] TEST_KEY = Hex.decode("808182838485868788898a8b8c8d8e8f909192939495969798999a9b9c9d9e9f");
    private static final byte[] TEST_NONCE = Hex.decode("070000004041424344454647");
    private static final byte[] TEST_CIPHERTEXT_OF_EMPTY = Hex.decode("a0784d7a4716f3feb4f64e7f4b39bf04");
    private static final ThreadLocal<Cipher> localCipher = new ThreadLocal<Cipher>() { // from class: com.google.crypto.tink.aead.internal.ChaCha20Poly1305Jce.1
        /* JADX INFO: Access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        @Nullable
        public Cipher initialValue() {
            try {
                Cipher engineFactory = EngineFactory.CIPHER.getInstance(ChaCha20Poly1305Jce.CIPHER_NAME);
                if (ChaCha20Poly1305Jce.isValid(engineFactory)) {
                    return engineFactory;
                }
                return null;
            } catch (GeneralSecurityException unused) {
                return null;
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean isValid(Cipher cipher) {
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(TEST_NONCE);
            byte[] bArr = TEST_KEY;
            cipher.init(2, new SecretKeySpec(bArr, KEY_NAME), ivParameterSpec);
            byte[] bArr2 = TEST_CIPHERTEXT_OF_EMPTY;
            if (cipher.doFinal(bArr2).length != 0) {
                return false;
            }
            cipher.init(2, new SecretKeySpec(bArr, KEY_NAME), ivParameterSpec);
            return cipher.doFinal(bArr2).length == 0;
        } catch (GeneralSecurityException unused) {
            return false;
        }
    }

    private ChaCha20Poly1305Jce(final byte[] key, final byte[] outputPrefix) throws GeneralSecurityException {
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
        this.outputPrefix = outputPrefix;
    }

    public static Aead create(ChaCha20Poly1305Key key) throws GeneralSecurityException {
        return new ChaCha20Poly1305Jce(key.getKeyBytes().toByteArray(InsecureSecretKeyAccess.get()), key.getOutputPrefix().toByteArray());
    }

    @Nullable
    static Cipher getThreadLocalCipherOrNull() {
        return localCipher.get();
    }

    public static boolean isSupported() {
        return localCipher.get() != null;
    }

    @Override // com.google.crypto.tink.Aead
    public byte[] encrypt(final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
        if (plaintext == null) {
            throw new NullPointerException("plaintext is null");
        }
        byte[] bArrRandBytes = Random.randBytes(12);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(bArrRandBytes);
        Cipher cipher = localCipher.get();
        cipher.init(1, this.keySpec, ivParameterSpec);
        if (associatedData != null && associatedData.length != 0) {
            cipher.updateAAD(associatedData);
        }
        int outputSize = cipher.getOutputSize(plaintext.length);
        byte[] bArr = this.outputPrefix;
        if (outputSize > 2147483635 - bArr.length) {
            throw new GeneralSecurityException("plaintext too long");
        }
        byte[] bArrCopyOf = Arrays.copyOf(bArr, bArr.length + 12 + outputSize);
        System.arraycopy(bArrRandBytes, 0, bArrCopyOf, this.outputPrefix.length, 12);
        if (cipher.doFinal(plaintext, 0, plaintext.length, bArrCopyOf, this.outputPrefix.length + 12) == outputSize) {
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
        if (length < bArr.length + 28) {
            throw new GeneralSecurityException("ciphertext too short");
        }
        if (!Util.isPrefix(bArr, ciphertext)) {
            throw new GeneralSecurityException("Decryption failed (OutputPrefix mismatch).");
        }
        byte[] bArr2 = new byte[12];
        System.arraycopy(ciphertext, this.outputPrefix.length, bArr2, 0, 12);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(bArr2);
        Cipher cipher = localCipher.get();
        cipher.init(2, this.keySpec, ivParameterSpec);
        if (associatedData != null && associatedData.length != 0) {
            cipher.updateAAD(associatedData);
        }
        byte[] bArr3 = this.outputPrefix;
        return cipher.doFinal(ciphertext, bArr3.length + 12, (ciphertext.length - bArr3.length) - 12);
    }
}
