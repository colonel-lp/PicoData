package com.google.crypto.tink.aead.internal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
final class ChaCha20Util {
    static final int BLOCK_SIZE_IN_BYTES = 64;
    static final int BLOCK_SIZE_IN_INTS = 16;
    static final int KEY_SIZE_IN_BYTES = 32;
    static final int KEY_SIZE_IN_INTS = 8;
    private static final int[] SIGMA = toIntArray(new byte[]{101, 120, 112, 97, 110, 100, 32, 51, 50, 45, 98, 121, 116, 101, 32, 107});

    private static int rotateLeft(int x, int y) {
        return (x >>> (-y)) | (x << y);
    }

    static void setSigmaAndKey(int[] state, final int[] key) {
        int[] iArr = SIGMA;
        System.arraycopy(iArr, 0, state, 0, iArr.length);
        System.arraycopy(key, 0, state, iArr.length, 8);
    }

    static void shuffleState(final int[] state) {
        for (int i = 0; i < 10; i++) {
            quarterRound(state, 0, 4, 8, 12);
            quarterRound(state, 1, 5, 9, 13);
            quarterRound(state, 2, 6, 10, 14);
            quarterRound(state, 3, 7, 11, 15);
            quarterRound(state, 0, 5, 10, 15);
            quarterRound(state, 1, 6, 11, 12);
            quarterRound(state, 2, 7, 8, 13);
            quarterRound(state, 3, 4, 9, 14);
        }
    }

    static void quarterRound(int[] x, int a, int b, int c, int d) {
        int i = x[a] + x[b];
        x[a] = i;
        int iRotateLeft = rotateLeft(i ^ x[d], 16);
        x[d] = iRotateLeft;
        int i2 = x[c] + iRotateLeft;
        x[c] = i2;
        int iRotateLeft2 = rotateLeft(x[b] ^ i2, 12);
        x[b] = iRotateLeft2;
        int i3 = x[a] + iRotateLeft2;
        x[a] = i3;
        int iRotateLeft3 = rotateLeft(x[d] ^ i3, 8);
        x[d] = iRotateLeft3;
        int i4 = x[c] + iRotateLeft3;
        x[c] = i4;
        x[b] = rotateLeft(x[b] ^ i4, 7);
    }

    static int[] toIntArray(final byte[] input) {
        if (input.length % 4 != 0) {
            throw new IllegalArgumentException("invalid input length");
        }
        IntBuffer intBufferAsIntBuffer = ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
        int[] iArr = new int[intBufferAsIntBuffer.remaining()];
        intBufferAsIntBuffer.get(iArr);
        return iArr;
    }

    static byte[] toByteArray(final int[] input) {
        ByteBuffer byteBufferOrder = ByteBuffer.allocate(input.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        byteBufferOrder.asIntBuffer().put(input);
        return byteBufferOrder.array();
    }

    static int[] hChaCha20(final int[] key, final int[] nonce) {
        int[] iArr = new int[16];
        setSigmaAndKey(iArr, key);
        iArr[12] = nonce[0];
        iArr[13] = nonce[1];
        iArr[14] = nonce[2];
        iArr[15] = nonce[3];
        shuffleState(iArr);
        iArr[4] = iArr[12];
        iArr[5] = iArr[13];
        iArr[6] = iArr[14];
        iArr[7] = iArr[15];
        return Arrays.copyOf(iArr, 8);
    }

    static byte[] hChaCha20(final byte[] key, final byte[] nonce) {
        return toByteArray(hChaCha20(toIntArray(key), toIntArray(nonce)));
    }

    private ChaCha20Util() {
    }
}
