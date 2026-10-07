package com.google.crypto.tink.signature;

import com.google.crypto.tink.proto.EcdsaKeyFormat;
import com.google.crypto.tink.proto.EcdsaParams;
import com.google.crypto.tink.proto.EcdsaSignatureEncoding;
import com.google.crypto.tink.proto.EllipticCurveType;
import com.google.crypto.tink.proto.HashType;
import com.google.crypto.tink.proto.KeyTemplate;
import com.google.crypto.tink.proto.OutputPrefixType;
import com.google.crypto.tink.proto.RsaSsaPkcs1KeyFormat;
import com.google.crypto.tink.proto.RsaSsaPkcs1Params;
import com.google.crypto.tink.proto.RsaSsaPssKeyFormat;
import com.google.crypto.tink.proto.RsaSsaPssParams;
import com.google.crypto.tink.shaded.protobuf.ByteString;
import java.math.BigInteger;
import java.security.spec.RSAKeyGenParameterSpec;

/* JADX INFO: loaded from: classes2.dex */
public final class SignatureKeyTemplates {
    public static final KeyTemplate ECDSA_P256 = createEcdsaKeyTemplate(HashType.SHA256, EllipticCurveType.NIST_P256, EcdsaSignatureEncoding.DER, OutputPrefixType.TINK);
    public static final KeyTemplate ECDSA_P384 = createEcdsaKeyTemplate(HashType.SHA512, EllipticCurveType.NIST_P384, EcdsaSignatureEncoding.DER, OutputPrefixType.TINK);
    public static final KeyTemplate ECDSA_P521 = createEcdsaKeyTemplate(HashType.SHA512, EllipticCurveType.NIST_P521, EcdsaSignatureEncoding.DER, OutputPrefixType.TINK);
    public static final KeyTemplate ECDSA_P256_IEEE_P1363 = createEcdsaKeyTemplate(HashType.SHA256, EllipticCurveType.NIST_P256, EcdsaSignatureEncoding.IEEE_P1363, OutputPrefixType.TINK);
    public static final KeyTemplate ECDSA_P384_IEEE_P1363 = createEcdsaKeyTemplate(HashType.SHA512, EllipticCurveType.NIST_P384, EcdsaSignatureEncoding.IEEE_P1363, OutputPrefixType.TINK);
    public static final KeyTemplate ECDSA_P256_IEEE_P1363_WITHOUT_PREFIX = createEcdsaKeyTemplate(HashType.SHA256, EllipticCurveType.NIST_P256, EcdsaSignatureEncoding.IEEE_P1363, OutputPrefixType.RAW);
    public static final KeyTemplate ECDSA_P521_IEEE_P1363 = createEcdsaKeyTemplate(HashType.SHA512, EllipticCurveType.NIST_P521, EcdsaSignatureEncoding.IEEE_P1363, OutputPrefixType.TINK);
    public static final KeyTemplate ED25519 = (KeyTemplate) KeyTemplate.newBuilder().setTypeUrl(Ed25519PrivateKeyManager.getKeyType()).setOutputPrefixType(OutputPrefixType.TINK).build();
    public static final KeyTemplate ED25519WithRawOutput = (KeyTemplate) KeyTemplate.newBuilder().setTypeUrl(Ed25519PrivateKeyManager.getKeyType()).setOutputPrefixType(OutputPrefixType.RAW).build();
    public static final KeyTemplate RSA_SSA_PKCS1_3072_SHA256_F4 = createRsaSsaPkcs1KeyTemplate(HashType.SHA256, 3072, RSAKeyGenParameterSpec.F4, OutputPrefixType.TINK);
    public static final KeyTemplate RSA_SSA_PKCS1_3072_SHA256_F4_WITHOUT_PREFIX = createRsaSsaPkcs1KeyTemplate(HashType.SHA256, 3072, RSAKeyGenParameterSpec.F4, OutputPrefixType.RAW);
    public static final KeyTemplate RSA_SSA_PKCS1_4096_SHA512_F4 = createRsaSsaPkcs1KeyTemplate(HashType.SHA512, 4096, RSAKeyGenParameterSpec.F4, OutputPrefixType.TINK);
    public static final KeyTemplate RSA_SSA_PSS_3072_SHA256_SHA256_32_F4 = createRsaSsaPssKeyTemplate(HashType.SHA256, HashType.SHA256, 32, 3072, RSAKeyGenParameterSpec.F4);
    public static final KeyTemplate RSA_SSA_PSS_4096_SHA512_SHA512_64_F4 = createRsaSsaPssKeyTemplate(HashType.SHA512, HashType.SHA512, 64, 4096, RSAKeyGenParameterSpec.F4);

    @Deprecated
    public static KeyTemplate createEcdsaKeyTemplate(HashType hashType, EllipticCurveType curve, EcdsaSignatureEncoding encoding, OutputPrefixType prefixType) {
        return (KeyTemplate) KeyTemplate.newBuilder().setValue(((EcdsaKeyFormat) EcdsaKeyFormat.newBuilder().setParams((EcdsaParams) EcdsaParams.newBuilder().setHashType(hashType).setCurve(curve).setEncoding(encoding).build()).build()).toByteString()).setTypeUrl(EcdsaSignKeyManager.getKeyType()).setOutputPrefixType(prefixType).build();
    }

    @Deprecated
    public static KeyTemplate createRsaSsaPkcs1KeyTemplate(HashType hashType, int modulusSize, BigInteger publicExponent, OutputPrefixType prefixType) {
        return (KeyTemplate) KeyTemplate.newBuilder().setValue(((RsaSsaPkcs1KeyFormat) RsaSsaPkcs1KeyFormat.newBuilder().setParams((RsaSsaPkcs1Params) RsaSsaPkcs1Params.newBuilder().setHashType(hashType).build()).setModulusSizeInBits(modulusSize).setPublicExponent(ByteString.copyFrom(publicExponent.toByteArray())).build()).toByteString()).setTypeUrl(RsaSsaPkcs1SignKeyManager.getKeyType()).setOutputPrefixType(prefixType).build();
    }

    @Deprecated
    public static KeyTemplate createRsaSsaPssKeyTemplate(HashType sigHash, HashType mgf1Hash, int saltLength, int modulusSize, BigInteger publicExponent) {
        return (KeyTemplate) KeyTemplate.newBuilder().setValue(((RsaSsaPssKeyFormat) RsaSsaPssKeyFormat.newBuilder().setParams((RsaSsaPssParams) RsaSsaPssParams.newBuilder().setSigHash(sigHash).setMgf1Hash(mgf1Hash).setSaltLength(saltLength).build()).setModulusSizeInBits(modulusSize).setPublicExponent(ByteString.copyFrom(publicExponent.toByteArray())).build()).toByteString()).setTypeUrl(RsaSsaPssSignKeyManager.getKeyType()).setOutputPrefixType(OutputPrefixType.TINK).build();
    }

    private SignatureKeyTemplates() {
    }
}
