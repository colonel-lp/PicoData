package com.google.crypto.tink.internal;

import com.google.crypto.tink.proto.KeyTemplate;
import com.google.crypto.tink.proto.OutputPrefixType;
import com.google.crypto.tink.shaded.protobuf.MessageLite;
import com.google.crypto.tink.util.Bytes;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class ProtoParametersSerialization implements Serialization {
    private final KeyTemplate keyTemplate;
    private final Bytes objectIdentifier;

    private ProtoParametersSerialization(KeyTemplate keyTemplate, Bytes objectIdentifier) {
        this.keyTemplate = keyTemplate;
        this.objectIdentifier = objectIdentifier;
    }

    public static ProtoParametersSerialization create(String typeUrl, OutputPrefixType outputPrefixType, MessageLite value) {
        return create((KeyTemplate) KeyTemplate.newBuilder().setTypeUrl(typeUrl).setOutputPrefixType(outputPrefixType).setValue(value.toByteString()).build());
    }

    public static ProtoParametersSerialization create(KeyTemplate keyTemplate) {
        return new ProtoParametersSerialization(keyTemplate, Util.toBytesFromPrintableAscii(keyTemplate.getTypeUrl()));
    }

    public static ProtoParametersSerialization checkedCreate(KeyTemplate keyTemplate) throws GeneralSecurityException {
        return new ProtoParametersSerialization(keyTemplate, Util.checkedToBytesFromPrintableAscii(keyTemplate.getTypeUrl()));
    }

    public KeyTemplate getKeyTemplate() {
        return this.keyTemplate;
    }

    @Override // com.google.crypto.tink.internal.Serialization
    public Bytes getObjectIdentifier() {
        return this.objectIdentifier;
    }
}
