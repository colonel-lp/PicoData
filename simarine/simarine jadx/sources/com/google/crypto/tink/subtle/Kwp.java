package com.google.crypto.tink.subtle;

import com.google.crypto.tink.KeyWrap;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import kotlin.UByte;

/* JADX INFO: loaded from: classes2.dex */
@Deprecated
public class Kwp implements KeyWrap {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final int MAX_WRAP_KEY_SIZE = 4096;
    static final int MIN_WRAP_KEY_SIZE = 16;
    static final byte[] PREFIX = {-90, 89, 89, -90};
    static final int ROUNDS = 6;
    private final SecretKey aesKey;

    public Kwp(final byte[] key) throws GeneralSecurityException {
        if (key.length != 16 && key.length != 32) {
            throw new GeneralSecurityException("Unsupported key length");
        }
        this.aesKey = new SecretKeySpec(key, "AES");
    }

    private int wrappingSize(int inputSize) {
        return inputSize + (7 - ((inputSize + 7) % 8)) + 8;
    }

    private byte[] computeW(final byte[] iv, final byte[] key) throws GeneralSecurityException {
        if (key.length <= 8 || key.length > 2147483631 || iv.length != 8) {
            throw new GeneralSecurityException("computeW called with invalid parameters");
        }
        int iWrappingSize = wrappingSize(key.length);
        byte[] bArr = new byte[iWrappingSize];
        System.arraycopy(iv, 0, bArr, 0, iv.length);
        System.arraycopy(key, 0, bArr, 8, key.length);
        int i = (iWrappingSize / 8) - 1;
        Cipher engineFactory = EngineFactory.CIPHER.getInstance("AES/ECB/NoPadding");
        engineFactory.init(1, this.aesKey);
        byte[] bArr2 = new byte[16];
        System.arraycopy(bArr, 0, bArr2, 0, 8);
        for (int i2 = 0; i2 < 6; i2++) {
            int i3 = 0;
            while (i3 < i) {
                int i4 = i3 + 1;
                int i5 = i4 * 8;
                System.arraycopy(bArr, i5, bArr2, 8, 8);
                engineFactory.doFinal(bArr2, 0, 16, bArr2);
                int i6 = (i2 * i) + i3 + 1;
                for (int i7 = 0; i7 < 4; i7++) {
                    int i8 = 7 - i7;
                    bArr2[i8] = (byte) (bArr2[i8] ^ ((byte) (i6 & 255)));
                    i6 >>>= 8;
                }
                System.arraycopy(bArr2, 8, bArr, i5, 8);
                i3 = i4;
            }
        }
        System.arraycopy(bArr2, 0, bArr, 0, 8);
        return bArr;
    }

    private byte[] invertW(final byte[] wrapped) throws GeneralSecurityException {
        if (wrapped.length < 24 || wrapped.length % 8 != 0) {
            throw new GeneralSecurityException("Incorrect data size");
        }
        byte[] bArrCopyOf = Arrays.copyOf(wrapped, wrapped.length);
        int length = bArrCopyOf.length / 8;
        int i = length - 1;
        Cipher engineFactory = EngineFactory.CIPHER.getInstance("AES/ECB/NoPadding");
        engineFactory.init(2, this.aesKey);
        byte[] bArr = new byte[16];
        System.arraycopy(bArrCopyOf, 0, bArr, 0, 8);
        for (int i2 = 5; i2 >= 0; i2--) {
            for (int i3 = length - 2; i3 >= 0; i3--) {
                int i4 = (i3 + 1) * 8;
                System.arraycopy(bArrCopyOf, i4, bArr, 8, 8);
                int i5 = (i2 * i) + i3 + 1;
                for (int i6 = 0; i6 < 4; i6++) {
                    int i7 = 7 - i6;
                    bArr[i7] = (byte) (((byte) (i5 & 255)) ^ bArr[i7]);
                    i5 >>>= 8;
                }
                engineFactory.doFinal(bArr, 0, 16, bArr);
                System.arraycopy(bArr, 8, bArrCopyOf, i4, 8);
            }
        }
        System.arraycopy(bArr, 0, bArrCopyOf, 0, 8);
        return bArrCopyOf;
    }

    @Override // com.google.crypto.tink.KeyWrap
    public byte[] wrap(final byte[] data) throws GeneralSecurityException {
        if (data.length < 16) {
            throw new GeneralSecurityException("Key size of key to wrap too small");
        }
        if (data.length > 4096) {
            throw new GeneralSecurityException("Key size of key to wrap too large");
        }
        byte[] bArr = new byte[8];
        byte[] bArr2 = PREFIX;
        System.arraycopy(bArr2, 0, bArr, 0, bArr2.length);
        for (int i = 0; i < 4; i++) {
            bArr[i + 4] = (byte) ((data.length >> ((3 - i) * 8)) & 255);
        }
        return computeW(bArr, data);
    }

    @Override // com.google.crypto.tink.KeyWrap
    public byte[] unwrap(final byte[] data) throws GeneralSecurityException {
        int i;
        if (data.length < wrappingSize(16)) {
            throw new GeneralSecurityException("Wrapped key size is too small");
        }
        if (data.length > wrappingSize(4096)) {
            throw new GeneralSecurityException("Wrapped key size is too large");
        }
        if (data.length % 8 != 0) {
            throw new GeneralSecurityException("Wrapped key size must be a multiple of 8 bytes");
        }
        byte[] bArrInvertW = invertW(data);
        boolean z = true;
        boolean z2 = false;
        int i2 = 0;
        while (true) {
            if (i2 >= 4) {
                break;
            }
            if (PREFIX[i2] != bArrInvertW[i2]) {
                z = false;
            }
            i2++;
        }
        int i3 = 0;
        for (i = 4; i < 8; i++) {
            i3 = (i3 << 8) + (bArrInvertW[i] & UByte.MAX_VALUE);
        }
        if (wrappingSize(i3) == bArrInvertW.length) {
            for (int i4 = i3 + 8; i4 < bArrInvertW.length; i4++) {
                if (bArrInvertW[i4] != 0) {
                    z = false;
                }
            }
            z2 = z;
        }
        if (z2) {
            return Arrays.copyOfRange(bArrInvertW, 8, i3 + 8);
        }
        throw new BadPaddingException("Invalid padding");
    }
}
