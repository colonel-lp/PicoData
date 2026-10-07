package com.google.crypto.tink.internal;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
public final class BigIntegerEncoding {
    public static byte[] toBigEndianBytes(BigInteger n) {
        if (n.signum() == -1) {
            throw new IllegalArgumentException("n must not be negative");
        }
        return n.toByteArray();
    }

    public static byte[] toUnsignedBigEndianBytes(BigInteger n) {
        if (n.signum() == -1) {
            throw new IllegalArgumentException("n must not be negative");
        }
        byte[] byteArray = n.toByteArray();
        return byteArray[0] == 0 ? Arrays.copyOfRange(byteArray, 1, byteArray.length) : byteArray;
    }

    public static byte[] toBigEndianBytesOfFixedLength(BigInteger n, int length) throws GeneralSecurityException {
        if (n.signum() == -1) {
            throw new IllegalArgumentException("integer must be nonnegative");
        }
        byte[] byteArray = n.toByteArray();
        if (byteArray.length == length) {
            return byteArray;
        }
        int i = length + 1;
        if (byteArray.length > i) {
            throw new GeneralSecurityException("integer too large");
        }
        if (byteArray.length == i) {
            if (byteArray[0] == 0) {
                return Arrays.copyOfRange(byteArray, 1, byteArray.length);
            }
            throw new GeneralSecurityException("integer too large");
        }
        byte[] bArr = new byte[length];
        System.arraycopy(byteArray, 0, bArr, length - byteArray.length, byteArray.length);
        return bArr;
    }

    public static BigInteger fromUnsignedBigEndianBytes(byte[] bytes) {
        return new BigInteger(1, bytes);
    }

    private BigIntegerEncoding() {
    }
}
