package com.google.crypto.tink.subtle;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.Mac;
import com.google.crypto.tink.aead.AesCtrHmacAeadKey;
import com.google.crypto.tink.internal.Util;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.spec.SecretKeySpec;

/* JADX INFO: loaded from: classes2.dex */
public final class EncryptThenAuthenticate implements Aead {
    private final IndCpaCipher cipher;
    private final Mac mac;
    private final int macLength;
    private final byte[] outputPrefix;

    public EncryptThenAuthenticate(final IndCpaCipher cipher, final Mac mac, int macLength) {
        this(cipher, mac, macLength, new byte[0]);
    }

    private EncryptThenAuthenticate(IndCpaCipher cipher, Mac mac, int macLength, byte[] outputPrefix) {
        this.cipher = cipher;
        this.mac = mac;
        this.macLength = macLength;
        this.outputPrefix = outputPrefix;
    }

    public static Aead create(AesCtrHmacAeadKey key) throws GeneralSecurityException {
        return new EncryptThenAuthenticate(new AesCtrJceCipher(key.getAesKeyBytes().toByteArray(InsecureSecretKeyAccess.get()), key.getParameters().getIvSizeBytes()), new PrfMac(new PrfHmacJce("HMAC" + key.getParameters().getHashType(), new SecretKeySpec(key.getHmacKeyBytes().toByteArray(InsecureSecretKeyAccess.get()), "HMAC")), key.getParameters().getTagSizeBytes()), key.getParameters().getTagSizeBytes(), key.getOutputPrefix().toByteArray());
    }

    public static Aead newAesCtrHmac(final byte[] aesCtrKey, int ivSize, String hmacAlgorithm, final byte[] hmacKey, int tagSize) throws GeneralSecurityException {
        return new EncryptThenAuthenticate(new AesCtrJceCipher(aesCtrKey, ivSize), new PrfMac(new PrfHmacJce(hmacAlgorithm, new SecretKeySpec(hmacKey, "HMAC")), tagSize), tagSize);
    }

    @Override // com.google.crypto.tink.Aead
    public byte[] encrypt(final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
        byte[] bArrEncrypt = this.cipher.encrypt(plaintext);
        if (associatedData == null) {
            associatedData = new byte[0];
        }
        return Bytes.concat(this.outputPrefix, bArrEncrypt, this.mac.computeMac(Bytes.concat(associatedData, bArrEncrypt, Arrays.copyOf(ByteBuffer.allocate(8).putLong(((long) associatedData.length) * 8).array(), 8))));
    }

    @Override // com.google.crypto.tink.Aead
    public byte[] decrypt(final byte[] ciphertext, final byte[] associatedData) throws GeneralSecurityException {
        int length = ciphertext.length;
        int i = this.macLength;
        byte[] bArr = this.outputPrefix;
        if (length < i + bArr.length) {
            throw new GeneralSecurityException("Decryption failed (ciphertext too short).");
        }
        if (!Util.isPrefix(bArr, ciphertext)) {
            throw new GeneralSecurityException("Decryption failed (OutputPrefix mismatch).");
        }
        byte[] bArrCopyOfRange = Arrays.copyOfRange(ciphertext, this.outputPrefix.length, ciphertext.length - this.macLength);
        byte[] bArrCopyOfRange2 = Arrays.copyOfRange(ciphertext, ciphertext.length - this.macLength, ciphertext.length);
        if (associatedData == null) {
            associatedData = new byte[0];
        }
        this.mac.verifyMac(bArrCopyOfRange2, Bytes.concat(associatedData, bArrCopyOfRange, Arrays.copyOf(ByteBuffer.allocate(8).putLong(((long) associatedData.length) * 8).array(), 8)));
        return this.cipher.decrypt(bArrCopyOfRange);
    }
}
