package com.google.crypto.tink.subtle;

import com.google.crypto.tink.shaded.protobuf.ByteString;
import java.security.GeneralSecurityException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;

/* JADX INFO: loaded from: classes2.dex */
public final class SelfKeyTestValidators {
    private static final ByteString TEST_MESSAGE = ByteString.copyFromUtf8("Tink and Wycheproof.");

    private SelfKeyTestValidators() {
    }

    public static final void validateRsaSsaPss(RSAPrivateCrtKey privateKey, RSAPublicKey publicKey, Enums.HashType sigHash, Enums.HashType mgf1Hash, int saltLength) throws GeneralSecurityException {
        RsaSsaPssSignJce rsaSsaPssSignJce = new RsaSsaPssSignJce(privateKey, sigHash, mgf1Hash, saltLength);
        RsaSsaPssVerifyJce rsaSsaPssVerifyJce = new RsaSsaPssVerifyJce(publicKey, sigHash, mgf1Hash, saltLength);
        try {
            ByteString byteString = TEST_MESSAGE;
            rsaSsaPssVerifyJce.verify(rsaSsaPssSignJce.sign(byteString.toByteArray()), byteString.toByteArray());
        } catch (GeneralSecurityException e) {
            throw new GeneralSecurityException("RSA PSS signing with private key followed by verifying with public key failed. The key may be corrupted.", e);
        }
    }

    public static final void validateRsaSsaPkcs1(RSAPrivateCrtKey privateKey, RSAPublicKey publicKey, Enums.HashType sigHash) throws GeneralSecurityException {
        RsaSsaPkcs1SignJce rsaSsaPkcs1SignJce = new RsaSsaPkcs1SignJce(privateKey, sigHash);
        RsaSsaPkcs1VerifyJce rsaSsaPkcs1VerifyJce = new RsaSsaPkcs1VerifyJce(publicKey, sigHash);
        try {
            ByteString byteString = TEST_MESSAGE;
            rsaSsaPkcs1VerifyJce.verify(rsaSsaPkcs1SignJce.sign(byteString.toByteArray()), byteString.toByteArray());
        } catch (GeneralSecurityException e) {
            throw new GeneralSecurityException("RSA PKCS1 signing with private key followed by verifying with public key failed. The key may be corrupted.", e);
        }
    }

    public static final void validateEcdsa(ECPrivateKey privateKey, ECPublicKey publicKey, Enums.HashType hash, EllipticCurves.EcdsaEncoding encoding) throws GeneralSecurityException {
        EcdsaSignJce ecdsaSignJce = new EcdsaSignJce(privateKey, hash, encoding);
        EcdsaVerifyJce ecdsaVerifyJce = new EcdsaVerifyJce(publicKey, hash, encoding);
        try {
            ByteString byteString = TEST_MESSAGE;
            ecdsaVerifyJce.verify(ecdsaSignJce.sign(byteString.toByteArray()), byteString.toByteArray());
        } catch (GeneralSecurityException e) {
            throw new GeneralSecurityException("ECDSA signing with private key followed by verifying with public key failed. The key may be corrupted.", e);
        }
    }
}
