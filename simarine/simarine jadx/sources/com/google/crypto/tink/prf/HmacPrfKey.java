package com.google.crypto.tink.prf;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.util.SecretBytes;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class HmacPrfKey extends PrfKey {
    private final SecretBytes keyBytes;
    private final HmacPrfParameters parameters;

    @Override // com.google.crypto.tink.Key
    @Nullable
    public Integer getIdRequirementOrNull() {
        return null;
    }

    public static final class Builder {

        @Nullable
        private SecretBytes keyBytes;

        @Nullable
        private HmacPrfParameters parameters;

        private Builder() {
            this.parameters = null;
            this.keyBytes = null;
        }

        public Builder setParameters(HmacPrfParameters parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder setKeyBytes(SecretBytes keyBytes) {
            this.keyBytes = keyBytes;
            return this;
        }

        public HmacPrfKey build() throws GeneralSecurityException {
            HmacPrfParameters hmacPrfParameters = this.parameters;
            if (hmacPrfParameters == null || this.keyBytes == null) {
                throw new GeneralSecurityException("Cannot build without parameters and/or key material");
            }
            if (hmacPrfParameters.getKeySizeBytes() != this.keyBytes.size()) {
                throw new GeneralSecurityException("Key size mismatch");
            }
            return new HmacPrfKey(this.parameters, this.keyBytes);
        }
    }

    private HmacPrfKey(HmacPrfParameters parameters, SecretBytes keyBytes) {
        this.parameters = parameters;
        this.keyBytes = keyBytes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public SecretBytes getKeyBytes() {
        return this.keyBytes;
    }

    @Override // com.google.crypto.tink.prf.PrfKey, com.google.crypto.tink.Key
    public HmacPrfParameters getParameters() {
        return this.parameters;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof HmacPrfKey)) {
            return false;
        }
        HmacPrfKey hmacPrfKey = (HmacPrfKey) o;
        return hmacPrfKey.parameters.equals(this.parameters) && hmacPrfKey.keyBytes.equalsSecretBytes(this.keyBytes);
    }
}
