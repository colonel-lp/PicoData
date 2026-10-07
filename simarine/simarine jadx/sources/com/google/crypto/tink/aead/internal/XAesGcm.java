package com.google.crypto.tink.aead.internal;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.aead.XAesGcmKey;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.prf.Prf;
import com.google.crypto.tink.subtle.PrfAesCmac;
import com.google.crypto.tink.subtle.Random;
import com.google.crypto.tink.util.Bytes;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class XAesGcm implements Aead {
    private static final int DERIVED_KEY_SIZE_IN_BYTES = 32;
    private static final int IV_SIZE_IN_BYTES = 12;
    private static final int MAX_SALT_SIZE_IN_BYTES = 12;
    private static final int MIN_SALT_SIZE_IN_BYTES = 8;
    private static final int TAG_SIZE_IN_BYTES = 16;
    private final Prf cmac;
    private final byte[] outputPrefix;
    private final int saltSize;

    private XAesGcm(final byte[] key, Bytes outputPrefix, int saltSize) throws GeneralSecurityException {
        this.cmac = new PrfAesCmac(key);
        this.outputPrefix = outputPrefix.toByteArray();
        this.saltSize = saltSize;
    }

    public static Aead create(XAesGcmKey key) throws GeneralSecurityException {
        if (key.getParameters().getSaltSizeBytes() < 8 || key.getParameters().getSaltSizeBytes() > 12) {
            throw new GeneralSecurityException("invalid salt size");
        }
        return new XAesGcm(key.getKeyBytes().toByteArray(InsecureSecretKeyAccess.get()), key.getOutputPrefix(), key.getParameters().getSaltSizeBytes());
    }

    private byte[] derivePerMessageKey(byte[] salt) throws GeneralSecurityException {
        byte[] bArr = {0, 1, 88, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] bArr2 = {0, 2, 88, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        if (salt.length > 12 || salt.length < 8) {
            throw new GeneralSecurityException("invalid salt size");
        }
        System.arraycopy(salt, 0, bArr, 4, salt.length);
        System.arraycopy(salt, 0, bArr2, 4, salt.length);
        byte[] bArr3 = new byte[32];
        System.arraycopy(this.cmac.compute(bArr, 16), 0, bArr3, 0, 16);
        System.arraycopy(this.cmac.compute(bArr2, 16), 0, bArr3, 16, 16);
        return bArr3;
    }

    @Override // com.google.crypto.tink.Aead
    public byte[] encrypt(final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
        if (plaintext == null) {
            throw new NullPointerException("plaintext is null");
        }
        byte[] bArrRandBytes = Random.randBytes(this.saltSize + 12);
        byte[] bArrCopyOf = Arrays.copyOf(bArrRandBytes, this.saltSize);
        int i = this.saltSize;
        byte[] bArrCopyOfRange = Arrays.copyOfRange(bArrRandBytes, i, i + 12);
        byte[] bArrEncrypt = new InsecureNonceAesGcmJce(derivePerMessageKey(bArrCopyOf)).encrypt(bArrCopyOfRange, plaintext, this.outputPrefix.length + this.saltSize + bArrCopyOfRange.length, associatedData);
        byte[] bArr = this.outputPrefix;
        System.arraycopy(bArr, 0, bArrEncrypt, 0, bArr.length);
        System.arraycopy(bArrRandBytes, 0, bArrEncrypt, this.outputPrefix.length, bArrRandBytes.length);
        return bArrEncrypt;
    }

    @Override // com.google.crypto.tink.Aead
    public byte[] decrypt(final byte[] ciphertext, final byte[] associatedData) throws GeneralSecurityException {
        if (ciphertext == null) {
            throw new NullPointerException("ciphertext is null");
        }
        int length = ciphertext.length;
        byte[] bArr = this.outputPrefix;
        if (length < bArr.length + this.saltSize + 28) {
            throw new GeneralSecurityException("ciphertext too short");
        }
        if (!Util.isPrefix(bArr, ciphertext)) {
            throw new GeneralSecurityException("Decryption failed (OutputPrefix mismatch).");
        }
        int length2 = this.outputPrefix.length + this.saltSize;
        InsecureNonceAesGcmJce insecureNonceAesGcmJce = new InsecureNonceAesGcmJce(derivePerMessageKey(Arrays.copyOfRange(ciphertext, this.outputPrefix.length, length2)));
        int i = length2 + 12;
        return insecureNonceAesGcmJce.decrypt(Arrays.copyOfRange(ciphertext, length2, i), ciphertext, i, associatedData);
    }
}
