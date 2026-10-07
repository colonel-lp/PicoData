package com.google.crypto.tink.hybrid.internal;

import com.google.crypto.tink.internal.ConscryptUtil;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.subtle.Bytes;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import javax.crypto.KeyAgreement;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class X25519Conscrypt implements X25519 {
    private static final int PRIVATE_KEY_LEN = 32;
    private static final int PUBLIC_KEY_LEN = 32;
    private static final byte[] x25519Pkcs8Prefix = {48, 46, 2, 1, 0, 48, 5, 6, 3, 43, 101, 110, 4, 34, 4, 32};
    private static final byte[] x25519X509Prefix = {48, 42, 48, 5, 6, 3, 43, 101, 110, 3, 33, 0};
    final Provider provider;

    private X25519Conscrypt(Provider provider) {
        this.provider = provider;
    }

    public static X25519 create() throws GeneralSecurityException {
        Provider providerProviderOrNull = ConscryptUtil.providerOrNull();
        if (providerProviderOrNull == null) {
            throw new GeneralSecurityException("Conscrypt is not available.");
        }
        KeyFactory.getInstance("XDH", providerProviderOrNull);
        KeyAgreement.getInstance("XDH", providerProviderOrNull);
        X25519Conscrypt x25519Conscrypt = new X25519Conscrypt(providerProviderOrNull);
        x25519Conscrypt.generateKeyPair();
        return x25519Conscrypt;
    }

    @Override // com.google.crypto.tink.hybrid.internal.X25519
    public X25519.KeyPair generateKeyPair() throws GeneralSecurityException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("XDH", this.provider);
        keyPairGenerator.initialize(255);
        KeyPair keyPairGenerateKeyPair = keyPairGenerator.generateKeyPair();
        byte[] encoded = keyPairGenerateKeyPair.getPrivate().getEncoded();
        int length = encoded.length;
        byte[] bArr = x25519Pkcs8Prefix;
        if (length != bArr.length + 32) {
            throw new GeneralSecurityException("Invalid encoded private key length");
        }
        if (!Util.isPrefix(bArr, encoded)) {
            throw new GeneralSecurityException("Invalid encoded private key prefix");
        }
        byte[] bArrCopyOfRange = Arrays.copyOfRange(encoded, bArr.length, encoded.length);
        byte[] encoded2 = keyPairGenerateKeyPair.getPublic().getEncoded();
        int length2 = encoded2.length;
        byte[] bArr2 = x25519X509Prefix;
        if (length2 != bArr2.length + 32) {
            throw new GeneralSecurityException("Invalid encoded public key length");
        }
        if (!Util.isPrefix(bArr2, encoded2)) {
            throw new GeneralSecurityException("Invalid encoded public key prefix");
        }
        return new X25519.KeyPair(bArrCopyOfRange, Arrays.copyOfRange(encoded2, bArr2.length, encoded2.length));
    }

    @Override // com.google.crypto.tink.hybrid.internal.X25519
    public byte[] computeSharedSecret(byte[] privateValue, byte[] peersPublicValue) throws GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance("XDH", this.provider);
        if (privateValue.length != 32) {
            throw new InvalidKeyException("Invalid X25519 private key");
        }
        PrivateKey privateKeyGeneratePrivate = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Bytes.concat(x25519Pkcs8Prefix, privateValue)));
        if (peersPublicValue.length != 32) {
            throw new InvalidKeyException("Invalid X25519 public key");
        }
        PublicKey publicKeyGeneratePublic = keyFactory.generatePublic(new X509EncodedKeySpec(Bytes.concat(x25519X509Prefix, peersPublicValue)));
        KeyAgreement keyAgreement = KeyAgreement.getInstance("XDH", this.provider);
        keyAgreement.init(privateKeyGeneratePrivate);
        keyAgreement.doPhase(publicKeyGeneratePublic, true);
        return keyAgreement.generateSecret();
    }
}
