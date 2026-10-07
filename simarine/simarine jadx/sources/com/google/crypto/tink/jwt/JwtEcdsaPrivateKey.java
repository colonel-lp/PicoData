package com.google.crypto.tink.jwt;

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
public final class JwtEcdsaPrivateKey extends JwtSignaturePrivateKey {
    public final SecretBigInteger privateKeyValue;
    public final JwtEcdsaPublicKey publicKey;

    private static void validatePrivateValue(BigInteger privateValue, ECPoint publicPoint, JwtEcdsaParameters.Algorithm algorithm) throws GeneralSecurityException {
        BigInteger order = algorithm.getECParameterSpec().getOrder();
        if (privateValue.signum() <= 0 || privateValue.compareTo(order) >= 0) {
            throw new GeneralSecurityException("Invalid private value");
        }
        if (!EllipticCurvesUtil.multiplyByGenerator(privateValue, algorithm.getECParameterSpec()).equals(publicPoint)) {
            throw new GeneralSecurityException("Invalid private value");
        }
    }

    public static JwtEcdsaPrivateKey create(JwtEcdsaPublicKey publicKey, SecretBigInteger privateValue) throws GeneralSecurityException {
        validatePrivateValue(privateValue.getBigInteger(InsecureSecretKeyAccess.get()), publicKey.getPublicPoint(), publicKey.getParameters().getAlgorithm());
        return new JwtEcdsaPrivateKey(publicKey, privateValue);
    }

    private JwtEcdsaPrivateKey(JwtEcdsaPublicKey publicKey, SecretBigInteger privateKeyValue) {
        this.publicKey = publicKey;
        this.privateKeyValue = privateKeyValue;
    }

    public SecretBigInteger getPrivateValue() {
        return this.privateKeyValue;
    }

    @Override // com.google.crypto.tink.jwt.JwtSignaturePrivateKey, com.google.crypto.tink.Key
    public JwtEcdsaParameters getParameters() {
        return this.publicKey.getParameters();
    }

    @Override // com.google.crypto.tink.jwt.JwtSignaturePrivateKey, com.google.crypto.tink.PrivateKey
    public JwtEcdsaPublicKey getPublicKey() {
        return this.publicKey;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof JwtEcdsaPrivateKey)) {
            return false;
        }
        JwtEcdsaPrivateKey jwtEcdsaPrivateKey = (JwtEcdsaPrivateKey) o;
        return jwtEcdsaPrivateKey.publicKey.equalsKey(this.publicKey) && this.privateKeyValue.equalsSecretBigInteger(jwtEcdsaPrivateKey.privateKeyValue);
    }
}
