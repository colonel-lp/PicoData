package com.google.crypto.tink.integration.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.BinaryKeysetReader;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.KeyTemplate;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.KeysetManager;
import com.google.crypto.tink.KeysetWriter;
import com.google.crypto.tink.LegacyKeysetSerialization;
import com.google.crypto.tink.TinkProtoParametersFormat;
import com.google.crypto.tink.subtle.Hex;
import java.io.CharConversionException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.ProviderException;

/* JADX INFO: loaded from: classes2.dex */
public final class AndroidKeysetManager {
    private static final String TAG = "AndroidKeysetManager";
    private static final Object lock = new Object();
    private KeysetManager keysetManager;
    private final Aead masterAead;
    private final KeysetWriter writer;

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean isAtLeastM() {
        return true;
    }

    private AndroidKeysetManager(Builder builder) {
        this.writer = new SharedPrefKeysetWriter(builder.context, builder.keysetName, builder.prefFileName);
        this.masterAead = builder.masterAead;
        this.keysetManager = builder.keysetManager;
    }

    public static final class Builder {
        private KeysetManager keysetManager;
        private Context context = null;
        private String keysetName = null;
        private String prefFileName = null;
        private String masterKeyUri = null;
        private Aead masterAead = null;
        private boolean useKeystore = true;
        private KeyTemplate keyTemplate = null;
        private com.google.crypto.tink.proto.KeyTemplate keyTemplateProto = null;

        public Builder withSharedPref(Context context, String keysetName, String prefFileName) throws IOException {
            if (context == null) {
                throw new IllegalArgumentException("need an Android context");
            }
            if (keysetName == null) {
                throw new IllegalArgumentException("need a keyset name");
            }
            this.context = context;
            this.keysetName = keysetName;
            this.prefFileName = prefFileName;
            return this;
        }

        public Builder withMasterKeyUri(String val) {
            if (!val.startsWith(AndroidKeystoreKmsClient.PREFIX)) {
                throw new IllegalArgumentException("key URI must start with android-keystore://");
            }
            if (!this.useKeystore) {
                throw new IllegalArgumentException("cannot call withMasterKeyUri() after calling doNotUseKeystore()");
            }
            this.masterKeyUri = val;
            return this;
        }

        public Builder withKeyTemplate(com.google.crypto.tink.proto.KeyTemplate val) {
            this.keyTemplateProto = val;
            return this;
        }

        public Builder withKeyTemplate(KeyTemplate val) {
            this.keyTemplate = val;
            return this;
        }

        @Deprecated
        public Builder doNotUseKeystore() {
            this.masterKeyUri = null;
            this.useKeystore = false;
            return this;
        }

        private static byte[] readKeysetFromPrefs(Context context, String keysetName, String prefFileName) throws IOException {
            SharedPreferences sharedPreferences;
            if (keysetName == null) {
                throw new IllegalArgumentException("keysetName cannot be null");
            }
            Context applicationContext = context.getApplicationContext();
            if (prefFileName == null) {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
            } else {
                sharedPreferences = applicationContext.getSharedPreferences(prefFileName, 0);
            }
            try {
                String string = sharedPreferences.getString(keysetName, null);
                if (string == null) {
                    return null;
                }
                return Hex.decode(string);
            } catch (ClassCastException | IllegalArgumentException unused) {
                throw new CharConversionException(String.format("can't read keyset; the pref value %s is not a valid hex string", keysetName));
            }
        }

        private KeysetManager readKeysetInCleartext(byte[] serializedKeyset) throws GeneralSecurityException, IOException {
            return KeysetManager.withKeysetHandle(LegacyKeysetSerialization.parseKeyset(BinaryKeysetReader.withBytes(serializedKeyset), InsecureSecretKeyAccess.get()));
        }

