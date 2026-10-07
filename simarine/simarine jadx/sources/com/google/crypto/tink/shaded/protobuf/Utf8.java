package com.google.crypto.tink.shaded.protobuf;

import java.nio.ByteBuffer;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
final class Utf8 {
    private static final long ASCII_MASK_LONG = -9187201950435737472L;
    static final int COMPLETE = 0;
    static final int MALFORMED = -1;
    static final int MAX_BYTES_PER_CHAR = 3;
    private static final int UNSAFE_COUNT_ASCII_THRESHOLD = 16;
    private static final Processor processor;

    /* JADX INFO: Access modifiers changed from: private */
    public static int incompleteStateFor(int byte1) {
        if (byte1 > -12) {
            return -1;
        }
        return byte1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int incompleteStateFor(int byte1, int byte2) {
        if (byte1 > -12 || byte2 > -65) {
            return -1;
        }
        return byte1 ^ (byte2 << 8);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int incompleteStateFor(int byte1, int byte2, int byte3) {
        if (byte1 > -12 || byte2 > -65 || byte3 > -65) {
            return -1;
        }
        return (byte1 ^ (byte2 << 8)) ^ (byte3 << 16);
    }

    static {
        Processor safeProcessor;
        if (UnsafeProcessor.isAvailable() && !Android.isOnAndroidDevice()) {
            safeProcessor = new UnsafeProcessor();
        } else {
            safeProcessor = new SafeProcessor();
        }
        processor = safeProcessor;
    }

    static boolean isValidUtf8(byte[] bytes) {
        return processor.isValidUtf8(bytes, 0, bytes.length);
    }

    static boolean isValidUtf8(byte[] bytes, int index, int limit) {
        return processor.isValidUtf8(bytes, index, limit);
    }

    static int partialIsValidUtf8(int state, byte[] bytes, int index, int limit) {
        return processor.partialIsValidUtf8(state, bytes, index, limit);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int incompleteStateFor(byte[] bytes, int index, int limit) {
        byte b = bytes[index - 1];
        int i = limit - index;
        if (i == 0) {
            return incompleteStateFor(b);
        }
        if (i == 1) {
            return incompleteStateFor(b, bytes[index]);
        }
        if (i == 2) {
            return incompleteStateFor(b, bytes[index], bytes[index + 1]);
        }
        throw new AssertionError();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int incompleteStateFor(final ByteBuffer buffer, final int byte1, final int index, final int remaining) {
        if (remaining == 0) {
            return incompleteStateFor(byte1);
        }
        if (remaining == 1) {
            return incompleteStateFor(byte1, buffer.get(index));
        }
        if (remaining == 2) {
            return incompleteStateFor(byte1, buffer.get(index), buffer.get(index + 1));
        }
        throw new AssertionError();
    }

    static class UnpairedSurrogateException extends IllegalArgumentException {
        UnpairedSurrogateException(int index, int length) {
            super("Unpaired surrogate at index " + index + " of " + length);
        }
    }

    static int encodedLength(String string) {
        int length = string.length();
        int i = 0;
        while (i < length && string.charAt(i) < 128) {
            i++;
        }
        int iEncodedLengthGeneral = length;
        while (i < length) {
            char cCharAt = string.charAt(i);
            if (cCharAt >= 2048) {
                iEncodedLengthGeneral += encodedLengthGeneral(string, i);
                break;
            }
            iEncodedLengthGeneral += (127 - cCharAt) >>> 31;
            i++;
        }
        if (iEncodedLengthGeneral >= length) {
            return iEncodedLengthGeneral;
        }
        throw new IllegalArgumentException("UTF-8 length does not fit in int: " + (((long) iEncodedLengthGeneral) + 4294967296L));
    }

    private static int encodedLengthGeneral(String string, int start) {
        int length = string.length();
        int i = 0;
        while (start < length) {
            char cCharAt = string.charAt(start);
            if (cCharAt < 2048) {
                i += (127 - cCharAt) >>> 31;
            } else {
                i += 2;
                if (55296 <= cCharAt && cCharAt <= 57343) {
                    if (Character.codePointAt(string, start) < 65536) {
                        throw new UnpairedSurrogateException(start, length);
                    }
                    start++;
                }
            }
            start++;
        }
        return i;
    }

    static int encode(String in, byte[] out, int offset, int length) {
        return processor.encodeUtf8(in, out, offset, length);
    }

    static boolean isValidUtf8(ByteBuffer buffer) {
        return processor.isValidUtf8(buffer, buffer.position(), buffer.remaining());
    }

    static int partialIsValidUtf8(int state, ByteBuffer buffer, int index, int limit) {
        return processor.partialIsValidUtf8(state, buffer, index, limit);
    }

    static String decodeUtf8(ByteBuffer buffer, int index, int size) throws InvalidProtocolBufferException {
        return processor.decodeUtf8(buffer, index, size);
    }

    static String decodeUtf8(byte[] bytes, int index, int size) throws InvalidProtocolBufferException {
        return processor.decodeUtf8(bytes, index, size);
    }

    static void encodeUtf8(String in, ByteBuffer out) {
        processor.encodeUtf8(in, out);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int estimateConsecutiveAscii(ByteBuffer buffer, int index, int limit) {
        int i = limit - 7;
        int i2 = index;
        while (i2 < i && (buffer.getLong(i2) & (-9187201950435737472L)) == 0) {
            i2 += 8;
        }
        return i2 - index;
    }

    static abstract class Processor {
        abstract String decodeUtf8(byte[] bytes, int index, int size) throws InvalidProtocolBufferException;

        abstract String decodeUtf8Direct(ByteBuffer buffer, int index, int size) throws InvalidProtocolBufferException;

        abstract int encodeUtf8(String in, byte[] out, int offset, int length);

        abstract void encodeUtf8Direct(String in, ByteBuffer out);

        abstract int partialIsValidUtf8(int state, byte[] bytes, int index, int limit);

        abstract int partialIsValidUtf8Direct(final int state, final ByteBuffer buffer, int index, final int limit);

        Processor() {
        }

        final boolean isValidUtf8(byte[] bytes, int index, int limit) {
            return partialIsValidUtf8(0, bytes, index, limit) == 0;
        }

        final boolean isValidUtf8(ByteBuffer buffer, int index, int limit) {
            return partialIsValidUtf8(0, buffer, index, limit) == 0;
        }

        final int partialIsValidUtf8(final int state, final ByteBuffer buffer, int index, final int limit) {
            if (buffer.hasArray()) {
                int iArrayOffset = buffer.arrayOffset();
                return partialIsValidUtf8(state, buffer.array(), index + iArrayOffset, iArrayOffset + limit);
            }
            if (buffer.isDirect()) {
                return partialIsValidUtf8Direct(state, buffer, index, limit);
            }
            return partialIsValidUtf8Default(state, buffer, index, limit);
        }

        /* JADX WARN: Code restructure failed: missing block: B:10:0x0017, code lost:
        
            if (r7.get(r8) > (-65)) goto L13;
         */
        /* JADX WARN: Code restructure failed: missing block: B:31:0x004c, code lost:
        
            if (r7.get(r8) > (-65)) goto L32;
         */
        /* JADX WARN: Code restructure failed: missing block: B:52:0x008f, code lost:
        
            if (r7.get(r6) > (-65)) goto L53;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        final int partialIsValidUtf8Default(final int r6, final java.nio.ByteBuffer r7, int r8, final int r9) {
            /*
                r5 = this;
                if (r6 == 0) goto L92
                if (r8 < r9) goto L5
                return r6
            L5:
                byte r5 = (byte) r6
                r0 = -32
                r1 = -1
                r2 = -65
                if (r5 >= r0) goto L1e
                r6 = -62
                if (r5 < r6) goto L1d
                int r5 = r8 + 1
                byte r6 = r7.get(r8)
                if (r6 <= r2) goto L1a
                goto L1d
            L1a:
                r8 = r5
                goto L92
            L1d:
                return r1
            L1e:
                r3 = -16
                if (r5 >= r3) goto L4f
                int r6 = r6 >> 8
                int r6 = ~r6
                byte r6 = (byte) r6
                if (r6 != 0) goto L38
                int r6 = r8 + 1
                byte r8 = r7.get(r8)
                if (r6 < r9) goto L35
                int r5 = com.google.crypto.tink.shaded.protobuf.Utf8.access$000(r5, r8)
                return r5
            L35:
                r4 = r8
                r8 = r6
                r6 = r4
            L38:
                if (r6 > r2) goto L4e
                r3 = -96
                if (r5 != r0) goto L40
                if (r6 < r3) goto L4e
            L40:
                r0 = -19
                if (r5 != r0) goto L46
                if (r6 >= r3) goto L4e
            L46:
                int r5 = r8 + 1
                byte r6 = r7.get(r8)
                if (r6 <= r2) goto L1a
            L4e:
                return r1
            L4f:
                int r0 = r6 >> 8
                int r0 = ~r0
                byte r0 = (byte) r0
                if (r0 != 0) goto L64
                int r6 = r8 + 1
                byte r0 = r7.get(r8)
                if (r6 < r9) goto L62
                int r5 = com.google.crypto.tink.shaded.protobuf.Utf8.access$000(r5, r0)
                return r5
            L62:
                r8 = 0
                goto L6a
            L64:
                int r6 = r6 >> 16
                byte r6 = (byte) r6
                r4 = r8
                r8 = r6
                r6 = r4
            L6a:
                if (r8 != 0) goto L7c
                int r8 = r6 + 1
                byte r6 = r7.get(r6)
                if (r8 < r9) goto L79
                int r5 = com.google.crypto.tink.shaded.protobuf.Utf8.access$100(r5, r0, r6)
                return r5
            L79:
                r4 = r8
                r8 = r6
                r6 = r4
            L7c:
                if (r0 > r2) goto L91
                int r5 = r5 << 28
                int r0 = r0 + 112
                int r5 = r5 + r0
                int r5 = r5 >> 30
                if (r5 != 0) goto L91
                if (r8 > r2) goto L91
                int r8 = r6 + 1
                byte r5 = r7.get(r6)
                if (r5 <= r2) goto L92
            L91:
                return r1
            L92:
                int r5 = partialIsValidUtf8(r7, r8, r9)
                return r5
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.crypto.tink.shaded.protobuf.Utf8.Processor.partialIsValidUtf8Default(int, java.nio.ByteBuffer, int, int):int");
        }

        private static int partialIsValidUtf8(final ByteBuffer buffer, int index, final int limit) {
            int iEstimateConsecutiveAscii = index + Utf8.estimateConsecutiveAscii(buffer, index, limit);
            while (iEstimateConsecutiveAscii < limit) {
                int i = iEstimateConsecutiveAscii + 1;
                byte b = buffer.get(iEstimateConsecutiveAscii);
                if (b >= 0) {
                    iEstimateConsecutiveAscii = i;
                } else if (b < -32) {
                    if (i >= limit) {
                        return b;
                    }
                    if (b < -62 || buffer.get(i) > -65) {
                        return -1;
                    }
                    iEstimateConsecutiveAscii += 2;
                } else {
                    if (b >= -16) {
                        if (i >= limit - 2) {
                            return Utf8.incompleteStateFor(buffer, b, i, limit - i);
                        }
                        int i2 = iEstimateConsecutiveAscii + 2;
                        byte b2 = buffer.get(i);
                        if (b2 <= -65 && (((b << 28) + (b2 + 112)) >> 30) == 0) {
                            int i3 = iEstimateConsecutiveAscii + 3;
                            if (buffer.get(i2) <= -65) {
                                iEstimateConsecutiveAscii += 4;
                                if (buffer.get(i3) > -65) {
                                }
                            }
                        }
                        return -1;
                    }
                    if (i >= limit - 1) {
                        return Utf8.incompleteStateFor(buffer, b, i, limit - i);
                    }
                    int i4 = iEstimateConsecutiveAscii + 2;
                    byte b3 = buffer.get(i);
                    if (b3 > -65 || ((b == -32 && b3 < -96) || ((b == -19 && b3 >= -96) || buffer.get(i4) > -65))) {
                        return -1;
                    }
                    iEstimateConsecutiveAscii += 3;
                }
            }
            return 0;
        }

        final String decodeUtf8(ByteBuffer buffer, int index, int size) throws InvalidProtocolBufferException {
            if (buffer.hasArray()) {
                return decodeUtf8(buffer.array(), buffer.arrayOffset() + index, size);
            }
            if (buffer.isDirect()) {
                return decodeUtf8Direct(buffer, index, size);
            }
            return decodeUtf8Default(buffer, index, size);
        }

        final String decodeUtf8Default(ByteBuffer buffer, int index, int size) throws InvalidProtocolBufferException {
            if ((index | size | ((buffer.limit() - index) - size)) < 0) {
                throw new ArrayIndexOutOfBoundsException(String.format("buffer limit=%d, index=%d, limit=%d", Integer.valueOf(buffer.limit()), Integer.valueOf(index), Integer.valueOf(size)));
            }
            int i = index + size;
            char[] cArr = new char[size];
            int i2 = 0;
            while (index < i) {
                byte b = buffer.get(index);
                if (!DecodeUtil.isOneByte(b)) {
                    break;
                }
                index++;
                DecodeUtil.handleOneByte(b, cArr, i2);
                i2++;
            }
            int i3 = i2;
            while (index < i) {
                int i4 = index + 1;
                byte b2 = buffer.get(index);
                if (DecodeUtil.isOneByte(b2)) {
                    int i5 = i3 + 1;
                    DecodeUtil.handleOneByte(b2, cArr, i3);
                    while (i4 < i) {
                        byte b3 = buffer.get(i4);
                        if (!DecodeUtil.isOneByte(b3)) {
                            break;
                        }
                        i4++;
                        DecodeUtil.handleOneByte(b3, cArr, i5);
                        i5++;
                    }
                    i3 = i5;
                    index = i4;
                } else if (DecodeUtil.isTwoBytes(b2)) {
                    if (i4 >= i) {
                        throw InvalidProtocolBufferException.invalidUtf8();
                    }
                    index += 2;
                    DecodeUtil.handleTwoBytes(b2, buffer.get(i4), cArr, i3);
                    i3++;
                } else if (DecodeUtil.isThreeBytes(b2)) {
                    if (i4 >= i - 1) {
                        throw InvalidProtocolBufferException.invalidUtf8();
                    }
                    int i6 = index + 2;
                    index += 3;
                    DecodeUtil.handleThreeBytes(b2, buffer.get(i4), buffer.get(i6), cArr, i3);
                    i3++;
                } else {
                    if (i4 >= i - 2) {
                        throw InvalidProtocolBufferException.invalidUtf8();
                    }
                    byte b4 = buffer.get(i4);
                    int i7 = index + 3;
                    byte b5 = buffer.get(index + 2);
                    index += 4;
                    DecodeUtil.handleFourBytes(b2, b4, b5, buffer.get(i7), cArr, i3);
                    i3 += 2;
                }
            }
            return new String(cArr, 0, i3);
        }

        final void encodeUtf8(String in, ByteBuffer out) {
            if (out.hasArray()) {
                int iArrayOffset = out.arrayOffset();
                Java8Compatibility.position(out, Utf8.encode(in, out.array(), out.position() + iArrayOffset, out.remaining()) - iArrayOffset);
            } else if (out.isDirect()) {
                encodeUtf8Direct(in, out);
            } else {
                encodeUtf8Default(in, out);
            }
        }

        final void encodeUtf8Default(String in, ByteBuffer out) {
            int length = in.length();
            int iPosition = out.position();
            int i = 0;
            while (i < length) {
                try {
                    char cCharAt = in.charAt(i);
                    if (cCharAt >= 128) {
                        break;
                    }
                    out.put(iPosition + i, (byte) cCharAt);
                    i++;
                } catch (IndexOutOfBoundsException unused) {
                }
            }
            if (i == length) {
                Java8Compatibility.position(out, iPosition + i);
                return;
            }
            iPosition += i;
            while (i < length) {
                char cCharAt2 = in.charAt(i);
                if (cCharAt2 < 128) {
                    out.put(iPosition, (byte) cCharAt2);
                } else if (cCharAt2 < 2048) {
                    int i2 = iPosition + 1;
                    try {
                        out.put(iPosition, (byte) ((cCharAt2 >>> 6) | 192));
                        out.put(i2, (byte) ((cCharAt2 & '?') | 128));
                        iPosition = i2;
                    } catch (IndexOutOfBoundsException unused2) {
                        iPosition = i2;
                    }
                } else if (cCharAt2 < 55296 || 57343 < cCharAt2) {
                    int i3 = iPosition + 1;
                    out.put(iPosition, (byte) ((cCharAt2 >>> '\f') | 224));
                    iPosition += 2;
                    out.put(i3, (byte) (((cCharAt2 >>> 6) & 63) | 128));
                    out.put(iPosition, (byte) ((cCharAt2 & '?') | 128));
                } else {
                    int i4 = i + 1;
                    if (i4 != length) {
                        try {
                            char cCharAt3 = in.charAt(i4);
                            if (Character.isSurrogatePair(cCharAt2, cCharAt3)) {
                                int codePoint = Character.toCodePoint(cCharAt2, cCharAt3);
                                int i5 = iPosition + 1;
                                try {
                                    out.put(iPosition, (byte) ((codePoint >>> 18) | 240));
                                    int i6 = iPosition + 2;
                                    try {
                                        out.put(i5, (byte) (((codePoint >>> 12) & 63) | 128));
                                        iPosition += 3;
                                        out.put(i6, (byte) (((codePoint >>> 6) & 63) | 128));
                                        out.put(iPosition, (byte) ((codePoint & 63) | 128));
                                        i = i4;
                                    } catch (IndexOutOfBoundsException unused3) {
                                        i = i4;
                                        iPosition = i6;
                                    }
                                } catch (IndexOutOfBoundsException unused4) {
                                    iPosition = i5;
                                    i = i4;
                                }
                            } else {
                                i = i4;
                            }
                        } catch (IndexOutOfBoundsException unused5) {
                        }
                        i = i4;
                        throw new ArrayIndexOutOfBoundsException("Failed writing " + in.charAt(i) + " at index " + (out.position() + Math.max(i, (iPosition - out.position()) + 1)));
                    }
                    throw new UnpairedSurrogateException(i, length);
                }
                i++;
                iPosition++;
            }
            Java8Compatibility.position(out, iPosition);
        }
    }

    static final class SafeProcessor extends Processor {
        SafeProcessor() {
        }

        /* JADX WARN: Code restructure failed: missing block: B:10:0x0015, code lost:
        
            if (r7[r8] > (-65)) goto L13;
         */
        /* JADX WARN: Code restructure failed: missing block: B:31:0x0046, code lost:
        
            if (r7[r8] > (-65)) goto L32;
         */
        /* JADX WARN: Code restructure failed: missing block: B:52:0x0083, code lost:
        
            if (r7[r6] > (-65)) goto L53;
         */
        @Override // com.google.crypto.tink.shaded.protobuf.Utf8.Processor
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        int partialIsValidUtf8(int r6, byte[] r7, int r8, int r9) {
            /*
                r5 = this;
                if (r6 == 0) goto L86
                if (r8 < r9) goto L5
                return r6
            L5:
                byte r5 = (byte) r6
                r0 = -32
                r1 = -1
                r2 = -65
                if (r5 >= r0) goto L1c
                r6 = -62
                if (r5 < r6) goto L1b
                int r5 = r8 + 1
                r6 = r7[r8]
                if (r6 <= r2) goto L18
                goto L1b
            L18:
                r8 = r5
                goto L86
            L1b:
                return r1
            L1c:
                r3 = -16
                if (r5 >= r3) goto L49
                int r6 = r6 >> 8
                int r6 = ~r6
                byte r6 = (byte) r6
                if (r6 != 0) goto L34
                int r6 = r8 + 1
                r8 = r7[r8]
                if (r6 < r9) goto L31
                int r5 = com.google.crypto.tink.shaded.protobuf.Utf8.access$000(r5, r8)
                return r5
            L31:
                r4 = r8
                r8 = r6
                r6 = r4
            L34:
                if (r6 > r2) goto L48
                r3 = -96
                if (r5 != r0) goto L3c
                if (r6 < r3) goto L48
            L3c:
                r0 = -19
                if (r5 != r0) goto L42
                if (r6 >= r3) goto L48
            L42:
                int r5 = r8 + 1
                r6 = r7[r8]
                if (r6 <= r2) goto L18
            L48:
                return r1
            L49:
                int r0 = r6 >> 8
                int r0 = ~r0
                byte r0 = (byte) r0
                if (r0 != 0) goto L5c
                int r6 = r8 + 1
                r0 = r7[r8]
                if (r6 < r9) goto L5a
                int r5 = com.google.crypto.tink.shaded.protobuf.Utf8.access$000(r5, r0)
                return r5
            L5a:
                r8 = 0
                goto L62
            L5c:
                int r6 = r6 >> 16
                byte r6 = (byte) r6
                r4 = r8
                r8 = r6
                r6 = r4
            L62:
                if (r8 != 0) goto L72
                int r8 = r6 + 1
                r6 = r7[r6]
                if (r8 < r9) goto L6f
                int r5 = com.google.crypto.tink.shaded.protobuf.Utf8.access$100(r5, r0, r6)
                return r5
            L6f:
                r4 = r8
                r8 = r6
                r6 = r4
            L72:
                if (r0 > r2) goto L85
                int r5 = r5 << 28
                int r0 = r0 + 112
                int r5 = r5 + r0
                int r5 = r5 >> 30
                if (r5 != 0) goto L85
                if (r8 > r2) goto L85
                int r8 = r6 + 1
                r5 = r7[r6]
                if (r5 <= r2) goto L86
            L85:
                return r1
            L86:
                int r5 = partialIsValidUtf8(r7, r8, r9)
                return r5
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.crypto.tink.shaded.protobuf.Utf8.SafeProcessor.partialIsValidUtf8(int, byte[], int, int):int");
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Utf8.Processor
        int partialIsValidUtf8Direct(int state, ByteBuffer buffer, int index, int limit) {
            return partialIsValidUtf8Default(state, buffer, index, limit);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Utf8.Processor
        String decodeUtf8(byte[] bytes, int index, int size) throws InvalidProtocolBufferException {
            if ((index | size | ((bytes.length - index) - size)) < 0) {
                throw new ArrayIndexOutOfBoundsException(String.format("buffer length=%d, index=%d, size=%d", Integer.valueOf(bytes.length), Integer.valueOf(index), Integer.valueOf(size)));
            }
            int i = index + size;
            char[] cArr = new char[size];
            int i2 = 0;
            while (index < i) {
                byte b = bytes[index];
                if (!DecodeUtil.isOneByte(b)) {
                    break;
                }
                index++;
                DecodeUtil.handleOneByte(b, cArr, i2);
                i2++;
            }
            int i3 = i2;
            while (index < i) {
                int i4 = index + 1;
                byte b2 = bytes[index];
                if (DecodeUtil.isOneByte(b2)) {
                    int i5 = i3 + 1;
                    DecodeUtil.handleOneByte(b2, cArr, i3);
                    while (i4 < i) {
                        byte b3 = bytes[i4];
                        if (!DecodeUtil.isOneByte(b3)) {
                            break;
                        }
                        i4++;
                        DecodeUtil.handleOneByte(b3, cArr, i5);
                        i5++;
                    }
                    i3 = i5;
                    index = i4;
                } else if (DecodeUtil.isTwoBytes(b2)) {
                    if (i4 >= i) {
                        throw InvalidProtocolBufferException.invalidUtf8();
                    }
                    index += 2;
                    DecodeUtil.handleTwoBytes(b2, bytes[i4], cArr, i3);
                    i3++;
                } else if (DecodeUtil.isThreeBytes(b2)) {
                    if (i4 >= i - 1) {
                        throw InvalidProtocolBufferException.invalidUtf8();
                    }
                    int i6 = index + 2;
                    index += 3;
                    DecodeUtil.handleThreeBytes(b2, bytes[i4], bytes[i6], cArr, i3);
                    i3++;
                } else {
                    if (i4 >= i - 2) {
                        throw InvalidProtocolBufferException.invalidUtf8();
                    }
                    byte b4 = bytes[i4];
                    int i7 = index + 3;
                    byte b5 = bytes[index + 2];
                    index += 4;
                    DecodeUtil.handleFourBytes(b2, b4, b5, bytes[i7], cArr, i3);
                    i3 += 2;
                }
            }
            return new String(cArr, 0, i3);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Utf8.Processor
        String decodeUtf8Direct(ByteBuffer buffer, int index, int size) throws InvalidProtocolBufferException {
            return decodeUtf8Default(buffer, index, size);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Utf8.Processor
        int encodeUtf8(String in, byte[] out, int offset, int length) {
            int i;
            int i2;
            char cCharAt;
            int length2 = in.length();
            int i3 = length + offset;
            int i4 = 0;
            while (i4 < length2 && (i2 = i4 + offset) < i3 && (cCharAt = in.charAt(i4)) < 128) {
                out[i2] = (byte) cCharAt;
                i4++;
            }
            if (i4 == length2) {
                return offset + length2;
            }
            int i5 = offset + i4;
            while (i4 < length2) {
                char cCharAt2 = in.charAt(i4);
                if (cCharAt2 < 128 && i5 < i3) {
                    out[i5] = (byte) cCharAt2;
                    i5++;
                } else if (cCharAt2 < 2048 && i5 <= i3 - 2) {
                    int i6 = i5 + 1;
                    out[i5] = (byte) ((cCharAt2 >>> 6) | 960);
                    i5 += 2;
                    out[i6] = (byte) ((cCharAt2 & '?') | 128);
                } else {
                    if ((cCharAt2 >= 55296 && 57343 >= cCharAt2) || i5 > i3 - 3) {
                        if (i5 <= i3 - 4) {
                            int i7 = i4 + 1;
                            if (i7 != in.length()) {
                                char cCharAt3 = in.charAt(i7);
                                if (Character.isSurrogatePair(cCharAt2, cCharAt3)) {
                                    int codePoint = Character.toCodePoint(cCharAt2, cCharAt3);
                                    out[i5] = (byte) ((codePoint >>> 18) | 240);
                                    out[i5 + 1] = (byte) (((codePoint >>> 12) & 63) | 128);
                                    int i8 = i5 + 3;
                                    out[i5 + 2] = (byte) (((codePoint >>> 6) & 63) | 128);
                                    i5 += 4;
                                    out[i8] = (byte) ((codePoint & 63) | 128);
                                    i4 = i7;
                                } else {
                                    i4 = i7;
                                }
                            }
                            throw new UnpairedSurrogateException(i4 - 1, length2);
                        }
                        if (55296 <= cCharAt2 && cCharAt2 <= 57343 && ((i = i4 + 1) == in.length() || !Character.isSurrogatePair(cCharAt2, in.charAt(i)))) {
                            throw new UnpairedSurrogateException(i4, length2);
                        }
                        throw new ArrayIndexOutOfBoundsException("Failed writing " + cCharAt2 + " at index " + i5);
                    }
                    out[i5] = (byte) ((cCharAt2 >>> '\f') | 480);
                    int i9 = i5 + 2;
                    out[i5 + 1] = (byte) (((cCharAt2 >>> 6) & 63) | 128);
                    i5 += 3;
                    out[i9] = (byte) ((cCharAt2 & '?') | 128);
                }
                i4++;
            }
            return i5;
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Utf8.Processor
        void encodeUtf8Direct(String in, ByteBuffer out) {
            encodeUtf8Default(in, out);
        }

        private static int partialIsValidUtf8(byte[] bytes, int index, int limit) {
            while (index < limit && bytes[index] >= 0) {
                index++;
            }
            if (index >= limit) {
                return 0;
            }
            return partialIsValidUtf8NonAscii(bytes, index, limit);
        }

        private static int partialIsValidUtf8NonAscii(byte[] bytes, int index, int limit) {
            while (index < limit) {
                int i = index + 1;
                byte b = bytes[index];
                if (b < 0) {
                    if (b < -32) {
                        if (i >= limit) {
                            return b;
                        }
                        if (b >= -62) {
                            index += 2;
                            if (bytes[i] > -65) {
                            }
                        }
                        return -1;
                    }
                    if (b >= -16) {
                        if (i >= limit - 2) {
                            return Utf8.incompleteStateFor(bytes, i, limit);
                        }
                        int i2 = index + 2;
                        byte b2 = bytes[i];
                        if (b2 <= -65 && (((b << 28) + (b2 + 112)) >> 30) == 0) {
                            int i3 = index + 3;
                            if (bytes[i2] <= -65) {
                                index += 4;
                                if (bytes[i3] > -65) {
                                }
                            }
                        }
                        return -1;
                    }
                    if (i >= limit - 1) {
                        return Utf8.incompleteStateFor(bytes, i, limit);
                    }
                    int i4 = index + 2;
                    byte b3 = bytes[i];
                    if (b3 <= -65 && ((b != -32 || b3 >= -96) && (b != -19 || b3 < -96))) {
                        index += 3;
                        if (bytes[i4] > -65) {
                        }
                    }
                    return -1;
                }
                index = i;
            }
            return 0;
        }
    }

    static final class UnsafeProcessor extends Processor {
        UnsafeProcessor() {
        }

        static boolean isAvailable() {
            return UnsafeUtil.hasUnsafeArrayOperations() && UnsafeUtil.hasUnsafeByteBufferOperations();
        }

        /* JADX WARN: Code restructure failed: missing block: B:35:0x0058, code lost:
        
            if (com.google.crypto.tink.shaded.protobuf.UnsafeUtil.getByte(r11, r0) > (-65)) goto L38;
         */
        /* JADX WARN: Code restructure failed: missing block: B:58:0x009e, code lost:
        
            if (com.google.crypto.tink.shaded.protobuf.UnsafeUtil.getByte(r11, r0) > (-65)) goto L59;
         */
        @Override // com.google.crypto.tink.shaded.protobuf.Utf8.Processor
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        int partialIsValidUtf8(int r10, byte[] r11, final int r12, final int r13) {
            /*
                r9 = this;
                r9 = r12 | r13
                int r0 = r11.length
                int r0 = r0 - r13
                r9 = r9 | r0
                if (r9 < 0) goto La8
                long r0 = (long) r12
                long r12 = (long) r13
                if (r10 == 0) goto La1
                int r9 = (r0 > r12 ? 1 : (r0 == r12 ? 0 : -1))
                if (r9 < 0) goto L10
                return r10
            L10:
                byte r9 = (byte) r10
                r2 = -32
                r3 = -1
                r4 = -65
                r5 = 1
                if (r9 >= r2) goto L2a
                r10 = -62
                if (r9 < r10) goto L29
                long r5 = r5 + r0
                byte r9 = com.google.crypto.tink.shaded.protobuf.UnsafeUtil.getByte(r11, r0)
                if (r9 <= r4) goto L26
                goto L29
            L26:
                r0 = r5
                goto La1
            L29:
                return r3
            L2a:
                r7 = -16
                if (r9 >= r7) goto L5e
                int r10 = r10 >> 8
                int r10 = ~r10
                byte r10 = (byte) r10
                if (r10 != 0) goto L44
                long r7 = r0 + r5
                byte r10 = com.google.crypto.tink.shaded.protobuf.UnsafeUtil.getByte(r11, r0)
                int r0 = (r7 > r12 ? 1 : (r7 == r12 ? 0 : -1))
                if (r0 < 0) goto L43
                int r9 = com.google.crypto.tink.shaded.protobuf.Utf8.access$000(r9, r10)
                return r9
            L43:
                r0 = r7
            L44:
                if (r10 > r4) goto L5d
                r7 = -96
                if (r9 != r2) goto L4c
                if (r10 < r7) goto L5d
            L4c:
                r2 = -19
                if (r9 != r2) goto L52
                if (r10 >= r7) goto L5d
            L52:
                long r9 = r0 + r5
                byte r0 = com.google.crypto.tink.shaded.protobuf.UnsafeUtil.getByte(r11, r0)
                if (r0 <= r4) goto L5b
                goto L5d
            L5b:
                r0 = r9
                goto La1
            L5d:
                return r3
            L5e:
                int r2 = r10 >> 8
                int r2 = ~r2
                byte r2 = (byte) r2
                if (r2 != 0) goto L76
                long r7 = r0 + r5
                byte r2 = com.google.crypto.tink.shaded.protobuf.UnsafeUtil.getByte(r11, r0)
                int r10 = (r7 > r12 ? 1 : (r7 == r12 ? 0 : -1))
                if (r10 < 0) goto L73
                int r9 = com.google.crypto.tink.shaded.protobuf.Utf8.access$000(r9, r2)
                return r9
            L73:
                r10 = 0
                r0 = r7
                goto L79
            L76:
                int r10 = r10 >> 16
                byte r10 = (byte) r10
            L79:
                if (r10 != 0) goto L8b
                long r7 = r0 + r5
                byte r10 = com.google.crypto.tink.shaded.protobuf.UnsafeUtil.getByte(r11, r0)
                int r0 = (r7 > r12 ? 1 : (r7 == r12 ? 0 : -1))
                if (r0 < 0) goto L8a
                int r9 = com.google.crypto.tink.shaded.protobuf.Utf8.access$100(r9, r2, r10)
                return r9
            L8a:
                r0 = r7
            L8b:
                if (r2 > r4) goto La0
                int r9 = r9 << 28
                int r2 = r2 + 112
                int r9 = r9 + r2
                int r9 = r9 >> 30
                if (r9 != 0) goto La0
                if (r10 > r4) goto La0
                long r9 = r0 + r5
                byte r0 = com.google.crypto.tink.shaded.protobuf.UnsafeUtil.getByte(r11, r0)
                if (r0 <= r4) goto L5b
            La0:
                return r3
            La1:
                long r12 = r12 - r0
                int r9 = (int) r12
                int r9 = partialIsValidUtf8(r11, r0, r9)
                return r9
            La8:
                java.lang.ArrayIndexOutOfBoundsException r9 = new java.lang.ArrayIndexOutOfBoundsException
                int r10 = r11.length
                java.lang.Integer r10 = java.lang.Integer.valueOf(r10)
                java.lang.Integer r11 = java.lang.Integer.valueOf(r12)
                java.lang.Integer r12 = java.lang.Integer.valueOf(r13)
                java.lang.Object[] r10 = new java.lang.Object[]{r10, r11, r12}
                java.lang.String r11 = "Array length=%d, index=%d, limit=%d"
                java.lang.String r10 = java.lang.String.format(r11, r10)
                r9.<init>(r10)
                throw r9
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.crypto.tink.shaded.protobuf.Utf8.UnsafeProcessor.partialIsValidUtf8(int, byte[], int, int):int");
        }

        /* JADX WARN: Code restructure failed: missing block: B:35:0x0062, code lost:
        
            if (com.google.crypto.tink.shaded.protobuf.UnsafeUtil.getByte(r0) > (-65)) goto L38;
         */
        /* JADX WARN: Code restructure failed: missing block: B:58:0x00a8, code lost:
        
            if (com.google.crypto.tink.shaded.protobuf.UnsafeUtil.getByte(r0) > (-65)) goto L59;
         */
        @Override // com.google.crypto.tink.shaded.protobuf.Utf8.Processor
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        int partialIsValidUtf8Direct(final int r9, java.nio.ByteBuffer r10, final int r11, final int r12) {
            /*
                Method dump skipped, instruction units count: 210
                To view this dump add '--comments-level debug' option
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.crypto.tink.shaded.protobuf.Utf8.UnsafeProcessor.partialIsValidUtf8Direct(int, java.nio.ByteBuffer, int, int):int");
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Utf8.Processor
        String decodeUtf8(byte[] bytes, int index, int size) throws InvalidProtocolBufferException {
            String str = new String(bytes, index, size, Internal.UTF_8);
            if (str.indexOf(65533) >= 0 && !Arrays.equals(str.getBytes(Internal.UTF_8), Arrays.copyOfRange(bytes, index, size + index))) {
                throw InvalidProtocolBufferException.invalidUtf8();
            }
            return str;
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Utf8.Processor
        String decodeUtf8Direct(ByteBuffer buffer, int index, int size) throws InvalidProtocolBufferException {
            if ((index | size | ((buffer.limit() - index) - size)) < 0) {
                throw new ArrayIndexOutOfBoundsException(String.format("buffer limit=%d, index=%d, limit=%d", Integer.valueOf(buffer.limit()), Integer.valueOf(index), Integer.valueOf(size)));
            }
            long jAddressOffset = UnsafeUtil.addressOffset(buffer) + ((long) index);
            long j = ((long) size) + jAddressOffset;
            char[] cArr = new char[size];
            int i = 0;
            while (jAddressOffset < j) {
                byte b = UnsafeUtil.getByte(jAddressOffset);
                if (!DecodeUtil.isOneByte(b)) {
                    break;
                }
                jAddressOffset++;
                DecodeUtil.handleOneByte(b, cArr, i);
                i++;
            }
            int i2 = i;
            while (jAddressOffset < j) {
                long j2 = jAddressOffset + 1;
                byte b2 = UnsafeUtil.getByte(jAddressOffset);
                if (DecodeUtil.isOneByte(b2)) {
                    int i3 = i2 + 1;
                    DecodeUtil.handleOneByte(b2, cArr, i2);
                    while (j2 < j) {
                        byte b3 = UnsafeUtil.getByte(j2);
                        if (!DecodeUtil.isOneByte(b3)) {
                            break;
                        }
                        j2++;
                        DecodeUtil.handleOneByte(b3, cArr, i3);
                        i3++;
                    }
                    i2 = i3;
                    jAddressOffset = j2;
                } else if (DecodeUtil.isTwoBytes(b2)) {
                    if (j2 >= j) {
                        throw InvalidProtocolBufferException.invalidUtf8();
                    }
                    jAddressOffset += 2;
                    DecodeUtil.handleTwoBytes(b2, UnsafeUtil.getByte(j2), cArr, i2);
                    i2++;
                } else if (DecodeUtil.isThreeBytes(b2)) {
                    if (j2 >= j - 1) {
                        throw InvalidProtocolBufferException.invalidUtf8();
                    }
                    long j3 = 2 + jAddressOffset;
                    jAddressOffset += 3;
                    DecodeUtil.handleThreeBytes(b2, UnsafeUtil.getByte(j2), UnsafeUtil.getByte(j3), cArr, i2);
                    i2++;
                } else {
                    if (j2 >= j - 2) {
                        throw InvalidProtocolBufferException.invalidUtf8();
                    }
                    byte b4 = UnsafeUtil.getByte(j2);
                    long j4 = 3 + jAddressOffset;
                    byte b5 = UnsafeUtil.getByte(2 + jAddressOffset);
                    jAddressOffset += 4;
                    DecodeUtil.handleFourBytes(b2, b4, b5, UnsafeUtil.getByte(j4), cArr, i2);
                    i2 += 2;
                }
            }
            return new String(cArr, 0, i2);
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Utf8.Processor
        int encodeUtf8(final String in, final byte[] out, final int offset, final int length) {
            long j;
            String str;
            String str2;
            int i;
            long j2;
            char cCharAt;
            long j3 = offset;
            long j4 = ((long) length) + j3;
            int length2 = in.length();
            String str3 = " at index ";
            String str4 = "Failed writing ";
            if (length2 > length || out.length - length < offset) {
                throw new ArrayIndexOutOfBoundsException("Failed writing " + in.charAt(length2 - 1) + " at index " + (offset + length));
            }
            int i2 = 0;
            while (true) {
                j = 1;
                if (i2 >= length2 || (cCharAt = in.charAt(i2)) >= 128) {
                    break;
                }
                UnsafeUtil.putByte(out, j3, (byte) cCharAt);
                i2++;
                j3 = 1 + j3;
            }
            if (i2 == length2) {
                return (int) j3;
            }
            while (i2 < length2) {
                char cCharAt2 = in.charAt(i2);
                if (cCharAt2 >= 128 || j3 >= j4) {
                    if (cCharAt2 >= 2048 || j3 > j4 - 2) {
                        str = str3;
                        str2 = str4;
                        if ((cCharAt2 >= 55296 && 57343 >= cCharAt2) || j3 > j4 - 3) {
                            if (j3 <= j4 - 4) {
                                int i3 = i2 + 1;
                                if (i3 != length2) {
                                    char cCharAt3 = in.charAt(i3);
                                    if (Character.isSurrogatePair(cCharAt2, cCharAt3)) {
                                        int codePoint = Character.toCodePoint(cCharAt2, cCharAt3);
                                        j2 = 1;
                                        UnsafeUtil.putByte(out, j3, (byte) ((codePoint >>> 18) | 240));
                                        UnsafeUtil.putByte(out, j3 + 1, (byte) (((codePoint >>> 12) & 63) | 128));
                                        long j5 = j3 + 3;
                                        UnsafeUtil.putByte(out, j3 + 2, (byte) (((codePoint >>> 6) & 63) | 128));
                                        j3 += 4;
                                        UnsafeUtil.putByte(out, j5, (byte) ((codePoint & 63) | 128));
                                        i2 = i3;
                                    } else {
                                        i2 = i3;
                                    }
                                }
                                throw new UnpairedSurrogateException(i2 - 1, length2);
                            }
                            if (55296 <= cCharAt2 && cCharAt2 <= 57343 && ((i = i2 + 1) == length2 || !Character.isSurrogatePair(cCharAt2, in.charAt(i)))) {
                                throw new UnpairedSurrogateException(i2, length2);
                            }
                            throw new ArrayIndexOutOfBoundsException(str2 + cCharAt2 + str + j3);
                        }
                        UnsafeUtil.putByte(out, j3, (byte) ((cCharAt2 >>> '\f') | 480));
                        long j6 = j3 + 2;
                        UnsafeUtil.putByte(out, j3 + 1, (byte) (((cCharAt2 >>> 6) & 63) | 128));
                        j3 += 3;
                        UnsafeUtil.putByte(out, j6, (byte) ((cCharAt2 & '?') | 128));
                    } else {
                        str = str3;
                        str2 = str4;
                        long j7 = j3 + j;
                        UnsafeUtil.putByte(out, j3, (byte) ((cCharAt2 >>> 6) | 960));
                        j3 += 2;
                        UnsafeUtil.putByte(out, j7, (byte) ((cCharAt2 & '?') | 128));
                    }
                    j2 = 1;
                } else {
                    UnsafeUtil.putByte(out, j3, (byte) cCharAt2);
                    str = str3;
                    j2 = j;
                    j3 += j;
                    str2 = str4;
                }
                i2++;
                str3 = str;
                str4 = str2;
                j = j2;
                j4 = j4;
            }
            return (int) j3;
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Utf8.Processor
        void encodeUtf8Direct(String in, ByteBuffer out) {
            long j;
            char c;
            long j2;
            int i;
            int i2;
            char c2;
            char cCharAt;
            long jAddressOffset = UnsafeUtil.addressOffset(out);
            long jPosition = ((long) out.position()) + jAddressOffset;
            long jLimit = ((long) out.limit()) + jAddressOffset;
            int length = in.length();
            if (length > jLimit - jPosition) {
                throw new ArrayIndexOutOfBoundsException("Failed writing " + in.charAt(length - 1) + " at index " + out.limit());
            }
            int i3 = 0;
            while (true) {
                j = 1;
                c = 128;
                if (i3 >= length || (cCharAt = in.charAt(i3)) >= 128) {
                    break;
                }
                UnsafeUtil.putByte(jPosition, (byte) cCharAt);
                i3++;
                jPosition = 1 + jPosition;
            }
            if (i3 == length) {
                Java8Compatibility.position(out, (int) (jPosition - jAddressOffset));
                return;
            }
            while (i3 < length) {
                char cCharAt2 = in.charAt(i3);
                if (cCharAt2 >= c || jPosition >= jLimit) {
                    if (cCharAt2 >= 2048 || jPosition > jLimit - 2) {
                        j2 = jAddressOffset;
                        if ((cCharAt2 >= 55296 && 57343 >= cCharAt2) || jPosition > jLimit - 3) {
                            if (jPosition <= jLimit - 4) {
                                i2 = i3 + 1;
                                if (i2 != length) {
                                    char cCharAt3 = in.charAt(i2);
                                    if (Character.isSurrogatePair(cCharAt2, cCharAt3)) {
                                        int codePoint = Character.toCodePoint(cCharAt2, cCharAt3);
                                        UnsafeUtil.putByte(jPosition, (byte) ((codePoint >>> 18) | 240));
                                        c2 = 128;
                                        UnsafeUtil.putByte(jPosition + 1, (byte) (((codePoint >>> 12) & 63) | 128));
                                        long j3 = jPosition + 3;
                                        UnsafeUtil.putByte(jPosition + 2, (byte) (((codePoint >>> 6) & 63) | 128));
                                        jPosition += 4;
                                        UnsafeUtil.putByte(j3, (byte) ((codePoint & 63) | 128));
                                    } else {
                                        i3 = i2;
                                    }
                                }
                                throw new UnpairedSurrogateException(i3 - 1, length);
                            }
                            if (55296 > cCharAt2 || cCharAt2 > 57343 || ((i = i3 + 1) != length && Character.isSurrogatePair(cCharAt2, in.charAt(i)))) {
                                throw new ArrayIndexOutOfBoundsException("Failed writing " + cCharAt2 + " at index " + jPosition);
                            }
                            throw new UnpairedSurrogateException(i3, length);
                        }
                        long j4 = jPosition + j;
                        UnsafeUtil.putByte(jPosition, (byte) ((cCharAt2 >>> '\f') | 480));
                        long j5 = jPosition + 2;
                        UnsafeUtil.putByte(j4, (byte) (((cCharAt2 >>> 6) & 63) | 128));
                        jPosition += 3;
                        UnsafeUtil.putByte(j5, (byte) ((cCharAt2 & '?') | 128));
                    } else {
                        j2 = jAddressOffset;
                        long j6 = jPosition + j;
                        UnsafeUtil.putByte(jPosition, (byte) ((cCharAt2 >>> 6) | 960));
                        jPosition += 2;
                        UnsafeUtil.putByte(j6, (byte) ((cCharAt2 & '?') | 128));
                    }
                    i2 = i3;
                    c2 = 128;
                } else {
                    UnsafeUtil.putByte(jPosition, (byte) cCharAt2);
                    j2 = jAddressOffset;
                    i2 = i3;
                    c2 = c;
                    jPosition += j;
                }
                c = c2;
                jAddressOffset = j2;
                j = 1;
                i3 = i2 + 1;
            }
            Java8Compatibility.position(out, (int) (jPosition - jAddressOffset));
        }

        private static int unsafeEstimateConsecutiveAscii(byte[] bytes, long offset, final int maxChars) {
            int i = 0;
            if (maxChars < 16) {
                return 0;
            }
            int i2 = 8 - (((int) offset) & 7);
            while (i < i2) {
                long j = 1 + offset;
                if (UnsafeUtil.getByte(bytes, offset) < 0) {
                    return i;
                }
                i++;
                offset = j;
            }
            while (true) {
                int i3 = i + 8;
                if (i3 > maxChars || (UnsafeUtil.getLong((Object) bytes, UnsafeUtil.BYTE_ARRAY_BASE_OFFSET + offset) & (-9187201950435737472L)) != 0) {
                    break;
                }
                offset += 8;
                i = i3;
            }
            while (i < maxChars) {
                long j2 = offset + 1;
                if (UnsafeUtil.getByte(bytes, offset) < 0) {
                    return i;
                }
                i++;
                offset = j2;
            }
            return maxChars;
        }

        private static int unsafeEstimateConsecutiveAscii(long address, final int maxChars) {
            if (maxChars < 16) {
                return 0;
            }
            int i = (int) ((-address) & 7);
            int i2 = i;
            while (i2 > 0) {
                long j = 1 + address;
                if (UnsafeUtil.getByte(address) < 0) {
                    return i - i2;
                }
                i2--;
                address = j;
            }
            int i3 = maxChars - i;
            while (i3 >= 8 && (UnsafeUtil.getLong(address) & (-9187201950435737472L)) == 0) {
                address += 8;
                i3 -= 8;
            }
            return maxChars - i3;
        }

        private static int partialIsValidUtf8(final byte[] bytes, long offset, int remaining) {
            int iUnsafeEstimateConsecutiveAscii = unsafeEstimateConsecutiveAscii(bytes, offset, remaining);
            int i = remaining - iUnsafeEstimateConsecutiveAscii;
            long j = offset + ((long) iUnsafeEstimateConsecutiveAscii);
            while (true) {
                byte b = 0;
                while (i > 0) {
                    long j2 = j + 1;
                    b = UnsafeUtil.getByte(bytes, j);
                    if (b < 0) {
                        j = j2;
                        break;
                    }
                    i--;
                    j = j2;
                }
                if (i == 0) {
                    return 0;
                }
                int i2 = i - 1;
                if (b < -32) {
                    if (i2 == 0) {
                        return b;
                    }
                    i -= 2;
                    if (b >= -62) {
                        long j3 = 1 + j;
                        if (UnsafeUtil.getByte(bytes, j) <= -65) {
                            j = j3;
                        }
                    }
                    return -1;
                }
                if (b >= -16) {
                    if (i2 < 3) {
                        return unsafeIncompleteStateFor(bytes, b, j, i2);
                    }
                    i -= 4;
                    long j4 = 1 + j;
                    byte b2 = UnsafeUtil.getByte(bytes, j);
                    if (b2 <= -65 && (((b << 28) + (b2 + 112)) >> 30) == 0) {
                        long j5 = 2 + j;
                        if (UnsafeUtil.getByte(bytes, j4) <= -65) {
                            j += 3;
                            if (UnsafeUtil.getByte(bytes, j5) > -65) {
                            }
                        }
                    }
                    return -1;
                }
                if (i2 < 2) {
                    return unsafeIncompleteStateFor(bytes, b, j, i2);
                }
                i -= 3;
                long j6 = 1 + j;
                byte b3 = UnsafeUtil.getByte(bytes, j);
                if (b3 <= -65 && ((b != -32 || b3 >= -96) && (b != -19 || b3 < -96))) {
                    j += 2;
                    if (UnsafeUtil.getByte(bytes, j6) > -65) {
                    }
                }
                return -1;
            }
        }

        private static int partialIsValidUtf8(long address, int remaining) {
            int iUnsafeEstimateConsecutiveAscii = unsafeEstimateConsecutiveAscii(address, remaining);
            long j = address + ((long) iUnsafeEstimateConsecutiveAscii);
            int i = remaining - iUnsafeEstimateConsecutiveAscii;
            while (true) {
                byte b = 0;
                while (i > 0) {
                    long j2 = j + 1;
                    b = UnsafeUtil.getByte(j);
                    if (b < 0) {
                        j = j2;
                        break;
                    }
                    i--;
                    j = j2;
                }
                if (i == 0) {
                    return 0;
                }
                int i2 = i - 1;
                if (b < -32) {
                    if (i2 == 0) {
                        return b;
                    }
                    i -= 2;
                    if (b >= -62) {
                        long j3 = 1 + j;
                        if (UnsafeUtil.getByte(j) <= -65) {
                            j = j3;
                        }
                    }
                    return -1;
                }
                if (b >= -16) {
                    if (i2 < 3) {
                        return unsafeIncompleteStateFor(j, b, i2);
                    }
                    i -= 4;
                    long j4 = 1 + j;
                    byte b2 = UnsafeUtil.getByte(j);
                    if (b2 <= -65 && (((b << 28) + (b2 + 112)) >> 30) == 0) {
                        long j5 = 2 + j;
                        if (UnsafeUtil.getByte(j4) <= -65) {
                            j += 3;
                            if (UnsafeUtil.getByte(j5) > -65) {
                            }
                        }
                    }
                    return -1;
                }
                if (i2 < 2) {
                    return unsafeIncompleteStateFor(j, b, i2);
                }
                i -= 3;
                long j6 = 1 + j;
                byte b3 = UnsafeUtil.getByte(j);
                if (b3 <= -65 && ((b != -32 || b3 >= -96) && (b != -19 || b3 < -96))) {
                    j += 2;
                    if (UnsafeUtil.getByte(j6) > -65) {
                    }
                }
                return -1;
            }
        }

        private static int unsafeIncompleteStateFor(byte[] bytes, int byte1, long offset, int remaining) {
            if (remaining == 0) {
                return Utf8.incompleteStateFor(byte1);
            }
            if (remaining == 1) {
                return Utf8.incompleteStateFor(byte1, UnsafeUtil.getByte(bytes, offset));
            }
            if (remaining == 2) {
                return Utf8.incompleteStateFor(byte1, UnsafeUtil.getByte(bytes, offset), UnsafeUtil.getByte(bytes, offset + 1));
            }
            throw new AssertionError();
        }

        private static int unsafeIncompleteStateFor(long address, final int byte1, int remaining) {
            if (remaining == 0) {
                return Utf8.incompleteStateFor(byte1);
            }
            if (remaining == 1) {
                return Utf8.incompleteStateFor(byte1, UnsafeUtil.getByte(address));
            }
            if (remaining == 2) {
                return Utf8.incompleteStateFor(byte1, UnsafeUtil.getByte(address), UnsafeUtil.getByte(address + 1));
            }
            throw new AssertionError();
        }
    }

    private static class DecodeUtil {
        private static char highSurrogate(int codePoint) {
            return (char) ((codePoint >>> 10) + 55232);
        }

        private static boolean isNotTrailingByte(byte b) {
            return b > -65;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static boolean isOneByte(byte b) {
            return b >= 0;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static boolean isThreeBytes(byte b) {
            return b < -16;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static boolean isTwoBytes(byte b) {
            return b < -32;
        }

        private static char lowSurrogate(int codePoint) {
            return (char) ((codePoint & 1023) + 56320);
        }

        private static int trailingByteValue(byte b) {
            return b & 63;
        }

        private DecodeUtil() {
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static void handleOneByte(byte byte1, char[] resultArr, int resultPos) {
            resultArr[resultPos] = (char) byte1;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static void handleTwoBytes(byte byte1, byte byte2, char[] resultArr, int resultPos) throws InvalidProtocolBufferException {
            if (byte1 < -62 || isNotTrailingByte(byte2)) {
                throw InvalidProtocolBufferException.invalidUtf8();
            }
            resultArr[resultPos] = (char) (((byte1 & 31) << 6) | trailingByteValue(byte2));
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static void handleThreeBytes(byte byte1, byte byte2, byte byte3, char[] resultArr, int resultPos) throws InvalidProtocolBufferException {
            if (isNotTrailingByte(byte2) || ((byte1 == -32 && byte2 < -96) || ((byte1 == -19 && byte2 >= -96) || isNotTrailingByte(byte3)))) {
                throw InvalidProtocolBufferException.invalidUtf8();
            }
            resultArr[resultPos] = (char) (((byte1 & 15) << 12) | (trailingByteValue(byte2) << 6) | trailingByteValue(byte3));
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static void handleFourBytes(byte byte1, byte byte2, byte byte3, byte byte4, char[] resultArr, int resultPos) throws InvalidProtocolBufferException {
            if (isNotTrailingByte(byte2) || (((byte1 << 28) + (byte2 + 112)) >> 30) != 0 || isNotTrailingByte(byte3) || isNotTrailingByte(byte4)) {
                throw InvalidProtocolBufferException.invalidUtf8();
            }
            int iTrailingByteValue = ((byte1 & 7) << 18) | (trailingByteValue(byte2) << 12) | (trailingByteValue(byte3) << 6) | trailingByteValue(byte4);
            resultArr[resultPos] = highSurrogate(iTrailingByteValue);
            resultArr[resultPos + 1] = lowSurrogate(iTrailingByteValue);
        }
    }

    private Utf8() {
    }
}
