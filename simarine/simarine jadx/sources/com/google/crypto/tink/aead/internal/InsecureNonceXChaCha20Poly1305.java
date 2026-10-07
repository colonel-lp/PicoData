package com.google.crypto.tink.aead.internal;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;

/* JADX INFO: loaded from: classes2.dex */
public final class InsecureNonceXChaCha20Poly1305 extends InsecureNonceChaCha20Poly1305Base {
    @Override // com.google.crypto.tink.aead.internal.InsecureNonceChaCha20Poly1305Base
    public /* bridge */ /* synthetic */ byte[] decrypt(ByteBuffer ciphertext, byte[] nonce, byte[] associatedData) throws GeneralSecurityException {
        return super.decrypt(ciphertext, nonce, associatedData);
    }

    @Override // com.google.crypto.tink.aead.internal.InsecureNonceChaCha20Poly1305Base
    public /* bridge */ /* synthetic */ byte[] decrypt(byte[] nonce, byte[] ciphertext, byte[] associatedData) throws GeneralSecurityException {
        return super.decrypt(nonce, ciphertext, associatedData);
    }

    @Override // com.google.crypto.tink.aead.internal.InsecureNonceChaCha20Poly1305Base
    public /* bridge */ /* synthetic */ void encrypt(ByteBuffer output, byte[] nonce, byte[] plaintext, byte[] associatedData) throws GeneralSecurityException {
        super.encrypt(output, nonce, plaintext, associatedData);
    }

    @Override // com.google.crypto.tink.aead.internal.InsecureNonceChaCha20Poly1305Base
    public /* bridge */ /* synthetic */ byte[] encrypt(byte[] nonce, byte[] plaintext, byte[] associatedData) throws GeneralSecurityException {
        return super.encrypt(nonce, plaintext, associatedData);
    }

    public InsecureNonceXChaCha20Poly1305(final byte[] key) throws GeneralSecurityException {
        super(key);
    }

    @Override // com.google.crypto.tink.aead.internal.InsecureNonceChaCha20Poly1305Base
    InsecureNonceChaCha20Base newChaCha20Instance(final byte[] key, int initialCounter) throws InvalidKeyException {
        return new InsecureNonceXChaCha20(key, initialCounter);
    }
}
