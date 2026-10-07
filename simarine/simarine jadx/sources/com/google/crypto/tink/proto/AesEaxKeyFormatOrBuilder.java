package com.google.crypto.tink.proto;

import com.google.crypto.tink.shaded.protobuf.MessageLiteOrBuilder;

/* JADX INFO: loaded from: classes2.dex */
public interface AesEaxKeyFormatOrBuilder extends MessageLiteOrBuilder {
    int getKeySize();

    AesEaxParams getParams();

    boolean hasParams();
}
