package com.google.crypto.tink.aead;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.internal.OutputPrefixUtil;
import com.google.crypto.tink.util.Bytes;
import com.google.crypto.tink.util.SecretBytes;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.util.Objects;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class XAesGcmKey extends AeadKey {

    @Nullable
    private final Integer idRequirement;
    private final SecretBytes keyBytes;
    private final Bytes outputPrefix;
    private final XAesGcmParameters parameters;

    private XAesGcmKey(XAesGcmParameters parameters, SecretBytes keyBytes, Bytes outputPrefix, @Nullable Integer idRequirement) {
        this.parameters = parameters;
        this.keyBytes = keyBytes;
        this.outputPrefix = outputPrefix;
        this.idRequirement = idRequirement;
    }

    private static Bytes getOutputPrefix(XAesGcmParameters parameters, @Nullable Integer idRequirement) {
        if (parameters.getVariant() == XAesGcmParameters.Variant.NO_PREFIX) {
            return OutputPrefixUtil.EMPTY_PREFIX;
        }
        if (parameters.getVariant() == XAesGcmParameters.Variant.TINK) {
            return OutputPrefixUtil.getTinkOutputPrefix(idRequirement.intValue());
        }
        throw new IllegalStateException("Unknown Variant: " + parameters.getVariant());
    }

    @Override // com.google.crypto.tink.aead.AeadKey
    public Bytes getOutputPrefix() {
        return this.outputPrefix;
    }

    public static XAesGcmKey create(XAesGcmParameters parameters, SecretBytes secretBytes, @Nullable Integer idRequirement) throws GeneralSecurityException {
        if (parameters.getVariant() != XAesGcmParameters.Variant.NO_PREFIX && idRequirement == null) {
            throw new GeneralSecurityException("For given Variant " + parameters.getVariant() + " the value of idRequirement must be non-null");
        }
        if (parameters.getVariant() == XAesGcmParameters.Variant.NO_PREFIX && idRequirement != null) {
            throw new GeneralSecurityException("For given Variant NO_PREFIX the value of idRequirement must be null");
        }
        if (secretBytes.size() != 32) {
            throw new GeneralSecurityException("XAesGcmKey key must be constructed with key of length 32 bytes, not " + secretBytes.size());
        }
        return new XAesGcmKey(parameters, secretBytes, getOutputPrefix(parameters, idRequirement), idRequirement);
    }

    public SecretBytes getKeyBytes() {
        return this.keyBytes;
    }

    @Override // com.google.crypto.tink.aead.AeadKey, com.google.crypto.tink.Key
    public XAesGcmParameters getParameters() {
        return this.parameters;
    }

    @Override // com.google.crypto.tink.Key
    @Nullable
    public Integer getIdRequirementOrNull() {
        return this.idRequirement;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof XAesGcmKey)) {
            return false;
        }
        XAesGcmKey xAesGcmKey = (XAesGcmKey) o;
        return xAesGcmKey.parameters.equals(this.parameters) && xAesGcmKey.keyBytes.equalsSecretBytes(this.keyBytes) && Objects.equals(xAesGcmKey.idRequirement, this.idRequirement);
    }
}
