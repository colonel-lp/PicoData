package com.google.crypto.tink.prf.internal;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.Registry;
import com.google.crypto.tink.internal.LegacyProtoKey;
import com.google.crypto.tink.internal.ProtoKeySerialization;
import com.google.crypto.tink.prf.Prf;
import com.google.crypto.tink.proto.KeyData;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public class LegacyFullPrf implements Prf {
    private final Prf rawPrf;

    public static Prf create(LegacyProtoKey key) throws GeneralSecurityException {
        ProtoKeySerialization serialization = key.getSerialization(InsecureSecretKeyAccess.get());
        return new LegacyFullPrf((Prf) Registry.getPrimitive((KeyData) KeyData.newBuilder().setTypeUrl(serialization.getTypeUrl()).setValue(serialization.getValue()).setKeyMaterialType(serialization.getKeyMaterialType()).build(), Prf.class));
    }

    private LegacyFullPrf(Prf rawPrf) {
        this.rawPrf = rawPrf;
    }

    @Override // com.google.crypto.tink.prf.Prf
    public byte[] compute(byte[] input, int outputLength) throws GeneralSecurityException {
        return this.rawPrf.compute(input, outputLength);
    }
}
