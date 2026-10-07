package com.google.crypto.tink.streamingaead;

import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.util.Objects;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public class AesCtrHmacStreamingParameters extends StreamingAeadParameters {
    private final Integer ciphertextSegmentSizeBytes;
    private final Integer derivedKeySizeBytes;
    private final HashType hkdfHashType;
    private final HashType hmacHashType;
    private final Integer hmacTagSizeBytes;
    private final Integer keySizeBytes;

    @Immutable
    public static final class HashType {
        public static final HashType SHA1 = new HashType("SHA1");
        public static final HashType SHA256 = new HashType("SHA256");
        public static final HashType SHA512 = new HashType("SHA512");
        private final String name;

        private HashType(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        @Nullable
        private Integer keySizeBytes = null;

        @Nullable
        private Integer derivedKeySizeBytes = null;

        @Nullable
        private HashType hkdfHashType = null;

        @Nullable
        private HashType hmacHashType = null;

        @Nullable
        private Integer hmacTagSizeBytes = null;

        @Nullable
        private Integer ciphertextSegmentSizeBytes = null;

        public Builder setKeySizeBytes(int keySizeBytes) {
            this.keySizeBytes = Integer.valueOf(keySizeBytes);
            return this;
        }

        public Builder setDerivedKeySizeBytes(int derivedKeySizeBytes) {
            this.derivedKeySizeBytes = Integer.valueOf(derivedKeySizeBytes);
            return this;
        }

        public Builder setHkdfHashType(HashType hkdfHashType) {
            this.hkdfHashType = hkdfHashType;
            return this;
        }

        public Builder setHmacHashType(HashType hmacHashType) {
            this.hmacHashType = hmacHashType;
            return this;
        }

        public Builder setHmacTagSizeBytes(Integer hmacTagSizeBytes) {
            this.hmacTagSizeBytes = hmacTagSizeBytes;
            return this;
        }

        public Builder setCiphertextSegmentSizeBytes(int ciphertextSegmentSizeBytes) {
            this.ciphertextSegmentSizeBytes = Integer.valueOf(ciphertextSegmentSizeBytes);
            return this;
        }

        public AesCtrHmacStreamingParameters build() throws GeneralSecurityException {
            if (this.keySizeBytes == null) {
                throw new GeneralSecurityException("keySizeBytes needs to be set");
            }
            Integer num = this.derivedKeySizeBytes;
            if (num == null) {
                throw new GeneralSecurityException("derivedKeySizeBytes needs to be set");
            }
            if (this.hkdfHashType == null) {
                throw new GeneralSecurityException("hkdfHashType needs to be set");
            }
            if (this.hmacHashType == null) {
                throw new GeneralSecurityException("hmacHashType needs to be set");
            }
            if (this.hmacTagSizeBytes == null) {
                throw new GeneralSecurityException("hmacTagSizeBytes needs to be set");
            }
            if (this.ciphertextSegmentSizeBytes == null) {
                throw new GeneralSecurityException("ciphertextSegmentSizeBytes needs to be set");
            }
            if (num.intValue() != 16 && this.derivedKeySizeBytes.intValue() != 32) {
                throw new GeneralSecurityException("derivedKeySizeBytes needs to be 16 or 32, not " + this.derivedKeySizeBytes);
            }
            if (this.keySizeBytes.intValue() < this.derivedKeySizeBytes.intValue()) {
                throw new GeneralSecurityException("keySizeBytes needs to be at least derivedKeySizeBytes, i.e., " + this.derivedKeySizeBytes);
            }
            if (this.ciphertextSegmentSizeBytes.intValue() <= this.derivedKeySizeBytes.intValue() + this.hmacTagSizeBytes.intValue() + 8) {
                throw new GeneralSecurityException("ciphertextSegmentSizeBytes needs to be at least derivedKeySizeBytes + hmacTagSizeBytes + 9, i.e., " + (this.derivedKeySizeBytes.intValue() + this.hmacTagSizeBytes.intValue() + 9));
            }
            int i = this.hmacHashType != HashType.SHA256 ? this.hmacHashType == HashType.SHA1 ? 20 : 0 : 32;
            if (this.hmacHashType == HashType.SHA512) {
                i = 64;
            }
            if (this.hmacTagSizeBytes.intValue() < 10 || this.hmacTagSizeBytes.intValue() > i) {
                throw new GeneralSecurityException("hmacTagSize must be in range [10, " + i + "], but is " + this.hmacTagSizeBytes);
            }
            return new AesCtrHmacStreamingParameters(this.keySizeBytes, this.derivedKeySizeBytes, this.hkdfHashType, this.hmacHashType, this.hmacTagSizeBytes, this.ciphertextSegmentSizeBytes);
        }
    }

    private AesCtrHmacStreamingParameters(Integer keySizeBytes, Integer derivedKeySizeBytes, HashType hkdfHashType, HashType hmacHashType, Integer hmacTagSizeBytes, Integer ciphertextSegmentSizeBytes) {
        this.keySizeBytes = keySizeBytes;
        this.derivedKeySizeBytes = derivedKeySizeBytes;
        this.hkdfHashType = hkdfHashType;
        this.hmacHashType = hmacHashType;
        this.hmacTagSizeBytes = hmacTagSizeBytes;
        this.ciphertextSegmentSizeBytes = ciphertextSegmentSizeBytes;
    }

    public int getKeySizeBytes() {
        return this.keySizeBytes.intValue();
    }

    public int getDerivedKeySizeBytes() {
        return this.derivedKeySizeBytes.intValue();
    }

    public HashType getHkdfHashType() {
        return this.hkdfHashType;
    }

    public HashType getHmacHashType() {
        return this.hmacHashType;
    }

    public int getHmacTagSizeBytes() {
        return this.hmacTagSizeBytes.intValue();
    }

    public int getCiphertextSegmentSizeBytes() {
        return this.ciphertextSegmentSizeBytes.intValue();
    }

    public boolean equals(Object o) {
        if (!(o instanceof AesCtrHmacStreamingParameters)) {
            return false;
        }
        AesCtrHmacStreamingParameters aesCtrHmacStreamingParameters = (AesCtrHmacStreamingParameters) o;
        return aesCtrHmacStreamingParameters.getKeySizeBytes() == getKeySizeBytes() && aesCtrHmacStreamingParameters.getDerivedKeySizeBytes() == getDerivedKeySizeBytes() && aesCtrHmacStreamingParameters.getHkdfHashType() == getHkdfHashType() && aesCtrHmacStreamingParameters.getHmacHashType() == getHmacHashType() && aesCtrHmacStreamingParameters.getHmacTagSizeBytes() == getHmacTagSizeBytes() && aesCtrHmacStreamingParameters.getCiphertextSegmentSizeBytes() == getCiphertextSegmentSizeBytes();
    }

    public int hashCode() {
        return Objects.hash(AesCtrHmacStreamingParameters.class, this.keySizeBytes, this.derivedKeySizeBytes, this.hkdfHashType, this.hmacHashType, this.hmacTagSizeBytes, this.ciphertextSegmentSizeBytes);
    }

    public String toString() {
        return "AesCtrHmacStreaming Parameters (IKM size: " + this.keySizeBytes + ", " + this.derivedKeySizeBytes + "-byte AES key, " + this.hkdfHashType + " for HKDF, " + this.hkdfHashType + " for HMAC, " + this.hmacTagSizeBytes + "-byte tags, " + this.ciphertextSegmentSizeBytes + "-byte ciphertexts)";
    }
}
