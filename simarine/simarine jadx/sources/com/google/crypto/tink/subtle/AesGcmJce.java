package com.google.crypto.tink.subtle;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.aead.AesGcmKey;
import com.google.crypto.tink.aead.internal.AesGcmJceUtil;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.Util;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class AesGcmJce implements Aead {
    public static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_REQUIRES_BORINGCRYPTO;
    private static final int IV_SIZE_IN_BYTES = 12;
    private static final int TAG_SIZE_IN_BYTES = 16;
    private final SecretKey keySpec;
    private final byte[] outputPrefix;

    private AesGcmJce(final byte[] key, com.google.crypto.tink.util.Bytes outputPrefix) throws GeneralSecurityException {
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Can not use AES-GCM in FIPS-mode, as BoringCrypto module is not available.");
        }
        this.keySpec = AesGcmJceUtil.getSecretKey(key);
        this.outputPrefix = outputPrefix.toByteArray();
    }

    public AesGcmJce(final byte[] key) throws GeneralSecurityException {
        this(key, com.google.crypto.tink.util.Bytes.copyFrom(new byte[0]));
    }

    public static Aead create(AesGcmKey key) throws GeneralSecurityException {
        if (key.getParameters().getIvSizeBytes() != 12) {
            throw new GeneralSecurityException("Expected IV Size 12, got " + key.getParameters().getIvSizeBytes());
        }
        if (key.getParameters().getTagSizeBytes() != 16) {
            throw new GeneralSecurityException("Expected tag Size 16, got " + key.getParameters().getTagSizeBytes());
        }
        return new AesGcmJce(key.getKeyBytes().toByteArray(InsecureSecretKeyAccess.get()), key.getOutputPrefix());
    }

    @Override // com.google.crypto.tink.Aead
    public byte[] encrypt(final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
        if (plaintext == null) {
            throw new NullPointerException("plaintext is null");
        }
        byte[] bArrRandBytes = Random.randBytes(12);
        AlgorithmParameterSpec params = AesGcmJceUtil.getParams(bArrRandBytes);
        Cipher threadLocalCipher = AesGcmJceUtil.getThreadLocalCipher();
        threadLocalCipher.init(1, this.keySpec, params);
        if (associatedData != null && associatedData.length != 0) {
            threadLocalCipher.updateAAD(associatedData);
        }
        int outputSize = threadLocalCipher.getOutputSize(plaintext.length);
        byte[] bArr = this.outputPrefix;
        if (outputSize > 2147483635 - bArr.length) {
            throw new GeneralSecurityException("plaintext too long");
        }
        byte[] bArrCopyOf = Arrays.copyOf(bArr, bArr.length + 12 + outputSize);
        System.arraycopy(bArrRandBytes, 0, bArrCopyOf, this.outputPrefix.length, 12);
        if (threadLocalCipher.doFinal(plaintext, 0, plaintext.length, bArrCopyOf, this.outputPrefix.length + 12) == outputSize) {
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
        AlgorithmParameterSpec params = AesGcmJceUtil.getParams(ciphertext, this.outputPrefix.length, 12);
        Cipher threadLocalCipher = AesGcmJceUtil.getThreadLocalCipher();
        threadLocalCipher.init(2, this.keySpec, params);
        if (associatedData != null && associatedData.length != 0) {
            threadLocalCipher.updateAAD(associatedData);
        }
        byte[] bArr2 = this.outputPrefix;
        return threadLocalCipher.doFinal(ciphertext, bArr2.length + 12, (ciphertext.length - bArr2.length) - 12);
    }
}
