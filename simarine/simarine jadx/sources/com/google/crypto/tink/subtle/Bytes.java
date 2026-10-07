package com.google.crypto.tink.subtle;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
public final class Bytes {
    public static final boolean equal(final byte[] x, final byte[] y) {
        return MessageDigest.isEqual(x, y);
    }

    public static byte[] concat(byte[]... chunks) throws GeneralSecurityException {
        int length = 0;
        for (byte[] bArr : chunks) {
            if (length > Integer.MAX_VALUE - bArr.length) {
                throw new GeneralSecurityException("exceeded size limit");
            }
            length += bArr.length;
        }
        byte[] bArr2 = new byte[length];
        int length2 = 0;
        for (byte[] bArr3 : chunks) {
            System.arraycopy(bArr3, 0, bArr2, length2, bArr3.length);
            length2 += bArr3.length;
        }
        return bArr2;
    }

    public static final byte[] xor(final byte[] x, int offsetX, final byte[] y, int offsetY, int len) {
        if (len < 0 || x.length - len < offsetX || y.length - len < offsetY) {
            throw new IllegalArgumentException("That combination of buffers, offsets and length to xor result in out-of-bond accesses.");
        }
        byte[] bArr = new byte[len];
        for (int i = 0; i < len; i++) {
            bArr[i] = (byte) (x[i + offsetX] ^ y[i + offsetY]);
        }
        return bArr;
    }

    public static final void xor(ByteBuffer output, ByteBuffer x, ByteBuffer y, int len) {
        if (len < 0 || x.remaining() < len || y.remaining() < len || output.remaining() < len) {
            throw new IllegalArgumentException("That combination of buffers, offsets and length to xor result in out-of-bond accesses.");
        }
        for (int i = 0; i < len; i++) {
            output.put((byte) (x.get() ^ y.get()));
        }
    }

    public static final byte[] xor(final byte[] x, final byte[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("The lengths of x and y should match.");
        }
        return xor(x, 0, y, 0, x.length);
    }

    public static final byte[] xorEnd(final byte[] a, final byte[] b) {
        if (a.length < b.length) {
            throw new IllegalArgumentException("xorEnd requires a.length >= b.length");
        }
        int length = a.length - b.length;
        byte[] bArrCopyOf = Arrays.copyOf(a, a.length);
        for (int i = 0; i < b.length; i++) {
            int i2 = length + i;
            bArrCopyOf[i2] = (byte) (bArrCopyOf[i2] ^ b[i]);
        }
        return bArrCopyOf;
    }

    public static byte[] intToByteArray(int capacity, int value) {
        if (capacity > 4 || capacity < 0) {
            throw new IllegalArgumentException("capacity must be between 0 and 4");
        }
        if (value < 0 || (capacity < 4 && value >= (1 << (capacity * 8)))) {
            throw new IllegalArgumentException("value too large");
        }
        byte[] bArr = new byte[capacity];
        for (int i = 0; i < capacity; i++) {
            bArr[i] = (byte) ((value >> (i * 8)) & 255);
        }
        return bArr;
    }

    public static int byteArrayToInt(byte[] bytes) {
        return byteArrayToInt(bytes, bytes.length);
    }

    public static int byteArrayToInt(byte[] bytes, int length) {
        return byteArrayToInt(bytes, 0, length);
    }

    public static int byteArrayToInt(byte[] bytes, int offset, int length) {
        if (length > 4 || length < 0) {
            throw new IllegalArgumentException("length must be between 0 and 4");
        }
        if (offset < 0 || offset + length > bytes.length) {
            throw new IllegalArgumentException("offset and length are out of bounds");
        }
        int i = 0;
        for (int i2 = 0; i2 < length; i2++) {
            i += (bytes[i2 + offset] & 255) << (i2 * 8);
        }
        return i;
    }

    private Bytes() {
    }
}
