package com.google.crypto.tink.hybrid;

import com.google.crypto.tink.aead.AesCtrHmacAeadParameters;
import com.google.crypto.tink.aead.AesGcmParameters;
import com.google.crypto.tink.internal.TinkBugException;

/* JADX INFO: loaded from: classes2.dex */
public final class PredefinedHybridParameters {
    public static final EciesParameters ECIES_P256_HKDF_HMAC_SHA256_AES128_GCM = (EciesParameters) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.hybrid.PredefinedHybridParameters$$ExternalSyntheticLambda0
        @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
        public final Object get() {
            return EciesParameters.builder().setCurveType(EciesParameters.CurveType.NIST_P256).setHashType(EciesParameters.HashType.SHA256).setNistCurvePointFormat(EciesParameters.PointFormat.UNCOMPRESSED).setVariant(EciesParameters.Variant.TINK).setDemParameters(AesGcmParameters.builder().setIvSizeBytes(12).setKeySizeBytes(16).setTagSizeBytes(16).setVariant(AesGcmParameters.Variant.NO_PREFIX).build()).build();
        }
    });
    public static final EciesParameters ECIES_P256_HKDF_HMAC_SHA256_AES128_GCM_COMPRESSED_WITHOUT_PREFIX = (EciesParameters) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.hybrid.PredefinedHybridParameters$$ExternalSyntheticLambda1
        @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
        public final Object get() {
            return EciesParameters.builder().setCurveType(EciesParameters.CurveType.NIST_P256).setHashType(EciesParameters.HashType.SHA256).setNistCurvePointFormat(EciesParameters.PointFormat.COMPRESSED).setVariant(EciesParameters.Variant.NO_PREFIX).setDemParameters(AesGcmParameters.builder().setIvSizeBytes(12).setKeySizeBytes(16).setTagSizeBytes(16).setVariant(AesGcmParameters.Variant.NO_PREFIX).build()).build();
        }
    });
    public static final EciesParameters ECIES_P256_HKDF_HMAC_SHA256_AES128_CTR_HMAC_SHA256 = (EciesParameters) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.hybrid.PredefinedHybridParameters$$ExternalSyntheticLambda2
        @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
        public final Object get() {
            return EciesParameters.builder().setCurveType(EciesParameters.CurveType.NIST_P256).setHashType(EciesParameters.HashType.SHA256).setNistCurvePointFormat(EciesParameters.PointFormat.UNCOMPRESSED).setVariant(EciesParameters.Variant.TINK).setDemParameters(AesCtrHmacAeadParameters.builder().setAesKeySizeBytes(16).setHmacKeySizeBytes(32).setTagSizeBytes(16).setIvSizeBytes(16).setHashType(AesCtrHmacAeadParameters.HashType.SHA256).setVariant(AesCtrHmacAeadParameters.Variant.NO_PREFIX).build()).build();
        }
    });

    private PredefinedHybridParameters() {
    }
}
