package com.google.crypto.tink.hybrid;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.internal.EllipticCurvesUtil;
import com.google.crypto.tink.subtle.EllipticCurves;
import com.google.crypto.tink.subtle.X25519;
import com.google.crypto.tink.util.SecretBigInteger;
import com.google.crypto.tink.util.SecretBytes;
import com.google.errorprone.annotations.Immutable;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.util.Arrays;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class EciesPrivateKey extends HybridPrivateKey {

    @Nullable
    private final SecretBigInteger nistPrivateKeyValue;
    private final EciesPublicKey publicKey;

    @Nullable
    private final SecretBytes x25519PrivateKeyBytes;

    private EciesPrivateKey(EciesPublicKey publicKey, @Nullable SecretBigInteger nistPrivateKeyValue, @Nullable SecretBytes x25519PrivateKeyBytes) {
        this.publicKey = publicKey;
        this.nistPrivateKeyValue = nistPrivateKeyValue;
        this.x25519PrivateKeyBytes = x25519PrivateKeyBytes;
    }

    private static ECParameterSpec toParameterSpecNistCurve(EciesParameters.CurveType curveType) {
        if (curveType == EciesParameters.CurveType.NIST_P256) {
            return EllipticCurves.getNistP256Params();
        }
        if (curveType == EciesParameters.CurveType.NIST_P384) {
            return EllipticCurves.getNistP384Params();
        }
        if (curveType == EciesParameters.CurveType.NIST_P521) {
            return EllipticCurves.getNistP521Params();
        }
        throw new IllegalArgumentException("Unable to determine NIST curve type for " + curveType);
    }

    private static void validateNistPrivateKeyValue(BigInteger privateValue, ECPoint publicPoint, EciesParameters.CurveType curveType) throws GeneralSecurityException {
        BigInteger order = toParameterSpecNistCurve(curveType).getOrder();
        if (privateValue.signum() <= 0 || privateValue.compareTo(order) >= 0) {
            throw new GeneralSecurityException("Invalid private value");
        }
        if (!EllipticCurvesUtil.multiplyByGenerator(privateValue, toParameterSpecNistCurve(curveType)).equals(publicPoint)) {
            throw new GeneralSecurityException("Invalid private value");
        }
    }

    private static void validateX25519PrivateKeyBytes(byte[] privateKeyBytes, byte[] publicKeyBytes) throws GeneralSecurityException {
        if (privateKeyBytes.length != 32) {
            throw new GeneralSecurityException("Private key bytes length for X25519 curve must be 32");
        }
        if (!Arrays.equals(X25519.publicFromPrivate(privateKeyBytes), publicKeyBytes)) {
            throw new GeneralSecurityException("Invalid private key for public key.");
        }
    }

    public static EciesPrivateKey createForCurveX25519(EciesPublicKey publicKey, SecretBytes x25519PrivateKeyBytes) throws GeneralSecurityException {
        if (publicKey == null) {
            throw new GeneralSecurityException("ECIES private key cannot be constructed without an ECIES public key");
        }
        if (publicKey.getX25519CurvePointBytes() == null) {
            throw new GeneralSecurityException("ECIES private key for X25519 curve cannot be constructed with NIST-curve public key");
        }
        if (x25519PrivateKeyBytes == null) {
            throw new GeneralSecurityException("ECIES private key cannot be constructed without secret");
        }
        validateX25519PrivateKeyBytes(x25519PrivateKeyBytes.toByteArray(InsecureSecretKeyAccess.get()), publicKey.getX25519CurvePointBytes().toByteArray());
        return new EciesPrivateKey(publicKey, null, x25519PrivateKeyBytes);
    }

    public static EciesPrivateKey createForNistCurve(EciesPublicKey publicKey, SecretBigInteger nistPrivateKeyValue) throws GeneralSecurityException {
        if (publicKey == null) {
            throw new GeneralSecurityException("ECIES private key cannot be constructed without an ECIES public key");
        }
        if (publicKey.getNistCurvePoint() == null) {
            throw new GeneralSecurityException("ECIES private key for NIST curve cannot be constructed with X25519-curve public key");
        }
        if (nistPrivateKeyValue == null) {
            throw new GeneralSecurityException("ECIES private key cannot be constructed without secret");
        }
        validateNistPrivateKeyValue(nistPrivateKeyValue.getBigInteger(InsecureSecretKeyAccess.get()), publicKey.getNistCurvePoint(), publicKey.getParameters().getCurveType());
        return new EciesPrivateKey(publicKey, nistPrivateKeyValue, null);
    }

    @Nullable
    public SecretBytes getX25519PrivateKeyBytes() {
        return this.x25519PrivateKeyBytes;
    }

    @Nullable
    public SecretBigInteger getNistPrivateKeyValue() {
        return this.nistPrivateKeyValue;
    }

    @Override // com.google.crypto.tink.hybrid.HybridPrivateKey, com.google.crypto.tink.Key
    public EciesParameters getParameters() {
        return this.publicKey.getParameters();
    }

    @Override // com.google.crypto.tink.hybrid.HybridPrivateKey, com.google.crypto.tink.PrivateKey
    public EciesPublicKey getPublicKey() {
        return this.publicKey;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof EciesPrivateKey)) {
            return false;
        }
        EciesPrivateKey eciesPrivateKey = (EciesPrivateKey) o;
        if (!this.publicKey.equalsKey(eciesPrivateKey.publicKey)) {
            return false;
        }
        SecretBytes secretBytes = this.x25519PrivateKeyBytes;
        if (secretBytes == null && eciesPrivateKey.x25519PrivateKeyBytes == null) {
            return this.nistPrivateKeyValue.equalsSecretBigInteger(eciesPrivateKey.nistPrivateKeyValue);
        }
        return secretBytes.equalsSecretBytes(eciesPrivateKey.x25519PrivateKeyBytes);
    }
}