        public synchronized AndroidKeysetManager build() throws GeneralSecurityException, IOException {
            AndroidKeysetManager androidKeysetManager;
            if (this.keysetName == null) {
                throw new IllegalArgumentException("keysetName cannot be null");
            }
            com.google.crypto.tink.proto.KeyTemplate keyTemplate = this.keyTemplateProto;
            if (keyTemplate != null && this.keyTemplate == null) {
                this.keyTemplate = KeyTemplate.createFrom(TinkProtoParametersFormat.parse(keyTemplate.toByteArray()));
            }
            synchronized (AndroidKeysetManager.lock) {
                byte[] keysetFromPrefs = readKeysetFromPrefs(this.context, this.keysetName, this.prefFileName);
                if (keysetFromPrefs == null) {
                    if (this.masterKeyUri != null) {
                        this.masterAead = readOrGenerateNewMasterKey();
                    }
                    this.keysetManager = generateKeysetAndWriteToPrefs();
                } else if (this.masterKeyUri == null || !AndroidKeysetManager.isAtLeastM()) {
                    this.keysetManager = readKeysetInCleartext(keysetFromPrefs);
                } else {
                    this.keysetManager = readMasterkeyDecryptAndParseKeyset(keysetFromPrefs);
                }
                androidKeysetManager = new AndroidKeysetManager(this);
            }
            return androidKeysetManager;
        }

        private Aead readOrGenerateNewMasterKey() throws GeneralSecurityException {
            if (!AndroidKeysetManager.isAtLeastM()) {
                Log.w(AndroidKeysetManager.TAG, "Android Keystore requires at least Android M");
                return null;
            }
            AndroidKeystoreKmsClient androidKeystoreKmsClient = new AndroidKeystoreKmsClient();
            try {
                boolean zGenerateKeyIfNotExist = AndroidKeystoreKmsClient.generateKeyIfNotExist(this.masterKeyUri);
                try {
                    return androidKeystoreKmsClient.getAead(this.masterKeyUri);
                } catch (GeneralSecurityException | ProviderException e) {
                    if (zGenerateKeyIfNotExist) {
                        Log.w(AndroidKeysetManager.TAG, "cannot use Android Keystore, it'll be disabled", e);
                        return null;
                    }
                    throw new KeyStoreException(String.format("the master key %s exists but is unusable", this.masterKeyUri), e);
                }
            } catch (GeneralSecurityException | ProviderException e2) {
                Log.w(AndroidKeysetManager.TAG, "cannot use Android Keystore, it'll be disabled", e2);
                return null;
            }
        }

        private KeysetManager generateKeysetAndWriteToPrefs() throws GeneralSecurityException, IOException {
            KeyTemplate keyTemplate = this.keyTemplate;
            if (keyTemplate == null) {
                throw new GeneralSecurityException("cannot read or generate keyset");
            }
            KeysetHandle keysetHandleGenerateNew = KeysetHandle.generateNew(keyTemplate);
            AndroidKeysetManager.write(keysetHandleGenerateNew, new SharedPrefKeysetWriter(this.context, this.keysetName, this.prefFileName), this.masterAead);
            return KeysetManager.withKeysetHandle(keysetHandleGenerateNew);
        }

        private KeysetManager readMasterkeyDecryptAndParseKeyset(byte[] serializedKeyset) throws GeneralSecurityException, IOException {
            try {
                this.masterAead = new AndroidKeystoreKmsClient().getAead(this.masterKeyUri);
                try {
                    return KeysetManager.withKeysetHandle(LegacyKeysetSerialization.parseEncryptedKeyset(BinaryKeysetReader.withBytes(serializedKeyset), this.masterAead, new byte[0]));
                } catch (IOException | GeneralSecurityException e) {
                    try {
                        return readKeysetInCleartext(serializedKeyset);
                    } catch (IOException unused) {
                        throw e;
                    }
                }
            } catch (GeneralSecurityException | ProviderException e2) {
                try {
                    KeysetManager keysetInCleartext = readKeysetInCleartext(serializedKeyset);
                    Log.w(AndroidKeysetManager.TAG, "cannot use Android Keystore, it'll be disabled", e2);
                    return keysetInCleartext;
                } catch (IOException unused2) {
                    throw e2;
                }
            }
        }
    }

