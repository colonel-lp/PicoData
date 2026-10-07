package com.google.crypto.tink.signature;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.internal.Ed25519;
import com.google.crypto.tink.util.SecretBytes;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class Ed25519PrivateKey extends SignaturePrivateKey {
    private final SecretBytes privateKeyBytes;
    private final Ed25519PublicKey publicKey;

    private Ed25519PrivateKey(Ed25519PublicKey publicKey, SecretBytes privateKeyBytes) {
        this.publicKey = publicKey;
        this.privateKeyBytes = privateKeyBytes;
    }

    public static Ed25519PrivateKey create(Ed25519PublicKey publicKey, SecretBytes privateKeyBytes) throws GeneralSecurityException {
        if (publicKey == null) {
            throw new GeneralSecurityException("Ed25519 key cannot be constructed without an Ed25519 public key");
        }
        if (privateKeyBytes.size() != 32) {
            throw new GeneralSecurityException("Ed25519 key must be constructed with key of length 32 bytes, not " + privateKeyBytes.size());
        }
        if (!Arrays.equals(publicKey.getPublicKeyBytes().toByteArray(), Ed25519.scalarMultWithBaseToBytes(Ed25519.getHashedScalar(privateKeyBytes.toByteArray(InsecureSecretKeyAccess.get()))))) {
            throw new GeneralSecurityException("Ed25519 keys mismatch");
        }
        return new Ed25519PrivateKey(publicKey, privateKeyBytes);
    }

    @Override // com.google.crypto.tink.signature.SignaturePrivateKey, com.google.crypto.tink.Key
    public Ed25519Parameters getParameters() {
        return this.publicKey.getParameters();
    }

    @Override // com.google.crypto.tink.signature.SignaturePrivateKey, com.google.crypto.tink.PrivateKey
    public Ed25519PublicKey getPublicKey() {
        return this.publicKey;
    }

    public SecretBytes getPrivateKeyBytes() {
        return this.privateKeyBytes;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof Ed25519PrivateKey)) {
            return false;
        }
        Ed25519PrivateKey ed25519PrivateKey = (Ed25519PrivateKey) o;
        return ed25519PrivateKey.publicKey.equalsKey(this.publicKey) && this.privateKeyBytes.equalsSecretBytes(ed25519PrivateKey.privateKeyBytes);
    }
}
