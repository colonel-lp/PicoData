package com.google.crypto.tink.internal;

import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.SecureRandom;

/* JADX INFO: loaded from: classes2.dex */
public final class Random {
    private static final ThreadLocal<SecureRandom> localRandom = new ThreadLocal<SecureRandom>() { // from class: com.google.crypto.tink.internal.Random.1
        /* JADX INFO: Access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public SecureRandom initialValue() {
            return Random.newDefaultSecureRandom();
        }
    };

    private static SecureRandom create() {
        Provider providerProviderOrNull = ConscryptUtil.providerOrNull();
        if (providerProviderOrNull != null) {
            try {
                return SecureRandom.getInstance("SHA1PRNG", providerProviderOrNull);
            } catch (GeneralSecurityException unused) {
            }
        }
        Provider providerProviderWithReflectionOrNull = ConscryptUtil.providerWithReflectionOrNull();
        if (providerProviderWithReflectionOrNull != null) {
            try {
                return SecureRandom.getInstance("SHA1PRNG", providerProviderWithReflectionOrNull);
            } catch (GeneralSecurityException unused2) {
            }
        }
        return new SecureRandom();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static SecureRandom newDefaultSecureRandom() {
        SecureRandom secureRandomCreate = create();
        secureRandomCreate.nextLong();
        return secureRandomCreate;
    }

    public static byte[] randBytes(int size) {
        byte[] bArr = new byte[size];
        localRandom.get().nextBytes(bArr);
        return bArr;
    }

    public static final int randInt(int max) {
        return localRandom.get().nextInt(max);
    }

    public static final int randInt() {
        return localRandom.get().nextInt();
    }

    public static final void validateUsesConscrypt() throws GeneralSecurityException {
        ThreadLocal<SecureRandom> threadLocal = localRandom;
        if (ConscryptUtil.isConscryptProvider(threadLocal.get().getProvider())) {
            return;
        }
        throw new GeneralSecurityException("Requires GmsCore_OpenSSL, AndroidOpenSSL or Conscrypt to generate randomness, but got " + threadLocal.get().getProvider().getName());
    }

    private Random() {
    }
}
