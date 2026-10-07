package com.google.crypto.tink.shaded.protobuf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import kotlinx.coroutines.scheduling.WorkQueueKt;

/* JADX INFO: loaded from: classes2.dex */
@CheckReturnValue
abstract class BinaryWriter extends ByteOutput implements Writer {
    public static final int DEFAULT_CHUNK_SIZE = 4096;
    private static final int MAP_KEY_NUMBER = 1;
    private static final int MAP_VALUE_NUMBER = 2;
    private final BufferAllocator alloc;
    final ArrayDeque<AllocatedBuffer> buffers;
    private final int chunkSize;
    int totalDoneBytes;

    /* JADX INFO: Access modifiers changed from: private */
    public static byte computeUInt64SizeNoTag(long value) {
        byte b;
        if (((-128) & value) == 0) {
            return (byte) 1;
        }
        if (value < 0) {
            return (byte) 10;
        }
        if (((-34359738368L) & value) != 0) {
            b = (byte) 6;
            value >>>= 28;
        } else {
            b = 2;
        }
        if (((-2097152) & value) != 0) {
            b = (byte) (b + 2);
            value >>>= 14;
        }
        return (value & (-16384)) != 0 ? (byte) (b + 1) : b;
    }

    abstract void finishCurrentBuffer();

    public abstract int getTotalBytesWritten();

    abstract void requireSpace(int size);

    abstract void writeBool(boolean value);

    abstract void writeFixed32(int value);

    abstract void writeFixed64(long value);

    abstract void writeInt32(int value);

    abstract void writeSInt32(int value);

    abstract void writeSInt64(long value);

    abstract void writeString(String in);

    abstract void writeTag(int fieldNumber, int wireType);

    abstract void writeVarint32(int value);

    abstract void writeVarint64(long value);

    /* synthetic */ BinaryWriter(BufferAllocator bufferAllocator, int i, AnonymousClass1 anonymousClass1) {
        this(bufferAllocator, i);
    }

    public static BinaryWriter newHeapInstance(BufferAllocator alloc) {
        return newHeapInstance(alloc, 4096);
    }

    public static BinaryWriter newHeapInstance(BufferAllocator alloc, int chunkSize) {
        if (isUnsafeHeapSupported()) {
            return newUnsafeHeapInstance(alloc, chunkSize);
        }
        return newSafeHeapInstance(alloc, chunkSize);
    }

    public static BinaryWriter newDirectInstance(BufferAllocator alloc) {
        return newDirectInstance(alloc, 4096);
    }

    public static BinaryWriter newDirectInstance(BufferAllocator alloc, int chunkSize) {
        if (isUnsafeDirectSupported()) {
            return newUnsafeDirectInstance(alloc, chunkSize);
        }
        return newSafeDirectInstance(alloc, chunkSize);
    }

    static boolean isUnsafeHeapSupported() {
        return UnsafeHeapWriter.isSupported();
    }

    static boolean isUnsafeDirectSupported() {
        return UnsafeDirectWriter.isSupported();
    }

    static BinaryWriter newSafeHeapInstance(BufferAllocator alloc, int chunkSize) {
        return new SafeHeapWriter(alloc, chunkSize);
    }

    static BinaryWriter newUnsafeHeapInstance(BufferAllocator alloc, int chunkSize) {
        if (!isUnsafeHeapSupported()) {
            throw new UnsupportedOperationException("Unsafe operations not supported");
        }
        return new UnsafeHeapWriter(alloc, chunkSize);
    }

    static BinaryWriter newSafeDirectInstance(BufferAllocator alloc, int chunkSize) {
        return new SafeDirectWriter(alloc, chunkSize);
    }

    static BinaryWriter newUnsafeDirectInstance(BufferAllocator alloc, int chunkSize) {
        if (!isUnsafeDirectSupported()) {
            throw new UnsupportedOperationException("Unsafe operations not supported");
        }
        return new UnsafeDirectWriter(alloc, chunkSize);
    }

    private BinaryWriter(BufferAllocator alloc, int chunkSize) {
        this.buffers = new ArrayDeque<>(4);
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be > 0");
        }
        this.alloc = (BufferAllocator) Internal.checkNotNull(alloc, "alloc");
        this.chunkSize = chunkSize;
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final Writer.FieldOrder fieldOrder() {
        return Writer.FieldOrder.DESCENDING;
    }

    public final Queue<AllocatedBuffer> complete() {
        finishCurrentBuffer();
        return this.buffers;
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeSFixed32(int fieldNumber, int value) throws IOException {
        writeFixed32(fieldNumber, value);
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeInt64(int fieldNumber, long value) throws IOException {
        writeUInt64(fieldNumber, value);
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeSFixed64(int fieldNumber, long value) throws IOException {
        writeFixed64(fieldNumber, value);
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeFloat(int fieldNumber, float value) throws IOException {
        writeFixed32(fieldNumber, Float.floatToRawIntBits(value));
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeDouble(int fieldNumber, double value) throws IOException {
        writeFixed64(fieldNumber, Double.doubleToRawLongBits(value));
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeEnum(int fieldNumber, int value) throws IOException {
        writeInt32(fieldNumber, value);
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeInt32List(int fieldNumber, List<Integer> list, boolean packed) throws IOException {
        if (list instanceof IntArrayList) {
            writeInt32List_Internal(fieldNumber, (IntArrayList) list, packed);
        } else {
            writeInt32List_Internal(fieldNumber, list, packed);
        }
    }

    private void writeInt32List_Internal(int fieldNumber, List<Integer> list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 10) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeInt32(list.get(size).intValue());
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeInt32(fieldNumber, list.get(size2).intValue());
        }
    }

    private void writeInt32List_Internal(int fieldNumber, IntArrayList list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 10) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeInt32(list.getInt(size));
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeInt32(fieldNumber, list.getInt(size2));
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeFixed32List(int fieldNumber, List<Integer> list, boolean packed) throws IOException {
        if (list instanceof IntArrayList) {
            writeFixed32List_Internal(fieldNumber, (IntArrayList) list, packed);
        } else {
            writeFixed32List_Internal(fieldNumber, list, packed);
        }
    }

    private void writeFixed32List_Internal(int fieldNumber, List<Integer> list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 4) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeFixed32(list.get(size).intValue());
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeFixed32(fieldNumber, list.get(size2).intValue());
        }
    }

    private void writeFixed32List_Internal(int fieldNumber, IntArrayList list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 4) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeFixed32(list.getInt(size));
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeFixed32(fieldNumber, list.getInt(size2));
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeInt64List(int fieldNumber, List<Long> list, boolean packed) throws IOException {
        writeUInt64List(fieldNumber, list, packed);
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeUInt64List(int fieldNumber, List<Long> list, boolean packed) throws IOException {
        if (list instanceof LongArrayList) {
            writeUInt64List_Internal(fieldNumber, (LongArrayList) list, packed);
        } else {
            writeUInt64List_Internal(fieldNumber, list, packed);
        }
    }

    private void writeUInt64List_Internal(int fieldNumber, List<Long> list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 10) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeVarint64(list.get(size).longValue());
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeUInt64(fieldNumber, list.get(size2).longValue());
        }
    }

    private void writeUInt64List_Internal(int fieldNumber, LongArrayList list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 10) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeVarint64(list.getLong(size));
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeUInt64(fieldNumber, list.getLong(size2));
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeFixed64List(int fieldNumber, List<Long> list, boolean packed) throws IOException {
        if (list instanceof LongArrayList) {
            writeFixed64List_Internal(fieldNumber, (LongArrayList) list, packed);
        } else {
            writeFixed64List_Internal(fieldNumber, list, packed);
        }
    }

    private void writeFixed64List_Internal(int fieldNumber, List<Long> list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 8) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeFixed64(list.get(size).longValue());
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeFixed64(fieldNumber, list.get(size2).longValue());
        }
    }

    private void writeFixed64List_Internal(int fieldNumber, LongArrayList list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 8) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeFixed64(list.getLong(size));
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeFixed64(fieldNumber, list.getLong(size2));
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeFloatList(int fieldNumber, List<Float> list, boolean packed) throws IOException {
        if (list instanceof FloatArrayList) {
            writeFloatList_Internal(fieldNumber, (FloatArrayList) list, packed);
        } else {
            writeFloatList_Internal(fieldNumber, list, packed);
        }
    }

