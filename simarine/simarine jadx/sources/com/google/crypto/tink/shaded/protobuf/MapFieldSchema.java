package com.google.crypto.tink.shaded.protobuf;

import java.util.Map;

/* JADX INFO: loaded from: classes2.dex */
@CheckReturnValue
interface MapFieldSchema {
    Map<?, ?> forMapData(Object mapField);

    MapEntryLite.Metadata<?, ?> forMapMetadata(Object mapDefaultEntry);

    Map<?, ?> forMutableMapData(Object mapField);

    int getSerializedSize(int fieldNumber, Object mapField, Object mapDefaultEntry);

    boolean isImmutable(Object mapField);

    Object mergeFrom(Object destMapField, Object srcMapField);

    Object newMapField(Object mapDefaultEntry);

    Object toImmutable(Object mapField);
}
