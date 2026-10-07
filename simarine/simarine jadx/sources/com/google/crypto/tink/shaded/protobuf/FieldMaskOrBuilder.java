package com.google.crypto.tink.shaded.protobuf;

import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public interface FieldMaskOrBuilder extends MessageLiteOrBuilder {
    String getPaths(int index);

    ByteString getPathsBytes(int index);

    int getPathsCount();

    List<String> getPathsList();
}
