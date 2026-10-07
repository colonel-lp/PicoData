package com.google.crypto.tink.prf;

import com.google.crypto.tink.Configuration;
import com.google.crypto.tink.ConfigurationFips140v2$$ExternalSyntheticLambda5;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.InternalConfiguration;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.internal.PrimitiveRegistry;
import com.google.crypto.tink.subtle.PrfAesCmac;
import com.google.crypto.tink.subtle.prf.HkdfStreamingPrf;
import com.google.crypto.tink.subtle.prf.PrfImpl;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
class PrfConfigurationV0 {
    private static final InternalConfiguration INTERNAL_CONFIGURATION = create();
    private static final int MIN_HKDF_PRF_KEY_SIZE = 32;

    private PrfConfigurationV0() {
    }

    private static InternalConfiguration create() {
        try {
            PrimitiveRegistry.Builder builder = PrimitiveRegistry.builder();
            PrfSetWrapper.registerToInternalPrimitiveRegistry(builder);
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationFips140v2$$ExternalSyntheticLambda5(), HmacPrfKey.class, Prf.class));
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.prf.PrfConfigurationV0$$ExternalSyntheticLambda0
                @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
                public final Object constructPrimitive(Key key) {
                    return PrfConfigurationV0.createHkdfPrf((HkdfPrfKey) key);
                }
            }, HkdfPrfKey.class, Prf.class));
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.prf.PrfConfigurationV0$$ExternalSyntheticLambda1
                @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
                public final Object constructPrimitive(Key key) {
                    return PrfConfigurationV0.createAesCmacPrf((AesCmacPrfKey) key);
                }
            }, AesCmacPrfKey.class, Prf.class));
            return InternalConfiguration.createFromPrimitiveRegistry(builder.build());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Configuration get() throws GeneralSecurityException {
        if (TinkFipsUtil.useOnlyFips()) {
            throw new GeneralSecurityException("Cannot use non-FIPS-compliant PrfConfigurationV0 in FIPS mode");
        }
        return INTERNAL_CONFIGURATION;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Prf createHkdfPrf(HkdfPrfKey key) throws GeneralSecurityException {
        if (key.getParameters().getKeySizeBytes() < 32) {
            throw new GeneralSecurityException("HkdfPrf key size must be at least 32");
        }
        if (key.getParameters().getHashType() != HkdfPrfParameters.HashType.SHA256 && key.getParameters().getHashType() != HkdfPrfParameters.HashType.SHA512) {
            throw new GeneralSecurityException("HkdfPrf hash type must be SHA256 or SHA512");
        }
        return PrfImpl.wrap(HkdfStreamingPrf.create(key));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Prf createAesCmacPrf(AesCmacPrfKey key) throws GeneralSecurityException {
        if (key.getParameters().getKeySizeBytes() != 32) {
            throw new GeneralSecurityException("AesCmacPrf key size must be 32 bytes");
        }
        return PrfAesCmac.create(key);
    }
}
