package com.google.crypto.tink.jwt;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.KeyManager;
import com.google.crypto.tink.KeyTemplate;
import com.google.crypto.tink.Mac;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.KeyManagerRegistry;
import com.google.crypto.tink.internal.LegacyKeyManagerImpl;
import com.google.crypto.tink.internal.MutableKeyCreationRegistry;
import com.google.crypto.tink.internal.MutableParametersRegistry;
import com.google.crypto.tink.internal.MutablePrimitiveRegistry;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.internal.TinkBugException;
import com.google.crypto.tink.mac.HmacKey;
import com.google.crypto.tink.mac.HmacParameters;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.subtle.PrfMac;
import com.google.crypto.tink.util.SecretBytes;
import com.google.errorprone.annotations.Immutable;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class JwtHmacKeyManager {
    private static final KeyManager<Void> legacyKeyManager = LegacyKeyManagerImpl.create("type.googleapis.com/google.crypto.tink.JwtHmacKey", Void.class, KeyData.KeyMaterialType.SYMMETRIC, com.google.crypto.tink.proto.JwtHmacKey.parser());
    private static final PrimitiveConstructor<JwtHmacKey, JwtMac> PRIMITIVE_CONSTRUCTOR = PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.jwt.JwtHmacKeyManager$$ExternalSyntheticLambda3
        @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
        public final Object constructPrimitive(Key key) {
            return JwtHmacKeyManager.createFullJwtHmac((JwtHmacKey) key);
        }
    }, JwtHmacKey.class, JwtMac.class);
    private static final MutableKeyCreationRegistry.KeyCreator<JwtHmacParameters> KEY_CREATOR = new MutableKeyCreationRegistry.KeyCreator() { // from class: com.google.crypto.tink.jwt.JwtHmacKeyManager$$ExternalSyntheticLambda4
        @Override // com.google.crypto.tink.internal.MutableKeyCreationRegistry.KeyCreator
        public final Key createKey(Parameters parameters, Integer num) {
            return JwtHmacKeyManager.createKey((JwtHmacParameters) parameters, num);
        }
    };
    private static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_REQUIRES_BORINGCRYPTO;

    @Immutable
    private static final class JwtHmac implements JwtMac {
        private final String algorithm;
        private final JwtHmacKey jwtHmacKey;
        private final Mac mac;

        private JwtHmac(Mac plainMac, JwtHmacKey jwtHmacKey) {
            this.algorithm = jwtHmacKey.getParameters().getAlgorithm().getStandardName();
            this.mac = plainMac;
            this.jwtHmacKey = jwtHmacKey;
        }

        @Override // com.google.crypto.tink.jwt.JwtMac
        public String computeMacAndEncode(RawJwt rawJwt) throws GeneralSecurityException {
            String strCreateUnsignedCompact = JwtFormat.createUnsignedCompact(this.algorithm, this.jwtHmacKey.getKid(), rawJwt);
            return JwtFormat.createSignedCompact(strCreateUnsignedCompact, this.mac.computeMac(strCreateUnsignedCompact.getBytes(StandardCharsets.US_ASCII)));
        }

        @Override // com.google.crypto.tink.jwt.JwtMac
        public VerifiedJwt verifyMacAndDecode(String compact, JwtValidator validator) throws GeneralSecurityException {
            JwtFormat.Parts partsSplitSignedCompact = JwtFormat.splitSignedCompact(compact);
            this.mac.verifyMac(partsSplitSignedCompact.signatureOrMac, partsSplitSignedCompact.unsignedCompact.getBytes(StandardCharsets.US_ASCII));
            JsonObject json = JsonUtil.parseJson(partsSplitSignedCompact.header);
            JwtFormat.validateHeader(json, this.jwtHmacKey.getParameters().getAlgorithm().getStandardName(), this.jwtHmacKey.getKid(), this.jwtHmacKey.getParameters().allowKidAbsent());
            return validator.validate(RawJwt.fromJsonPayload(JwtFormat.getTypeHeader(json), partsSplitSignedCompact.payload));
        }
    }

    private static void validate(JwtHmacParameters parameters) throws GeneralSecurityException {
        int i = parameters.getAlgorithm().equals(JwtHmacParameters.Algorithm.HS256) ? 32 : Integer.MAX_VALUE;
        if (parameters.getAlgorithm().equals(JwtHmacParameters.Algorithm.HS384)) {
            i = 48;
        }
        if (parameters.getAlgorithm().equals(JwtHmacParameters.Algorithm.HS512)) {
            i = 64;
        }
        if (parameters.getKeySizeBytes() >= i) {
            return;
        }
        throw new GeneralSecurityException("Key size must be at least " + i);
    }

    private static int getTagLength(JwtHmacParameters.Algorithm algorithm) throws GeneralSecurityException {
        if (algorithm.equals(JwtHmacParameters.Algorithm.HS256)) {
            return 32;
        }
        if (algorithm.equals(JwtHmacParameters.Algorithm.HS384)) {
            return 48;
        }
        if (algorithm.equals(JwtHmacParameters.Algorithm.HS512)) {
            return 64;
        }
        throw new GeneralSecurityException("Unsupported algorithm: " + algorithm);
    }

    private static HmacParameters.HashType getHmacHashType(JwtHmacParameters.Algorithm algorithm) throws GeneralSecurityException {
        if (algorithm.equals(JwtHmacParameters.Algorithm.HS256)) {
            return HmacParameters.HashType.SHA256;
        }
        if (algorithm.equals(JwtHmacParameters.Algorithm.HS384)) {
            return HmacParameters.HashType.SHA384;
        }
        if (algorithm.equals(JwtHmacParameters.Algorithm.HS512)) {
            return HmacParameters.HashType.SHA512;
        }
        throw new GeneralSecurityException("Unsupported algorithm: " + algorithm);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static JwtMac createFullJwtHmac(JwtHmacKey key) throws GeneralSecurityException {
        validate(key.getParameters());
        return new JwtHmac(PrfMac.create(HmacKey.builder().setParameters(HmacParameters.builder().setKeySizeBytes(key.getParameters().getKeySizeBytes()).setHashType(getHmacHashType(key.getParameters().getAlgorithm())).setTagSizeBytes(getTagLength(key.getParameters().getAlgorithm())).build()).setKeyBytes(key.getKeyBytes()).build()), key);
    }

    static String getKeyType() {
        return "type.googleapis.com/google.crypto.tink.JwtHmacKey";
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static JwtHmacKey createKey(JwtHmacParameters parameters, @Nullable Integer idRequirement) throws GeneralSecurityException {
        validate(parameters);
        JwtHmacKey.Builder keyBytes = JwtHmacKey.builder().setParameters(parameters).setKeyBytes(SecretBytes.randomBytes(parameters.getKeySizeBytes()));
        if (parameters.hasIdRequirement()) {
            if (idRequirement == null) {
                throw new GeneralSecurityException("Cannot create key without ID requirement with parameters with ID requirement");
            }
            keyBytes.setIdRequirement(idRequirement.intValue());
        }
        return keyBytes.build();
    }

    private static Map<String, Parameters> namedParameters() throws GeneralSecurityException {
        HashMap map = new HashMap();
        map.put("JWT_HS256_RAW", JwtHmacParameters.builder().setKeySizeBytes(32).setAlgorithm(JwtHmacParameters.Algorithm.HS256).setKidStrategy(JwtHmacParameters.KidStrategy.IGNORED).build());
        map.put("JWT_HS256", JwtHmacParameters.builder().setKeySizeBytes(32).setAlgorithm(JwtHmacParameters.Algorithm.HS256).setKidStrategy(JwtHmacParameters.KidStrategy.BASE64_ENCODED_KEY_ID).build());
        map.put("JWT_HS384_RAW", JwtHmacParameters.builder().setKeySizeBytes(48).setAlgorithm(JwtHmacParameters.Algorithm.HS384).setKidStrategy(JwtHmacParameters.KidStrategy.IGNORED).build());
        map.put("JWT_HS384", JwtHmacParameters.builder().setKeySizeBytes(48).setAlgorithm(JwtHmacParameters.Algorithm.HS384).setKidStrategy(JwtHmacParameters.KidStrategy.BASE64_ENCODED_KEY_ID).build());
        map.put("JWT_HS512_RAW", JwtHmacParameters.builder().setKeySizeBytes(64).setAlgorithm(JwtHmacParameters.Algorithm.HS512).setKidStrategy(JwtHmacParameters.KidStrategy.IGNORED).build());
        map.put("JWT_HS512", JwtHmacParameters.builder().setKeySizeBytes(64).setAlgorithm(JwtHmacParameters.Algorithm.HS512).setKidStrategy(JwtHmacParameters.KidStrategy.BASE64_ENCODED_KEY_ID).build());
        return Collections.unmodifiableMap(map);
    }

    public TinkFipsUtil.AlgorithmFipsCompatibility fipsStatus() {
        return FIPS;
    }

    public static void register(boolean newKeyAllowed) throws GeneralSecurityException {
        TinkFipsUtil.AlgorithmFipsCompatibility algorithmFipsCompatibility = FIPS;
        if (!algorithmFipsCompatibility.isCompatible()) {
            throw new GeneralSecurityException("Can not use HMAC in FIPS-mode, as BoringCrypto module is not available.");
        }
        JwtHmacProtoSerialization.register();
        MutableKeyCreationRegistry.globalInstance().add(KEY_CREATOR, JwtHmacParameters.class);
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveConstructor(PRIMITIVE_CONSTRUCTOR);
        MutableParametersRegistry.globalInstance().putAll(namedParameters());
        KeyManagerRegistry.globalInstance().registerKeyManagerWithFipsCompatibility(legacyKeyManager, algorithmFipsCompatibility, newKeyAllowed);
    }

    public static final KeyTemplate hs256Template() {
        return (KeyTemplate) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.jwt.JwtHmacKeyManager$$ExternalSyntheticLambda1
            @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
            public final Object get() {
                return KeyTemplate.createFrom(JwtHmacParameters.builder().setKeySizeBytes(32).setKidStrategy(JwtHmacParameters.KidStrategy.IGNORED).setAlgorithm(JwtHmacParameters.Algorithm.HS256).build());
            }
        });
    }

    public static final KeyTemplate hs384Template() {
        return (KeyTemplate) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.jwt.JwtHmacKeyManager$$ExternalSyntheticLambda0
            @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
            public final Object get() {
                return KeyTemplate.createFrom(JwtHmacParameters.builder().setKeySizeBytes(48).setKidStrategy(JwtHmacParameters.KidStrategy.IGNORED).setAlgorithm(JwtHmacParameters.Algorithm.HS384).build());
            }
        });
    }

    public static final KeyTemplate hs512Template() {
        return (KeyTemplate) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.jwt.JwtHmacKeyManager$$ExternalSyntheticLambda2
            @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
            public final Object get() {
                return KeyTemplate.createFrom(JwtHmacParameters.builder().setKeySizeBytes(64).setKidStrategy(JwtHmacParameters.KidStrategy.IGNORED).setAlgorithm(JwtHmacParameters.Algorithm.HS512).build());
            }
        });
    }

    private JwtHmacKeyManager() {
    }
}
