package com.google.crypto.tink;

import com.google.crypto.tink.internal.LegacyProtoParameters;
import com.google.crypto.tink.internal.MutableSerializationRegistry;
import com.google.crypto.tink.internal.ProtoParametersSerialization;
import com.google.crypto.tink.shaded.protobuf.ExtensionRegistryLite;
import java.io.IOException;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public final class TinkProtoParametersFormat {
    public static byte[] serialize(Parameters parameters) throws GeneralSecurityException {
        if (parameters instanceof LegacyProtoParameters) {
            return ((LegacyProtoParameters) parameters).getSerialization().getKeyTemplate().toByteArray();
        }
        return ((ProtoParametersSerialization) MutableSerializationRegistry.globalInstance().serializeParameters(parameters, ProtoParametersSerialization.class)).getKeyTemplate().toByteArray();
    }

    public static Parameters parse(byte[] serializedParameters) throws GeneralSecurityException {
        try {
            return MutableSerializationRegistry.globalInstance().parseParametersWithLegacyFallback(ProtoParametersSerialization.checkedCreate(com.google.crypto.tink.proto.KeyTemplate.parseFrom(serializedParameters, ExtensionRegistryLite.getEmptyRegistry())));
        } catch (IOException e) {
            throw new GeneralSecurityException("Failed to parse proto", e);
        }
    }

    private TinkProtoParametersFormat() {
    }
}
