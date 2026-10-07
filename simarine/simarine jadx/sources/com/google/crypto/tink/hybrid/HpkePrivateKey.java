package com.google.crypto.tink.hybrid;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.internal.BigIntegerEncoding;
import com.google.crypto.tink.internal.EllipticCurvesUtil;
import com.google.crypto.tink.subtle.EllipticCurves;
import com.google.crypto.tink.subtle.X25519;
import com.google.crypto.tink.util.SecretBytes;
import com.google.errorprone.annotations.Immutable;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.spec.ECParameterSpec;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class HpkePrivateKey extends HybridPrivateKey {
    private final SecretBytes privateKeyBytes;
    private final HpkePublicKey publicKey;

    private HpkePrivateKey(HpkePublicKey publicKey, SecretBytes privateKeyBytes) {
        this.publicKey = publicKey;
        this.privateKeyBytes = privateKeyBytes;
    }

    private static void validatePrivateKeyByteLength(HpkeParameters.KemId kemId, SecretBytes privateKeyBytes) throws GeneralSecurityException {
        int size = privateKeyBytes.size();
        String str = "Encoded private key byte length for " + kemId + " must be %d, not " + size;
        if (kemId == HpkeParameters.KemId.DHKEM_P256_HKDF_SHA256) {
            if (size != 32) {
                throw new GeneralSecurityException(String.format(str, 32));
            }
            return;
        }
        if (kemId == HpkeParameters.KemId.DHKEM_P384_HKDF_SHA384) {
            if (size != 48) {
                throw new GeneralSecurityException(String.format(str, 48));
            }
        } else if (kemId == HpkeParameters.KemId.DHKEM_P521_HKDF_SHA512) {
            if (size != 66) {
                throw new GeneralSecurityException(String.format(str, 66));
            }
        } else if (kemId == HpkeParameters.KemId.DHKEM_X25519_HKDF_SHA256) {
            if (size != 32) {
                throw new GeneralSecurityException(String.format(str, 32));
            }
        } else {
            throw new GeneralSecurityException("Unable to validate private key length for " + kemId);
        }
    }

    private static boolean isNistKem(HpkeParameters.KemId kemId) {
        return kemId == HpkeParameters.KemId.DHKEM_P256_HKDF_SHA256 || kemId == HpkeParameters.KemId.DHKEM_P384_HKDF_SHA384 || kemId == HpkeParameters.KemId.DHKEM_P521_HKDF_SHA512;
    }

    private static ECParameterSpec getNistCurveParams(HpkeParameters.KemId kemId) {
        if (kemId == HpkeParameters.KemId.DHKEM_P256_HKDF_SHA256) {
            return EllipticCurves.getNistP256Params();
        }
        if (kemId == HpkeParameters.KemId.DHKEM_P384_HKDF_SHA384) {
            return EllipticCurves.getNistP384Params();
        }
        if (kemId == HpkeParameters.KemId.DHKEM_P521_HKDF_SHA512) {
            return EllipticCurves.getNistP521Params();
        }
        throw new IllegalArgumentException("Unable to determine NIST curve params for " + kemId);
    }

    private static void validateKeyPair(HpkeParameters.KemId kemId, byte[] publicKeyBytes, byte[] privateKeyBytes) throws GeneralSecurityException {
        if (isNistKem(kemId)) {
            ECParameterSpec nistCurveParams = getNistCurveParams(kemId);
            BigInteger order = nistCurveParams.getOrder();
            BigInteger bigIntegerFromUnsignedBigEndianBytes = BigIntegerEncoding.fromUnsignedBigEndianBytes(privateKeyBytes);
            if (bigIntegerFromUnsignedBigEndianBytes.signum() <= 0 || bigIntegerFromUnsignedBigEndianBytes.compareTo(order) >= 0) {
                throw new GeneralSecurityException("Invalid private key.");
            }
            if (!EllipticCurvesUtil.multiplyByGenerator(bigIntegerFromUnsignedBigEndianBytes, nistCurveParams).equals(EllipticCurves.pointDecode(nistCurveParams.getCurve(), EllipticCurves.PointFormatType.UNCOMPRESSED, publicKeyBytes))) {
                throw new GeneralSecurityException("Invalid private key for public key.");
            }
            return;
        }
        if (kemId == HpkeParameters.KemId.DHKEM_X25519_HKDF_SHA256) {
            if (!Arrays.equals(X25519.publicFromPrivate(privateKeyBytes), publicKeyBytes)) {
                throw new GeneralSecurityException("Invalid private key for public key.");
            }
        } else {
            throw new IllegalArgumentException("Unable to validate key pair for " + kemId);
        }
    }

    public static HpkePrivateKey create(HpkePublicKey publicKey, SecretBytes privateKeyBytes) throws GeneralSecurityException {
        if (publicKey == null) {
            throw new GeneralSecurityException("HPKE private key cannot be constructed without an HPKE public key");
        }
        if (privateKeyBytes == null) {
            throw new GeneralSecurityException("HPKE private key cannot be constructed without secret");
        }
        validatePrivateKeyByteLength(publicKey.getParameters().getKemId(), privateKeyBytes);
        validateKeyPair(publicKey.getParameters().getKemId(), publicKey.getPublicKeyBytes().toByteArray(), privateKeyBytes.toByteArray(InsecureSecretKeyAccess.get()));
        return new HpkePrivateKey(publicKey, privateKeyBytes);
    }

    public SecretBytes getPrivateKeyBytes() {
        return this.privateKeyBytes;
    }

    @Override // com.google.crypto.tink.hybrid.HybridPrivateKey, com.google.crypto.tink.Key
    public HpkeParameters getParameters() {
        return this.publicKey.getParameters();
    }

    @Override // com.google.crypto.tink.hybrid.HybridPrivateKey, com.google.crypto.tink.PrivateKey
    public HpkePublicKey getPublicKey() {
        return this.publicKey;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof HpkePrivateKey)) {
            return false;
        }
        HpkePrivateKey hpkePrivateKey = (HpkePrivateKey) o;
        return this.publicKey.equalsKey(hpkePrivateKey.publicKey) && this.privateKeyBytes.equalsSecretBytes(hpkePrivateKey.privateKeyBytes);
    }
}
