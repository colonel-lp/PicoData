package com.google.crypto.tink.internal;

import java.lang.reflect.Array;
import java.math.BigInteger;

/* JADX INFO: loaded from: classes2.dex */
final class Ed25519Constants {
    static final Ed25519.CachedXYT[] B2;
    static final Ed25519.CachedXYT[][] B_TABLE;
    static final long[] D;
    static final long[] D2;
    private static final BigInteger D2_BI;
    private static final BigInteger D_BI;
    private static final BigInteger P_BI;
    static final long[] SQRTM1;
    private static final BigInteger SQRTM1_BI;

    static {
        BigInteger bigIntegerSubtract = BigInteger.valueOf(2L).pow(255).subtract(BigInteger.valueOf(19L));
        P_BI = bigIntegerSubtract;
        BigInteger bigIntegerMod = BigInteger.valueOf(-121665L).multiply(BigInteger.valueOf(121666L).modInverse(bigIntegerSubtract)).mod(bigIntegerSubtract);
        D_BI = bigIntegerMod;
        BigInteger bigIntegerMod2 = BigInteger.valueOf(2L).multiply(bigIntegerMod).mod(bigIntegerSubtract);
        D2_BI = bigIntegerMod2;
        BigInteger bigIntegerModPow = BigInteger.valueOf(2L).modPow(bigIntegerSubtract.subtract(BigInteger.ONE).divide(BigInteger.valueOf(4L)), bigIntegerSubtract);
        SQRTM1_BI = bigIntegerModPow;
        Point point = new Point();
        point.y = BigInteger.valueOf(4L).multiply(BigInteger.valueOf(5L).modInverse(bigIntegerSubtract)).mod(bigIntegerSubtract);
        point.x = recoverX(point.y);
        D = Field25519.expand(toLittleEndian(bigIntegerMod));
        D2 = Field25519.expand(toLittleEndian(bigIntegerMod2));
        SQRTM1 = Field25519.expand(toLittleEndian(bigIntegerModPow));
        B_TABLE = (Ed25519.CachedXYT[][]) Array.newInstance((Class<?>) Ed25519.CachedXYT.class, 32, 8);
        Point pointEdwards = point;
        for (int i = 0; i < 32; i++) {
            Point pointEdwards2 = pointEdwards;
            for (int i2 = 0; i2 < 8; i2++) {
                B_TABLE[i][i2] = getCachedXYT(pointEdwards2);
                pointEdwards2 = edwards(pointEdwards2, pointEdwards);
            }
            for (int i3 = 0; i3 < 8; i3++) {
                pointEdwards = edwards(pointEdwards, pointEdwards);
            }
        }
        Point pointEdwards3 = edwards(point, point);
        B2 = new Ed25519.CachedXYT[8];
        for (int i4 = 0; i4 < 8; i4++) {
            B2[i4] = getCachedXYT(point);
            point = edwards(point, pointEdwards3);
        }
    }

    private static class Point {
        private BigInteger x;
        private BigInteger y;

        private Point() {
        }
    }

    private static BigInteger recoverX(BigInteger y) {
        BigInteger bigIntegerSubtract = y.pow(2).subtract(BigInteger.ONE);
        BigInteger bigIntegerAdd = D_BI.multiply(y.pow(2)).add(BigInteger.ONE);
        BigInteger bigInteger = P_BI;
        BigInteger bigIntegerMultiply = bigIntegerSubtract.multiply(bigIntegerAdd.modInverse(bigInteger));
        BigInteger bigIntegerModPow = bigIntegerMultiply.modPow(bigInteger.add(BigInteger.valueOf(3L)).divide(BigInteger.valueOf(8L)), bigInteger);
        if (!bigIntegerModPow.pow(2).subtract(bigIntegerMultiply).mod(bigInteger).equals(BigInteger.ZERO)) {
            bigIntegerModPow = bigIntegerModPow.multiply(SQRTM1_BI).mod(bigInteger);
        }
        return bigIntegerModPow.testBit(0) ? bigInteger.subtract(bigIntegerModPow) : bigIntegerModPow;
    }

    private static Point edwards(Point a, Point b) {
        Point point = new Point();
        BigInteger bigIntegerMultiply = D_BI.multiply(a.x.multiply(b.x).multiply(a.y).multiply(b.y));
        BigInteger bigInteger = P_BI;
        BigInteger bigIntegerMod = bigIntegerMultiply.mod(bigInteger);
        point.x = a.x.multiply(b.y).add(b.x.multiply(a.y)).multiply(BigInteger.ONE.add(bigIntegerMod).modInverse(bigInteger)).mod(bigInteger);
        point.y = a.y.multiply(b.y).add(a.x.multiply(b.x)).multiply(BigInteger.ONE.subtract(bigIntegerMod).modInverse(bigInteger)).mod(bigInteger);
        return point;
    }

    private static byte[] toLittleEndian(BigInteger n) {
        byte[] bArr = new byte[32];
        byte[] byteArray = n.toByteArray();
        System.arraycopy(byteArray, 0, bArr, 32 - byteArray.length, byteArray.length);
        for (int i = 0; i < 16; i++) {
            byte b = bArr[i];
            int i2 = 31 - i;
            bArr[i] = bArr[i2];
            bArr[i2] = b;
        }
        return bArr;
    }

    private static Ed25519.CachedXYT getCachedXYT(Point p) {
        BigInteger bigIntegerAdd = p.y.add(p.x);
        BigInteger bigInteger = P_BI;
        return new Ed25519.CachedXYT(Field25519.expand(toLittleEndian(bigIntegerAdd.mod(bigInteger))), Field25519.expand(toLittleEndian(p.y.subtract(p.x).mod(bigInteger))), Field25519.expand(toLittleEndian(D2_BI.multiply(p.x).multiply(p.y).mod(bigInteger))));
    }

    private Ed25519Constants() {
    }
}
