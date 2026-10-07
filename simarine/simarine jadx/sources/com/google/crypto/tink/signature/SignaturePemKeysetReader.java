package com.google.crypto.tink.signature;

import com.google.crypto.tink.KeysetReader;
import com.google.crypto.tink.PemKeyType;
import com.google.crypto.tink.proto.EcdsaParams;
import com.google.crypto.tink.proto.EcdsaSignatureEncoding;
import com.google.crypto.tink.proto.EllipticCurveType;
import com.google.crypto.tink.proto.EncryptedKeyset;
import com.google.crypto.tink.proto.HashType;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.proto.KeyStatusType;
import com.google.crypto.tink.proto.Keyset;
import com.google.crypto.tink.proto.OutputPrefixType;
import com.google.crypto.tink.proto.RsaSsaPkcs1Params;
import com.google.crypto.tink.proto.RsaSsaPssParams;
import com.google.crypto.tink.signature.internal.SigUtil;
import com.google.crypto.tink.subtle.Enums;
import com.google.crypto.tink.subtle.Random;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.Key;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class SignaturePemKeysetReader implements KeysetReader {
    private List<PemKey> pemKeys;

    SignaturePemKeysetReader(List<PemKey> pemKeys) {
        this.pemKeys = pemKeys;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private List<PemKey> pemKeys = new ArrayList();

        Builder() {
        }

        public KeysetReader build() {
            return new SignaturePemKeysetReader(this.pemKeys);
        }

        public Builder addPem(String pem, PemKeyType keyType) {
            PemKey pemKey = new PemKey(null);
            pemKey.reader = new BufferedReader(new StringReader(pem));
            pemKey.type = keyType;
            this.pemKeys.add(pemKey);
            return this;
        }
    }

    private static final class PemKey {
        BufferedReader reader;
        PemKeyType type;

        private PemKey() {
        }

        /* synthetic */ PemKey(AnonymousClass1 anonymousClass1) {
            this();
        }
    }

    @Override // com.google.crypto.tink.KeysetReader
    public Keyset read() throws IOException {
        Keyset.Builder builderNewBuilder = Keyset.newBuilder();
        for (PemKey pemKey : this.pemKeys) {
            for (Keyset.Key key = readKey(pemKey.reader, pemKey.type); key != null; key = readKey(pemKey.reader, pemKey.type)) {
                builderNewBuilder.addKey(key);
            }
        }
        if (builderNewBuilder.getKeyCount() == 0) {
            throw new IOException("cannot find any key");
        }
        builderNewBuilder.setPrimaryKeyId(builderNewBuilder.getKey(0).getKeyId());
        return (Keyset) builderNewBuilder.build();
    }

    @Override // com.google.crypto.tink.KeysetReader
    public EncryptedKeyset readEncrypted() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Nullable
    private static Keyset.Key readKey(BufferedReader reader, PemKeyType pemKeyType) throws IOException {
        KeyData keyDataConvertEcPublicKey;
        Key key = pemKeyType.readKey(reader);
        if (key == null) {
            return null;
        }
        if (key instanceof RSAPublicKey) {
            keyDataConvertEcPublicKey = convertRsaPublicKey(pemKeyType, (RSAPublicKey) key);
        } else {
            if (!(key instanceof ECPublicKey)) {
                return null;
            }
            keyDataConvertEcPublicKey = convertEcPublicKey(pemKeyType, (ECPublicKey) key);
        }
        return (Keyset.Key) Keyset.Key.newBuilder().setKeyData(keyDataConvertEcPublicKey).setStatus(KeyStatusType.ENABLED).setOutputPrefixType(OutputPrefixType.RAW).setKeyId(Random.randInt()).build();
    }

    private static KeyData convertRsaPublicKey(PemKeyType pemKeyType, RSAPublicKey key) throws IOException {
        if (pemKeyType.algorithm.equals("RSASSA-PKCS1-v1_5")) {
            return (KeyData) KeyData.newBuilder().setTypeUrl(RsaSsaPkcs1VerifyKeyManager.getKeyType()).setValue(((com.google.crypto.tink.proto.RsaSsaPkcs1PublicKey) com.google.crypto.tink.proto.RsaSsaPkcs1PublicKey.newBuilder().setVersion(0).setParams((RsaSsaPkcs1Params) RsaSsaPkcs1Params.newBuilder().setHashType(getHashType(pemKeyType)).build()).setE(SigUtil.toUnsignedIntByteString(key.getPublicExponent())).setN(SigUtil.toUnsignedIntByteString(key.getModulus())).build()).toByteString()).setKeyMaterialType(KeyData.KeyMaterialType.ASYMMETRIC_PUBLIC).build();
        }
        if (pemKeyType.algorithm.equals("RSASSA-PSS")) {
            return (KeyData) KeyData.newBuilder().setTypeUrl(RsaSsaPssVerifyKeyManager.getKeyType()).setValue(((com.google.crypto.tink.proto.RsaSsaPssPublicKey) com.google.crypto.tink.proto.RsaSsaPssPublicKey.newBuilder().setVersion(0).setParams((RsaSsaPssParams) RsaSsaPssParams.newBuilder().setSigHash(getHashType(pemKeyType)).setMgf1Hash(getHashType(pemKeyType)).setSaltLength(getDigestSizeInBytes(pemKeyType)).build()).setE(SigUtil.toUnsignedIntByteString(key.getPublicExponent())).setN(SigUtil.toUnsignedIntByteString(key.getModulus())).build()).toByteString()).setKeyMaterialType(KeyData.KeyMaterialType.ASYMMETRIC_PUBLIC).build();
        }
        throw new IOException("unsupported RSA signature algorithm: " + pemKeyType.algorithm);
    }

    private static KeyData convertEcPublicKey(PemKeyType pemKeyType, ECPublicKey key) throws IOException {
        if (pemKeyType.algorithm.equals("ECDSA")) {
            return (KeyData) KeyData.newBuilder().setTypeUrl(EcdsaVerifyKeyManager.getKeyType()).setValue(((com.google.crypto.tink.proto.EcdsaPublicKey) com.google.crypto.tink.proto.EcdsaPublicKey.newBuilder().setVersion(0).setParams((EcdsaParams) EcdsaParams.newBuilder().setHashType(getHashType(pemKeyType)).setCurve(getCurveType(pemKeyType)).setEncoding(EcdsaSignatureEncoding.DER).build()).setX(SigUtil.toUnsignedIntByteString(key.getW().getAffineX())).setY(SigUtil.toUnsignedIntByteString(key.getW().getAffineY())).build()).toByteString()).setKeyMaterialType(KeyData.KeyMaterialType.ASYMMETRIC_PUBLIC).build();
        }
        throw new IOException("unsupported EC signature algorithm: " + pemKeyType.algorithm);
    }

    /* JADX INFO: renamed from: com.google.crypto.tink.signature.SignaturePemKeysetReader$1, reason: invalid class name */
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

    private static HashType getHashType(PemKeyType pemKeyType) {
        int i = AnonymousClass1.$SwitchMap$com$google$crypto$tink$subtle$Enums$HashType[pemKeyType.hash.ordinal()];
        if (i == 1) {
            return HashType.SHA256;
        }
        if (i == 2) {
            return HashType.SHA384;
        }
        if (i == 3) {
            return HashType.SHA512;
        }
        throw new IllegalArgumentException("unsupported hash type: " + pemKeyType.hash.name());
    }

    private static int getDigestSizeInBytes(PemKeyType pemKeyType) {
        int i = AnonymousClass1.$SwitchMap$com$google$crypto$tink$subtle$Enums$HashType[pemKeyType.hash.ordinal()];
        if (i == 1) {
            return 32;
        }
        if (i == 2) {
            return 48;
        }
        if (i == 3) {
            return 64;
        }
        throw new IllegalArgumentException("unsupported hash type: " + pemKeyType.hash.name());
    }

    private static EllipticCurveType getCurveType(PemKeyType pemKeyType) {
        int i = pemKeyType.keySizeInBits;
        if (i == 256) {
            return EllipticCurveType.NIST_P256;
        }
        if (i == 384) {
            return EllipticCurveType.NIST_P384;
        }
        if (i == 521) {
            return EllipticCurveType.NIST_P521;
        }
        throw new IllegalArgumentException("unsupported curve for key size: " + pemKeyType.keySizeInBits);
    }
}
