package com.google.crypto.tink.signature;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.internal.OutputPrefixUtil;
import com.google.crypto.tink.util.Bytes;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.util.Objects;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class Ed25519PublicKey extends SignaturePublicKey {

    @Nullable
    private final Integer idRequirement;
    private final Bytes outputPrefix;
    private final Ed25519Parameters parameters;
    private final Bytes publicKeyBytes;

    private Ed25519PublicKey(Ed25519Parameters parameters, Bytes publicKeyBytes, Bytes outputPrefix, @Nullable Integer idRequirement) {
        this.parameters = parameters;
        this.publicKeyBytes = publicKeyBytes;
        this.outputPrefix = outputPrefix;
        this.idRequirement = idRequirement;
    }

    private static Bytes createOutputPrefix(Ed25519Parameters parameters, @Nullable Integer idRequirement) {
        if (parameters.getVariant() == Ed25519Parameters.Variant.NO_PREFIX) {
            return OutputPrefixUtil.EMPTY_PREFIX;
        }
        if (parameters.getVariant() == Ed25519Parameters.Variant.CRUNCHY || parameters.getVariant() == Ed25519Parameters.Variant.LEGACY) {
            return OutputPrefixUtil.getLegacyOutputPrefix(idRequirement.intValue());
        }
        if (parameters.getVariant() == Ed25519Parameters.Variant.TINK) {
            return OutputPrefixUtil.getTinkOutputPrefix(idRequirement.intValue());
        }
        throw new IllegalStateException("Unknown Variant: " + parameters.getVariant());
    }

    @Override // com.google.crypto.tink.signature.SignaturePublicKey
    public Bytes getOutputPrefix() {
        return this.outputPrefix;
    }

    public static Ed25519PublicKey create(Bytes publicKeyBytes) throws GeneralSecurityException {
        return create(Ed25519Parameters.Variant.NO_PREFIX, publicKeyBytes, null);
    }

    public static Ed25519PublicKey create(Ed25519Parameters.Variant variant, Bytes publicKeyBytes, @Nullable Integer idRequirement) throws GeneralSecurityException {
        Ed25519Parameters ed25519ParametersCreate = Ed25519Parameters.create(variant);
        if (!variant.equals(Ed25519Parameters.Variant.NO_PREFIX) && idRequirement == null) {
            throw new GeneralSecurityException("For given Variant " + variant + " the value of idRequirement must be non-null");
        }
        if (variant.equals(Ed25519Parameters.Variant.NO_PREFIX) && idRequirement != null) {
            throw new GeneralSecurityException("For given Variant NO_PREFIX the value of idRequirement must be null");
        }
        if (publicKeyBytes.size() != 32) {
            throw new GeneralSecurityException("Ed25519 key must be constructed with key of length 32 bytes, not " + publicKeyBytes.size());
        }
        return new Ed25519PublicKey(ed25519ParametersCreate, publicKeyBytes, createOutputPrefix(ed25519ParametersCreate, idRequirement), idRequirement);
    }

    public Bytes getPublicKeyBytes() {
        return this.publicKeyBytes;
    }

    @Override // com.google.crypto.tink.signature.SignaturePublicKey, com.google.crypto.tink.Key
    public Ed25519Parameters getParameters() {
        return this.parameters;
    }

    @Override // com.google.crypto.tink.Key
    @Nullable
    public Integer getIdRequirementOrNull() {
        return this.idRequirement;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof Ed25519PublicKey)) {
            return false;
        }
        Ed25519PublicKey ed25519PublicKey = (Ed25519PublicKey) o;
        return ed25519PublicKey.parameters.equals(this.parameters) && ed25519PublicKey.publicKeyBytes.equals(this.publicKeyBytes) && Objects.equals(ed25519PublicKey.idRequirement, this.idRequirement);
    }
}
