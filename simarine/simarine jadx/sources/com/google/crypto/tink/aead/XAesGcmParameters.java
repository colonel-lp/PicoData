package com.google.crypto.tink.aead;

import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.util.Objects;

/* JADX INFO: loaded from: classes2.dex */
public final class XAesGcmParameters extends AeadParameters {
    private final int saltSizeBytes;
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

    public static XAesGcmParameters create(Variant variant, int saltSizeBytes) throws GeneralSecurityException {
        if (saltSizeBytes < 8 || saltSizeBytes > 12) {
            throw new GeneralSecurityException("Salt size must be between 8 and 12 bytes");
        }
        return new XAesGcmParameters(variant, saltSizeBytes);
    }

    private XAesGcmParameters(Variant variant, int saltSizeBytes) {
        this.variant = variant;
        this.saltSizeBytes = saltSizeBytes;
    }

    public Variant getVariant() {
        return this.variant;
    }

    public int getSaltSizeBytes() {
        return this.saltSizeBytes;
    }

    public boolean equals(Object o) {
        if (!(o instanceof XAesGcmParameters)) {
            return false;
        }
        XAesGcmParameters xAesGcmParameters = (XAesGcmParameters) o;
        return xAesGcmParameters.getVariant() == getVariant() && xAesGcmParameters.getSaltSizeBytes() == getSaltSizeBytes();
    }

    public int hashCode() {
        return Objects.hash(XAesGcmParameters.class, this.variant, Integer.valueOf(this.saltSizeBytes));
    }

    @Override // com.google.crypto.tink.Parameters
    public boolean hasIdRequirement() {
        return this.variant != Variant.NO_PREFIX;
    }

    public String toString() {
        return "X-AES-GCM Parameters (variant: " + this.variant + "salt_size_bytes: " + this.saltSizeBytes + ")";
    }
}
