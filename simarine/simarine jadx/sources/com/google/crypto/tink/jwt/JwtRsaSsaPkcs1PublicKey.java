package com.google.crypto.tink.jwt;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.subtle.Base64;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Optional;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class JwtRsaSsaPkcs1PublicKey extends JwtSignaturePublicKey {
    private final Optional<Integer> idRequirement;
    private final Optional<String> kid;
    private final BigInteger modulus;
    private final JwtRsaSsaPkcs1Parameters parameters;

    public static class Builder {
        private Optional<String> customKid;
        private Optional<Integer> idRequirement;
        private Optional<BigInteger> modulus;
        private Optional<JwtRsaSsaPkcs1Parameters> parameters;

        private Builder() {
            this.parameters = Optional.empty();
            this.modulus = Optional.empty();
            this.idRequirement = Optional.empty();
            this.customKid = Optional.empty();
        }

        public Builder setParameters(JwtRsaSsaPkcs1Parameters parameters) {
            this.parameters = Optional.of(parameters);
            return this;
        }

        public Builder setModulus(BigInteger modulus) {
            this.modulus = Optional.of(modulus);
            return this;
        }

        public Builder setIdRequirement(Integer idRequirement) {
            this.idRequirement = Optional.of(idRequirement);
            return this;
        }

        public Builder setCustomKid(String customKid) {
            this.customKid = Optional.of(customKid);
            return this;
        }

        private Optional<String> computeKid() throws GeneralSecurityException {
            if (this.parameters.get().getKidStrategy().equals(JwtRsaSsaPkcs1Parameters.KidStrategy.BASE64_ENCODED_KEY_ID)) {
                if (this.customKid.isPresent()) {
                    throw new GeneralSecurityException("customKid must not be set for KidStrategy BASE64_ENCODED_KEY_ID");
                }
                return Optional.of(Base64.urlSafeEncode(ByteBuffer.allocate(4).putInt(this.idRequirement.get().intValue()).array()));
            }
            if (this.parameters.get().getKidStrategy().equals(JwtRsaSsaPkcs1Parameters.KidStrategy.CUSTOM)) {
                if (!this.customKid.isPresent()) {
                    throw new GeneralSecurityException("customKid needs to be set for KidStrategy CUSTOM");
                }
                return this.customKid;
            }
            if (this.parameters.get().getKidStrategy().equals(JwtRsaSsaPkcs1Parameters.KidStrategy.IGNORED)) {
                if (this.customKid.isPresent()) {
                    throw new GeneralSecurityException("customKid must not be set for KidStrategy IGNORED");
                }
                return Optional.empty();
            }
            throw new IllegalStateException("Unknown kid strategy");
        }

        public JwtRsaSsaPkcs1PublicKey build() throws GeneralSecurityException {
            if (!this.parameters.isPresent()) {
                throw new GeneralSecurityException("Cannot build without parameters");
            }
            if (!this.modulus.isPresent()) {
                throw new GeneralSecurityException("Cannot build without modulus");
            }
            int iBitLength = this.modulus.get().bitLength();
            int modulusSizeBits = this.parameters.get().getModulusSizeBits();
            if (iBitLength != modulusSizeBits) {
                throw new GeneralSecurityException("Got modulus size " + iBitLength + ", but parameters requires modulus size " + modulusSizeBits);
            }
            if (this.parameters.get().hasIdRequirement() && !this.idRequirement.isPresent()) {
                throw new GeneralSecurityException("Cannot create key without ID requirement with parameters with ID requirement");
            }
            if (!this.parameters.get().hasIdRequirement() && this.idRequirement.isPresent()) {
                throw new GeneralSecurityException("Cannot create key with ID requirement with parameters without ID requirement");
            }
            return new JwtRsaSsaPkcs1PublicKey(this.parameters.get(), this.modulus.get(), this.idRequirement, computeKid());
        }
    }

    private JwtRsaSsaPkcs1PublicKey(JwtRsaSsaPkcs1Parameters parameters, BigInteger modulus, Optional<Integer> idRequirement, Optional<String> kid) {
        this.parameters = parameters;
        this.modulus = modulus;
        this.idRequirement = idRequirement;
        this.kid = kid;
    }

    public static Builder builder() {
        return new Builder();
    }

    public BigInteger getModulus() {
        return this.modulus;
    }

    @Override // com.google.crypto.tink.jwt.JwtSignaturePublicKey
    public Optional<String> getKid() {
        return this.kid;
    }

    @Override // com.google.crypto.tink.jwt.JwtSignaturePublicKey, com.google.crypto.tink.Key
    public JwtRsaSsaPkcs1Parameters getParameters() {
        return this.parameters;
    }

    @Override // com.google.crypto.tink.Key
    @Nullable
    public Integer getIdRequirementOrNull() {
        return this.idRequirement.orElse(null);
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof JwtRsaSsaPkcs1PublicKey)) {
            return false;
        }
        JwtRsaSsaPkcs1PublicKey jwtRsaSsaPkcs1PublicKey = (JwtRsaSsaPkcs1PublicKey) o;
        return jwtRsaSsaPkcs1PublicKey.parameters.equals(this.parameters) && jwtRsaSsaPkcs1PublicKey.modulus.equals(this.modulus) && jwtRsaSsaPkcs1PublicKey.kid.equals(this.kid) && jwtRsaSsaPkcs1PublicKey.idRequirement.equals(this.idRequirement);
    }
}
