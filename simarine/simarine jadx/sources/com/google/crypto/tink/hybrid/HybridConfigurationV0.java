package com.google.crypto.tink.hybrid;

import com.google.crypto.tink.Configuration;
import com.google.crypto.tink.ConfigurationV0$$ExternalSyntheticLambda4;
import com.google.crypto.tink.ConfigurationV0$$ExternalSyntheticLambda5;
import com.google.crypto.tink.ConfigurationV0$$ExternalSyntheticLambda6;
import com.google.crypto.tink.ConfigurationV0$$ExternalSyntheticLambda7;
import com.google.crypto.tink.HybridDecrypt;
import com.google.crypto.tink.HybridEncrypt;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.InternalConfiguration;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.internal.PrimitiveRegistry;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
class HybridConfigurationV0 {
    private static final InternalConfiguration INTERNAL_CONFIGURATION = create();

    private HybridConfigurationV0() {
    }

    private static InternalConfiguration create() {
        try {
            PrimitiveRegistry.Builder builder = PrimitiveRegistry.builder();
            HybridEncryptWrapper.registerToInternalPrimitiveRegistry(builder);
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationV0$$ExternalSyntheticLambda4(), EciesPublicKey.class, HybridEncrypt.class));
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationV0$$ExternalSyntheticLambda6(), HpkePublicKey.class, HybridEncrypt.class));
            HybridDecryptWrapper.registerToInternalPrimitiveRegistry(builder);
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationV0$$ExternalSyntheticLambda5(), EciesPrivateKey.class, HybridDecrypt.class));
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationV0$$ExternalSyntheticLambda7(), HpkePrivateKey.class, HybridDecrypt.class));
            return InternalConfiguration.createFromPrimitiveRegistry(builder.build());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Configuration get() throws GeneralSecurityException {
        if (TinkFipsUtil.useOnlyFips()) {
            throw new GeneralSecurityException("Cannot use non-FIPS-compliant HybridConfigurationV0 in FIPS mode");
        }
        return INTERNAL_CONFIGURATION;
    }
}
