package com.google.crypto.tink;

import com.google.crypto.tink.internal.MonitoringAnnotations;
import com.google.crypto.tink.proto.Keyset;
import com.google.crypto.tink.shaded.protobuf.ExtensionRegistryLite;
import com.google.crypto.tink.shaded.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

/* JADX INFO: loaded from: classes2.dex */
public final class CleartextKeysetHandle {
    @Deprecated
    public static final KeysetHandle parseFrom(final byte[] serialized) throws GeneralSecurityException {
        try {
            return KeysetHandle.fromKeyset(Keyset.parseFrom(serialized, ExtensionRegistryLite.getEmptyRegistry()));
        } catch (InvalidProtocolBufferException unused) {
            throw new GeneralSecurityException("invalid keyset");
        }
    }

    public static KeysetHandle read(KeysetReader reader) throws GeneralSecurityException, IOException {
        return KeysetHandle.fromKeyset(reader.read());
    }

    @Deprecated
    public static KeysetHandle read(KeysetReader reader, Map<String, String> monitoringAnnotations) throws GeneralSecurityException, IOException {
        return KeysetHandle.fromKeysetAndAnnotations(reader.read(), MonitoringAnnotations.newBuilder().addAll(monitoringAnnotations).build());
    }

    @Deprecated
    public static Keyset getKeyset(KeysetHandle keysetHandle) {
        return keysetHandle.getKeyset();
    }

    @Deprecated
    public static KeysetHandle fromKeyset(Keyset keyset) throws GeneralSecurityException {
        return KeysetHandle.fromKeyset(keyset);
    }

    public static void write(KeysetHandle handle, KeysetWriter keysetWriter) throws IOException {
        keysetWriter.write(handle.getKeyset());
    }

    private CleartextKeysetHandle() {
    }
}
