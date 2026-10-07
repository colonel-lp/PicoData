package com.google.crypto.tink.mac;

import com.google.crypto.tink.internal.TinkBugException;

/* JADX INFO: loaded from: classes2.dex */
public final class PredefinedMacParameters {
    public static final HmacParameters HMAC_SHA256_128BITTAG = (HmacParameters) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.mac.PredefinedMacParameters$$ExternalSyntheticLambda0
        @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
        public final Object get() {
            return HmacParameters.builder().setKeySizeBytes(32).setTagSizeBytes(16).setVariant(HmacParameters.Variant.TINK).setHashType(HmacParameters.HashType.SHA256).build();
        }
    });
    public static final HmacParameters HMAC_SHA256_256BITTAG = (HmacParameters) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.mac.PredefinedMacParameters$$ExternalSyntheticLambda1
        @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
        public final Object get() {
            return HmacParameters.builder().setKeySizeBytes(32).setTagSizeBytes(32).setVariant(HmacParameters.Variant.TINK).setHashType(HmacParameters.HashType.SHA256).build();
        }
    });
    public static final HmacParameters HMAC_SHA512_256BITTAG = (HmacParameters) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.mac.PredefinedMacParameters$$ExternalSyntheticLambda2
        @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
        public final Object get() {
            return HmacParameters.builder().setKeySizeBytes(64).setTagSizeBytes(32).setVariant(HmacParameters.Variant.TINK).setHashType(HmacParameters.HashType.SHA512).build();
        }
    });
    public static final HmacParameters HMAC_SHA512_512BITTAG = (HmacParameters) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.mac.PredefinedMacParameters$$ExternalSyntheticLambda3
        @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
        public final Object get() {
            return HmacParameters.builder().setKeySizeBytes(64).setTagSizeBytes(64).setVariant(HmacParameters.Variant.TINK).setHashType(HmacParameters.HashType.SHA512).build();
        }
    });
    public static final AesCmacParameters AES_CMAC = (AesCmacParameters) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.mac.PredefinedMacParameters$$ExternalSyntheticLambda4
        @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
        public final Object get() {
            return AesCmacParameters.builder().setKeySizeBytes(32).setTagSizeBytes(16).setVariant(AesCmacParameters.Variant.TINK).build();
        }
    });

    private PredefinedMacParameters() {
    }
}
