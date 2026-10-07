package com.google.crypto.tink.proto;

import com.google.crypto.tink.shaded.protobuf.MessageLiteOrBuilder;

/* JADX INFO: loaded from: classes2.dex */
public interface PrfBasedDeriverKeyOrBuilder extends MessageLiteOrBuilder {
    PrfBasedDeriverParams getParams();

    KeyData getPrfKey();

    int getVersion();

    boolean hasParams();

    boolean hasPrfKey();
}
