package com.google.crypto.tink.signature;

import com.google.crypto.tink.internal.EllipticCurvesUtil;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.spec.ECParameterSpec;
import java.util.Objects;

/* JADX INFO: loaded from: classes2.dex */
public final class EcdsaParameters extends SignatureParameters {
    private final CurveType curveType;
    private final HashType hashType;
    private final SignatureEncoding signatureEncoding;
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
    public static final class SignatureEncoding {
        private final String name;
        public static final SignatureEncoding IEEE_P1363 = new SignatureEncoding("IEEE_P1363");
        public static final SignatureEncoding DER = new SignatureEncoding("DER");

        private SignatureEncoding(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    @Immutable
    public static final class CurveType {
        public static final CurveType NIST_P256 = new CurveType("NIST_P256", EllipticCurvesUtil.NIST_P256_PARAMS);
        public static final CurveType NIST_P384 = new CurveType("NIST_P384", EllipticCurvesUtil.NIST_P384_PARAMS);
        public static final CurveType NIST_P521 = new CurveType("NIST_P521", EllipticCurvesUtil.NIST_P521_PARAMS);
        private final String name;
        private final ECParameterSpec spec;

        private CurveType(String name, ECParameterSpec spec) {
            this.name = name;
            this.spec = spec;
        }

        public String toString() {
            return this.name;
        }

        public ECParameterSpec toParameterSpec() {
            return this.spec;
        }

        public static CurveType fromParameterSpec(ECParameterSpec spec) throws GeneralSecurityException {
            CurveType curveType = NIST_P256;
            if (EllipticCurvesUtil.isSameEcParameterSpec(spec, curveType.toParameterSpec())) {
                return curveType;
            }
            CurveType curveType2 = NIST_P384;
            if (EllipticCurvesUtil.isSameEcParameterSpec(spec, curveType2.toParameterSpec())) {
                return curveType2;
            }
            CurveType curveType3 = NIST_P521;
            if (EllipticCurvesUtil.isSameEcParameterSpec(spec, curveType3.toParameterSpec())) {
                return curveType3;
            }
            throw new GeneralSecurityException("unknown ECParameterSpec");
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
        private CurveType curveType;
        private HashType hashType;
        private SignatureEncoding signatureEncoding;
        private Variant variant;

        private Builder() {
            this.signatureEncoding = null;
            this.curveType = null;
            this.hashType = null;
            this.variant = Variant.NO_PREFIX;
        }

        public Builder setSignatureEncoding(SignatureEncoding signatureEncoding) {
            this.signatureEncoding = signatureEncoding;
            return this;
        }

        public Builder setCurveType(CurveType curveType) {
            this.curveType = curveType;
            return this;
        }

        public Builder setHashType(HashType hashType) {
            this.hashType = hashType;
            return this;
        }

        public Builder setVariant(Variant variant) {
            this.variant = variant;
            return this;
        }

        public EcdsaParameters build() throws GeneralSecurityException {
            if (this.signatureEncoding == null) {
                throw new GeneralSecurityException("signature encoding is not set");
            }
            CurveType curveType = this.curveType;
            if (curveType == null) {
                throw new GeneralSecurityException("EC curve type is not set");
            }
            if (this.hashType == null) {
                throw new GeneralSecurityException("hash type is not set");
            }
            if (this.variant == null) {
                throw new GeneralSecurityException("variant is not set");
            }
            if (curveType == CurveType.NIST_P256 && this.hashType != HashType.SHA256) {
                throw new GeneralSecurityException("NIST_P256 requires SHA256");
            }
            if (this.curveType == CurveType.NIST_P384 && this.hashType != HashType.SHA384 && this.hashType != HashType.SHA512) {
                throw new GeneralSecurityException("NIST_P384 requires SHA384 or SHA512");
            }
            if (this.curveType == CurveType.NIST_P521 && this.hashType != HashType.SHA512) {
                throw new GeneralSecurityException("NIST_P521 requires SHA512");
            }
            return new EcdsaParameters(this.signatureEncoding, this.curveType, this.hashType, this.variant);
        }
    }

    private EcdsaParameters(SignatureEncoding signatureEncoding, CurveType curveType, HashType hashType, Variant variant) {
        this.signatureEncoding = signatureEncoding;
        this.curveType = curveType;
        this.hashType = hashType;
        this.variant = variant;
    }

    public static Builder builder() {
        return new Builder();
    }

    public SignatureEncoding getSignatureEncoding() {
        return this.signatureEncoding;
    }

    public CurveType getCurveType() {
        return this.curveType;
    }

    public HashType getHashType() {
        return this.hashType;
    }

    public Variant getVariant() {
        return this.variant;
    }

    public boolean equals(Object o) {
        if (!(o instanceof EcdsaParameters)) {
            return false;
        }
        EcdsaParameters ecdsaParameters = (EcdsaParameters) o;
        return ecdsaParameters.getSignatureEncoding() == getSignatureEncoding() && ecdsaParameters.getCurveType() == getCurveType() && ecdsaParameters.getHashType() == getHashType() && ecdsaParameters.getVariant() == getVariant();
    }

    public int hashCode() {
        return Objects.hash(EcdsaParameters.class, this.signatureEncoding, this.curveType, this.hashType, this.variant);
    }

    @Override // com.google.crypto.tink.Parameters
    public boolean hasIdRequirement() {
        return this.variant != Variant.NO_PREFIX;
    }

    public String toString() {
        return "ECDSA Parameters (variant: " + this.variant + ", hashType: " + this.hashType + ", encoding: " + this.signatureEncoding + ", curve: " + this.curveType + ")";
    }
}
