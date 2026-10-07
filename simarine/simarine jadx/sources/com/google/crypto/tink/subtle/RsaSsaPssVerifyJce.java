package com.google.crypto.tink.subtle;

import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.EnumTypeProtoConverter;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.signature.RsaSsaPssParameters;
import com.google.crypto.tink.signature.RsaSsaPssPublicKey;
import com.google.crypto.tink.signature.internal.RsaSsaPssVerifyConscrypt;
import com.google.errorprone.annotations.Immutable;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchProviderException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class RsaSsaPssVerifyJce implements PublicKeyVerify {
    private final PublicKeyVerify verify;
    public static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_REQUIRES_BORINGCRYPTO;
    static final EnumTypeProtoConverter<Enums.HashType, RsaSsaPssParameters.HashType> HASH_TYPE_CONVERTER = EnumTypeProtoConverter.builder().add(Enums.HashType.SHA256, RsaSsaPssParameters.HashType.SHA256).add(Enums.HashType.SHA384, RsaSsaPssParameters.HashType.SHA384).add(Enums.HashType.SHA512, RsaSsaPssParameters.HashType.SHA512).build();
    private static final byte[] EMPTY = new byte[0];
    private static final byte[] LEGACY_MESSAGE_SUFFIX = {0};

    private static final class InternalImpl implements PublicKeyVerify {
        private final byte[] messageSuffix;
        private final Enums.HashType mgf1Hash;
        private final byte[] outputPrefix;
        private final RSAPublicKey publicKey;
        private final int saltLength;
        private final Enums.HashType sigHash;

        /* synthetic */ InternalImpl(RSAPublicKey rSAPublicKey, Enums.HashType hashType, Enums.HashType hashType2, int i, byte[] bArr, byte[] bArr2, AnonymousClass1 anonymousClass1) throws GeneralSecurityException {
            this(rSAPublicKey, hashType, hashType2, i, bArr, bArr2);
        }

        private InternalImpl(final RSAPublicKey pubKey, Enums.HashType sigHash, Enums.HashType mgf1Hash, int saltLength, byte[] outputPrefix, byte[] messageSuffix) throws GeneralSecurityException {
            if (TinkFipsUtil.useOnlyFips()) {
                throw new GeneralSecurityException("Can not use RSA PSS in FIPS-mode, as BoringCrypto module is not available.");
            }
            Validators.validateSignatureHash(sigHash);
            if (!sigHash.equals(mgf1Hash)) {
                throw new GeneralSecurityException("sigHash and mgf1Hash must be the same");
            }
            Validators.validateRsaModulusSize(pubKey.getModulus().bitLength());
            Validators.validateRsaPublicExponent(pubKey.getPublicExponent());
            this.publicKey = pubKey;
            this.sigHash = sigHash;
            this.mgf1Hash = mgf1Hash;
            this.saltLength = saltLength;
            this.outputPrefix = outputPrefix;
            this.messageSuffix = messageSuffix;
        }

        private void noPrefixVerify(final byte[] signature, final byte[] data) throws GeneralSecurityException {
            BigInteger publicExponent = this.publicKey.getPublicExponent();
            BigInteger modulus = this.publicKey.getModulus();
            int iBitLength = (modulus.bitLength() + 7) / 8;
            int iBitLength2 = (modulus.bitLength() + 6) / 8;
            if (iBitLength != signature.length) {
                throw new GeneralSecurityException("invalid signature's length");
            }
            BigInteger bigIntegerBytes2Integer = SubtleUtil.bytes2Integer(signature);
            if (bigIntegerBytes2Integer.compareTo(modulus) >= 0) {
                throw new GeneralSecurityException("signature out of range");
            }
            emsaPssVerify(data, SubtleUtil.integer2Bytes(bigIntegerBytes2Integer.modPow(publicExponent, modulus), iBitLength2), modulus.bitLength() - 1);
        }

        private void emsaPssVerify(byte[] message, byte[] em, int emBits) throws GeneralSecurityException {
            Validators.validateSignatureHash(this.sigHash);
            MessageDigest engineFactory = EngineFactory.MESSAGE_DIGEST.getInstance(SubtleUtil.toDigestAlgo(this.sigHash));
            engineFactory.update(message);
            byte[] bArr = this.messageSuffix;
            if (bArr.length != 0) {
                engineFactory.update(bArr);
            }
            byte[] bArrDigest = engineFactory.digest();
            int digestLength = engineFactory.getDigestLength();
            int length = em.length;
            if (length < this.saltLength + digestLength + 2) {
                throw new GeneralSecurityException("inconsistent");
            }
            if (em[em.length - 1] != -68) {
                throw new GeneralSecurityException("inconsistent");
            }
            int i = length - digestLength;
            int i2 = i - 1;
            byte[] bArrCopyOf = Arrays.copyOf(em, i2);
            byte[] bArrCopyOfRange = Arrays.copyOfRange(em, bArrCopyOf.length, bArrCopyOf.length + digestLength);
            int i3 = 0;
            while (true) {
                int i4 = i2;
                MessageDigest messageDigest = engineFactory;
                byte[] bArr2 = bArrDigest;
                long j = (((long) length) * 8) - ((long) emBits);
                if (i3 < j) {
                    if (((bArrCopyOf[i3 / 8] >> (7 - (i3 % 8))) & 1) != 0) {
                        throw new GeneralSecurityException("inconsistent");
                    }
                    i3++;
                    i2 = i4;
                    engineFactory = messageDigest;
                    bArrDigest = bArr2;
                } else {
                    byte[] bArrMgf1 = SubtleUtil.mgf1(bArrCopyOfRange, i4, this.mgf1Hash);
                    int length2 = bArrMgf1.length;
                    byte[] bArr3 = new byte[length2];
                    for (int i5 = 0; i5 < length2; i5++) {
                        bArr3[i5] = (byte) (bArrMgf1[i5] ^ bArrCopyOf[i5]);
                    }
                    for (int i6 = 0; i6 <= j; i6++) {
                        int i7 = i6 / 8;
                        bArr3[i7] = (byte) ((~(1 << (7 - (i6 % 8)))) & bArr3[i7]);
                    }
                    int i8 = 0;
                    while (true) {
                        int i9 = this.saltLength;
                        if (i8 < (i - i9) - 2) {
                            if (bArr3[i8] != 0) {
                                throw new GeneralSecurityException("inconsistent");
                            }
                            i8++;
                        } else {
                            if (bArr3[(i - i9) - 2] != 1) {
                                throw new GeneralSecurityException("inconsistent");
                            }
                            byte[] bArrCopyOfRange2 = Arrays.copyOfRange(bArr3, length2 - i9, length2);
                            int i10 = digestLength + 8;
                            byte[] bArr4 = new byte[this.saltLength + i10];
                            System.arraycopy(bArr2, 0, bArr4, 8, bArr2.length);
                            System.arraycopy(bArrCopyOfRange2, 0, bArr4, i10, bArrCopyOfRange2.length);
                            if (!Bytes.equal(messageDigest.digest(bArr4), bArrCopyOfRange)) {
                                throw new GeneralSecurityException("inconsistent");
                            }
                            return;
                        }
                    }
                }
            }
        }

        @Override // com.google.crypto.tink.PublicKeyVerify
        public void verify(final byte[] signature, final byte[] data) throws GeneralSecurityException {
            byte[] bArr = this.outputPrefix;
            if (bArr.length == 0) {
                noPrefixVerify(signature, data);
            } else {
                if (!Util.isPrefix(bArr, signature)) {
                    throw new GeneralSecurityException("Invalid signature (output prefix mismatch)");
                }
                noPrefixVerify(Arrays.copyOfRange(signature, this.outputPrefix.length, signature.length), data);
            }
        }
    }

    public static PublicKeyVerify create(RsaSsaPssPublicKey key) throws GeneralSecurityException {
        byte[] bArr;
        try {
            return RsaSsaPssVerifyConscrypt.create(key);
        } catch (NoSuchProviderException unused) {
            RSAPublicKey rSAPublicKey = (RSAPublicKey) EngineFactory.KEY_FACTORY.getInstance("RSA").generatePublic(new RSAPublicKeySpec(key.getModulus(), key.getParameters().getPublicExponent()));
            RsaSsaPssParameters parameters = key.getParameters();
            Enums.HashType hashType = (Enums.HashType) HASH_TYPE_CONVERTER.toProtoEnum(parameters.getSigHashType());
            Enums.HashType hashType2 = (Enums.HashType) HASH_TYPE_CONVERTER.toProtoEnum(parameters.getMgf1HashType());
            int saltLengthBytes = parameters.getSaltLengthBytes();
            byte[] byteArray = key.getOutputPrefix().toByteArray();
            if (key.getParameters().getVariant().equals(RsaSsaPssParameters.Variant.LEGACY)) {
                bArr = LEGACY_MESSAGE_SUFFIX;
            } else {
                bArr = EMPTY;
            }
            return new InternalImpl(rSAPublicKey, hashType, hashType2, saltLengthBytes, byteArray, bArr, null);
        }
    }

    /* JADX INFO: renamed from: com.google.crypto.tink.subtle.RsaSsaPssVerifyJce$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$google$crypto$tink$subtle$Enums$HashType;

        static {
            int[] iArr = new int[Enums.HashType.values().length];
            $SwitchMap$com$google$crypto$tink$subtle$Enums$HashType = iArr;
            try {
                iArr[Enums.HashType.SHA256.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$subtle$Enums$HashType[Enums.HashType.SHA384.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$subtle$Enums$HashType[Enums.HashType.SHA512.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    private static RsaSsaPssParameters.HashType getHashType(Enums.HashType hash) throws GeneralSecurityException {
        int i = AnonymousClass1.$SwitchMap$com$google$crypto$tink$subtle$Enums$HashType[hash.ordinal()];
        if (i == 1) {
            return RsaSsaPssParameters.HashType.SHA256;
        }
        if (i == 2) {
            return RsaSsaPssParameters.HashType.SHA384;
        }
        if (i == 3) {
            return RsaSsaPssParameters.HashType.SHA512;
        }
        throw new GeneralSecurityException("Unsupported hash: " + hash);
    }

    private RsaSsaPssPublicKey convertKey(final RSAPublicKey pubKey, Enums.HashType sigHash, Enums.HashType mgf1Hash, int saltLength) throws GeneralSecurityException {
        return RsaSsaPssPublicKey.builder().setParameters(RsaSsaPssParameters.builder().setModulusSizeBits(pubKey.getModulus().bitLength()).setPublicExponent(pubKey.getPublicExponent()).setSigHashType(getHashType(sigHash)).setMgf1HashType(getHashType(mgf1Hash)).setSaltLengthBytes(saltLength).setVariant(RsaSsaPssParameters.Variant.NO_PREFIX).build()).setModulus(pubKey.getModulus()).build();
    }

    public RsaSsaPssVerifyJce(final RSAPublicKey pubKey, Enums.HashType sigHash, Enums.HashType mgf1Hash, int saltLength) throws GeneralSecurityException {
        this.verify = create(convertKey(pubKey, sigHash, mgf1Hash, saltLength));
    }

    @Override // com.google.crypto.tink.PublicKeyVerify
    public void verify(final byte[] signature, final byte[] data) throws GeneralSecurityException {
        this.verify.verify(signature, data);
    }
}
