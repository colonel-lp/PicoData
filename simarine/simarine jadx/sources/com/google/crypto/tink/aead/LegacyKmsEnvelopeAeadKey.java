package com.google.crypto.tink.aead;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.internal.OutputPrefixUtil;
import com.google.crypto.tink.util.Bytes;
import java.security.GeneralSecurityException;
import java.util.Objects;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public class LegacyKmsEnvelopeAeadKey extends AeadKey {

    @Nullable
    private final Integer idRequirement;
    private final Bytes outputPrefix;
    private final LegacyKmsEnvelopeAeadParameters parameters;

    private LegacyKmsEnvelopeAeadKey(LegacyKmsEnvelopeAeadParameters parameters, Bytes outputPrefix, @Nullable Integer idRequirement) {
        this.parameters = parameters;
        this.outputPrefix = outputPrefix;
        this.idRequirement = idRequirement;
    }

    public static LegacyKmsEnvelopeAeadKey create(LegacyKmsEnvelopeAeadParameters parameters, @Nullable Integer idRequirement) throws GeneralSecurityException {
        Bytes tinkOutputPrefix;
        if (parameters.getVariant() == LegacyKmsEnvelopeAeadParameters.Variant.NO_PREFIX) {
            if (idRequirement != null) {
                throw new GeneralSecurityException("For given Variant NO_PREFIX the value of idRequirement must be null");
            }
            tinkOutputPrefix = OutputPrefixUtil.EMPTY_PREFIX;
        } else {
            if (parameters.getVariant() != LegacyKmsEnvelopeAeadParameters.Variant.TINK) {
                throw new GeneralSecurityException("Unknown Variant: " + parameters.getVariant());
            }
            if (idRequirement == null) {
                throw new GeneralSecurityException("For given Variant TINK the value of idRequirement must be non-null");
            }
            tinkOutputPrefix = OutputPrefixUtil.getTinkOutputPrefix(idRequirement.intValue());
        }
        return new LegacyKmsEnvelopeAeadKey(parameters, tinkOutputPrefix, idRequirement);
    }

    public static LegacyKmsEnvelopeAeadKey create(LegacyKmsEnvelopeAeadParameters parameters) throws GeneralSecurityException {
        return create(parameters, null);
    }

    @Override // com.google.crypto.tink.aead.AeadKey
    public Bytes getOutputPrefix() {
        return this.outputPrefix;
    }

    @Override // com.google.crypto.tink.aead.AeadKey, com.google.crypto.tink.Key
    public LegacyKmsEnvelopeAeadParameters getParameters() {
        return this.parameters;
    }

    @Override // com.google.crypto.tink.Key
    public Integer getIdRequirementOrNull() {
        return this.idRequirement;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof LegacyKmsEnvelopeAeadKey)) {
            return false;
        }
        LegacyKmsEnvelopeAeadKey legacyKmsEnvelopeAeadKey = (LegacyKmsEnvelopeAeadKey) o;
        return legacyKmsEnvelopeAeadKey.parameters.equals(this.parameters) && Objects.equals(legacyKmsEnvelopeAeadKey.idRequirement, this.idRequirement);
    }
}
