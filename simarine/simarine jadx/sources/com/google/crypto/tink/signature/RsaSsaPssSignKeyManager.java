package com.google.crypto.tink.signature;

import com.google.crypto.tink.ConfigurationV0$$ExternalSyntheticLambda14;
import com.google.crypto.tink.ConfigurationV0$$ExternalSyntheticLambda15;
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
import com.google.crypto.tink.signature.internal.RsaSsaPssProtoSerialization;
import com.google.crypto.tink.subtle.EngineFactory;
import com.google.crypto.tink.util.SecretBigInteger;
import java.math.BigInteger;
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
public final class RsaSsaPssSignKeyManager {
    private static final PrimitiveConstructor<RsaSsaPssPrivateKey, PublicKeySign> PUBLIC_KEY_SIGN_PRIMITIVE_CONSTRUCTOR = PrimitiveConstructor.create(new ConfigurationV0$$ExternalSyntheticLambda14(), RsaSsaPssPrivateKey.class, PublicKeySign.class);
    private static final PrimitiveConstructor<RsaSsaPssPublicKey, PublicKeyVerify> PUBLIC_KEY_VERIFY_PRIMITIVE_CONSTRUCTOR = PrimitiveConstructor.create(new ConfigurationV0$$ExternalSyntheticLambda15(), RsaSsaPssPublicKey.class, PublicKeyVerify.class);
    private static final PrivateKeyManager<PublicKeySign> legacyPrivateKeyManager = LegacyKeyManagerImpl.createPrivateKeyManager(getKeyType(), PublicKeySign.class, com.google.crypto.tink.proto.RsaSsaPssPrivateKey.parser());
    private static final KeyManager<PublicKeyVerify> legacyPublicKeyManager = LegacyKeyManagerImpl.create(RsaSsaPssVerifyKeyManager.getKeyType(), PublicKeyVerify.class, KeyData.KeyMaterialType.ASYMMETRIC_PUBLIC, com.google.crypto.tink.proto.RsaSsaPssPublicKey.parser());
    private static final MutableKeyCreationRegistry.KeyCreator<RsaSsaPssParameters> KEY_CREATOR = new MutableKeyCreationRegistry.KeyCreator() { // from class: com.google.crypto.tink.signature.RsaSsaPssSignKeyManager$$ExternalSyntheticLambda1
        @Override // com.google.crypto.tink.internal.MutableKeyCreationRegistry.KeyCreator
        public final Key createKey(Parameters parameters, Integer num) {
            return RsaSsaPssSignKeyManager.createKey((RsaSsaPssParameters) parameters, num);
        }
    };
    private static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_REQUIRES_BORINGCRYPTO;