    private void writeFloatList_Internal(int fieldNumber, List<Float> list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 4) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeFixed32(Float.floatToRawIntBits(list.get(size).floatValue()));
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeFloat(fieldNumber, list.get(size2).floatValue());
        }
    }

    private void writeFloatList_Internal(int fieldNumber, FloatArrayList list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 4) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeFixed32(Float.floatToRawIntBits(list.getFloat(size)));
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeFloat(fieldNumber, list.getFloat(size2));
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeDoubleList(int fieldNumber, List<Double> list, boolean packed) throws IOException {
        if (list instanceof DoubleArrayList) {
            writeDoubleList_Internal(fieldNumber, (DoubleArrayList) list, packed);
        } else {
            writeDoubleList_Internal(fieldNumber, list, packed);
        }
    }

    private void writeDoubleList_Internal(int fieldNumber, List<Double> list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 8) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeFixed64(Double.doubleToRawLongBits(list.get(size).doubleValue()));
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeDouble(fieldNumber, list.get(size2).doubleValue());
        }
    }

    private void writeDoubleList_Internal(int fieldNumber, DoubleArrayList list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 8) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeFixed64(Double.doubleToRawLongBits(list.getDouble(size)));
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeDouble(fieldNumber, list.getDouble(size2));
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeEnumList(int fieldNumber, List<Integer> list, boolean packed) throws IOException {
        writeInt32List(fieldNumber, list, packed);
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeBoolList(int fieldNumber, List<Boolean> list, boolean packed) throws IOException {
        if (list instanceof BooleanArrayList) {
            writeBoolList_Internal(fieldNumber, (BooleanArrayList) list, packed);
        } else {
            writeBoolList_Internal(fieldNumber, list, packed);
        }
    }

    private void writeBoolList_Internal(int fieldNumber, List<Boolean> list, boolean packed) throws IOException {
        if (packed) {
            requireSpace(list.size() + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeBool(list.get(size).booleanValue());
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeBool(fieldNumber, list.get(size2).booleanValue());
        }
    }

    private void writeBoolList_Internal(int fieldNumber, BooleanArrayList list, boolean packed) throws IOException {
        if (packed) {
            requireSpace(list.size() + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeBool(list.getBoolean(size));
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeBool(fieldNumber, list.getBoolean(size2));
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeStringList(int fieldNumber, List<String> list) throws IOException {
        if (list instanceof LazyStringList) {
            LazyStringList lazyStringList = (LazyStringList) list;
            for (int size = list.size() - 1; size >= 0; size--) {
                writeLazyString(fieldNumber, lazyStringList.getRaw(size));
            }
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeString(fieldNumber, list.get(size2));
        }
    }

    private void writeLazyString(int fieldNumber, Object value) throws IOException {
        if (value instanceof String) {
            writeString(fieldNumber, (String) value);
        } else {
            writeBytes(fieldNumber, (ByteString) value);
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeBytesList(int fieldNumber, List<ByteString> list) throws IOException {
        for (int size = list.size() - 1; size >= 0; size--) {
            writeBytes(fieldNumber, list.get(size));
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeUInt32List(int fieldNumber, List<Integer> list, boolean packed) throws IOException {
        if (list instanceof IntArrayList) {
            writeUInt32List_Internal(fieldNumber, (IntArrayList) list, packed);
        } else {
            writeUInt32List_Internal(fieldNumber, list, packed);
        }
    }

    private void writeUInt32List_Internal(int fieldNumber, List<Integer> list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 5) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeVarint32(list.get(size).intValue());
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeUInt32(fieldNumber, list.get(size2).intValue());
        }
    }

    private void writeUInt32List_Internal(int fieldNumber, IntArrayList list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 5) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeVarint32(list.getInt(size));
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeUInt32(fieldNumber, list.getInt(size2));
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeSFixed32List(int fieldNumber, List<Integer> list, boolean packed) throws IOException {
        writeFixed32List(fieldNumber, list, packed);
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeSFixed64List(int fieldNumber, List<Long> list, boolean packed) throws IOException {
        writeFixed64List(fieldNumber, list, packed);
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeSInt32List(int fieldNumber, List<Integer> list, boolean packed) throws IOException {
        if (list instanceof IntArrayList) {
            writeSInt32List_Internal(fieldNumber, (IntArrayList) list, packed);
        } else {
            writeSInt32List_Internal(fieldNumber, list, packed);
        }
    }

    private void writeSInt32List_Internal(int fieldNumber, List<Integer> list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 5) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeSInt32(list.get(size).intValue());
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeSInt32(fieldNumber, list.get(size2).intValue());
        }
    }

    private void writeSInt32List_Internal(int fieldNumber, IntArrayList list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 5) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeSInt32(list.getInt(size));
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeSInt32(fieldNumber, list.getInt(size2));
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeSInt64List(int fieldNumber, List<Long> list, boolean packed) throws IOException {
        if (list instanceof LongArrayList) {
            writeSInt64List_Internal(fieldNumber, (LongArrayList) list, packed);
        } else {
            writeSInt64List_Internal(fieldNumber, list, packed);
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public <K, V> void writeMap(int fieldNumber, MapEntryLite.Metadata<K, V> metadata, Map<K, V> map) throws IOException {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            int totalBytesWritten = getTotalBytesWritten();
            writeMapEntryField(this, 2, metadata.valueType, entry.getValue());
            writeMapEntryField(this, 1, metadata.keyType, entry.getKey());
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
        }
    }

    /* JADX INFO: renamed from: com.google.crypto.tink.shaded.protobuf.BinaryWriter$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$google$protobuf$WireFormat$FieldType;

        static {
            int[] iArr = new int[WireFormat.FieldType.values().length];
            $SwitchMap$com$google$protobuf$WireFormat$FieldType = iArr;
            try {
                iArr[WireFormat.FieldType.BOOL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.FIXED32.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.FIXED64.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.INT32.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.INT64.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.SFIXED32.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.SFIXED64.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.SINT32.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.SINT64.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.STRING.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.UINT32.ordinal()] = 11;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.UINT64.ordinal()] = 12;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.FLOAT.ordinal()] = 13;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.DOUBLE.ordinal()] = 14;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.MESSAGE.ordinal()] = 15;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.BYTES.ordinal()] = 16;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.ENUM.ordinal()] = 17;
            } catch (NoSuchFieldError unused17) {
            }
        }
    }

    static final void writeMapEntryField(Writer writer, int fieldNumber, WireFormat.FieldType fieldType, Object object) throws IOException {
        switch (AnonymousClass1.$SwitchMap$com$google$protobuf$WireFormat$FieldType[fieldType.ordinal()]) {
            case 1:
                writer.writeBool(fieldNumber, ((Boolean) object).booleanValue());
                return;
            case 2:
                writer.writeFixed32(fieldNumber, ((Integer) object).intValue());
                return;
            case 3:
                writer.writeFixed64(fieldNumber, ((Long) object).longValue());
                return;
            case 4:
                writer.writeInt32(fieldNumber, ((Integer) object).intValue());
                return;
            case 5:
                writer.writeInt64(fieldNumber, ((Long) object).longValue());
                return;
            case 6:
                writer.writeSFixed32(fieldNumber, ((Integer) object).intValue());
                return;
            case 7:
                writer.writeSFixed64(fieldNumber, ((Long) object).longValue());
                return;
            case 8:
                writer.writeSInt32(fieldNumber, ((Integer) object).intValue());
                return;
            case 9:
                writer.writeSInt64(fieldNumber, ((Long) object).longValue());
                return;
            case 10:
                writer.writeString(fieldNumber, (String) object);
                return;
            case 11:
                writer.writeUInt32(fieldNumber, ((Integer) object).intValue());
                return;
            case 12:
                writer.writeUInt64(fieldNumber, ((Long) object).longValue());
                return;
            case 13:
                writer.writeFloat(fieldNumber, ((Float) object).floatValue());
                return;
            case 14:
                writer.writeDouble(fieldNumber, ((Double) object).doubleValue());
                return;
            case 15:
                writer.writeMessage(fieldNumber, object);
                return;
            case 16:
                writer.writeBytes(fieldNumber, (ByteString) object);
                return;
            case 17:
                if (object instanceof Internal.EnumLite) {
                    writer.writeEnum(fieldNumber, ((Internal.EnumLite) object).getNumber());
                    return;
                } else {
                    if (object instanceof Integer) {
                        writer.writeEnum(fieldNumber, ((Integer) object).intValue());
                        return;
                    }
                    throw new IllegalArgumentException("Unexpected type for enum in map.");
                }
            default:
                throw new IllegalArgumentException("Unsupported map value type for: " + fieldType);
        }
    }

    private void writeSInt64List_Internal(int fieldNumber, List<Long> list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 10) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeSInt64(list.get(size).longValue());
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeSInt64(fieldNumber, list.get(size2).longValue());
        }
    }

    private void writeSInt64List_Internal(int fieldNumber, LongArrayList list, boolean packed) throws IOException {
        if (packed) {
            requireSpace((list.size() * 10) + 10);
            int totalBytesWritten = getTotalBytesWritten();
            for (int size = list.size() - 1; size >= 0; size--) {
                writeSInt64(list.getLong(size));
            }
            writeVarint32(getTotalBytesWritten() - totalBytesWritten);
            writeTag(fieldNumber, 2);
            return;
        }
        for (int size2 = list.size() - 1; size2 >= 0; size2--) {
            writeSInt64(fieldNumber, list.getLong(size2));
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeMessageList(int fieldNumber, List<?> list) throws IOException {
        for (int size = list.size() - 1; size >= 0; size--) {
            writeMessage(fieldNumber, list.get(size));
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeMessageList(int fieldNumber, List<?> list, Schema schema) throws IOException {
        for (int size = list.size() - 1; size >= 0; size--) {
            writeMessage(fieldNumber, list.get(size), schema);
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    @Deprecated
    public final void writeGroupList(int fieldNumber, List<?> list) throws IOException {
        for (int size = list.size() - 1; size >= 0; size--) {
            writeGroup(fieldNumber, list.get(size));
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    @Deprecated
    public final void writeGroupList(int fieldNumber, List<?> list, Schema schema) throws IOException {
        for (int size = list.size() - 1; size >= 0; size--) {
            writeGroup(fieldNumber, list.get(size), schema);
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Writer
    public final void writeMessageSetItem(int fieldNumber, Object value) throws IOException {
        writeTag(1, 4);
        if (value instanceof ByteString) {
            writeBytes(3, (ByteString) value);
        } else {
            writeMessage(3, value);
        }
        writeUInt32(2, fieldNumber);
        writeTag(1, 3);
    }

    final AllocatedBuffer newHeapBuffer() {
        return this.alloc.allocateHeapBuffer(this.chunkSize);
    }

    final AllocatedBuffer newHeapBuffer(int capacity) {
        return this.alloc.allocateHeapBuffer(Math.max(capacity, this.chunkSize));
    }

    final AllocatedBuffer newDirectBuffer() {
        return this.alloc.allocateDirectBuffer(this.chunkSize);
    }

    final AllocatedBuffer newDirectBuffer(int capacity) {
        return this.alloc.allocateDirectBuffer(Math.max(capacity, this.chunkSize));
    }

    private static final class SafeHeapWriter extends BinaryWriter {
        private AllocatedBuffer allocatedBuffer;
        private byte[] buffer;
        private int limit;
        private int limitMinusOne;
        private int offset;
        private int offsetMinusOne;
        private int pos;

        SafeHeapWriter(BufferAllocator alloc, int chunkSize) {
            super(alloc, chunkSize, null);
            nextBuffer();
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void finishCurrentBuffer() {
            if (this.allocatedBuffer != null) {
                this.totalDoneBytes += bytesWrittenToCurrentBuffer();
                AllocatedBuffer allocatedBuffer = this.allocatedBuffer;
                allocatedBuffer.position((this.pos - allocatedBuffer.arrayOffset()) + 1);
                this.allocatedBuffer = null;
                this.pos = 0;
                this.limitMinusOne = 0;
            }
        }

        private void nextBuffer() {
            nextBuffer(newHeapBuffer());
        }

        private void nextBuffer(int capacity) {
            nextBuffer(newHeapBuffer(capacity));
        }

        private void nextBuffer(AllocatedBuffer allocatedBuffer) {
            if (!allocatedBuffer.hasArray()) {
                throw new RuntimeException("Allocator returned non-heap buffer");
            }
            finishCurrentBuffer();
            this.buffers.addFirst(allocatedBuffer);
            this.allocatedBuffer = allocatedBuffer;
            this.buffer = allocatedBuffer.array();
            int iArrayOffset = allocatedBuffer.arrayOffset();
            this.limit = allocatedBuffer.limit() + iArrayOffset;
            int iPosition = iArrayOffset + allocatedBuffer.position();
            this.offset = iPosition;
            this.offsetMinusOne = iPosition - 1;
            int i = this.limit - 1;
            this.limitMinusOne = i;
            this.pos = i;
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        public int getTotalBytesWritten() {
            return this.totalDoneBytes + bytesWrittenToCurrentBuffer();
        }

        int bytesWrittenToCurrentBuffer() {
            return this.limitMinusOne - this.pos;
        }

        int spaceLeft() {
            return this.pos - this.offsetMinusOne;
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeUInt32(int fieldNumber, int value) throws IOException {
            requireSpace(10);
            writeVarint32(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeInt32(int fieldNumber, int value) throws IOException {
            requireSpace(15);
            writeInt32(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeSInt32(int fieldNumber, int value) throws IOException {
            requireSpace(10);
            writeSInt32(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeFixed32(int fieldNumber, int value) throws IOException {
            requireSpace(9);
            writeFixed32(value);
            writeTag(fieldNumber, 5);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeUInt64(int fieldNumber, long value) throws IOException {
            requireSpace(15);
            writeVarint64(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeSInt64(int fieldNumber, long value) throws IOException {
            requireSpace(15);
            writeSInt64(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeFixed64(int fieldNumber, long value) throws IOException {
            requireSpace(13);
            writeFixed64(value);
            writeTag(fieldNumber, 1);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeBool(int i, boolean z) throws IOException {
            requireSpace(6);
            write(z ? (byte) 1 : (byte) 0);
            writeTag(i, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeString(int fieldNumber, String value) throws IOException {
            int totalBytesWritten = getTotalBytesWritten();
            writeString(value);
            int totalBytesWritten2 = getTotalBytesWritten() - totalBytesWritten;
            requireSpace(10);
            writeVarint32(totalBytesWritten2);
            writeTag(fieldNumber, 2);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeBytes(int fieldNumber, ByteString value) throws IOException {
            try {
                value.writeToReverse(this);
                requireSpace(10);
                writeVarint32(value.size());
                writeTag(fieldNumber, 2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeMessage(int fieldNumber, Object value) throws IOException {
            int totalBytesWritten = getTotalBytesWritten();
            Protobuf.getInstance().writeTo(value, this);
            int totalBytesWritten2 = getTotalBytesWritten() - totalBytesWritten;
            requireSpace(10);
            writeVarint32(totalBytesWritten2);
            writeTag(fieldNumber, 2);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeMessage(int fieldNumber, Object value, Schema schema) throws IOException {
            int totalBytesWritten = getTotalBytesWritten();
            schema.writeTo(value, this);
            int totalBytesWritten2 = getTotalBytesWritten() - totalBytesWritten;
            requireSpace(10);
            writeVarint32(totalBytesWritten2);
            writeTag(fieldNumber, 2);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        @Deprecated
        public void writeGroup(int fieldNumber, Object value) throws IOException {
            writeTag(fieldNumber, 4);
            Protobuf.getInstance().writeTo(value, this);
            writeTag(fieldNumber, 3);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeGroup(int fieldNumber, Object value, Schema schema) throws IOException {
            writeTag(fieldNumber, 4);
            schema.writeTo(value, this);
            writeTag(fieldNumber, 3);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeStartGroup(int fieldNumber) {
            writeTag(fieldNumber, 3);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeEndGroup(int fieldNumber) {
            writeTag(fieldNumber, 4);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeInt32(int value) {
            if (value >= 0) {
                writeVarint32(value);
            } else {
                writeVarint64(value);
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeSInt32(int value) {
            writeVarint32(CodedOutputStream.encodeZigZag32(value));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeSInt64(long value) {
            writeVarint64(CodedOutputStream.encodeZigZag64(value));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeBool(boolean z) {
            write(z ? (byte) 1 : (byte) 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeTag(int fieldNumber, int wireType) {
            writeVarint32(WireFormat.makeTag(fieldNumber, wireType));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeVarint32(int value) {
            if ((value & (-128)) == 0) {
                writeVarint32OneByte(value);
                return;
            }
            if ((value & (-16384)) == 0) {
                writeVarint32TwoBytes(value);
                return;
            }
            if (((-2097152) & value) == 0) {
                writeVarint32ThreeBytes(value);
            } else if (((-268435456) & value) == 0) {
                writeVarint32FourBytes(value);
            } else {
                writeVarint32FiveBytes(value);
            }
        }

        private void writeVarint32OneByte(int value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            this.pos = i - 1;
            bArr[i] = (byte) value;
        }

        private void writeVarint32TwoBytes(int value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            int i2 = i - 1;
            this.pos = i2;
            bArr[i] = (byte) (value >>> 7);
            this.pos = i - 2;
            bArr[i2] = (byte) ((value & WorkQueueKt.MASK) | 128);
        }

        private void writeVarint32ThreeBytes(int value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            int i2 = i - 1;
            this.pos = i2;
            bArr[i] = (byte) (value >>> 14);
            int i3 = i - 2;
            this.pos = i3;
            bArr[i2] = (byte) (((value >>> 7) & WorkQueueKt.MASK) | 128);
            this.pos = i - 3;
            bArr[i3] = (byte) ((value & WorkQueueKt.MASK) | 128);
        }

        private void writeVarint32FourBytes(int value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            int i2 = i - 1;
            this.pos = i2;
            bArr[i] = (byte) (value >>> 21);
            int i3 = i - 2;
            this.pos = i3;
            bArr[i2] = (byte) (((value >>> 14) & WorkQueueKt.MASK) | 128);
            int i4 = i - 3;
            this.pos = i4;
            bArr[i3] = (byte) (((value >>> 7) & WorkQueueKt.MASK) | 128);
            this.pos = i - 4;
            bArr[i4] = (byte) ((value & WorkQueueKt.MASK) | 128);
        }

        private void writeVarint32FiveBytes(int value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            int i2 = i - 1;
            this.pos = i2;
            bArr[i] = (byte) (value >>> 28);
            int i3 = i - 2;
            this.pos = i3;
            bArr[i2] = (byte) (((value >>> 21) & WorkQueueKt.MASK) | 128);
            int i4 = i - 3;
            this.pos = i4;
            bArr[i3] = (byte) (((value >>> 14) & WorkQueueKt.MASK) | 128);
            int i5 = i - 4;
            this.pos = i5;
            bArr[i4] = (byte) (((value >>> 7) & WorkQueueKt.MASK) | 128);
            this.pos = i - 5;
            bArr[i5] = (byte) ((value & WorkQueueKt.MASK) | 128);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeVarint64(long value) {
            switch (BinaryWriter.computeUInt64SizeNoTag(value)) {
                case 1:
                    writeVarint64OneByte(value);
                    break;
                case 2:
                    writeVarint64TwoBytes(value);
                    break;
                case 3:
                    writeVarint64ThreeBytes(value);
                    break;
                case 4:
                    writeVarint64FourBytes(value);
                    break;
                case 5:
                    writeVarint64FiveBytes(value);
                    break;
                case 6:
                    writeVarint64SixBytes(value);
                    break;
                case 7:
                    writeVarint64SevenBytes(value);
                    break;
                case 8:
                    writeVarint64EightBytes(value);
                    break;
                case 9:
                    writeVarint64NineBytes(value);
                    break;
                case 10:
                    writeVarint64TenBytes(value);
                    break;
            }
        }

        private void writeVarint64OneByte(long value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            this.pos = i - 1;
            bArr[i] = (byte) value;
        }

        private void writeVarint64TwoBytes(long value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            int i2 = i - 1;
            this.pos = i2;
            bArr[i] = (byte) (value >>> 7);
            this.pos = i - 2;
            bArr[i2] = (byte) ((((int) value) & WorkQueueKt.MASK) | 128);
        }

        private void writeVarint64ThreeBytes(long value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            int i2 = i - 1;
            this.pos = i2;
            bArr[i] = (byte) (((int) value) >>> 14);
            int i3 = i - 2;
            this.pos = i3;
            bArr[i2] = (byte) (((value >>> 7) & 127) | 128);
            this.pos = i - 3;
            bArr[i3] = (byte) ((value & 127) | 128);
        }

        private void writeVarint64FourBytes(long value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            int i2 = i - 1;
            this.pos = i2;
            bArr[i] = (byte) (value >>> 21);
            int i3 = i - 2;
            this.pos = i3;
            bArr[i2] = (byte) (((value >>> 14) & 127) | 128);
            int i4 = i - 3;
            this.pos = i4;
            bArr[i3] = (byte) (((value >>> 7) & 127) | 128);
            this.pos = i - 4;
            bArr[i4] = (byte) ((value & 127) | 128);
        }

        private void writeVarint64FiveBytes(long value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            int i2 = i - 1;
            this.pos = i2;
            bArr[i] = (byte) (value >>> 28);
            int i3 = i - 2;
            this.pos = i3;
            bArr[i2] = (byte) (((value >>> 21) & 127) | 128);
            int i4 = i - 3;
            this.pos = i4;
            bArr[i3] = (byte) (((value >>> 14) & 127) | 128);
            int i5 = i - 4;
            this.pos = i5;
            bArr[i4] = (byte) (((value >>> 7) & 127) | 128);
            this.pos = i - 5;
            bArr[i5] = (byte) ((value & 127) | 128);
        }

        private void writeVarint64SixBytes(long value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            int i2 = i - 1;
            this.pos = i2;
            bArr[i] = (byte) (value >>> 35);
            int i3 = i - 2;
            this.pos = i3;
            bArr[i2] = (byte) (((value >>> 28) & 127) | 128);
            int i4 = i - 3;
            this.pos = i4;
            bArr[i3] = (byte) (((value >>> 21) & 127) | 128);
            int i5 = i - 4;
            this.pos = i5;
            bArr[i4] = (byte) (((value >>> 14) & 127) | 128);
            int i6 = i - 5;
            this.pos = i6;
            bArr[i5] = (byte) (((value >>> 7) & 127) | 128);
            this.pos = i - 6;
            bArr[i6] = (byte) ((value & 127) | 128);
        }

        private void writeVarint64SevenBytes(long value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            int i2 = i - 1;
            this.pos = i2;
            bArr[i] = (byte) (value >>> 42);
            int i3 = i - 2;
            this.pos = i3;
            bArr[i2] = (byte) (((value >>> 35) & 127) | 128);
            int i4 = i - 3;
            this.pos = i4;
            bArr[i3] = (byte) (((value >>> 28) & 127) | 128);
            int i5 = i - 4;
            this.pos = i5;
            bArr[i4] = (byte) (((value >>> 21) & 127) | 128);
            int i6 = i - 5;
            this.pos = i6;
            bArr[i5] = (byte) (((value >>> 14) & 127) | 128);
            int i7 = i - 6;
            this.pos = i7;
            bArr[i6] = (byte) (((value >>> 7) & 127) | 128);
            this.pos = i - 7;
            bArr[i7] = (byte) ((value & 127) | 128);
        }

        private void writeVarint64EightBytes(long value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            int i2 = i - 1;
            this.pos = i2;
            bArr[i] = (byte) (value >>> 49);
            int i3 = i - 2;
            this.pos = i3;
            bArr[i2] = (byte) (((value >>> 42) & 127) | 128);
            int i4 = i - 3;
            this.pos = i4;
            bArr[i3] = (byte) (((value >>> 35) & 127) | 128);
            int i5 = i - 4;
            this.pos = i5;
            bArr[i4] = (byte) (((value >>> 28) & 127) | 128);
            int i6 = i - 5;
            this.pos = i6;
            bArr[i5] = (byte) (((value >>> 21) & 127) | 128);
            int i7 = i - 6;
            this.pos = i7;
            bArr[i6] = (byte) (((value >>> 14) & 127) | 128);
            int i8 = i - 7;
            this.pos = i8;
            bArr[i7] = (byte) (((value >>> 7) & 127) | 128);
            this.pos = i - 8;
            bArr[i8] = (byte) ((value & 127) | 128);
        }

        private void writeVarint64NineBytes(long value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            int i2 = i - 1;
            this.pos = i2;
            bArr[i] = (byte) (value >>> 56);
            int i3 = i - 2;
            this.pos = i3;
            bArr[i2] = (byte) (((value >>> 49) & 127) | 128);
            int i4 = i - 3;
            this.pos = i4;
            bArr[i3] = (byte) (((value >>> 42) & 127) | 128);
            int i5 = i - 4;
            this.pos = i5;
            bArr[i4] = (byte) (((value >>> 35) & 127) | 128);
            int i6 = i - 5;
            this.pos = i6;
            bArr[i5] = (byte) (((value >>> 28) & 127) | 128);
            int i7 = i - 6;
            this.pos = i7;
            bArr[i6] = (byte) (((value >>> 21) & 127) | 128);
            int i8 = i - 7;
            this.pos = i8;
            bArr[i7] = (byte) (((value >>> 14) & 127) | 128);
            int i9 = i - 8;
            this.pos = i9;
            bArr[i8] = (byte) (((value >>> 7) & 127) | 128);
            this.pos = i - 9;
            bArr[i9] = (byte) ((value & 127) | 128);
        }

        private void writeVarint64TenBytes(long value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            int i2 = i - 1;
            this.pos = i2;
            bArr[i] = (byte) (value >>> 63);
            int i3 = i - 2;
            this.pos = i3;
            bArr[i2] = (byte) (((value >>> 56) & 127) | 128);
            int i4 = i - 3;
            this.pos = i4;
            bArr[i3] = (byte) (((value >>> 49) & 127) | 128);
            int i5 = i - 4;
            this.pos = i5;
            bArr[i4] = (byte) (((value >>> 42) & 127) | 128);
            int i6 = i - 5;
            this.pos = i6;
            bArr[i5] = (byte) (((value >>> 35) & 127) | 128);
            int i7 = i - 6;
            this.pos = i7;
            bArr[i6] = (byte) (((value >>> 28) & 127) | 128);
            int i8 = i - 7;
            this.pos = i8;
            bArr[i7] = (byte) (((value >>> 21) & 127) | 128);
            int i9 = i - 8;
            this.pos = i9;
            bArr[i8] = (byte) (((value >>> 14) & 127) | 128);
            int i10 = i - 9;
            this.pos = i10;
            bArr[i9] = (byte) (((value >>> 7) & 127) | 128);
            this.pos = i - 10;
            bArr[i10] = (byte) ((value & 127) | 128);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeFixed32(int value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            int i2 = i - 1;
            this.pos = i2;
            bArr[i] = (byte) ((value >> 24) & 255);
            int i3 = i - 2;
            this.pos = i3;
            bArr[i2] = (byte) ((value >> 16) & 255);
            int i4 = i - 3;
            this.pos = i4;
            bArr[i3] = (byte) ((value >> 8) & 255);
            this.pos = i - 4;
            bArr[i4] = (byte) (value & 255);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeFixed64(long value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            int i2 = i - 1;
            this.pos = i2;
            bArr[i] = (byte) (((int) (value >> 56)) & 255);
            int i3 = i - 2;
            this.pos = i3;
            bArr[i2] = (byte) (((int) (value >> 48)) & 255);
            int i4 = i - 3;
            this.pos = i4;
            bArr[i3] = (byte) (((int) (value >> 40)) & 255);
            int i5 = i - 4;
            this.pos = i5;
            bArr[i4] = (byte) (((int) (value >> 32)) & 255);
            int i6 = i - 5;
            this.pos = i6;
            bArr[i5] = (byte) (((int) (value >> 24)) & 255);
            int i7 = i - 6;
            this.pos = i7;
            bArr[i6] = (byte) (((int) (value >> 16)) & 255);
            int i8 = i - 7;
            this.pos = i8;
            bArr[i7] = (byte) (((int) (value >> 8)) & 255);
            this.pos = i - 8;
            bArr[i8] = (byte) (((int) value) & 255);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeString(String in) {
            int i;
            int i2;
            int i3;
            char cCharAt;
            requireSpace(in.length());
            int length = in.length() - 1;
            this.pos -= length;
            while (length >= 0 && (cCharAt = in.charAt(length)) < 128) {
                this.buffer[this.pos + length] = (byte) cCharAt;
                length--;
            }
            if (length == -1) {
                this.pos--;
                return;
            }
            this.pos += length;
            while (length >= 0) {
                char cCharAt2 = in.charAt(length);
                if (cCharAt2 < 128 && (i3 = this.pos) > this.offsetMinusOne) {
                    byte[] bArr = this.buffer;
                    this.pos = i3 - 1;
                    bArr[i3] = (byte) cCharAt2;
                } else if (cCharAt2 < 2048 && (i2 = this.pos) > this.offset) {
                    byte[] bArr2 = this.buffer;
                    int i4 = i2 - 1;
                    this.pos = i4;
                    bArr2[i2] = (byte) ((cCharAt2 & '?') | 128);
                    this.pos = i2 - 2;
                    bArr2[i4] = (byte) ((cCharAt2 >>> 6) | 960);
                } else if ((cCharAt2 < 55296 || 57343 < cCharAt2) && (i = this.pos) > this.offset + 1) {
                    byte[] bArr3 = this.buffer;
                    int i5 = i - 1;
                    this.pos = i5;
                    bArr3[i] = (byte) ((cCharAt2 & '?') | 128);
                    int i6 = i - 2;
                    this.pos = i6;
                    bArr3[i5] = (byte) (((cCharAt2 >>> 6) & 63) | 128);
                    this.pos = i - 3;
                    bArr3[i6] = (byte) ((cCharAt2 >>> '\f') | 480);
                } else {
                    if (this.pos > this.offset + 2) {
                        if (length != 0) {
                            char cCharAt3 = in.charAt(length - 1);
                            if (Character.isSurrogatePair(cCharAt3, cCharAt2)) {
                                length--;
                                int codePoint = Character.toCodePoint(cCharAt3, cCharAt2);
                                byte[] bArr4 = this.buffer;
                                int i7 = this.pos;
                                int i8 = i7 - 1;
                                this.pos = i8;
                                bArr4[i7] = (byte) ((codePoint & 63) | 128);
                                int i9 = i7 - 2;
                                this.pos = i9;
                                bArr4[i8] = (byte) (((codePoint >>> 6) & 63) | 128);
                                int i10 = i7 - 3;
                                this.pos = i10;
                                bArr4[i9] = (byte) (((codePoint >>> 12) & 63) | 128);
                                this.pos = i7 - 4;
                                bArr4[i10] = (byte) ((codePoint >>> 18) | 240);
                            }
                        }
                        throw new Utf8.UnpairedSurrogateException(length - 1, length);
                    }
                    requireSpace(length);
                    length++;
                }
                length--;
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void write(byte value) {
            byte[] bArr = this.buffer;
            int i = this.pos;
            this.pos = i - 1;
            bArr[i] = value;
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void write(byte[] value, int offset, int length) {
            if (spaceLeft() < length) {
                nextBuffer(length);
            }
            int i = this.pos - length;
            this.pos = i;
            System.arraycopy(value, offset, this.buffer, i + 1, length);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void writeLazy(byte[] value, int offset, int length) {
            if (spaceLeft() < length) {
                this.totalDoneBytes += length;
                this.buffers.addFirst(AllocatedBuffer.wrap(value, offset, length));
                nextBuffer();
            } else {
                int i = this.pos - length;
                this.pos = i;
                System.arraycopy(value, offset, this.buffer, i + 1, length);
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void write(ByteBuffer value) {
            int iRemaining = value.remaining();
            if (spaceLeft() < iRemaining) {
                nextBuffer(iRemaining);
            }
            int i = this.pos - iRemaining;
            this.pos = i;
            value.get(this.buffer, i + 1, iRemaining);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void writeLazy(ByteBuffer value) {
            int iRemaining = value.remaining();
            if (spaceLeft() < iRemaining) {
                this.totalDoneBytes += iRemaining;
                this.buffers.addFirst(AllocatedBuffer.wrap(value));
                nextBuffer();
            }
            int i = this.pos - iRemaining;
            this.pos = i;
            value.get(this.buffer, i + 1, iRemaining);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void requireSpace(int size) {
            if (spaceLeft() < size) {
                nextBuffer(size);
            }
        }
    }

    private static final class UnsafeHeapWriter extends BinaryWriter {
        private AllocatedBuffer allocatedBuffer;
        private byte[] buffer;
        private long limit;
        private long limitMinusOne;
        private long offset;
        private long offsetMinusOne;
        private long pos;

        UnsafeHeapWriter(BufferAllocator alloc, int chunkSize) {
            super(alloc, chunkSize, null);
            nextBuffer();
        }

        static boolean isSupported() {
            return UnsafeUtil.hasUnsafeArrayOperations();
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void finishCurrentBuffer() {
            if (this.allocatedBuffer != null) {
                this.totalDoneBytes += bytesWrittenToCurrentBuffer();
                this.allocatedBuffer.position((arrayPos() - this.allocatedBuffer.arrayOffset()) + 1);
                this.allocatedBuffer = null;
                this.pos = 0L;
                this.limitMinusOne = 0L;
            }
        }

        private int arrayPos() {
            return (int) this.pos;
        }

        private void nextBuffer() {
            nextBuffer(newHeapBuffer());
        }

        private void nextBuffer(int capacity) {
            nextBuffer(newHeapBuffer(capacity));
        }

        private void nextBuffer(AllocatedBuffer allocatedBuffer) {
            if (!allocatedBuffer.hasArray()) {
                throw new RuntimeException("Allocator returned non-heap buffer");
            }
            finishCurrentBuffer();
            this.buffers.addFirst(allocatedBuffer);
            this.allocatedBuffer = allocatedBuffer;
            this.buffer = allocatedBuffer.array();
            long jArrayOffset = allocatedBuffer.arrayOffset();
            this.limit = ((long) allocatedBuffer.limit()) + jArrayOffset;
            long jPosition = jArrayOffset + ((long) allocatedBuffer.position());
            this.offset = jPosition;
            this.offsetMinusOne = jPosition - 1;
            long j = this.limit - 1;
            this.limitMinusOne = j;
            this.pos = j;
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        public int getTotalBytesWritten() {
            return this.totalDoneBytes + bytesWrittenToCurrentBuffer();
        }

        int bytesWrittenToCurrentBuffer() {
            return (int) (this.limitMinusOne - this.pos);
        }

        int spaceLeft() {
            return (int) (this.pos - this.offsetMinusOne);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeUInt32(int fieldNumber, int value) {
            requireSpace(10);
            writeVarint32(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeInt32(int fieldNumber, int value) {
            requireSpace(15);
            writeInt32(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeSInt32(int fieldNumber, int value) {
            requireSpace(10);
            writeSInt32(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeFixed32(int fieldNumber, int value) {
            requireSpace(9);
            writeFixed32(value);
            writeTag(fieldNumber, 5);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeUInt64(int fieldNumber, long value) {
            requireSpace(15);
            writeVarint64(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeSInt64(int fieldNumber, long value) {
            requireSpace(15);
            writeSInt64(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeFixed64(int fieldNumber, long value) {
            requireSpace(13);
            writeFixed64(value);
            writeTag(fieldNumber, 1);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeBool(int i, boolean z) {
            requireSpace(6);
            write(z ? (byte) 1 : (byte) 0);
            writeTag(i, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeString(int fieldNumber, String value) {
            int totalBytesWritten = getTotalBytesWritten();
            writeString(value);
            int totalBytesWritten2 = getTotalBytesWritten() - totalBytesWritten;
            requireSpace(10);
            writeVarint32(totalBytesWritten2);
            writeTag(fieldNumber, 2);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeBytes(int fieldNumber, ByteString value) {
            try {
                value.writeToReverse(this);
                requireSpace(10);
                writeVarint32(value.size());
                writeTag(fieldNumber, 2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeMessage(int fieldNumber, Object value) throws IOException {
            int totalBytesWritten = getTotalBytesWritten();
            Protobuf.getInstance().writeTo(value, this);
            int totalBytesWritten2 = getTotalBytesWritten() - totalBytesWritten;
            requireSpace(10);
            writeVarint32(totalBytesWritten2);
            writeTag(fieldNumber, 2);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeMessage(int fieldNumber, Object value, Schema schema) throws IOException {
            int totalBytesWritten = getTotalBytesWritten();
            schema.writeTo(value, this);
            int totalBytesWritten2 = getTotalBytesWritten() - totalBytesWritten;
            requireSpace(10);
            writeVarint32(totalBytesWritten2);
            writeTag(fieldNumber, 2);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeGroup(int fieldNumber, Object value) throws IOException {
            writeTag(fieldNumber, 4);
            Protobuf.getInstance().writeTo(value, this);
            writeTag(fieldNumber, 3);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeGroup(int fieldNumber, Object value, Schema schema) throws IOException {
            writeTag(fieldNumber, 4);
            schema.writeTo(value, this);
            writeTag(fieldNumber, 3);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeStartGroup(int fieldNumber) {
            writeTag(fieldNumber, 3);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeEndGroup(int fieldNumber) {
            writeTag(fieldNumber, 4);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeInt32(int value) {
            if (value >= 0) {
                writeVarint32(value);
            } else {
                writeVarint64(value);
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeSInt32(int value) {
            writeVarint32(CodedOutputStream.encodeZigZag32(value));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeSInt64(long value) {
            writeVarint64(CodedOutputStream.encodeZigZag64(value));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeBool(boolean z) {
            write(z ? (byte) 1 : (byte) 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeTag(int fieldNumber, int wireType) {
            writeVarint32(WireFormat.makeTag(fieldNumber, wireType));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeVarint32(int value) {
            if ((value & (-128)) == 0) {
                writeVarint32OneByte(value);
                return;
            }
            if ((value & (-16384)) == 0) {
                writeVarint32TwoBytes(value);
                return;
            }
            if (((-2097152) & value) == 0) {
                writeVarint32ThreeBytes(value);
            } else if (((-268435456) & value) == 0) {
                writeVarint32FourBytes(value);
            } else {
                writeVarint32FiveBytes(value);
            }
        }

        private void writeVarint32OneByte(int value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, (byte) value);
        }

        private void writeVarint32TwoBytes(int value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, (byte) (value >>> 7));
            byte[] bArr2 = this.buffer;
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(bArr2, j2, (byte) ((value & WorkQueueKt.MASK) | 128));
        }

        private void writeVarint32ThreeBytes(int value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, (byte) (value >>> 14));
            byte[] bArr2 = this.buffer;
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(bArr2, j2, (byte) (((value >>> 7) & WorkQueueKt.MASK) | 128));
            byte[] bArr3 = this.buffer;
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(bArr3, j3, (byte) ((value & WorkQueueKt.MASK) | 128));
        }

        private void writeVarint32FourBytes(int value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, (byte) (value >>> 21));
            byte[] bArr2 = this.buffer;
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(bArr2, j2, (byte) (((value >>> 14) & WorkQueueKt.MASK) | 128));
            byte[] bArr3 = this.buffer;
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(bArr3, j3, (byte) (((value >>> 7) & WorkQueueKt.MASK) | 128));
            byte[] bArr4 = this.buffer;
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(bArr4, j4, (byte) ((value & WorkQueueKt.MASK) | 128));
        }

        private void writeVarint32FiveBytes(int value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, (byte) (value >>> 28));
            byte[] bArr2 = this.buffer;
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(bArr2, j2, (byte) (((value >>> 21) & WorkQueueKt.MASK) | 128));
            byte[] bArr3 = this.buffer;
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(bArr3, j3, (byte) (((value >>> 14) & WorkQueueKt.MASK) | 128));
            byte[] bArr4 = this.buffer;
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(bArr4, j4, (byte) (((value >>> 7) & WorkQueueKt.MASK) | 128));
            byte[] bArr5 = this.buffer;
            long j5 = this.pos;
            this.pos = j5 - 1;
            UnsafeUtil.putByte(bArr5, j5, (byte) ((value & WorkQueueKt.MASK) | 128));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeVarint64(long value) {
            switch (BinaryWriter.computeUInt64SizeNoTag(value)) {
                case 1:
                    writeVarint64OneByte(value);
                    break;
                case 2:
                    writeVarint64TwoBytes(value);
                    break;
                case 3:
                    writeVarint64ThreeBytes(value);
                    break;
                case 4:
                    writeVarint64FourBytes(value);
                    break;
                case 5:
                    writeVarint64FiveBytes(value);
                    break;
                case 6:
                    writeVarint64SixBytes(value);
                    break;
                case 7:
                    writeVarint64SevenBytes(value);
                    break;
                case 8:
                    writeVarint64EightBytes(value);
                    break;
                case 9:
                    writeVarint64NineBytes(value);
                    break;
                case 10:
                    writeVarint64TenBytes(value);
                    break;
            }
        }

        private void writeVarint64OneByte(long value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, (byte) value);
        }

        private void writeVarint64TwoBytes(long value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, (byte) (value >>> 7));
            byte[] bArr2 = this.buffer;
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(bArr2, j2, (byte) ((((int) value) & WorkQueueKt.MASK) | 128));
        }

        private void writeVarint64ThreeBytes(long value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, (byte) (((int) value) >>> 14));
            byte[] bArr2 = this.buffer;
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(bArr2, j2, (byte) (((value >>> 7) & 127) | 128));
            byte[] bArr3 = this.buffer;
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(bArr3, j3, (byte) ((value & 127) | 128));
        }

        private void writeVarint64FourBytes(long value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, (byte) (value >>> 21));
            byte[] bArr2 = this.buffer;
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(bArr2, j2, (byte) (((value >>> 14) & 127) | 128));
            byte[] bArr3 = this.buffer;
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(bArr3, j3, (byte) (((value >>> 7) & 127) | 128));
            byte[] bArr4 = this.buffer;
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(bArr4, j4, (byte) ((value & 127) | 128));
        }

        private void writeVarint64FiveBytes(long value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, (byte) (value >>> 28));
            byte[] bArr2 = this.buffer;
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(bArr2, j2, (byte) (((value >>> 21) & 127) | 128));
            byte[] bArr3 = this.buffer;
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(bArr3, j3, (byte) (((value >>> 14) & 127) | 128));
            byte[] bArr4 = this.buffer;
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(bArr4, j4, (byte) (((value >>> 7) & 127) | 128));
            byte[] bArr5 = this.buffer;
            long j5 = this.pos;
            this.pos = j5 - 1;
            UnsafeUtil.putByte(bArr5, j5, (byte) ((value & 127) | 128));
        }

        private void writeVarint64SixBytes(long value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, (byte) (value >>> 35));
            byte[] bArr2 = this.buffer;
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(bArr2, j2, (byte) (((value >>> 28) & 127) | 128));
            byte[] bArr3 = this.buffer;
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(bArr3, j3, (byte) (((value >>> 21) & 127) | 128));
            byte[] bArr4 = this.buffer;
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(bArr4, j4, (byte) (((value >>> 14) & 127) | 128));
            byte[] bArr5 = this.buffer;
            long j5 = this.pos;
            this.pos = j5 - 1;
            UnsafeUtil.putByte(bArr5, j5, (byte) (((value >>> 7) & 127) | 128));
            byte[] bArr6 = this.buffer;
            long j6 = this.pos;
            this.pos = j6 - 1;
            UnsafeUtil.putByte(bArr6, j6, (byte) ((value & 127) | 128));
        }

        private void writeVarint64SevenBytes(long value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, (byte) (value >>> 42));
            byte[] bArr2 = this.buffer;
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(bArr2, j2, (byte) (((value >>> 35) & 127) | 128));
            byte[] bArr3 = this.buffer;
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(bArr3, j3, (byte) (((value >>> 28) & 127) | 128));
            byte[] bArr4 = this.buffer;
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(bArr4, j4, (byte) (((value >>> 21) & 127) | 128));
            byte[] bArr5 = this.buffer;
            long j5 = this.pos;
            this.pos = j5 - 1;
            UnsafeUtil.putByte(bArr5, j5, (byte) (((value >>> 14) & 127) | 128));
            byte[] bArr6 = this.buffer;
            long j6 = this.pos;
            this.pos = j6 - 1;
            UnsafeUtil.putByte(bArr6, j6, (byte) (((value >>> 7) & 127) | 128));
            byte[] bArr7 = this.buffer;
            long j7 = this.pos;
            this.pos = j7 - 1;
            UnsafeUtil.putByte(bArr7, j7, (byte) ((value & 127) | 128));
        }

        private void writeVarint64EightBytes(long value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, (byte) (value >>> 49));
            byte[] bArr2 = this.buffer;
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(bArr2, j2, (byte) (((value >>> 42) & 127) | 128));
            byte[] bArr3 = this.buffer;
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(bArr3, j3, (byte) (((value >>> 35) & 127) | 128));
            byte[] bArr4 = this.buffer;
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(bArr4, j4, (byte) (((value >>> 28) & 127) | 128));
            byte[] bArr5 = this.buffer;
            long j5 = this.pos;
            this.pos = j5 - 1;
            UnsafeUtil.putByte(bArr5, j5, (byte) (((value >>> 21) & 127) | 128));
            byte[] bArr6 = this.buffer;
            long j6 = this.pos;
            this.pos = j6 - 1;
            UnsafeUtil.putByte(bArr6, j6, (byte) (((value >>> 14) & 127) | 128));
            byte[] bArr7 = this.buffer;
            long j7 = this.pos;
            this.pos = j7 - 1;
            UnsafeUtil.putByte(bArr7, j7, (byte) (((value >>> 7) & 127) | 128));
            byte[] bArr8 = this.buffer;
            long j8 = this.pos;
            this.pos = j8 - 1;
            UnsafeUtil.putByte(bArr8, j8, (byte) ((value & 127) | 128));
        }

        private void writeVarint64NineBytes(long value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, (byte) (value >>> 56));
            byte[] bArr2 = this.buffer;
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(bArr2, j2, (byte) (((value >>> 49) & 127) | 128));
            byte[] bArr3 = this.buffer;
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(bArr3, j3, (byte) (((value >>> 42) & 127) | 128));
            byte[] bArr4 = this.buffer;
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(bArr4, j4, (byte) (((value >>> 35) & 127) | 128));
            byte[] bArr5 = this.buffer;
            long j5 = this.pos;
            this.pos = j5 - 1;
            UnsafeUtil.putByte(bArr5, j5, (byte) (((value >>> 28) & 127) | 128));
            byte[] bArr6 = this.buffer;
            long j6 = this.pos;
            this.pos = j6 - 1;
            UnsafeUtil.putByte(bArr6, j6, (byte) (((value >>> 21) & 127) | 128));
            byte[] bArr7 = this.buffer;
            long j7 = this.pos;
            this.pos = j7 - 1;
            UnsafeUtil.putByte(bArr7, j7, (byte) (((value >>> 14) & 127) | 128));
            byte[] bArr8 = this.buffer;
            long j8 = this.pos;
            this.pos = j8 - 1;
            UnsafeUtil.putByte(bArr8, j8, (byte) (((value >>> 7) & 127) | 128));
            byte[] bArr9 = this.buffer;
            long j9 = this.pos;
            this.pos = j9 - 1;
            UnsafeUtil.putByte(bArr9, j9, (byte) ((value & 127) | 128));
        }

        private void writeVarint64TenBytes(long value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, (byte) (value >>> 63));
            byte[] bArr2 = this.buffer;
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(bArr2, j2, (byte) (((value >>> 56) & 127) | 128));
            byte[] bArr3 = this.buffer;
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(bArr3, j3, (byte) (((value >>> 49) & 127) | 128));
            byte[] bArr4 = this.buffer;
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(bArr4, j4, (byte) (((value >>> 42) & 127) | 128));
            byte[] bArr5 = this.buffer;
            long j5 = this.pos;
            this.pos = j5 - 1;
            UnsafeUtil.putByte(bArr5, j5, (byte) (((value >>> 35) & 127) | 128));
            byte[] bArr6 = this.buffer;
            long j6 = this.pos;
            this.pos = j6 - 1;
            UnsafeUtil.putByte(bArr6, j6, (byte) (((value >>> 28) & 127) | 128));
            byte[] bArr7 = this.buffer;
            long j7 = this.pos;
            this.pos = j7 - 1;
            UnsafeUtil.putByte(bArr7, j7, (byte) (((value >>> 21) & 127) | 128));
            byte[] bArr8 = this.buffer;
            long j8 = this.pos;
            this.pos = j8 - 1;
            UnsafeUtil.putByte(bArr8, j8, (byte) (((value >>> 14) & 127) | 128));
            byte[] bArr9 = this.buffer;
            long j9 = this.pos;
            this.pos = j9 - 1;
            UnsafeUtil.putByte(bArr9, j9, (byte) (((value >>> 7) & 127) | 128));
            byte[] bArr10 = this.buffer;
            long j10 = this.pos;
            this.pos = j10 - 1;
            UnsafeUtil.putByte(bArr10, j10, (byte) ((value & 127) | 128));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeFixed32(int value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, (byte) ((value >> 24) & 255));
            byte[] bArr2 = this.buffer;
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(bArr2, j2, (byte) ((value >> 16) & 255));
            byte[] bArr3 = this.buffer;
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(bArr3, j3, (byte) ((value >> 8) & 255));
            byte[] bArr4 = this.buffer;
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(bArr4, j4, (byte) (value & 255));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeFixed64(long value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, (byte) (((int) (value >> 56)) & 255));
            byte[] bArr2 = this.buffer;
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(bArr2, j2, (byte) (((int) (value >> 48)) & 255));
            byte[] bArr3 = this.buffer;
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(bArr3, j3, (byte) (((int) (value >> 40)) & 255));
            byte[] bArr4 = this.buffer;
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(bArr4, j4, (byte) (((int) (value >> 32)) & 255));
            byte[] bArr5 = this.buffer;
            long j5 = this.pos;
            this.pos = j5 - 1;
            UnsafeUtil.putByte(bArr5, j5, (byte) (((int) (value >> 24)) & 255));
            byte[] bArr6 = this.buffer;
            long j6 = this.pos;
            this.pos = j6 - 1;
            UnsafeUtil.putByte(bArr6, j6, (byte) (((int) (value >> 16)) & 255));
            byte[] bArr7 = this.buffer;
            long j7 = this.pos;
            this.pos = j7 - 1;
            UnsafeUtil.putByte(bArr7, j7, (byte) (((int) (value >> 8)) & 255));
            byte[] bArr8 = this.buffer;
            long j8 = this.pos;
            this.pos = j8 - 1;
            UnsafeUtil.putByte(bArr8, j8, (byte) (((int) value) & 255));
        }

        /* JADX WARN: Code duplicated, block: B:17:0x0048  */
        /* JADX WARN: Code duplicated, block: B:19:0x004c  */
        /* JADX WARN: Code duplicated, block: B:21:0x0054  */
        /* JADX WARN: Code duplicated, block: B:22:0x0073  */
        /* JADX WARN: Code duplicated, block: B:24:0x0078  */
        /* JADX WARN: Code duplicated, block: B:26:0x007d  */
        /* JADX WARN: Code duplicated, block: B:28:0x0086  */
        /* JADX WARN: Code duplicated, block: B:29:0x00b5  */
        /* JADX WARN: Code duplicated, block: B:31:0x00c0 A[DONT_INVERT] */
        /* JADX WARN: Code duplicated, block: B:32:0x00c2  */
        /* JADX WARN: Code duplicated, block: B:34:0x00ce  */
        /* JADX WARN: Code duplicated, block: B:37:0x011e  */
        /* JADX WARN: Code duplicated, block: B:43:0x0116 A[SYNTHETIC] */
        /* JADX WARN: Code duplicated, block: B:44:0x0116 A[SYNTHETIC] */
        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeString(String in) {
            long j;
            char cCharAt;
            long j2;
            char cCharAt2;
            requireSpace(in.length());
            int length = in.length();
            while (true) {
                length--;
                if (length < 0 || (cCharAt2 = in.charAt(length)) >= 128) {
                    break;
                }
                byte[] bArr = this.buffer;
                long j3 = this.pos;
                this.pos = j3 - 1;
                UnsafeUtil.putByte(bArr, j3, (byte) cCharAt2);
            }
            if (length == -1) {
                return;
            }
            while (length >= 0) {
                char cCharAt3 = in.charAt(length);
                if (cCharAt3 < 128) {
                    long j4 = this.pos;
                    if (j4 > this.offsetMinusOne) {
                        byte[] bArr2 = this.buffer;
                        this.pos = j4 - 1;
                        UnsafeUtil.putByte(bArr2, j4, (byte) cCharAt3);
                    } else if (cCharAt3 < 2048) {
                        j2 = this.pos;
                        if (j2 > this.offset) {
                            byte[] bArr3 = this.buffer;
                            this.pos = j2 - 1;
                            UnsafeUtil.putByte(bArr3, j2, (byte) ((cCharAt3 & '?') | 128));
                            byte[] bArr4 = this.buffer;
                            long j5 = this.pos;
                            this.pos = j5 - 1;
                            UnsafeUtil.putByte(bArr4, j5, (byte) ((cCharAt3 >>> 6) | 960));
                        } else if (cCharAt3 >= 55296 || 57343 < cCharAt3) {
                            j = this.pos;
                            if (j > this.offset + 1) {
                                byte[] bArr5 = this.buffer;
                                this.pos = j - 1;
                                UnsafeUtil.putByte(bArr5, j, (byte) ((cCharAt3 & '?') | 128));
                                byte[] bArr6 = this.buffer;
                                long j6 = this.pos;
                                this.pos = j6 - 1;
                                UnsafeUtil.putByte(bArr6, j6, (byte) (((cCharAt3 >>> 6) & 63) | 128));
                                byte[] bArr7 = this.buffer;
                                long j7 = this.pos;
                                this.pos = j7 - 1;
                                UnsafeUtil.putByte(bArr7, j7, (byte) ((cCharAt3 >>> '\f') | 480));
                            } else {
                                if (this.pos > this.offset + 2) {
                                    if (length != 0) {
                                        cCharAt = in.charAt(length - 1);
                                        if (Character.isSurrogatePair(cCharAt, cCharAt3)) {
                                            length--;
                                            int codePoint = Character.toCodePoint(cCharAt, cCharAt3);
                                            byte[] bArr8 = this.buffer;
                                            long j8 = this.pos;
                                            this.pos = j8 - 1;
                                            UnsafeUtil.putByte(bArr8, j8, (byte) ((codePoint & 63) | 128));
                                            byte[] bArr9 = this.buffer;
                                            long j9 = this.pos;
                                            this.pos = j9 - 1;
                                            UnsafeUtil.putByte(bArr9, j9, (byte) (((codePoint >>> 6) & 63) | 128));
                                            byte[] bArr10 = this.buffer;
                                            long j10 = this.pos;
                                            this.pos = j10 - 1;
                                            UnsafeUtil.putByte(bArr10, j10, (byte) (((codePoint >>> 12) & 63) | 128));
                                            byte[] bArr11 = this.buffer;
                                            long j11 = this.pos;
                                            this.pos = j11 - 1;
                                            UnsafeUtil.putByte(bArr11, j11, (byte) ((codePoint >>> 18) | 240));
                                        }
                                    }
                                    throw new Utf8.UnpairedSurrogateException(length - 1, length);
                                }
                                requireSpace(length);
                                length++;
                            }
                        } else {
                            if (this.pos > this.offset + 2) {
                                if (length != 0) {
                                    cCharAt = in.charAt(length - 1);
                                    if (Character.isSurrogatePair(cCharAt, cCharAt3)) {
                                        length--;
                                        int codePoint2 = Character.toCodePoint(cCharAt, cCharAt3);
                                        byte[] bArr12 = this.buffer;
                                        long j12 = this.pos;
                                        this.pos = j12 - 1;
                                        UnsafeUtil.putByte(bArr12, j12, (byte) ((codePoint2 & 63) | 128));
                                        byte[] bArr13 = this.buffer;
                                        long j13 = this.pos;
                                        this.pos = j13 - 1;
                                        UnsafeUtil.putByte(bArr13, j13, (byte) (((codePoint2 >>> 6) & 63) | 128));
                                        byte[] bArr14 = this.buffer;
                                        long j14 = this.pos;
                                        this.pos = j14 - 1;
                                        UnsafeUtil.putByte(bArr14, j14, (byte) (((codePoint2 >>> 12) & 63) | 128));
                                        byte[] bArr15 = this.buffer;
                                        long j15 = this.pos;
                                        this.pos = j15 - 1;
                                        UnsafeUtil.putByte(bArr15, j15, (byte) ((codePoint2 >>> 18) | 240));
                                    }
                                }
                                throw new Utf8.UnpairedSurrogateException(length - 1, length);
                            }
                            requireSpace(length);
                            length++;
                        }
                    } else if (cCharAt3 >= 55296) {
                        j = this.pos;
                        if (j > this.offset + 1) {
                            byte[] bArr16 = this.buffer;
                            this.pos = j - 1;
                            UnsafeUtil.putByte(bArr16, j, (byte) ((cCharAt3 & '?') | 128));
                            byte[] bArr17 = this.buffer;
                            long j16 = this.pos;
                            this.pos = j16 - 1;
                            UnsafeUtil.putByte(bArr17, j16, (byte) (((cCharAt3 >>> 6) & 63) | 128));
                            byte[] bArr18 = this.buffer;
                            long j17 = this.pos;
                            this.pos = j17 - 1;
                            UnsafeUtil.putByte(bArr18, j17, (byte) ((cCharAt3 >>> '\f') | 480));
                        } else {
                            if (this.pos > this.offset + 2) {
                                if (length != 0) {
                                    cCharAt = in.charAt(length - 1);
                                    if (Character.isSurrogatePair(cCharAt, cCharAt3)) {
                                        length--;
                                        int codePoint3 = Character.toCodePoint(cCharAt, cCharAt3);
                                        byte[] bArr19 = this.buffer;
                                        long j18 = this.pos;
                                        this.pos = j18 - 1;
                                        UnsafeUtil.putByte(bArr19, j18, (byte) ((codePoint3 & 63) | 128));
                                        byte[] bArr110 = this.buffer;
                                        long j19 = this.pos;
                                        this.pos = j19 - 1;
                                        UnsafeUtil.putByte(bArr110, j19, (byte) (((codePoint3 >>> 6) & 63) | 128));
                                        byte[] bArr111 = this.buffer;
                                        long j110 = this.pos;
                                        this.pos = j110 - 1;
                                        UnsafeUtil.putByte(bArr111, j110, (byte) (((codePoint3 >>> 12) & 63) | 128));
                                        byte[] bArr112 = this.buffer;
                                        long j111 = this.pos;
                                        this.pos = j111 - 1;
                                        UnsafeUtil.putByte(bArr112, j111, (byte) ((codePoint3 >>> 18) | 240));
                                    }
                                }
                                throw new Utf8.UnpairedSurrogateException(length - 1, length);
                            }
                            requireSpace(length);
                            length++;
                        }
                    } else {
                        j = this.pos;
                        if (j > this.offset + 1) {
                            byte[] bArr113 = this.buffer;
                            this.pos = j - 1;
                            UnsafeUtil.putByte(bArr113, j, (byte) ((cCharAt3 & '?') | 128));
                            byte[] bArr114 = this.buffer;
                            long j112 = this.pos;
                            this.pos = j112 - 1;
                            UnsafeUtil.putByte(bArr114, j112, (byte) (((cCharAt3 >>> 6) & 63) | 128));
                            byte[] bArr115 = this.buffer;
                            long j113 = this.pos;
                            this.pos = j113 - 1;
                            UnsafeUtil.putByte(bArr115, j113, (byte) ((cCharAt3 >>> '\f') | 480));
                        } else {
                            if (this.pos > this.offset + 2) {
                                if (length != 0) {
                                    cCharAt = in.charAt(length - 1);
                                    if (Character.isSurrogatePair(cCharAt, cCharAt3)) {
                                        length--;
                                        int codePoint4 = Character.toCodePoint(cCharAt, cCharAt3);
                                        byte[] bArr116 = this.buffer;
                                        long j114 = this.pos;
                                        this.pos = j114 - 1;
                                        UnsafeUtil.putByte(bArr116, j114, (byte) ((codePoint4 & 63) | 128));
                                        byte[] bArr117 = this.buffer;
                                        long j115 = this.pos;
                                        this.pos = j115 - 1;
                                        UnsafeUtil.putByte(bArr117, j115, (byte) (((codePoint4 >>> 6) & 63) | 128));
                                        byte[] bArr118 = this.buffer;
                                        long j116 = this.pos;
                                        this.pos = j116 - 1;
                                        UnsafeUtil.putByte(bArr118, j116, (byte) (((codePoint4 >>> 12) & 63) | 128));
                                        byte[] bArr119 = this.buffer;
                                        long j117 = this.pos;
                                        this.pos = j117 - 1;
                                        UnsafeUtil.putByte(bArr119, j117, (byte) ((codePoint4 >>> 18) | 240));
                                    }
                                }
                                throw new Utf8.UnpairedSurrogateException(length - 1, length);
                            }
                            requireSpace(length);
                            length++;
                        }
                    }
                } else if (cCharAt3 < 2048) {
                    j2 = this.pos;
                    if (j2 > this.offset) {
                        byte[] bArr20 = this.buffer;
                        this.pos = j2 - 1;
                        UnsafeUtil.putByte(bArr20, j2, (byte) ((cCharAt3 & '?') | 128));
                        byte[] bArr21 = this.buffer;
                        long j20 = this.pos;
                        this.pos = j20 - 1;
                        UnsafeUtil.putByte(bArr21, j20, (byte) ((cCharAt3 >>> 6) | 960));
                    } else if (cCharAt3 >= 55296) {
                        j = this.pos;
                        if (j > this.offset + 1) {
                            byte[] bArr1110 = this.buffer;
                            this.pos = j - 1;
                            UnsafeUtil.putByte(bArr1110, j, (byte) ((cCharAt3 & '?') | 128));
                            byte[] bArr1111 = this.buffer;
                            long j118 = this.pos;
                            this.pos = j118 - 1;
                            UnsafeUtil.putByte(bArr1111, j118, (byte) (((cCharAt3 >>> 6) & 63) | 128));
                            byte[] bArr1112 = this.buffer;
                            long j119 = this.pos;
                            this.pos = j119 - 1;
                            UnsafeUtil.putByte(bArr1112, j119, (byte) ((cCharAt3 >>> '\f') | 480));
                        } else {
                            if (this.pos > this.offset + 2) {
                                if (length != 0) {
                                    cCharAt = in.charAt(length - 1);
                                    if (Character.isSurrogatePair(cCharAt, cCharAt3)) {
                                        length--;
                                        int codePoint5 = Character.toCodePoint(cCharAt, cCharAt3);
                                        byte[] bArr1113 = this.buffer;
                                        long j1110 = this.pos;
                                        this.pos = j1110 - 1;
                                        UnsafeUtil.putByte(bArr1113, j1110, (byte) ((codePoint5 & 63) | 128));
                                        byte[] bArr1114 = this.buffer;
                                        long j1111 = this.pos;
                                        this.pos = j1111 - 1;
                                        UnsafeUtil.putByte(bArr1114, j1111, (byte) (((codePoint5 >>> 6) & 63) | 128));
                                        byte[] bArr1115 = this.buffer;
                                        long j1112 = this.pos;
                                        this.pos = j1112 - 1;
                                        UnsafeUtil.putByte(bArr1115, j1112, (byte) (((codePoint5 >>> 12) & 63) | 128));
                                        byte[] bArr1116 = this.buffer;
                                        long j1113 = this.pos;
                                        this.pos = j1113 - 1;
                                        UnsafeUtil.putByte(bArr1116, j1113, (byte) ((codePoint5 >>> 18) | 240));
                                    }
                                }
                                throw new Utf8.UnpairedSurrogateException(length - 1, length);
                            }
                            requireSpace(length);
                            length++;
                        }
                    } else {
                        j = this.pos;
                        if (j > this.offset + 1) {
                            byte[] bArr1117 = this.buffer;
                            this.pos = j - 1;
                            UnsafeUtil.putByte(bArr1117, j, (byte) ((cCharAt3 & '?') | 128));
                            byte[] bArr1118 = this.buffer;
                            long j1114 = this.pos;
                            this.pos = j1114 - 1;
                            UnsafeUtil.putByte(bArr1118, j1114, (byte) (((cCharAt3 >>> 6) & 63) | 128));
                            byte[] bArr1119 = this.buffer;
                            long j1115 = this.pos;
                            this.pos = j1115 - 1;
                            UnsafeUtil.putByte(bArr1119, j1115, (byte) ((cCharAt3 >>> '\f') | 480));
                        } else {
                            if (this.pos > this.offset + 2) {
                                if (length != 0) {
                                    cCharAt = in.charAt(length - 1);
                                    if (Character.isSurrogatePair(cCharAt, cCharAt3)) {
                                        length--;
                                        int codePoint6 = Character.toCodePoint(cCharAt, cCharAt3);
                                        byte[] bArr11110 = this.buffer;
                                        long j1116 = this.pos;
                                        this.pos = j1116 - 1;
                                        UnsafeUtil.putByte(bArr11110, j1116, (byte) ((codePoint6 & 63) | 128));
                                        byte[] bArr11111 = this.buffer;
                                        long j1117 = this.pos;
                                        this.pos = j1117 - 1;
                                        UnsafeUtil.putByte(bArr11111, j1117, (byte) (((codePoint6 >>> 6) & 63) | 128));
                                        byte[] bArr11112 = this.buffer;
                                        long j1118 = this.pos;
                                        this.pos = j1118 - 1;
                                        UnsafeUtil.putByte(bArr11112, j1118, (byte) (((codePoint6 >>> 12) & 63) | 128));
                                        byte[] bArr11113 = this.buffer;
                                        long j1119 = this.pos;
                                        this.pos = j1119 - 1;
                                        UnsafeUtil.putByte(bArr11113, j1119, (byte) ((codePoint6 >>> 18) | 240));
                                    }
                                }
                                throw new Utf8.UnpairedSurrogateException(length - 1, length);
                            }
                            requireSpace(length);
                            length++;
                        }
                    }
                } else if (cCharAt3 >= 55296) {
                    j = this.pos;
                    if (j > this.offset + 1) {
                        byte[] bArr11114 = this.buffer;
                        this.pos = j - 1;
                        UnsafeUtil.putByte(bArr11114, j, (byte) ((cCharAt3 & '?') | 128));
                        byte[] bArr11115 = this.buffer;
                        long j11110 = this.pos;
                        this.pos = j11110 - 1;
                        UnsafeUtil.putByte(bArr11115, j11110, (byte) (((cCharAt3 >>> 6) & 63) | 128));
                        byte[] bArr11116 = this.buffer;
                        long j11111 = this.pos;
                        this.pos = j11111 - 1;
                        UnsafeUtil.putByte(bArr11116, j11111, (byte) ((cCharAt3 >>> '\f') | 480));
                    } else {
                        if (this.pos > this.offset + 2) {
                            if (length != 0) {
                                cCharAt = in.charAt(length - 1);
                                if (Character.isSurrogatePair(cCharAt, cCharAt3)) {
                                    length--;
                                    int codePoint7 = Character.toCodePoint(cCharAt, cCharAt3);
                                    byte[] bArr11117 = this.buffer;
                                    long j11112 = this.pos;
                                    this.pos = j11112 - 1;
                                    UnsafeUtil.putByte(bArr11117, j11112, (byte) ((codePoint7 & 63) | 128));
                                    byte[] bArr11118 = this.buffer;
                                    long j11113 = this.pos;
                                    this.pos = j11113 - 1;
                                    UnsafeUtil.putByte(bArr11118, j11113, (byte) (((codePoint7 >>> 6) & 63) | 128));
                                    byte[] bArr11119 = this.buffer;
                                    long j11114 = this.pos;
                                    this.pos = j11114 - 1;
                                    UnsafeUtil.putByte(bArr11119, j11114, (byte) (((codePoint7 >>> 12) & 63) | 128));
                                    byte[] bArr111110 = this.buffer;
                                    long j11115 = this.pos;
                                    this.pos = j11115 - 1;
                                    UnsafeUtil.putByte(bArr111110, j11115, (byte) ((codePoint7 >>> 18) | 240));
                                }
                            }
                            throw new Utf8.UnpairedSurrogateException(length - 1, length);
                        }
                        requireSpace(length);
                        length++;
                    }
                } else {
                    j = this.pos;
                    if (j > this.offset + 1) {
                        byte[] bArr111111 = this.buffer;
                        this.pos = j - 1;
                        UnsafeUtil.putByte(bArr111111, j, (byte) ((cCharAt3 & '?') | 128));
                        byte[] bArr111112 = this.buffer;
                        long j11116 = this.pos;
                        this.pos = j11116 - 1;
                        UnsafeUtil.putByte(bArr111112, j11116, (byte) (((cCharAt3 >>> 6) & 63) | 128));
                        byte[] bArr111113 = this.buffer;
                        long j11117 = this.pos;
                        this.pos = j11117 - 1;
                        UnsafeUtil.putByte(bArr111113, j11117, (byte) ((cCharAt3 >>> '\f') | 480));
                    } else {
                        if (this.pos > this.offset + 2) {
                            if (length != 0) {
                                cCharAt = in.charAt(length - 1);
                                if (Character.isSurrogatePair(cCharAt, cCharAt3)) {
                                    length--;
                                    int codePoint8 = Character.toCodePoint(cCharAt, cCharAt3);
                                    byte[] bArr111114 = this.buffer;
                                    long j11118 = this.pos;
                                    this.pos = j11118 - 1;
                                    UnsafeUtil.putByte(bArr111114, j11118, (byte) ((codePoint8 & 63) | 128));
                                    byte[] bArr111115 = this.buffer;
                                    long j11119 = this.pos;
                                    this.pos = j11119 - 1;
                                    UnsafeUtil.putByte(bArr111115, j11119, (byte) (((codePoint8 >>> 6) & 63) | 128));
                                    byte[] bArr111116 = this.buffer;
                                    long j111110 = this.pos;
                                    this.pos = j111110 - 1;
                                    UnsafeUtil.putByte(bArr111116, j111110, (byte) (((codePoint8 >>> 12) & 63) | 128));
                                    byte[] bArr111117 = this.buffer;
                                    long j111111 = this.pos;
                                    this.pos = j111111 - 1;
                                    UnsafeUtil.putByte(bArr111117, j111111, (byte) ((codePoint8 >>> 18) | 240));
                                }
                            }
                            throw new Utf8.UnpairedSurrogateException(length - 1, length);
                        }
                        requireSpace(length);
                        length++;
                    }
                }
                length--;
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void write(byte value) {
            byte[] bArr = this.buffer;
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(bArr, j, value);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void write(byte[] value, int offset, int length) {
            if (offset < 0 || offset + length > value.length) {
                throw new ArrayIndexOutOfBoundsException(String.format("value.length=%d, offset=%d, length=%d", Integer.valueOf(value.length), Integer.valueOf(offset), Integer.valueOf(length)));
            }
            requireSpace(length);
            this.pos -= (long) length;
            System.arraycopy(value, offset, this.buffer, arrayPos() + 1, length);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void writeLazy(byte[] value, int offset, int length) {
            if (offset < 0 || offset + length > value.length) {
                throw new ArrayIndexOutOfBoundsException(String.format("value.length=%d, offset=%d, length=%d", Integer.valueOf(value.length), Integer.valueOf(offset), Integer.valueOf(length)));
            }
            if (spaceLeft() < length) {
                this.totalDoneBytes += length;
                this.buffers.addFirst(AllocatedBuffer.wrap(value, offset, length));
                nextBuffer();
            } else {
                this.pos -= (long) length;
                System.arraycopy(value, offset, this.buffer, arrayPos() + 1, length);
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void write(ByteBuffer value) {
            int iRemaining = value.remaining();
            requireSpace(iRemaining);
            this.pos -= (long) iRemaining;
            value.get(this.buffer, arrayPos() + 1, iRemaining);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void writeLazy(ByteBuffer value) {
            int iRemaining = value.remaining();
            if (spaceLeft() < iRemaining) {
                this.totalDoneBytes += iRemaining;
                this.buffers.addFirst(AllocatedBuffer.wrap(value));
                nextBuffer();
            }
            this.pos -= (long) iRemaining;
            value.get(this.buffer, arrayPos() + 1, iRemaining);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void requireSpace(int size) {
            if (spaceLeft() < size) {
                nextBuffer(size);
            }
        }
    }

    private static final class SafeDirectWriter extends BinaryWriter {
        private ByteBuffer buffer;
        private int limitMinusOne;
        private int pos;

        SafeDirectWriter(BufferAllocator alloc, int chunkSize) {
            super(alloc, chunkSize, null);
            nextBuffer();
        }

        private void nextBuffer() {
            nextBuffer(newDirectBuffer());
        }

        private void nextBuffer(int capacity) {
            nextBuffer(newDirectBuffer(capacity));
        }

        private void nextBuffer(AllocatedBuffer allocatedBuffer) {
            if (!allocatedBuffer.hasNioBuffer()) {
                throw new RuntimeException("Allocated buffer does not have NIO buffer");
            }
            ByteBuffer byteBufferNioBuffer = allocatedBuffer.nioBuffer();
            if (!byteBufferNioBuffer.isDirect()) {
                throw new RuntimeException("Allocator returned non-direct buffer");
            }
            finishCurrentBuffer();
            this.buffers.addFirst(allocatedBuffer);
            this.buffer = byteBufferNioBuffer;
            Java8Compatibility.limit(byteBufferNioBuffer, byteBufferNioBuffer.capacity());
            Java8Compatibility.position(this.buffer, 0);
            this.buffer.order(ByteOrder.LITTLE_ENDIAN);
            int iLimit = this.buffer.limit() - 1;
            this.limitMinusOne = iLimit;
            this.pos = iLimit;
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        public int getTotalBytesWritten() {
            return this.totalDoneBytes + bytesWrittenToCurrentBuffer();
        }

        private int bytesWrittenToCurrentBuffer() {
            return this.limitMinusOne - this.pos;
        }

        private int spaceLeft() {
            return this.pos + 1;
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void finishCurrentBuffer() {
            if (this.buffer != null) {
                this.totalDoneBytes += bytesWrittenToCurrentBuffer();
                Java8Compatibility.position(this.buffer, this.pos + 1);
                this.buffer = null;
                this.pos = 0;
                this.limitMinusOne = 0;
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeUInt32(int fieldNumber, int value) {
            requireSpace(10);
            writeVarint32(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeInt32(int fieldNumber, int value) {
            requireSpace(15);
            writeInt32(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeSInt32(int fieldNumber, int value) {
            requireSpace(10);
            writeSInt32(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeFixed32(int fieldNumber, int value) {
            requireSpace(9);
            writeFixed32(value);
            writeTag(fieldNumber, 5);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeUInt64(int fieldNumber, long value) {
            requireSpace(15);
            writeVarint64(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeSInt64(int fieldNumber, long value) {
            requireSpace(15);
            writeSInt64(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeFixed64(int fieldNumber, long value) {
            requireSpace(13);
            writeFixed64(value);
            writeTag(fieldNumber, 1);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeBool(int i, boolean z) {
            requireSpace(6);
            write(z ? (byte) 1 : (byte) 0);
            writeTag(i, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeString(int fieldNumber, String value) {
            int totalBytesWritten = getTotalBytesWritten();
            writeString(value);
            int totalBytesWritten2 = getTotalBytesWritten() - totalBytesWritten;
            requireSpace(10);
            writeVarint32(totalBytesWritten2);
            writeTag(fieldNumber, 2);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeBytes(int fieldNumber, ByteString value) {
            try {
                value.writeToReverse(this);
                requireSpace(10);
                writeVarint32(value.size());
                writeTag(fieldNumber, 2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeMessage(int fieldNumber, Object value) throws IOException {
            int totalBytesWritten = getTotalBytesWritten();
            Protobuf.getInstance().writeTo(value, this);
            int totalBytesWritten2 = getTotalBytesWritten() - totalBytesWritten;
            requireSpace(10);
            writeVarint32(totalBytesWritten2);
            writeTag(fieldNumber, 2);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeMessage(int fieldNumber, Object value, Schema schema) throws IOException {
            int totalBytesWritten = getTotalBytesWritten();
            schema.writeTo(value, this);
            int totalBytesWritten2 = getTotalBytesWritten() - totalBytesWritten;
            requireSpace(10);
            writeVarint32(totalBytesWritten2);
            writeTag(fieldNumber, 2);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        @Deprecated
        public void writeGroup(int fieldNumber, Object value) throws IOException {
            writeTag(fieldNumber, 4);
            Protobuf.getInstance().writeTo(value, this);
            writeTag(fieldNumber, 3);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeGroup(int fieldNumber, Object value, Schema schema) throws IOException {
            writeTag(fieldNumber, 4);
            schema.writeTo(value, this);
            writeTag(fieldNumber, 3);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        @Deprecated
        public void writeStartGroup(int fieldNumber) {
            writeTag(fieldNumber, 3);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        @Deprecated
        public void writeEndGroup(int fieldNumber) {
            writeTag(fieldNumber, 4);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeInt32(int value) {
            if (value >= 0) {
                writeVarint32(value);
            } else {
                writeVarint64(value);
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeSInt32(int value) {
            writeVarint32(CodedOutputStream.encodeZigZag32(value));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeSInt64(long value) {
            writeVarint64(CodedOutputStream.encodeZigZag64(value));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeBool(boolean z) {
            write(z ? (byte) 1 : (byte) 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeTag(int fieldNumber, int wireType) {
            writeVarint32(WireFormat.makeTag(fieldNumber, wireType));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeVarint32(int value) {
            if ((value & (-128)) == 0) {
                writeVarint32OneByte(value);
                return;
            }
            if ((value & (-16384)) == 0) {
                writeVarint32TwoBytes(value);
                return;
            }
            if (((-2097152) & value) == 0) {
                writeVarint32ThreeBytes(value);
            } else if (((-268435456) & value) == 0) {
                writeVarint32FourBytes(value);
            } else {
                writeVarint32FiveBytes(value);
            }
        }

        private void writeVarint32OneByte(int value) {
            ByteBuffer byteBuffer = this.buffer;
            int i = this.pos;
            this.pos = i - 1;
            byteBuffer.put(i, (byte) value);
        }

        private void writeVarint32TwoBytes(int value) {
            int i = this.pos;
            this.pos = i - 2;
            this.buffer.putShort(i - 1, (short) ((value & WorkQueueKt.MASK) | 128 | ((value & 16256) << 1)));
        }

        private void writeVarint32ThreeBytes(int value) {
            int i = this.pos - 3;
            this.pos = i;
            this.buffer.putInt(i, (((value & WorkQueueKt.MASK) | 128) << 8) | ((2080768 & value) << 10) | (((value & 16256) | 16384) << 9));
        }

        private void writeVarint32FourBytes(int value) {
            int i = this.pos;
            this.pos = i - 4;
            this.buffer.putInt(i - 3, (value & WorkQueueKt.MASK) | 128 | ((266338304 & value) << 3) | (((2080768 & value) | 2097152) << 2) | (((value & 16256) | 16384) << 1));
        }

        private void writeVarint32FiveBytes(int value) {
            ByteBuffer byteBuffer = this.buffer;
            int i = this.pos;
            this.pos = i - 1;
            byteBuffer.put(i, (byte) (value >>> 28));
            int i2 = this.pos;
            this.pos = i2 - 4;
            this.buffer.putInt(i2 - 3, (value & WorkQueueKt.MASK) | 128 | ((((value >>> 21) & WorkQueueKt.MASK) | 128) << 24) | ((((value >>> 14) & WorkQueueKt.MASK) | 128) << 16) | ((((value >>> 7) & WorkQueueKt.MASK) | 128) << 8));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeVarint64(long value) {
            switch (BinaryWriter.computeUInt64SizeNoTag(value)) {
                case 1:
                    writeVarint64OneByte(value);
                    break;
                case 2:
                    writeVarint64TwoBytes(value);
                    break;
                case 3:
                    writeVarint64ThreeBytes(value);
                    break;
                case 4:
                    writeVarint64FourBytes(value);
                    break;
                case 5:
                    writeVarint64FiveBytes(value);
                    break;
                case 6:
                    writeVarint64SixBytes(value);
                    break;
                case 7:
                    writeVarint64SevenBytes(value);
                    break;
                case 8:
                    writeVarint64EightBytes(value);
                    break;
                case 9:
                    writeVarint64NineBytes(value);
                    break;
                case 10:
                    writeVarint64TenBytes(value);
                    break;
            }
        }

        private void writeVarint64OneByte(long value) {
            writeVarint32OneByte((int) value);
        }

        private void writeVarint64TwoBytes(long value) {
            writeVarint32TwoBytes((int) value);
        }

        private void writeVarint64ThreeBytes(long value) {
            writeVarint32ThreeBytes((int) value);
        }

        private void writeVarint64FourBytes(long value) {
            writeVarint32FourBytes((int) value);
        }

        private void writeVarint64FiveBytes(long value) {
            int i = this.pos;
            this.pos = i - 5;
            this.buffer.putLong(i - 7, (((value & 127) | 128) << 24) | ((34091302912L & value) << 28) | (((266338304 & value) | 268435456) << 27) | (((2080768 & value) | 2097152) << 26) | (((16256 & value) | 16384) << 25));
        }

        private void writeVarint64SixBytes(long value) {
            int i = this.pos;
            this.pos = i - 6;
            this.buffer.putLong(i - 7, (((value & 127) | 128) << 16) | ((4363686772736L & value) << 21) | (((34091302912L & value) | 34359738368L) << 20) | (((266338304 & value) | 268435456) << 19) | (((2080768 & value) | 2097152) << 18) | (((16256 & value) | 16384) << 17));
        }

        private void writeVarint64SevenBytes(long value) {
            int i = this.pos - 7;
            this.pos = i;
            this.buffer.putLong(i, (((value & 127) | 128) << 8) | ((558551906910208L & value) << 14) | (((4363686772736L & value) | 4398046511104L) << 13) | (((34091302912L & value) | 34359738368L) << 12) | (((266338304 & value) | 268435456) << 11) | (((2080768 & value) | 2097152) << 10) | (((16256 & value) | 16384) << 9));
        }

        private void writeVarint64EightBytes(long value) {
            int i = this.pos;
            this.pos = i - 8;
            this.buffer.putLong(i - 7, (value & 127) | 128 | ((71494644084506624L & value) << 7) | (((558551906910208L & value) | 562949953421312L) << 6) | (((4363686772736L & value) | 4398046511104L) << 5) | (((34091302912L & value) | 34359738368L) << 4) | (((266338304 & value) | 268435456) << 3) | (((2080768 & value) | 2097152) << 2) | (((16256 & value) | 16384) << 1));
        }

        private void writeVarint64EightBytesWithSign(long value) {
            int i = this.pos;
            this.pos = i - 8;
            this.buffer.putLong(i - 7, (value & 127) | 128 | (((71494644084506624L & value) | 72057594037927936L) << 7) | (((558551906910208L & value) | 562949953421312L) << 6) | (((4363686772736L & value) | 4398046511104L) << 5) | (((34091302912L & value) | 34359738368L) << 4) | (((266338304 & value) | 268435456) << 3) | (((2080768 & value) | 2097152) << 2) | (((16256 & value) | 16384) << 1));
        }

        private void writeVarint64NineBytes(long value) {
            ByteBuffer byteBuffer = this.buffer;
            int i = this.pos;
            this.pos = i - 1;
            byteBuffer.put(i, (byte) (value >>> 56));
            writeVarint64EightBytesWithSign(value & 72057594037927935L);
        }

        private void writeVarint64TenBytes(long value) {
            ByteBuffer byteBuffer = this.buffer;
            int i = this.pos;
            this.pos = i - 1;
            byteBuffer.put(i, (byte) (value >>> 63));
            ByteBuffer byteBuffer2 = this.buffer;
            int i2 = this.pos;
            this.pos = i2 - 1;
            byteBuffer2.put(i2, (byte) (((value >>> 56) & 127) | 128));
            writeVarint64EightBytesWithSign(value & 72057594037927935L);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeFixed32(int value) {
            int i = this.pos;
            this.pos = i - 4;
            this.buffer.putInt(i - 3, value);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeFixed64(long value) {
            int i = this.pos;
            this.pos = i - 8;
            this.buffer.putLong(i - 7, value);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeString(String in) {
            int i;
            int i2;
            int i3;
            char cCharAt;
            requireSpace(in.length());
            int length = in.length() - 1;
            this.pos -= length;
            while (length >= 0 && (cCharAt = in.charAt(length)) < 128) {
                this.buffer.put(this.pos + length, (byte) cCharAt);
                length--;
            }
            if (length == -1) {
                this.pos--;
                return;
            }
            this.pos += length;
            while (length >= 0) {
                char cCharAt2 = in.charAt(length);
                if (cCharAt2 < 128 && (i3 = this.pos) >= 0) {
                    ByteBuffer byteBuffer = this.buffer;
                    this.pos = i3 - 1;
                    byteBuffer.put(i3, (byte) cCharAt2);
                } else if (cCharAt2 < 2048 && (i2 = this.pos) > 0) {
                    ByteBuffer byteBuffer2 = this.buffer;
                    this.pos = i2 - 1;
                    byteBuffer2.put(i2, (byte) ((cCharAt2 & '?') | 128));
                    ByteBuffer byteBuffer3 = this.buffer;
                    int i4 = this.pos;
                    this.pos = i4 - 1;
                    byteBuffer3.put(i4, (byte) ((cCharAt2 >>> 6) | 960));
                } else if ((cCharAt2 < 55296 || 57343 < cCharAt2) && (i = this.pos) > 1) {
                    ByteBuffer byteBuffer4 = this.buffer;
                    this.pos = i - 1;
                    byteBuffer4.put(i, (byte) ((cCharAt2 & '?') | 128));
                    ByteBuffer byteBuffer5 = this.buffer;
                    int i5 = this.pos;
                    this.pos = i5 - 1;
                    byteBuffer5.put(i5, (byte) (((cCharAt2 >>> 6) & 63) | 128));
                    ByteBuffer byteBuffer6 = this.buffer;
                    int i6 = this.pos;
                    this.pos = i6 - 1;
                    byteBuffer6.put(i6, (byte) ((cCharAt2 >>> '\f') | 480));
                } else {
                    if (this.pos > 2) {
                        if (length != 0) {
                            char cCharAt3 = in.charAt(length - 1);
                            if (Character.isSurrogatePair(cCharAt3, cCharAt2)) {
                                length--;
                                int codePoint = Character.toCodePoint(cCharAt3, cCharAt2);
                                ByteBuffer byteBuffer7 = this.buffer;
                                int i7 = this.pos;
                                this.pos = i7 - 1;
                                byteBuffer7.put(i7, (byte) ((codePoint & 63) | 128));
                                ByteBuffer byteBuffer8 = this.buffer;
                                int i8 = this.pos;
                                this.pos = i8 - 1;
                                byteBuffer8.put(i8, (byte) (((codePoint >>> 6) & 63) | 128));
                                ByteBuffer byteBuffer9 = this.buffer;
                                int i9 = this.pos;
                                this.pos = i9 - 1;
                                byteBuffer9.put(i9, (byte) (((codePoint >>> 12) & 63) | 128));
                                ByteBuffer byteBuffer10 = this.buffer;
                                int i10 = this.pos;
                                this.pos = i10 - 1;
                                byteBuffer10.put(i10, (byte) ((codePoint >>> 18) | 240));
                            }
                        }
                        throw new Utf8.UnpairedSurrogateException(length - 1, length);
                    }
                    requireSpace(length);
                    length++;
                }
                length--;
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void write(byte value) {
            ByteBuffer byteBuffer = this.buffer;
            int i = this.pos;
            this.pos = i - 1;
            byteBuffer.put(i, value);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void write(byte[] value, int offset, int length) {
            if (spaceLeft() < length) {
                nextBuffer(length);
            }
            int i = this.pos - length;
            this.pos = i;
            Java8Compatibility.position(this.buffer, i + 1);
            this.buffer.put(value, offset, length);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void writeLazy(byte[] value, int offset, int length) {
            if (spaceLeft() < length) {
                this.totalDoneBytes += length;
                this.buffers.addFirst(AllocatedBuffer.wrap(value, offset, length));
                nextBuffer();
            } else {
                int i = this.pos - length;
                this.pos = i;
                Java8Compatibility.position(this.buffer, i + 1);
                this.buffer.put(value, offset, length);
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void write(ByteBuffer value) {
            int iRemaining = value.remaining();
            if (spaceLeft() < iRemaining) {
                nextBuffer(iRemaining);
            }
            int i = this.pos - iRemaining;
            this.pos = i;
            Java8Compatibility.position(this.buffer, i + 1);
            this.buffer.put(value);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void writeLazy(ByteBuffer value) {
            int iRemaining = value.remaining();
            if (spaceLeft() < iRemaining) {
                this.totalDoneBytes += iRemaining;
                this.buffers.addFirst(AllocatedBuffer.wrap(value));
                nextBuffer();
            } else {
                int i = this.pos - iRemaining;
                this.pos = i;
                Java8Compatibility.position(this.buffer, i + 1);
                this.buffer.put(value);
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void requireSpace(int size) {
            if (spaceLeft() < size) {
                nextBuffer(size);
            }
        }
    }

    private static final class UnsafeDirectWriter extends BinaryWriter {
        private ByteBuffer buffer;
        private long bufferOffset;
        private long limitMinusOne;
        private long pos;

        UnsafeDirectWriter(BufferAllocator alloc, int chunkSize) {
            super(alloc, chunkSize, null);
            nextBuffer();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static boolean isSupported() {
            return UnsafeUtil.hasUnsafeByteBufferOperations();
        }

        private void nextBuffer() {
            nextBuffer(newDirectBuffer());
        }

        private void nextBuffer(int capacity) {
            nextBuffer(newDirectBuffer(capacity));
        }

        private void nextBuffer(AllocatedBuffer allocatedBuffer) {
            if (!allocatedBuffer.hasNioBuffer()) {
                throw new RuntimeException("Allocated buffer does not have NIO buffer");
            }
            ByteBuffer byteBufferNioBuffer = allocatedBuffer.nioBuffer();
            if (!byteBufferNioBuffer.isDirect()) {
                throw new RuntimeException("Allocator returned non-direct buffer");
            }
            finishCurrentBuffer();
            this.buffers.addFirst(allocatedBuffer);
            this.buffer = byteBufferNioBuffer;
            Java8Compatibility.limit(byteBufferNioBuffer, byteBufferNioBuffer.capacity());
            Java8Compatibility.position(this.buffer, 0);
            long jAddressOffset = UnsafeUtil.addressOffset(this.buffer);
            this.bufferOffset = jAddressOffset;
            long jLimit = jAddressOffset + ((long) (this.buffer.limit() - 1));
            this.limitMinusOne = jLimit;
            this.pos = jLimit;
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        public int getTotalBytesWritten() {
            return this.totalDoneBytes + bytesWrittenToCurrentBuffer();
        }

        private int bytesWrittenToCurrentBuffer() {
            return (int) (this.limitMinusOne - this.pos);
        }

        private int spaceLeft() {
            return bufferPos() + 1;
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void finishCurrentBuffer() {
            if (this.buffer != null) {
                this.totalDoneBytes += bytesWrittenToCurrentBuffer();
                Java8Compatibility.position(this.buffer, bufferPos() + 1);
                this.buffer = null;
                this.pos = 0L;
                this.limitMinusOne = 0L;
            }
        }

        private int bufferPos() {
            return (int) (this.pos - this.bufferOffset);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeUInt32(int fieldNumber, int value) {
            requireSpace(10);
            writeVarint32(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeInt32(int fieldNumber, int value) {
            requireSpace(15);
            writeInt32(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeSInt32(int fieldNumber, int value) {
            requireSpace(10);
            writeSInt32(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeFixed32(int fieldNumber, int value) {
            requireSpace(9);
            writeFixed32(value);
            writeTag(fieldNumber, 5);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeUInt64(int fieldNumber, long value) {
            requireSpace(15);
            writeVarint64(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeSInt64(int fieldNumber, long value) {
            requireSpace(15);
            writeSInt64(value);
            writeTag(fieldNumber, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeFixed64(int fieldNumber, long value) {
            requireSpace(13);
            writeFixed64(value);
            writeTag(fieldNumber, 1);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeBool(int i, boolean z) {
            requireSpace(6);
            write(z ? (byte) 1 : (byte) 0);
            writeTag(i, 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeString(int fieldNumber, String value) {
            int totalBytesWritten = getTotalBytesWritten();
            writeString(value);
            int totalBytesWritten2 = getTotalBytesWritten() - totalBytesWritten;
            requireSpace(10);
            writeVarint32(totalBytesWritten2);
            writeTag(fieldNumber, 2);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeBytes(int fieldNumber, ByteString value) {
            try {
                value.writeToReverse(this);
                requireSpace(10);
                writeVarint32(value.size());
                writeTag(fieldNumber, 2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeMessage(int fieldNumber, Object value) throws IOException {
            int totalBytesWritten = getTotalBytesWritten();
            Protobuf.getInstance().writeTo(value, this);
            int totalBytesWritten2 = getTotalBytesWritten() - totalBytesWritten;
            requireSpace(10);
            writeVarint32(totalBytesWritten2);
            writeTag(fieldNumber, 2);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeMessage(int fieldNumber, Object value, Schema schema) throws IOException {
            int totalBytesWritten = getTotalBytesWritten();
            schema.writeTo(value, this);
            int totalBytesWritten2 = getTotalBytesWritten() - totalBytesWritten;
            requireSpace(10);
            writeVarint32(totalBytesWritten2);
            writeTag(fieldNumber, 2);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeGroup(int fieldNumber, Object value) throws IOException {
            writeTag(fieldNumber, 4);
            Protobuf.getInstance().writeTo(value, this);
            writeTag(fieldNumber, 3);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        public void writeGroup(int fieldNumber, Object value, Schema schema) throws IOException {
            writeTag(fieldNumber, 4);
            schema.writeTo(value, this);
            writeTag(fieldNumber, 3);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        @Deprecated
        public void writeStartGroup(int fieldNumber) {
            writeTag(fieldNumber, 3);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Writer
        @Deprecated
        public void writeEndGroup(int fieldNumber) {
            writeTag(fieldNumber, 4);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeInt32(int value) {
            if (value >= 0) {
                writeVarint32(value);
            } else {
                writeVarint64(value);
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeSInt32(int value) {
            writeVarint32(CodedOutputStream.encodeZigZag32(value));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeSInt64(long value) {
            writeVarint64(CodedOutputStream.encodeZigZag64(value));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeBool(boolean z) {
            write(z ? (byte) 1 : (byte) 0);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeTag(int fieldNumber, int wireType) {
            writeVarint32(WireFormat.makeTag(fieldNumber, wireType));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeVarint32(int value) {
            if ((value & (-128)) == 0) {
                writeVarint32OneByte(value);
                return;
            }
            if ((value & (-16384)) == 0) {
                writeVarint32TwoBytes(value);
                return;
            }
            if (((-2097152) & value) == 0) {
                writeVarint32ThreeBytes(value);
            } else if (((-268435456) & value) == 0) {
                writeVarint32FourBytes(value);
            } else {
                writeVarint32FiveBytes(value);
            }
        }

        private void writeVarint32OneByte(int value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, (byte) value);
        }

        private void writeVarint32TwoBytes(int value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, (byte) (value >>> 7));
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(j2, (byte) ((value & WorkQueueKt.MASK) | 128));
        }

        private void writeVarint32ThreeBytes(int value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, (byte) (value >>> 14));
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(j2, (byte) (((value >>> 7) & WorkQueueKt.MASK) | 128));
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(j3, (byte) ((value & WorkQueueKt.MASK) | 128));
        }

        private void writeVarint32FourBytes(int value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, (byte) (value >>> 21));
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(j2, (byte) (((value >>> 14) & WorkQueueKt.MASK) | 128));
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(j3, (byte) (((value >>> 7) & WorkQueueKt.MASK) | 128));
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(j4, (byte) ((value & WorkQueueKt.MASK) | 128));
        }

        private void writeVarint32FiveBytes(int value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, (byte) (value >>> 28));
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(j2, (byte) (((value >>> 21) & WorkQueueKt.MASK) | 128));
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(j3, (byte) (((value >>> 14) & WorkQueueKt.MASK) | 128));
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(j4, (byte) (((value >>> 7) & WorkQueueKt.MASK) | 128));
            long j5 = this.pos;
            this.pos = j5 - 1;
            UnsafeUtil.putByte(j5, (byte) ((value & WorkQueueKt.MASK) | 128));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeVarint64(long value) {
            switch (BinaryWriter.computeUInt64SizeNoTag(value)) {
                case 1:
                    writeVarint64OneByte(value);
                    break;
                case 2:
                    writeVarint64TwoBytes(value);
                    break;
                case 3:
                    writeVarint64ThreeBytes(value);
                    break;
                case 4:
                    writeVarint64FourBytes(value);
                    break;
                case 5:
                    writeVarint64FiveBytes(value);
                    break;
                case 6:
                    writeVarint64SixBytes(value);
                    break;
                case 7:
                    writeVarint64SevenBytes(value);
                    break;
                case 8:
                    writeVarint64EightBytes(value);
                    break;
                case 9:
                    writeVarint64NineBytes(value);
                    break;
                case 10:
                    writeVarint64TenBytes(value);
                    break;
            }
        }

        private void writeVarint64OneByte(long value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, (byte) value);
        }

        private void writeVarint64TwoBytes(long value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, (byte) (value >>> 7));
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(j2, (byte) ((((int) value) & WorkQueueKt.MASK) | 128));
        }

        private void writeVarint64ThreeBytes(long value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, (byte) (((int) value) >>> 14));
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(j2, (byte) (((value >>> 7) & 127) | 128));
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(j3, (byte) ((value & 127) | 128));
        }

        private void writeVarint64FourBytes(long value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, (byte) (value >>> 21));
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(j2, (byte) (((value >>> 14) & 127) | 128));
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(j3, (byte) (((value >>> 7) & 127) | 128));
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(j4, (byte) ((value & 127) | 128));
        }

        private void writeVarint64FiveBytes(long value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, (byte) (value >>> 28));
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(j2, (byte) (((value >>> 21) & 127) | 128));
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(j3, (byte) (((value >>> 14) & 127) | 128));
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(j4, (byte) (((value >>> 7) & 127) | 128));
            long j5 = this.pos;
            this.pos = j5 - 1;
            UnsafeUtil.putByte(j5, (byte) ((value & 127) | 128));
        }

        private void writeVarint64SixBytes(long value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, (byte) (value >>> 35));
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(j2, (byte) (((value >>> 28) & 127) | 128));
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(j3, (byte) (((value >>> 21) & 127) | 128));
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(j4, (byte) (((value >>> 14) & 127) | 128));
            long j5 = this.pos;
            this.pos = j5 - 1;
            UnsafeUtil.putByte(j5, (byte) (((value >>> 7) & 127) | 128));
            long j6 = this.pos;
            this.pos = j6 - 1;
            UnsafeUtil.putByte(j6, (byte) ((value & 127) | 128));
        }

        private void writeVarint64SevenBytes(long value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, (byte) (value >>> 42));
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(j2, (byte) (((value >>> 35) & 127) | 128));
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(j3, (byte) (((value >>> 28) & 127) | 128));
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(j4, (byte) (((value >>> 21) & 127) | 128));
            long j5 = this.pos;
            this.pos = j5 - 1;
            UnsafeUtil.putByte(j5, (byte) (((value >>> 14) & 127) | 128));
            long j6 = this.pos;
            this.pos = j6 - 1;
            UnsafeUtil.putByte(j6, (byte) (((value >>> 7) & 127) | 128));
            long j7 = this.pos;
            this.pos = j7 - 1;
            UnsafeUtil.putByte(j7, (byte) ((value & 127) | 128));
        }

        private void writeVarint64EightBytes(long value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, (byte) (value >>> 49));
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(j2, (byte) (((value >>> 42) & 127) | 128));
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(j3, (byte) (((value >>> 35) & 127) | 128));
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(j4, (byte) (((value >>> 28) & 127) | 128));
            long j5 = this.pos;
            this.pos = j5 - 1;
            UnsafeUtil.putByte(j5, (byte) (((value >>> 21) & 127) | 128));
            long j6 = this.pos;
            this.pos = j6 - 1;
            UnsafeUtil.putByte(j6, (byte) (((value >>> 14) & 127) | 128));
            long j7 = this.pos;
            this.pos = j7 - 1;
            UnsafeUtil.putByte(j7, (byte) (((value >>> 7) & 127) | 128));
            long j8 = this.pos;
            this.pos = j8 - 1;
            UnsafeUtil.putByte(j8, (byte) ((value & 127) | 128));
        }

        private void writeVarint64NineBytes(long value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, (byte) (value >>> 56));
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(j2, (byte) (((value >>> 49) & 127) | 128));
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(j3, (byte) (((value >>> 42) & 127) | 128));
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(j4, (byte) (((value >>> 35) & 127) | 128));
            long j5 = this.pos;
            this.pos = j5 - 1;
            UnsafeUtil.putByte(j5, (byte) (((value >>> 28) & 127) | 128));
            long j6 = this.pos;
            this.pos = j6 - 1;
            UnsafeUtil.putByte(j6, (byte) (((value >>> 21) & 127) | 128));
            long j7 = this.pos;
            this.pos = j7 - 1;
            UnsafeUtil.putByte(j7, (byte) (((value >>> 14) & 127) | 128));
            long j8 = this.pos;
            this.pos = j8 - 1;
            UnsafeUtil.putByte(j8, (byte) (((value >>> 7) & 127) | 128));
            long j9 = this.pos;
            this.pos = j9 - 1;
            UnsafeUtil.putByte(j9, (byte) ((value & 127) | 128));
        }

        private void writeVarint64TenBytes(long value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, (byte) (value >>> 63));
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(j2, (byte) (((value >>> 56) & 127) | 128));
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(j3, (byte) (((value >>> 49) & 127) | 128));
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(j4, (byte) (((value >>> 42) & 127) | 128));
            long j5 = this.pos;
            this.pos = j5 - 1;
            UnsafeUtil.putByte(j5, (byte) (((value >>> 35) & 127) | 128));
            long j6 = this.pos;
            this.pos = j6 - 1;
            UnsafeUtil.putByte(j6, (byte) (((value >>> 28) & 127) | 128));
            long j7 = this.pos;
            this.pos = j7 - 1;
            UnsafeUtil.putByte(j7, (byte) (((value >>> 21) & 127) | 128));
            long j8 = this.pos;
            this.pos = j8 - 1;
            UnsafeUtil.putByte(j8, (byte) (((value >>> 14) & 127) | 128));
            long j9 = this.pos;
            this.pos = j9 - 1;
            UnsafeUtil.putByte(j9, (byte) (((value >>> 7) & 127) | 128));
            long j10 = this.pos;
            this.pos = j10 - 1;
            UnsafeUtil.putByte(j10, (byte) ((value & 127) | 128));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeFixed32(int value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, (byte) ((value >> 24) & 255));
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(j2, (byte) ((value >> 16) & 255));
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(j3, (byte) ((value >> 8) & 255));
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(j4, (byte) (value & 255));
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeFixed64(long value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, (byte) (((int) (value >> 56)) & 255));
            long j2 = this.pos;
            this.pos = j2 - 1;
            UnsafeUtil.putByte(j2, (byte) (((int) (value >> 48)) & 255));
            long j3 = this.pos;
            this.pos = j3 - 1;
            UnsafeUtil.putByte(j3, (byte) (((int) (value >> 40)) & 255));
            long j4 = this.pos;
            this.pos = j4 - 1;
            UnsafeUtil.putByte(j4, (byte) (((int) (value >> 32)) & 255));
            long j5 = this.pos;
            this.pos = j5 - 1;
            UnsafeUtil.putByte(j5, (byte) (((int) (value >> 24)) & 255));
            long j6 = this.pos;
            this.pos = j6 - 1;
            UnsafeUtil.putByte(j6, (byte) (((int) (value >> 16)) & 255));
            long j7 = this.pos;
            this.pos = j7 - 1;
            UnsafeUtil.putByte(j7, (byte) (((int) (value >> 8)) & 255));
            long j8 = this.pos;
            this.pos = j8 - 1;
            UnsafeUtil.putByte(j8, (byte) (((int) value) & 255));
        }

        /* JADX WARN: Code duplicated, block: B:17:0x0044  */
        /* JADX WARN: Code duplicated, block: B:19:0x0048  */
        /* JADX WARN: Code duplicated, block: B:21:0x0050  */
        /* JADX WARN: Code duplicated, block: B:22:0x006b  */
        /* JADX WARN: Code duplicated, block: B:24:0x0070  */
        /* JADX WARN: Code duplicated, block: B:26:0x0075  */
        /* JADX WARN: Code duplicated, block: B:28:0x007e  */
        /* JADX WARN: Code duplicated, block: B:29:0x00a7  */
        /* JADX WARN: Code duplicated, block: B:31:0x00b2 A[DONT_INVERT] */
        /* JADX WARN: Code duplicated, block: B:32:0x00b4  */
        /* JADX WARN: Code duplicated, block: B:34:0x00c0  */
        /* JADX WARN: Code duplicated, block: B:37:0x0108  */
        /* JADX WARN: Code duplicated, block: B:43:0x0100 A[SYNTHETIC] */
        /* JADX WARN: Code duplicated, block: B:44:0x0100 A[SYNTHETIC] */
        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void writeString(String in) {
            long j;
            char cCharAt;
            long j2;
            char cCharAt2;
            requireSpace(in.length());
            int length = in.length();
            while (true) {
                length--;
                if (length < 0 || (cCharAt2 = in.charAt(length)) >= 128) {
                    break;
                }
                long j3 = this.pos;
                this.pos = j3 - 1;
                UnsafeUtil.putByte(j3, (byte) cCharAt2);
            }
            if (length == -1) {
                return;
            }
            while (length >= 0) {
                char cCharAt3 = in.charAt(length);
                if (cCharAt3 < 128) {
                    long j4 = this.pos;
                    if (j4 >= this.bufferOffset) {
                        this.pos = j4 - 1;
                        UnsafeUtil.putByte(j4, (byte) cCharAt3);
                    } else if (cCharAt3 < 2048) {
                        j2 = this.pos;
                        if (j2 > this.bufferOffset) {
                            this.pos = j2 - 1;
                            UnsafeUtil.putByte(j2, (byte) ((cCharAt3 & '?') | 128));
                            long j5 = this.pos;
                            this.pos = j5 - 1;
                            UnsafeUtil.putByte(j5, (byte) ((cCharAt3 >>> 6) | 960));
                        } else if (cCharAt3 >= 55296 || 57343 < cCharAt3) {
                            j = this.pos;
                            if (j > this.bufferOffset + 1) {
                                this.pos = j - 1;
                                UnsafeUtil.putByte(j, (byte) ((cCharAt3 & '?') | 128));
                                long j6 = this.pos;
                                this.pos = j6 - 1;
                                UnsafeUtil.putByte(j6, (byte) (((cCharAt3 >>> 6) & 63) | 128));
                                long j7 = this.pos;
                                this.pos = j7 - 1;
                                UnsafeUtil.putByte(j7, (byte) ((cCharAt3 >>> '\f') | 480));
                            } else {
                                if (this.pos > this.bufferOffset + 2) {
                                    if (length != 0) {
                                        cCharAt = in.charAt(length - 1);
                                        if (Character.isSurrogatePair(cCharAt, cCharAt3)) {
                                            length--;
                                            int codePoint = Character.toCodePoint(cCharAt, cCharAt3);
                                            long j8 = this.pos;
                                            this.pos = j8 - 1;
                                            UnsafeUtil.putByte(j8, (byte) ((codePoint & 63) | 128));
                                            long j9 = this.pos;
                                            this.pos = j9 - 1;
                                            UnsafeUtil.putByte(j9, (byte) (((codePoint >>> 6) & 63) | 128));
                                            long j10 = this.pos;
                                            this.pos = j10 - 1;
                                            UnsafeUtil.putByte(j10, (byte) (((codePoint >>> 12) & 63) | 128));
                                            long j11 = this.pos;
                                            this.pos = j11 - 1;
                                            UnsafeUtil.putByte(j11, (byte) ((codePoint >>> 18) | 240));
                                        }
                                    }
                                    throw new Utf8.UnpairedSurrogateException(length - 1, length);
                                }
                                requireSpace(length);
                                length++;
                            }
                        } else {
                            if (this.pos > this.bufferOffset + 2) {
                                if (length != 0) {
                                    cCharAt = in.charAt(length - 1);
                                    if (Character.isSurrogatePair(cCharAt, cCharAt3)) {
                                        length--;
                                        int codePoint2 = Character.toCodePoint(cCharAt, cCharAt3);
                                        long j12 = this.pos;
                                        this.pos = j12 - 1;
                                        UnsafeUtil.putByte(j12, (byte) ((codePoint2 & 63) | 128));
                                        long j13 = this.pos;
                                        this.pos = j13 - 1;
                                        UnsafeUtil.putByte(j13, (byte) (((codePoint2 >>> 6) & 63) | 128));
                                        long j14 = this.pos;
                                        this.pos = j14 - 1;
                                        UnsafeUtil.putByte(j14, (byte) (((codePoint2 >>> 12) & 63) | 128));
                                        long j15 = this.pos;
                                        this.pos = j15 - 1;
                                        UnsafeUtil.putByte(j15, (byte) ((codePoint2 >>> 18) | 240));
                                    }
                                }
                                throw new Utf8.UnpairedSurrogateException(length - 1, length);
                            }
                            requireSpace(length);
                            length++;
                        }
                    } else if (cCharAt3 >= 55296) {
                        j = this.pos;
                        if (j > this.bufferOffset + 1) {
                            this.pos = j - 1;
                            UnsafeUtil.putByte(j, (byte) ((cCharAt3 & '?') | 128));
                            long j16 = this.pos;
                            this.pos = j16 - 1;
                            UnsafeUtil.putByte(j16, (byte) (((cCharAt3 >>> 6) & 63) | 128));
                            long j17 = this.pos;
                            this.pos = j17 - 1;
                            UnsafeUtil.putByte(j17, (byte) ((cCharAt3 >>> '\f') | 480));
                        } else {
                            if (this.pos > this.bufferOffset + 2) {
                                if (length != 0) {
                                    cCharAt = in.charAt(length - 1);
                                    if (Character.isSurrogatePair(cCharAt, cCharAt3)) {
                                        length--;
                                        int codePoint3 = Character.toCodePoint(cCharAt, cCharAt3);
                                        long j18 = this.pos;
                                        this.pos = j18 - 1;
                                        UnsafeUtil.putByte(j18, (byte) ((codePoint3 & 63) | 128));
                                        long j19 = this.pos;
                                        this.pos = j19 - 1;
                                        UnsafeUtil.putByte(j19, (byte) (((codePoint3 >>> 6) & 63) | 128));
                                        long j110 = this.pos;
                                        this.pos = j110 - 1;
                                        UnsafeUtil.putByte(j110, (byte) (((codePoint3 >>> 12) & 63) | 128));
                                        long j111 = this.pos;
                                        this.pos = j111 - 1;
                                        UnsafeUtil.putByte(j111, (byte) ((codePoint3 >>> 18) | 240));
                                    }
                                }
                                throw new Utf8.UnpairedSurrogateException(length - 1, length);
                            }
                            requireSpace(length);
                            length++;
                        }
                    } else {
                        j = this.pos;
                        if (j > this.bufferOffset + 1) {
                            this.pos = j - 1;
                            UnsafeUtil.putByte(j, (byte) ((cCharAt3 & '?') | 128));
                            long j112 = this.pos;
                            this.pos = j112 - 1;
                            UnsafeUtil.putByte(j112, (byte) (((cCharAt3 >>> 6) & 63) | 128));
                            long j113 = this.pos;
                            this.pos = j113 - 1;
                            UnsafeUtil.putByte(j113, (byte) ((cCharAt3 >>> '\f') | 480));
                        } else {
                            if (this.pos > this.bufferOffset + 2) {
                                if (length != 0) {
                                    cCharAt = in.charAt(length - 1);
                                    if (Character.isSurrogatePair(cCharAt, cCharAt3)) {
                                        length--;
                                        int codePoint4 = Character.toCodePoint(cCharAt, cCharAt3);
                                        long j114 = this.pos;
                                        this.pos = j114 - 1;
                                        UnsafeUtil.putByte(j114, (byte) ((codePoint4 & 63) | 128));
                                        long j115 = this.pos;
                                        this.pos = j115 - 1;
                                        UnsafeUtil.putByte(j115, (byte) (((codePoint4 >>> 6) & 63) | 128));
                                        long j116 = this.pos;
                                        this.pos = j116 - 1;
                                        UnsafeUtil.putByte(j116, (byte) (((codePoint4 >>> 12) & 63) | 128));
                                        long j117 = this.pos;
                                        this.pos = j117 - 1;
                                        UnsafeUtil.putByte(j117, (byte) ((codePoint4 >>> 18) | 240));
                                    }
                                }
                                throw new Utf8.UnpairedSurrogateException(length - 1, length);
                            }
                            requireSpace(length);
                            length++;
                        }
                    }
                } else if (cCharAt3 < 2048) {
                    j2 = this.pos;
                    if (j2 > this.bufferOffset) {
                        this.pos = j2 - 1;
                        UnsafeUtil.putByte(j2, (byte) ((cCharAt3 & '?') | 128));
                        long j20 = this.pos;
                        this.pos = j20 - 1;
                        UnsafeUtil.putByte(j20, (byte) ((cCharAt3 >>> 6) | 960));
                    } else if (cCharAt3 >= 55296) {
                        j = this.pos;
                        if (j > this.bufferOffset + 1) {
                            this.pos = j - 1;
                            UnsafeUtil.putByte(j, (byte) ((cCharAt3 & '?') | 128));
                            long j118 = this.pos;
                            this.pos = j118 - 1;
                            UnsafeUtil.putByte(j118, (byte) (((cCharAt3 >>> 6) & 63) | 128));
                            long j119 = this.pos;
                            this.pos = j119 - 1;
                            UnsafeUtil.putByte(j119, (byte) ((cCharAt3 >>> '\f') | 480));
                        } else {
                            if (this.pos > this.bufferOffset + 2) {
                                if (length != 0) {
                                    cCharAt = in.charAt(length - 1);
                                    if (Character.isSurrogatePair(cCharAt, cCharAt3)) {
                                        length--;
                                        int codePoint5 = Character.toCodePoint(cCharAt, cCharAt3);
                                        long j1110 = this.pos;
                                        this.pos = j1110 - 1;
                                        UnsafeUtil.putByte(j1110, (byte) ((codePoint5 & 63) | 128));
                                        long j1111 = this.pos;
                                        this.pos = j1111 - 1;
                                        UnsafeUtil.putByte(j1111, (byte) (((codePoint5 >>> 6) & 63) | 128));
                                        long j1112 = this.pos;
                                        this.pos = j1112 - 1;
                                        UnsafeUtil.putByte(j1112, (byte) (((codePoint5 >>> 12) & 63) | 128));
                                        long j1113 = this.pos;
                                        this.pos = j1113 - 1;
                                        UnsafeUtil.putByte(j1113, (byte) ((codePoint5 >>> 18) | 240));
                                    }
                                }
                                throw new Utf8.UnpairedSurrogateException(length - 1, length);
                            }
                            requireSpace(length);
                            length++;
                        }
                    } else {
                        j = this.pos;
                        if (j > this.bufferOffset + 1) {
                            this.pos = j - 1;
                            UnsafeUtil.putByte(j, (byte) ((cCharAt3 & '?') | 128));
                            long j1114 = this.pos;
                            this.pos = j1114 - 1;
                            UnsafeUtil.putByte(j1114, (byte) (((cCharAt3 >>> 6) & 63) | 128));
                            long j1115 = this.pos;
                            this.pos = j1115 - 1;
                            UnsafeUtil.putByte(j1115, (byte) ((cCharAt3 >>> '\f') | 480));
                        } else {
                            if (this.pos > this.bufferOffset + 2) {
                                if (length != 0) {
                                    cCharAt = in.charAt(length - 1);
                                    if (Character.isSurrogatePair(cCharAt, cCharAt3)) {
                                        length--;
                                        int codePoint6 = Character.toCodePoint(cCharAt, cCharAt3);
                                        long j1116 = this.pos;
                                        this.pos = j1116 - 1;
                                        UnsafeUtil.putByte(j1116, (byte) ((codePoint6 & 63) | 128));
                                        long j1117 = this.pos;
                                        this.pos = j1117 - 1;
                                        UnsafeUtil.putByte(j1117, (byte) (((codePoint6 >>> 6) & 63) | 128));
                                        long j1118 = this.pos;
                                        this.pos = j1118 - 1;
                                        UnsafeUtil.putByte(j1118, (byte) (((codePoint6 >>> 12) & 63) | 128));
                                        long j1119 = this.pos;
                                        this.pos = j1119 - 1;
                                        UnsafeUtil.putByte(j1119, (byte) ((codePoint6 >>> 18) | 240));
                                    }
                                }
                                throw new Utf8.UnpairedSurrogateException(length - 1, length);
                            }
                            requireSpace(length);
                            length++;
                        }
                    }
                } else if (cCharAt3 >= 55296) {
                    j = this.pos;
                    if (j > this.bufferOffset + 1) {
                        this.pos = j - 1;
                        UnsafeUtil.putByte(j, (byte) ((cCharAt3 & '?') | 128));
                        long j11110 = this.pos;
                        this.pos = j11110 - 1;
                        UnsafeUtil.putByte(j11110, (byte) (((cCharAt3 >>> 6) & 63) | 128));
                        long j11111 = this.pos;
                        this.pos = j11111 - 1;
                        UnsafeUtil.putByte(j11111, (byte) ((cCharAt3 >>> '\f') | 480));
                    } else {
                        if (this.pos > this.bufferOffset + 2) {
                            if (length != 0) {
                                cCharAt = in.charAt(length - 1);
                                if (Character.isSurrogatePair(cCharAt, cCharAt3)) {
                                    length--;
                                    int codePoint7 = Character.toCodePoint(cCharAt, cCharAt3);
                                    long j11112 = this.pos;
                                    this.pos = j11112 - 1;
                                    UnsafeUtil.putByte(j11112, (byte) ((codePoint7 & 63) | 128));
                                    long j11113 = this.pos;
                                    this.pos = j11113 - 1;
                                    UnsafeUtil.putByte(j11113, (byte) (((codePoint7 >>> 6) & 63) | 128));
                                    long j11114 = this.pos;
                                    this.pos = j11114 - 1;
                                    UnsafeUtil.putByte(j11114, (byte) (((codePoint7 >>> 12) & 63) | 128));
                                    long j11115 = this.pos;
                                    this.pos = j11115 - 1;
                                    UnsafeUtil.putByte(j11115, (byte) ((codePoint7 >>> 18) | 240));
                                }
                            }
                            throw new Utf8.UnpairedSurrogateException(length - 1, length);
                        }
                        requireSpace(length);
                        length++;
                    }
                } else {
                    j = this.pos;
                    if (j > this.bufferOffset + 1) {
                        this.pos = j - 1;
                        UnsafeUtil.putByte(j, (byte) ((cCharAt3 & '?') | 128));
                        long j11116 = this.pos;
                        this.pos = j11116 - 1;
                        UnsafeUtil.putByte(j11116, (byte) (((cCharAt3 >>> 6) & 63) | 128));
                        long j11117 = this.pos;
                        this.pos = j11117 - 1;
                        UnsafeUtil.putByte(j11117, (byte) ((cCharAt3 >>> '\f') | 480));
                    } else {
                        if (this.pos > this.bufferOffset + 2) {
                            if (length != 0) {
                                cCharAt = in.charAt(length - 1);
                                if (Character.isSurrogatePair(cCharAt, cCharAt3)) {
                                    length--;
                                    int codePoint8 = Character.toCodePoint(cCharAt, cCharAt3);
                                    long j11118 = this.pos;
                                    this.pos = j11118 - 1;
                                    UnsafeUtil.putByte(j11118, (byte) ((codePoint8 & 63) | 128));
                                    long j11119 = this.pos;
                                    this.pos = j11119 - 1;
                                    UnsafeUtil.putByte(j11119, (byte) (((codePoint8 >>> 6) & 63) | 128));
                                    long j111110 = this.pos;
                                    this.pos = j111110 - 1;
                                    UnsafeUtil.putByte(j111110, (byte) (((codePoint8 >>> 12) & 63) | 128));
                                    long j111111 = this.pos;
                                    this.pos = j111111 - 1;
                                    UnsafeUtil.putByte(j111111, (byte) ((codePoint8 >>> 18) | 240));
                                }
                            }
                            throw new Utf8.UnpairedSurrogateException(length - 1, length);
                        }
                        requireSpace(length);
                        length++;
                    }
                }
                length--;
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void write(byte value) {
            long j = this.pos;
            this.pos = j - 1;
            UnsafeUtil.putByte(j, value);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void write(byte[] value, int offset, int length) {
            if (spaceLeft() < length) {
                nextBuffer(length);
            }
            this.pos -= (long) length;
            Java8Compatibility.position(this.buffer, bufferPos() + 1);
            this.buffer.put(value, offset, length);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void writeLazy(byte[] value, int offset, int length) {
            if (spaceLeft() < length) {
                this.totalDoneBytes += length;
                this.buffers.addFirst(AllocatedBuffer.wrap(value, offset, length));
                nextBuffer();
            } else {
                this.pos -= (long) length;
                Java8Compatibility.position(this.buffer, bufferPos() + 1);
                this.buffer.put(value, offset, length);
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void write(ByteBuffer value) {
            int iRemaining = value.remaining();
            if (spaceLeft() < iRemaining) {
                nextBuffer(iRemaining);
            }
            this.pos -= (long) iRemaining;
            Java8Compatibility.position(this.buffer, bufferPos() + 1);
            this.buffer.put(value);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.ByteOutput
        public void writeLazy(ByteBuffer value) {
            int iRemaining = value.remaining();
            if (spaceLeft() < iRemaining) {
                this.totalDoneBytes += iRemaining;
                this.buffers.addFirst(AllocatedBuffer.wrap(value));
                nextBuffer();
            } else {
                this.pos -= (long) iRemaining;
                Java8Compatibility.position(this.buffer, bufferPos() + 1);
                this.buffer.put(value);
            }
        }

        @Override // com.google.crypto.tink.shaded.protobuf.BinaryWriter
        void requireSpace(int size) {
            if (spaceLeft() < size) {
                nextBuffer(size);
            }
        }
    }
}
