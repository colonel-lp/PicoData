package com.google.crypto.tink.signature.internal;

import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.ConscryptUtil;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.signature.RsaSsaPssParameters;
import com.google.crypto.tink.signature.RsaSsaPssPublicKey;
import com.google.crypto.tink.subtle.Validators;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class RsaSsaPssVerifyConscrypt implements PublicKeyVerify {
    private static final String MGF_1 = "MGF1";
    private static final int TRAILER_FIELD_BC = 1;
    private final Provider conscrypt;
    private final byte[] messageSuffix;
    private final byte[] outputPrefix;
    private final PSSParameterSpec parameterSpec;
    private final RSAPublicKey publicKey;
    private final String signatureAlgorithm;
    public static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_REQUIRES_BORINGCRYPTO;
    private static final byte[] EMPTY = new byte[0];
    private static final byte[] LEGACY_MESSAGE_SUFFIX = {0};

    @Nullable
    static Provider conscryptProviderOrNull() {
        if (!Util.isAndroid() || Util.getAndroidApiLevel().intValue() > 23) {
            return ConscryptUtil.providerOrNull();
        }
        return null;
    }

    static String getConscryptRsaSsaPssAlgo(RsaSsaPssParameters.HashType hash) {
        if (hash == RsaSsaPssParameters.HashType.SHA256) {
            return "SHA256withRSA/PSS";
        }
        if (hash == RsaSsaPssParameters.HashType.SHA384) {
            return "SHA384withRSA/PSS";
        }
        if (hash == RsaSsaPssParameters.HashType.SHA512) {
            return "SHA512withRSA/PSS";
        }
        throw new IllegalArgumentException("Unsupported hash: " + hash);
    }

    private static String getMdName(RsaSsaPssParameters.HashType sigHash) {
        if (sigHash == RsaSsaPssParameters.HashType.SHA256) {
            return "SHA-256";
        }
        if (sigHash == RsaSsaPssParameters.HashType.SHA384) {
            return "SHA-384";
        }
        if (sigHash == RsaSsaPssParameters.HashType.SHA512) {
            return "SHA-512";
        }
        throw new IllegalArgumentException("Unsupported MD hash: " + sigHash);
    }

    private static MGF1ParameterSpec getMgf1Hash(RsaSsaPssParameters.HashType mgf1Hash) {
        if (mgf1Hash == RsaSsaPssParameters.HashType.SHA256) {
            return MGF1ParameterSpec.SHA256;
        }
        if (mgf1Hash == RsaSsaPssParameters.HashType.SHA384) {
            return MGF1ParameterSpec.SHA384;
        }
        if (mgf1Hash == RsaSsaPssParameters.HashType.SHA512) {
            return MGF1ParameterSpec.SHA512;
        }
        throw new IllegalArgumentException("Unsupported MGF1 hash: " + mgf1Hash);
    }

    static PSSParameterSpec getPssParameterSpec(RsaSsaPssParameters.HashType sigHash, RsaSsaPssParameters.HashType mgf1Hash, int saltLength) {
        return new PSSParameterSpec(getMdName(sigHash), MGF_1, getMgf1Hash(mgf1Hash), saltLength, 1);
    }

    private RsaSsaPssVerifyConscrypt(final RSAPublicKey pubKey, RsaSsaPssParameters.HashType sigHash, RsaSsaPssParameters.HashType mgf1Hash, int saltLength, byte[] outputPrefix, byte[] messageSuffix, Provider conscrypt) throws GeneralSecurityException {
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Cannot use RSA SSA PSS in FIPS-mode, as BoringCrypto module is not available.");
        }
        if (!sigHash.equals(mgf1Hash)) {
            throw new GeneralSecurityException("sigHash and mgf1Hash must be the same");
        }
        Validators.validateRsaModulusSize(pubKey.getModulus().bitLength());
        Validators.validateRsaPublicExponent(pubKey.getPublicExponent());
        this.publicKey = pubKey;
        this.signatureAlgorithm = getConscryptRsaSsaPssAlgo(sigHash);
        this.parameterSpec = getPssParameterSpec(sigHash, mgf1Hash, saltLength);
        this.outputPrefix = outputPrefix;
        this.messageSuffix = messageSuffix;
        this.conscrypt = conscrypt;
    }

    public static PublicKeyVerify create(RsaSsaPssPublicKey key) throws GeneralSecurityException {
        byte[] bArr;
        Provider providerConscryptProviderOrNull = conscryptProviderOrNull();
        if (providerConscryptProviderOrNull == null) {
            throw new NoSuchProviderException("RSA SSA PSS using Conscrypt is not supported.");
        }
        RSAPublicKey rSAPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA", providerConscryptProviderOrNull).generatePublic(new RSAPublicKeySpec(key.getModulus(), key.getParameters().getPublicExponent()));
        RsaSsaPssParameters parameters = key.getParameters();
        RsaSsaPssParameters.HashType sigHashType = parameters.getSigHashType();
        RsaSsaPssParameters.HashType mgf1HashType = parameters.getMgf1HashType();
        int saltLengthBytes = parameters.getSaltLengthBytes();
        byte[] byteArray = key.getOutputPrefix().toByteArray();
        if (key.getParameters().getVariant().equals(RsaSsaPssParameters.Variant.LEGACY)) {
            bArr = LEGACY_MESSAGE_SUFFIX;
        } else {
            bArr = EMPTY;
        }
        return new RsaSsaPssVerifyConscrypt(rSAPublicKey, sigHashType, mgf1HashType, saltLengthBytes, byteArray, bArr, providerConscryptProviderOrNull);
    }

    @Override // com.google.crypto.tink.PublicKeyVerify
    public void verify(final byte[] signature, final byte[] data) throws GeneralSecurityException {
        if (!Util.isPrefix(this.outputPrefix, signature)) {
            throw new GeneralSecurityException("Invalid signature (output prefix mismatch)");
        }
        Signature signature2 = Signature.getInstance(this.signatureAlgorithm, this.conscrypt);
        signature2.initVerify(this.publicKey);
        signature2.setParameter(this.parameterSpec);
        signature2.update(data);
        byte[] bArr = this.messageSuffix;
        if (bArr.length > 0) {
            signature2.update(bArr);
        }
        byte[] bArr2 = this.outputPrefix;
        if (!signature2.verify(signature, bArr2.length, signature.length - bArr2.length)) {
            throw new GeneralSecurityException("signature verification failed");
        }
    }
}
