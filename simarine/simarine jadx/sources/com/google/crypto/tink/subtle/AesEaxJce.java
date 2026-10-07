package com.google.crypto.tink.subtle;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.aead.AesEaxKey;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.Util;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import kotlin.UByte;
import kotlin.jvm.internal.ByteCompanionObject;

/* JADX INFO: loaded from: classes2.dex */
public final class AesEaxJce implements Aead {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final int BLOCK_SIZE_IN_BYTES = 16;
    static final int TAG_SIZE_IN_BYTES = 16;
    private final byte[] b;
    private final int ivSizeInBytes;
    private final SecretKeySpec keySpec;
    private final byte[] outputPrefix;
    private final byte[] p;
    public static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_NOT_FIPS;
    private static final ThreadLocal<Cipher> localEcbCipher = new ThreadLocal<Cipher>() { // from class: com.google.crypto.tink.subtle.AesEaxJce.1
        /* JADX INFO: Access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public Cipher initialValue() {
            try {
                return EngineFactory.CIPHER.getInstance("AES/ECB/NOPADDING");
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException(e);
            }
        }
    };
    private static final ThreadLocal<Cipher> localCtrCipher = new ThreadLocal<Cipher>() { // from class: com.google.crypto.tink.subtle.AesEaxJce.2
        /* JADX INFO: Access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public Cipher initialValue() {
            try {
                return EngineFactory.CIPHER.getInstance("AES/CTR/NOPADDING");
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException(e);
            }
        }
    };

    public static Aead create(AesEaxKey key) throws GeneralSecurityException {
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Can not use AES-EAX in FIPS-mode.");
        }
        if (key.getParameters().getTagSizeBytes() != 16) {
            throw new GeneralSecurityException("AesEaxJce only supports 16 byte tag size, not " + key.getParameters().getTagSizeBytes());
        }
        return new AesEaxJce(key.getKeyBytes().toByteArray(InsecureSecretKeyAccess.get()), key.getParameters().getIvSizeBytes(), key.getOutputPrefix().toByteArray());
    }

    private AesEaxJce(final byte[] key, int ivSizeInBytes, byte[] outputPrefix) throws GeneralSecurityException {
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Can not use AES-EAX in FIPS-mode.");
        }
        if (ivSizeInBytes != 12 && ivSizeInBytes != 16) {
            throw new IllegalArgumentException("IV size should be either 12 or 16 bytes");
        }
        this.ivSizeInBytes = ivSizeInBytes;
        Validators.validateAesKeySize(key.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        this.keySpec = secretKeySpec;
        Cipher cipher = localEcbCipher.get();
        cipher.init(1, secretKeySpec);
        byte[] bArrMultiplyByX = multiplyByX(cipher.doFinal(new byte[16]));
        this.b = bArrMultiplyByX;
        this.p = multiplyByX(bArrMultiplyByX);
        this.outputPrefix = outputPrefix;
    }

    public AesEaxJce(final byte[] key, int ivSizeInBytes) throws GeneralSecurityException {
        this(key, ivSizeInBytes, new byte[0]);
    }

    private static void xor(final byte[] x, final byte[] y) {
        int length = x.length;
        for (int i = 0; i < length; i++) {
            x[i] = (byte) (x[i] ^ y[i]);
        }
    }

    private static byte[] multiplyByX(final byte[] block) {
        byte[] bArr = new byte[16];
        int i = 0;
        while (i < 15) {
            int i2 = i + 1;
            bArr[i] = (byte) (((block[i] << 1) ^ ((block[i2] & UByte.MAX_VALUE) >>> 7)) & 255);
            i = i2;
        }
        bArr[15] = (byte) (((block[0] >> 7) & 135) ^ (block[15] << 1));
        return bArr;
    }

    private byte[] pad(final byte[] data, int lastBlockFrom, int lastBlockTo) {
        byte[] bArrCopyOfRange = Arrays.copyOfRange(data, lastBlockFrom, lastBlockTo);
        if (bArrCopyOfRange.length == 16) {
            xor(bArrCopyOfRange, this.b);
            return bArrCopyOfRange;
        }
        byte[] bArrCopyOf = Arrays.copyOf(this.p, 16);
        for (int i = 0; i < bArrCopyOfRange.length; i++) {
            bArrCopyOf[i] = (byte) (bArrCopyOf[i] ^ bArrCopyOfRange[i]);
        }
        bArrCopyOf[bArrCopyOfRange.length] = (byte) (bArrCopyOf[bArrCopyOfRange.length] ^ ByteCompanionObject.MIN_VALUE);
        return bArrCopyOf;
    }

    private byte[] omac(Cipher ecb, int tag, final byte[] data, int offset, int length) throws BadPaddingException, IllegalBlockSizeException, ShortBufferException {
        byte[] bArr = new byte[16];
        bArr[15] = (byte) tag;
        if (length == 0) {
            xor(bArr, this.b);
            return ecb.doFinal(bArr);
        }
        byte[] bArr2 = new byte[16];
        ecb.doFinal(bArr, 0, 16, bArr2);
        int i = 0;
        while (true) {
            byte[] bArr3 = bArr;
            bArr = bArr2;
            bArr2 = bArr3;
            if (length - i > 16) {
                for (int i2 = 0; i2 < 16; i2++) {
                    bArr[i2] = (byte) (bArr[i2] ^ data[(offset + i) + i2]);
                }
                ecb.doFinal(bArr, 0, 16, bArr2);
                i += 16;
            } else {
                xor(bArr, pad(data, i + offset, offset + length));
                ecb.doFinal(bArr, 0, 16, bArr2);
                return bArr2;
            }
        }
    }

    @Override // com.google.crypto.tink.Aead
    public byte[] encrypt(final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
        int length = plaintext.length;
        byte[] bArr = this.outputPrefix;
        int length2 = Integer.MAX_VALUE - bArr.length;
        int i = this.ivSizeInBytes;
        if (length > (length2 - i) - 16) {
            throw new GeneralSecurityException("plaintext too long");
        }
        byte[] bArrCopyOf = Arrays.copyOf(bArr, bArr.length + i + plaintext.length + 16);
        byte[] bArrRandBytes = Random.randBytes(this.ivSizeInBytes);
        System.arraycopy(bArrRandBytes, 0, bArrCopyOf, this.outputPrefix.length, this.ivSizeInBytes);
        Cipher cipher = localEcbCipher.get();
        cipher.init(1, this.keySpec);
        byte[] bArrOmac = omac(cipher, 0, bArrRandBytes, 0, bArrRandBytes.length);
        byte[] bArr2 = associatedData == null ? new byte[0] : associatedData;
        byte[] bArrOmac2 = omac(cipher, 1, bArr2, 0, bArr2.length);
        Cipher cipher2 = localCtrCipher.get();
        cipher2.init(1, this.keySpec, new IvParameterSpec(bArrOmac));
        cipher2.doFinal(plaintext, 0, plaintext.length, bArrCopyOf, this.outputPrefix.length + this.ivSizeInBytes);
        byte[] bArrOmac3 = omac(cipher, 2, bArrCopyOf, this.outputPrefix.length + this.ivSizeInBytes, plaintext.length);
        int length3 = this.outputPrefix.length + plaintext.length + this.ivSizeInBytes;
        for (int i2 = 0; i2 < 16; i2++) {
            bArrCopyOf[length3 + i2] = (byte) ((bArrOmac2[i2] ^ bArrOmac[i2]) ^ bArrOmac3[i2]);
        }
        return bArrCopyOf;
    }

    @Override // com.google.crypto.tink.Aead
    public byte[] decrypt(final byte[] ciphertext, final byte[] associatedData) throws GeneralSecurityException {
        int length = ciphertext.length;
        byte[] bArr = this.outputPrefix;
        int length2 = ((length - bArr.length) - this.ivSizeInBytes) - 16;
        if (length2 < 0) {
            throw new GeneralSecurityException("ciphertext too short");
        }
        if (!Util.isPrefix(bArr, ciphertext)) {
            throw new GeneralSecurityException("Decryption failed (OutputPrefix mismatch).");
        }
        Cipher cipher = localEcbCipher.get();
        cipher.init(1, this.keySpec);
        byte[] bArrOmac = omac(cipher, 0, ciphertext, this.outputPrefix.length, this.ivSizeInBytes);
        if (associatedData == null) {
            associatedData = new byte[0];
        }
        byte[] bArr2 = associatedData;
        byte[] bArrOmac2 = omac(cipher, 1, bArr2, 0, bArr2.length);
        byte[] bArrOmac3 = omac(cipher, 2, ciphertext, this.outputPrefix.length + this.ivSizeInBytes, length2);
        int length3 = ciphertext.length - 16;
        byte b = 0;
        for (int i = 0; i < 16; i++) {
            b = (byte) (b | (((ciphertext[length3 + i] ^ bArrOmac2[i]) ^ bArrOmac[i]) ^ bArrOmac3[i]));
        }
        if (b != 0) {
            throw new AEADBadTagException("tag mismatch");
        }
        Cipher cipher2 = localCtrCipher.get();
        cipher2.init(1, this.keySpec, new IvParameterSpec(bArrOmac));
        return cipher2.doFinal(ciphertext, this.outputPrefix.length + this.ivSizeInBytes, length2);
    }
}
