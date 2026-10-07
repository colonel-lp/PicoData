package com.google.crypto.tink.internal;

import java.util.Arrays;
import kotlin.UByte;

/* JADX INFO: loaded from: classes2.dex */
public final class Field25519 {
    public static final int FIELD_LEN = 32;
    public static final int LIMB_CNT = 10;
    private static final long TWO_TO_25 = 33554432;
    private static final long TWO_TO_26 = 67108864;
    private static final int[] EXPAND_START = {0, 3, 6, 9, 12, 16, 19, 22, 25, 28};
    private static final int[] EXPAND_SHIFT = {0, 2, 3, 5, 6, 0, 1, 3, 4, 6};
    private static final int[] MASK = {67108863, 33554431};
    private static final int[] SHIFT = {26, 25};

    private static int eq(int a, int b) {
        int i = ~(a ^ b);
        int i2 = i & (i << 16);
        int i3 = i2 & (i2 << 8);
        int i4 = i3 & (i3 << 4);
        int i5 = i4 & (i4 << 2);
        return (i5 & (i5 << 1)) >> 31;
    }

    private static int gte(int a, int b) {
        return ~((a - b) >> 31);
    }

    static void sum(long[] output, long[] in1, long[] in2) {
        for (int i = 0; i < 10; i++) {
            output[i] = in1[i] + in2[i];
        }
    }

    static void sum(long[] output, long[] in) {
        sum(output, output, in);
    }

    static void sub(long[] output, long[] in1, long[] in2) {
        for (int i = 0; i < 10; i++) {
            output[i] = in1[i] - in2[i];
        }
    }

    static void sub(long[] output, long[] in) {
        sub(output, in, output);
    }

    static void scalarProduct(long[] output, long[] in, long scalar) {
        for (int i = 0; i < 10; i++) {
            output[i] = in[i] * scalar;
        }
    }

    static void product(long[] out, long[] in2, long[] in) {
        out[0] = in2[0] * in[0];
        long j = in2[0];
        long j2 = in[1] * j;
        long j3 = in2[1];
        long j4 = in[0];
        out[1] = j2 + (j3 * j4);
        long j5 = in2[1];
        long j6 = in[1];
        out[2] = (j5 * 2 * j6) + (in[2] * j) + (in2[2] * j4);
        long j7 = in[2];
        long j8 = in2[2];
        out[3] = (j5 * j7) + (j8 * j6) + (in[3] * j) + (in2[3] * j4);
        long j9 = in[3];
        long j10 = in2[3];
        out[4] = (j8 * j7) + (((j5 * j9) + (j10 * j6)) * 2) + (in[4] * j) + (in2[4] * j4);
        long j11 = in[4];
        long j12 = in2[4];
        out[5] = (j8 * j9) + (j10 * j7) + (j5 * j11) + (j12 * j6) + (in[5] * j) + (in2[5] * j4);
        long j13 = in[5];
        long j14 = in2[5];
        out[6] = (((j10 * j9) + (j5 * j13) + (j14 * j6)) * 2) + (j8 * j11) + (j12 * j7) + (in[6] * j) + (in2[6] * j4);
        long j15 = in[6];
        long j16 = in2[6];
        out[7] = (j10 * j11) + (j12 * j9) + (j8 * j13) + (j14 * j7) + (j5 * j15) + (j16 * j6) + (in[7] * j) + (in2[7] * j4);
        long j17 = in[7];
        long j18 = in2[7];
        out[8] = (j12 * j11) + (((j10 * j13) + (j14 * j9) + (j5 * j17) + (j18 * j6)) * 2) + (j8 * j15) + (j16 * j7) + (in[8] * j) + (in2[8] * j4);
        long j19 = in[8];
        long j20 = in2[8];
        out[9] = (j12 * j13) + (j14 * j11) + (j10 * j15) + (j16 * j9) + (j8 * j17) + (j18 * j7) + (j5 * j19) + (j20 * j6) + (j * in[9]) + (in2[9] * j4);
        long j21 = in[9];
        long j22 = in2[9];
        out[10] = (((j14 * j13) + (j10 * j17) + (j18 * j9) + (j5 * j21) + (j6 * j22)) * 2) + (j12 * j15) + (j16 * j11) + (j8 * j19) + (j20 * j7);
        out[11] = (j14 * j15) + (j16 * j13) + (j12 * j17) + (j18 * j11) + (j10 * j19) + (j20 * j9) + (j8 * j21) + (j7 * j22);
        out[12] = (j16 * j15) + (((j14 * j17) + (j18 * j13) + (j10 * j21) + (j9 * j22)) * 2) + (j12 * j19) + (j20 * j11);
        out[13] = (j16 * j17) + (j18 * j15) + (j14 * j19) + (j20 * j13) + (j12 * j21) + (j11 * j22);
        out[14] = (((j18 * j17) + (j14 * j21) + (j13 * j22)) * 2) + (j16 * j19) + (j20 * j15);
        out[15] = (j18 * j19) + (j20 * j17) + (j16 * j21) + (j15 * j22);
        out[16] = (j20 * j19) + (((j18 * j21) + (j17 * j22)) * 2);
        out[17] = (j20 * j21) + (j19 * j22);
        out[18] = j22 * 2 * j21;
    }

