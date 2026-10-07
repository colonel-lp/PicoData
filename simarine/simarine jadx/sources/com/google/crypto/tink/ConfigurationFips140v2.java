package com.google.crypto.tink;

import com.google.crypto.tink.aead.AeadWrapper;
import com.google.crypto.tink.aead.AesCtrHmacAeadKey;
import com.google.crypto.tink.aead.AesGcmKey;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.InternalConfiguration;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.internal.PrimitiveRegistry;
import com.google.crypto.tink.internal.Random;
import com.google.crypto.tink.mac.ChunkedMac;
import com.google.crypto.tink.mac.ChunkedMacWrapper;
import com.google.crypto.tink.mac.HmacKey;
import com.google.crypto.tink.mac.MacWrapper;
import com.google.crypto.tink.prf.HmacPrfKey;
import com.google.crypto.tink.prf.Prf;
import com.google.crypto.tink.prf.PrfSetWrapper;
import com.google.crypto.tink.signature.EcdsaPrivateKey;
import com.google.crypto.tink.signature.EcdsaPublicKey;
import com.google.crypto.tink.signature.PublicKeySignWrapper;
import com.google.crypto.tink.signature.PublicKeyVerifyWrapper;
import com.google.crypto.tink.signature.RsaSsaPkcs1PrivateKey;
import com.google.crypto.tink.signature.RsaSsaPkcs1PublicKey;
import com.google.crypto.tink.signature.RsaSsaPssPrivateKey;
import com.google.crypto.tink.signature.RsaSsaPssPublicKey;
import com.google.crypto.tink.signature.internal.RsaSsaPkcs1VerifyConscrypt;
import com.google.crypto.tink.signature.internal.RsaSsaPssSignConscrypt;
import com.google.crypto.tink.signature.internal.RsaSsaPssVerifyConscrypt;
import com.google.crypto.tink.subtle.RsaSsaPkcs1SignJce;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public class ConfigurationFips140v2 {
    private ConfigurationFips140v2() {
    }

    public static Configuration get() throws GeneralSecurityException {
        if (!TinkFipsUtil.fipsModuleAvailable()) {
            throw new GeneralSecurityException("Conscrypt is not available or does not support checking for FIPS build.");
        }
        Random.validateUsesConscrypt();
        PrimitiveRegistry.Builder builder = PrimitiveRegistry.builder();
        MacWrapper.registerToInternalPrimitiveRegistry(builder);
        ChunkedMacWrapper.registerToInternalPrimitiveRegistry(builder);
        builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationFips140v2$$ExternalSyntheticLambda0(), HmacKey.class, Mac.class));
        builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationFips140v2$$ExternalSyntheticLambda2(), HmacKey.class, ChunkedMac.class));
        AeadWrapper.registerToInternalPrimitiveRegistry(builder);
        builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationFips140v2$$ExternalSyntheticLambda3(), AesCtrHmacAeadKey.class, Aead.class));
        builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationFips140v2$$ExternalSyntheticLambda4(), AesGcmKey.class, Aead.class));
        PrfSetWrapper.registerToInternalPrimitiveRegistry(builder);
        builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationFips140v2$$ExternalSyntheticLambda5(), HmacPrfKey.class, Prf.class));
        PublicKeySignWrapper.registerToInternalPrimitiveRegistry(builder);
        PublicKeyVerifyWrapper.registerToInternalPrimitiveRegistry(builder);
        builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationFips140v2$$ExternalSyntheticLambda6(), EcdsaPrivateKey.class, PublicKeySign.class));
        builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new ConfigurationFips140v2$$ExternalSyntheticLambda7(), EcdsaPublicKey.class, PublicKeyVerify.class));
        builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.ConfigurationFips140v2$$ExternalSyntheticLambda8
            @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
            public final Object constructPrimitive(Key key) {
                return ConfigurationFips140v2.rsaSsaPkcs1SignCreate((RsaSsaPkcs1PrivateKey) key);
            }
        }, RsaSsaPkcs1PrivateKey.class, PublicKeySign.class));
        builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.ConfigurationFips140v2$$ExternalSyntheticLambda9
            @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
            public final Object constructPrimitive(Key key) {
                return ConfigurationFips140v2.rsaSsaPkcs1VerifyCreate((RsaSsaPkcs1PublicKey) key);
            }
        }, RsaSsaPkcs1PublicKey.class, PublicKeyVerify.class));
        builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.ConfigurationFips140v2$$ExternalSyntheticLambda10
            @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
            public final Object constructPrimitive(Key key) {
                return ConfigurationFips140v2.rsaSsaPssSignCreate((RsaSsaPssPrivateKey) key);
            }
        }, RsaSsaPssPrivateKey.class, PublicKeySign.class));
        builder.registerPrimitiveConstructor(PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.ConfigurationFips140v2$$ExternalSyntheticLambda1
            @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
            public final Object constructPrimitive(Key key) {
                return ConfigurationFips140v2.rsaSsaPssVerifyCreate((RsaSsaPssPublicKey) key);
            }
        }, RsaSsaPssPublicKey.class, PublicKeyVerify.class));
        return InternalConfiguration.createFromPrimitiveRegistry(builder.build());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static PublicKeySign rsaSsaPkcs1SignCreate(RsaSsaPkcs1PrivateKey key) throws GeneralSecurityException {
        if (key.getParameters().getModulusSizeBits() != 2048 && key.getParameters().getModulusSizeBits() != 3072) {
            throw new GeneralSecurityException("Cannot create FIPS-compliant PublicKeySign: wrong RsaSsaPkcs1 key modulus size");
        }
        return RsaSsaPkcs1SignJce.create(key);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static PublicKeyVerify rsaSsaPkcs1VerifyCreate(RsaSsaPkcs1PublicKey key) throws GeneralSecurityException {
        if (key.getParameters().getModulusSizeBits() != 2048 && key.getParameters().getModulusSizeBits() != 3072) {
            throw new GeneralSecurityException("Cannot create FIPS-compliant PublicKeyVerify: wrong RsaSsaPkcs1 key modulus size");
        }
        return RsaSsaPkcs1VerifyConscrypt.create(key);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static PublicKeySign rsaSsaPssSignCreate(RsaSsaPssPrivateKey key) throws GeneralSecurityException {
        if (key.getParameters().getModulusSizeBits() != 2048 && key.getParameters().getModulusSizeBits() != 3072) {
            throw new GeneralSecurityException("Cannot create FIPS-compliant PublicKeySign: wrong RsaSsaPss key modulus size");
        }
        return RsaSsaPssSignConscrypt.create(key);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static PublicKeyVerify rsaSsaPssVerifyCreate(RsaSsaPssPublicKey key) throws GeneralSecurityException {
        if (key.getParameters().getModulusSizeBits() != 2048 && key.getParameters().getModulusSizeBits() != 3072) {
            throw new GeneralSecurityException("Cannot create FIPS-compliant PublicKeyVerify: wrong RsaSsaPss key modulus size");
        }
        return RsaSsaPssVerifyConscrypt.create(key);
    }
}
