package com.google.crypto.tink.proto;

import com.google.crypto.tink.shaded.protobuf.ByteString;
import com.google.crypto.tink.shaded.protobuf.MessageLiteOrBuilder;

/* JADX INFO: loaded from: classes2.dex */
public interface JwtRsaSsaPssPublicKeyOrBuilder extends MessageLiteOrBuilder {
    JwtRsaSsaPssAlgorithm getAlgorithm();

    int getAlgorithmValue();

    JwtRsaSsaPssPublicKey.CustomKid getCustomKid();

    ByteString getE();

    ByteString getN();

    int getVersion();

    boolean hasCustomKid();
}
