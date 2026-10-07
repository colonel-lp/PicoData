package com.google.crypto.tink.subtle;

import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.subtle.EngineWrapper;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;

/* JADX INFO: loaded from: classes2.dex */
public final class EngineFactory<T_WRAPPER extends EngineWrapper<JcePrimitiveT>, JcePrimitiveT> {
    private final Policy<JcePrimitiveT> policy;
    public static final EngineFactory<EngineWrapper.TCipher, Cipher> CIPHER = new EngineFactory<>(new EngineWrapper.TCipher());
    public static final EngineFactory<EngineWrapper.TMac, Mac> MAC = new EngineFactory<>(new EngineWrapper.TMac());
    public static final EngineFactory<EngineWrapper.TSignature, Signature> SIGNATURE = new EngineFactory<>(new EngineWrapper.TSignature());
    public static final EngineFactory<EngineWrapper.TMessageDigest, MessageDigest> MESSAGE_DIGEST = new EngineFactory<>(new EngineWrapper.TMessageDigest());
    public static final EngineFactory<EngineWrapper.TKeyAgreement, KeyAgreement> KEY_AGREEMENT = new EngineFactory<>(new EngineWrapper.TKeyAgreement());
    public static final EngineFactory<EngineWrapper.TKeyPairGenerator, KeyPairGenerator> KEY_PAIR_GENERATOR = new EngineFactory<>(new EngineWrapper.TKeyPairGenerator());
    public static final EngineFactory<EngineWrapper.TKeyFactory, KeyFactory> KEY_FACTORY = new EngineFactory<>(new EngineWrapper.TKeyFactory());

    private interface Policy<JcePrimitiveT> {
        JcePrimitiveT getInstance(String algorithm) throws GeneralSecurityException;

        JcePrimitiveT getInstance(String algorithm, List<Provider> preferredProviders) throws GeneralSecurityException;
    }

    private static class DefaultPolicy<JcePrimitiveT> implements Policy<JcePrimitiveT> {
        private final EngineWrapper<JcePrimitiveT> jceFactory;

        private DefaultPolicy(EngineWrapper<JcePrimitiveT> jceFactory) {
            this.jceFactory = jceFactory;
        }

        @Override // com.google.crypto.tink.subtle.EngineFactory.Policy
        public JcePrimitiveT getInstance(String algorithm) throws GeneralSecurityException {
            return this.jceFactory.getInstance(algorithm, null);
        }

        @Override // com.google.crypto.tink.subtle.EngineFactory.Policy
        public JcePrimitiveT getInstance(String algorithm, List<Provider> preferredProviders) throws GeneralSecurityException {
            Iterator<Provider> it = preferredProviders.iterator();
            while (it.hasNext()) {
                try {
                    return this.jceFactory.getInstance(algorithm, it.next());
                } catch (Exception unused) {
                }
            }
            return this.getInstance(algorithm);
        }
    }

    private static class FipsPolicy<JcePrimitiveT> implements Policy<JcePrimitiveT> {
        private final EngineWrapper<JcePrimitiveT> jceFactory;

        private FipsPolicy(EngineWrapper<JcePrimitiveT> jceFactory) {
            this.jceFactory = jceFactory;
        }

        @Override // com.google.crypto.tink.subtle.EngineFactory.Policy
        public JcePrimitiveT getInstance(String algorithm) throws GeneralSecurityException {
            Iterator<Provider> it = EngineFactory.toProviderList("GmsCore_OpenSSL", "AndroidOpenSSL", "Conscrypt").iterator();
            Exception exc = null;
            while (it.hasNext()) {
                try {
                    return this.jceFactory.getInstance(algorithm, it.next());
                } catch (Exception e) {
                    if (exc == null) {
                        exc = e;
                    }
                }
            }
            throw new GeneralSecurityException("No good Provider found.", exc);
        }

        @Override // com.google.crypto.tink.subtle.EngineFactory.Policy
        public JcePrimitiveT getInstance(String algorithm, List<Provider> preferredProviders) throws GeneralSecurityException {
            return getInstance(algorithm);
        }
    }

    private static class AndroidPolicy<JcePrimitiveT> implements Policy<JcePrimitiveT> {
        private final EngineWrapper<JcePrimitiveT> jceFactory;

        private AndroidPolicy(EngineWrapper<JcePrimitiveT> jceFactory) {
            this.jceFactory = jceFactory;
        }

        @Override // com.google.crypto.tink.subtle.EngineFactory.Policy
        public JcePrimitiveT getInstance(String algorithm) throws GeneralSecurityException {
            Iterator<Provider> it = EngineFactory.toProviderList("GmsCore_OpenSSL", "AndroidOpenSSL").iterator();
            Exception exc = null;
            while (it.hasNext()) {
                try {
                    return this.jceFactory.getInstance(algorithm, it.next());
                } catch (Exception e) {
                    if (exc == null) {
                        exc = e;
                    }
                }
            }
            return this.jceFactory.getInstance(algorithm, null);
        }

        @Override // com.google.crypto.tink.subtle.EngineFactory.Policy
        public JcePrimitiveT getInstance(String algorithm, List<Provider> preferredProviders) throws GeneralSecurityException {
            return getInstance(algorithm);
        }
    }

    public static List<Provider> toProviderList(String... providerNames) {
        ArrayList arrayList = new ArrayList();
        for (String str : providerNames) {
            Provider provider = Security.getProvider(str);
            if (provider != null) {
                arrayList.add(provider);
            }
        }
        return arrayList;
    }

    public EngineFactory(T_WRAPPER instanceBuilder) {
        if (TinkFipsUtil.useOnlyFips()) {
            this.policy = new FipsPolicy(instanceBuilder);
        } else if (SubtleUtil.isAndroid()) {
            this.policy = new AndroidPolicy(instanceBuilder);
        } else {
            this.policy = new DefaultPolicy(instanceBuilder);
        }
    }

    public JcePrimitiveT getInstance(String algorithm) throws GeneralSecurityException {
        return this.policy.getInstance(algorithm);
    }

    JcePrimitiveT getInstance(String algorithm, List<Provider> preferredProviders) throws GeneralSecurityException {
        return this.policy.getInstance(algorithm, preferredProviders);
    }
}
