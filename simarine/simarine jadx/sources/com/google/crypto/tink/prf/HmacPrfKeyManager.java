package com.google.crypto.tink.prf;

import com.google.crypto.tink.ConfigurationFips140v2$$ExternalSyntheticLambda5;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.KeyManager;
import com.google.crypto.tink.KeyTemplate;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.SecretKeyAccess;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.KeyManagerRegistry;
import com.google.crypto.tink.internal.LegacyKeyManagerImpl;
import com.google.crypto.tink.internal.MutableKeyCreationRegistry;
import com.google.crypto.tink.internal.MutableKeyDerivationRegistry;
import com.google.crypto.tink.internal.MutableParametersRegistry;
import com.google.crypto.tink.internal.MutablePrimitiveRegistry;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.internal.TinkBugException;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.prf.internal.HmacPrfProtoSerialization;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.util.SecretBytes;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class HmacPrfKeyManager {
    private static final PrimitiveConstructor<HmacPrfKey, Prf> PRF_PRIMITIVE_CONSTRUCTOR = PrimitiveConstructor.create(new ConfigurationFips140v2$$ExternalSyntheticLambda5(), HmacPrfKey.class, Prf.class);
    private static final KeyManager<Prf> legacyKeyManager = LegacyKeyManagerImpl.create(getKeyType(), Prf.class, KeyData.KeyMaterialType.SYMMETRIC, com.google.crypto.tink.proto.HmacPrfKey.parser());
    private static final MutableKeyCreationRegistry.KeyCreator<HmacPrfParameters> KEY_CREATOR = new MutableKeyCreationRegistry.KeyCreator() { // from class: com.google.crypto.tink.prf.HmacPrfKeyManager$$ExternalSyntheticLambda1
        @Override // com.google.crypto.tink.internal.MutableKeyCreationRegistry.KeyCreator
        public final Key createKey(Parameters parameters, Integer num) {
            return HmacPrfKeyManager.newKey((HmacPrfParameters) parameters, num);
        }
    };
    private static final MutableKeyDerivationRegistry.InsecureKeyCreator<HmacPrfParameters> KEY_DERIVER = new MutableKeyDerivationRegistry.InsecureKeyCreator() { // from class: com.google.crypto.tink.prf.HmacPrfKeyManager$$ExternalSyntheticLambda2
        @Override // com.google.crypto.tink.internal.MutableKeyDerivationRegistry.InsecureKeyCreator
        public final Key createKeyFromRandomness(Parameters parameters, InputStream inputStream, Integer num, SecretKeyAccess secretKeyAccess) {
            return HmacPrfKeyManager.createHmacKeyFromRandomness((HmacPrfParameters) parameters, inputStream, num, secretKeyAccess);
        }
    };
    private static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_REQUIRES_BORINGCRYPTO;

    /* JADX INFO: Access modifiers changed from: private */
    public static HmacPrfKey newKey(HmacPrfParameters parameters, @Nullable Integer idRequirement) throws GeneralSecurityException {
        if (idRequirement != null) {
            throw new GeneralSecurityException("Id Requirement is not supported for HMAC PRF keys");
        }
        return HmacPrfKey.builder().setParameters(parameters).setKeyBytes(SecretBytes.randomBytes(parameters.getKeySizeBytes())).build();
    }

    static String getKeyType() {
        return "type.googleapis.com/google.crypto.tink.HmacPrfKey";
    }

    static HmacPrfKey createHmacKeyFromRandomness(HmacPrfParameters parameters, InputStream stream, @Nullable Integer idRequirement, SecretKeyAccess access) throws GeneralSecurityException {
        return HmacPrfKey.builder().setParameters(parameters).setKeyBytes(Util.readIntoSecretBytes(stream, parameters.getKeySizeBytes(), access)).build();
    }

    private static Map<String, Parameters> namedParameters() throws GeneralSecurityException {
        HashMap map = new HashMap();
        map.put("HMAC_SHA256_PRF", PredefinedPrfParameters.HMAC_SHA256_PRF);
        map.put("HMAC_SHA512_PRF", PredefinedPrfParameters.HMAC_SHA512_PRF);
        return Collections.unmodifiableMap(map);
    }

    public static void register(boolean newKeyAllowed) throws GeneralSecurityException {
        TinkFipsUtil.AlgorithmFipsCompatibility algorithmFipsCompatibility = FIPS;
        if (!algorithmFipsCompatibility.isCompatible()) {
            throw new GeneralSecurityException("Can not use HMAC in FIPS-mode, as BoringCrypto module is not available.");
        }
        HmacPrfProtoSerialization.register();
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveConstructor(PRF_PRIMITIVE_CONSTRUCTOR);
        MutableParametersRegistry.globalInstance().putAll(namedParameters());
        MutableKeyCreationRegistry.globalInstance().add(KEY_CREATOR, HmacPrfParameters.class);
        MutableKeyDerivationRegistry.globalInstance().add(KEY_DERIVER, HmacPrfParameters.class);
        KeyManagerRegistry.globalInstance().registerKeyManagerWithFipsCompatibility(legacyKeyManager, algorithmFipsCompatibility, newKeyAllowed);
    }

    public static final KeyTemplate hmacSha256Template() {
        return (KeyTemplate) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.prf.HmacPrfKeyManager$$ExternalSyntheticLambda0
            @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
            public final Object get() {
                return KeyTemplate.createFrom(HmacPrfParameters.builder().setKeySizeBytes(32).setHashType(HmacPrfParameters.HashType.SHA256).build());
            }
        });
    }

    public static final KeyTemplate hmacSha512Template() {
        return (KeyTemplate) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.prf.HmacPrfKeyManager$$ExternalSyntheticLambda3
            @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
            public final Object get() {
                return KeyTemplate.createFrom(HmacPrfParameters.builder().setKeySizeBytes(64).setHashType(HmacPrfParameters.HashType.SHA512).build());
            }
        });
    }

    private HmacPrfKeyManager() {
    }
}
