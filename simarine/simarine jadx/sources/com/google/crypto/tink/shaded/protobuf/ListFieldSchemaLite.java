package com.google.crypto.tink.shaded.protobuf;

import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
final class ListFieldSchemaLite implements ListFieldSchema {
    ListFieldSchemaLite() {
    }

    @Override // com.google.crypto.tink.shaded.protobuf.ListFieldSchema
    public <L> List<L> mutableListAt(Object message, long offset) {
        Internal.ProtobufList protobufList = getProtobufList(message, offset);
        if (protobufList.isModifiable()) {
            return protobufList;
        }
        int size = protobufList.size();
        Internal.ProtobufList protobufListMutableCopyWithCapacity2 = protobufList.mutableCopyWithCapacity2(size == 0 ? 10 : size * 2);
        UnsafeUtil.putObject(message, offset, protobufListMutableCopyWithCapacity2);
        return protobufListMutableCopyWithCapacity2;
    }

    @Override // com.google.crypto.tink.shaded.protobuf.ListFieldSchema
    public void makeImmutableListAt(Object message, long offset) {
        getProtobufList(message, offset).makeImmutable();
    }

    @Override // com.google.crypto.tink.shaded.protobuf.ListFieldSchema
    public <E> void mergeListsAt(Object msg, Object otherMsg, long offset) {
        Internal.ProtobufList protobufList = getProtobufList(msg, offset);
        Internal.ProtobufList protobufList2 = getProtobufList(otherMsg, offset);
        int size = protobufList.size();
        int size2 = protobufList2.size();
        if (size > 0 && size2 > 0) {
            if (!protobufList.isModifiable()) {
                protobufList = protobufList.mutableCopyWithCapacity2(size2 + size);
            }
            protobufList.addAll(protobufList2);
        }
        if (size > 0) {
            protobufList2 = protobufList;
        }
        UnsafeUtil.putObject(msg, offset, protobufList2);
    }

    static <E> Internal.ProtobufList<E> getProtobufList(Object message, long offset) {
        return (Internal.ProtobufList) UnsafeUtil.getObject(message, offset);
    }
}
