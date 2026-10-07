package com.google.crypto.tink.aead;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.internal.OutputPrefixUtil;
import com.google.crypto.tink.util.Bytes;
import com.google.crypto.tink.util.SecretBytes;
import java.security.GeneralSecurityException;
import java.util.Objects;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class AesCtrHmacAeadKey extends AeadKey {
    private final SecretBytes aesKeyBytes;
    private final SecretBytes hmacKeyBytes;

    @Nullable
    private final Integer idRequirement;
    private final Bytes outputPrefix;
    private final AesCtrHmacAeadParameters parameters;

    public static class Builder {

        @Nullable
        private SecretBytes aesKeyBytes;

        @Nullable
        private SecretBytes hmacKeyBytes;

        @Nullable
        private Integer idRequirement;

        @Nullable
        private AesCtrHmacAeadParameters parameters;

        private Builder() {
            this.parameters = null;
            this.aesKeyBytes = null;
            this.hmacKeyBytes = null;
            this.idRequirement = null;
        }

        public Builder setParameters(AesCtrHmacAeadParameters parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder setAesKeyBytes(SecretBytes aesKeyBytes) {
            this.aesKeyBytes = aesKeyBytes;
            return this;
        }

        public Builder setHmacKeyBytes(SecretBytes hmacKeyBytes) {
            this.hmacKeyBytes = hmacKeyBytes;
            return this;
        }

        public Builder setIdRequirement(@Nullable Integer idRequirement) {
            this.idRequirement = idRequirement;
            return this;
        }

        private Bytes getOutputPrefix() {
            if (this.parameters.getVariant() == AesCtrHmacAeadParameters.Variant.NO_PREFIX) {
                return OutputPrefixUtil.EMPTY_PREFIX;
            }
            if (this.parameters.getVariant() == AesCtrHmacAeadParameters.Variant.CRUNCHY) {
                return OutputPrefixUtil.getLegacyOutputPrefix(this.idRequirement.intValue());
            }
            if (this.parameters.getVariant() == AesCtrHmacAeadParameters.Variant.TINK) {
                return OutputPrefixUtil.getTinkOutputPrefix(this.idRequirement.intValue());
            }
            throw new IllegalStateException("Unknown AesCtrHmacAeadParameters.Variant: " + this.parameters.getVariant());
        }

        public AesCtrHmacAeadKey build() throws GeneralSecurityException {
            AesCtrHmacAeadParameters aesCtrHmacAeadParameters = this.parameters;
            if (aesCtrHmacAeadParameters == null) {
                throw new GeneralSecurityException("Cannot build without parameters");
            }
            if (this.aesKeyBytes == null || this.hmacKeyBytes == null) {
                throw new GeneralSecurityException("Cannot build without key material");
            }
            if (aesCtrHmacAeadParameters.getAesKeySizeBytes() != this.aesKeyBytes.size()) {
                throw new GeneralSecurityException("AES key size mismatch");
            }
            if (this.parameters.getHmacKeySizeBytes() != this.hmacKeyBytes.size()) {
                throw new GeneralSecurityException("HMAC key size mismatch");
            }
            if (this.parameters.hasIdRequirement() && this.idRequirement == null) {
                throw new GeneralSecurityException("Cannot create key without ID requirement with parameters with ID requirement");
            }
            if (!this.parameters.hasIdRequirement() && this.idRequirement != null) {
                throw new GeneralSecurityException("Cannot create key with ID requirement with parameters without ID requirement");
            }
            return new AesCtrHmacAeadKey(this.parameters, this.aesKeyBytes, this.hmacKeyBytes, getOutputPrefix(), this.idRequirement);
        }
    }

    private AesCtrHmacAeadKey(AesCtrHmacAeadParameters parameters, SecretBytes aesKeyBytes, SecretBytes hmacKeyBytes, Bytes outputPrefix, @Nullable Integer idRequirement) {
        this.parameters = parameters;
        this.aesKeyBytes = aesKeyBytes;
        this.hmacKeyBytes = hmacKeyBytes;
        this.outputPrefix = outputPrefix;
        this.idRequirement = idRequirement;
    }

    public static Builder builder() {
        return new Builder();
    }

    public SecretBytes getAesKeyBytes() {
        return this.aesKeyBytes;
    }

    public SecretBytes getHmacKeyBytes() {
        return this.hmacKeyBytes;
    }

    @Override // com.google.crypto.tink.aead.AeadKey
    public Bytes getOutputPrefix() {
        return this.outputPrefix;
    }

    @Override // com.google.crypto.tink.aead.AeadKey, com.google.crypto.tink.Key
    public AesCtrHmacAeadParameters getParameters() {
        return this.parameters;
    }

    @Override // com.google.crypto.tink.Key
    @Nullable
    public Integer getIdRequirementOrNull() {
        return this.idRequirement;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof AesCtrHmacAeadKey)) {
            return false;
        }
        AesCtrHmacAeadKey aesCtrHmacAeadKey = (AesCtrHmacAeadKey) o;
        return aesCtrHmacAeadKey.parameters.equals(this.parameters) && aesCtrHmacAeadKey.aesKeyBytes.equalsSecretBytes(this.aesKeyBytes) && aesCtrHmacAeadKey.hmacKeyBytes.equalsSecretBytes(this.hmacKeyBytes) && Objects.equals(aesCtrHmacAeadKey.idRequirement, this.idRequirement);
    }
}
