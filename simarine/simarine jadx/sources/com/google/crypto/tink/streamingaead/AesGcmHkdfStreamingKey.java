package com.google.crypto.tink.streamingaead;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.util.SecretBytes;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public final class AesGcmHkdfStreamingKey extends StreamingAeadKey {
    private final SecretBytes initialKeymaterial;
    private final AesGcmHkdfStreamingParameters parameters;

    private AesGcmHkdfStreamingKey(AesGcmHkdfStreamingParameters parameters, SecretBytes initialKeymaterial) {
        this.parameters = parameters;
        this.initialKeymaterial = initialKeymaterial;
    }

    public static AesGcmHkdfStreamingKey create(AesGcmHkdfStreamingParameters parameters, SecretBytes initialKeymaterial) throws GeneralSecurityException {
        if (parameters.getKeySizeBytes() != initialKeymaterial.size()) {
            throw new GeneralSecurityException("Key size mismatch");
        }
        return new AesGcmHkdfStreamingKey(parameters, initialKeymaterial);
    }

    public SecretBytes getInitialKeyMaterial() {
        return this.initialKeymaterial;
    }

    @Override // com.google.crypto.tink.streamingaead.StreamingAeadKey, com.google.crypto.tink.Key
    public AesGcmHkdfStreamingParameters getParameters() {
        return this.parameters;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof AesGcmHkdfStreamingKey)) {
            return false;
        }
        AesGcmHkdfStreamingKey aesGcmHkdfStreamingKey = (AesGcmHkdfStreamingKey) o;
        return aesGcmHkdfStreamingKey.parameters.equals(this.parameters) && aesGcmHkdfStreamingKey.initialKeymaterial.equalsSecretBytes(this.initialKeymaterial);
    }
}
