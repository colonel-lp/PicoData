package com.google.crypto.tink.integration.android;

import android.util.Log;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KmsClient;
import com.google.crypto.tink.subtle.Random;
import com.google.crypto.tink.subtle.Validators;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.util.Arrays;
import java.util.Locale;

/* JADX INFO: loaded from: classes2.dex */
public final class AndroidKeystoreKmsClient implements KmsClient {
    private static final int MAX_WAIT_TIME_MILLISECONDS_BEFORE_RETRY = 40;
    public static final String PREFIX = "android-keystore://";
    private static final String TAG = "AndroidKeystoreKmsClient";
    private static final Object keystoreLock = new Object();
    private final String keyUri;

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean isAtLeastM() {
        return true;
    }

    public AndroidKeystoreKmsClient() throws GeneralSecurityException {
        this(new Builder());
    }

    @Deprecated
    public AndroidKeystoreKmsClient(String uri) {
        this(new Builder().setKeyUri(uri));
    }

    private AndroidKeystoreKmsClient(Builder builder) {
        this.keyUri = builder.keyUri;
    }

    public static final class Builder {
        String keyUri = null;

        public Builder() {
            if (!AndroidKeystoreKmsClient.isAtLeastM()) {
                throw new IllegalStateException("need Android Keystore on Android M or newer");
            }
        }

        public Builder setKeyUri(String val) {
            if (val == null || !val.toLowerCase(Locale.US).startsWith(AndroidKeystoreKmsClient.PREFIX)) {
                throw new IllegalArgumentException("val must start with android-keystore://");
            }
            this.keyUri = val;
            return this;
        }

        public AndroidKeystoreKmsClient build() {
            return new AndroidKeystoreKmsClient(this);
        }
    }

    @Override // com.google.crypto.tink.KmsClient
    public boolean doesSupport(String uri) {
        String str = this.keyUri;
        if (str == null || !str.equals(uri)) {
            return this.keyUri == null && uri.toLowerCase(Locale.US).startsWith(PREFIX);
        }
        return true;
    }

    @Override // com.google.crypto.tink.KmsClient
    public KmsClient withCredentials(String unused) throws GeneralSecurityException {
        return new AndroidKeystoreKmsClient();
    }

    @Override // com.google.crypto.tink.KmsClient
    public KmsClient withDefaultCredentials() throws GeneralSecurityException {
        return new AndroidKeystoreKmsClient();
    }

    @Override // com.google.crypto.tink.KmsClient
    public Aead getAead(String uri) throws GeneralSecurityException {
        Aead aeadValidateAead;
        String str = this.keyUri;
        if (str != null && !str.equals(uri)) {
            throw new GeneralSecurityException(String.format("this client is bound to %s, cannot load keys bound to %s", this.keyUri, uri));
        }
        try {
            synchronized (keystoreLock) {
                aeadValidateAead = validateAead(new AndroidKeystoreAesGcm(Validators.validateKmsKeyUriAndRemovePrefix(PREFIX, uri)));
            }
            return aeadValidateAead;
        } catch (IOException e) {
            throw new GeneralSecurityException(e);
        }
    }

    public void deleteKey(String keyUri) throws GeneralSecurityException {
        String strValidateKmsKeyUriAndRemovePrefix = Validators.validateKmsKeyUriAndRemovePrefix(PREFIX, keyUri);
        synchronized (keystoreLock) {
            AndroidKeystore.deleteKey(strValidateKmsKeyUriAndRemovePrefix);
        }
    }

    boolean hasKey(String keyUri) throws GeneralSecurityException {
        boolean zHasKey;
        String strValidateKmsKeyUriAndRemovePrefix = Validators.validateKmsKeyUriAndRemovePrefix(PREFIX, keyUri);
        try {
            synchronized (keystoreLock) {
                zHasKey = AndroidKeystore.hasKey(strValidateKmsKeyUriAndRemovePrefix);
            }
            return zHasKey;
        } catch (NullPointerException unused) {
            Log.w(TAG, "Keystore is temporarily unavailable, wait, reinitialize Keystore and try again.");
            sleepRandomAmount();
            synchronized (keystoreLock) {
                return AndroidKeystore.hasKey(strValidateKmsKeyUriAndRemovePrefix);
            }
        }
    }

    private static void sleepRandomAmount() {
        try {
            Thread.sleep((int) (Math.random() * 40.0d));
        } catch (InterruptedException unused) {
        }
    }

    public static Aead getOrGenerateNewAeadKey(String keyUri) throws GeneralSecurityException, IOException {
        Aead aeadValidateAead;
        String strValidateKmsKeyUriAndRemovePrefix = Validators.validateKmsKeyUriAndRemovePrefix(PREFIX, keyUri);
        synchronized (keystoreLock) {
            if (!AndroidKeystore.hasKey(strValidateKmsKeyUriAndRemovePrefix)) {
                AndroidKeystore.generateNewAes256GcmKey(strValidateKmsKeyUriAndRemovePrefix);
            }
            aeadValidateAead = validateAead(new AndroidKeystoreAesGcm(strValidateKmsKeyUriAndRemovePrefix));
        }
        return aeadValidateAead;
    }

    public static void generateNewAeadKey(String keyUri) throws GeneralSecurityException {
        synchronized (keystoreLock) {
            String strValidateKmsKeyUriAndRemovePrefix = Validators.validateKmsKeyUriAndRemovePrefix(PREFIX, keyUri);
            if (AndroidKeystore.hasKey(strValidateKmsKeyUriAndRemovePrefix)) {
                throw new IllegalArgumentException(String.format("cannot generate a new key %s because it already exists; please delete it with deleteKey() and try again", keyUri));
            }
            AndroidKeystore.generateNewAes256GcmKey(strValidateKmsKeyUriAndRemovePrefix);
        }
    }

    static boolean generateKeyIfNotExist(String keyUri) throws GeneralSecurityException {
        synchronized (keystoreLock) {
            String strValidateKmsKeyUriAndRemovePrefix = Validators.validateKmsKeyUriAndRemovePrefix(PREFIX, keyUri);
            if (AndroidKeystore.hasKey(strValidateKmsKeyUriAndRemovePrefix)) {
                return false;
            }
            AndroidKeystore.generateNewAes256GcmKey(strValidateKmsKeyUriAndRemovePrefix);
            return true;
        }
    }

    private static Aead validateAead(Aead aead) throws GeneralSecurityException {
        byte[] bArrRandBytes = Random.randBytes(10);
        byte[] bArr = new byte[0];
        if (Arrays.equals(bArrRandBytes, aead.decrypt(aead.encrypt(bArrRandBytes, bArr), bArr))) {
            return aead;
        }
        throw new KeyStoreException("cannot use Android Keystore: encryption/decryption of non-empty message and empty aad returns an incorrect result");
    }
}
