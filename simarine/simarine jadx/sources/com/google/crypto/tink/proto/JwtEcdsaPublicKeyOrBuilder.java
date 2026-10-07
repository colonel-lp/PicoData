package com.google.crypto.tink.proto;

import com.google.crypto.tink.shaded.protobuf.ByteString;
import com.google.crypto.tink.shaded.protobuf.MessageLiteOrBuilder;

/* JADX INFO: loaded from: classes2.dex */
public interface JwtEcdsaPublicKeyOrBuilder extends MessageLiteOrBuilder {
    JwtEcdsaAlgorithm getAlgorithm();

    int getAlgorithmValue();

    JwtEcdsaPublicKey.CustomKid getCustomKid();

    int getVersion();

    ByteString getX();

    ByteString getY();

    boolean hasCustomKid();
}
