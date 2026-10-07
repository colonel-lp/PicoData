package com.google.crypto.tink.daead;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.internal.OutputPrefixUtil;
import com.google.crypto.tink.util.Bytes;
import com.google.crypto.tink.util.SecretBytes;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.util.Objects;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class AesSivKey extends DeterministicAeadKey {

    @Nullable
    private final Integer idRequirement;
    private final SecretBytes keyBytes;
    private final Bytes outputPrefix;
    private final AesSivParameters parameters;

    public static class Builder {

        @Nullable
        private Integer idRequirement;

        @Nullable
        private SecretBytes keyBytes;

        @Nullable
        private AesSivParameters parameters;

        private Builder() {
            this.parameters = null;
            this.keyBytes = null;
            this.idRequirement = null;
        }

        public Builder setParameters(AesSivParameters parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder setKeyBytes(SecretBytes keyBytes) {
            this.keyBytes = keyBytes;
            return this;
        }

        public Builder setIdRequirement(@Nullable Integer idRequirement) {
            this.idRequirement = idRequirement;
            return this;
        }

        public AesSivKey build() throws GeneralSecurityException {
            AesSivParameters aesSivParameters = this.parameters;
            if (aesSivParameters == null || this.keyBytes == null) {
                throw new IllegalArgumentException("Cannot build without parameters and/or key material");
            }
            if (aesSivParameters.getKeySizeBytes() != this.keyBytes.size()) {
                throw new GeneralSecurityException("Key size mismatch");
            }
            if (this.parameters.hasIdRequirement() && this.idRequirement == null) {
                throw new GeneralSecurityException("Cannot create key without ID requirement with parameters with ID requirement");
            }
            if (!this.parameters.hasIdRequirement() && this.idRequirement != null) {
                throw new GeneralSecurityException("Cannot create key with ID requirement with parameters without ID requirement");
            }
            return new AesSivKey(this.parameters, this.keyBytes, getOutputPrefix(), this.idRequirement);
        }

        private Bytes getOutputPrefix() {
            if (this.parameters.getVariant() == AesSivParameters.Variant.NO_PREFIX) {
                return OutputPrefixUtil.EMPTY_PREFIX;
            }
            if (this.parameters.getVariant() == AesSivParameters.Variant.CRUNCHY) {
                return OutputPrefixUtil.getLegacyOutputPrefix(this.idRequirement.intValue());
            }
            if (this.parameters.getVariant() == AesSivParameters.Variant.TINK) {
                return OutputPrefixUtil.getTinkOutputPrefix(this.idRequirement.intValue());
            }
            throw new IllegalStateException("Unknown AesSivParameters.Variant: " + this.parameters.getVariant());
        }
    }

    private AesSivKey(AesSivParameters parameters, SecretBytes keyBytes, Bytes outputPrefix, @Nullable Integer idRequirement) {
        this.parameters = parameters;
        this.keyBytes = keyBytes;
        this.outputPrefix = outputPrefix;
        this.idRequirement = idRequirement;
    }

    public static Builder builder() {
        return new Builder();
    }

    public SecretBytes getKeyBytes() {
        return this.keyBytes;
    }

    @Override // com.google.crypto.tink.daead.DeterministicAeadKey
    public Bytes getOutputPrefix() {
        return this.outputPrefix;
    }

    @Override // com.google.crypto.tink.daead.DeterministicAeadKey, com.google.crypto.tink.Key
    public AesSivParameters getParameters() {
        return this.parameters;
    }

    @Override // com.google.crypto.tink.Key
    @Nullable
    public Integer getIdRequirementOrNull() {
        return this.idRequirement;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof AesSivKey)) {
            return false;
        }
        AesSivKey aesSivKey = (AesSivKey) o;
        return aesSivKey.parameters.equals(this.parameters) && aesSivKey.keyBytes.equalsSecretBytes(this.keyBytes) && Objects.equals(aesSivKey.idRequirement, this.idRequirement);
    }
}
