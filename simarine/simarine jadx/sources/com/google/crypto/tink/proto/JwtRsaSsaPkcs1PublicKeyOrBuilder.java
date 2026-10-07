package com.google.crypto.tink.proto;

import com.google.crypto.tink.shaded.protobuf.ByteString;
import com.google.crypto.tink.shaded.protobuf.MessageLiteOrBuilder;

/* JADX INFO: loaded from: classes2.dex */
public interface JwtRsaSsaPkcs1PublicKeyOrBuilder extends MessageLiteOrBuilder {
    JwtRsaSsaPkcs1Algorithm getAlgorithm();

    int getAlgorithmValue();

    JwtRsaSsaPkcs1PublicKey.CustomKid getCustomKid();

    ByteString getE();

    ByteString getN();

    int getVersion();

    boolean hasCustomKid();
}
