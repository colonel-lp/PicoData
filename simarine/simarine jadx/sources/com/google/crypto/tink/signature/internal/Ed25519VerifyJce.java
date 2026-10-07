package com.google.crypto.tink.signature.internal;

import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.signature.Ed25519Parameters;
import com.google.crypto.tink.signature.Ed25519PublicKey;
import com.google.crypto.tink.subtle.Bytes;
import com.google.crypto.tink.subtle.EngineFactory;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class Ed25519VerifyJce implements PublicKeyVerify {
    private static final String ALGORITHM_NAME = "Ed25519";
    private static final int PUBLIC_KEY_LEN = 32;
    private static final int SIGNATURE_LEN = 64;
    private final byte[] messageSuffix;
    private final byte[] outputPrefix;
    private final PublicKey publicKey;
    public static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_NOT_FIPS;
    private static final byte[] ED25519_X509_PREFIX = {48, 42, 48, 5, 6, 3, 43, 101, 112, 3, 33, 0};

    static byte[] x509EncodePublicKey(byte[] publicKey) throws GeneralSecurityException {
        if (publicKey.length != 32) {
            throw new IllegalArgumentException(String.format("Given public key's length is not %s.", 32));
        }
        return Bytes.concat(ED25519_X509_PREFIX, publicKey);
    }

    public static PublicKeyVerify create(Ed25519PublicKey key) throws GeneralSecurityException {
        byte[] bArr;
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Can not use Ed25519 in FIPS-mode.");
        }
        byte[] byteArray = key.getPublicKeyBytes().toByteArray();
        byte[] byteArray2 = key.getOutputPrefix().toByteArray();
        if (key.getParameters().getVariant().equals(Ed25519Parameters.Variant.LEGACY)) {
            bArr = new byte[]{0};
        } else {
            bArr = new byte[0];
        }
        return new Ed25519VerifyJce(byteArray, byteArray2, bArr);
    }

    Ed25519VerifyJce(final byte[] publicKey) throws GeneralSecurityException {
        this(publicKey, new byte[0], new byte[0]);
    }

    private Ed25519VerifyJce(final byte[] publicKey, final byte[] outputPrefix, final byte[] messageSuffix) throws GeneralSecurityException {
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Can not use Ed25519 in FIPS-mode.");
        }
        this.publicKey = EngineFactory.KEY_FACTORY.getInstance(ALGORITHM_NAME).generatePublic(new X509EncodedKeySpec(x509EncodePublicKey(publicKey)));
        this.outputPrefix = outputPrefix;
        this.messageSuffix = messageSuffix;
    }

    public static boolean isSupported() {
        try {
            EngineFactory.KEY_FACTORY.getInstance(ALGORITHM_NAME);
            EngineFactory.SIGNATURE.getInstance(ALGORITHM_NAME);
            return true;
        } catch (GeneralSecurityException unused) {
            return false;
        }
    }

    @Override // com.google.crypto.tink.PublicKeyVerify
    public void verify(final byte[] signature, final byte[] data) throws GeneralSecurityException {
        boolean zVerify;
        int length = signature.length;
        byte[] bArr = this.outputPrefix;
        if (length != bArr.length + 64) {
            throw new GeneralSecurityException(String.format("Invalid signature length: %s", 64));
        }
        if (!Util.isPrefix(bArr, signature)) {
            throw new GeneralSecurityException("Invalid signature (output prefix mismatch)");
        }
        Signature engineFactory = EngineFactory.SIGNATURE.getInstance(ALGORITHM_NAME);
        engineFactory.initVerify(this.publicKey);
        engineFactory.update(data);
        engineFactory.update(this.messageSuffix);
        try {
            zVerify = engineFactory.verify(signature, this.outputPrefix.length, 64);
        } catch (RuntimeException unused) {
            zVerify = false;
        }
        if (!zVerify) {
            throw new GeneralSecurityException("Signature check failed.");
        }
    }
}
