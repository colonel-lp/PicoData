package com.google.crypto.tink.subtle;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.mac.internal.AesUtil;
import com.google.crypto.tink.prf.AesCmacPrfKey;
import com.google.crypto.tink.prf.Prf;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class PrfAesCmac implements Prf {
    public static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_NOT_FIPS;
    private static final ThreadLocal<Cipher> localAesCipher = new ThreadLocal<Cipher>() { // from class: com.google.crypto.tink.subtle.PrfAesCmac.1
        /* JADX INFO: Access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public Cipher initialValue() {
            try {
                return EngineFactory.CIPHER.getInstance("AES/ECB/NoPadding");
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException(e);
            }
        }
    };
    private final SecretKey keySpec;
    private byte[] subKey1;
    private byte[] subKey2;

    private static Cipher instance() throws GeneralSecurityException {
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Can not use AES-CMAC in FIPS-mode.");
        }
        return localAesCipher.get();
    }

    public PrfAesCmac(final byte[] key) throws GeneralSecurityException {
        Validators.validateAesKeySize(key.length);
        this.keySpec = new SecretKeySpec(key, "AES");
        generateSubKeys();
    }

    public static Prf create(AesCmacPrfKey key) throws GeneralSecurityException {
        return new PrfAesCmac(key.getKeyBytes().toByteArray(InsecureSecretKeyAccess.get()));
    }

    static int calcN(int dataLength) {
        if (dataLength == 0) {
            return 1;
        }
        return ((dataLength - 1) / 16) + 1;
    }

    private static void xorBlock(final byte[] x, final byte[] y, int offsetY, byte[] output) {
        for (int i = 0; i < 16; i++) {
            output[i] = (byte) (x[i] ^ y[i + offsetY]);
        }
    }

    @Override // com.google.crypto.tink.prf.Prf
    public byte[] compute(final byte[] data, int outputLength) throws GeneralSecurityException {
        byte[] bArrXor;
        if (outputLength > 16) {
            throw new InvalidAlgorithmParameterException("outputLength too large, max is 16 bytes");
        }
        Cipher cipherInstance = instance();
        cipherInstance.init(1, this.keySpec);
        int iCalcN = calcN(data.length);
        if (iCalcN * 16 == data.length) {
            bArrXor = Bytes.xor(data, (iCalcN - 1) * 16, this.subKey1, 0, 16);
        } else {
            bArrXor = Bytes.xor(AesUtil.cmacPad(Arrays.copyOfRange(data, (iCalcN - 1) * 16, data.length)), this.subKey2);
        }
        byte[] bArr = new byte[16];
        byte[] bArr2 = new byte[16];
        for (int i = 0; i < iCalcN - 1; i++) {
            xorBlock(bArr, data, i * 16, bArr2);
            if (cipherInstance.doFinal(bArr2, 0, 16, bArr) != 16) {
                throw new IllegalStateException("Cipher didn't write full block");
            }
        }
        xorBlock(bArr, bArrXor, 0, bArr2);
        if (cipherInstance.doFinal(bArr2, 0, 16, bArr) == 16) {
            return 16 == outputLength ? bArr : Arrays.copyOf(bArr, outputLength);
        }
        throw new IllegalStateException("Cipher didn't write full block");
    }

    private void generateSubKeys() throws GeneralSecurityException {
        Cipher cipherInstance = instance();
        cipherInstance.init(1, this.keySpec);
        byte[] bArrDbl = AesUtil.dbl(cipherInstance.doFinal(new byte[16]));
        this.subKey1 = bArrDbl;
        this.subKey2 = AesUtil.dbl(bArrDbl);
    }
}
