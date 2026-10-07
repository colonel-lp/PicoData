package com.google.crypto.tink.hybrid.internal;

/* JADX INFO: loaded from: classes2.dex */
final class HpkeKemEncapOutput {
    private final byte[] encapsulatedKey;
    private final byte[] sharedSecret;

    HpkeKemEncapOutput(byte[] sharedSecret, byte[] encapsulatedKey) {
        this.sharedSecret = sharedSecret;
        this.encapsulatedKey = encapsulatedKey;
    }

    byte[] getSharedSecret() {
        return this.sharedSecret;
    }

    byte[] getEncapsulatedKey() {
        return this.encapsulatedKey;
    }
}
