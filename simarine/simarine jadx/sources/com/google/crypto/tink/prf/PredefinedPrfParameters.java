package com.google.crypto.tink.prf;

import com.google.crypto.tink.internal.TinkBugException;

/* JADX INFO: loaded from: classes2.dex */
public final class PredefinedPrfParameters {
    public static final HkdfPrfParameters HKDF_SHA256 = (HkdfPrfParameters) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.prf.PredefinedPrfParameters$$ExternalSyntheticLambda0
        @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
        public final Object get() {
            return HkdfPrfParameters.builder().setKeySizeBytes(32).setHashType(HkdfPrfParameters.HashType.SHA256).build();
        }
    });
    public static final HmacPrfParameters HMAC_SHA256_PRF = (HmacPrfParameters) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.prf.PredefinedPrfParameters$$ExternalSyntheticLambda1
        @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
        public final Object get() {
            return HmacPrfParameters.builder().setKeySizeBytes(32).setHashType(HmacPrfParameters.HashType.SHA256).build();
        }
    });
    public static final HmacPrfParameters HMAC_SHA512_PRF = (HmacPrfParameters) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.prf.PredefinedPrfParameters$$ExternalSyntheticLambda2
        @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
        public final Object get() {
            return HmacPrfParameters.builder().setKeySizeBytes(64).setHashType(HmacPrfParameters.HashType.SHA512).build();
        }
    });
    public static final AesCmacPrfParameters AES_CMAC_PRF = (AesCmacPrfParameters) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.prf.PredefinedPrfParameters$$ExternalSyntheticLambda3
        @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
        public final Object get() {
            return AesCmacPrfParameters.create(32);
        }
    });

    private PredefinedPrfParameters() {
    }
}
