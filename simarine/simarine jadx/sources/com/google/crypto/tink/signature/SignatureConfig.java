package com.google.crypto.tink.signature;

import com.google.crypto.tink.config.TinkFips;
import com.google.crypto.tink.proto.RegistryConfig;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public final class SignatureConfig {
    public static final String ECDSA_PUBLIC_KEY_TYPE_URL = initializeClassReturnInput("type.googleapis.com/google.crypto.tink.EcdsaPublicKey");
    public static final String ECDSA_PRIVATE_KEY_TYPE_URL = initializeClassReturnInput("type.googleapis.com/google.crypto.tink.EcdsaPrivateKey");
    public static final String ED25519_PUBLIC_KEY_TYPE_URL = initializeClassReturnInput("type.googleapis.com/google.crypto.tink.Ed25519PublicKey");
    public static final String ED25519_PRIVATE_KEY_TYPE_URL = initializeClassReturnInput("type.googleapis.com/google.crypto.tink.Ed25519PrivateKey");
    public static final String RSA_PKCS1_PRIVATE_KEY_TYPE_URL = initializeClassReturnInput("type.googleapis.com/google.crypto.tink.RsaSsaPkcs1PrivateKey");
    public static final String RSA_PKCS1_PUBLIC_KEY_TYPE_URL = initializeClassReturnInput("type.googleapis.com/google.crypto.tink.RsaSsaPkcs1PublicKey");
    public static final String RSA_PSS_PRIVATE_KEY_TYPE_URL = initializeClassReturnInput("type.googleapis.com/google.crypto.tink.RsaSsaPssPrivateKey");
    public static final String RSA_PSS_PUBLIC_KEY_TYPE_URL = initializeClassReturnInput("type.googleapis.com/google.crypto.tink.RsaSsaPssPublicKey");

    @Deprecated
    public static final RegistryConfig TINK_1_0_0 = RegistryConfig.getDefaultInstance();

    @Deprecated
    public static final RegistryConfig TINK_1_1_0 = RegistryConfig.getDefaultInstance();

    @Deprecated
    public static final RegistryConfig LATEST = RegistryConfig.getDefaultInstance();

    private static String initializeClassReturnInput(String s) {
        return s;
    }

    static {
        try {
            init();
        } catch (GeneralSecurityException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Deprecated
    public static void init() throws GeneralSecurityException {
        register();
    }

    public static void register() throws GeneralSecurityException {
        PublicKeySignWrapper.register();
        PublicKeyVerifyWrapper.register();
        EcdsaSignKeyManager.registerPair(true);
        RsaSsaPkcs1SignKeyManager.registerPair(true);
        RsaSsaPssSignKeyManager.registerPair(true);
        if (TinkFips.useOnlyFips()) {
            return;
        }
        Ed25519PrivateKeyManager.registerPair(true);
    }

    private SignatureConfig() {
    }
}
