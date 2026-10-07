package com.google.crypto.tink.internal;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.SecretKeyAccess;
import com.google.crypto.tink.internal.Serialization;
import com.google.crypto.tink.util.Bytes;
import java.security.GeneralSecurityException;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public abstract class KeyParser<SerializationT extends Serialization> {
    private final Bytes objectIdentifier;
    private final Class<SerializationT> serializationClass;

    public interface KeyParsingFunction<SerializationT extends Serialization> {
        Key parseKey(SerializationT serialization, @Nullable SecretKeyAccess access) throws GeneralSecurityException;
    }

    public abstract Key parseKey(SerializationT serialization, @Nullable SecretKeyAccess access) throws GeneralSecurityException;

    private KeyParser(Bytes objectIdentifier, Class<SerializationT> serializationClass) {
        this.objectIdentifier = objectIdentifier;
        this.serializationClass = serializationClass;
    }

    public final Bytes getObjectIdentifier() {
        return this.objectIdentifier;
    }

    public final Class<SerializationT> getSerializationClass() {
        return this.serializationClass;
    }

    public static <SerializationT extends Serialization> KeyParser<SerializationT> create(final KeyParsingFunction<SerializationT> keyParsingFunction, Bytes bytes, Class<SerializationT> cls) {
        return (KeyParser<SerializationT>) new KeyParser<SerializationT>(bytes, cls) { // from class: com.google.crypto.tink.internal.KeyParser.1
            @Override // com.google.crypto.tink.internal.KeyParser
            public Key parseKey(SerializationT serialization, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
                return keyParsingFunction.parseKey(serialization, access);
            }
        };
    }
}
