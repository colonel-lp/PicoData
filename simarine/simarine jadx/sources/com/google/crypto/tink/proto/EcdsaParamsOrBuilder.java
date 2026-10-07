package com.google.crypto.tink.proto;

import com.google.crypto.tink.shaded.protobuf.MessageLiteOrBuilder;

/* JADX INFO: loaded from: classes2.dex */
public interface EcdsaParamsOrBuilder extends MessageLiteOrBuilder {
    EllipticCurveType getCurve();

    int getCurveValue();

    EcdsaSignatureEncoding getEncoding();

    int getEncodingValue();

    HashType getHashType();

    int getHashTypeValue();
}
