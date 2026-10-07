package com.google.crypto.tink.hybrid.internal;

import com.google.crypto.tink.aead.internal.InsecureNonceAesGcmJce;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
final class AesGcmHpkeAead implements HpkeAead {
    private final int keyLength;

    @Override // com.google.crypto.tink.hybrid.internal.HpkeAead
    public int getNonceLength() {
        return 12;
    }

    AesGcmHpkeAead(int keyLength) throws InvalidAlgorithmParameterException {
        if (keyLength != 16 && keyLength != 32) {
            throw new InvalidAlgorithmParameterException("Unsupported key length: " + keyLength);
        }
        this.keyLength = keyLength;
    }

    @Override // com.google.crypto.tink.hybrid.internal.HpkeAead
    public byte[] seal(byte[] key, byte[] nonce, byte[] plaintext, int ciphertextOffset, byte[] associatedData) throws GeneralSecurityException {
        if (key.length != this.keyLength) {
            throw new InvalidAlgorithmParameterException("Unexpected key length: " + key.length);
        }
        return new InsecureNonceAesGcmJce(key).encrypt(nonce, plaintext, ciphertextOffset, associatedData);
    }

    @Override // com.google.crypto.tink.hybrid.internal.HpkeAead
    public byte[] open(byte[] key, byte[] nonce, byte[] ciphertext, int ciphertextOffset, byte[] associatedData) throws GeneralSecurityException {
        if (key.length != this.keyLength) {
            throw new InvalidAlgorithmParameterException("Unexpected key length: " + key.length);
        }
        return new InsecureNonceAesGcmJce(key).decrypt(nonce, ciphertext, ciphertextOffset, associatedData);
    }

    @Override // com.google.crypto.tink.hybrid.internal.HpkeAead
    public byte[] getAeadId() throws GeneralSecurityException {
        int i = this.keyLength;
        if (i == 16) {
            return HpkeUtil.AES_128_GCM_AEAD_ID;
        }
        if (i == 32) {
            return HpkeUtil.AES_256_GCM_AEAD_ID;
        }
        throw new GeneralSecurityException("Could not determine HPKE AEAD ID");
    }

    @Override // com.google.crypto.tink.hybrid.internal.HpkeAead
    public int getKeyLength() {
        return this.keyLength;
    }
}