    static void reduce(long[] input, long[] output) {
        if (input.length != 19) {
            long[] jArr = new long[19];
            System.arraycopy(input, 0, jArr, 0, input.length);
            input = jArr;
        }
        reduceSizeByModularReduction(input);
        reduceCoefficients(input);
        System.arraycopy(input, 0, output, 0, 10);
    }

    static void reduceSizeByModularReduction(long[] output) {
        long j = output[8];
        long j2 = output[18];
        long j3 = j + (j2 << 4);
        output[8] = j3;
        long j4 = j3 + (j2 << 1);
        output[8] = j4;
        output[8] = j4 + j2;
        long j5 = output[7];
        long j6 = output[17];
        long j7 = j5 + (j6 << 4);
        output[7] = j7;
        long j8 = j7 + (j6 << 1);
        output[7] = j8;
        output[7] = j8 + j6;
        long j9 = output[6];
        long j10 = output[16];
        long j11 = j9 + (j10 << 4);
        output[6] = j11;
        long j12 = j11 + (j10 << 1);
        output[6] = j12;
        output[6] = j12 + j10;
        long j13 = output[5];
        long j14 = output[15];
        long j15 = j13 + (j14 << 4);
        output[5] = j15;
        long j16 = j15 + (j14 << 1);
        output[5] = j16;
        output[5] = j16 + j14;
        long j17 = output[4];
        long j18 = output[14];
        long j19 = j17 + (j18 << 4);
        output[4] = j19;
        long j20 = j19 + (j18 << 1);
        output[4] = j20;
        output[4] = j20 + j18;
        long j21 = output[3];
        long j22 = output[13];
        long j23 = j21 + (j22 << 4);
        output[3] = j23;
        long j24 = j23 + (j22 << 1);
        output[3] = j24;
        output[3] = j24 + j22;
        long j25 = output[2];
        long j26 = output[12];
        long j27 = j25 + (j26 << 4);
        output[2] = j27;
        long j28 = j27 + (j26 << 1);
        output[2] = j28;
        output[2] = j28 + j26;
        long j29 = output[1];
        long j30 = output[11];
        long j31 = j29 + (j30 << 4);
        output[1] = j31;
        long j32 = j31 + (j30 << 1);
        output[1] = j32;
        output[1] = j32 + j30;
        long j33 = output[0];
        long j34 = output[10];
        long j35 = j33 + (j34 << 4);
        output[0] = j35;
        long j36 = j35 + (j34 << 1);
        output[0] = j36;
        output[0] = j36 + j34;
    }

    static void reduceCoefficients(long[] output) {
        output[10] = 0;
        int i = 0;
        while (i < 10) {
            long j = output[i];
            long j2 = j / TWO_TO_26;
            output[i] = j - (j2 << 26);
            int i2 = i + 1;
            long j3 = output[i2] + j2;
            output[i2] = j3;
            long j4 = j3 / TWO_TO_25;
            output[i2] = j3 - (j4 << 25);
            i += 2;
            output[i] = output[i] + j4;
        }
        long j5 = output[0];
        long j6 = output[10];
        long j7 = j5 + (j6 << 4);
        output[0] = j7;
        long j8 = j7 + (j6 << 1);
        output[0] = j8;
        long j9 = j8 + j6;
        output[0] = j9;
        output[10] = 0;
        long j10 = j9 / TWO_TO_26;
        output[0] = j9 - (j10 << 26);
        output[1] = output[1] + j10;
    }

    static void mult(long[] output, long[] in, long[] in2) {
        long[] jArr = new long[19];
        product(jArr, in, in2);
        reduce(jArr, output);
    }

