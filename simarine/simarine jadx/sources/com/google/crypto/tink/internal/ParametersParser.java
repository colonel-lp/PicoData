package com.google.crypto.tink.internal;

import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.internal.Serialization;
import com.google.crypto.tink.util.Bytes;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public abstract class ParametersParser<SerializationT extends Serialization> {
    private final Bytes objectIdentifier;
    private final Class<SerializationT> serializationClass;

    public interface ParametersParsingFunction<SerializationT extends Serialization> {
        Parameters parseParameters(SerializationT serialization) throws GeneralSecurityException;
    }

    public abstract Parameters parseParameters(SerializationT serialization) throws GeneralSecurityException;

    private ParametersParser(Bytes objectIdentifier, Class<SerializationT> serializationClass) {
        this.objectIdentifier = objectIdentifier;
        this.serializationClass = serializationClass;
    }

    public final Bytes getObjectIdentifier() {
        return this.objectIdentifier;
    }

    public final Class<SerializationT> getSerializationClass() {
        return this.serializationClass;
    }

    public static <SerializationT extends Serialization> ParametersParser<SerializationT> create(final ParametersParsingFunction<SerializationT> parametersParsingFunction, Bytes bytes, Class<SerializationT> cls) {
        return (ParametersParser<SerializationT>) new ParametersParser<SerializationT>(bytes, cls) { // from class: com.google.crypto.tink.internal.ParametersParser.1
            @Override // com.google.crypto.tink.internal.ParametersParser
            public Parameters parseParameters(SerializationT serialization) throws GeneralSecurityException {
                return parametersParsingFunction.parseParameters(serialization);
            }
        };
    }
}
