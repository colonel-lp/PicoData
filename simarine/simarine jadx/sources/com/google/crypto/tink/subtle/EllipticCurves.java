package com.google.crypto.tink.subtle;

import com.google.crypto.tink.internal.BigIntegerEncoding;
import com.google.crypto.tink.internal.EllipticCurvesUtil;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import javax.crypto.KeyAgreement;
import kotlin.UByte;
import kotlin.jvm.internal.ByteCompanionObject;

/* JADX INFO: loaded from: classes2.dex */
public final class EllipticCurves {

    public enum CurveType {
        NIST_P256,
        NIST_P384,
        NIST_P521
    }

    public enum EcdsaEncoding {
        IEEE_P1363,
        DER
    }

    public enum PointFormatType {
        UNCOMPRESSED,
        COMPRESSED,
        DO_NOT_USE_CRUNCHY_UNCOMPRESSED
    }

    public static ECParameterSpec getNistP256Params() {
        return EllipticCurvesUtil.NIST_P256_PARAMS;
    }

    public static ECParameterSpec getNistP384Params() {
        return EllipticCurvesUtil.NIST_P384_PARAMS;
    }

    public static ECParameterSpec getNistP521Params() {
        return EllipticCurvesUtil.NIST_P521_PARAMS;
    }

    static void checkPublicKey(ECPublicKey key) throws GeneralSecurityException {
        EllipticCurvesUtil.checkPointOnCurve(key.getW(), key.getParams().getCurve());
    }

    public static boolean isNistEcParameterSpec(ECParameterSpec spec) {
        return EllipticCurvesUtil.isNistEcParameterSpec(spec);
    }

    public static boolean isSameEcParameterSpec(ECParameterSpec one, ECParameterSpec two) {
        return EllipticCurvesUtil.isSameEcParameterSpec(one, two);
    }

    public static void validatePublicKey(ECPublicKey publicKey, ECPrivateKey privateKey) throws GeneralSecurityException {
        validatePublicKeySpec(publicKey, privateKey);
        EllipticCurvesUtil.checkPointOnCurve(publicKey.getW(), privateKey.getParams().getCurve());
    }

