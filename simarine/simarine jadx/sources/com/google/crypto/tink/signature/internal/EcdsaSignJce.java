package com.google.crypto.tink.signature.internal;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.PublicKeySign;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.ConscryptUtil;
import com.google.crypto.tink.signature.EcdsaParameters;
import com.google.crypto.tink.signature.EcdsaPrivateKey;
import com.google.crypto.tink.subtle.Bytes;
import com.google.crypto.tink.subtle.EllipticCurves;
import com.google.crypto.tink.subtle.EngineFactory;
import com.google.crypto.tink.subtle.Enums;
import com.google.crypto.tink.subtle.SubtleUtil;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.Provider;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECPrivateKeySpec;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class EcdsaSignJce implements PublicKeySign {
    private final EllipticCurves.EcdsaEncoding encoding;
    private final byte[] messageSuffix;
    private final byte[] outputPrefix;
    private final ECPrivateKey privateKey;
    private final Provider provider;
    private final String signatureAlgorithm;
    public static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_REQUIRES_BORINGCRYPTO;
    private static final byte[] EMPTY = new byte[0];
    private static final byte[] legacyMessageSuffix = {0};

    private EcdsaSignJce(final ECPrivateKey privateKey, Enums.HashType hash, EllipticCurves.EcdsaEncoding encoding, byte[] outputPrefix, byte[] messageSuffix, Provider provider) throws GeneralSecurityException {
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Can not use ECDSA in FIPS-mode, as BoringCrypto is not available.");
        }
        this.privateKey = privateKey;
        this.signatureAlgorithm = SubtleUtil.toEcdsaAlgo(hash);
        this.encoding = encoding;
        this.outputPrefix = outputPrefix;
        this.messageSuffix = messageSuffix;
        this.provider = provider;
    }

    /* JADX WARN: Illegal instructions before constructor call */
    public EcdsaSignJce(final ECPrivateKey privateKey, Enums.HashType hash, EllipticCurves.EcdsaEncoding encoding) throws GeneralSecurityException {
        byte[] bArr = EMPTY;
        this(privateKey, hash, encoding, bArr, bArr, ConscryptUtil.providerOrNull());
    }

    public static PublicKeySign create(EcdsaPrivateKey key) throws GeneralSecurityException {
        KeyFactory engineFactory;
        byte[] bArr;
        Enums.HashType hashType = (Enums.HashType) EcdsaVerifyJce.HASH_TYPE_CONVERTER.toProtoEnum(key.getParameters().getHashType());
        EllipticCurves.EcdsaEncoding ecdsaEncoding = (EllipticCurves.EcdsaEncoding) EcdsaVerifyJce.ENCODING_CONVERTER.toProtoEnum(key.getParameters().getSignatureEncoding());
        EllipticCurves.CurveType curveType = (EllipticCurves.CurveType) EcdsaVerifyJce.CURVE_TYPE_CONVERTER.toProtoEnum(key.getParameters().getCurveType());
        Provider providerProviderOrNull = ConscryptUtil.providerOrNull();
        ECPrivateKeySpec eCPrivateKeySpec = new ECPrivateKeySpec(key.getPrivateValue().getBigInteger(InsecureSecretKeyAccess.get()), EllipticCurves.getCurveSpec(curveType));
        if (providerProviderOrNull == null) {
            engineFactory = EngineFactory.KEY_FACTORY.getInstance("EC");
        } else {
            engineFactory = KeyFactory.getInstance("EC", providerProviderOrNull);
        }
        ECPrivateKey eCPrivateKey = (ECPrivateKey) engineFactory.generatePrivate(eCPrivateKeySpec);
        byte[] byteArray = key.getOutputPrefix().toByteArray();
        if (key.getParameters().getVariant().equals(EcdsaParameters.Variant.LEGACY)) {
            bArr = legacyMessageSuffix;
        } else {
            bArr = EMPTY;
        }
        return new EcdsaSignJce(eCPrivateKey, hashType, ecdsaEncoding, byteArray, bArr, providerProviderOrNull);
    }

    private Signature getInstance(String signatureAlgorithm) throws GeneralSecurityException {
        Provider provider = this.provider;
        if (provider != null) {
            return Signature.getInstance(signatureAlgorithm, provider);
        }
        return EngineFactory.SIGNATURE.getInstance(signatureAlgorithm);
    }

    @Override // com.google.crypto.tink.PublicKeySign
    public byte[] sign(final byte[] data) throws GeneralSecurityException {
        Signature ecdsaSignJce = getInstance(this.signatureAlgorithm);
        ecdsaSignJce.initSign(this.privateKey);
        ecdsaSignJce.update(data);
        byte[] bArr = this.messageSuffix;
        if (bArr.length > 0) {
            ecdsaSignJce.update(bArr);
        }
        byte[] bArrSign = ecdsaSignJce.sign();
        if (this.encoding == EllipticCurves.EcdsaEncoding.IEEE_P1363) {
            bArrSign = EllipticCurves.ecdsaDer2Ieee(bArrSign, EllipticCurves.fieldSizeInBytes(this.privateKey.getParams().getCurve()) * 2);
        }
        byte[] bArr2 = this.outputPrefix;
        return bArr2.length == 0 ? bArrSign : Bytes.concat(bArr2, bArrSign);
    }
}
