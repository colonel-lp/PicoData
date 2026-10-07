package com.google.crypto.tink.hybrid.internal;

import com.google.crypto.tink.subtle.Bytes;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
final class X25519HpkeKem implements HpkeKem {
    private final HkdfHpkeKdf hkdf;
    private final X25519 x25519;

    @Immutable
    private static final class X25519Java implements X25519 {
        private X25519Java() {
        }

        @Override // com.google.crypto.tink.hybrid.internal.X25519
        public X25519.KeyPair generateKeyPair() throws GeneralSecurityException {
            byte[] bArrGeneratePrivateKey = com.google.crypto.tink.subtle.X25519.generatePrivateKey();
            return new X25519.KeyPair(bArrGeneratePrivateKey, com.google.crypto.tink.subtle.X25519.publicFromPrivate(bArrGeneratePrivateKey));
        }

        @Override // com.google.crypto.tink.hybrid.internal.X25519
        public byte[] computeSharedSecret(byte[] privateKey, byte[] publicKey) throws GeneralSecurityException {
            return com.google.crypto.tink.subtle.X25519.computeSharedSecret(privateKey, publicKey);
        }
    }

    X25519HpkeKem(HkdfHpkeKdf hkdf) {
        X25519 x25519Java;
        this.hkdf = hkdf;
        try {
            x25519Java = X25519Conscrypt.create();
        } catch (GeneralSecurityException unused) {
            x25519Java = new X25519Java();
        }
        this.x25519 = x25519Java;
    }

    private byte[] deriveKemSharedSecret(byte[] dhSharedSecret, byte[] senderEphemeralPublicKey, byte[] recipientPublicKey) throws GeneralSecurityException {
        return extractAndExpand(dhSharedSecret, Bytes.concat(senderEphemeralPublicKey, recipientPublicKey));
    }

    private byte[] deriveKemSharedSecret(byte[] dhSharedSecret, byte[] senderEphemeralPublicKey, byte[] recipientPublicKey, byte[] senderPublicKey) throws GeneralSecurityException {
        return extractAndExpand(dhSharedSecret, Bytes.concat(senderEphemeralPublicKey, recipientPublicKey, senderPublicKey));
    }

    private byte[] extractAndExpand(byte[] dhSharedSecret, byte[] kemContext) throws GeneralSecurityException {
        byte[] bArrKemSuiteId = HpkeUtil.kemSuiteId(HpkeUtil.X25519_HKDF_SHA256_KEM_ID);
        HkdfHpkeKdf hkdfHpkeKdf = this.hkdf;
        return hkdfHpkeKdf.extractAndExpand(null, dhSharedSecret, "eae_prk", kemContext, "shared_secret", bArrKemSuiteId, hkdfHpkeKdf.getMacLength());
    }

    HpkeKemEncapOutput encapsulateWithFixedEphemeralKey(byte[] recipientPublicKey, byte[] ephemeralPrivateKey, byte[] ephemeralPublicKey) throws GeneralSecurityException {
        return new HpkeKemEncapOutput(deriveKemSharedSecret(this.x25519.computeSharedSecret(ephemeralPrivateKey, recipientPublicKey), ephemeralPublicKey, recipientPublicKey), ephemeralPublicKey);
    }

    @Override // com.google.crypto.tink.hybrid.internal.HpkeKem
    public HpkeKemEncapOutput encapsulate(byte[] recipientPublicKey) throws GeneralSecurityException {
        X25519.KeyPair keyPairGenerateKeyPair = this.x25519.generateKeyPair();
        return encapsulateWithFixedEphemeralKey(recipientPublicKey, keyPairGenerateKeyPair.privateKey, keyPairGenerateKeyPair.publicKey);
    }

    HpkeKemEncapOutput authEncapsulateWithFixedEphemeralKey(byte[] recipientPublicKey, byte[] ephemeralPrivateKey, byte[] ephemeralPublicKey, HpkeKemPrivateKey senderPrivateKey) throws GeneralSecurityException {
        return new HpkeKemEncapOutput(deriveKemSharedSecret(Bytes.concat(this.x25519.computeSharedSecret(ephemeralPrivateKey, recipientPublicKey), this.x25519.computeSharedSecret(senderPrivateKey.getSerializedPrivate().toByteArray(), recipientPublicKey)), ephemeralPublicKey, recipientPublicKey, senderPrivateKey.getSerializedPublic().toByteArray()), ephemeralPublicKey);
    }

    @Override // com.google.crypto.tink.hybrid.internal.HpkeKem
    public HpkeKemEncapOutput authEncapsulate(byte[] recipientPublicKey, HpkeKemPrivateKey senderPrivateKey) throws GeneralSecurityException {
        X25519.KeyPair keyPairGenerateKeyPair = this.x25519.generateKeyPair();
        return authEncapsulateWithFixedEphemeralKey(recipientPublicKey, keyPairGenerateKeyPair.privateKey, keyPairGenerateKeyPair.publicKey, senderPrivateKey);
    }

    @Override // com.google.crypto.tink.hybrid.internal.HpkeKem
    public byte[] decapsulate(byte[] encapsulatedKey, HpkeKemPrivateKey recipientPrivateKey) throws GeneralSecurityException {
        return deriveKemSharedSecret(this.x25519.computeSharedSecret(recipientPrivateKey.getSerializedPrivate().toByteArray(), encapsulatedKey), encapsulatedKey, recipientPrivateKey.getSerializedPublic().toByteArray());
    }

    @Override // com.google.crypto.tink.hybrid.internal.HpkeKem
    public byte[] authDecapsulate(byte[] encapsulatedKey, HpkeKemPrivateKey recipientPrivateKey, byte[] senderPublicKey) throws GeneralSecurityException {
        byte[] byteArray = recipientPrivateKey.getSerializedPrivate().toByteArray();
        return deriveKemSharedSecret(Bytes.concat(this.x25519.computeSharedSecret(byteArray, encapsulatedKey), this.x25519.computeSharedSecret(byteArray, senderPublicKey)), encapsulatedKey, recipientPrivateKey.getSerializedPublic().toByteArray(), senderPublicKey);
    }

    @Override // com.google.crypto.tink.hybrid.internal.HpkeKem
    public byte[] getKemId() throws GeneralSecurityException {
        if (Arrays.equals(this.hkdf.getKdfId(), HpkeUtil.HKDF_SHA256_KDF_ID)) {
            return HpkeUtil.X25519_HKDF_SHA256_KEM_ID;
        }
        throw new GeneralSecurityException("Could not determine HPKE KEM ID");
    }
}
