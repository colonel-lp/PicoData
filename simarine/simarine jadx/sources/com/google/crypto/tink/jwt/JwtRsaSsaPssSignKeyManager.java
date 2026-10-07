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
import com.google.crypto.tink.signature.RsaSsaPssPrivateKey;
import com.google.crypto.tink.subtle.EngineFactory;
import com.google.crypto.tink.subtle.RsaSsaPssSignJce;
import com.google.crypto.tink.util.SecretBigInteger;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class JwtRsaSsaPssSignKeyManager {
    private static final PrivateKeyManager<Void> legacyPrivateKeyManager = LegacyKeyManagerImpl.createPrivateKeyManager(getKeyType(), Void.class, com.google.crypto.tink.proto.JwtRsaSsaPssPrivateKey.parser());
    private static final KeyManager<Void> legacyPublicKeyManager = LegacyKeyManagerImpl.create(JwtRsaSsaPssVerifyKeyManager.getKeyType(), Void.class, KeyData.KeyMaterialType.ASYMMETRIC_PUBLIC, com.google.crypto.tink.proto.JwtRsaSsaPssPublicKey.parser());
    private static final MutableKeyCreationRegistry.KeyCreator<JwtRsaSsaPssParameters> KEY_CREATOR = new MutableKeyCreationRegistry.KeyCreator() { // from class: com.google.crypto.tink.jwt.JwtRsaSsaPssSignKeyManager$$ExternalSyntheticLambda0
        @Override // com.google.crypto.tink.internal.MutableKeyCreationRegistry.KeyCreator
        public final Key createKey(Parameters parameters, Integer num) {
            return JwtRsaSsaPssSignKeyManager.createKey((JwtRsaSsaPssParameters) parameters, num);
        }
    };
    private static final PrimitiveConstructor<JwtRsaSsaPssPrivateKey, JwtPublicKeySign> PRIMITIVE_CONSTRUCTOR = PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.jwt.JwtRsaSsaPssSignKeyManager$$ExternalSyntheticLambda1
        @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
        public final Object constructPrimitive(Key key) {
            return JwtRsaSsaPssSignKeyManager.createFullPrimitive((JwtRsaSsaPssPrivateKey) key);
        }
    }, JwtRsaSsaPssPrivateKey.class, JwtPublicKeySign.class);
    private static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_REQUIRES_BORINGCRYPTO;

    /* JADX INFO: Access modifiers changed from: private */
    public static JwtRsaSsaPssPrivateKey createKey(JwtRsaSsaPssParameters parameters, @Nullable Integer idRequirement) throws GeneralSecurityException {
        KeyPairGenerator engineFactory = EngineFactory.KEY_PAIR_GENERATOR.getInstance("RSA");
        engineFactory.initialize(new RSAKeyGenParameterSpec(parameters.getModulusSizeBits(), new BigInteger(1, parameters.getPublicExponent().toByteArray())));
        KeyPair keyPairGenerateKeyPair = engineFactory.generateKeyPair();
        RSAPublicKey rSAPublicKey = (RSAPublicKey) keyPairGenerateKeyPair.getPublic();
        RSAPrivateCrtKey rSAPrivateCrtKey = (RSAPrivateCrtKey) keyPairGenerateKeyPair.getPrivate();
        JwtRsaSsaPssPublicKey.Builder modulus = JwtRsaSsaPssPublicKey.builder().setParameters(parameters).setModulus(rSAPublicKey.getModulus());
        if (idRequirement != null) {
            modulus.setIdRequirement(idRequirement);
        }
        return JwtRsaSsaPssPrivateKey.builder().setPublicKey(modulus.build()).setPrimes(SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getPrimeP(), InsecureSecretKeyAccess.get()), SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getPrimeQ(), InsecureSecretKeyAccess.get())).setPrivateExponent(SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getPrivateExponent(), InsecureSecretKeyAccess.get())).setPrimeExponents(SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getPrimeExponentP(), InsecureSecretKeyAccess.get()), SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getPrimeExponentQ(), InsecureSecretKeyAccess.get())).setCrtCoefficient(SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getCrtCoefficient(), InsecureSecretKeyAccess.get())).build();
    }

    private static RsaSsaPssPrivateKey toRsaSsaPssPrivateKey(JwtRsaSsaPssPrivateKey privateKey) throws GeneralSecurityException {
        return RsaSsaPssPrivateKey.builder().setPublicKey(JwtRsaSsaPssVerifyKeyManager.toRsaSsaPssPublicKey(privateKey.getPublicKey())).setPrimes(privateKey.getPrimeP(), privateKey.getPrimeQ()).setPrivateExponent(privateKey.getPrivateExponent()).setPrimeExponents(privateKey.getPrimeExponentP(), privateKey.getPrimeExponentQ()).setCrtCoefficient(privateKey.getCrtCoefficient()).build();
    }

    static JwtPublicKeySign createFullPrimitive(final JwtRsaSsaPssPrivateKey privateKey) throws GeneralSecurityException {
        final PublicKeySign publicKeySignCreate = RsaSsaPssSignJce.create(toRsaSsaPssPrivateKey(privateKey));
        final String standardName = privateKey.getParameters().getAlgorithm().getStandardName();
        return new JwtPublicKeySign() { // from class: com.google.crypto.tink.jwt.JwtRsaSsaPssSignKeyManager.1
            @Override // com.google.crypto.tink.jwt.JwtPublicKeySign
            public String signAndEncode(RawJwt rawJwt) throws GeneralSecurityException {
                String strCreateUnsignedCompact = JwtFormat.createUnsignedCompact(standardName, privateKey.getPublicKey().getKid(), rawJwt);
                return JwtFormat.createSignedCompact(strCreateUnsignedCompact, publicKeySignCreate.sign(strCreateUnsignedCompact.getBytes(StandardCharsets.US_ASCII)));
            }
        };
    }

    static String getKeyType() {
        return "type.googleapis.com/google.crypto.tink.JwtRsaSsaPssPrivateKey";
    }

    private static Map<String, Parameters> namedParameters() throws GeneralSecurityException {
        HashMap map = new HashMap();
        map.put("JWT_PS256_2048_F4_RAW", JwtRsaSsaPssParameters.builder().setModulusSizeBits(2048).setPublicExponent(JwtRsaSsaPssParameters.F4).setAlgorithm(JwtRsaSsaPssParameters.Algorithm.PS256).setKidStrategy(JwtRsaSsaPssParameters.KidStrategy.IGNORED).build());
        map.put("JWT_PS256_2048_F4", JwtRsaSsaPssParameters.builder().setModulusSizeBits(2048).setPublicExponent(JwtRsaSsaPssParameters.F4).setAlgorithm(JwtRsaSsaPssParameters.Algorithm.PS256).setKidStrategy(JwtRsaSsaPssParameters.KidStrategy.BASE64_ENCODED_KEY_ID).build());
        map.put("JWT_PS256_3072_F4_RAW", JwtRsaSsaPssParameters.builder().setModulusSizeBits(3072).setPublicExponent(JwtRsaSsaPssParameters.F4).setAlgorithm(JwtRsaSsaPssParameters.Algorithm.PS256).setKidStrategy(JwtRsaSsaPssParameters.KidStrategy.IGNORED).build());
        map.put("JWT_PS256_3072_F4", JwtRsaSsaPssParameters.builder().setModulusSizeBits(3072).setPublicExponent(JwtRsaSsaPssParameters.F4).setAlgorithm(JwtRsaSsaPssParameters.Algorithm.PS256).setKidStrategy(JwtRsaSsaPssParameters.KidStrategy.BASE64_ENCODED_KEY_ID).build());
        map.put("JWT_PS384_3072_F4_RAW", JwtRsaSsaPssParameters.builder().setModulusSizeBits(3072).setPublicExponent(JwtRsaSsaPssParameters.F4).setAlgorithm(JwtRsaSsaPssParameters.Algorithm.PS384).setKidStrategy(JwtRsaSsaPssParameters.KidStrategy.IGNORED).build());
        map.put("JWT_PS384_3072_F4", JwtRsaSsaPssParameters.builder().setModulusSizeBits(3072).setPublicExponent(JwtRsaSsaPssParameters.F4).setAlgorithm(JwtRsaSsaPssParameters.Algorithm.PS384).setKidStrategy(JwtRsaSsaPssParameters.KidStrategy.BASE64_ENCODED_KEY_ID).build());
        map.put("JWT_PS512_4096_F4_RAW", JwtRsaSsaPssParameters.builder().setModulusSizeBits(4096).setPublicExponent(JwtRsaSsaPssParameters.F4).setAlgorithm(JwtRsaSsaPssParameters.Algorithm.PS512).setKidStrategy(JwtRsaSsaPssParameters.KidStrategy.IGNORED).build());
        map.put("JWT_PS512_4096_F4", JwtRsaSsaPssParameters.builder().setModulusSizeBits(4096).setPublicExponent(JwtRsaSsaPssParameters.F4).setAlgorithm(JwtRsaSsaPssParameters.Algorithm.PS512).setKidStrategy(JwtRsaSsaPssParameters.KidStrategy.BASE64_ENCODED_KEY_ID).build());
        return Collections.unmodifiableMap(map);
    }

    public static void registerPair(boolean newKeyAllowed) throws GeneralSecurityException {
        TinkFipsUtil.AlgorithmFipsCompatibility algorithmFipsCompatibility = FIPS;
        if (!algorithmFipsCompatibility.isCompatible()) {
            throw new GeneralSecurityException("Can not use RSA SSA PSS in FIPS-mode, as BoringCrypto module is not available.");
        }
        JwtRsaSsaPssProtoSerialization.register();
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveConstructor(JwtRsaSsaPssVerifyKeyManager.PRIMITIVE_CONSTRUCTOR);
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveConstructor(PRIMITIVE_CONSTRUCTOR);
        MutableParametersRegistry.globalInstance().putAll(namedParameters());
        MutableKeyCreationRegistry.globalInstance().add(KEY_CREATOR, JwtRsaSsaPssParameters.class);
        KeyManagerRegistry.globalInstance().registerKeyManagerWithFipsCompatibility(legacyPrivateKeyManager, algorithmFipsCompatibility, newKeyAllowed);
        KeyManagerRegistry.globalInstance().registerKeyManagerWithFipsCompatibility(legacyPublicKeyManager, algorithmFipsCompatibility, false);
    }

    private JwtRsaSsaPssSignKeyManager() {
    }
}
