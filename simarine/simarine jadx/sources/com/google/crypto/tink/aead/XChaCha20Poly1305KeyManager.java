package com.google.crypto.tink.aead;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.KeyManager;
import com.google.crypto.tink.KeyTemplate;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.SecretKeyAccess;
import com.google.crypto.tink.aead.internal.XChaCha20Poly1305Jce;
import com.google.crypto.tink.aead.internal.XChaCha20Poly1305ProtoSerialization;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.KeyManagerRegistry;
import com.google.crypto.tink.internal.LegacyKeyManagerImpl;
import com.google.crypto.tink.internal.MutableKeyCreationRegistry;
import com.google.crypto.tink.internal.MutableKeyDerivationRegistry;
import com.google.crypto.tink.internal.MutableParametersRegistry;
import com.google.crypto.tink.internal.MutablePrimitiveRegistry;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.internal.TinkBugException;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.subtle.XChaCha20Poly1305;
import com.google.crypto.tink.util.SecretBytes;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class XChaCha20Poly1305KeyManager {
    private static final int KEY_SIZE_IN_BYTES = 32;
    private static final PrimitiveConstructor<XChaCha20Poly1305Key, Aead> X_CHA_CHA_20_POLY_1305_PRIMITIVE_CONSTRUCTOR = PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.aead.XChaCha20Poly1305KeyManager$$ExternalSyntheticLambda2
        @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
        public final Object constructPrimitive(Key key) {
            return XChaCha20Poly1305KeyManager.createAead((XChaCha20Poly1305Key) key);
        }
    }, XChaCha20Poly1305Key.class, Aead.class);
    private static final KeyManager<Aead> legacyKeyManager = LegacyKeyManagerImpl.create(getKeyType(), Aead.class, KeyData.KeyMaterialType.SYMMETRIC, com.google.crypto.tink.proto.XChaCha20Poly1305Key.parser());
    private static final MutableKeyDerivationRegistry.InsecureKeyCreator<XChaCha20Poly1305Parameters> KEY_DERIVER = new MutableKeyDerivationRegistry.InsecureKeyCreator() { // from class: com.google.crypto.tink.aead.XChaCha20Poly1305KeyManager$$ExternalSyntheticLambda3
        @Override // com.google.crypto.tink.internal.MutableKeyDerivationRegistry.InsecureKeyCreator
        public final Key createKeyFromRandomness(Parameters parameters, InputStream inputStream, Integer num, SecretKeyAccess secretKeyAccess) {
            return XChaCha20Poly1305KeyManager.createXChaChaKeyFromRandomness((XChaCha20Poly1305Parameters) parameters, inputStream, num, secretKeyAccess);
        }
    };
    private static final MutableKeyCreationRegistry.KeyCreator<XChaCha20Poly1305Parameters> KEY_CREATOR = new MutableKeyCreationRegistry.KeyCreator() { // from class: com.google.crypto.tink.aead.XChaCha20Poly1305KeyManager$$ExternalSyntheticLambda4
        @Override // com.google.crypto.tink.internal.MutableKeyCreationRegistry.KeyCreator
        public final Key createKey(Parameters parameters, Integer num) {
            return XChaCha20Poly1305KeyManager.createXChaChaKey((XChaCha20Poly1305Parameters) parameters, num);
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public static Aead createAead(XChaCha20Poly1305Key key) throws GeneralSecurityException {
        if (XChaCha20Poly1305Jce.isSupported()) {
            return XChaCha20Poly1305Jce.create(key);
        }
        return XChaCha20Poly1305.create(key);
    }

    static String getKeyType() {
        return "type.googleapis.com/google.crypto.tink.XChaCha20Poly1305Key";
    }

    static XChaCha20Poly1305Key createXChaChaKeyFromRandomness(XChaCha20Poly1305Parameters parameters, InputStream stream, @Nullable Integer idRequirement, SecretKeyAccess access) throws GeneralSecurityException {
        return XChaCha20Poly1305Key.create(parameters.getVariant(), Util.readIntoSecretBytes(stream, 32, access), idRequirement);
    }

    static XChaCha20Poly1305Key createXChaChaKey(XChaCha20Poly1305Parameters parameters, @Nullable Integer idRequirement) throws GeneralSecurityException {
        return XChaCha20Poly1305Key.create(parameters.getVariant(), SecretBytes.randomBytes(32), idRequirement);
    }

    private static Map<String, Parameters> namedParameters() throws GeneralSecurityException {
        HashMap map = new HashMap();
        map.put("XCHACHA20_POLY1305", XChaCha20Poly1305Parameters.create(XChaCha20Poly1305Parameters.Variant.TINK));
        map.put("XCHACHA20_POLY1305_RAW", XChaCha20Poly1305Parameters.create(XChaCha20Poly1305Parameters.Variant.NO_PREFIX));
        return Collections.unmodifiableMap(map);
    }

    public static void register(boolean newKeyAllowed) throws GeneralSecurityException {
        if (!TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_NOT_FIPS.isCompatible()) {
            throw new GeneralSecurityException("Registering XChaCha20Poly1305 is not supported in FIPS mode");
        }
        XChaCha20Poly1305ProtoSerialization.register();
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveConstructor(X_CHA_CHA_20_POLY_1305_PRIMITIVE_CONSTRUCTOR);
        MutableParametersRegistry.globalInstance().putAll(namedParameters());
        MutableKeyCreationRegistry.globalInstance().add(KEY_CREATOR, XChaCha20Poly1305Parameters.class);
        MutableKeyDerivationRegistry.globalInstance().add(KEY_DERIVER, XChaCha20Poly1305Parameters.class);
        KeyManagerRegistry.globalInstance().registerKeyManager(legacyKeyManager, newKeyAllowed);
    }

    public static final KeyTemplate xChaCha20Poly1305Template() {
        return (KeyTemplate) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.aead.XChaCha20Poly1305KeyManager$$ExternalSyntheticLambda0
            @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
            public final Object get() {
                return KeyTemplate.createFrom(XChaCha20Poly1305Parameters.create(XChaCha20Poly1305Parameters.Variant.TINK));
            }
        });
    }

    public static final KeyTemplate rawXChaCha20Poly1305Template() {
        return (KeyTemplate) TinkBugException.exceptionIsBug(new TinkBugException.ThrowingSupplier() { // from class: com.google.crypto.tink.aead.XChaCha20Poly1305KeyManager$$ExternalSyntheticLambda1
            @Override // com.google.crypto.tink.internal.TinkBugException.ThrowingSupplier
            public final Object get() {
                return KeyTemplate.createFrom(XChaCha20Poly1305Parameters.create(XChaCha20Poly1305Parameters.Variant.NO_PREFIX));
            }
        });
    }

    private XChaCha20Poly1305KeyManager() {
    }
}
