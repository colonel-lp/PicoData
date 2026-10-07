package com.google.crypto.tink.internal;

import com.bumptech.glide.load.Key;
import com.google.crypto.tink.SecretKeyAccess;
import com.google.crypto.tink.util.Bytes;
import com.google.crypto.tink.util.SecretBytes;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Objects;
import javax.annotation.Nullable;
import kotlin.UByte;

/* JADX INFO: loaded from: classes2.dex */
public final class Util {
    public static final Charset UTF_8 = Charset.forName(Key.STRING_CHARSET_NAME);

    public static int randKeyId() {
        int i = 0;
        while (i == 0) {
            byte[] bArrRandBytes = Random.randBytes(4);
            i = (bArrRandBytes[3] & UByte.MAX_VALUE) | ((bArrRandBytes[0] & UByte.MAX_VALUE) << 24) | ((bArrRandBytes[1] & UByte.MAX_VALUE) << 16) | ((bArrRandBytes[2] & UByte.MAX_VALUE) << 8);
        }
        return i;
    }

    private static final byte toByteFromPrintableAscii(char c) {
        if (c >= '!' && c <= '~') {
            return (byte) c;
        }
        throw new TinkBugException("Not a printable ASCII character: " + c);
    }

    private static final byte checkedToByteFromPrintableAscii(char c) throws GeneralSecurityException {
        if (c >= '!' && c <= '~') {
            return (byte) c;
        }
        throw new GeneralSecurityException("Not a printable ASCII character: " + c);
    }

    public static final Bytes toBytesFromPrintableAscii(String s) {
        byte[] bArr = new byte[s.length()];
        for (int i = 0; i < s.length(); i++) {
            bArr[i] = toByteFromPrintableAscii(s.charAt(i));
        }
        return Bytes.copyFrom(bArr);
    }

    public static final Bytes checkedToBytesFromPrintableAscii(String s) throws GeneralSecurityException {
        byte[] bArr = new byte[s.length()];
        for (int i = 0; i < s.length(); i++) {
            bArr[i] = checkedToByteFromPrintableAscii(s.charAt(i));
        }
        return Bytes.copyFrom(bArr);
    }

    public static boolean isAndroid() {
        return Objects.equals(System.getProperty("java.vendor"), "The Android Project");
    }

    @Nullable
    public static Integer getAndroidApiLevel() {
        if (isAndroid()) {
            return BuildDispatchedCode.getApiLevel();
        }
        return null;
    }

    public static boolean isPrefix(byte[] prefix, byte[] complete) {
        if (complete.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (complete[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    public static SecretBytes readIntoSecretBytes(InputStream input, int length, SecretKeyAccess access) throws GeneralSecurityException {
        byte[] bArr = new byte[length];
        int i = 0;
        while (i < length) {
            try {
                int i2 = input.read(bArr, i, length - i);
                if (i2 == -1) {
                    throw new GeneralSecurityException("Not enough pseudorandomness provided");
                }
                i += i2;
            } catch (IOException unused) {
                throw new GeneralSecurityException("Reading pseudorandomness failed");
            }
        }
        return SecretBytes.copyFrom(bArr, access);
    }

    private Util() {
    }
}