    static void validatePublicKeySpec(ECPublicKey publicKey, ECPrivateKey privateKey) throws GeneralSecurityException {
        try {
            if (isSameEcParameterSpec(publicKey.getParams(), privateKey.getParams())) {
            } else {
                throw new GeneralSecurityException("invalid public key spec");
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new GeneralSecurityException(e);
        }
    }

    public static BigInteger getModulus(EllipticCurve curve) throws GeneralSecurityException {
        return EllipticCurvesUtil.getModulus(curve);
    }

    public static int fieldSizeInBits(EllipticCurve curve) throws GeneralSecurityException {
        return getModulus(curve).subtract(BigInteger.ONE).bitLength();
    }

    public static int fieldSizeInBytes(EllipticCurve curve) throws GeneralSecurityException {
        return (fieldSizeInBits(curve) + 7) / 8;
    }

    private static BigInteger modSqrt(BigInteger x, BigInteger p) throws GeneralSecurityException {
        BigInteger bigIntegerModPow;
        if (p.signum() != 1) {
            throw new InvalidAlgorithmParameterException("p must be positive");
        }
        BigInteger bigIntegerMod = x.mod(p);
        if (bigIntegerMod.equals(BigInteger.ZERO)) {
            return BigInteger.ZERO;
        }
        int i = 0;
        if (p.testBit(0) && p.testBit(1)) {
            bigIntegerModPow = bigIntegerMod.modPow(p.add(BigInteger.ONE).shiftRight(2), p);
        } else if (p.testBit(0) && !p.testBit(1)) {
            BigInteger bigIntegerAdd = BigInteger.ONE;
            BigInteger bigIntegerShiftRight = p.subtract(BigInteger.ONE).shiftRight(1);
            while (true) {
                BigInteger bigIntegerMod2 = bigIntegerAdd.multiply(bigIntegerAdd).subtract(bigIntegerMod).mod(p);
                if (bigIntegerMod2.equals(BigInteger.ZERO)) {
                    return bigIntegerAdd;
                }
                BigInteger bigIntegerModPow2 = bigIntegerMod2.modPow(bigIntegerShiftRight, p);
                if (!bigIntegerModPow2.add(BigInteger.ONE).equals(p)) {
                    if (!bigIntegerModPow2.equals(BigInteger.ONE)) {
                        throw new InvalidAlgorithmParameterException("p is not prime");
                    }
                    bigIntegerAdd = bigIntegerAdd.add(BigInteger.ONE);
                    i++;
                    if (i == 128 && !p.isProbablePrime(80)) {
                        throw new InvalidAlgorithmParameterException("p is not prime");
                    }
                } else {
                    BigInteger bigIntegerShiftRight2 = p.add(BigInteger.ONE).shiftRight(1);
                    BigInteger bigInteger = BigInteger.ONE;
                    BigInteger bigIntegerMod3 = bigInteger;
                    BigInteger bigIntegerMod4 = bigIntegerAdd;
                    for (int iBitLength = bigIntegerShiftRight2.bitLength() - 2; iBitLength >= 0; iBitLength--) {
                        BigInteger bigIntegerMultiply = bigIntegerMod4.multiply(bigIntegerMod3);
                        bigIntegerMod4 = bigIntegerMod4.multiply(bigIntegerMod4).add(bigIntegerMod3.multiply(bigIntegerMod3).mod(p).multiply(bigIntegerMod2)).mod(p);
                        bigIntegerMod3 = bigIntegerMultiply.add(bigIntegerMultiply).mod(p);
                        if (bigIntegerShiftRight2.testBit(iBitLength)) {
                            BigInteger bigIntegerMod5 = bigIntegerMod4.multiply(bigIntegerAdd).add(bigIntegerMod3.multiply(bigIntegerMod2)).mod(p);
                            bigIntegerMod3 = bigIntegerAdd.multiply(bigIntegerMod3).add(bigIntegerMod4).mod(p);
                            bigIntegerMod4 = bigIntegerMod5;
                        }
                    }
                    bigIntegerModPow = bigIntegerMod4;
                    break;
                }
            }
        } else {
            bigIntegerModPow = null;
        }
        if (bigIntegerModPow == null || bigIntegerModPow.multiply(bigIntegerModPow).mod(p).compareTo(bigIntegerMod) == 0) {
            return bigIntegerModPow;
        }
        throw new GeneralSecurityException("Could not find a modular square root");
    }

    private static BigInteger computeY(BigInteger x, boolean lsb, EllipticCurve curve) throws GeneralSecurityException {
        BigInteger modulus = getModulus(curve);
        BigInteger bigIntegerModSqrt = modSqrt(x.multiply(x).add(curve.getA()).multiply(x).add(curve.getB()).mod(modulus), modulus);
        return lsb != bigIntegerModSqrt.testBit(0) ? modulus.subtract(bigIntegerModSqrt).mod(modulus) : bigIntegerModSqrt;
    }

    @Deprecated
    public static BigInteger getY(BigInteger x, boolean lsb, EllipticCurve curve) throws GeneralSecurityException {
        return computeY(x, lsb, curve);
    }

    private static byte[] toMinimalSignedNumber(byte[] bs) {
        int length = 0;
        while (length < bs.length && bs[length] == 0) {
            length++;
        }
        if (length == bs.length) {
            length = bs.length - 1;
        }
        int i = (bs[length] & ByteCompanionObject.MIN_VALUE) == 128 ? 1 : 0;
        byte[] bArr = new byte[(bs.length - length) + i];
        System.arraycopy(bs, length, bArr, i, bs.length - length);
        return bArr;
    }

    public static byte[] ecdsaIeee2Der(byte[] ieee) throws GeneralSecurityException {
        byte[] bArr;
        int i;
        if (ieee.length % 2 != 0 || ieee.length == 0 || ieee.length > 132) {
            throw new GeneralSecurityException("Invalid IEEE_P1363 encoding");
        }
        byte[] minimalSignedNumber = toMinimalSignedNumber(Arrays.copyOf(ieee, ieee.length / 2));
        byte[] minimalSignedNumber2 = toMinimalSignedNumber(Arrays.copyOfRange(ieee, ieee.length / 2, ieee.length));
        int length = minimalSignedNumber.length + 4 + minimalSignedNumber2.length;
        if (length >= 128) {
            bArr = new byte[length + 3];
            bArr[0] = 48;
            bArr[1] = -127;
            bArr[2] = (byte) length;
            i = 3;
        } else {
            bArr = new byte[length + 2];
            bArr[0] = 48;
            bArr[1] = (byte) length;
            i = 2;
        }
        int i2 = i + 1;
        bArr[i] = 2;
        int i3 = i + 2;
        bArr[i2] = (byte) minimalSignedNumber.length;
        System.arraycopy(minimalSignedNumber, 0, bArr, i3, minimalSignedNumber.length);
        int length2 = i3 + minimalSignedNumber.length;
        bArr[length2] = 2;
        bArr[length2 + 1] = (byte) minimalSignedNumber2.length;
        System.arraycopy(minimalSignedNumber2, 0, bArr, length2 + 2, minimalSignedNumber2.length);
        return bArr;
    }

    public static byte[] ecdsaDer2Ieee(byte[] der, int ieeeLength) throws GeneralSecurityException {
        if (!isValidDerEncoding(der)) {
            throw new GeneralSecurityException("Invalid DER encoding");
        }
        byte[] bArr = new byte[ieeeLength];
        int i = (der[1] & UByte.MAX_VALUE) >= 128 ? 3 : 2;
        int i2 = i + 1;
        int i3 = i + 2;
        int i4 = der[i2];
        int i5 = der[i3] == 0 ? 1 : 0;
        System.arraycopy(der, i3 + i5, bArr, ((ieeeLength / 2) - i4) + i5, i4 - i5);
        int i6 = i3 + i4 + 1;
        int i7 = i6 + 1;
        int i8 = der[i6];
        int i9 = der[i7] != 0 ? 0 : 1;
        System.arraycopy(der, i7 + i9, bArr, (ieeeLength - i8) + i9, i8 - i9);
        return bArr;
    }

    public static boolean isValidDerEncoding(final byte[] sig) {
        int i;
        int i2;
        int i3;
        int i4;
        if (sig.length < 8 || sig[0] != 48) {
            return false;
        }
        int i5 = sig[1] & UByte.MAX_VALUE;
        if (i5 == 129) {
            i5 = sig[2] & UByte.MAX_VALUE;
            if (i5 < 128) {
                return false;
            }
            i = 2;
        } else {
            if (i5 == 128 || i5 > 129) {
                return false;
            }
            i = 1;
        }
        if (i5 != (sig.length - 1) - i || sig[i + 1] != 2 || (i4 = (i3 = i + 3 + (i2 = sig[i + 2] & UByte.MAX_VALUE)) + 1) >= sig.length || i2 == 0) {
            return false;
        }
        int i6 = i + 3;
        byte b = sig[i6];
        if ((b & UByte.MAX_VALUE) >= 128) {
            return false;
        }
        if ((i2 > 1 && b == 0 && (sig[i + 4] & UByte.MAX_VALUE) < 128) || sig[i6 + i2] != 2) {
            return false;
        }
        int i7 = sig[i4] & UByte.MAX_VALUE;
        if (i3 + 2 + i7 != sig.length || i7 == 0) {
            return false;
        }
        byte b2 = sig[i + 5 + i2];
        if ((b2 & UByte.MAX_VALUE) >= 128) {
            return false;
        }
        return i7 <= 1 || b2 != 0 || (sig[(i + 6) + i2] & UByte.MAX_VALUE) >= 128;
    }

    public static int encodingSizeInBytes(EllipticCurve curve, PointFormatType format) throws GeneralSecurityException {
        int iFieldSizeInBytes = fieldSizeInBytes(curve);
        int i = AnonymousClass1.$SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$PointFormatType[format.ordinal()];
        if (i == 1) {
            return (iFieldSizeInBytes * 2) + 1;
        }
        if (i == 2) {
            return iFieldSizeInBytes * 2;
        }
        if (i == 3) {
            return iFieldSizeInBytes + 1;
        }
        throw new GeneralSecurityException("unknown EC point format");
    }

    public static ECPoint ecPointDecode(EllipticCurve curve, PointFormatType format, byte[] encoded) throws GeneralSecurityException {
        return pointDecode(curve, format, encoded);
    }

    public static ECPoint pointDecode(CurveType curveType, PointFormatType format, byte[] encoded) throws GeneralSecurityException {
        return pointDecode(getCurveSpec(curveType).getCurve(), format, encoded);
    }

    public static ECPoint pointDecode(EllipticCurve curve, PointFormatType format, byte[] encoded) throws GeneralSecurityException {
        int iFieldSizeInBytes = fieldSizeInBytes(curve);
        int i = AnonymousClass1.$SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$PointFormatType[format.ordinal()];
        boolean z = false;
        if (i == 1) {
            if (encoded.length != (iFieldSizeInBytes * 2) + 1) {
                throw new GeneralSecurityException("invalid point size");
            }
            if (encoded[0] != 4) {
                throw new GeneralSecurityException("invalid point format");
            }
            int i2 = iFieldSizeInBytes + 1;
            ECPoint eCPoint = new ECPoint(new BigInteger(1, Arrays.copyOfRange(encoded, 1, i2)), new BigInteger(1, Arrays.copyOfRange(encoded, i2, encoded.length)));
            EllipticCurvesUtil.checkPointOnCurve(eCPoint, curve);
            return eCPoint;
        }
        if (i == 2) {
            if (encoded.length != iFieldSizeInBytes * 2) {
                throw new GeneralSecurityException("invalid point size");
            }
            ECPoint eCPoint2 = new ECPoint(new BigInteger(1, Arrays.copyOf(encoded, iFieldSizeInBytes)), new BigInteger(1, Arrays.copyOfRange(encoded, iFieldSizeInBytes, encoded.length)));
            EllipticCurvesUtil.checkPointOnCurve(eCPoint2, curve);
            return eCPoint2;
        }
        if (i == 3) {
            BigInteger modulus = getModulus(curve);
            if (encoded.length != iFieldSizeInBytes + 1) {
                throw new GeneralSecurityException("compressed point has wrong length");
            }
            byte b = encoded[0];
            if (b != 2) {
                if (b != 3) {
                    throw new GeneralSecurityException("invalid format");
                }
                z = true;
            }
            BigInteger bigInteger = new BigInteger(1, Arrays.copyOfRange(encoded, 1, encoded.length));
            if (bigInteger.signum() == -1 || bigInteger.compareTo(modulus) >= 0) {
                throw new GeneralSecurityException("x is out of range");
            }
            return new ECPoint(bigInteger, computeY(bigInteger, z, curve));
        }
        throw new GeneralSecurityException("invalid format:" + format);
    }

    public static byte[] pointEncode(CurveType curveType, PointFormatType format, ECPoint point) throws GeneralSecurityException {
        return pointEncode(getCurveSpec(curveType).getCurve(), format, point);
    }

    public static byte[] pointEncode(EllipticCurve curve, PointFormatType format, ECPoint point) throws GeneralSecurityException {
        EllipticCurvesUtil.checkPointOnCurve(point, curve);
        int iFieldSizeInBytes = fieldSizeInBytes(curve);
        int i = AnonymousClass1.$SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$PointFormatType[format.ordinal()];
        if (i == 1) {
            int i2 = (iFieldSizeInBytes * 2) + 1;
            byte[] bArr = new byte[i2];
            byte[] bigEndianBytes = BigIntegerEncoding.toBigEndianBytes(point.getAffineX());
            byte[] bigEndianBytes2 = BigIntegerEncoding.toBigEndianBytes(point.getAffineY());
            System.arraycopy(bigEndianBytes2, 0, bArr, i2 - bigEndianBytes2.length, bigEndianBytes2.length);
            System.arraycopy(bigEndianBytes, 0, bArr, (iFieldSizeInBytes + 1) - bigEndianBytes.length, bigEndianBytes.length);
            bArr[0] = 4;
            return bArr;
        }
        if (i != 2) {
            if (i == 3) {
                int i3 = iFieldSizeInBytes + 1;
                byte[] bArr2 = new byte[i3];
                byte[] bigEndianBytes3 = BigIntegerEncoding.toBigEndianBytes(point.getAffineX());
                System.arraycopy(bigEndianBytes3, 0, bArr2, i3 - bigEndianBytes3.length, bigEndianBytes3.length);
                bArr2[0] = (byte) (point.getAffineY().testBit(0) ? 3 : 2);
                return bArr2;
            }
            throw new GeneralSecurityException("invalid format:" + format);
        }
        int i4 = iFieldSizeInBytes * 2;
        byte[] bArr3 = new byte[i4];
        byte[] bigEndianBytes4 = BigIntegerEncoding.toBigEndianBytes(point.getAffineX());
        if (bigEndianBytes4.length > iFieldSizeInBytes) {
            bigEndianBytes4 = Arrays.copyOfRange(bigEndianBytes4, bigEndianBytes4.length - iFieldSizeInBytes, bigEndianBytes4.length);
        }
        byte[] bigEndianBytes5 = BigIntegerEncoding.toBigEndianBytes(point.getAffineY());
        if (bigEndianBytes5.length > iFieldSizeInBytes) {
            bigEndianBytes5 = Arrays.copyOfRange(bigEndianBytes5, bigEndianBytes5.length - iFieldSizeInBytes, bigEndianBytes5.length);
        }
        System.arraycopy(bigEndianBytes5, 0, bArr3, i4 - bigEndianBytes5.length, bigEndianBytes5.length);
        System.arraycopy(bigEndianBytes4, 0, bArr3, iFieldSizeInBytes - bigEndianBytes4.length, bigEndianBytes4.length);
        return bArr3;
    }

    /* JADX INFO: renamed from: com.google.crypto.tink.subtle.EllipticCurves$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$CurveType;
        static final /* synthetic */ int[] $SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$PointFormatType;

        static {
            int[] iArr = new int[CurveType.values().length];
            $SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$CurveType = iArr;
            try {
                iArr[CurveType.NIST_P256.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$CurveType[CurveType.NIST_P384.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$CurveType[CurveType.NIST_P521.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            int[] iArr2 = new int[PointFormatType.values().length];
            $SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$PointFormatType = iArr2;
            try {
                iArr2[PointFormatType.UNCOMPRESSED.ordinal()] = 1;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$PointFormatType[PointFormatType.DO_NOT_USE_CRUNCHY_UNCOMPRESSED.ordinal()] = 2;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$PointFormatType[PointFormatType.COMPRESSED.ordinal()] = 3;
            } catch (NoSuchFieldError unused6) {
            }
        }
    }

    public static ECParameterSpec getCurveSpec(CurveType curve) throws NoSuchAlgorithmException {
        int i = AnonymousClass1.$SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$CurveType[curve.ordinal()];
        if (i == 1) {
            return getNistP256Params();
        }
        if (i == 2) {
            return getNistP384Params();
        }
        if (i == 3) {
            return getNistP521Params();
        }
        throw new NoSuchAlgorithmException("curve not implemented:" + curve);
    }

    public static ECPublicKey getEcPublicKey(final byte[] x509PublicKey) throws GeneralSecurityException {
        return (ECPublicKey) EngineFactory.KEY_FACTORY.getInstance("EC").generatePublic(new X509EncodedKeySpec(x509PublicKey));
    }

    public static ECPublicKey getEcPublicKey(CurveType curve, PointFormatType pointFormat, final byte[] publicKey) throws GeneralSecurityException {
        return getEcPublicKey(getCurveSpec(curve), pointFormat, publicKey);
    }

    public static ECPublicKey getEcPublicKey(ECParameterSpec spec, PointFormatType pointFormat, final byte[] publicKey) throws GeneralSecurityException {
        return (ECPublicKey) EngineFactory.KEY_FACTORY.getInstance("EC").generatePublic(new ECPublicKeySpec(pointDecode(spec.getCurve(), pointFormat, publicKey), spec));
    }

    public static ECPublicKey getEcPublicKey(CurveType curve, final byte[] x, final byte[] y) throws GeneralSecurityException {
        ECParameterSpec curveSpec = getCurveSpec(curve);
        ECPoint eCPoint = new ECPoint(new BigInteger(1, x), new BigInteger(1, y));
        EllipticCurvesUtil.checkPointOnCurve(eCPoint, curveSpec.getCurve());
        return (ECPublicKey) EngineFactory.KEY_FACTORY.getInstance("EC").generatePublic(new ECPublicKeySpec(eCPoint, curveSpec));
    }

    public static ECPrivateKey getEcPrivateKey(final byte[] pkcs8PrivateKey) throws GeneralSecurityException {
        return (ECPrivateKey) EngineFactory.KEY_FACTORY.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(pkcs8PrivateKey));
    }

    public static ECPrivateKey getEcPrivateKey(CurveType curve, final byte[] keyValue) throws GeneralSecurityException {
        return (ECPrivateKey) EngineFactory.KEY_FACTORY.getInstance("EC").generatePrivate(new ECPrivateKeySpec(BigIntegerEncoding.fromUnsignedBigEndianBytes(keyValue), getCurveSpec(curve)));
    }

    public static KeyPair generateKeyPair(CurveType curve) throws GeneralSecurityException {
        return generateKeyPair(getCurveSpec(curve));
    }

    public static KeyPair generateKeyPair(ECParameterSpec spec) throws GeneralSecurityException {
        KeyPairGenerator engineFactory = EngineFactory.KEY_PAIR_GENERATOR.getInstance("EC");
        engineFactory.initialize(spec);
        return engineFactory.generateKeyPair();
    }

    static void validateSharedSecret(byte[] secret, ECPrivateKey privateKey) throws GeneralSecurityException {
        EllipticCurve curve = privateKey.getParams().getCurve();
        BigInteger bigInteger = new BigInteger(1, secret);
        if (bigInteger.signum() == -1 || bigInteger.compareTo(getModulus(curve)) >= 0) {
            throw new GeneralSecurityException("shared secret is out of range");
        }
        computeY(bigInteger, true, curve);
    }

    public static byte[] computeSharedSecret(ECPrivateKey myPrivateKey, ECPublicKey peerPublicKey) throws GeneralSecurityException {
        validatePublicKeySpec(peerPublicKey, myPrivateKey);
        return computeSharedSecret(myPrivateKey, peerPublicKey.getW());
    }

    public static byte[] computeSharedSecret(ECPrivateKey myPrivateKey, ECPoint publicPoint) throws GeneralSecurityException {
        EllipticCurvesUtil.checkPointOnCurve(publicPoint, myPrivateKey.getParams().getCurve());
        PublicKey publicKeyGeneratePublic = EngineFactory.KEY_FACTORY.getInstance("EC").generatePublic(new ECPublicKeySpec(publicPoint, myPrivateKey.getParams()));
        KeyAgreement engineFactory = EngineFactory.KEY_AGREEMENT.getInstance("ECDH");
        engineFactory.init(myPrivateKey);
        try {
            engineFactory.doPhase(publicKeyGeneratePublic, true);
            byte[] bArrGenerateSecret = engineFactory.generateSecret();
            validateSharedSecret(bArrGenerateSecret, myPrivateKey);
            return bArrGenerateSecret;
        } catch (IllegalStateException e) {
            throw new GeneralSecurityException(e);
        }
    }

    private EllipticCurves() {
    }
}
