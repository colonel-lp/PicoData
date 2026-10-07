package com.google.crypto.tink.internal;

import com.google.crypto.tink.Key;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public abstract class PrimitiveConstructor<KeyT extends Key, PrimitiveT> {
    private final Class<KeyT> keyClass;
    private final Class<PrimitiveT> primitiveClass;

    public interface PrimitiveConstructionFunction<KeyT extends Key, PrimitiveT> {
        PrimitiveT constructPrimitive(KeyT key) throws GeneralSecurityException;
    }

    public abstract PrimitiveT constructPrimitive(KeyT key) throws GeneralSecurityException;

    private PrimitiveConstructor(Class<KeyT> keyClass, Class<PrimitiveT> primitiveClass) {
        this.keyClass = keyClass;
        this.primitiveClass = primitiveClass;
    }

    public Class<KeyT> getKeyClass() {
        return this.keyClass;
    }

    public Class<PrimitiveT> getPrimitiveClass() {
        return this.primitiveClass;
    }

    public static <KeyT extends Key, PrimitiveT> PrimitiveConstructor<KeyT, PrimitiveT> create(final PrimitiveConstructionFunction<KeyT, PrimitiveT> primitiveConstructionFunction, Class<KeyT> cls, Class<PrimitiveT> cls2) {
        return (PrimitiveConstructor<KeyT, PrimitiveT>) new PrimitiveConstructor<KeyT, PrimitiveT>(cls, cls2) { // from class: com.google.crypto.tink.internal.PrimitiveConstructor.1
            @Override // com.google.crypto.tink.internal.PrimitiveConstructor
            public PrimitiveT constructPrimitive(KeyT keyt) throws GeneralSecurityException {
                return (PrimitiveT) primitiveConstructionFunction.constructPrimitive(keyt);
            }
        };
    }
}
