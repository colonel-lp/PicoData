package com.google.crypto.tink.integration.android;

import android.util.Log;
import com.google.crypto.tink.Aead;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.ProviderException;
import javax.crypto.BadPaddingException;

/* JADX INFO: loaded from: classes2.dex */
public final class AndroidKeystoreAesGcm implements Aead {
    private static final int MAX_WAIT_TIME_MILLISECONDS_BEFORE_RETRY = 100;
    private static final String TAG = "AndroidKeystoreAesGcm";
    private final Aead keystoreAead;

    public AndroidKeystoreAesGcm(String keyId) throws GeneralSecurityException, IOException {
        this.keystoreAead = AndroidKeystore.getAead(keyId);
    }

    @Override // com.google.crypto.tink.Aead
    public byte[] encrypt(final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
        try {
            return this.keystoreAead.encrypt(plaintext, associatedData);
        } catch (GeneralSecurityException | ProviderException e) {
            Log.w(TAG, "encountered a potentially transient KeyStore error, will wait and retry", e);
            sleepRandomAmount();
            return this.keystoreAead.encrypt(plaintext, associatedData);
        }
    }

    @Override // com.google.crypto.tink.Aead
    public byte[] decrypt(final byte[] ciphertext, final byte[] associatedData) throws GeneralSecurityException {
        try {
            return this.keystoreAead.decrypt(ciphertext, associatedData);
        } catch (BadPaddingException e) {
            throw e;
        } catch (GeneralSecurityException | ProviderException e2) {
            Log.w(TAG, "encountered a potentially transient KeyStore error, will wait and retry", e2);
            sleepRandomAmount();
            return this.keystoreAead.decrypt(ciphertext, associatedData);
        }
    }

    private static void sleepRandomAmount() {
        try {
            Thread.sleep((int) (Math.random() * 100.0d));
        } catch (InterruptedException unused) {
        }
    }
}
