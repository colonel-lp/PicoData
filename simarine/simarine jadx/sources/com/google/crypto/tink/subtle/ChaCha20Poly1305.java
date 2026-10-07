package com.google.crypto.tink.subtle;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.aead.ChaCha20Poly1305Key;
import com.google.crypto.tink.aead.internal.InsecureNonceChaCha20Poly1305;
import com.google.crypto.tink.internal.Util;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
public final class ChaCha20Poly1305 implements Aead {
    private final InsecureNonceChaCha20Poly1305 cipher;
    private final byte[] outputPrefix;

    private ChaCha20Poly1305(final byte[] key, final byte[] outputPrefix) throws GeneralSecurityException {
        this.cipher = new InsecureNonceChaCha20Poly1305(key);
        this.outputPrefix = outputPrefix;
    }

    public ChaCha20Poly1305(final byte[] key) throws GeneralSecurityException {
        this(key, new byte[0]);
    }

    public static Aead create(ChaCha20Poly1305Key key) throws GeneralSecurityException {
        return new ChaCha20Poly1305(key.getKeyBytes().toByteArray(InsecureSecretKeyAccess.get()), key.getOutputPrefix().toByteArray());
    }

    private byte[] rawEncrypt(final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
        ByteBuffer byteBufferAllocate = ByteBuffer.allocate(plaintext.length + 28);
        byte[] bArrRandBytes = Random.randBytes(12);
        byteBufferAllocate.put(bArrRandBytes);
        this.cipher.encrypt(byteBufferAllocate, bArrRandBytes, plaintext, associatedData);
        return byteBufferAllocate.array();
    }

    @Override // com.google.crypto.tink.Aead
    public byte[] encrypt(final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
        byte[] bArrRawEncrypt = rawEncrypt(plaintext, associatedData);
        byte[] bArr = this.outputPrefix;
        return bArr.length == 0 ? bArrRawEncrypt : Bytes.concat(bArr, bArrRawEncrypt);
    }

    private byte[] rawDecrypt(final byte[] ciphertext, final byte[] associatedData) throws GeneralSecurityException {
        if (ciphertext.length < 28) {
            throw new GeneralSecurityException("ciphertext too short");
        }
        byte[] bArrCopyOf = Arrays.copyOf(ciphertext, 12);
        return this.cipher.decrypt(ByteBuffer.wrap(ciphertext, 12, ciphertext.length - 12), bArrCopyOf, associatedData);
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
}