    public synchronized KeysetHandle getKeysetHandle() throws GeneralSecurityException {
        return this.keysetManager.getKeysetHandle();
    }

    @Deprecated
    public synchronized AndroidKeysetManager rotate(com.google.crypto.tink.proto.KeyTemplate keyTemplate) throws GeneralSecurityException {
        KeysetManager keysetManagerRotate = this.keysetManager.rotate(keyTemplate);
        this.keysetManager = keysetManagerRotate;
        write(keysetManagerRotate.getKeysetHandle(), this.writer, this.masterAead);
        return this;
    }

    public synchronized AndroidKeysetManager add(com.google.crypto.tink.proto.KeyTemplate keyTemplate) throws GeneralSecurityException {
        KeysetManager keysetManagerAdd = this.keysetManager.add(keyTemplate);
        this.keysetManager = keysetManagerAdd;
        write(keysetManagerAdd.getKeysetHandle(), this.writer, this.masterAead);
        return this;
    }

    public synchronized AndroidKeysetManager add(KeyTemplate keyTemplate) throws GeneralSecurityException {
        KeysetManager keysetManagerAdd = this.keysetManager.add(keyTemplate);
        this.keysetManager = keysetManagerAdd;
        write(keysetManagerAdd.getKeysetHandle(), this.writer, this.masterAead);
        return this;
    }

    public synchronized AndroidKeysetManager setPrimary(int keyId) throws GeneralSecurityException {
        KeysetManager primary = this.keysetManager.setPrimary(keyId);
        this.keysetManager = primary;
        write(primary.getKeysetHandle(), this.writer, this.masterAead);
        return this;
    }

    @Deprecated
    public synchronized AndroidKeysetManager promote(int keyId) throws GeneralSecurityException {
        return setPrimary(keyId);
    }

    public synchronized AndroidKeysetManager enable(int keyId) throws GeneralSecurityException {
        KeysetManager keysetManagerEnable = this.keysetManager.enable(keyId);
        this.keysetManager = keysetManagerEnable;
        write(keysetManagerEnable.getKeysetHandle(), this.writer, this.masterAead);
        return this;
    }

    public synchronized AndroidKeysetManager disable(int keyId) throws GeneralSecurityException {
        KeysetManager keysetManagerDisable = this.keysetManager.disable(keyId);
        this.keysetManager = keysetManagerDisable;
        write(keysetManagerDisable.getKeysetHandle(), this.writer, this.masterAead);
        return this;
    }

    public synchronized AndroidKeysetManager delete(int keyId) throws GeneralSecurityException {
        KeysetManager keysetManagerDelete = this.keysetManager.delete(keyId);
        this.keysetManager = keysetManagerDelete;
        write(keysetManagerDelete.getKeysetHandle(), this.writer, this.masterAead);
        return this;
    }

    public synchronized AndroidKeysetManager destroy(int keyId) throws GeneralSecurityException {
        KeysetManager keysetManagerDestroy = this.keysetManager.destroy(keyId);
        this.keysetManager = keysetManagerDestroy;
        write(keysetManagerDestroy.getKeysetHandle(), this.writer, this.masterAead);
        return this;
    }

    public synchronized boolean isUsingKeystore() {
        return this.masterAead != null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void write(KeysetHandle handle, KeysetWriter writer, Aead masterAead) throws GeneralSecurityException {
        try {
            if (masterAead != null) {
                LegacyKeysetSerialization.serializeEncryptedKeyset(handle, writer, masterAead, new byte[0]);
            } else {
                LegacyKeysetSerialization.serializeKeyset(handle, writer, InsecureSecretKeyAccess.get());
            }
        } catch (IOException e) {
            throw new GeneralSecurityException(e);
        }
    }
}
