package com.google.crypto.tink.streamingaead;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.util.SecretBytes;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public final class AesCtrHmacStreamingKey extends StreamingAeadKey {
    private final SecretBytes initialKeymaterial;
    private final AesCtrHmacStreamingParameters parameters;

    private AesCtrHmacStreamingKey(AesCtrHmacStreamingParameters parameters, SecretBytes initialKeymaterial) {
        this.parameters = parameters;
        this.initialKeymaterial = initialKeymaterial;
    }

    public static AesCtrHmacStreamingKey create(AesCtrHmacStreamingParameters parameters, SecretBytes initialKeymaterial) throws GeneralSecurityException {
        if (parameters.getKeySizeBytes() != initialKeymaterial.size()) {
            throw new GeneralSecurityException("Key size mismatch");
        }
        return new AesCtrHmacStreamingKey(parameters, initialKeymaterial);
    }

    public SecretBytes getInitialKeyMaterial() {
        return this.initialKeymaterial;
    }

    @Override // com.google.crypto.tink.streamingaead.StreamingAeadKey, com.google.crypto.tink.Key
    public AesCtrHmacStreamingParameters getParameters() {
        return this.parameters;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof AesCtrHmacStreamingKey)) {
            return false;
        }
        AesCtrHmacStreamingKey aesCtrHmacStreamingKey = (AesCtrHmacStreamingKey) o;
        return aesCtrHmacStreamingKey.parameters.equals(this.parameters) && aesCtrHmacStreamingKey.initialKeymaterial.equalsSecretBytes(this.initialKeymaterial);
    }
}
