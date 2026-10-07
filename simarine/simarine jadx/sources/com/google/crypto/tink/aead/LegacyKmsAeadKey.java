package com.google.crypto.tink.aead;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.util.Bytes;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Objects;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public class LegacyKmsAeadKey extends AeadKey {

    @Nullable
    private final Integer idRequirement;
    private final Bytes outputPrefix;
    private final LegacyKmsAeadParameters parameters;

    private LegacyKmsAeadKey(LegacyKmsAeadParameters parameters, Bytes outputPrefix, @Nullable Integer idRequirement) {
        this.parameters = parameters;
        this.outputPrefix = outputPrefix;
        this.idRequirement = idRequirement;
    }

    public static LegacyKmsAeadKey create(LegacyKmsAeadParameters parameters, @Nullable Integer idRequirement) throws GeneralSecurityException {
        Bytes bytesCopyFrom;
        if (parameters.variant() == LegacyKmsAeadParameters.Variant.TINK) {
            if (idRequirement == null) {
                throw new GeneralSecurityException("For given Variant TINK the value of idRequirement must be non-null");
            }
            bytesCopyFrom = Bytes.copyFrom(ByteBuffer.allocate(5).put((byte) 1).putInt(idRequirement.intValue()).array());
        } else {
            if (parameters.variant() != LegacyKmsAeadParameters.Variant.NO_PREFIX) {
                throw new GeneralSecurityException("Unknown Variant: " + parameters.variant());
            }
            if (idRequirement != null) {
                throw new GeneralSecurityException("For given Variant NO_PREFIX the value of idRequirement must be null");
            }
            bytesCopyFrom = Bytes.copyFrom(new byte[0]);
        }
        return new LegacyKmsAeadKey(parameters, bytesCopyFrom, idRequirement);
    }

    public static LegacyKmsAeadKey create(LegacyKmsAeadParameters parameters) throws GeneralSecurityException {
        return create(parameters, null);
    }

    @Override // com.google.crypto.tink.aead.AeadKey
    public Bytes getOutputPrefix() {
        return this.outputPrefix;
    }

    @Override // com.google.crypto.tink.aead.AeadKey, com.google.crypto.tink.Key
    public LegacyKmsAeadParameters getParameters() {
        return this.parameters;
    }

    @Override // com.google.crypto.tink.Key
    public Integer getIdRequirementOrNull() {
        return this.idRequirement;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof LegacyKmsAeadKey)) {
            return false;
        }
        LegacyKmsAeadKey legacyKmsAeadKey = (LegacyKmsAeadKey) o;
        return legacyKmsAeadKey.parameters.equals(this.parameters) && Objects.equals(legacyKmsAeadKey.idRequirement, this.idRequirement);
    }
}
