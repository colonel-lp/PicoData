package com.google.crypto.tink;

import com.google.crypto.tink.hybrid.EciesPrivateKey;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.subtle.EciesAeadHkdfHybridDecrypt;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class ConfigurationV0$$ExternalSyntheticLambda5 implements PrimitiveConstructor.PrimitiveConstructionFunction {
    @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
    public final Object constructPrimitive(Key key) {
        return EciesAeadHkdfHybridDecrypt.create((EciesPrivateKey) key);
    }
}
