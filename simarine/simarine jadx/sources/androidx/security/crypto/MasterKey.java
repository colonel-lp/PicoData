package androidx.security.crypto;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/* JADX INFO: loaded from: classes.dex */
@Deprecated
public final class MasterKey {

    @Deprecated
    public static final int DEFAULT_AES_GCM_MASTER_KEY_SIZE = 256;
    private static final int DEFAULT_AUTHENTICATION_VALIDITY_DURATION_SECONDS = 300;

    @Deprecated
    public static final String DEFAULT_MASTER_KEY_ALIAS = "_androidx_security_master_key_";
    static final String KEYSTORE_PATH_URI = "android-keystore://";
    private final String mKeyAlias;
    private final KeyGenParameterSpec mKeyGenParameterSpec;

    @Deprecated
    public enum KeyScheme {
        AES256_GCM
    }

    public static int getDefaultAuthenticationValidityDurationSeconds() {
        return 300;
    }

    MasterKey(String str, Object obj) {
        this.mKeyAlias = str;
        this.mKeyGenParameterSpec = (KeyGenParameterSpec) obj;
    }

    public boolean isKeyStoreBacked() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            return keyStore.containsAlias(this.mKeyAlias);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException unused) {
            return false;
        }
    }

    public boolean isUserAuthenticationRequired() {
        KeyGenParameterSpec keyGenParameterSpec = this.mKeyGenParameterSpec;
        return keyGenParameterSpec != null && Api23Impl.isUserAuthenticationRequired(keyGenParameterSpec);
    }

    public int getUserAuthenticationValidityDurationSeconds() {
        KeyGenParameterSpec keyGenParameterSpec = this.mKeyGenParameterSpec;
        if (keyGenParameterSpec == null) {
            return 0;
        }
        return Api23Impl.getUserAuthenticationValidityDurationSeconds(keyGenParameterSpec);
    }

    public boolean isStrongBoxBacked() {
        KeyGenParameterSpec keyGenParameterSpec = this.mKeyGenParameterSpec;
        if (keyGenParameterSpec == null) {
            return false;
        }
        return Api28Impl.isStrongBoxBacked(keyGenParameterSpec);
    }

    public String toString() {
        return "MasterKey{keyAlias=" + this.mKeyAlias + ", isKeyStoreBacked=" + isKeyStoreBacked() + "}";
    }

    String getKeyAlias() {
        return this.mKeyAlias;
    }

    @Deprecated
    public static final class Builder {
        boolean mAuthenticationRequired;
        final Context mContext;
        final String mKeyAlias;
        KeyGenParameterSpec mKeyGenParameterSpec;
        KeyScheme mKeyScheme;
        boolean mRequestStrongBoxBacked;
        int mUserAuthenticationValidityDurationSeconds;

        public Builder(Context context) {
            this(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS);
        }

        public Builder(Context context, String str) {
            this.mContext = context.getApplicationContext();
            this.mKeyAlias = str;
        }

        public Builder setKeyScheme(KeyScheme keyScheme) {
            if (keyScheme.ordinal() == 0) {
                if (this.mKeyGenParameterSpec != null) {
                    throw new IllegalArgumentException("KeyScheme set after setting a KeyGenParamSpec");
                }
                this.mKeyScheme = keyScheme;
                return this;
            }
            throw new IllegalArgumentException("Unsupported scheme: " + keyScheme);
        }

        public Builder setUserAuthenticationRequired(boolean z) {
            return setUserAuthenticationRequired(z, MasterKey.getDefaultAuthenticationValidityDurationSeconds());
        }

        public Builder setUserAuthenticationRequired(boolean z, int i) {
            this.mAuthenticationRequired = z;
            this.mUserAuthenticationValidityDurationSeconds = i;
            return this;
        }

        public Builder setRequestStrongBoxBacked(boolean z) {
            this.mRequestStrongBoxBacked = z;
            return this;
        }

        public Builder setKeyGenParameterSpec(KeyGenParameterSpec keyGenParameterSpec) {
            if (this.mKeyScheme != null) {
                throw new IllegalArgumentException("KeyGenParamSpec set after setting a KeyScheme");
            }
            if (!this.mKeyAlias.equals(Api23Impl.getKeystoreAlias(keyGenParameterSpec))) {
                throw new IllegalArgumentException("KeyGenParamSpec's key alias does not match provided alias (" + this.mKeyAlias + " vs " + Api23Impl.getKeystoreAlias(keyGenParameterSpec));
            }
            this.mKeyGenParameterSpec = keyGenParameterSpec;
            return this;
        }

        public MasterKey build() throws GeneralSecurityException, IOException {
            return Api23Impl.build(this);
        }

        static class Api23Impl {
            private Api23Impl() {
            }

            static String getKeystoreAlias(KeyGenParameterSpec keyGenParameterSpec) {
                return keyGenParameterSpec.getKeystoreAlias();
            }

            static MasterKey build(Builder builder) throws GeneralSecurityException, IOException {
                if (builder.mKeyScheme == null && builder.mKeyGenParameterSpec == null) {
                    throw new IllegalArgumentException("build() called before setKeyGenParameterSpec or setKeyScheme.");
                }
                if (builder.mKeyScheme == KeyScheme.AES256_GCM) {
                    KeyGenParameterSpec.Builder keySize = new KeyGenParameterSpec.Builder(builder.mKeyAlias, 3).setBlockModes("GCM").setEncryptionPaddings("NoPadding").setKeySize(256);
                    if (builder.mAuthenticationRequired) {
                        keySize.setUserAuthenticationRequired(true);
                        Api30Impl.setUserAuthenticationParameters(keySize, builder.mUserAuthenticationValidityDurationSeconds, 3);
                    }
                    if (builder.mRequestStrongBoxBacked && builder.mContext.getPackageManager().hasSystemFeature("android.hardware.strongbox_keystore")) {
                        Api28Impl.setIsStrongBoxBacked(keySize);
                    }
                    builder.mKeyGenParameterSpec = keySize.build();
                }
                if (builder.mKeyGenParameterSpec == null) {
                    throw new NullPointerException("KeyGenParameterSpec was null after build() check");
                }
                return new MasterKey(MasterKeys.getOrCreate(builder.mKeyGenParameterSpec), builder.mKeyGenParameterSpec);
            }

            static class Api28Impl {
                private Api28Impl() {
                }

                static void setIsStrongBoxBacked(KeyGenParameterSpec.Builder builder) {
                    builder.setIsStrongBoxBacked(true);
                }
            }

            static class Api30Impl {
                private Api30Impl() {
                }

                static void setUserAuthenticationParameters(KeyGenParameterSpec.Builder builder, int i, int i2) {
                    builder.setUserAuthenticationParameters(i, i2);
                }
            }
        }
    }

    static class Api23Impl {
        private Api23Impl() {
        }

        static boolean isUserAuthenticationRequired(KeyGenParameterSpec keyGenParameterSpec) {
            return keyGenParameterSpec.isUserAuthenticationRequired();
        }

        static int getUserAuthenticationValidityDurationSeconds(KeyGenParameterSpec keyGenParameterSpec) {
            return keyGenParameterSpec.getUserAuthenticationValidityDurationSeconds();
        }
    }

    static class Api28Impl {
        private Api28Impl() {
        }

        static boolean isStrongBoxBacked(KeyGenParameterSpec keyGenParameterSpec) {
            return keyGenParameterSpec.isStrongBoxBacked();
        }
    }
}
