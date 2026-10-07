package com.google.crypto.tink.proto;

import com.google.crypto.tink.shaded.protobuf.MessageLiteOrBuilder;

/* JADX INFO: loaded from: classes2.dex */
public interface PrfBasedDeriverKeyFormatOrBuilder extends MessageLiteOrBuilder {
    PrfBasedDeriverParams getParams();

    KeyTemplate getPrfKeyTemplate();

    boolean hasParams();

    boolean hasPrfKeyTemplate();
}
