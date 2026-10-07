package com.google.crypto.tink.subtle;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
@Deprecated
public enum PemKeyType {
    RSA_PSS_2048_SHA256("RSA", "RSASSA-PSS", 2048, Enums.HashType.SHA256),
    RSA_PSS_3072_SHA256("RSA", "RSASSA-PSS", 3072, Enums.HashType.SHA256),
    RSA_PSS_4096_SHA256("RSA", "RSASSA-PSS", 4096, Enums.HashType.SHA256),
    RSA_PSS_4096_SHA512("RSA", "RSASSA-PSS", 4096, Enums.HashType.SHA512),
    RSA_SIGN_PKCS1_2048_SHA256("RSA", "RSASSA-PKCS1-v1_5", 2048, Enums.HashType.SHA256),
    RSA_SIGN_PKCS1_3072_SHA256("RSA", "RSASSA-PKCS1-v1_5", 3072, Enums.HashType.SHA256),
    RSA_SIGN_PKCS1_4096_SHA256("RSA", "RSASSA-PKCS1-v1_5", 4096, Enums.HashType.SHA256),
    RSA_SIGN_PKCS1_4096_SHA512("RSA", "RSASSA-PKCS1-v1_5", 4096, Enums.HashType.SHA512),
    ECDSA_P256_SHA256("EC", "ECDSA", 256, Enums.HashType.SHA256),
    ECDSA_P384_SHA384("EC", "ECDSA", 384, Enums.HashType.SHA384),
    ECDSA_P521_SHA512("EC", "ECDSA", 521, Enums.HashType.SHA512);

    private static final String BEGIN = "-----BEGIN ";
    private static final String END = "-----END ";
    private static final String MARKER = "-----";
    private static final String PRIVATE_KEY = "PRIVATE KEY";
    private static final String PUBLIC_KEY = "PUBLIC KEY";
    public final String algorithm;
    public final Enums.HashType hash;
    public final int keySizeInBits;
    public final String keyType;

    PemKeyType(String keyType, String algorithm, int keySizeInBits, Enums.HashType hash) {
        this.keyType = keyType;
        this.algorithm = algorithm;
        this.keySizeInBits = keySizeInBits;
        this.hash = hash;
    }

    @Nullable
    public Key readKey(BufferedReader reader) throws IOException {
        String strSubstring;
        int iIndexOf;
        String line = reader.readLine();
        while (line != null && !line.startsWith(BEGIN)) {
            line = reader.readLine();
        }
        if (line == null || (iIndexOf = (strSubstring = line.trim().substring(11)).indexOf(MARKER)) < 0) {
            return null;
        }
        String strSubstring2 = strSubstring.substring(0, iIndexOf);
        String str = END + strSubstring2 + MARKER;
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line2 = reader.readLine();
            if (line2 == null) {
                break;
            }
            if (line2.indexOf(":") <= 0) {
                if (line2.contains(str)) {
                    break;
                }
                sb.append(line2);
            }
        }
        try {
            byte[] bArrDecode = Base64.decode(sb.toString(), 0);
            if (strSubstring2.contains(PUBLIC_KEY)) {
                return getPublicKey(bArrDecode);
            }
            if (strSubstring2.contains(PRIVATE_KEY)) {
                return getPrivateKey(bArrDecode);
            }
            return null;
        } catch (IllegalArgumentException | GeneralSecurityException unused) {
        }
    }

    private Key getPublicKey(final byte[] key) throws GeneralSecurityException {
        return validate(EngineFactory.KEY_FACTORY.getInstance(this.keyType).generatePublic(new X509EncodedKeySpec(key)));
    }

    private Key getPrivateKey(final byte[] key) throws GeneralSecurityException {
        return validate(EngineFactory.KEY_FACTORY.getInstance(this.keyType).generatePrivate(new PKCS8EncodedKeySpec(key)));
    }

    private Key validate(Key key) throws GeneralSecurityException {
        if (this.keyType.equals("RSA")) {
            int iBitLength = ((RSAKey) key).getModulus().bitLength();
            if (iBitLength != this.keySizeInBits) {
                throw new GeneralSecurityException(String.format("invalid RSA key size, want %d got %d", Integer.valueOf(this.keySizeInBits), Integer.valueOf(iBitLength)));
            }
        } else {
            ECParameterSpec params = ((ECKey) key).getParams();
            if (!EllipticCurves.isNistEcParameterSpec(params)) {
                throw new GeneralSecurityException("unsupport EC spec: " + params.toString());
            }
            int iFieldSizeInBits = EllipticCurves.fieldSizeInBits(params.getCurve());
            if (iFieldSizeInBits != this.keySizeInBits) {
                throw new GeneralSecurityException(String.format("invalid EC key size, want %d got %d", Integer.valueOf(this.keySizeInBits), Integer.valueOf(iFieldSizeInBits)));
            }
        }
        return key;
    }
}
