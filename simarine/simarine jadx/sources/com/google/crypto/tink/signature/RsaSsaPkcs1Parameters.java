package com.google.crypto.tink.signature;

import com.google.errorprone.annotations.Immutable;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Objects;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class RsaSsaPkcs1Parameters extends SignatureParameters {
    public static final BigInteger F4 = BigInteger.valueOf(65537);
    private final HashType hashType;
    private final int modulusSizeBits;
    private final BigInteger publicExponent;
    private final Variant variant;

    @Immutable
    public static final class Variant {
        private final String name;
        public static final Variant TINK = new Variant("TINK");
        public static final Variant CRUNCHY = new Variant("CRUNCHY");
        public static final Variant LEGACY = new Variant("LEGACY");
        public static final Variant NO_PREFIX = new Variant("NO_PREFIX");

        private Variant(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    @Immutable
    public static final class HashType {
        public static final HashType SHA256 = new HashType("SHA256");
        public static final HashType SHA384 = new HashType("SHA384");
        public static final HashType SHA512 = new HashType("SHA512");
        private final String name;

        private HashType(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    public static final class Builder {
        private static final BigInteger PUBLIC_EXPONENT_UPPER_BOUND;
        private static final BigInteger TWO;

        @Nullable
        private HashType hashType;

        @Nullable
        private Integer modulusSizeBits;

        @Nullable
        private BigInteger publicExponent;
        private Variant variant;

        private Builder() {
            this.modulusSizeBits = null;
            this.publicExponent = RsaSsaPkcs1Parameters.F4;
            this.hashType = null;
            this.variant = Variant.NO_PREFIX;
        }

        public Builder setModulusSizeBits(int modulusSizeBits) {
            this.modulusSizeBits = Integer.valueOf(modulusSizeBits);
            return this;
        }

        public Builder setPublicExponent(BigInteger e) {
            this.publicExponent = e;
            return this;
        }

        public Builder setVariant(Variant variant) {
            this.variant = variant;
            return this;
        }

        public Builder setHashType(HashType hashType) {
            this.hashType = hashType;
            return this;
        }

        static {
            BigInteger bigIntegerValueOf = BigInteger.valueOf(2L);
            TWO = bigIntegerValueOf;
            PUBLIC_EXPONENT_UPPER_BOUND = bigIntegerValueOf.pow(256);
        }

        private void validatePublicExponent(BigInteger publicExponent) throws InvalidAlgorithmParameterException {
            int iCompareTo = publicExponent.compareTo(RsaSsaPkcs1Parameters.F4);
            if (iCompareTo == 0) {
                return;
            }
            if (iCompareTo < 0) {
                throw new InvalidAlgorithmParameterException("Public exponent must be at least 65537.");
            }
            if (publicExponent.mod(TWO).equals(BigInteger.ZERO)) {
                throw new InvalidAlgorithmParameterException("Invalid public exponent");
            }
            if (publicExponent.compareTo(PUBLIC_EXPONENT_UPPER_BOUND) > 0) {
                throw new InvalidAlgorithmParameterException("Public exponent cannot be larger than 2^256.");
            }
        }

        public RsaSsaPkcs1Parameters build() throws GeneralSecurityException {
            Integer num = this.modulusSizeBits;
            if (num == null) {
                throw new GeneralSecurityException("key size is not set");
            }
            if (this.publicExponent == null) {
                throw new GeneralSecurityException("publicExponent is not set");
            }
            if (this.hashType == null) {
                throw new GeneralSecurityException("hash type is not set");
            }
            if (this.variant == null) {
                throw new GeneralSecurityException("variant is not set");
            }
            if (num.intValue() < 2048) {
                throw new InvalidAlgorithmParameterException(String.format("Invalid key size in bytes %d; must be at least 2048 bits", this.modulusSizeBits));
            }
            validatePublicExponent(this.publicExponent);
            return new RsaSsaPkcs1Parameters(this.modulusSizeBits.intValue(), this.publicExponent, this.variant, this.hashType);
        }
    }

    private RsaSsaPkcs1Parameters(int modulusSizeBits, BigInteger publicExponent, Variant variant, HashType hashType) {
        this.modulusSizeBits = modulusSizeBits;
        this.publicExponent = publicExponent;
        this.variant = variant;
        this.hashType = hashType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getModulusSizeBits() {
        return this.modulusSizeBits;
    }

    public BigInteger getPublicExponent() {
        return this.publicExponent;
    }

    public Variant getVariant() {
        return this.variant;
    }

    public HashType getHashType() {
        return this.hashType;
    }

    public boolean equals(Object o) {
        if (!(o instanceof RsaSsaPkcs1Parameters)) {
            return false;
        }
        RsaSsaPkcs1Parameters rsaSsaPkcs1Parameters = (RsaSsaPkcs1Parameters) o;
        return rsaSsaPkcs1Parameters.getModulusSizeBits() == getModulusSizeBits() && Objects.equals(rsaSsaPkcs1Parameters.getPublicExponent(), getPublicExponent()) && rsaSsaPkcs1Parameters.getVariant() == getVariant() && rsaSsaPkcs1Parameters.getHashType() == getHashType();
    }

    public int hashCode() {
        return Objects.hash(RsaSsaPkcs1Parameters.class, Integer.valueOf(this.modulusSizeBits), this.publicExponent, this.variant, this.hashType);
    }

    @Override // com.google.crypto.tink.Parameters
    public boolean hasIdRequirement() {
        return this.variant != Variant.NO_PREFIX;
    }

    public String toString() {
        return "RSA SSA PKCS1 Parameters (variant: " + this.variant + ", hashType: " + this.hashType + ", publicExponent: " + this.publicExponent + ", and " + this.modulusSizeBits + "-bit modulus)";
    }
}
