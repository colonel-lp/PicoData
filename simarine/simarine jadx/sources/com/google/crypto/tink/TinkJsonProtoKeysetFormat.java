package com.google.crypto.tink;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public final class TinkJsonProtoKeysetFormat {
    public static KeysetHandle parseKeyset(String serializedKeyset, SecretKeyAccess access) throws GeneralSecurityException {
        if (access == null) {
            throw new NullPointerException("SecretKeyAccess cannot be null");
        }
        try {
            return CleartextKeysetHandle.read(JsonKeysetReader.withString(serializedKeyset));
        } catch (IOException unused) {
            throw new GeneralSecurityException("Parse keyset failed");
        }
    }

    public static String serializeKeyset(KeysetHandle keysetHandle, SecretKeyAccess access) throws GeneralSecurityException {
        if (access == null) {
            throw new NullPointerException("SecretKeyAccess cannot be null");
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withOutputStream(byteArrayOutputStream));
            return new String(byteArrayOutputStream.toByteArray(), com.google.crypto.tink.internal.Util.UTF_8);
        } catch (IOException unused) {
            throw new GeneralSecurityException("Serialize keyset failed");
        }
    }

    public static KeysetHandle parseKeysetWithoutSecret(String serializedKeyset) throws GeneralSecurityException {
        try {
            return KeysetHandle.readNoSecret(JsonKeysetReader.withString(serializedKeyset));
        } catch (IOException unused) {
            throw new GeneralSecurityException("Parse keyset failed");
        }
    }

    public static String serializeKeysetWithoutSecret(KeysetHandle keysetHandle) throws GeneralSecurityException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            keysetHandle.writeNoSecret(JsonKeysetWriter.withOutputStream(byteArrayOutputStream));
            return new String(byteArrayOutputStream.toByteArray(), com.google.crypto.tink.internal.Util.UTF_8);
        } catch (IOException unused) {
            throw new GeneralSecurityException("Serialize keyset failed");
        }
    }

    public static KeysetHandle parseEncryptedKeyset(String serializedEncryptedKeyset, Aead keysetEncryptionAead, byte[] associatedData) throws GeneralSecurityException {
        try {
            return KeysetHandle.readWithAssociatedData(JsonKeysetReader.withString(serializedEncryptedKeyset), keysetEncryptionAead, associatedData);
        } catch (IOException unused) {
            throw new GeneralSecurityException("Parse keyset failed");
        }
    }

    public static String serializeEncryptedKeyset(KeysetHandle keysetHandle, Aead keysetEncryptionAead, byte[] associatedData) throws GeneralSecurityException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            keysetHandle.writeWithAssociatedData(JsonKeysetWriter.withOutputStream(byteArrayOutputStream), keysetEncryptionAead, associatedData);
            return new String(byteArrayOutputStream.toByteArray(), com.google.crypto.tink.internal.Util.UTF_8);
        } catch (IOException unused) {
            throw new GeneralSecurityException("Serialize keyset failed");
        }
    }

    private TinkJsonProtoKeysetFormat() {
    }
}
