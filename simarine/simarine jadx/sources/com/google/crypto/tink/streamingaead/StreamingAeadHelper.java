package com.google.crypto.tink.streamingaead;

import com.google.crypto.tink.StreamingAead;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.GeneralSecurityException;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
final class StreamingAeadHelper implements StreamingAead {
    private final List<StreamingAead> allPrimitives;
    private final StreamingAead primary;

    public StreamingAeadHelper(List<StreamingAead> allPrimitives, StreamingAead primary) throws GeneralSecurityException {
        this.allPrimitives = allPrimitives;
        this.primary = primary;
    }

    @Override // com.google.crypto.tink.StreamingAead
    public WritableByteChannel newEncryptingChannel(WritableByteChannel ciphertextDestination, byte[] associatedData) throws GeneralSecurityException, IOException {
        return this.primary.newEncryptingChannel(ciphertextDestination, associatedData);
    }

    @Override // com.google.crypto.tink.StreamingAead
    public ReadableByteChannel newDecryptingChannel(ReadableByteChannel ciphertextChannel, byte[] associatedData) throws GeneralSecurityException, IOException {
        return new ReadableByteChannelDecrypter(this.allPrimitives, ciphertextChannel, associatedData);
    }

    @Override // com.google.crypto.tink.StreamingAead
    public SeekableByteChannel newSeekableDecryptingChannel(SeekableByteChannel ciphertextChannel, byte[] associatedData) throws GeneralSecurityException, IOException {
        return new SeekableByteChannelDecrypter(this.allPrimitives, ciphertextChannel, associatedData);
    }

    @Override // com.google.crypto.tink.StreamingAead
    public InputStream newDecryptingStream(InputStream ciphertextStream, byte[] associatedData) throws GeneralSecurityException, IOException {
        return new InputStreamDecrypter(this.allPrimitives, ciphertextStream, associatedData);
    }

    @Override // com.google.crypto.tink.StreamingAead
    public OutputStream newEncryptingStream(OutputStream ciphertext, byte[] associatedData) throws GeneralSecurityException, IOException {
        return this.primary.newEncryptingStream(ciphertext, associatedData);
    }
}
