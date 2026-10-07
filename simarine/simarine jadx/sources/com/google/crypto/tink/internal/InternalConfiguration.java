package com.google.crypto.tink.internal;

import com.google.crypto.tink.Configuration;
import com.google.crypto.tink.Key;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public abstract class InternalConfiguration extends Configuration {
    public abstract Class<?> getInputPrimitiveClass(Class<?> wrapperClassObject) throws GeneralSecurityException;

    public abstract <P> P getPrimitive(Key key, Class<P> primitiveClass) throws GeneralSecurityException;

    public abstract <B, P> P wrap(PrimitiveSet<B> primitiveSet, Class<P> clazz) throws GeneralSecurityException;

    public static InternalConfiguration createFromPrimitiveRegistry(PrimitiveRegistry registry) {
        return new InternalConfigurationImpl(registry);
    }

    private static class InternalConfigurationImpl extends InternalConfiguration {
        private final PrimitiveRegistry registry;

        private InternalConfigurationImpl(PrimitiveRegistry registry) {
            this.registry = registry;
        }

        @Override // com.google.crypto.tink.internal.InternalConfiguration
        public <P> P getPrimitive(Key key, Class<P> cls) throws GeneralSecurityException {
            return (P) this.registry.getPrimitive(key, cls);
        }

        @Override // com.google.crypto.tink.internal.InternalConfiguration
        public Class<?> getInputPrimitiveClass(Class<?> wrapperClassObject) throws GeneralSecurityException {
            return this.registry.getInputPrimitiveClass(wrapperClassObject);
        }

        @Override // com.google.crypto.tink.internal.InternalConfiguration
        public <B, P> P wrap(PrimitiveSet<B> primitiveSet, Class<P> cls) throws GeneralSecurityException {
            return (P) this.registry.wrap(primitiveSet, cls);
        }
    }
}
