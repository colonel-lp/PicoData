package com.google.crypto.tink.aead;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.KeyManager;
import com.google.crypto.tink.KeyTemplate;
import com.google.crypto.tink.KmsClients;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.aead.internal.LegacyFullAead;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.KeyManagerRegistry;
import com.google.crypto.tink.internal.LegacyKeyManagerImpl;
import com.google.crypto.tink.internal.MutableKeyCreationRegistry;
import com.google.crypto.tink.internal.MutablePrimitiveRegistry;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.proto.KmsAeadKey;
import java.security.GeneralSecurityException;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class KmsAeadKeyManager {
    private static final PrimitiveConstructor<LegacyKmsAeadKey, Aead> LEGACY_KMS_AEAD_PRIMITIVE_CONSTRUCTOR = PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.aead.KmsAeadKeyManager$$ExternalSyntheticLambda0
        @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
        public final Object constructPrimitive(Key key) {
            return KmsAeadKeyManager.create((LegacyKmsAeadKey) key);
        }
    }, LegacyKmsAeadKey.class, Aead.class);
    private static final KeyManager<Aead> legacyKeyManager = LegacyKeyManagerImpl.create(getKeyType(), Aead.class, KeyData.KeyMaterialType.REMOTE, KmsAeadKey.parser());
    private static final MutableKeyCreationRegistry.KeyCreator<LegacyKmsAeadParameters> KEY_CREATOR = new MutableKeyCreationRegistry.KeyCreator() { // from class: com.google.crypto.tink.aead.KmsAeadKeyManager$$ExternalSyntheticLambda1
        @Override // com.google.crypto.tink.internal.MutableKeyCreationRegistry.KeyCreator
        public final Key createKey(Parameters parameters, Integer num) {
            return KmsAeadKeyManager.newKey((LegacyKmsAeadParameters) parameters, num);
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public static Aead create(LegacyKmsAeadKey key) throws GeneralSecurityException {
        return LegacyFullAead.create(KmsClients.get(key.getParameters().keyUri()).getAead(key.getParameters().keyUri()), key.getOutputPrefix());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static LegacyKmsAeadKey newKey(LegacyKmsAeadParameters parameters, @Nullable Integer idRequirement) throws GeneralSecurityException {
        return LegacyKmsAeadKey.create(parameters, idRequirement);
    }

    static String getKeyType() {
        return "type.googleapis.com/google.crypto.tink.KmsAeadKey";
    }

    public static void register(boolean newKeyAllowed) throws GeneralSecurityException {
        if (!TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_NOT_FIPS.isCompatible()) {
            throw new GeneralSecurityException("Registering KMS AEAD is not supported in FIPS mode");
        }
        LegacyKmsAeadProtoSerialization.register();
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveConstructor(LEGACY_KMS_AEAD_PRIMITIVE_CONSTRUCTOR);
        MutableKeyCreationRegistry.globalInstance().add(KEY_CREATOR, LegacyKmsAeadParameters.class);
        KeyManagerRegistry.globalInstance().registerKeyManager(legacyKeyManager, newKeyAllowed);
    }

    public static KeyTemplate createKeyTemplate(String keyUri) {
        try {
            return KeyTemplate.createFrom(LegacyKmsAeadParameters.create(keyUri));
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private KmsAeadKeyManager() {
    }
}
