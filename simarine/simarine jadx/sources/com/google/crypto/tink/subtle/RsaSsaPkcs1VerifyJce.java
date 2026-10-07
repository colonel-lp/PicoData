package com.google.crypto.tink.subtle;

import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.EnumTypeProtoConverter;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.signature.RsaSsaPkcs1Parameters;
import com.google.crypto.tink.signature.RsaSsaPkcs1PublicKey;
import com.google.crypto.tink.signature.internal.RsaSsaPkcs1VerifyConscrypt;
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
public final class RsaSsaPkcs1VerifyJce implements PublicKeyVerify {
    private final PublicKeyVerify verify;
    public static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_REQUIRES_BORINGCRYPTO;
    private static final byte[] EMPTY = new byte[0];
    private static final byte[] LEGACY_MESSAGE_SUFFIX = {0};
    static final EnumTypeProtoConverter<Enums.HashType, RsaSsaPkcs1Parameters.HashType> HASH_TYPE_CONVERTER = EnumTypeProtoConverter.builder().add(Enums.HashType.SHA256, RsaSsaPkcs1Parameters.HashType.SHA256).add(Enums.HashType.SHA384, RsaSsaPkcs1Parameters.HashType.SHA384).add(Enums.HashType.SHA512, RsaSsaPkcs1Parameters.HashType.SHA512).build();

    private static final class InternalJavaImpl implements PublicKeyVerify {
        private static final String ASN_PREFIX_SHA256 = "3031300d060960864801650304020105000420";
        private static final String ASN_PREFIX_SHA384 = "3041300d060960864801650304020205000430";
        private static final String ASN_PREFIX_SHA512 = "3051300d060960864801650304020305000440";
        private final Enums.HashType hash;
        private final byte[] messageSuffix;
        private final byte[] outputPrefix;
        private final RSAPublicKey publicKey;

        /* synthetic */ InternalJavaImpl(RSAPublicKey rSAPublicKey, Enums.HashType hashType, byte[] bArr, byte[] bArr2, AnonymousClass1 anonymousClass1) throws GeneralSecurityException {
            this(rSAPublicKey, hashType, bArr, bArr2);
        }

        private InternalJavaImpl(final RSAPublicKey pubKey, Enums.HashType hash, byte[] outputPrefix, byte[] messageSuffix) throws GeneralSecurityException {
            if (TinkFipsUtil.useOnlyFips()) {
                throw new GeneralSecurityException("Conscrypt is not available, and we cannot use Java Implementation of RSA-PKCS1.5 in FIPS-mode.");
            }
            Validators.validateSignatureHash(hash);
            Validators.validateRsaModulusSize(pubKey.getModulus().bitLength());
            Validators.validateRsaPublicExponent(pubKey.getPublicExponent());
            this.publicKey = pubKey;
            this.hash = hash;
            this.outputPrefix = outputPrefix;
            this.messageSuffix = messageSuffix;
        }

        private void noPrefixVerify(final byte[] signature, final byte[] data) throws GeneralSecurityException {
            BigInteger publicExponent = this.publicKey.getPublicExponent();
            BigInteger modulus = this.publicKey.getModulus();
            int iBitLength = (modulus.bitLength() + 7) / 8;
            if (iBitLength != signature.length) {
                throw new GeneralSecurityException("invalid signature's length");
            }
            BigInteger bigIntegerBytes2Integer = SubtleUtil.bytes2Integer(signature);
            if (bigIntegerBytes2Integer.compareTo(modulus) >= 0) {
                throw new GeneralSecurityException("signature out of range");
            }
            if (!Bytes.equal(SubtleUtil.integer2Bytes(bigIntegerBytes2Integer.modPow(publicExponent, modulus), iBitLength), emsaPkcs1(data, iBitLength, this.hash))) {
                throw new GeneralSecurityException("invalid signature");
            }
        }

