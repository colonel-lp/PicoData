package com.google.crypto.tink.streamingaead.internal;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.Registry;
import com.google.crypto.tink.StreamingAead;
import com.google.crypto.tink.internal.LegacyProtoKey;
import com.google.crypto.tink.internal.ProtoKeySerialization;
import com.google.crypto.tink.proto.KeyData;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public class LegacyFullStreamingAead implements StreamingAead {
    private final StreamingAead rawStreamingAead;

    public static StreamingAead create(LegacyProtoKey key) throws GeneralSecurityException {
        ProtoKeySerialization serialization = key.getSerialization(InsecureSecretKeyAccess.get());
        return new LegacyFullStreamingAead((StreamingAead) Registry.getPrimitive((KeyData) KeyData.newBuilder().setTypeUrl(serialization.getTypeUrl()).setValue(serialization.getValue()).setKeyMaterialType(serialization.getKeyMaterialType()).build(), StreamingAead.class));
    }

    private LegacyFullStreamingAead(StreamingAead rawStreamingAead) {
        this.rawStreamingAead = rawStreamingAead;
    }

    @Override // com.google.crypto.tink.StreamingAead
    public WritableByteChannel newEncryptingChannel(WritableByteChannel ciphertextDestination, byte[] associatedData) throws GeneralSecurityException, IOException {
        return this.rawStreamingAead.newEncryptingChannel(ciphertextDestination, associatedData);
    }

    @Override // com.google.crypto.tink.StreamingAead
    public SeekableByteChannel newSeekableDecryptingChannel(SeekableByteChannel ciphertextSource, byte[] associatedData) throws GeneralSecurityException, IOException {
        return this.rawStreamingAead.newSeekableDecryptingChannel(ciphertextSource, associatedData);
    }

    @Override // com.google.crypto.tink.StreamingAead
    public ReadableByteChannel newDecryptingChannel(ReadableByteChannel ciphertextSource, byte[] associatedData) throws GeneralSecurityException, IOException {
        return this.rawStreamingAead.newDecryptingChannel(ciphertextSource, associatedData);
    }

    @Override // com.google.crypto.tink.StreamingAead
    public OutputStream newEncryptingStream(OutputStream ciphertextDestination, byte[] associatedData) throws GeneralSecurityException, IOException {
        return this.rawStreamingAead.newEncryptingStream(ciphertextDestination, associatedData);
    }

    @Override // com.google.crypto.tink.StreamingAead
    public InputStream newDecryptingStream(InputStream ciphertextSource, byte[] associatedData) throws GeneralSecurityException, IOException {
        return this.rawStreamingAead.newDecryptingStream(ciphertextSource, associatedData);
    }
}
