package com.google.crypto.tink.util;

import com.google.crypto.tink.subtle.Hex;
import com.google.errorprone.annotations.Immutable;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class Bytes {
    private final byte[] data;

    public static Bytes copyFrom(final byte[] data) {
        if (data == null) {
            throw new NullPointerException("data must be non-null");
        }
        return copyFrom(data, 0, data.length);
    }

    public static Bytes copyFrom(byte[] data, int start, int len) {
        if (data == null) {
            throw new NullPointerException("data must be non-null");
        }
        if (start + len > data.length) {
            len = data.length - start;
        }
        return new Bytes(data, start, len);
    }

    public byte[] toByteArray() {
        byte[] bArr = this.data;
        byte[] bArr2 = new byte[bArr.length];
        System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
        return bArr2;
    }

    public int size() {
        return this.data.length;
    }

    private Bytes(final byte[] buf, final int start, final int len) {
        byte[] bArr = new byte[len];
        this.data = bArr;
        System.arraycopy(buf, start, bArr, 0, len);
    }

    public boolean equals(Object o) {
        if (o instanceof Bytes) {
            return Arrays.equals(((Bytes) o).data, this.data);
        }
        return false;
    }

    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    public String toString() {
        return "Bytes(" + Hex.encode(this.data) + ")";
    }
}
