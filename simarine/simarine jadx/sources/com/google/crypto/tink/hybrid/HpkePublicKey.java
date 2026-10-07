package com.google.crypto.tink.hybrid;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.internal.EllipticCurvesUtil;
import com.google.crypto.tink.internal.OutputPrefixUtil;
import com.google.crypto.tink.subtle.EllipticCurves;
import com.google.crypto.tink.util.Bytes;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.spec.EllipticCurve;
import java.util.Objects;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class HpkePublicKey extends HybridPublicKey {

    @Nullable
    private final Integer idRequirement;
    private final Bytes outputPrefix;
    private final HpkeParameters parameters;
    private final Bytes publicKeyBytes;

    private HpkePublicKey(HpkeParameters parameters, Bytes publicKeyBytes, Bytes outputPrefix, @Nullable Integer idRequirement) {
        this.parameters = parameters;
        this.publicKeyBytes = publicKeyBytes;
        this.outputPrefix = outputPrefix;
        this.idRequirement = idRequirement;
    }

    private static void validateIdRequirement(HpkeParameters.Variant variant, @Nullable Integer idRequirement) throws GeneralSecurityException {
        if (!variant.equals(HpkeParameters.Variant.NO_PREFIX) && idRequirement == null) {
            throw new GeneralSecurityException("'idRequirement' must be non-null for " + variant + " variant.");
        }
        if (variant.equals(HpkeParameters.Variant.NO_PREFIX) && idRequirement != null) {
            throw new GeneralSecurityException("'idRequirement' must be null for NO_PREFIX variant.");
        }
    }

    private static void validatePublicKeyByteLength(HpkeParameters.KemId kemId, Bytes publicKeyBytes) throws GeneralSecurityException {
        int size = publicKeyBytes.size();
        String str = "Encoded public key byte length for " + kemId + " must be %d, not " + size;
        if (kemId == HpkeParameters.KemId.DHKEM_P256_HKDF_SHA256) {
            if (size != 65) {
                throw new GeneralSecurityException(String.format(str, 65));
            }
            return;
        }
        if (kemId == HpkeParameters.KemId.DHKEM_P384_HKDF_SHA384) {
            if (size != 97) {
                throw new GeneralSecurityException(String.format(str, 97));
            }
        } else if (kemId == HpkeParameters.KemId.DHKEM_P521_HKDF_SHA512) {
            if (size != 133) {
                throw new GeneralSecurityException(String.format(str, 133));
            }
        } else if (kemId == HpkeParameters.KemId.DHKEM_X25519_HKDF_SHA256) {
            if (size != 32) {
                throw new GeneralSecurityException(String.format(str, 32));
            }
        } else {
            throw new GeneralSecurityException("Unable to validate public key length for " + kemId);
        }
    }

    private static boolean isNistKem(HpkeParameters.KemId kemId) {
        return kemId == HpkeParameters.KemId.DHKEM_P256_HKDF_SHA256 || kemId == HpkeParameters.KemId.DHKEM_P384_HKDF_SHA384 || kemId == HpkeParameters.KemId.DHKEM_P521_HKDF_SHA512;
    }

    private static EllipticCurve getNistCurve(HpkeParameters.KemId kemId) {
        if (kemId == HpkeParameters.KemId.DHKEM_P256_HKDF_SHA256) {
            return EllipticCurves.getNistP256Params().getCurve();
        }
        if (kemId == HpkeParameters.KemId.DHKEM_P384_HKDF_SHA384) {
            return EllipticCurves.getNistP384Params().getCurve();
        }
        if (kemId == HpkeParameters.KemId.DHKEM_P521_HKDF_SHA512) {
            return EllipticCurves.getNistP521Params().getCurve();
        }
        throw new IllegalArgumentException("Unable to determine NIST curve type for " + kemId);
    }

    private static void validatePublicKeyOnCurve(HpkeParameters.KemId kemId, Bytes publicKeyBytes) throws GeneralSecurityException {
        if (isNistKem(kemId)) {
            EllipticCurve nistCurve = getNistCurve(kemId);
            EllipticCurvesUtil.checkPointOnCurve(EllipticCurves.pointDecode(nistCurve, EllipticCurves.PointFormatType.UNCOMPRESSED, publicKeyBytes.toByteArray()), nistCurve);
        }
    }

    private static void validatePublicKey(HpkeParameters.KemId kemId, Bytes publicKeyBytes) throws GeneralSecurityException {
        validatePublicKeyByteLength(kemId, publicKeyBytes);
        validatePublicKeyOnCurve(kemId, publicKeyBytes);
    }

    private static Bytes createOutputPrefix(HpkeParameters.Variant variant, @Nullable Integer idRequirement) {
        if (variant == HpkeParameters.Variant.NO_PREFIX) {
            return OutputPrefixUtil.EMPTY_PREFIX;
        }
        if (idRequirement == null) {
            throw new IllegalStateException("idRequirement must be non-null for HpkeParameters.Variant " + variant);
        }
        if (variant == HpkeParameters.Variant.CRUNCHY) {
            return OutputPrefixUtil.getLegacyOutputPrefix(idRequirement.intValue());
        }
        if (variant == HpkeParameters.Variant.TINK) {
            return OutputPrefixUtil.getTinkOutputPrefix(idRequirement.intValue());
        }
        throw new IllegalStateException("Unknown HpkeParameters.Variant: " + variant);
    }

    public static HpkePublicKey create(HpkeParameters parameters, Bytes publicKeyBytes, @Nullable Integer idRequirement) throws GeneralSecurityException {
        validateIdRequirement(parameters.getVariant(), idRequirement);
        validatePublicKey(parameters.getKemId(), publicKeyBytes);
        return new HpkePublicKey(parameters, publicKeyBytes, createOutputPrefix(parameters.getVariant(), idRequirement), idRequirement);
    }

    public Bytes getPublicKeyBytes() {
        return this.publicKeyBytes;
    }

    @Override // com.google.crypto.tink.hybrid.HybridPublicKey
    public Bytes getOutputPrefix() {
        return this.outputPrefix;
    }

    @Override // com.google.crypto.tink.hybrid.HybridPublicKey, com.google.crypto.tink.Key
    public HpkeParameters getParameters() {
        return this.parameters;
    }

    @Override // com.google.crypto.tink.Key
    @Nullable
    public Integer getIdRequirementOrNull() {
        return this.idRequirement;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof HpkePublicKey)) {
            return false;
        }
        HpkePublicKey hpkePublicKey = (HpkePublicKey) o;
        return this.parameters.equals(hpkePublicKey.parameters) && this.publicKeyBytes.equals(hpkePublicKey.publicKeyBytes) && Objects.equals(this.idRequirement, hpkePublicKey.idRequirement);
    }
}
