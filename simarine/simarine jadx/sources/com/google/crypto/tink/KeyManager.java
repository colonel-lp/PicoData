package com.google.crypto.tink;

import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.shaded.protobuf.ByteString;
import com.google.crypto.tink.shaded.protobuf.MessageLite;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public interface KeyManager<P> {
    String getKeyType();

    P getPrimitive(ByteString serializedKey) throws GeneralSecurityException;

    Class<P> getPrimitiveClass();

    KeyData newKeyData(ByteString serializedKeyFormat) throws GeneralSecurityException;

    @Deprecated
    default P getPrimitive(MessageLite key) throws GeneralSecurityException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    default MessageLite newKey(ByteString serializedKeyFormat) throws GeneralSecurityException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    default MessageLite newKey(MessageLite keyFormat) throws GeneralSecurityException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    default boolean doesSupport(String typeUrl) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    default int getVersion() {
        throw new UnsupportedOperationException();
    }
}
