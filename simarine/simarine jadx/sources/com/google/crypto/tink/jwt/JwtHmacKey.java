package com.google.crypto.tink.jwt;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.subtle.Base64;
import com.google.crypto.tink.util.SecretBytes;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Optional;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public class JwtHmacKey extends JwtMacKey {
    private final Optional<Integer> idRequirement;
    private final SecretBytes key;
    private final Optional<String> kid;
    private final JwtHmacParameters parameters;

    public static class Builder {
        private Optional<String> customKid;
        private Optional<Integer> idRequirement;
        private Optional<SecretBytes> keyBytes;
        private Optional<JwtHmacParameters> parameters;

        private Builder() {
            this.parameters = Optional.empty();
            this.keyBytes = Optional.empty();
            this.idRequirement = Optional.empty();
            this.customKid = Optional.empty();
        }

        public Builder setParameters(JwtHmacParameters parameters) {
            this.parameters = Optional.of(parameters);
            return this;
        }

        public Builder setKeyBytes(SecretBytes keyBytes) {
            this.keyBytes = Optional.of(keyBytes);
            return this;
        }

        public Builder setIdRequirement(int idRequirement) {
            this.idRequirement = Optional.of(Integer.valueOf(idRequirement));
            return this;
        }

        public Builder setCustomKid(String customKid) {
            this.customKid = Optional.of(customKid);
            return this;
        }

        private Optional<String> computeKid() throws GeneralSecurityException {
            if (this.parameters.get().getKidStrategy().equals(JwtHmacParameters.KidStrategy.BASE64_ENCODED_KEY_ID)) {
                byte[] bArrArray = ByteBuffer.allocate(4).putInt(this.idRequirement.get().intValue()).array();
                if (this.customKid.isPresent()) {
                    throw new GeneralSecurityException("customKid must not be set for KidStrategy BASE64_ENCODED_KEY_ID");
                }
                return Optional.of(Base64.urlSafeEncode(bArrArray));
            }
            if (this.parameters.get().getKidStrategy().equals(JwtHmacParameters.KidStrategy.CUSTOM)) {
                if (!this.customKid.isPresent()) {
                    throw new GeneralSecurityException("customKid needs to be set for KidStrategy CUSTOM");
                }
                return this.customKid;
            }
            if (this.parameters.get().getKidStrategy().equals(JwtHmacParameters.KidStrategy.IGNORED)) {
                if (this.customKid.isPresent()) {
                    throw new GeneralSecurityException("customKid must not be set for KidStrategy IGNORED");
                }
                return Optional.empty();
            }
            throw new IllegalStateException("Unknown kid strategy");
        }

        public JwtHmacKey build() throws GeneralSecurityException {
            if (!this.parameters.isPresent()) {
                throw new GeneralSecurityException("Parameters are required");
            }
            if (!this.keyBytes.isPresent()) {
                throw new GeneralSecurityException("KeyBytes are required");
            }
            if (this.parameters.get().getKeySizeBytes() != this.keyBytes.get().size()) {
                throw new GeneralSecurityException("Key size mismatch");
            }
            if (this.parameters.get().hasIdRequirement() && !this.idRequirement.isPresent()) {
                throw new GeneralSecurityException("Cannot create key without ID requirement with parameters with ID requirement");
            }
            if (!this.parameters.get().hasIdRequirement() && this.idRequirement.isPresent()) {
                throw new GeneralSecurityException("Cannot create key with ID requirement with parameters without ID requirement");
            }
            return new JwtHmacKey(this.parameters.get(), this.keyBytes.get(), this.idRequirement, computeKid());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private JwtHmacKey(JwtHmacParameters parameters, SecretBytes key, Optional<Integer> idRequirement, Optional<String> kid) {
        this.parameters = parameters;
        this.key = key;
        this.idRequirement = idRequirement;
        this.kid = kid;
    }

    public SecretBytes getKeyBytes() {
        return this.key;
    }

    @Override // com.google.crypto.tink.jwt.JwtMacKey
    public Optional<String> getKid() {
        return this.kid;
    }

    @Override // com.google.crypto.tink.Key
    @Nullable
    public Integer getIdRequirementOrNull() {
        return this.idRequirement.orElse(null);
    }

    @Override // com.google.crypto.tink.jwt.JwtMacKey, com.google.crypto.tink.Key
    public JwtHmacParameters getParameters() {
        return this.parameters;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof JwtHmacKey)) {
            return false;
        }
        JwtHmacKey jwtHmacKey = (JwtHmacKey) o;
        return jwtHmacKey.parameters.equals(this.parameters) && jwtHmacKey.key.equalsSecretBytes(this.key) && jwtHmacKey.kid.equals(this.kid) && jwtHmacKey.idRequirement.equals(this.idRequirement);
    }
}
