package com.google.crypto.tink.hybrid.internal;

import com.google.crypto.tink.HybridDecrypt;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.hybrid.HpkeParameters;
import com.google.crypto.tink.hybrid.HpkePrivateKey;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.util.Bytes;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class HpkeDecrypt implements HybridDecrypt {
    private static final byte[] EMPTY_ASSOCIATED_DATA = new byte[0];
    private final HpkeAead aead;
    private final int encapsulatedKeyLength;
    private final HpkeKdf kdf;
    private final HpkeKem kem;
    private final byte[] outputPrefix;
    private final HpkeKemPrivateKey recipientPrivateKey;

    private HpkeDecrypt(HpkeKemPrivateKey recipientPrivateKey, HpkeKem kem, HpkeKdf kdf, HpkeAead aead, int encapsulatedKeyLength, Bytes outputPrefix) {
        this.recipientPrivateKey = recipientPrivateKey;
        this.kem = kem;
        this.kdf = kdf;
        this.aead = aead;
        this.encapsulatedKeyLength = encapsulatedKeyLength;
        this.outputPrefix = outputPrefix.toByteArray();
    }

    private static int encodingSizeInBytes(HpkeParameters.KemId kemId) throws GeneralSecurityException {
        if (kemId.equals(HpkeParameters.KemId.DHKEM_X25519_HKDF_SHA256)) {
            return 32;
        }
        if (kemId.equals(HpkeParameters.KemId.DHKEM_P256_HKDF_SHA256)) {
            return 65;
        }
        if (kemId.equals(HpkeParameters.KemId.DHKEM_P384_HKDF_SHA384)) {
            return 97;
        }
        if (kemId.equals(HpkeParameters.KemId.DHKEM_P521_HKDF_SHA512)) {
            return 133;
        }
        throw new GeneralSecurityException("Unrecognized HPKE KEM identifier");
    }

    private static HpkeKemPrivateKey createHpkeKemPrivateKey(HpkePrivateKey privateKey) throws GeneralSecurityException {
        HpkeParameters.KemId kemId = privateKey.getParameters().getKemId();
        if (kemId.equals(HpkeParameters.KemId.DHKEM_X25519_HKDF_SHA256) || kemId.equals(HpkeParameters.KemId.DHKEM_P256_HKDF_SHA256) || kemId.equals(HpkeParameters.KemId.DHKEM_P384_HKDF_SHA384) || kemId.equals(HpkeParameters.KemId.DHKEM_P521_HKDF_SHA512)) {
            return new HpkeKemPrivateKey(Bytes.copyFrom(privateKey.getPrivateKeyBytes().toByteArray(InsecureSecretKeyAccess.get())), privateKey.getPublicKey().getPublicKeyBytes());
        }
        throw new GeneralSecurityException("Unrecognized HPKE KEM identifier");
    }

    public static HybridDecrypt create(HpkePrivateKey privateKey) throws GeneralSecurityException {
        HpkeParameters parameters = privateKey.getParameters();
        return new HpkeDecrypt(createHpkeKemPrivateKey(privateKey), HpkePrimitiveFactory.createKem(parameters.getKemId()), HpkePrimitiveFactory.createKdf(parameters.getKdfId()), HpkePrimitiveFactory.createAead(parameters.getAeadId()), encodingSizeInBytes(parameters.getKemId()), privateKey.getOutputPrefix());
    }

    @Override // com.google.crypto.tink.HybridDecrypt
    public byte[] decrypt(final byte[] ciphertext, final byte[] contextInfo) throws GeneralSecurityException {
        byte[] bArr = this.outputPrefix;
        int length = bArr.length + this.encapsulatedKeyLength;
        if (ciphertext.length < length) {
            throw new GeneralSecurityException("Ciphertext is too short.");
        }
        if (!Util.isPrefix(bArr, ciphertext)) {
            throw new GeneralSecurityException("Invalid ciphertext (output prefix mismatch)");
        }
        if (contextInfo == null) {
            contextInfo = new byte[0];
        }
        return HpkeContext.createRecipientContext(Arrays.copyOfRange(ciphertext, this.outputPrefix.length, length), this.recipientPrivateKey, this.kem, this.kdf, this.aead, contextInfo).open(ciphertext, length, EMPTY_ASSOCIATED_DATA);
    }
}