    private static void squareInner(long[] out, long[] in) {
        long j = in[0];
        out[0] = j * j;
        long j2 = in[0];
        out[1] = j2 * 2 * in[1];
        long j3 = in[1];
        out[2] = ((j3 * j3) + (in[2] * j2)) * 2;
        long j4 = in[2];
        out[3] = ((j3 * j4) + (in[3] * j2)) * 2;
        long j5 = in[3];
        out[4] = (j4 * j4) + (j3 * 4 * j5) + (j2 * 2 * in[4]);
        long j6 = in[4];
        out[5] = ((j4 * j5) + (j3 * j6) + (in[5] * j2)) * 2;
        long j7 = (j5 * j5) + (j4 * j6) + (in[6] * j2);
        long j8 = in[5];
        out[6] = (j7 + (j3 * 2 * j8)) * 2;
        long j9 = in[6];
        out[7] = ((j5 * j6) + (j4 * j8) + (j3 * j9) + (in[7] * j2)) * 2;
        long j10 = (j4 * j9) + (in[8] * j2);
        long j11 = in[7];
        out[8] = (j6 * j6) + ((j10 + (((j3 * j11) + (j5 * j8)) * 2)) * 2);
        long j12 = in[8];
        out[9] = ((j6 * j8) + (j5 * j9) + (j4 * j11) + (j3 * j12) + (j2 * in[9])) * 2;
        long j13 = in[9];
        out[10] = ((j8 * j8) + (j6 * j9) + (j4 * j12) + (((j5 * j11) + (j3 * j13)) * 2)) * 2;
        out[11] = ((j8 * j9) + (j6 * j11) + (j5 * j12) + (j4 * j13)) * 2;
        out[12] = (j9 * j9) + (((j6 * j12) + (((j8 * j11) + (j5 * j13)) * 2)) * 2);
        out[13] = ((j9 * j11) + (j8 * j12) + (j6 * j13)) * 2;
        out[14] = ((j11 * j11) + (j9 * j12) + (j8 * 2 * j13)) * 2;
        out[15] = ((j11 * j12) + (j9 * j13)) * 2;
        out[16] = (j12 * j12) + (j11 * 4 * j13);
        out[17] = j12 * 2 * j13;
        out[18] = 2 * j13 * j13;
    }

    static void square(long[] output, long[] in) {
        long[] jArr = new long[19];
        squareInner(jArr, in);
        reduce(jArr, output);
    }

    static long[] expand(byte[] input) {
        long[] jArr = new long[10];
        for (int i = 0; i < 10; i++) {
            int i2 = EXPAND_START[i];
            jArr[i] = ((((((long) (input[i2] & UByte.MAX_VALUE)) | (((long) (input[i2 + 1] & UByte.MAX_VALUE)) << 8)) | (((long) (input[i2 + 2] & UByte.MAX_VALUE)) << 16)) | (((long) (input[i2 + 3] & UByte.MAX_VALUE)) << 24)) >> EXPAND_SHIFT[i]) & ((long) MASK[i & 1]);
        }
        return jArr;
    }

    public static byte[] contract(long[] inputLimbs) {
        int i;
        long[] jArrCopyOf = Arrays.copyOf(inputLimbs, 10);
        int i2 = 0;
        while (true) {
            if (i2 >= 2) {
                break;
            }
            int i3 = 0;
            while (i3 < 9) {
                long j = jArrCopyOf[i3];
                int i4 = SHIFT[i3 & 1];
                int i5 = -((int) (((j >> 31) & j) >> i4));
                jArrCopyOf[i3] = j + ((long) (i5 << i4));
                i3++;
                jArrCopyOf[i3] = jArrCopyOf[i3] - ((long) i5);
            }
            long j2 = jArrCopyOf[9];
            int i6 = -((int) (((j2 >> 31) & j2) >> 25));
            jArrCopyOf[9] = j2 + ((long) (i6 << 25));
            jArrCopyOf[0] = jArrCopyOf[0] - (((long) i6) * 19);
            i2++;
        }
        long j3 = jArrCopyOf[0];
        int i7 = -((int) (((j3 >> 31) & j3) >> 26));
        jArrCopyOf[0] = j3 + ((long) (i7 << 26));
        jArrCopyOf[1] = jArrCopyOf[1] - ((long) i7);
        int i8 = 0;
        for (i = 2; i8 < i; i = 2) {
            int i9 = 0;
            while (i9 < 9) {
                long j4 = jArrCopyOf[i9];
                int i10 = i9 & 1;
                int i11 = (int) (j4 >> SHIFT[i10]);
                jArrCopyOf[i9] = j4 & ((long) MASK[i10]);
                i9++;
                jArrCopyOf[i9] = jArrCopyOf[i9] + ((long) i11);
            }
            i8++;
        }
        long j5 = jArrCopyOf[9];
        jArrCopyOf[9] = j5 & 33554431;
        long j6 = jArrCopyOf[0] + (((long) ((int) (j5 >> 25))) * 19);
        jArrCopyOf[0] = j6;
        int iGte = gte((int) j6, 67108845);
        for (int i12 = 1; i12 < 10; i12++) {
            iGte &= eq((int) jArrCopyOf[i12], MASK[i12 & 1]);
        }
        jArrCopyOf[0] = jArrCopyOf[0] - ((long) (67108845 & iGte));
        long j7 = 33554431 & iGte;
        jArrCopyOf[1] = jArrCopyOf[1] - j7;
        for (int i13 = 2; i13 < 10; i13 += 2) {
            jArrCopyOf[i13] = jArrCopyOf[i13] - ((long) (67108863 & iGte));
            int i14 = i13 + 1;
            jArrCopyOf[i14] = jArrCopyOf[i14] - j7;
        }
        for (int i15 = 0; i15 < 10; i15++) {
            jArrCopyOf[i15] = jArrCopyOf[i15] << EXPAND_SHIFT[i15];
        }
        byte[] bArr = new byte[32];
        for (int i16 = 0; i16 < 10; i16++) {
            int i17 = EXPAND_START[i16];
            long j8 = bArr[i17];
            long j9 = jArrCopyOf[i16];
            bArr[i17] = (byte) (j8 | (j9 & 255));
            int i18 = i17 + 1;
            bArr[i18] = (byte) (((long) bArr[i18]) | ((j9 >> 8) & 255));
            int i19 = i17 + 2;
            bArr[i19] = (byte) (((long) bArr[i19]) | ((j9 >> 16) & 255));
            int i20 = i17 + 3;
            bArr[i20] = (byte) (((long) bArr[i20]) | ((j9 >> 24) & 255));
        }
        return bArr;
    }

