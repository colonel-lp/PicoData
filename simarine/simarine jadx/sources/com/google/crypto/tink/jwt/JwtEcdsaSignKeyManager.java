package com.google.crypto.tink.jwt;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.KeyManager;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.PrivateKeyManager;
import com.google.crypto.tink.PublicKeySign;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.KeyManagerRegistry;
import com.google.crypto.tink.internal.LegacyKeyManagerImpl;
import com.google.crypto.tink.internal.MutableKeyCreationRegistry;
import com.google.crypto.tink.internal.MutableParametersRegistry;
import com.google.crypto.tink.internal.MutablePrimitiveRegistry;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.signature.EcdsaPrivateKey;
import com.google.crypto.tink.subtle.EcdsaSignJce;
import com.google.crypto.tink.subtle.EllipticCurves;
import com.google.crypto.tink.util.SecretBigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class JwtEcdsaSignKeyManager {
    private static final PrivateKeyManager<Void> legacyPrivateKeyManager = LegacyKeyManagerImpl.createPrivateKeyManager(getKeyType(), Void.class, com.google.crypto.tink.proto.JwtEcdsaPrivateKey.parser());
    private static final KeyManager<Void> legacyPublicKeyManager = LegacyKeyManagerImpl.create(JwtEcdsaVerifyKeyManager.getKeyType(), Void.class, KeyData.KeyMaterialType.ASYMMETRIC_PUBLIC, com.google.crypto.tink.proto.JwtEcdsaPublicKey.parser());
    private static final PrimitiveConstructor<JwtEcdsaPrivateKey, JwtPublicKeySign> PRIMITIVE_CONSTRUCTOR = PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.jwt.JwtEcdsaSignKeyManager$$ExternalSyntheticLambda0
        @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
        public final Object constructPrimitive(Key key) {
            return JwtEcdsaSignKeyManager.createFullPrimitive((JwtEcdsaPrivateKey) key);
        }
    }, JwtEcdsaPrivateKey.class, JwtPublicKeySign.class);
    private static final MutableKeyCreationRegistry.KeyCreator<JwtEcdsaParameters> KEY_CREATOR = new MutableKeyCreationRegistry.KeyCreator() { // from class: com.google.crypto.tink.jwt.JwtEcdsaSignKeyManager$$ExternalSyntheticLambda1
        @Override // com.google.crypto.tink.internal.MutableKeyCreationRegistry.KeyCreator
        public final Key createKey(Parameters parameters, Integer num) {
            return JwtEcdsaSignKeyManager.createKey((JwtEcdsaParameters) parameters, num);
        }
    };
    private static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_REQUIRES_BORINGCRYPTO;

    private static EcdsaPrivateKey toEcdsaPrivateKey(JwtEcdsaPrivateKey privateKey) throws GeneralSecurityException {
        return EcdsaPrivateKey.builder().setPublicKey(JwtEcdsaVerifyKeyManager.toEcdsaPublicKey(privateKey.getPublicKey())).setPrivateValue(privateKey.getPrivateValue()).build();
    }

    static JwtPublicKeySign createFullPrimitive(final JwtEcdsaPrivateKey privateKey) throws GeneralSecurityException {
        final PublicKeySign publicKeySignCreate = EcdsaSignJce.create(toEcdsaPrivateKey(privateKey));
        final String standardName = privateKey.getParameters().getAlgorithm().getStandardName();
        return new JwtPublicKeySign() { // from class: com.google.crypto.tink.jwt.JwtEcdsaSignKeyManager.1
            @Override // com.google.crypto.tink.jwt.JwtPublicKeySign
            public String signAndEncode(RawJwt rawJwt) throws GeneralSecurityException {
                String strCreateUnsignedCompact = JwtFormat.createUnsignedCompact(standardName, privateKey.getPublicKey().getKid(), rawJwt);
                return JwtFormat.createSignedCompact(strCreateUnsignedCompact, publicKeySignCreate.sign(strCreateUnsignedCompact.getBytes(StandardCharsets.US_ASCII)));
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static JwtEcdsaPrivateKey createKey(JwtEcdsaParameters parameters, @Nullable Integer idRequirement) throws GeneralSecurityException {
        KeyPair keyPairGenerateKeyPair = EllipticCurves.generateKeyPair(parameters.getAlgorithm().getECParameterSpec());
        ECPublicKey eCPublicKey = (ECPublicKey) keyPairGenerateKeyPair.getPublic();
        ECPrivateKey eCPrivateKey = (ECPrivateKey) keyPairGenerateKeyPair.getPrivate();
        JwtEcdsaPublicKey.Builder publicPoint = JwtEcdsaPublicKey.builder().setParameters(parameters).setPublicPoint(eCPublicKey.getW());
        if (idRequirement != null) {
            publicPoint.setIdRequirement(idRequirement);
        }
        return JwtEcdsaPrivateKey.create(publicPoint.build(), SecretBigInteger.fromBigInteger(eCPrivateKey.getS(), InsecureSecretKeyAccess.get()));
    }

    private JwtEcdsaSignKeyManager() {
    }

    static String getKeyType() {
        return "type.googleapis.com/google.crypto.tink.JwtEcdsaPrivateKey";
    }

    private static Map<String, Parameters> namedParameters() throws GeneralSecurityException {
        HashMap map = new HashMap();
        map.put("JWT_ES256_RAW", JwtEcdsaParameters.builder().setAlgorithm(JwtEcdsaParameters.Algorithm.ES256).setKidStrategy(JwtEcdsaParameters.KidStrategy.IGNORED).build());
        map.put("JWT_ES256", JwtEcdsaParameters.builder().setAlgorithm(JwtEcdsaParameters.Algorithm.ES256).setKidStrategy(JwtEcdsaParameters.KidStrategy.BASE64_ENCODED_KEY_ID).build());
        map.put("JWT_ES384_RAW", JwtEcdsaParameters.builder().setAlgorithm(JwtEcdsaParameters.Algorithm.ES384).setKidStrategy(JwtEcdsaParameters.KidStrategy.IGNORED).build());
        map.put("JWT_ES384", JwtEcdsaParameters.builder().setAlgorithm(JwtEcdsaParameters.Algorithm.ES384).setKidStrategy(JwtEcdsaParameters.KidStrategy.BASE64_ENCODED_KEY_ID).build());
        map.put("JWT_ES512_RAW", JwtEcdsaParameters.builder().setAlgorithm(JwtEcdsaParameters.Algorithm.ES512).setKidStrategy(JwtEcdsaParameters.KidStrategy.IGNORED).build());
        map.put("JWT_ES512", JwtEcdsaParameters.builder().setAlgorithm(JwtEcdsaParameters.Algorithm.ES512).setKidStrategy(JwtEcdsaParameters.KidStrategy.BASE64_ENCODED_KEY_ID).build());
        return Collections.unmodifiableMap(map);
    }

    public static void registerPair(boolean newKeyAllowed) throws GeneralSecurityException {
        TinkFipsUtil.AlgorithmFipsCompatibility algorithmFipsCompatibility = FIPS;
        if (!algorithmFipsCompatibility.isCompatible()) {
            throw new GeneralSecurityException("Can not use ECDSA in FIPS-mode, as BoringCrypto module is not available.");
        }
        KeyManagerRegistry.globalInstance().registerKeyManagerWithFipsCompatibility(legacyPrivateKeyManager, algorithmFipsCompatibility, newKeyAllowed);
        KeyManagerRegistry.globalInstance().registerKeyManagerWithFipsCompatibility(legacyPublicKeyManager, algorithmFipsCompatibility, false);
        MutableKeyCreationRegistry.globalInstance().add(KEY_CREATOR, JwtEcdsaParameters.class);
        JwtEcdsaProtoSerialization.register();
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveConstructor(JwtEcdsaVerifyKeyManager.PRIMITIVE_CONSTRUCTOR);
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveConstructor(PRIMITIVE_CONSTRUCTOR);
        MutableParametersRegistry.globalInstance().putAll(namedParameters());
    }
}
