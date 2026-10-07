package com.google.crypto.tink;

import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.prf.HmacPrfKey;
import com.google.crypto.tink.subtle.PrfHmacJce;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class ConfigurationFips140v2$$ExternalSyntheticLambda5 implements PrimitiveConstructor.PrimitiveConstructionFunction {
    @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
    public final Object constructPrimitive(Key key) {
        return PrfHmacJce.create((HmacPrfKey) key);
    }
}
