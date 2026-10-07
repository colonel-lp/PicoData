package com.google.crypto.tink;

import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.mac.HmacKey;
import com.google.crypto.tink.subtle.PrfMac;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class ConfigurationFips140v2$$ExternalSyntheticLambda0 implements PrimitiveConstructor.PrimitiveConstructionFunction {
    @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
    public final Object constructPrimitive(Key key) {
        return PrfMac.create((HmacKey) key);
    }
}
