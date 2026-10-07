package com.google.crypto.tink.keyderivation;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.prf.PrfKey;
import java.security.GeneralSecurityException;
import java.util.Objects;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class PrfBasedKeyDerivationKey extends KeyDerivationKey {
    private final Integer idRequirementOrNull;
    private final PrfBasedKeyDerivationParameters parameters;
    private final PrfKey prfKey;

    public static PrfBasedKeyDerivationKey create(PrfBasedKeyDerivationParameters parameters, PrfKey prfKey, @Nullable Integer idRequirement) throws GeneralSecurityException {
        if (!parameters.getPrfParameters().equals(prfKey.getParameters())) {
            throw new GeneralSecurityException("PrfParameters of passed in PrfBasedKeyDerivationParameters and passed in prfKey parameters object must match. DerivationParameters gave: " + parameters.getPrfParameters() + ", key gives: " + prfKey.getParameters());
        }
        if (parameters.getDerivedKeyParameters().hasIdRequirement() && idRequirement == null) {
            throw new GeneralSecurityException("Derived key has an ID requirement, but no idRequirement was passed in on creation of this key");
        }
        if (!parameters.getDerivedKeyParameters().hasIdRequirement() && idRequirement != null) {
            throw new GeneralSecurityException("Derived key has no ID requirement, but idRequirement was passed in on creation of this key");
        }
        return new PrfBasedKeyDerivationKey(parameters, prfKey, idRequirement);
    }

    private PrfBasedKeyDerivationKey(PrfBasedKeyDerivationParameters parameters, PrfKey prfKey, @Nullable Integer idRequirement) {
        this.parameters = parameters;
        this.prfKey = prfKey;
        this.idRequirementOrNull = idRequirement;
    }

    public PrfKey getPrfKey() {
        return this.prfKey;
    }

    @Override // com.google.crypto.tink.keyderivation.KeyDerivationKey, com.google.crypto.tink.Key
    public PrfBasedKeyDerivationParameters getParameters() {
        return this.parameters;
    }

    @Override // com.google.crypto.tink.Key
    @Nullable
    public Integer getIdRequirementOrNull() {
        return this.idRequirementOrNull;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key other) {
        if (!(other instanceof PrfBasedKeyDerivationKey)) {
            return false;
        }
        PrfBasedKeyDerivationKey prfBasedKeyDerivationKey = (PrfBasedKeyDerivationKey) other;
        return prfBasedKeyDerivationKey.getParameters().equals(getParameters()) && prfBasedKeyDerivationKey.prfKey.equalsKey(this.prfKey) && Objects.equals(prfBasedKeyDerivationKey.idRequirementOrNull, this.idRequirementOrNull);
    }
}
