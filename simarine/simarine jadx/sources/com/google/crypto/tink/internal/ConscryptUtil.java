package com.google.crypto.tink.internal;

import java.lang.reflect.InvocationTargetException;
import java.security.Provider;
import java.security.Security;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class ConscryptUtil {
    private static final String[] CONSCRYPT_PROVIDER_NAMES = {"GmsCore_OpenSSL", "AndroidOpenSSL", "Conscrypt"};

    @Nullable
    public static Provider providerOrNull() {
        for (String str : CONSCRYPT_PROVIDER_NAMES) {
            Provider provider = Security.getProvider(str);
            if (provider != null) {
                return provider;
            }
        }
        return null;
    }

    @Nullable
    public static Provider providerWithReflectionOrNull() {
        try {
            return (Provider) Class.forName("org.conscrypt.Conscrypt").getMethod("newProvider", null).invoke(null, null);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException unused) {
            return null;
        }
    }

    public static final boolean isConscryptProvider(Provider provider) {
        String name = provider.getName();
        return name.equals("GmsCore_OpenSSL") || name.equals("AndroidOpenSSL") || name.equals("Conscrypt");
    }

    private ConscryptUtil() {
    }
}
