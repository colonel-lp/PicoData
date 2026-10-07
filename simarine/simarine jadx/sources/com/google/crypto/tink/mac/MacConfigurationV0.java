package com.google.crypto.tink.mac;

import com.google.crypto.tink.Configuration;
import com.google.crypto.tink.ConfigurationFips140v2$$ExternalSyntheticLambda0;
import com.google.crypto.tink.ConfigurationFips140v2$$ExternalSyntheticLambda2;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.Mac;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.InternalConfiguration;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.internal.PrimitiveRegistry;
import com.google.crypto.tink.mac.internal.ChunkedAesCmacImpl;
import com.google.crypto.tink.subtle.PrfMac;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
class MacConfigurationV0 {
    private static final int AES_CMAC_KEY_SIZE_BYTES = 32;
    private static final InternalConfiguration INTERNAL_CONFIGURATION = create();

    private MacConfigurationV0() {
    }

    private static InternalConfiguration create() {
        try {
            PrimitiveRegistry.Builder builder = PrimitiveRegistry.builder();
            MacWrapper.registerToInternalPrimitiveRegistry(builder);
            ChunkedMacWrapper.registerToInternalPrimitiveRegistry(builder);
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.mac.MacConfigurationV0$$ExternalSyntheticLambda0
                @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
                public final Object constructPrimitive(Key key) {
                    return MacConfigurationV0.createAesCmac((AesCmacKey) key);
                }
            }, AesCmacKey.class, Mac.class));
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationFips140v2$$ExternalSyntheticLambda0(), HmacKey.class, Mac.class));
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.mac.MacConfigurationV0$$ExternalSyntheticLambda1
                @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
                public final Object constructPrimitive(Key key) {
                    return MacConfigurationV0.createChunkedAesCmac((AesCmacKey) key);
                }
            }, AesCmacKey.class, ChunkedMac.class));
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationFips140v2$$ExternalSyntheticLambda2(), HmacKey.class, ChunkedMac.class));
            return InternalConfiguration.createFromPrimitiveRegistry(builder.build());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Configuration get() throws GeneralSecurityException {
        if (TinkFipsUtil.useOnlyFips()) {
            throw new GeneralSecurityException("Cannot use non-FIPS-compliant MacConfigurationV0 in FIPS mode");
        }
        return INTERNAL_CONFIGURATION;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ChunkedMac createChunkedAesCmac(AesCmacKey key) throws GeneralSecurityException {
        if (key.getParameters().getKeySizeBytes() != 32) {
            throw new GeneralSecurityException("AesCmac key size is not 32 bytes");
        }
        return new ChunkedAesCmacImpl(key);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Mac createAesCmac(AesCmacKey key) throws GeneralSecurityException {
        if (key.getParameters().getKeySizeBytes() != 32) {
            throw new GeneralSecurityException("AesCmac key size is not 32 bytes");
        }
        return PrfMac.create(key);
    }
}
