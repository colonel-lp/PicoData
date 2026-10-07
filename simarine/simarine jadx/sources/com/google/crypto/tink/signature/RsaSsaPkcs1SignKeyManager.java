package com.google.crypto.tink.signature;

import com.google.crypto.tink.ConfigurationV0$$ExternalSyntheticLambda12;
import com.google.crypto.tink.ConfigurationV0$$ExternalSyntheticLambda13;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.KeyManager;
import com.google.crypto.tink.KeyTemplate;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.PrivateKeyManager;
import com.google.crypto.tink.PublicKeySign;
import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.KeyManagerRegistry;
import com.google.crypto.tink.internal.LegacyKeyManagerImpl;
import com.google.crypto.tink.internal.MutableKeyCreationRegistry;
import com.google.crypto.tink.internal.MutableParametersRegistry;
import com.google.crypto.tink.internal.MutablePrimitiveRegistry;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.internal.TinkBugException;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.signature.internal.RsaSsaPkcs1ProtoSerialization;
import com.google.crypto.tink.subtle.EngineFactory;
import com.google.crypto.tink.util.SecretBigInteger;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class RsaSsaPkcs1SignKeyManager {
    private static final PrimitiveConstructor<RsaSsaPkcs1PrivateKey, PublicKeySign> PUBLIC_KEY_SIGN_PRIMITIVE_CONSTRUCTOR = PrimitiveConstructor.create(new ConfigurationV0$$ExternalSyntheticLambda12(), RsaSsaPkcs1PrivateKey.class, PublicKeySign.class);
    private static final PrimitiveConstructor<RsaSsaPkcs1PublicKey, PublicKeyVerify> PUBLIC_KEY_VERIFY_PRIMITIVE_CONSTRUCTOR = PrimitiveConstructor.create(new ConfigurationV0$$ExternalSyntheticLambda13(), RsaSsaPkcs1PublicKey.class, PublicKeyVerify.class);
    private static final PrivateKeyManager<PublicKeySign> legacyPrivateKeyManager = LegacyKeyManagerImpl.createPrivateKeyManager(getKeyType(), PublicKeySign.class, com.google.crypto.tink.proto.RsaSsaPkcs1PrivateKey.parser());
    private static final KeyManager<PublicKeyVerify> legacyPublicKeyManager = LegacyKeyManagerImpl.create(RsaSsaPkcs1VerifyKeyManager.getKeyType(), PublicKeyVerify.class, KeyData.KeyMaterialType.ASYMMETRIC_PUBLIC, com.google.crypto.tink.proto.RsaSsaPkcs1PublicKey.parser());
    private static final MutableKeyCreationRegistry.KeyCreator<RsaSsaPkcs1Parameters> KEY_CREATOR = new MutableKeyCreationRegistry.KeyCreator() { // from class: com.google.crypto.tink.signature.RsaSsaPkcs1SignKeyManager$$ExternalSyntheticLambda4
        @Override // com.google.crypto.tink.internal.MutableKeyCreationRegistry.KeyCreator
        public final Key createKey(Parameters parameters, Integer num) {
            return RsaSsaPkcs1SignKeyManager.createKey((RsaSsaPkcs1Parameters) parameters, num);
        }
    };
    private static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_REQUIRES_BORINGCRYPTO;

    static String getKeyType() {
        return "type.googleapis.com/google.crypto.tink.RsaSsaPkcs1PrivateKey";
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static RsaSsaPkcs1PrivateKey createKey(RsaSsaPkcs1Parameters parameters, @Nullable Integer idRequirement) throws GeneralSecurityException {
        KeyPairGenerator engineFactory = EngineFactory.KEY_PAIR_GENERATOR.getInstance("RSA");
        engineFactory.initialize(new RSAKeyGenParameterSpec(parameters.getModulusSizeBits(), new BigInteger(1, parameters.getPublicExponent().toByteArray())));
        KeyPair keyPairGenerateKeyPair = engineFactory.generateKeyPair();
        RSAPublicKey rSAPublicKey = (RSAPublicKey) keyPairGenerateKeyPair.getPublic();
        RSAPrivateCrtKey rSAPrivateCrtKey = (RSAPrivateCrtKey) keyPairGenerateKeyPair.getPrivate();
        return RsaSsaPkcs1PrivateKey.builder().setPublicKey(RsaSsaPkcs1PublicKey.builder().setParameters(parameters).setModulus(rSAPublicKey.getModulus()).setIdRequirement(idRequirement).build()).setPrimes(SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getPrimeP(), InsecureSecretKeyAccess.get()), SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getPrimeQ(), InsecureSecretKeyAccess.get())).setPrivateExponent(SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getPrivateExponent(), InsecureSecretKeyAccess.get())).setPrimeExponents(SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getPrimeExponentP(), InsecureSecretKeyAccess.get()), SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getPrimeExponentQ(), InsecureSecretKeyAccess.get())).setCrtCoefficient(SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getCrtCoefficient(), InsecureSecretKeyAccess.get())).build();
    }

    private static Map<String, Parameters> namedParameters() throws GeneralSecurityException {
        HashMap map = new HashMap();
        map.put("RSA_SSA_PKCS1_3072_SHA256_F4", PredefinedSignatureParameters.RSA_SSA_PKCS1_3072_SHA256_F4);
        map.put("RSA_SSA_PKCS1_3072_SHA256_F4_RAW", RsaSsaPkcs1Parameters.builder().setHashType(RsaSsaPkcs1Parameters.HashType.SHA256).setModulusSizeBits(3072).setPublicExponent(RsaSsaPkcs1Parameters.F4).setVariant(RsaSsaPkcs1Parameters.Variant.NO_PREFIX).build());
        map.put("RSA_SSA_PKCS1_3072_SHA256_F4_WITHOUT_PREFIX", PredefinedSignatureParameters.RSA_SSA_PKCS1_3072_SHA256_F4_WITHOUT_PREFIX);
        map.put("RSA_SSA_PKCS1_4096_SHA512_F4", PredefinedSignatureParameters.RSA_SSA_PKCS1_4096_SHA512_F4);
        map.put("RSA_SSA_PKCS1_4096_SHA512_F4_RAW", RsaSsaPkcs1Parameters.builder().setHashType(RsaSsaPkcs1Parameters.HashType.SHA512).setModulusSizeBits(4096).setPublicExponent(RsaSsaPkcs1Parameters.F4).setVariant(RsaSsaPkcs1Parameters.Variant.NO_PREFIX).build());
        return map;
    }

    public static void registerPair(boolean newKeyAllowed) throws GeneralSecurityException {
        TinkFipsUtil.AlgorithmFipsCompatibility algorithmFipsCompatibility = FIPS;
        if (!algorithmFipsCompatibility.isCompatible()) {
            throw new GeneralSecurityException("Can not use RSA SSA PKCS1 in FIPS-mode, as BoringCrypto module is not available.");
        }
        RsaSsaPkcs1ProtoSerialization.register();
        MutableParametersRegistry.globalInstance().putAll(namedParameters());
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveConstructor(PUBLIC_KEY_SIGN_PRIMITIVE_CONSTRUCTOR);
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveConstructor(PUBLIC_KEY_VERIFY_PRIMITIVE_CONSTRUCTOR);
        MutableKeyCreationRegistry.globalInstance().add(KEY_CREATOR, RsaSsaPkcs1Parameters.class);
        KeyManagerRegistry.globalInstance().registerKeyManagerWithFipsCompatibility(legacyPrivateKeyManager, algorithmFipsCompatibility, newKeyAllowed);
        KeyManagerRegistry.globalInstance().registerKeyManagerWithFipsCompatibility(legacyPublicKeyManager, algorithmFipsCompatibility, false);
    }

    public static final KeyTemplate rsa3072SsaPkcs1Sha256F4Template() {
        return (KeyTemplate) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.signature.RsaSsaPkcs1SignKeyManager$$ExternalSyntheticLambda3
            @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
            public final Object get() {
                return KeyTemplate.createFrom(RsaSsaPkcs1Parameters.builder().setModulusSizeBits(3072).setPublicExponent(RsaSsaPkcs1Parameters.F4).setHashType(RsaSsaPkcs1Parameters.HashType.SHA256).setVariant(RsaSsaPkcs1Parameters.Variant.TINK).build());
            }
        });
    }

    public static final KeyTemplate rawRsa3072SsaPkcs1Sha256F4Template() {
        return (KeyTemplate) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.signature.RsaSsaPkcs1SignKeyManager$$ExternalSyntheticLambda0
            @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
            public final Object get() {
                return KeyTemplate.createFrom(RsaSsaPkcs1Parameters.builder().setModulusSizeBits(3072).setPublicExponent(RsaSsaPkcs1Parameters.F4).setHashType(RsaSsaPkcs1Parameters.HashType.SHA256).setVariant(RsaSsaPkcs1Parameters.Variant.NO_PREFIX).build());
            }
        });
    }

    public static final KeyTemplate rsa4096SsaPkcs1Sha512F4Template() {
        return (KeyTemplate) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.signature.RsaSsaPkcs1SignKeyManager$$ExternalSyntheticLambda2
            @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
            public final Object get() {
                return KeyTemplate.createFrom(RsaSsaPkcs1Parameters.builder().setModulusSizeBits(4096).setPublicExponent(RsaSsaPkcs1Parameters.F4).setHashType(RsaSsaPkcs1Parameters.HashType.SHA512).setVariant(RsaSsaPkcs1Parameters.Variant.TINK).build());
            }
        });
    }

    public static final KeyTemplate rawRsa4096SsaPkcs1Sha512F4Template() {
        return (KeyTemplate) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.signature.RsaSsaPkcs1SignKeyManager$$ExternalSyntheticLambda1
            @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
            public final Object get() {
                return KeyTemplate.createFrom(RsaSsaPkcs1Parameters.builder().setModulusSizeBits(4096).setPublicExponent(RsaSsaPkcs1Parameters.F4).setHashType(RsaSsaPkcs1Parameters.HashType.SHA512).setVariant(RsaSsaPkcs1Parameters.Variant.NO_PREFIX).build());
            }
        });
    }

    private RsaSsaPkcs1SignKeyManager() {
    }
}
