package com.google.crypto.tink.prf;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Objects;

/* JADX INFO: loaded from: classes2.dex */
public final class AesCmacPrfParameters extends PrfParameters {
    private final int keySizeBytes;

    @Override // com.google.crypto.tink.Parameters
    public boolean hasIdRequirement() {
        return false;
    }

    public static AesCmacPrfParameters create(int keySizeBytes) throws GeneralSecurityException {
        if (keySizeBytes != 16 && keySizeBytes != 32) {
            throw new InvalidAlgorithmParameterException(String.format("Invalid key size %d; only 128-bit and 256-bit are supported", Integer.valueOf(keySizeBytes * 8)));
        }
        return new AesCmacPrfParameters(keySizeBytes);
    }

    private AesCmacPrfParameters(int keySizeBytes) {
        this.keySizeBytes = keySizeBytes;
    }

    public int getKeySizeBytes() {
        return this.keySizeBytes;
    }

    public boolean equals(Object o) {
        return (o instanceof AesCmacPrfParameters) && ((AesCmacPrfParameters) o).getKeySizeBytes() == getKeySizeBytes();
    }

    public int hashCode() {
        return Objects.hash(AesCmacPrfParameters.class, Integer.valueOf(this.keySizeBytes));
    }

    public String toString() {
        return "AesCmac PRF Parameters (" + this.keySizeBytes + "-byte key)";
    }
}
