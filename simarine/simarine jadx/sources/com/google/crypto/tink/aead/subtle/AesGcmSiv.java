package com.google.crypto.tink.aead.subtle;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.aead.AesGcmSivKey;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.subtle.Bytes;
import com.google.crypto.tink.subtle.EngineFactory;
import com.google.crypto.tink.subtle.Hex;
import com.google.crypto.tink.subtle.Random;
import com.google.crypto.tink.subtle.Validators;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/* JADX INFO: loaded from: classes2.dex */
public final class AesGcmSiv implements Aead {
    private static final int IV_SIZE_IN_BYTES = 12;
    private static final int TAG_SIZE_IN_BYTES = 16;
    private final SecretKey keySpec;
    private final byte[] outputPrefix;
    private static final byte[] TEST_PLAINTEXT = Hex.decode("7a806c");
    private static final byte[] TEST_AAD = Hex.decode("46bb91c3c5");
    private static final byte[] TEST_KEY = Hex.decode("36864200e0eaf5284d884a0e77d31646");
    private static final byte[] TEST_NOUNCE = Hex.decode("bae8e37fc83441b16034566b");
    private static final byte[] TEST_RESULT = Hex.decode("af60eb711bd85bc1e4d3e0a462e074eea428a8");
    private static final ThreadLocal<Cipher> localAesGcmSivCipher = new ThreadLocal<Cipher>() { // from class: com.google.crypto.tink.aead.subtle.AesGcmSiv.1
        /* JADX INFO: Access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public Cipher initialValue() {
            try {
                Cipher engineFactory = EngineFactory.CIPHER.getInstance("AES/GCM-SIV/NoPadding");
                if (AesGcmSiv.isAesGcmSivCipher(engineFactory)) {
                    return engineFactory;
                }
                return null;
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException(e);
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean isAesGcmSivCipher(Cipher cipher) {
        try {
            cipher.init(2, new SecretKeySpec(TEST_KEY, "AES"), getParams(TEST_NOUNCE));
            cipher.updateAAD(TEST_AAD);
            byte[] bArr = TEST_RESULT;
            return Bytes.equal(cipher.doFinal(bArr, 0, bArr.length), TEST_PLAINTEXT);
        } catch (GeneralSecurityException unused) {
            return false;
        }
    }

    public static Aead create(AesGcmSivKey key) throws GeneralSecurityException {
        return new AesGcmSiv(key.getKeyBytes().toByteArray(InsecureSecretKeyAccess.get()), key.getOutputPrefix().toByteArray());
    }

    private AesGcmSiv(byte[] key, byte[] outputPrefix) throws GeneralSecurityException {
        this.outputPrefix = outputPrefix;
        Validators.validateAesKeySize(key.length);
        this.keySpec = new SecretKeySpec(key, "AES");
    }

    public AesGcmSiv(final byte[] key) throws GeneralSecurityException {
        this(key, new byte[0]);
    }

    private Cipher getAesGcmSivCipher() throws GeneralSecurityException {
        Cipher cipher = localAesGcmSivCipher.get();
        if (cipher != null) {
            return cipher;
        }
        throw new GeneralSecurityException("AES GCM SIV cipher is not available or is invalid.");
    }

    private byte[] rawEncrypt(final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
        Cipher aesGcmSivCipher = getAesGcmSivCipher();
        if (plaintext.length > 2147483619) {
            throw new GeneralSecurityException("plaintext too long");
        }
        byte[] bArr = new byte[plaintext.length + 28];
        byte[] bArrRandBytes = Random.randBytes(12);
        System.arraycopy(bArrRandBytes, 0, bArr, 0, 12);
        aesGcmSivCipher.init(1, this.keySpec, getParams(bArrRandBytes));
        if (associatedData != null && associatedData.length != 0) {
            aesGcmSivCipher.updateAAD(associatedData);
        }
        int iDoFinal = aesGcmSivCipher.doFinal(plaintext, 0, plaintext.length, bArr, 12);
        if (iDoFinal == plaintext.length + 16) {
            return bArr;
        }
        throw new GeneralSecurityException(String.format("encryption failed; GCM tag must be %s bytes, but got only %s bytes", 16, Integer.valueOf(iDoFinal - plaintext.length)));
    }

    @Override // com.google.crypto.tink.Aead
    public byte[] encrypt(final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
        byte[] bArrRawEncrypt = rawEncrypt(plaintext, associatedData);
        byte[] bArr = this.outputPrefix;
        return bArr.length == 0 ? bArrRawEncrypt : Bytes.concat(bArr, bArrRawEncrypt);
    }

    private byte[] rawDecrypt(final byte[] ciphertext, final byte[] associatedData) throws GeneralSecurityException {
        Cipher aesGcmSivCipher = getAesGcmSivCipher();
        if (ciphertext.length < 28) {
            throw new GeneralSecurityException("ciphertext too short");
        }
        aesGcmSivCipher.init(2, this.keySpec, getParams(ciphertext, 0, 12));
        if (associatedData != null && associatedData.length != 0) {
            aesGcmSivCipher.updateAAD(associatedData);
        }
        return aesGcmSivCipher.doFinal(ciphertext, 12, ciphertext.length - 12);
    }

    @Override // com.google.crypto.tink.Aead
    public byte[] decrypt(final byte[] ciphertext, final byte[] associatedData) throws GeneralSecurityException {
        byte[] bArr = this.outputPrefix;
        if (bArr.length == 0) {
            return rawDecrypt(ciphertext, associatedData);
        }
        if (!Util.isPrefix(bArr, ciphertext)) {
            throw new GeneralSecurityException("Decryption failed (OutputPrefix mismatch).");
        }
        return rawDecrypt(Arrays.copyOfRange(ciphertext, this.outputPrefix.length, ciphertext.length), associatedData);
    }

    private static AlgorithmParameterSpec getParams(final byte[] iv) throws GeneralSecurityException {
        return getParams(iv, 0, iv.length);
    }

    private static AlgorithmParameterSpec getParams(final byte[] buf, int offset, int len) throws GeneralSecurityException {
        return new GCMParameterSpec(128, buf, offset, len);
    }
}
