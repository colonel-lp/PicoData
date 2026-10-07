package com.google.crypto.tink.subtle;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.PublicKeySign;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.signature.RsaSsaPssParameters;
import com.google.crypto.tink.signature.RsaSsaPssPrivateKey;
import com.google.crypto.tink.signature.RsaSsaPssPublicKey;
import com.google.crypto.tink.signature.internal.RsaSsaPssSignConscrypt;
import com.google.crypto.tink.util.SecretBigInteger;
import com.google.errorprone.annotations.Immutable;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchProviderException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class RsaSsaPssSignJce implements PublicKeySign {
    private final PublicKeySign sign;
    public static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_REQUIRES_BORINGCRYPTO;
    private static final byte[] EMPTY = new byte[0];
    private static final byte[] LEGACY_MESSAGE_SUFFIX = {0};

    private static final class InternalImpl implements PublicKeySign {
        private static final String RAW_RSA_ALGORITHM = "RSA/ECB/NOPADDING";
        private final byte[] messageSuffix;
        private final Enums.HashType mgf1Hash;
        private final byte[] outputPrefix;
        private final RSAPrivateCrtKey privateKey;
        private final RSAPublicKey publicKey;
        private final int saltLength;
        private final Enums.HashType sigHash;

        /* synthetic */ InternalImpl(RSAPrivateCrtKey rSAPrivateCrtKey, Enums.HashType hashType, Enums.HashType hashType2, int i, byte[] bArr, byte[] bArr2, AnonymousClass1 anonymousClass1) throws GeneralSecurityException {
            this(rSAPrivateCrtKey, hashType, hashType2, i, bArr, bArr2);
        }

        private InternalImpl(final RSAPrivateCrtKey priv, Enums.HashType sigHash, Enums.HashType mgf1Hash, int saltLength, byte[] outputPrefix, byte[] messageSuffix) throws GeneralSecurityException {
            if (TinkFipsUtil.useOnlyFips()) {
                throw new GeneralSecurityException("Can not use RSA PSS in FIPS-mode, as BoringCrypto module is not available.");
            }
            Validators.validateSignatureHash(sigHash);
            if (!sigHash.equals(mgf1Hash)) {
                throw new GeneralSecurityException("sigHash and mgf1Hash must be the same");
            }
            Validators.validateRsaModulusSize(priv.getModulus().bitLength());
            Validators.validateRsaPublicExponent(priv.getPublicExponent());
            this.privateKey = priv;
            this.publicKey = (RSAPublicKey) EngineFactory.KEY_FACTORY.getInstance("RSA").generatePublic(new RSAPublicKeySpec(priv.getModulus(), priv.getPublicExponent()));
            this.sigHash = sigHash;
            this.mgf1Hash = mgf1Hash;
            this.saltLength = saltLength;
            this.outputPrefix = outputPrefix;
            this.messageSuffix = messageSuffix;
        }

        private byte[] noPrefixSign(final byte[] data) throws GeneralSecurityException {
            return rsasp1(emsaPssEncode(data, this.publicKey.getModulus().bitLength() - 1));
        }

        @Override // com.google.crypto.tink.PublicKeySign
        public byte[] sign(final byte[] data) throws GeneralSecurityException {
            byte[] bArrNoPrefixSign = noPrefixSign(data);
            byte[] bArr = this.outputPrefix;
            return bArr.length == 0 ? bArrNoPrefixSign : Bytes.concat(bArr, bArrNoPrefixSign);
        }

        private byte[] rsasp1(byte[] m) throws GeneralSecurityException {
            Cipher engineFactory = EngineFactory.CIPHER.getInstance(RAW_RSA_ALGORITHM);
            engineFactory.init(2, this.privateKey);
            byte[] bArrDoFinal = engineFactory.doFinal(m);
            Cipher engineFactory2 = EngineFactory.CIPHER.getInstance(RAW_RSA_ALGORITHM);
            engineFactory2.init(1, this.publicKey);
            if (new BigInteger(1, m).equals(new BigInteger(1, engineFactory2.doFinal(bArrDoFinal)))) {
                return bArrDoFinal;
            }
            throw new IllegalStateException("Security bug: RSA signature computation error");
        }

        private byte[] emsaPssEncode(byte[] message, int emBits) throws GeneralSecurityException {
            Validators.validateSignatureHash(this.sigHash);
            MessageDigest engineFactory = EngineFactory.MESSAGE_DIGEST.getInstance(SubtleUtil.toDigestAlgo(this.sigHash));
            engineFactory.update(message);
            byte[] bArr = this.messageSuffix;
            if (bArr.length != 0) {
                engineFactory.update(bArr);
            }
            byte[] bArrDigest = engineFactory.digest();
            int digestLength = engineFactory.getDigestLength();
            int i = ((emBits - 1) / 8) + 1;
            int i2 = this.saltLength;
            if (i < digestLength + i2 + 2) {
                throw new GeneralSecurityException("encoding error");
            }
            byte[] bArrRandBytes = Random.randBytes(i2);
            int i3 = digestLength + 8;
            byte[] bArr2 = new byte[this.saltLength + i3];
            System.arraycopy(bArrDigest, 0, bArr2, 8, digestLength);
            System.arraycopy(bArrRandBytes, 0, bArr2, i3, bArrRandBytes.length);
            byte[] bArrDigest2 = engineFactory.digest(bArr2);
            int i4 = (i - digestLength) - 1;
            byte[] bArr3 = new byte[i4];
            int i5 = this.saltLength;
            bArr3[((i - i5) - digestLength) - 2] = 1;
            System.arraycopy(bArrRandBytes, 0, bArr3, ((i - i5) - digestLength) - 1, bArrRandBytes.length);
            byte[] bArrMgf1 = SubtleUtil.mgf1(bArrDigest2, i4, this.mgf1Hash);
            byte[] bArr4 = new byte[i4];
            for (int i6 = 0; i6 < i4; i6++) {
                bArr4[i6] = (byte) (bArr3[i6] ^ bArrMgf1[i6]);
            }
            for (int i7 = 0; i7 < (((long) i) * 8) - ((long) emBits); i7++) {
                int i8 = i7 / 8;
                bArr4[i8] = (byte) ((~(1 << (7 - (i7 % 8)))) & bArr4[i8]);
            }
            int i9 = digestLength + i4;
            byte[] bArr5 = new byte[i9 + 1];
            System.arraycopy(bArr4, 0, bArr5, 0, i4);
            System.arraycopy(bArrDigest2, 0, bArr5, i4, bArrDigest2.length);
            bArr5[i9] = -68;
            return bArr5;
        }
    }

    public static PublicKeySign create(RsaSsaPssPrivateKey key) throws GeneralSecurityException {
        byte[] bArr;
        try {
            return RsaSsaPssSignConscrypt.create(key);
        } catch (NoSuchProviderException unused) {
            RSAPrivateCrtKey rSAPrivateCrtKey = (RSAPrivateCrtKey) EngineFactory.KEY_FACTORY.getInstance("RSA").generatePrivate(new RSAPrivateCrtKeySpec(key.getPublicKey().getModulus(), key.getParameters().getPublicExponent(), key.getPrivateExponent().getBigInteger(InsecureSecretKeyAccess.get()), key.getPrimeP().getBigInteger(InsecureSecretKeyAccess.get()), key.getPrimeQ().getBigInteger(InsecureSecretKeyAccess.get()), key.getPrimeExponentP().getBigInteger(InsecureSecretKeyAccess.get()), key.getPrimeExponentQ().getBigInteger(InsecureSecretKeyAccess.get()), key.getCrtCoefficient().getBigInteger(InsecureSecretKeyAccess.get())));
            RsaSsaPssParameters parameters = key.getParameters();
            Enums.HashType hashType = (Enums.HashType) RsaSsaPssVerifyJce.HASH_TYPE_CONVERTER.toProtoEnum(parameters.getSigHashType());
            Enums.HashType hashType2 = (Enums.HashType) RsaSsaPssVerifyJce.HASH_TYPE_CONVERTER.toProtoEnum(parameters.getMgf1HashType());
            int saltLengthBytes = parameters.getSaltLengthBytes();
            byte[] byteArray = key.getOutputPrefix().toByteArray();
            if (key.getParameters().getVariant().equals(RsaSsaPssParameters.Variant.LEGACY)) {
                bArr = LEGACY_MESSAGE_SUFFIX;
            } else {
                bArr = EMPTY;
            }
            return new InternalImpl(rSAPrivateCrtKey, hashType, hashType2, saltLengthBytes, byteArray, bArr, null);
        }
    }

    /* JADX INFO: renamed from: com.google.crypto.tink.subtle.RsaSsaPssSignJce$1, reason: invalid class name */
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

    private RsaSsaPssPrivateKey convertKey(final RSAPrivateCrtKey key, Enums.HashType sigHash, Enums.HashType mgf1Hash, int saltLength) throws GeneralSecurityException {
        return RsaSsaPssPrivateKey.builder().setPublicKey(RsaSsaPssPublicKey.builder().setParameters(RsaSsaPssParameters.builder().setModulusSizeBits(key.getModulus().bitLength()).setPublicExponent(key.getPublicExponent()).setSigHashType(getHashType(sigHash)).setMgf1HashType(getHashType(mgf1Hash)).setSaltLengthBytes(saltLength).setVariant(RsaSsaPssParameters.Variant.NO_PREFIX).build()).setModulus(key.getModulus()).build()).setPrimes(SecretBigInteger.fromBigInteger(key.getPrimeP(), InsecureSecretKeyAccess.get()), SecretBigInteger.fromBigInteger(key.getPrimeQ(), InsecureSecretKeyAccess.get())).setPrivateExponent(SecretBigInteger.fromBigInteger(key.getPrivateExponent(), InsecureSecretKeyAccess.get())).setPrimeExponents(SecretBigInteger.fromBigInteger(key.getPrimeExponentP(), InsecureSecretKeyAccess.get()), SecretBigInteger.fromBigInteger(key.getPrimeExponentQ(), InsecureSecretKeyAccess.get())).setCrtCoefficient(SecretBigInteger.fromBigInteger(key.getCrtCoefficient(), InsecureSecretKeyAccess.get())).build();
    }

    public RsaSsaPssSignJce(final RSAPrivateCrtKey priv, Enums.HashType sigHash, Enums.HashType mgf1Hash, int saltLength) throws GeneralSecurityException {
        this.sign = create(convertKey(priv, sigHash, mgf1Hash, saltLength));
    }

    @Override // com.google.crypto.tink.PublicKeySign
    public byte[] sign(final byte[] data) throws GeneralSecurityException {
        return this.sign.sign(data);
    }
}
