package com.google.crypto.tink.proto;

import com.google.crypto.tink.shaded.protobuf.ByteString;
import com.google.crypto.tink.shaded.protobuf.MessageLiteOrBuilder;

/* JADX INFO: loaded from: classes2.dex */
public interface JwtHmacKeyOrBuilder extends MessageLiteOrBuilder {
    JwtHmacAlgorithm getAlgorithm();

    int getAlgorithmValue();

    JwtHmacKey.CustomKid getCustomKid();

    ByteString getKeyValue();

    int getVersion();

    boolean hasCustomKid();
}