    static String getKeyType() {
        return "type.googleapis.com/google.crypto.tink.RsaSsaPssPrivateKey";
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static RsaSsaPssPrivateKey createKey(RsaSsaPssParameters parameters, @Nullable Integer idRequirement) throws GeneralSecurityException {
        KeyPairGenerator engineFactory = EngineFactory.KEY_PAIR_GENERATOR.getInstance("RSA");
        engineFactory.initialize(new RSAKeyGenParameterSpec(parameters.getModulusSizeBits(), new BigInteger(1, parameters.getPublicExponent().toByteArray())));
        KeyPair keyPairGenerateKeyPair = engineFactory.generateKeyPair();
        RSAPublicKey rSAPublicKey = (RSAPublicKey) keyPairGenerateKeyPair.getPublic();
        RSAPrivateCrtKey rSAPrivateCrtKey = (RSAPrivateCrtKey) keyPairGenerateKeyPair.getPrivate();
        return RsaSsaPssPrivateKey.builder().setPublicKey(RsaSsaPssPublicKey.builder().setParameters(parameters).setModulus(rSAPublicKey.getModulus()).setIdRequirement(idRequirement).build()).setPrimes(SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getPrimeP(), InsecureSecretKeyAccess.get()), SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getPrimeQ(), InsecureSecretKeyAccess.get())).setPrivateExponent(SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getPrivateExponent(), InsecureSecretKeyAccess.get())).setPrimeExponents(SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getPrimeExponentP(), InsecureSecretKeyAccess.get()), SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getPrimeExponentQ(), InsecureSecretKeyAccess.get())).setCrtCoefficient(SecretBigInteger.fromBigInteger(rSAPrivateCrtKey.getCrtCoefficient(), InsecureSecretKeyAccess.get())).build();
    }

    private static Map<String, Parameters> namedParameters() throws GeneralSecurityException {
        HashMap map = new HashMap();
        map.put("RSA_SSA_PSS_3072_SHA256_F4", RsaSsaPssParameters.builder().setSigHashType(RsaSsaPssParameters.HashType.SHA256).setMgf1HashType(RsaSsaPssParameters.HashType.SHA256).setSaltLengthBytes(32).setModulusSizeBits(3072).setPublicExponent(RsaSsaPssParameters.F4).setVariant(RsaSsaPssParameters.Variant.TINK).build());
        map.put("RSA_SSA_PSS_3072_SHA256_F4_RAW", RsaSsaPssParameters.builder().setSigHashType(RsaSsaPssParameters.HashType.SHA256).setMgf1HashType(RsaSsaPssParameters.HashType.SHA256).setSaltLengthBytes(32).setModulusSizeBits(3072).setPublicExponent(RsaSsaPssParameters.F4).setVariant(RsaSsaPssParameters.Variant.NO_PREFIX).build());
        map.put("RSA_SSA_PSS_3072_SHA256_SHA256_32_F4", PredefinedSignatureParameters.RSA_SSA_PSS_3072_SHA256_SHA256_32_F4);
        map.put("RSA_SSA_PSS_4096_SHA512_F4", RsaSsaPssParameters.builder().setSigHashType(RsaSsaPssParameters.HashType.SHA512).setMgf1HashType(RsaSsaPssParameters.HashType.SHA512).setSaltLengthBytes(64).setModulusSizeBits(4096).setPublicExponent(RsaSsaPssParameters.F4).setVariant(RsaSsaPssParameters.Variant.TINK).build());
        map.put("RSA_SSA_PSS_4096_SHA512_F4_RAW", RsaSsaPssParameters.builder().setSigHashType(RsaSsaPssParameters.HashType.SHA512).setMgf1HashType(RsaSsaPssParameters.HashType.SHA512).setSaltLengthBytes(64).setModulusSizeBits(4096).setPublicExponent(RsaSsaPssParameters.F4).setVariant(RsaSsaPssParameters.Variant.NO_PREFIX).build());
        map.put("RSA_SSA_PSS_4096_SHA512_SHA512_64_F4", PredefinedSignatureParameters.RSA_SSA_PSS_4096_SHA512_SHA512_64_F4);
        return Collections.unmodifiableMap(map);
    }

    public static void registerPair(boolean newKeyAllowed) throws GeneralSecurityException {
        TinkFipsUtil.AlgorithmFipsCompatibility algorithmFipsCompatibility = FIPS;
        if (!algorithmFipsCompatibility.isCompatible()) {
            throw new GeneralSecurityException("Can not use RSA SSA PSS in FIPS-mode, as BoringCrypto module is not available.");
        }
        RsaSsaPssProtoSerialization.register();
        MutableParametersRegistry.globalInstance().putAll(namedParameters());
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveConstructor(PUBLIC_KEY_SIGN_PRIMITIVE_CONSTRUCTOR);
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveConstructor(PUBLIC_KEY_VERIFY_PRIMITIVE_CONSTRUCTOR);
        MutableKeyCreationRegistry.globalInstance().add(KEY_CREATOR, RsaSsaPssParameters.class);
        KeyManagerRegistry.globalInstance().registerKeyManagerWithFipsCompatibility(legacyPrivateKeyManager, algorithmFipsCompatibility, newKeyAllowed);
        KeyManagerRegistry.globalInstance().registerKeyManagerWithFipsCompatibility(legacyPublicKeyManager, algorithmFipsCompatibility, false);
    }

    public static final KeyTemplate rsa3072PssSha256F4Template() {
        return (KeyTemplate) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.signature.RsaSsaPssSignKeyManager$$ExternalSyntheticLambda4
            @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
            public final Object get() {
                return KeyTemplate.createFrom(RsaSsaPssParameters.builder().setSigHashType(RsaSsaPssParameters.HashType.SHA256).setMgf1HashType(RsaSsaPssParameters.HashType.SHA256).setSaltLengthBytes(32).setModulusSizeBits(3072).setPublicExponent(RsaSsaPssParameters.F4).setVariant(RsaSsaPssParameters.Variant.TINK).build());
            }
        });
    }

    public static final KeyTemplate rawRsa3072PssSha256F4Template() {
        return (KeyTemplate) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.signature.RsaSsaPssSignKeyManager$$ExternalSyntheticLambda2
            @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
            public final Object get() {
                return KeyTemplate.createFrom(RsaSsaPssParameters.builder().setSigHashType(RsaSsaPssParameters.HashType.SHA256).setMgf1HashType(RsaSsaPssParameters.HashType.SHA256).setSaltLengthBytes(32).setModulusSizeBits(3072).setPublicExponent(RsaSsaPssParameters.F4).setVariant(RsaSsaPssParameters.Variant.NO_PREFIX).build());
            }
        });
    }

    public static final KeyTemplate rsa4096PssSha512F4Template() {
        return (KeyTemplate) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.signature.RsaSsaPssSignKeyManager$$ExternalSyntheticLambda0
            @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
            public final Object get() {
                return KeyTemplate.createFrom(RsaSsaPssParameters.builder().setSigHashType(RsaSsaPssParameters.HashType.SHA512).setMgf1HashType(RsaSsaPssParameters.HashType.SHA512).setSaltLengthBytes(64).setModulusSizeBits(4096).setPublicExponent(RsaSsaPssParameters.F4).setVariant(RsaSsaPssParameters.Variant.TINK).build());
            }
        });
    }

    public static final KeyTemplate rawRsa4096PssSha512F4Template() {
        return (KeyTemplate) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.signature.RsaSsaPssSignKeyManager$$ExternalSyntheticLambda3
            @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
            public final Object get() {
                return KeyTemplate.createFrom(RsaSsaPssParameters.builder().setSigHashType(RsaSsaPssParameters.HashType.SHA512).setMgf1HashType(RsaSsaPssParameters.HashType.SHA512).setSaltLengthBytes(64).setModulusSizeBits(4096).setPublicExponent(RsaSsaPssParameters.F4).setVariant(RsaSsaPssParameters.Variant.NO_PREFIX).build());
            }
        });
    }

    private RsaSsaPssSignKeyManager() {
    }
}
