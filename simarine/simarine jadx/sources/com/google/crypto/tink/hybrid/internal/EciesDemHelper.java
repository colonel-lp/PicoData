package com.google.crypto.tink.hybrid.internal;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.DeterministicAead;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.aead.AesCtrHmacAeadKey;
import com.google.crypto.tink.aead.AesCtrHmacAeadParameters;
import com.google.crypto.tink.aead.AesGcmParameters;
import com.google.crypto.tink.aead.internal.AesGcmJceUtil;
import com.google.crypto.tink.daead.AesSivKey;
import com.google.crypto.tink.daead.AesSivParameters;
import com.google.crypto.tink.hybrid.EciesParameters;
import com.google.crypto.tink.subtle.AesSiv;
import com.google.crypto.tink.subtle.Bytes;
import com.google.crypto.tink.subtle.EncryptThenAuthenticate;
import com.google.crypto.tink.subtle.Random;
import com.google.crypto.tink.util.SecretBytes;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

/* JADX INFO: loaded from: classes2.dex */
public final class EciesDemHelper {
    private static final byte[] EMPTY_AAD = new byte[0];

    public interface Dem {
        byte[] decrypt(byte[] demKeyValue, byte[] ciphertext, int prefixAndHeaderSize) throws GeneralSecurityException;

        byte[] encrypt(byte[] demKeyValue, byte[] prefix, byte[] header, byte[] plaintext) throws GeneralSecurityException;

        int getSymmetricKeySizeInBytes();
    }

    private static final class AesGcmDem implements Dem {
        private static final int AES_GCM_IV_SIZE_IN_BYTES = 12;
        private static final int AES_GCM_TAG_SIZE_IN_BYTES = 16;
        private final int keySizeInBytes;

        public AesGcmDem(AesGcmParameters parameters) throws GeneralSecurityException {
            if (parameters.getIvSizeBytes() != 12) {
                throw new GeneralSecurityException("invalid IV size");
            }
            if (parameters.getTagSizeBytes() != 16) {
                throw new GeneralSecurityException("invalid tag size");
            }
            if (parameters.getVariant() != AesGcmParameters.Variant.NO_PREFIX) {
                throw new GeneralSecurityException("invalid variant");
            }
            this.keySizeInBytes = parameters.getKeySizeBytes();
        }

        @Override // com.google.crypto.tink.hybrid.internal.EciesDemHelper.Dem
        public int getSymmetricKeySizeInBytes() {
            return this.keySizeInBytes;
        }

        @Override // com.google.crypto.tink.hybrid.internal.EciesDemHelper.Dem
        public byte[] encrypt(byte[] demKeyValue, byte[] prefix, byte[] header, byte[] plaintext) throws GeneralSecurityException {
            if (demKeyValue.length != this.keySizeInBytes) {
                throw new GeneralSecurityException("invalid key size");
            }
            SecretKey secretKey = AesGcmJceUtil.getSecretKey(demKeyValue);
            byte[] bArrRandBytes = Random.randBytes(12);
            AlgorithmParameterSpec params = AesGcmJceUtil.getParams(bArrRandBytes);
            Cipher threadLocalCipher = AesGcmJceUtil.getThreadLocalCipher();
            threadLocalCipher.init(1, secretKey, params);
            int outputSize = threadLocalCipher.getOutputSize(plaintext.length);
            int length = prefix.length + header.length;
            if (outputSize > 2147483635 - length) {
                throw new GeneralSecurityException("plaintext too long");
            }
            int i = length + 12;
            byte[] bArrCopyOf = Arrays.copyOf(prefix, i + outputSize);
            System.arraycopy(header, 0, bArrCopyOf, prefix.length, header.length);
            System.arraycopy(bArrRandBytes, 0, bArrCopyOf, length, 12);
            if (threadLocalCipher.doFinal(plaintext, 0, plaintext.length, bArrCopyOf, i) == outputSize) {
                return bArrCopyOf;
            }
            throw new GeneralSecurityException("not enough data written");
        }

        @Override // com.google.crypto.tink.hybrid.internal.EciesDemHelper.Dem
        public byte[] decrypt(byte[] demKeyValue, byte[] ciphertext, int prefixAndHeaderSize) throws GeneralSecurityException {
            if (ciphertext.length < prefixAndHeaderSize) {
                throw new GeneralSecurityException("ciphertext too short");
            }
            if (demKeyValue.length != this.keySizeInBytes) {
                throw new GeneralSecurityException("invalid key size");
            }
            SecretKey secretKey = AesGcmJceUtil.getSecretKey(demKeyValue);
            int i = prefixAndHeaderSize + 12;
            if (ciphertext.length < prefixAndHeaderSize + 28) {
                throw new GeneralSecurityException("ciphertext too short");
            }
            AlgorithmParameterSpec params = AesGcmJceUtil.getParams(ciphertext, prefixAndHeaderSize, 12);
            Cipher threadLocalCipher = AesGcmJceUtil.getThreadLocalCipher();
            threadLocalCipher.init(2, secretKey, params);
            return threadLocalCipher.doFinal(ciphertext, i, (ciphertext.length - prefixAndHeaderSize) - 12);
        }
    }

