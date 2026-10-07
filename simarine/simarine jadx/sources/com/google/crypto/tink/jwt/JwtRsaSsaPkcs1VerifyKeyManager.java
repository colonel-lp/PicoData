package com.google.crypto.tink.jwt;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.signature.RsaSsaPkcs1Parameters;
import com.google.crypto.tink.signature.RsaSsaPkcs1PublicKey;
import com.google.crypto.tink.subtle.RsaSsaPkcs1VerifyJce;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
final class JwtRsaSsaPkcs1VerifyKeyManager {
    static final PrimitiveConstructor<JwtRsaSsaPkcs1PublicKey, JwtPublicKeyVerify> PRIMITIVE_CONSTRUCTOR = PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.jwt.JwtRsaSsaPkcs1VerifyKeyManager$$ExternalSyntheticLambda0
        @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
        public final Object constructPrimitive(Key key) {
            return JwtRsaSsaPkcs1VerifyKeyManager.createFullPrimitive((JwtRsaSsaPkcs1PublicKey) key);
        }
    }, JwtRsaSsaPkcs1PublicKey.class, JwtPublicKeyVerify.class);

    private static RsaSsaPkcs1Parameters.HashType hashTypeForAlgorithm(JwtRsaSsaPkcs1Parameters.Algorithm algorithm) throws GeneralSecurityException {
        if (algorithm.equals(JwtRsaSsaPkcs1Parameters.Algorithm.RS256)) {
            return RsaSsaPkcs1Parameters.HashType.SHA256;
        }
        if (algorithm.equals(JwtRsaSsaPkcs1Parameters.Algorithm.RS384)) {
            return RsaSsaPkcs1Parameters.HashType.SHA384;
        }
        if (algorithm.equals(JwtRsaSsaPkcs1Parameters.Algorithm.RS512)) {
            return RsaSsaPkcs1Parameters.HashType.SHA512;
        }
        throw new GeneralSecurityException("unknown algorithm " + algorithm);
    }

    static RsaSsaPkcs1PublicKey toRsaSsaPkcs1PublicKey(JwtRsaSsaPkcs1PublicKey publicKey) throws GeneralSecurityException {
        return RsaSsaPkcs1PublicKey.builder().setParameters(RsaSsaPkcs1Parameters.builder().setModulusSizeBits(publicKey.getParameters().getModulusSizeBits()).setPublicExponent(publicKey.getParameters().getPublicExponent()).setHashType(hashTypeForAlgorithm(publicKey.getParameters().getAlgorithm())).setVariant(RsaSsaPkcs1Parameters.Variant.NO_PREFIX).build()).setModulus(publicKey.getModulus()).build();
    }

    static JwtPublicKeyVerify createFullPrimitive(final JwtRsaSsaPkcs1PublicKey publicKey) throws GeneralSecurityException {
        final PublicKeyVerify publicKeyVerifyCreate = RsaSsaPkcs1VerifyJce.create(toRsaSsaPkcs1PublicKey(publicKey));
        return new JwtPublicKeyVerify() { // from class: com.google.crypto.tink.jwt.JwtRsaSsaPkcs1VerifyKeyManager.1
            @Override // com.google.crypto.tink.jwt.JwtPublicKeyVerify
            public VerifiedJwt verifyAndDecode(String compact, JwtValidator validator) throws GeneralSecurityException {
                JwtFormat.Parts partsSplitSignedCompact = JwtFormat.splitSignedCompact(compact);
                publicKeyVerifyCreate.verify(partsSplitSignedCompact.signatureOrMac, partsSplitSignedCompact.unsignedCompact.getBytes(StandardCharsets.US_ASCII));
                JsonObject json = JsonUtil.parseJson(partsSplitSignedCompact.header);
                JwtFormat.validateHeader(json, publicKey.getParameters().getAlgorithm().getStandardName(), publicKey.getKid(), publicKey.getParameters().allowKidAbsent());
                return validator.validate(RawJwt.fromJsonPayload(JwtFormat.getTypeHeader(json), partsSplitSignedCompact.payload));
            }
        };
    }

    static String getKeyType() {
        return "type.googleapis.com/google.crypto.tink.JwtRsaSsaPkcs1PublicKey";
    }

    private JwtRsaSsaPkcs1VerifyKeyManager() {
    }
}
