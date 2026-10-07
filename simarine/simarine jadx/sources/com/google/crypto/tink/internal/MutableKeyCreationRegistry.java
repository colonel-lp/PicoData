package com.google.crypto.tink.internal;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.KeyManager;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.proto.KeyTemplate;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class MutableKeyCreationRegistry {
    private static final KeyCreator<LegacyProtoParameters> LEGACY_PROTO_KEY_CREATOR = new KeyCreator() { // from class: com.google.crypto.tink.internal.MutableKeyCreationRegistry$$ExternalSyntheticLambda0
        @Override // com.google.crypto.tink.internal.MutableKeyCreationRegistry.KeyCreator
        public final Key createKey(Parameters parameters, Integer num) {
            return MutableKeyCreationRegistry.createProtoKeyFromProtoParameters((LegacyProtoParameters) parameters, num);
        }
    };
    private static final MutableKeyCreationRegistry globalInstance = newRegistryWithLegacyFallback();
    private final Map<Class<? extends Parameters>, KeyCreator<? extends Parameters>> creators = new HashMap();

    public interface KeyCreator<ParametersT extends Parameters> {
        Key createKey(ParametersT parameters, @Nullable Integer idRequirement) throws GeneralSecurityException;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static LegacyProtoKey createProtoKeyFromProtoParameters(LegacyProtoParameters parameters, @Nullable Integer idRequirement) throws GeneralSecurityException {
        KeyTemplate keyTemplate = parameters.getSerialization().getKeyTemplate();
        KeyManager<?> untypedKeyManager = KeyManagerRegistry.globalInstance().getUntypedKeyManager(keyTemplate.getTypeUrl());
        if (!KeyManagerRegistry.globalInstance().isNewKeyAllowed(keyTemplate.getTypeUrl())) {
            throw new GeneralSecurityException("Creating new keys is not allowed.");
        }
        KeyData keyDataNewKeyData = untypedKeyManager.newKeyData(keyTemplate.getValue());
        return new LegacyProtoKey(ProtoKeySerialization.create(keyDataNewKeyData.getTypeUrl(), keyDataNewKeyData.getValue(), keyDataNewKeyData.getKeyMaterialType(), keyTemplate.getOutputPrefixType(), idRequirement), InsecureSecretKeyAccess.get());
    }

    private static MutableKeyCreationRegistry newRegistryWithLegacyFallback() {
        MutableKeyCreationRegistry mutableKeyCreationRegistry = new MutableKeyCreationRegistry();
        try {
            mutableKeyCreationRegistry.add(LEGACY_PROTO_KEY_CREATOR, LegacyProtoParameters.class);
            return mutableKeyCreationRegistry;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("unexpected error.", e);
        }
    }

    public static MutableKeyCreationRegistry globalInstance() {
        return globalInstance;
    }

    public synchronized <ParametersT extends Parameters> void add(KeyCreator<ParametersT> creator, Class<ParametersT> parametersClass) throws GeneralSecurityException {
        KeyCreator<? extends Parameters> keyCreator = this.creators.get(parametersClass);
        if (keyCreator != null && !keyCreator.equals(creator)) {
            throw new GeneralSecurityException("Different key creator for parameters class " + parametersClass + " already inserted");
        }
        this.creators.put(parametersClass, creator);
    }

    public Key createKey(Parameters parameters, @Nullable Integer idRequirement) throws GeneralSecurityException {
        return createKeyTyped(parameters, idRequirement);
    }

    private synchronized <ParametersT extends Parameters> Key createKeyTyped(ParametersT parameters, @Nullable Integer idRequirement) throws GeneralSecurityException {
        KeyCreator<? extends Parameters> keyCreator;
        keyCreator = this.creators.get(parameters.getClass());
        if (keyCreator == null) {
            throw new GeneralSecurityException("Cannot create a new key for parameters " + parameters + ": no key creator for this class was registered.");
        }
        return keyCreator.createKey(parameters, idRequirement);
    }
}
