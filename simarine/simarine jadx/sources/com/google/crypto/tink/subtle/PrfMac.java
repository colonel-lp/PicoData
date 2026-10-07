package com.google.crypto.tink.subtle;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.Mac;
import com.google.crypto.tink.mac.AesCmacKey;
import com.google.crypto.tink.mac.AesCmacParameters;
import com.google.crypto.tink.mac.HmacKey;
import com.google.crypto.tink.mac.HmacParameters;
import com.google.crypto.tink.prf.Prf;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Arrays;
import javax.crypto.spec.SecretKeySpec;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public class PrfMac implements Mac {
    private static final byte[] FORMAT_VERSION = {0};
    static final int MIN_TAG_SIZE_IN_BYTES = 10;
    private final byte[] outputPrefix;
    private final byte[] plaintextLegacySuffix;
    private final int tagSize;
    private final Prf wrappedPrf;

    public PrfMac(Prf wrappedPrf, int tagSize) throws GeneralSecurityException {
        this.wrappedPrf = wrappedPrf;
        this.tagSize = tagSize;
        this.outputPrefix = new byte[0];
        this.plaintextLegacySuffix = new byte[0];
        if (tagSize < 10) {
            throw new InvalidAlgorithmParameterException("tag size too small, need at least 10 bytes");
        }
        wrappedPrf.compute(new byte[0], tagSize);
    }

    private PrfMac(AesCmacKey key) throws GeneralSecurityException {
        this.wrappedPrf = new PrfAesCmac(key.getAesKey().toByteArray(InsecureSecretKeyAccess.get()));
        this.tagSize = key.getParameters().getCryptographicTagSizeBytes();
        this.outputPrefix = key.getOutputPrefix().toByteArray();
        if (key.getParameters().getVariant().equals(AesCmacParameters.Variant.LEGACY)) {
            byte[] bArr = FORMAT_VERSION;
            this.plaintextLegacySuffix = Arrays.copyOf(bArr, bArr.length);
        } else {
            this.plaintextLegacySuffix = new byte[0];
        }
    }

    private PrfMac(HmacKey key) throws GeneralSecurityException {
        this.wrappedPrf = new PrfHmacJce("HMAC" + key.getParameters().getHashType(), new SecretKeySpec(key.getKeyBytes().toByteArray(InsecureSecretKeyAccess.get()), "HMAC"));
        this.tagSize = key.getParameters().getCryptographicTagSizeBytes();
        this.outputPrefix = key.getOutputPrefix().toByteArray();
        if (key.getParameters().getVariant().equals(HmacParameters.Variant.LEGACY)) {
            byte[] bArr = FORMAT_VERSION;
            this.plaintextLegacySuffix = Arrays.copyOf(bArr, bArr.length);
        } else {
            this.plaintextLegacySuffix = new byte[0];
        }
    }

    public static Mac create(AesCmacKey key) throws GeneralSecurityException {
        return new PrfMac(key);
    }

    public static Mac create(HmacKey key) throws GeneralSecurityException {
        return new PrfMac(key);
    }

    @Override // com.google.crypto.tink.Mac
    public byte[] computeMac(byte[] data) throws GeneralSecurityException {
        byte[] bArr = this.plaintextLegacySuffix;
        if (bArr.length > 0) {
            return Bytes.concat(this.outputPrefix, this.wrappedPrf.compute(Bytes.concat(data, bArr), this.tagSize));
        }
        return Bytes.concat(this.outputPrefix, this.wrappedPrf.compute(data, this.tagSize));
    }

    @Override // com.google.crypto.tink.Mac
    public void verifyMac(byte[] mac, byte[] data) throws GeneralSecurityException {
        if (!Bytes.equal(computeMac(data), mac)) {
            throw new GeneralSecurityException("invalid MAC");
        }
    }
}
