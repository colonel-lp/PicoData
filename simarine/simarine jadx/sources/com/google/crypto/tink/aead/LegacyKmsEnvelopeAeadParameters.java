package com.google.crypto.tink.aead;

import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.util.Objects;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class LegacyKmsEnvelopeAeadParameters extends AeadParameters {
    private final AeadParameters dekParametersForNewKeys;
    private final DekParsingStrategy dekParsingStrategy;
    private final String kekUri;
    private final Variant variant;

    @Immutable
    public static final class Variant {
        private final String name;
        public static final Variant TINK = new Variant("TINK");
        public static final Variant NO_PREFIX = new Variant("NO_PREFIX");

        private Variant(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    @Immutable
    public static final class DekParsingStrategy {
        private final String name;
        public static final DekParsingStrategy ASSUME_AES_GCM = new DekParsingStrategy("ASSUME_AES_GCM");
        public static final DekParsingStrategy ASSUME_XCHACHA20POLY1305 = new DekParsingStrategy("ASSUME_XCHACHA20POLY1305");
        public static final DekParsingStrategy ASSUME_CHACHA20POLY1305 = new DekParsingStrategy("ASSUME_CHACHA20POLY1305");
        public static final DekParsingStrategy ASSUME_AES_CTR_HMAC = new DekParsingStrategy("ASSUME_AES_CTR_HMAC");
        public static final DekParsingStrategy ASSUME_AES_EAX = new DekParsingStrategy("ASSUME_AES_EAX");
        public static final DekParsingStrategy ASSUME_AES_GCM_SIV = new DekParsingStrategy("ASSUME_AES_GCM_SIV");

        private DekParsingStrategy(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    private LegacyKmsEnvelopeAeadParameters(Variant variant, String kekUri, DekParsingStrategy dekParsingStrategy, AeadParameters dekParametersForNewKeys) {
        this.variant = variant;
        this.kekUri = kekUri;
        this.dekParsingStrategy = dekParsingStrategy;
        this.dekParametersForNewKeys = dekParametersForNewKeys;
    }

    public static class Builder {

        @Nullable
        private AeadParameters dekParametersForNewKeys;

        @Nullable
        private DekParsingStrategy dekParsingStrategy;

        @Nullable
        private String kekUri;

        @Nullable
        private Variant variant;

        private Builder() {
        }

        public Builder setVariant(Variant variant) {
            this.variant = variant;
            return this;
        }

        public Builder setKekUri(String kekUri) {
            this.kekUri = kekUri;
            return this;
        }

        public Builder setDekParsingStrategy(DekParsingStrategy dekParsingStrategy) {
            this.dekParsingStrategy = dekParsingStrategy;
            return this;
        }

        public Builder setDekParametersForNewKeys(AeadParameters aeadParameters) {
            this.dekParametersForNewKeys = aeadParameters;
            return this;
        }

        private static boolean parsingStrategyAllowed(DekParsingStrategy parsingStrategy, AeadParameters aeadParameters) {
            if (parsingStrategy.equals(DekParsingStrategy.ASSUME_AES_GCM) && (aeadParameters instanceof AesGcmParameters)) {
                return true;
            }
            if (parsingStrategy.equals(DekParsingStrategy.ASSUME_CHACHA20POLY1305) && (aeadParameters instanceof ChaCha20Poly1305Parameters)) {
                return true;
            }
            if (parsingStrategy.equals(DekParsingStrategy.ASSUME_XCHACHA20POLY1305) && (aeadParameters instanceof XChaCha20Poly1305Parameters)) {
                return true;
            }
            if (parsingStrategy.equals(DekParsingStrategy.ASSUME_AES_CTR_HMAC) && (aeadParameters instanceof AesCtrHmacAeadParameters)) {
                return true;
            }
            if (parsingStrategy.equals(DekParsingStrategy.ASSUME_AES_EAX) && (aeadParameters instanceof AesEaxParameters)) {
                return true;
            }
            return parsingStrategy.equals(DekParsingStrategy.ASSUME_AES_GCM_SIV) && (aeadParameters instanceof AesGcmSivParameters);
        }

        public LegacyKmsEnvelopeAeadParameters build() throws GeneralSecurityException {
            if (this.variant == null) {
                this.variant = Variant.NO_PREFIX;
            }
            if (this.kekUri == null) {
                throw new GeneralSecurityException("kekUri must be set");
            }
            if (this.dekParsingStrategy == null) {
                throw new GeneralSecurityException("dekParsingStrategy must be set");
            }
            AeadParameters aeadParameters = this.dekParametersForNewKeys;
            if (aeadParameters == null) {
                throw new GeneralSecurityException("dekParametersForNewKeys must be set");
            }
            if (aeadParameters.hasIdRequirement()) {
                throw new GeneralSecurityException("dekParametersForNewKeys must not have ID Requirements");
            }
            if (!parsingStrategyAllowed(this.dekParsingStrategy, this.dekParametersForNewKeys)) {
                throw new GeneralSecurityException("Cannot use parsing strategy " + this.dekParsingStrategy.toString() + " when new keys are picked according to " + this.dekParametersForNewKeys + ".");
            }
            return new LegacyKmsEnvelopeAeadParameters(this.variant, this.kekUri, this.dekParsingStrategy, this.dekParametersForNewKeys);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getKekUri() {
        return this.kekUri;
    }

    public Variant getVariant() {
        return this.variant;
    }

    @Override // com.google.crypto.tink.Parameters
    public boolean hasIdRequirement() {
        return this.variant != Variant.NO_PREFIX;
    }

    public DekParsingStrategy getDekParsingStrategy() {
        return this.dekParsingStrategy;
    }

    public AeadParameters getDekParametersForNewKeys() {
        return this.dekParametersForNewKeys;
    }

    public boolean equals(Object o) {
        if (!(o instanceof LegacyKmsEnvelopeAeadParameters)) {
            return false;
        }
        LegacyKmsEnvelopeAeadParameters legacyKmsEnvelopeAeadParameters = (LegacyKmsEnvelopeAeadParameters) o;
        return legacyKmsEnvelopeAeadParameters.dekParsingStrategy.equals(this.dekParsingStrategy) && legacyKmsEnvelopeAeadParameters.dekParametersForNewKeys.equals(this.dekParametersForNewKeys) && legacyKmsEnvelopeAeadParameters.kekUri.equals(this.kekUri) && legacyKmsEnvelopeAeadParameters.variant.equals(this.variant);
    }

    public int hashCode() {
        return Objects.hash(LegacyKmsEnvelopeAeadParameters.class, this.kekUri, this.dekParsingStrategy, this.dekParametersForNewKeys, this.variant);
    }

    public String toString() {
        return "LegacyKmsEnvelopeAead Parameters (kekUri: " + this.kekUri + ", dekParsingStrategy: " + this.dekParsingStrategy + ", dekParametersForNewKeys: " + this.dekParametersForNewKeys + ", variant: " + this.variant + ")";
    }
}
