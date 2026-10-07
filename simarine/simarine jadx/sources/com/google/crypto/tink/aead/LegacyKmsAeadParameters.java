package com.google.crypto.tink.aead;

import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.util.Objects;

/* JADX INFO: loaded from: classes2.dex */
public final class LegacyKmsAeadParameters extends AeadParameters {
    private final String keyUri;
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

    private LegacyKmsAeadParameters(String keyUri, Variant variant) {
        this.keyUri = keyUri;
        this.variant = variant;
    }

    public static LegacyKmsAeadParameters create(String keyUri) throws GeneralSecurityException {
        return new LegacyKmsAeadParameters(keyUri, Variant.NO_PREFIX);
    }

    public static LegacyKmsAeadParameters create(String keyUri, Variant variant) {
        return new LegacyKmsAeadParameters(keyUri, variant);
    }

    public String keyUri() {
        return this.keyUri;
    }

    public Variant variant() {
        return this.variant;
    }

    @Override // com.google.crypto.tink.Parameters
    public boolean hasIdRequirement() {
        return this.variant != Variant.NO_PREFIX;
    }

    public boolean equals(Object o) {
        if (!(o instanceof LegacyKmsAeadParameters)) {
            return false;
        }
        LegacyKmsAeadParameters legacyKmsAeadParameters = (LegacyKmsAeadParameters) o;
        return legacyKmsAeadParameters.keyUri.equals(this.keyUri) && legacyKmsAeadParameters.variant.equals(this.variant);
    }

    public int hashCode() {
        return Objects.hash(LegacyKmsAeadParameters.class, this.keyUri, this.variant);
    }

    public String toString() {
        return "LegacyKmsAead Parameters (keyUri: " + this.keyUri + ", variant: " + this.variant + ")";
    }
}
