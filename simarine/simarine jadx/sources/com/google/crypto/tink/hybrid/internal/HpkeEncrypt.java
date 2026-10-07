package com.google.crypto.tink.hybrid.internal;

import com.google.crypto.tink.HybridEncrypt;
import com.google.crypto.tink.hybrid.HpkeParameters;
import com.google.crypto.tink.hybrid.HpkePublicKey;
import com.google.crypto.tink.util.Bytes;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class HpkeEncrypt implements HybridEncrypt {
    private static final byte[] EMPTY_ASSOCIATED_DATA = new byte[0];
    private final HpkeAead aead;
    private final HpkeKdf kdf;
    private final HpkeKem kem;
    private final byte[] outputPrefix;
    private final byte[] recipientPublicKey;

    private HpkeEncrypt(Bytes recipientPublicKey, HpkeKem kem, HpkeKdf kdf, HpkeAead aead, Bytes outputPrefix) {
        this.recipientPublicKey = recipientPublicKey.toByteArray();
        this.kem = kem;
        this.kdf = kdf;
        this.aead = aead;
        this.outputPrefix = outputPrefix.toByteArray();
    }

    public static HybridEncrypt create(HpkePublicKey key) throws GeneralSecurityException {
        HpkeParameters parameters = key.getParameters();
        return new HpkeEncrypt(key.getPublicKeyBytes(), HpkePrimitiveFactory.createKem(parameters.getKemId()), HpkePrimitiveFactory.createKdf(parameters.getKdfId()), HpkePrimitiveFactory.createAead(parameters.getAeadId()), key.getOutputPrefix());
    }

    @Override // com.google.crypto.tink.HybridEncrypt
    public byte[] encrypt(final byte[] plaintext, final byte[] contextInfo) throws GeneralSecurityException {
        if (contextInfo == null) {
            contextInfo = new byte[0];
        }
        HpkeContext hpkeContextCreateSenderContext = HpkeContext.createSenderContext(this.recipientPublicKey, this.kem, this.kdf, this.aead, contextInfo);
        byte[] encapsulatedKey = hpkeContextCreateSenderContext.getEncapsulatedKey();
        byte[] bArrSeal = hpkeContextCreateSenderContext.seal(plaintext, this.outputPrefix.length + encapsulatedKey.length, EMPTY_ASSOCIATED_DATA);
        byte[] bArr = this.outputPrefix;
        System.arraycopy(bArr, 0, bArrSeal, 0, bArr.length);
        System.arraycopy(encapsulatedKey, 0, bArrSeal, this.outputPrefix.length, encapsulatedKey.length);
        return bArrSeal;
    }
}
