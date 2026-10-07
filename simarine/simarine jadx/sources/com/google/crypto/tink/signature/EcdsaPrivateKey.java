package com.google.crypto.tink.signature;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.internal.EllipticCurvesUtil;
import com.google.crypto.tink.util.SecretBigInteger;
import com.google.errorprone.annotations.Immutable;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.spec.ECPoint;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class EcdsaPrivateKey extends SignaturePrivateKey {
    private final SecretBigInteger privateValue;
    private final EcdsaPublicKey publicKey;

    public static class Builder {
        private SecretBigInteger privateValue;
        private EcdsaPublicKey publicKey;

        private Builder() {
            this.publicKey = null;
            this.privateValue = null;
        }

        public Builder setPublicKey(EcdsaPublicKey publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        public Builder setPrivateValue(SecretBigInteger privateValue) {
            this.privateValue = privateValue;
            return this;
        }

        private static void validatePrivateValue(BigInteger privateValue, ECPoint publicPoint, EcdsaParameters.CurveType curveType) throws GeneralSecurityException {
            BigInteger order = curveType.toParameterSpec().getOrder();
            if (privateValue.signum() <= 0 || privateValue.compareTo(order) >= 0) {
                throw new GeneralSecurityException("Invalid private value");
            }
            if (!EllipticCurvesUtil.multiplyByGenerator(privateValue, curveType.toParameterSpec()).equals(publicPoint)) {
                throw new GeneralSecurityException("Invalid private value");
            }
        }

        public EcdsaPrivateKey build() throws GeneralSecurityException {
            if (this.publicKey == null) {
                throw new GeneralSecurityException("Cannot build without a ecdsa public key");
            }
            SecretBigInteger secretBigInteger = this.privateValue;
            if (secretBigInteger == null) {
                throw new GeneralSecurityException("Cannot build without a private value");
            }
            validatePrivateValue(secretBigInteger.getBigInteger(InsecureSecretKeyAccess.get()), this.publicKey.getPublicPoint(), this.publicKey.getParameters().getCurveType());
            return new EcdsaPrivateKey(this.publicKey, this.privateValue);
        }
    }

    private EcdsaPrivateKey(EcdsaPublicKey publicKey, SecretBigInteger privateValue) {
        this.publicKey = publicKey;
        this.privateValue = privateValue;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override // com.google.crypto.tink.signature.SignaturePrivateKey, com.google.crypto.tink.Key
    public EcdsaParameters getParameters() {
        return this.publicKey.getParameters();
    }

    @Override // com.google.crypto.tink.signature.SignaturePrivateKey, com.google.crypto.tink.PrivateKey
    public EcdsaPublicKey getPublicKey() {
        return this.publicKey;
    }

    public SecretBigInteger getPrivateValue() {
        return this.privateValue;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof EcdsaPrivateKey)) {
            return false;
        }
        EcdsaPrivateKey ecdsaPrivateKey = (EcdsaPrivateKey) o;
        return ecdsaPrivateKey.publicKey.equalsKey(this.publicKey) && this.privateValue.equalsSecretBigInteger(ecdsaPrivateKey.privateValue);
    }
}
