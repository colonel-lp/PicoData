package com.google.crypto.tink.keyderivation.internal;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.KeyManager;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.internal.KeyManagerRegistry;
import com.google.crypto.tink.internal.MutableKeyCreationRegistry;
import com.google.crypto.tink.internal.MutablePrimitiveRegistry;
import com.google.crypto.tink.internal.MutableSerializationRegistry;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.internal.ProtoKeySerialization;
import com.google.crypto.tink.internal.ProtoParametersSerialization;
import com.google.crypto.tink.keyderivation.PrfBasedKeyDerivationKey;
import com.google.crypto.tink.keyderivation.PrfBasedKeyDerivationParameters;
import com.google.crypto.tink.prf.PrfKey;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.proto.KeyTemplate;
import com.google.crypto.tink.proto.OutputPrefixType;
import com.google.crypto.tink.proto.PrfBasedDeriverKey;
import com.google.crypto.tink.proto.PrfBasedDeriverKeyFormat;
import com.google.crypto.tink.shaded.protobuf.ByteString;
import com.google.crypto.tink.shaded.protobuf.ExtensionRegistryLite;
import com.google.crypto.tink.shaded.protobuf.InvalidProtocolBufferException;
import com.google.crypto.tink.shaded.protobuf.MessageLite;
import java.security.GeneralSecurityException;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class PrfBasedDeriverKeyManager implements KeyManager<Void> {
    private static final String TYPE_URL = "type.googleapis.com/google.crypto.tink.PrfBasedDeriverKey";
    private static final PrimitiveConstructor<PrfBasedKeyDerivationKey, KeyDeriver> PRIMITIVE_CONSTRUCTOR = PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.keyderivation.internal.PrfBasedDeriverKeyManager$$ExternalSyntheticLambda0
        @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
        public final Object constructPrimitive(Key key) {
            return PrfBasedKeyDeriver.create((PrfBasedKeyDerivationKey) key);
        }
    }, PrfBasedKeyDerivationKey.class, KeyDeriver.class);
    private static final MutableKeyCreationRegistry.KeyCreator<PrfBasedKeyDerivationParameters> KEY_CREATOR = new MutableKeyCreationRegistry.KeyCreator() { // from class: com.google.crypto.tink.keyderivation.internal.PrfBasedDeriverKeyManager$$ExternalSyntheticLambda1
        @Override // com.google.crypto.tink.internal.MutableKeyCreationRegistry.KeyCreator
        public final Key createKey(Parameters parameters, Integer num) {
            return PrfBasedDeriverKeyManager.createNewKey((PrfBasedKeyDerivationParameters) parameters, num);
        }
    };

    @Override // com.google.crypto.tink.KeyManager
    public int getVersion() {
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final PrfBasedKeyDerivationKey createNewKey(PrfBasedKeyDerivationParameters parameters, @Nullable Integer idRequirement) throws GeneralSecurityException {
        Key keyCreateKey = MutableKeyCreationRegistry.globalInstance().createKey(parameters.getPrfParameters(), null);
        if (!(keyCreateKey instanceof PrfKey)) {
            throw new GeneralSecurityException("Failed to create PrfKey from parameters" + parameters.getPrfParameters() + ", instead got " + keyCreateKey.getClass());
        }
        return PrfBasedKeyDerivationKey.create(parameters, (PrfKey) keyCreateKey, idRequirement);
    }

    PrfBasedDeriverKeyManager() {
    }

    @Override // com.google.crypto.tink.KeyManager
    public Void getPrimitive(ByteString serializedKey) throws GeneralSecurityException {
        throw new GeneralSecurityException("Cannot use the KeyManager to get a primitive for KeyDerivation");
    }

    @Override // com.google.crypto.tink.KeyManager
    public final Void getPrimitive(MessageLite key) throws GeneralSecurityException {
        throw new GeneralSecurityException("Cannot use the KeyManager to get a primitive for KeyDerivation");
    }

    @Override // com.google.crypto.tink.KeyManager
    public final MessageLite newKey(ByteString serializedKeyFormat) throws GeneralSecurityException {
        try {
            return PrfBasedDeriverKey.parseFrom(newKeyData(serializedKeyFormat).getValue(), ExtensionRegistryLite.getEmptyRegistry());
        } catch (InvalidProtocolBufferException unused) {
            throw new GeneralSecurityException("Unexpectedly failed to parse key");
        }
    }

    @Override // com.google.crypto.tink.KeyManager
    public final MessageLite newKey(MessageLite keyFormat) throws GeneralSecurityException {
        return newKey(keyFormat.toByteString());
    }

    @Override // com.google.crypto.tink.KeyManager
    public final boolean doesSupport(String typeUrl) {
        return typeUrl.equals(getKeyType());
    }

    @Override // com.google.crypto.tink.KeyManager
    public final String getKeyType() {
        return TYPE_URL;
    }

    private static OutputPrefixType getOutputPrefixTypeFromSerializedKeyFormat(ByteString serializedKeyFormat) throws GeneralSecurityException {
        try {
            return PrfBasedDeriverKeyFormat.parseFrom(serializedKeyFormat, ExtensionRegistryLite.getEmptyRegistry()).getParams().getDerivedKeyTemplate().getOutputPrefixType();
        } catch (InvalidProtocolBufferException e) {
            throw new GeneralSecurityException("Unexpectedly failed to parse key format", e);
        }
    }

    @Override // com.google.crypto.tink.KeyManager
    public final KeyData newKeyData(ByteString serializedKeyFormat) throws GeneralSecurityException {
        OutputPrefixType outputPrefixTypeFromSerializedKeyFormat = getOutputPrefixTypeFromSerializedKeyFormat(serializedKeyFormat);
        ProtoKeySerialization protoKeySerialization = (ProtoKeySerialization) MutableSerializationRegistry.globalInstance().serializeKey(MutableKeyCreationRegistry.globalInstance().createKey(MutableSerializationRegistry.globalInstance().parseParameters(ProtoParametersSerialization.checkedCreate((KeyTemplate) KeyTemplate.newBuilder().setTypeUrl(TYPE_URL).setValue(serializedKeyFormat).setOutputPrefixType(outputPrefixTypeFromSerializedKeyFormat).build())), !outputPrefixTypeFromSerializedKeyFormat.equals(OutputPrefixType.RAW) ? 123 : null), ProtoKeySerialization.class, InsecureSecretKeyAccess.get());
        return (KeyData) KeyData.newBuilder().setTypeUrl(protoKeySerialization.getTypeUrl()).setValue(protoKeySerialization.getValue()).setKeyMaterialType(protoKeySerialization.getKeyMaterialType()).build();
    }

    @Override // com.google.crypto.tink.KeyManager
    public final Class<Void> getPrimitiveClass() {
        return Void.class;
    }

    public static void register(boolean newKeyAllowed) throws GeneralSecurityException {
        KeyManagerRegistry.globalInstance().registerKeyManager(new PrfBasedDeriverKeyManager(), newKeyAllowed);
        MutableKeyCreationRegistry.globalInstance().add(KEY_CREATOR, PrfBasedKeyDerivationParameters.class);
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveConstructor(PRIMITIVE_CONSTRUCTOR);
        PrfBasedKeyDerivationKeyProtoSerialization.register();
    }
}
