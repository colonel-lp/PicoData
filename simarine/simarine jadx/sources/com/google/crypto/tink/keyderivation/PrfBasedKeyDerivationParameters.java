package com.google.crypto.tink.keyderivation;

import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.prf.PrfParameters;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.util.Objects;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class PrfBasedKeyDerivationParameters extends KeyDerivationParameters {
    private final Parameters derivedKeyParameters;
    private final PrfParameters prfParameters;

    public static class Builder {

        @Nullable
        private PrfParameters prfParameters = null;

        @Nullable
        private Parameters derivedKeyParameters = null;

        public Builder setPrfParameters(PrfParameters prfParameters) {
            this.prfParameters = prfParameters;
            return this;
        }

        public Builder setDerivedKeyParameters(Parameters derivedKeyParameters) {
            this.derivedKeyParameters = derivedKeyParameters;
            return this;
        }

        public PrfBasedKeyDerivationParameters build() throws GeneralSecurityException {
            PrfParameters prfParameters = this.prfParameters;
            if (prfParameters == null) {
                throw new GeneralSecurityException("PrfParameters must be set.");
            }
            Parameters parameters = this.derivedKeyParameters;
            if (parameters == null) {
                throw new GeneralSecurityException("DerivedKeyParameters must be set.");
            }
            return new PrfBasedKeyDerivationParameters(prfParameters, parameters);
        }
    }

    private PrfBasedKeyDerivationParameters(PrfParameters prfParameters, Parameters derivedKeyParameters) {
        this.prfParameters = prfParameters;
        this.derivedKeyParameters = derivedKeyParameters;
    }

    public static Builder builder() {
        return new Builder();
    }

    public PrfParameters getPrfParameters() {
        return this.prfParameters;
    }

    @Override // com.google.crypto.tink.keyderivation.KeyDerivationParameters
    public Parameters getDerivedKeyParameters() {
        return this.derivedKeyParameters;
    }

    public boolean equals(Object o) {
        if (!(o instanceof PrfBasedKeyDerivationParameters)) {
            return false;
        }
        PrfBasedKeyDerivationParameters prfBasedKeyDerivationParameters = (PrfBasedKeyDerivationParameters) o;
        return prfBasedKeyDerivationParameters.getPrfParameters().equals(getPrfParameters()) && prfBasedKeyDerivationParameters.getDerivedKeyParameters().equals(getDerivedKeyParameters());
    }

    public int hashCode() {
        return Objects.hash(PrfBasedKeyDerivationParameters.class, this.prfParameters, this.derivedKeyParameters);
    }

    public String toString() {
        return String.format("PrfBasedKeyDerivationParameters(%s, %s)", this.prfParameters, this.derivedKeyParameters);
    }
}
