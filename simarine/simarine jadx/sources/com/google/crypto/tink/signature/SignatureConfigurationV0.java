package com.google.crypto.tink.signature;

import com.google.crypto.tink.Configuration;
import com.google.crypto.tink.ConfigurationFips140v2$$ExternalSyntheticLambda6;
import com.google.crypto.tink.ConfigurationFips140v2$$ExternalSyntheticLambda7;
import com.google.crypto.tink.ConfigurationV0$$ExternalSyntheticLambda10;
import com.google.crypto.tink.ConfigurationV0$$ExternalSyntheticLambda11;
import com.google.crypto.tink.ConfigurationV0$$ExternalSyntheticLambda12;
import com.google.crypto.tink.ConfigurationV0$$ExternalSyntheticLambda13;
import com.google.crypto.tink.ConfigurationV0$$ExternalSyntheticLambda14;
import com.google.crypto.tink.ConfigurationV0$$ExternalSyntheticLambda15;
import com.google.crypto.tink.PublicKeySign;
import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.InternalConfiguration;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.internal.PrimitiveRegistry;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
class SignatureConfigurationV0 {
    private static final InternalConfiguration INTERNAL_CONFIGURATION = create();

    private SignatureConfigurationV0() {
    }

    private static InternalConfiguration create() {
        try {
            PrimitiveRegistry.Builder builder = PrimitiveRegistry.builder();
            PublicKeySignWrapper.registerToInternalPrimitiveRegistry(builder);
            PublicKeyVerifyWrapper.registerToInternalPrimitiveRegistry(builder);
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationFips140v2$$ExternalSyntheticLambda6(), EcdsaPrivateKey.class, PublicKeySign.class));
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationFips140v2$$ExternalSyntheticLambda7(), EcdsaPublicKey.class, PublicKeyVerify.class));
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationV0$$ExternalSyntheticLambda14(), RsaSsaPssPrivateKey.class, PublicKeySign.class));
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationV0$$ExternalSyntheticLambda15(), RsaSsaPssPublicKey.class, PublicKeyVerify.class));
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationV0$$ExternalSyntheticLambda12(), RsaSsaPkcs1PrivateKey.class, PublicKeySign.class));
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationV0$$ExternalSyntheticLambda13(), RsaSsaPkcs1PublicKey.class, PublicKeyVerify.class));
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationV0$$ExternalSyntheticLambda10(), Ed25519PrivateKey.class, PublicKeySign.class));
            builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationV0$$ExternalSyntheticLambda11(), Ed25519PublicKey.class, PublicKeyVerify.class));
            return InternalConfiguration.createFromPrimitiveRegistry(builder.build());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Configuration get() throws GeneralSecurityException {
        if (TinkFipsUtil.useOnlyFips()) {
            throw new GeneralSecurityException("Cannot use non-FIPS-compliant SignatureConfigurationV0 in FIPS mode");
        }
        return INTERNAL_CONFIGURATION;
    }
}
