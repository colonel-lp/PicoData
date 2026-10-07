package com.google.crypto.tink.prf;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.KeyManager;
import com.google.crypto.tink.KeyTemplate;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.KeyManagerRegistry;
import com.google.crypto.tink.internal.LegacyKeyManagerImpl;
import com.google.crypto.tink.internal.MutableKeyCreationRegistry;
import com.google.crypto.tink.internal.MutableParametersRegistry;
import com.google.crypto.tink.internal.MutablePrimitiveRegistry;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.internal.TinkBugException;
import com.google.crypto.tink.prf.internal.AesCmacPrfProtoSerialization;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.subtle.PrfAesCmac;
import com.google.crypto.tink.util.SecretBytes;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class AesCmacPrfKeyManager {
    private static final PrimitiveConstructor<AesCmacPrfKey, Prf> PRF_PRIMITIVE_CONSTRUCTOR = PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.prf.AesCmacPrfKeyManager$$ExternalSyntheticLambda1
        @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
        public final Object constructPrimitive(Key key) {
            return AesCmacPrfKeyManager.createPrimitive((AesCmacPrfKey) key);
        }
    }, AesCmacPrfKey.class, Prf.class);
    private static final KeyManager<Prf> legacyKeyManager = LegacyKeyManagerImpl.create(getKeyType(), Prf.class, KeyData.KeyMaterialType.SYMMETRIC, com.google.crypto.tink.proto.AesCmacPrfKey.parser());
    private static final MutableKeyCreationRegistry.KeyCreator<AesCmacPrfParameters> KEY_CREATOR = new MutableKeyCreationRegistry.KeyCreator() { // from class: com.google.crypto.tink.prf.AesCmacPrfKeyManager$$ExternalSyntheticLambda2
        @Override // com.google.crypto.tink.internal.MutableKeyCreationRegistry.KeyCreator
        public final Key createKey(Parameters parameters, Integer num) {
            return AesCmacPrfKeyManager.newKey((AesCmacPrfParameters) parameters, num);
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public static Prf createPrimitive(AesCmacPrfKey key) throws GeneralSecurityException {
        validate(key.getParameters());
        return PrfAesCmac.create(key);
    }

    private static void validate(AesCmacPrfParameters parameters) throws GeneralSecurityException {
        if (parameters.getKeySizeBytes() != 32) {
            throw new GeneralSecurityException("Key size must be 32 bytes");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static AesCmacPrfKey newKey(AesCmacPrfParameters parameters, @Nullable Integer idRequirement) throws GeneralSecurityException {
        if (idRequirement != null) {
            throw new GeneralSecurityException("Id Requirement is not supported for AES CMAC PRF keys");
        }
        validate(parameters);
        return AesCmacPrfKey.create(parameters, SecretBytes.randomBytes(parameters.getKeySizeBytes()));
    }

    static String getKeyType() {
        return "type.googleapis.com/google.crypto.tink.AesCmacPrfKey";
    }

    private static Map<String, Parameters> namedParameters() throws GeneralSecurityException {
        HashMap map = new HashMap();
        map.put("AES256_CMAC_PRF", PredefinedPrfParameters.AES_CMAC_PRF);
        map.put("AES_CMAC_PRF", PredefinedPrfParameters.AES_CMAC_PRF);
        return Collections.unmodifiableMap(map);
    }

    public static void register(boolean newKeyAllowed) throws GeneralSecurityException {
        if (!TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_NOT_FIPS.isCompatible()) {
            throw new GeneralSecurityException("Registering AES CMAC PRF is not supported in FIPS mode");
        }
        AesCmacPrfProtoSerialization.register();
        MutableKeyCreationRegistry.globalInstance().add(KEY_CREATOR, AesCmacPrfParameters.class);
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveConstructor(PRF_PRIMITIVE_CONSTRUCTOR);
        MutableParametersRegistry.globalInstance().putAll(namedParameters());
        KeyManagerRegistry.globalInstance().registerKeyManager(legacyKeyManager, newKeyAllowed);
    }

    public static final KeyTemplate aes256CmacTemplate() {
        return (KeyTemplate) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.prf.AesCmacPrfKeyManager$$ExternalSyntheticLambda0
            @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
            public final Object get() {
                return KeyTemplate.createFrom(AesCmacPrfParameters.create(32));
            }
        });
    }

    private AesCmacPrfKeyManager() {
    }
}
