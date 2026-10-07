package com.google.crypto.tink.jwt;

import com.google.crypto.tink.internal.EllipticCurvesUtil;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.spec.ECParameterSpec;
import java.util.Objects;
import java.util.Optional;

/* JADX INFO: loaded from: classes2.dex */
public final class JwtEcdsaParameters extends JwtSignatureParameters {
    private final Algorithm algorithm;
    private final KidStrategy kidStrategy;

    @Immutable
    public static final class KidStrategy {
        private final String name;
        public static final KidStrategy BASE64_ENCODED_KEY_ID = new KidStrategy("BASE64_ENCODED_KEY_ID");
        public static final KidStrategy IGNORED = new KidStrategy("IGNORED");
        public static final KidStrategy CUSTOM = new KidStrategy("CUSTOM");

        private KidStrategy(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    @Immutable
    public static final class Algorithm {
        public static final Algorithm ES256 = new Algorithm("ES256", EllipticCurvesUtil.NIST_P256_PARAMS);
        public static final Algorithm ES384 = new Algorithm("ES384", EllipticCurvesUtil.NIST_P384_PARAMS);
        public static final Algorithm ES512 = new Algorithm("ES512", EllipticCurvesUtil.NIST_P521_PARAMS);
        private final ECParameterSpec ecParameterSpec;
        private final String name;

        private Algorithm(String name, ECParameterSpec ecParameterSpec) {
            this.name = name;
            this.ecParameterSpec = ecParameterSpec;
        }

        public String toString() {
            return this.name;
        }

        public String getStandardName() {
            return this.name;
        }

        ECParameterSpec getECParameterSpec() {
            return this.ecParameterSpec;
        }
    }

    public static final class Builder {
        Optional<Algorithm> algorithm;
        Optional<KidStrategy> kidStrategy;

        public Builder setKidStrategy(KidStrategy kidStrategy) {
            this.kidStrategy = Optional.of(kidStrategy);
            return this;
        }

        public Builder setAlgorithm(Algorithm algorithm) {
            this.algorithm = Optional.of(algorithm);
            return this;
        }

        public JwtEcdsaParameters build() throws GeneralSecurityException {
            if (!this.algorithm.isPresent()) {
                throw new GeneralSecurityException("Algorithm must be set");
            }
            if (!this.kidStrategy.isPresent()) {
                throw new GeneralSecurityException("KidStrategy must be set");
            }
            return new JwtEcdsaParameters(this.kidStrategy.get(), this.algorithm.get());
        }

        private Builder() {
            this.kidStrategy = Optional.empty();
            this.algorithm = Optional.empty();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private JwtEcdsaParameters(KidStrategy kidStrategy, Algorithm algorithm) {
        this.kidStrategy = kidStrategy;
        this.algorithm = algorithm;
    }

    public KidStrategy getKidStrategy() {
        return this.kidStrategy;
    }

    public Algorithm getAlgorithm() {
        return this.algorithm;
    }

    @Override // com.google.crypto.tink.Parameters
    public boolean hasIdRequirement() {
        return this.kidStrategy.equals(KidStrategy.BASE64_ENCODED_KEY_ID);
    }

    @Override // com.google.crypto.tink.jwt.JwtSignatureParameters
    public boolean allowKidAbsent() {
        return this.kidStrategy.equals(KidStrategy.CUSTOM) || this.kidStrategy.equals(KidStrategy.IGNORED);
    }

    public boolean equals(Object o) {
        if (!(o instanceof JwtEcdsaParameters)) {
            return false;
        }
        JwtEcdsaParameters jwtEcdsaParameters = (JwtEcdsaParameters) o;
        return jwtEcdsaParameters.kidStrategy.equals(this.kidStrategy) && jwtEcdsaParameters.algorithm.equals(this.algorithm);
    }

    public int hashCode() {
        return Objects.hash(JwtEcdsaParameters.class, this.kidStrategy, this.algorithm);
    }

    public String toString() {
        return "JWT ECDSA Parameters (kidStrategy: " + this.kidStrategy + ", Algorithm " + this.algorithm + ")";
    }
}
