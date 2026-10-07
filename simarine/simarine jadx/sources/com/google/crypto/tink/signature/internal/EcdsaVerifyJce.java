package com.google.crypto.tink.signature.internal;

import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.ConscryptUtil;
import com.google.crypto.tink.internal.EllipticCurvesUtil;
import com.google.crypto.tink.internal.EnumTypeProtoConverter;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.signature.EcdsaParameters;
import com.google.crypto.tink.signature.EcdsaPublicKey;
import com.google.crypto.tink.subtle.EllipticCurves;
import com.google.crypto.tink.subtle.EngineFactory;
import com.google.crypto.tink.subtle.Enums;
import com.google.crypto.tink.subtle.SubtleUtil;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.Provider;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class EcdsaVerifyJce implements PublicKeyVerify {
    private final EllipticCurves.EcdsaEncoding encoding;
    private final byte[] messageSuffix;
    private final byte[] outputPrefix;
    private final Provider provider;
    private final ECPublicKey publicKey;
    private final String signatureAlgorithm;
    public static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_REQUIRES_BORINGCRYPTO;
    private static final byte[] EMPTY = new byte[0];
    private static final byte[] legacyMessageSuffix = {0};
    static final EnumTypeProtoConverter<Enums.HashType, EcdsaParameters.HashType> HASH_TYPE_CONVERTER = EnumTypeProtoConverter.builder().add(Enums.HashType.SHA256, EcdsaParameters.HashType.SHA256).add(Enums.HashType.SHA384, EcdsaParameters.HashType.SHA384).add(Enums.HashType.SHA512, EcdsaParameters.HashType.SHA512).build();
    static final EnumTypeProtoConverter<EllipticCurves.EcdsaEncoding, EcdsaParameters.SignatureEncoding> ENCODING_CONVERTER = EnumTypeProtoConverter.builder().add(EllipticCurves.EcdsaEncoding.IEEE_P1363, EcdsaParameters.SignatureEncoding.IEEE_P1363).add(EllipticCurves.EcdsaEncoding.DER, EcdsaParameters.SignatureEncoding.DER).build();
    static final EnumTypeProtoConverter<EllipticCurves.CurveType, EcdsaParameters.CurveType> CURVE_TYPE_CONVERTER = EnumTypeProtoConverter.builder().add(EllipticCurves.CurveType.NIST_P256, EcdsaParameters.CurveType.NIST_P256).add(EllipticCurves.CurveType.NIST_P384, EcdsaParameters.CurveType.NIST_P384).add(EllipticCurves.CurveType.NIST_P521, EcdsaParameters.CurveType.NIST_P521).build();

    public static PublicKeyVerify create(EcdsaPublicKey key) throws GeneralSecurityException {
        KeyFactory engineFactory;
        byte[] bArr;
        Provider providerProviderOrNull = ConscryptUtil.providerOrNull();
        ECPublicKeySpec eCPublicKeySpec = new ECPublicKeySpec(key.getPublicPoint(), EllipticCurves.getCurveSpec((EllipticCurves.CurveType) CURVE_TYPE_CONVERTER.toProtoEnum(key.getParameters().getCurveType())));
        if (providerProviderOrNull == null) {
            engineFactory = EngineFactory.KEY_FACTORY.getInstance("EC");
        } else {
            engineFactory = KeyFactory.getInstance("EC", providerProviderOrNull);
        }
        ECPublicKey eCPublicKey = (ECPublicKey) engineFactory.generatePublic(eCPublicKeySpec);
        Enums.HashType hashType = (Enums.HashType) HASH_TYPE_CONVERTER.toProtoEnum(key.getParameters().getHashType());
        EllipticCurves.EcdsaEncoding ecdsaEncoding = (EllipticCurves.EcdsaEncoding) ENCODING_CONVERTER.toProtoEnum(key.getParameters().getSignatureEncoding());
        byte[] byteArray = key.getOutputPrefix().toByteArray();
        if (key.getParameters().getVariant().equals(EcdsaParameters.Variant.LEGACY)) {
            bArr = legacyMessageSuffix;
        } else {
            bArr = EMPTY;
        }
        return new EcdsaVerifyJce(eCPublicKey, hashType, ecdsaEncoding, byteArray, bArr, providerProviderOrNull);
    }

    private EcdsaVerifyJce(final ECPublicKey publicKey, Enums.HashType hash, EllipticCurves.EcdsaEncoding encoding, byte[] outputPrefix, byte[] messageSuffix, Provider provider) throws GeneralSecurityException {
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Can not use ECDSA in FIPS-mode, as BoringCrypto is not available.");
        }
        this.signatureAlgorithm = SubtleUtil.toEcdsaAlgo(hash);
        this.publicKey = publicKey;
        this.encoding = encoding;
        this.outputPrefix = outputPrefix;
        this.messageSuffix = messageSuffix;
        this.provider = provider;
    }

    /* JADX WARN: Illegal instructions before constructor call */
    public EcdsaVerifyJce(final ECPublicKey publicKey, Enums.HashType hash, EllipticCurves.EcdsaEncoding encoding) throws GeneralSecurityException {
        byte[] bArr = EMPTY;
        this(publicKey, hash, encoding, bArr, bArr, ConscryptUtil.providerOrNull());
        EllipticCurvesUtil.checkPointOnCurve(publicKey.getW(), publicKey.getParams().getCurve());
    }

    private Signature getInstance(String signatureAlgorithm) throws GeneralSecurityException {
        Provider provider = this.provider;
        if (provider != null) {
            return Signature.getInstance(signatureAlgorithm, provider);
        }
        return EngineFactory.SIGNATURE.getInstance(signatureAlgorithm);
    }

    private void noPrefixVerify(final byte[] signature, final byte[] data) throws GeneralSecurityException {
        boolean zVerify;
        if (this.encoding == EllipticCurves.EcdsaEncoding.IEEE_P1363) {
            if (signature.length != EllipticCurves.fieldSizeInBytes(this.publicKey.getParams().getCurve()) * 2) {
                throw new GeneralSecurityException("Invalid signature");
            }
            signature = EllipticCurves.ecdsaIeee2Der(signature);
        }
        if (!EllipticCurves.isValidDerEncoding(signature)) {
            throw new GeneralSecurityException("Invalid signature");
        }
        Signature ecdsaVerifyJce = getInstance(this.signatureAlgorithm);
        ecdsaVerifyJce.initVerify(this.publicKey);
        ecdsaVerifyJce.update(data);
        byte[] bArr = this.messageSuffix;
        if (bArr.length > 0) {
            ecdsaVerifyJce.update(bArr);
        }
        try {
            zVerify = ecdsaVerifyJce.verify(signature);
        } catch (RuntimeException unused) {
            zVerify = false;
        }
        if (!zVerify) {
            throw new GeneralSecurityException("Invalid signature");
        }
    }

    @Override // com.google.crypto.tink.PublicKeyVerify
    public void verify(final byte[] signature, final byte[] data) throws GeneralSecurityException {
        byte[] bArr = this.outputPrefix;
        if (bArr.length == 0) {
            noPrefixVerify(signature, data);
        } else {
            if (!Util.isPrefix(bArr, signature)) {
                throw new GeneralSecurityException("Invalid signature (output prefix mismatch)");
            }
            noPrefixVerify(Arrays.copyOfRange(signature, this.outputPrefix.length, signature.length), data);
        }
    }
}
