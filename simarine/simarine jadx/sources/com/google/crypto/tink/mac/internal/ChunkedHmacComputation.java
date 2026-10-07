package com.google.crypto.tink.mac.internal;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.mac.ChunkedMacComputation;
import com.google.crypto.tink.mac.HmacKey;
import com.google.crypto.tink.mac.HmacParameters;
import com.google.crypto.tink.subtle.Bytes;
import com.google.crypto.tink.subtle.EngineFactory;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/* JADX INFO: loaded from: classes2.dex */
final class ChunkedHmacComputation implements ChunkedMacComputation {
    private static final byte[] FORMAT_VERSION = {0};
    private boolean finalized = false;
    private final HmacKey key;
    private final Mac mac;

    ChunkedHmacComputation(HmacKey key) throws GeneralSecurityException {
        Mac engineFactory = EngineFactory.MAC.getInstance(composeAlgorithmName(key));
        this.mac = engineFactory;
        engineFactory.init(new SecretKeySpec(key.getKeyBytes().toByteArray(InsecureSecretKeyAccess.get()), "HMAC"));
        this.key = key;
    }

    @Override // com.google.crypto.tink.mac.ChunkedMacComputation
    public void update(ByteBuffer data) {
        if (this.finalized) {
            throw new IllegalStateException("Cannot update after computing the MAC tag. Please create a new object.");
        }
        this.mac.update(data);
    }

    @Override // com.google.crypto.tink.mac.ChunkedMacComputation
    public byte[] computeMac() throws GeneralSecurityException {
        if (this.finalized) {
            throw new IllegalStateException("Cannot compute after already computing the MAC tag. Please create a new object.");
        }
        if (this.key.getParameters().getVariant() == HmacParameters.Variant.LEGACY) {
            update(ByteBuffer.wrap(FORMAT_VERSION));
        }
        this.finalized = true;
        return Bytes.concat(this.key.getOutputPrefix().toByteArray(), Arrays.copyOf(this.mac.doFinal(), this.key.getParameters().getCryptographicTagSizeBytes()));
    }

    private static String composeAlgorithmName(HmacKey key) {
        return "HMAC" + key.getParameters().getHashType();
    }
}
