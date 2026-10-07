package com.google.crypto.tink.proto;

import com.google.crypto.tink.shaded.protobuf.MessageLiteOrBuilder;

/* JADX INFO: loaded from: classes2.dex */
public interface HpkeParamsOrBuilder extends MessageLiteOrBuilder {
    HpkeAead getAead();

    int getAeadValue();

    HpkeKdf getKdf();

    int getKdfValue();

    HpkeKem getKem();

    int getKemValue();
}
