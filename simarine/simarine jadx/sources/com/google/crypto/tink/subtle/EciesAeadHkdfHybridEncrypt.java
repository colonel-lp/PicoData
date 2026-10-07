package com.google.crypto.tink.subtle;

import com.google.crypto.tink.HybridEncrypt;
import com.google.crypto.tink.hybrid.EciesParameters;
import com.google.crypto.tink.hybrid.EciesPublicKey;
import com.google.crypto.tink.hybrid.internal.EciesDemHelper;
import com.google.crypto.tink.internal.EnumTypeProtoConverter;
import java.security.GeneralSecurityException;
import java.security.interfaces.ECPublicKey;

/* JADX INFO: loaded from: classes2.dex */
public final class EciesAeadHkdfHybridEncrypt implements HybridEncrypt {
    static final EnumTypeProtoConverter<EllipticCurves.CurveType, EciesParameters.CurveType> CURVE_TYPE_CONVERTER = EnumTypeProtoConverter.builder().add(EllipticCurves.CurveType.NIST_P256, EciesParameters.CurveType.NIST_P256).add(EllipticCurves.CurveType.NIST_P384, EciesParameters.CurveType.NIST_P384).add(EllipticCurves.CurveType.NIST_P521, EciesParameters.CurveType.NIST_P521).build();
    static final EnumTypeProtoConverter<EllipticCurves.PointFormatType, EciesParameters.PointFormat> POINT_FORMAT_TYPE_CONVERTER = EnumTypeProtoConverter.builder().add(EllipticCurves.PointFormatType.UNCOMPRESSED, EciesParameters.PointFormat.UNCOMPRESSED).add(EllipticCurves.PointFormatType.COMPRESSED, EciesParameters.PointFormat.COMPRESSED).add(EllipticCurves.PointFormatType.DO_NOT_USE_CRUNCHY_UNCOMPRESSED, EciesParameters.PointFormat.LEGACY_UNCOMPRESSED).build();
    private final EciesDemHelper.Dem dem;
    private final EllipticCurves.PointFormatType ecPointFormat;
    private final String hkdfHmacAlgo;
    private final byte[] hkdfSalt;
    private final byte[] outputPrefix;
    private final EciesHkdfSenderKem senderKem;

    static final String toHmacAlgo(EciesParameters.HashType hash) throws GeneralSecurityException {
        if (hash.equals(EciesParameters.HashType.SHA1)) {
            return "HmacSha1";
        }
        if (hash.equals(EciesParameters.HashType.SHA224)) {
            return "HmacSha224";
        }
        if (hash.equals(EciesParameters.HashType.SHA256)) {
            return "HmacSha256";
        }
        if (hash.equals(EciesParameters.HashType.SHA384)) {
            return "HmacSha384";
        }
        if (hash.equals(EciesParameters.HashType.SHA512)) {
            return "HmacSha512";
        }
        throw new GeneralSecurityException("hash unsupported for EciesAeadHkdf: " + hash);
    }

    private EciesAeadHkdfHybridEncrypt(final ECPublicKey recipientPublicKey, final byte[] hkdfSalt, String hkdfHmacAlgo, EllipticCurves.PointFormatType ecPointFormat, EciesDemHelper.Dem dem, byte[] outputPrefix) throws GeneralSecurityException {
        EllipticCurves.checkPublicKey(recipientPublicKey);
        this.senderKem = new EciesHkdfSenderKem(recipientPublicKey);
        this.hkdfSalt = hkdfSalt;
        this.hkdfHmacAlgo = hkdfHmacAlgo;
        this.ecPointFormat = ecPointFormat;
        this.dem = dem;
        this.outputPrefix = outputPrefix;
    }

    public static HybridEncrypt create(EciesPublicKey key) throws GeneralSecurityException {
        ECPublicKey ecPublicKey = EllipticCurves.getEcPublicKey((EllipticCurves.CurveType) CURVE_TYPE_CONVERTER.toProtoEnum(key.getParameters().getCurveType()), key.getNistCurvePoint().getAffineX().toByteArray(), key.getNistCurvePoint().getAffineY().toByteArray());
        byte[] byteArray = new byte[0];
        if (key.getParameters().getSalt() != null) {
            byteArray = key.getParameters().getSalt().toByteArray();
        }
        return new EciesAeadHkdfHybridEncrypt(ecPublicKey, byteArray, toHmacAlgo(key.getParameters().getHashType()), (EllipticCurves.PointFormatType) POINT_FORMAT_TYPE_CONVERTER.toProtoEnum(key.getParameters().getNistCurvePointFormat()), EciesDemHelper.getDem(key.getParameters()), key.getOutputPrefix().toByteArray());
    }

    @Override // com.google.crypto.tink.HybridEncrypt
    public byte[] encrypt(final byte[] plaintext, final byte[] contextInfo) throws GeneralSecurityException {
        EciesHkdfSenderKem.KemKey kemKeyGenerateKey = this.senderKem.generateKey(this.hkdfHmacAlgo, this.hkdfSalt, contextInfo, this.dem.getSymmetricKeySizeInBytes(), this.ecPointFormat);
        return this.dem.encrypt(kemKeyGenerateKey.getSymmetricKey(), this.outputPrefix, kemKeyGenerateKey.getKemBytes(), plaintext);
    }
}
