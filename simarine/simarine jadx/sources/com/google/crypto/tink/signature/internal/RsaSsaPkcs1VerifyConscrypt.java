package com.google.crypto.tink.signature.internal;

import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.ConscryptUtil;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.signature.RsaSsaPkcs1Parameters;
import com.google.crypto.tink.signature.RsaSsaPkcs1PublicKey;
import com.google.crypto.tink.subtle.Validators;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class RsaSsaPkcs1VerifyConscrypt implements PublicKeyVerify {
    private final Provider conscrypt;
    private final byte[] messageSuffix;
    private final byte[] outputPrefix;
    private final RSAPublicKey publicKey;
    private final String signatureAlgorithm;
    public static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_REQUIRES_BORINGCRYPTO;
    private static final byte[] EMPTY = new byte[0];
    private static final byte[] LEGACY_MESSAGE_SUFFIX = {0};

    @Nullable
    private static Provider conscryptProviderOrNull() {
        if (!Util.isAndroid() || Util.getAndroidApiLevel().intValue() > 21) {
            return ConscryptUtil.providerOrNull();
        }
        return null;
    }

    public static String toRsaSsaPkcs1Algo(RsaSsaPkcs1Parameters.HashType hashType) throws GeneralSecurityException {
        if (hashType == RsaSsaPkcs1Parameters.HashType.SHA256) {
            return "SHA256withRSA";
        }
        if (hashType == RsaSsaPkcs1Parameters.HashType.SHA384) {
            return "SHA384withRSA";
        }
        if (hashType == RsaSsaPkcs1Parameters.HashType.SHA512) {
            return "SHA512withRSA";
        }
        throw new GeneralSecurityException("unknown hash type");
    }

    public static PublicKeyVerify create(RsaSsaPkcs1PublicKey key) throws GeneralSecurityException {
        byte[] bArr;
        Provider providerConscryptProviderOrNull = conscryptProviderOrNull();
        if (providerConscryptProviderOrNull == null) {
            throw new NoSuchProviderException("RSA-PKCS1.5 using Conscrypt is not supported.");
        }
        RSAPublicKey rSAPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA", providerConscryptProviderOrNull).generatePublic(new RSAPublicKeySpec(key.getModulus(), key.getParameters().getPublicExponent()));
        RsaSsaPkcs1Parameters.HashType hashType = key.getParameters().getHashType();
        byte[] byteArray = key.getOutputPrefix().toByteArray();
        if (key.getParameters().getVariant().equals(RsaSsaPkcs1Parameters.Variant.LEGACY)) {
            bArr = LEGACY_MESSAGE_SUFFIX;
        } else {
            bArr = EMPTY;
        }
        return new RsaSsaPkcs1VerifyConscrypt(rSAPublicKey, hashType, byteArray, bArr, providerConscryptProviderOrNull);
    }

    private RsaSsaPkcs1VerifyConscrypt(final RSAPublicKey pubKey, RsaSsaPkcs1Parameters.HashType hashType, byte[] outputPrefix, byte[] messageSuffix, Provider conscrypt) throws GeneralSecurityException {
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Can not use RSA-PKCS1.5 in FIPS-mode, as BoringCrypto module is not available.");
        }
        Validators.validateRsaModulusSize(pubKey.getModulus().bitLength());
        Validators.validateRsaPublicExponent(pubKey.getPublicExponent());
        this.publicKey = pubKey;
        this.signatureAlgorithm = toRsaSsaPkcs1Algo(hashType);
        this.outputPrefix = outputPrefix;
        this.messageSuffix = messageSuffix;
        this.conscrypt = conscrypt;
    }

    @Override // com.google.crypto.tink.PublicKeyVerify
    public void verify(final byte[] signature, final byte[] data) throws GeneralSecurityException {
        boolean zVerify;
        if (!Util.isPrefix(this.outputPrefix, signature)) {
            throw new GeneralSecurityException("Invalid signature (output prefix mismatch)");
        }
        Signature signature2 = Signature.getInstance(this.signatureAlgorithm, this.conscrypt);
        signature2.initVerify(this.publicKey);
        signature2.update(data);
        byte[] bArr = this.messageSuffix;
        if (bArr.length > 0) {
            signature2.update(bArr);
        }
        try {
            zVerify = signature2.verify(Arrays.copyOfRange(signature, this.outputPrefix.length, signature.length));
        } catch (RuntimeException unused) {
            zVerify = false;
        }
        if (!zVerify) {
            throw new GeneralSecurityException("Invalid signature");
        }
    }
}