    private static final class AesCtrHmacDem implements Dem {
        private final int keySizeInBytes;
        private final AesCtrHmacAeadParameters parameters;

        public AesCtrHmacDem(AesCtrHmacAeadParameters parameters) {
            this.parameters = parameters;
            this.keySizeInBytes = parameters.getAesKeySizeBytes() + parameters.getHmacKeySizeBytes();
        }

        @Override // com.google.crypto.tink.hybrid.internal.EciesDemHelper.Dem
        public int getSymmetricKeySizeInBytes() {
            return this.keySizeInBytes;
        }

        private Aead getAead(byte[] symmetricKeyValue) throws GeneralSecurityException {
            return EncryptThenAuthenticate.create(AesCtrHmacAeadKey.builder().setParameters(this.parameters).setAesKeyBytes(SecretBytes.copyFrom(Arrays.copyOf(symmetricKeyValue, this.parameters.getAesKeySizeBytes()), InsecureSecretKeyAccess.get())).setHmacKeyBytes(SecretBytes.copyFrom(Arrays.copyOfRange(symmetricKeyValue, this.parameters.getAesKeySizeBytes(), this.parameters.getAesKeySizeBytes() + this.parameters.getHmacKeySizeBytes()), InsecureSecretKeyAccess.get())).build());
        }

        @Override // com.google.crypto.tink.hybrid.internal.EciesDemHelper.Dem
        public byte[] encrypt(byte[] demKeyValue, byte[] prefix, byte[] header, byte[] plaintext) throws GeneralSecurityException {
            return Bytes.concat(prefix, header, getAead(demKeyValue).encrypt(plaintext, EciesDemHelper.EMPTY_AAD));
        }

        @Override // com.google.crypto.tink.hybrid.internal.EciesDemHelper.Dem
        public byte[] decrypt(byte[] demKeyValue, byte[] ciphertext, int prefixAndHeaderSize) throws GeneralSecurityException {
            if (ciphertext.length < prefixAndHeaderSize) {
                throw new GeneralSecurityException("ciphertext too short");
            }
            return getAead(demKeyValue).decrypt(Arrays.copyOfRange(ciphertext, prefixAndHeaderSize, ciphertext.length), EciesDemHelper.EMPTY_AAD);
        }
    }

    private static final class AesSivDem implements Dem {
        private final int keySizeInBytes;
        private final AesSivParameters parameters;

        public AesSivDem(AesSivParameters parameters) {
            this.parameters = parameters;
            this.keySizeInBytes = parameters.getKeySizeBytes();
        }

        @Override // com.google.crypto.tink.hybrid.internal.EciesDemHelper.Dem
        public int getSymmetricKeySizeInBytes() {
            return this.keySizeInBytes;
        }

        private DeterministicAead getDaead(byte[] symmetricKeyValue) throws GeneralSecurityException {
            return AesSiv.create(AesSivKey.builder().setParameters(this.parameters).setKeyBytes(SecretBytes.copyFrom(symmetricKeyValue, InsecureSecretKeyAccess.get())).build());
        }

        @Override // com.google.crypto.tink.hybrid.internal.EciesDemHelper.Dem
        public byte[] encrypt(byte[] demKeyValue, byte[] prefix, byte[] header, byte[] plaintext) throws GeneralSecurityException {
            return Bytes.concat(prefix, header, getDaead(demKeyValue).encryptDeterministically(plaintext, EciesDemHelper.EMPTY_AAD));
        }

        @Override // com.google.crypto.tink.hybrid.internal.EciesDemHelper.Dem
        public byte[] decrypt(byte[] demKeyValue, byte[] ciphertext, int prefixAndHeaderSize) throws GeneralSecurityException {
            if (ciphertext.length < prefixAndHeaderSize) {
                throw new GeneralSecurityException("ciphertext too short");
            }
            return getDaead(demKeyValue).decryptDeterministically(Arrays.copyOfRange(ciphertext, prefixAndHeaderSize, ciphertext.length), EciesDemHelper.EMPTY_AAD);
        }
    }

    public static Dem getDem(EciesParameters parameters) throws GeneralSecurityException {
        Parameters demParameters = parameters.getDemParameters();
        if (demParameters instanceof AesGcmParameters) {
            return new AesGcmDem((AesGcmParameters) demParameters);
        }
        if (demParameters instanceof AesCtrHmacAeadParameters) {
            return new AesCtrHmacDem((AesCtrHmacAeadParameters) demParameters);
        }
        if (demParameters instanceof AesSivParameters) {
            return new AesSivDem((AesSivParameters) demParameters);
        }
        throw new GeneralSecurityException("Unsupported DEM parameters: " + demParameters);
    }

    private EciesDemHelper() {
    }
}
