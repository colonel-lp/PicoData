package com.google.crypto.tink.aead.internal;

import com.google.crypto.tink.config.internal.TinkFipsUtil;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import javax.crypto.AEADBadTagException;

/* JADX INFO: loaded from: classes2.dex */
abstract class InsecureNonceChaCha20Poly1305Base {
    public static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_NOT_FIPS;
    private final InsecureNonceChaCha20Base chacha20;
    private final InsecureNonceChaCha20Base macKeyChaCha20;

    abstract InsecureNonceChaCha20Base newChaCha20Instance(final byte[] key, int initialCounter) throws InvalidKeyException;

    public InsecureNonceChaCha20Poly1305Base(final byte[] key) throws GeneralSecurityException {
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Can not use ChaCha20Poly1305 in FIPS-mode.");
        }
        this.chacha20 = newChaCha20Instance(key, 1);
        this.macKeyChaCha20 = newChaCha20Instance(key, 0);
    }

    public byte[] encrypt(final byte[] nonce, final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
        if (plaintext.length > 2147483631) {
            throw new GeneralSecurityException("plaintext too long");
        }
        ByteBuffer byteBufferAllocate = ByteBuffer.allocate(plaintext.length + 16);
        encrypt(byteBufferAllocate, nonce, plaintext, associatedData);
        return byteBufferAllocate.array();
    }

    public void encrypt(ByteBuffer output, final byte[] nonce, final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
        if (output.remaining() < plaintext.length + 16) {
            throw new IllegalArgumentException("Given ByteBuffer output is too small");
        }
        int iPosition = output.position();
        this.chacha20.encrypt(output, nonce, plaintext);
        output.position(iPosition);
        output.limit(output.limit() - 16);
        if (associatedData == null) {
            associatedData = new byte[0];
        }
        byte[] bArrComputeMac = Poly1305.computeMac(getMacKey(nonce), macDataRfc8439(associatedData, output));
        output.limit(output.limit() + 16);
        output.put(bArrComputeMac);
    }

    public byte[] decrypt(final byte[] nonce, final byte[] ciphertext, final byte[] associatedData) throws GeneralSecurityException {
        return decrypt(ByteBuffer.wrap(ciphertext), nonce, associatedData);
    }

    public byte[] decrypt(ByteBuffer ciphertext, final byte[] nonce, final byte[] associatedData) throws GeneralSecurityException {
        if (ciphertext.remaining() < 16) {
            throw new GeneralSecurityException("ciphertext too short");
        }
        int iPosition = ciphertext.position();
        byte[] bArr = new byte[16];
        ciphertext.position(ciphertext.limit() - 16);
        ciphertext.get(bArr);
        ciphertext.position(iPosition);
        ciphertext.limit(ciphertext.limit() - 16);
        if (associatedData == null) {
            associatedData = new byte[0];
        }
        try {
            Poly1305.verifyMac(getMacKey(nonce), macDataRfc8439(associatedData, ciphertext), bArr);
            ciphertext.position(iPosition);
            return this.chacha20.decrypt(nonce, ciphertext);
        } catch (GeneralSecurityException e) {
            throw new AEADBadTagException(e.toString());
        }
    }

    private byte[] getMacKey(final byte[] nonce) throws GeneralSecurityException {
        ByteBuffer byteBufferChacha20Block = this.macKeyChaCha20.chacha20Block(nonce, 0);
        byte[] bArr = new byte[32];
        byteBufferChacha20Block.get(bArr);
        return bArr;
    }

    private static byte[] macDataRfc8439(final byte[] aad, ByteBuffer ciphertext) {
        int length = aad.length % 16 == 0 ? aad.length : (aad.length + 16) - (aad.length % 16);
        int iRemaining = ciphertext.remaining();
        int i = iRemaining % 16;
        int i2 = (i == 0 ? iRemaining : (iRemaining + 16) - i) + length;
        ByteBuffer byteBufferOrder = ByteBuffer.allocate(i2 + 16).order(ByteOrder.LITTLE_ENDIAN);
        byteBufferOrder.put(aad);
        byteBufferOrder.position(length);
        byteBufferOrder.put(ciphertext);
        byteBufferOrder.position(i2);
        byteBufferOrder.putLong(aad.length);
        byteBufferOrder.putLong(iRemaining);
        return byteBufferOrder.array();
    }
}
