package com.google.crypto.tink.util;

import com.google.crypto.tink.SecretKeyAccess;
import com.google.crypto.tink.subtle.Random;
import com.google.errorprone.annotations.Immutable;
import java.security.MessageDigest;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class SecretBytes {
    private final Bytes bytes;

    private SecretBytes(Bytes bytes) {
        this.bytes = bytes;
    }

    public static SecretBytes copyFrom(byte[] value, SecretKeyAccess access) {
        if (access == null) {
            throw new NullPointerException("SecretKeyAccess required");
        }
        return new SecretBytes(Bytes.copyFrom(value));
    }

    public static SecretBytes randomBytes(int length) {
        return new SecretBytes(Bytes.copyFrom(Random.randBytes(length)));
    }

    public byte[] toByteArray(SecretKeyAccess access) {
        if (access == null) {
            throw new NullPointerException("SecretKeyAccess required");
        }
        return this.bytes.toByteArray();
    }

    public int size() {
        return this.bytes.size();
    }

    public boolean equalsSecretBytes(SecretBytes other) {
        return MessageDigest.isEqual(this.bytes.toByteArray(), other.bytes.toByteArray());
    }
}
