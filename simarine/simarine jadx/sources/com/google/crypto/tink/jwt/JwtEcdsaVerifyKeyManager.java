package com.google.crypto.tink.jwt;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.proto.JwtEcdsaAlgorithm;
import com.google.crypto.tink.signature.EcdsaParameters;
import com.google.crypto.tink.signature.EcdsaPublicKey;
import com.google.crypto.tink.subtle.EcdsaVerifyJce;
import com.google.crypto.tink.subtle.EllipticCurves;
import com.google.crypto.tink.subtle.Enums;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
class JwtEcdsaVerifyKeyManager {
    static final PrimitiveConstructor<JwtEcdsaPublicKey, JwtPublicKeyVerify> PRIMITIVE_CONSTRUCTOR = PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.jwt.JwtEcdsaVerifyKeyManager$$ExternalSyntheticLambda0
        @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
        public final Object constructPrimitive(Key key) {
            return JwtEcdsaVerifyKeyManager.createFullPrimitive((JwtEcdsaPublicKey) key);
        }
    }, JwtEcdsaPublicKey.class, JwtPublicKeyVerify.class);

    static EcdsaParameters.CurveType getCurveType(JwtEcdsaParameters parameters) throws GeneralSecurityException {
        if (parameters.getAlgorithm().equals(JwtEcdsaParameters.Algorithm.ES256)) {
            return EcdsaParameters.CurveType.NIST_P256;
        }
        if (parameters.getAlgorithm().equals(JwtEcdsaParameters.Algorithm.ES384)) {
            return EcdsaParameters.CurveType.NIST_P384;
        }
        if (parameters.getAlgorithm().equals(JwtEcdsaParameters.Algorithm.ES512)) {
            return EcdsaParameters.CurveType.NIST_P521;
        }
        throw new GeneralSecurityException("unknown algorithm in parameters: " + parameters);
    }

    static EcdsaParameters.HashType getHash(JwtEcdsaParameters parameters) throws GeneralSecurityException {
        if (parameters.getAlgorithm().equals(JwtEcdsaParameters.Algorithm.ES256)) {
            return EcdsaParameters.HashType.SHA256;
        }
        if (parameters.getAlgorithm().equals(JwtEcdsaParameters.Algorithm.ES384)) {
            return EcdsaParameters.HashType.SHA384;
        }
        if (parameters.getAlgorithm().equals(JwtEcdsaParameters.Algorithm.ES512)) {
            return EcdsaParameters.HashType.SHA512;
        }
        throw new GeneralSecurityException("unknown algorithm in parameters: " + parameters);
    }

    static EcdsaPublicKey toEcdsaPublicKey(JwtEcdsaPublicKey publicKey) throws GeneralSecurityException {
        return EcdsaPublicKey.builder().setParameters(EcdsaParameters.builder().setSignatureEncoding(EcdsaParameters.SignatureEncoding.IEEE_P1363).setCurveType(getCurveType(publicKey.getParameters())).setHashType(getHash(publicKey.getParameters())).build()).setPublicPoint(publicKey.getPublicPoint()).build();
    }

    static JwtPublicKeyVerify createFullPrimitive(final JwtEcdsaPublicKey publicKey) throws GeneralSecurityException {
        final PublicKeyVerify publicKeyVerifyCreate = EcdsaVerifyJce.create(toEcdsaPublicKey(publicKey));
        return new JwtPublicKeyVerify() { // from class: com.google.crypto.tink.jwt.JwtEcdsaVerifyKeyManager.1
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

    /* JADX INFO: renamed from: com.google.crypto.tink.jwt.JwtEcdsaVerifyKeyManager$2, reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$google$crypto$tink$proto$JwtEcdsaAlgorithm;

        static {
            int[] iArr = new int[JwtEcdsaAlgorithm.values().length];
            $SwitchMap$com$google$crypto$tink$proto$JwtEcdsaAlgorithm = iArr;
            try {
                iArr[JwtEcdsaAlgorithm.ES256.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$proto$JwtEcdsaAlgorithm[JwtEcdsaAlgorithm.ES384.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$proto$JwtEcdsaAlgorithm[JwtEcdsaAlgorithm.ES512.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    static final EllipticCurves.CurveType getCurve(JwtEcdsaAlgorithm algorithm) throws GeneralSecurityException {
        int i = AnonymousClass2.$SwitchMap$com$google$crypto$tink$proto$JwtEcdsaAlgorithm[algorithm.ordinal()];
        if (i == 1) {
            return EllipticCurves.CurveType.NIST_P256;
        }
        if (i == 2) {
            return EllipticCurves.CurveType.NIST_P384;
        }
        if (i == 3) {
            return EllipticCurves.CurveType.NIST_P521;
        }
        throw new GeneralSecurityException("unknown algorithm " + algorithm.name());
    }

    public static Enums.HashType hashForEcdsaAlgorithm(JwtEcdsaAlgorithm algorithm) throws GeneralSecurityException {
        int i = AnonymousClass2.$SwitchMap$com$google$crypto$tink$proto$JwtEcdsaAlgorithm[algorithm.ordinal()];
        if (i == 1) {
            return Enums.HashType.SHA256;
        }
        if (i == 2) {
            return Enums.HashType.SHA384;
        }
        if (i == 3) {
            return Enums.HashType.SHA512;
        }
        throw new GeneralSecurityException("unknown algorithm " + algorithm.name());
    }

    static final void validateEcdsaAlgorithm(JwtEcdsaAlgorithm algorithm) throws GeneralSecurityException {
        hashForEcdsaAlgorithm(algorithm);
    }

    private JwtEcdsaVerifyKeyManager() {
    }

    static String getKeyType() {
        return "type.googleapis.com/google.crypto.tink.JwtEcdsaPublicKey";
    }
}