        private byte[] emsaPkcs1(byte[] m, int emLen, Enums.HashType hash) throws GeneralSecurityException {
            Validators.validateSignatureHash(hash);
            MessageDigest engineFactory = EngineFactory.MESSAGE_DIGEST.getInstance(SubtleUtil.toDigestAlgo(this.hash));
            engineFactory.update(m);
            byte[] bArr = this.messageSuffix;
            if (bArr.length != 0) {
                engineFactory.update(bArr);
            }
            byte[] bArrDigest = engineFactory.digest();
            byte[] asnPrefix = toAsnPrefix(hash);
            int length = asnPrefix.length + bArrDigest.length;
            if (emLen < length + 11) {
                throw new GeneralSecurityException("intended encoded message length too short");
            }
            byte[] bArr2 = new byte[emLen];
            bArr2[0] = 0;
            bArr2[1] = 1;
            int i = 2;
            int i2 = 0;
            while (i2 < (emLen - length) - 3) {
                bArr2[i] = -1;
                i2++;
                i++;
            }
            int i3 = i + 1;
            bArr2[i] = 0;
            System.arraycopy(asnPrefix, 0, bArr2, i3, asnPrefix.length);
            System.arraycopy(bArrDigest, 0, bArr2, i3 + asnPrefix.length, bArrDigest.length);
            return bArr2;
        }

        private byte[] toAsnPrefix(Enums.HashType hash) throws GeneralSecurityException {
            int i = AnonymousClass1.$SwitchMap$com$google$crypto$tink$subtle$Enums$HashType[hash.ordinal()];
            if (i == 1) {
                return Hex.decode(ASN_PREFIX_SHA256);
            }
            if (i == 2) {
                return Hex.decode(ASN_PREFIX_SHA384);
            }
            if (i == 3) {
                return Hex.decode(ASN_PREFIX_SHA512);
            }
            throw new GeneralSecurityException("Unsupported hash " + hash);
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

    /* JADX INFO: renamed from: com.google.crypto.tink.subtle.RsaSsaPkcs1VerifyJce$1, reason: invalid class name */
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

    public static PublicKeyVerify create(RsaSsaPkcs1PublicKey key) throws GeneralSecurityException {
        byte[] bArr;
        try {
            return RsaSsaPkcs1VerifyConscrypt.create(key);
        } catch (NoSuchProviderException unused) {
            RSAPublicKey rSAPublicKey = (RSAPublicKey) EngineFactory.KEY_FACTORY.getInstance("RSA").generatePublic(new RSAPublicKeySpec(key.getModulus(), key.getParameters().getPublicExponent()));
            Enums.HashType hashType = (Enums.HashType) HASH_TYPE_CONVERTER.toProtoEnum(key.getParameters().getHashType());
            byte[] byteArray = key.getOutputPrefix().toByteArray();
            if (key.getParameters().getVariant().equals(RsaSsaPkcs1Parameters.Variant.LEGACY)) {
                bArr = LEGACY_MESSAGE_SUFFIX;
            } else {
                bArr = EMPTY;
            }
            return new InternalJavaImpl(rSAPublicKey, hashType, byteArray, bArr, null);
        }
    }

    private static RsaSsaPkcs1Parameters.HashType getHashType(Enums.HashType hash) throws GeneralSecurityException {
        int i = AnonymousClass1.$SwitchMap$com$google$crypto$tink$subtle$Enums$HashType[hash.ordinal()];
        if (i == 1) {
            return RsaSsaPkcs1Parameters.HashType.SHA256;
        }
        if (i == 2) {
            return RsaSsaPkcs1Parameters.HashType.SHA384;
        }
        if (i == 3) {
            return RsaSsaPkcs1Parameters.HashType.SHA512;
        }
        throw new GeneralSecurityException("Unsupported hash: " + hash);
    }

    private RsaSsaPkcs1PublicKey convertKey(final RSAPublicKey pubKey, Enums.HashType hash) throws GeneralSecurityException {
        return RsaSsaPkcs1PublicKey.builder().setParameters(RsaSsaPkcs1Parameters.builder().setModulusSizeBits(pubKey.getModulus().bitLength()).setPublicExponent(pubKey.getPublicExponent()).setHashType(getHashType(hash)).setVariant(RsaSsaPkcs1Parameters.Variant.NO_PREFIX).build()).setModulus(pubKey.getModulus()).build();
    }

    public RsaSsaPkcs1VerifyJce(final RSAPublicKey pubKey, Enums.HashType hash) throws GeneralSecurityException {
        this.verify = create(convertKey(pubKey, hash));
    }

    @Override // com.google.crypto.tink.PublicKeyVerify
    public void verify(final byte[] signature, final byte[] data) throws GeneralSecurityException {
        this.verify.verify(signature, data);
    }
}
