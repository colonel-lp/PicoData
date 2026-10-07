package com.google.crypto.tink.internal;

import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public interface PrimitiveWrapper<B, P> {
    Class<B> getInputPrimitiveClass();

    Class<P> getPrimitiveClass();

    P wrap(PrimitiveSet<B> primitiveSet) throws GeneralSecurityException;
}