    static void inverse(long[] out, long[] z) {
        long[] jArr = new long[10];
        long[] jArr2 = new long[10];
        long[] jArr3 = new long[10];
        long[] jArr4 = new long[10];
        long[] jArr5 = new long[10];
        long[] jArr6 = new long[10];
        long[] jArr7 = new long[10];
        long[] jArr8 = new long[10];
        long[] jArr9 = new long[10];
        long[] jArr10 = new long[10];
        square(jArr, z);
        square(jArr10, jArr);
        square(jArr9, jArr10);
        mult(jArr2, jArr9, z);
        mult(jArr3, jArr2, jArr);
        square(jArr9, jArr3);
        mult(jArr4, jArr9, jArr2);
        square(jArr9, jArr4);
        square(jArr10, jArr9);
        square(jArr9, jArr10);
        square(jArr10, jArr9);
        square(jArr9, jArr10);
        mult(jArr5, jArr9, jArr4);
        square(jArr9, jArr5);
        square(jArr10, jArr9);
        for (int i = 2; i < 10; i += 2) {
            square(jArr9, jArr10);
            square(jArr10, jArr9);
        }
        mult(jArr6, jArr10, jArr5);
        square(jArr9, jArr6);
        square(jArr10, jArr9);
        for (int i2 = 2; i2 < 20; i2 += 2) {
            square(jArr9, jArr10);
            square(jArr10, jArr9);
        }
        mult(jArr9, jArr10, jArr6);
        square(jArr10, jArr9);
        square(jArr9, jArr10);
        for (int i3 = 2; i3 < 10; i3 += 2) {
            square(jArr10, jArr9);
            square(jArr9, jArr10);
        }
        mult(jArr7, jArr9, jArr5);
        square(jArr9, jArr7);
        square(jArr10, jArr9);
        for (int i4 = 2; i4 < 50; i4 += 2) {
            square(jArr9, jArr10);
            square(jArr10, jArr9);
        }
        mult(jArr8, jArr10, jArr7);
        square(jArr10, jArr8);
        square(jArr9, jArr10);
        for (int i5 = 2; i5 < 100; i5 += 2) {
            square(jArr10, jArr9);
            square(jArr9, jArr10);
        }
        mult(jArr10, jArr9, jArr8);
        square(jArr9, jArr10);
        square(jArr10, jArr9);
        for (int i6 = 2; i6 < 50; i6 += 2) {
            square(jArr9, jArr10);
            square(jArr10, jArr9);
        }
        mult(jArr9, jArr10, jArr7);
        square(jArr10, jArr9);
        square(jArr9, jArr10);
        square(jArr10, jArr9);
        square(jArr9, jArr10);
        square(jArr10, jArr9);
        mult(out, jArr10, jArr3);
    }

    private Field25519() {
    }
}
