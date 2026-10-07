package com.google.crypto.tink.integration.android;

import android.security.keystore.KeyGenParameterSpec;
import com.google.crypto.tink.Aead;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/* JADX INFO: loaded from: classes2.dex */
public final class AndroidKeystore {
    private static boolean isAtLeastM() {
        return true;
    }

    public static void generateNewAes256GcmKey(String alias) throws GeneralSecurityException {
        generateNewKeyWithSpec(new KeyGenParameterSpec.Builder(alias, 3).setKeySize(256).setBlockModes("GCM").setEncryptionPaddings("NoPadding").build());
    }

    public static void generateNewKeyWithSpec(KeyGenParameterSpec spec) throws GeneralSecurityException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
        keyGenerator.init(spec);
        keyGenerator.generateKey();
    }

    public static Aead getAead(String alias) throws GeneralSecurityException {
        return new AeadImpl(alias, getAndroidKeyStore());
    }

    public static void deleteKey(String alias) throws GeneralSecurityException {
        getAndroidKeyStore().deleteEntry(alias);
    }

    public static boolean hasKey(String alias) throws GeneralSecurityException {
        return getAndroidKeyStore().containsAlias(alias);
    }

    private static KeyStore getAndroidKeyStore() throws GeneralSecurityException {
        if (!isAtLeastM()) {
            throw new IllegalStateException("Need Android Keystore on Android M or newer");
        }
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            return keyStore;
        } catch (IOException e) {
            throw new GeneralSecurityException(e);
        }
    }

    private static final class AeadImpl implements Aead {
        private static final int IV_SIZE_IN_BYTES = 12;
        private static final int TAG_SIZE_IN_BYTES = 16;
        private final SecretKey key;

        public AeadImpl(String alias, KeyStore keyStore) throws GeneralSecurityException {
            SecretKey secretKey = (SecretKey) keyStore.getKey(alias, null);
            this.key = secretKey;
            if (secretKey != null) {
                return;
            }
            throw new InvalidKeyException("Keystore cannot load the key with ID: " + alias);
        }

        @Override // com.google.crypto.tink.Aead
        public byte[] encrypt(final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
            if (plaintext.length > 2147483619) {
                throw new GeneralSecurityException("plaintext too long");
            }
            byte[] bArr = new byte[plaintext.length + 28];
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(1, this.key);
            cipher.updateAAD(associatedData);
            cipher.doFinal(plaintext, 0, plaintext.length, bArr, 12);
            byte[] iv = cipher.getIV();
            if (iv.length != 12) {
                throw new GeneralSecurityException("IV has unexpected length");
            }
            System.arraycopy(iv, 0, bArr, 0, 12);
            return bArr;
        }

        @Override // com.google.crypto.tink.Aead
        public byte[] decrypt(final byte[] ciphertext, final byte[] associatedData) throws GeneralSecurityException {
            if (ciphertext.length < 28) {
                throw new BadPaddingException("ciphertext too short");
            }
            GCMParameterSpec gCMParameterSpec = new GCMParameterSpec(128, ciphertext, 0, 12);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(2, this.key, gCMParameterSpec);
            cipher.updateAAD(associatedData);
            return cipher.doFinal(ciphertext, 12, ciphertext.length - 12);
        }
    }

    private AndroidKeystore() {
    }
}
