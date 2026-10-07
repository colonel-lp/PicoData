package net.dot.android.crypto;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

/* JADX INFO: loaded from: classes2.dex */
public final class PalPbkdf2 {
    private static final int ERROR_UNSUPPORTED_ALGORITHM = -1;
    private static final int SUCCESS = 1;

    public static int pbkdf2OneShot(String str, byte[] bArr, ByteBuffer byteBuffer, int i, ByteBuffer byteBuffer2) throws InvalidKeyException, IllegalArgumentException, ShortBufferException {
        if (str == null || bArr == null || byteBuffer2 == null) {
            throw new IllegalArgumentException("algorithmName, password, and destination must not be null.");
        }
        String str2 = "Hmac" + str;
        try {
            Mac mac = Mac.getInstance(str2);
            if (bArr.length == 0) {
                bArr = new byte[]{0};
            }
            if (byteBuffer != null) {
                byteBuffer.mark();
            }
            mac.init(new SecretKeySpec(bArr, str2));
            byte[] bArr2 = new byte[4];
            int macLength = mac.getMacLength();
            byte[] bArr3 = new byte[macLength];
            byte[] bArr4 = new byte[macLength];
            int i2 = 1;
            int i3 = 0;
            while (i3 < byteBuffer2.capacity()) {
                writeBigEndianInt(i2, bArr2);
                if (byteBuffer != null) {
                    mac.update(byteBuffer);
                    byteBuffer.reset();
                }
                mac.update(bArr2);
                mac.doFinal(bArr4, 0);
                System.arraycopy(bArr4, 0, bArr3, 0, macLength);
                for (int i4 = 2; i4 <= i; i4++) {
                    mac.update(bArr4);
                    mac.doFinal(bArr4, 0);
                    for (int i5 = 0; i5 < macLength; i5++) {
                        bArr3[i5] = (byte) (bArr3[i5] ^ bArr4[i5]);
                    }
                }
                byteBuffer2.put(bArr3, 0, Math.min(macLength, byteBuffer2.capacity() - i3));
                i3 += macLength;
                i2++;
            }
            return 1;
        } catch (NoSuchAlgorithmException unused) {
            return -1;
        }
    }

    private static void writeBigEndianInt(int i, byte[] bArr) {
        bArr[0] = (byte) ((i >> 24) & 255);
        bArr[1] = (byte) ((i >> 16) & 255);
        bArr[2] = (byte) ((i >> 8) & 255);
        bArr[3] = (byte) (i & 255);
    }
}
