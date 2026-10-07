package com.google.crypto.tink.signature;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.internal.OutputPrefixUtil;
import com.google.crypto.tink.util.Bytes;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Objects;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class RsaSsaPkcs1PublicKey extends SignaturePublicKey {

    @Nullable
    private final Integer idRequirement;
    private final BigInteger modulus;
    private final Bytes outputPrefix;
    private final RsaSsaPkcs1Parameters parameters;

    public static class Builder {

        @Nullable
        private Integer idRequirement;

        @Nullable
        private BigInteger modulus;

        @Nullable
        private RsaSsaPkcs1Parameters parameters;

        private Builder() {
            this.parameters = null;
            this.modulus = null;
            this.idRequirement = null;
        }

        public Builder setParameters(RsaSsaPkcs1Parameters parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder setModulus(BigInteger modulus) {
            this.modulus = modulus;
            return this;
        }

        public Builder setIdRequirement(@Nullable Integer idRequirement) {
            this.idRequirement = idRequirement;
            return this;
        }

        private Bytes getOutputPrefix() {
            if (this.parameters.getVariant() == RsaSsaPkcs1Parameters.Variant.NO_PREFIX) {
                return OutputPrefixUtil.EMPTY_PREFIX;
            }
            if (this.parameters.getVariant() == RsaSsaPkcs1Parameters.Variant.LEGACY || this.parameters.getVariant() == RsaSsaPkcs1Parameters.Variant.CRUNCHY) {
                return OutputPrefixUtil.getLegacyOutputPrefix(this.idRequirement.intValue());
            }
            if (this.parameters.getVariant() == RsaSsaPkcs1Parameters.Variant.TINK) {
                return OutputPrefixUtil.getTinkOutputPrefix(this.idRequirement.intValue());
            }
            throw new IllegalStateException("Unknown RsaSsaPkcs1Parameters.Variant: " + this.parameters.getVariant());
        }

        public RsaSsaPkcs1PublicKey build() throws GeneralSecurityException {
            if (this.parameters == null) {
                throw new GeneralSecurityException("Cannot build without parameters");
            }
            BigInteger bigInteger = this.modulus;
            if (bigInteger == null) {
                throw new GeneralSecurityException("Cannot build without modulus");
            }
            int iBitLength = bigInteger.bitLength();
            int modulusSizeBits = this.parameters.getModulusSizeBits();
            if (iBitLength != modulusSizeBits) {
                throw new GeneralSecurityException("Got modulus size " + iBitLength + ", but parameters requires modulus size " + modulusSizeBits);
            }
            if (this.parameters.hasIdRequirement() && this.idRequirement == null) {
                throw new GeneralSecurityException("Cannot create key without ID requirement with parameters with ID requirement");
            }
            if (!this.parameters.hasIdRequirement() && this.idRequirement != null) {
                throw new GeneralSecurityException("Cannot create key with ID requirement with parameters without ID requirement");
            }
            return new RsaSsaPkcs1PublicKey(this.parameters, this.modulus, getOutputPrefix(), this.idRequirement);
        }
    }

    private RsaSsaPkcs1PublicKey(RsaSsaPkcs1Parameters parameters, BigInteger modulus, Bytes outputPrefix, @Nullable Integer idRequirement) {
        this.parameters = parameters;
        this.modulus = modulus;
        this.outputPrefix = outputPrefix;
        this.idRequirement = idRequirement;
    }

    public static Builder builder() {
        return new Builder();
    }

    public BigInteger getModulus() {
        return this.modulus;
    }

    @Override // com.google.crypto.tink.signature.SignaturePublicKey
    public Bytes getOutputPrefix() {
        return this.outputPrefix;
    }

    @Override // com.google.crypto.tink.signature.SignaturePublicKey, com.google.crypto.tink.Key
    public RsaSsaPkcs1Parameters getParameters() {
        return this.parameters;
    }

    @Override // com.google.crypto.tink.Key
    @Nullable
    public Integer getIdRequirementOrNull() {
        return this.idRequirement;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof RsaSsaPkcs1PublicKey)) {
            return false;
        }
        RsaSsaPkcs1PublicKey rsaSsaPkcs1PublicKey = (RsaSsaPkcs1PublicKey) o;
        return rsaSsaPkcs1PublicKey.parameters.equals(this.parameters) && rsaSsaPkcs1PublicKey.modulus.equals(this.modulus) && Objects.equals(rsaSsaPkcs1PublicKey.idRequirement, this.idRequirement);
    }
}
