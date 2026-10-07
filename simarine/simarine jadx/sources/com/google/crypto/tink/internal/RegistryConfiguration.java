package com.google.crypto.tink.internal;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.Registry;
import java.security.GeneralSecurityException;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class RegistryConfiguration extends InternalConfiguration {
    private static final RegistryConfiguration CONFIG = new RegistryConfiguration();

    public static RegistryConfiguration get() {
        return CONFIG;
    }

    private RegistryConfiguration() {
    }

    @Override // com.google.crypto.tink.internal.InternalConfiguration
    public <P> P getPrimitive(Key key, Class<P> cls) throws GeneralSecurityException {
        return (P) MutablePrimitiveRegistry.globalInstance().getPrimitive(key, cls);
    }

    @Override // com.google.crypto.tink.internal.InternalConfiguration
    public <B, P> P wrap(PrimitiveSet<B> primitiveSet, Class<P> cls) throws GeneralSecurityException {
        return (P) Registry.wrap(primitiveSet, cls);
    }

    @Override // com.google.crypto.tink.internal.InternalConfiguration
    @Nullable
    public Class<?> getInputPrimitiveClass(Class<?> wrapperClassObject) {
        return Registry.getInputPrimitive(wrapperClassObject);
    }

    public static InternalConfiguration createFromPrimitiveRegistry(PrimitiveRegistry registry) {
        throw new UnsupportedOperationException("Cannot create RegistryConfiguration from a PrimitiveRegistry");
    }
}
