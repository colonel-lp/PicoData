package com.google.crypto.tink.proto;

import com.google.crypto.tink.shaded.protobuf.MessageLiteOrBuilder;

/* JADX INFO: loaded from: classes2.dex */
public interface HkdfPrfKeyFormatOrBuilder extends MessageLiteOrBuilder {
    int getKeySize();

    HkdfPrfParams getParams();

    int getVersion();

    boolean hasParams();
}
