package com.google.crypto.tink.internal;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.KeyManager;
import com.google.crypto.tink.PrivateKey;
import com.google.crypto.tink.PrivateKeyManager;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.proto.KeyTemplate;
import com.google.crypto.tink.proto.OutputPrefixType;
import com.google.crypto.tink.shaded.protobuf.ByteString;
import com.google.crypto.tink.shaded.protobuf.ExtensionRegistryLite;
import com.google.crypto.tink.shaded.protobuf.InvalidProtocolBufferException;
import com.google.crypto.tink.shaded.protobuf.MessageLite;
import com.google.crypto.tink.shaded.protobuf.Parser;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public class LegacyKeyManagerImpl<P> implements KeyManager<P> {
    final KeyData.KeyMaterialType keyMaterialType;
    final Class<P> primitiveClass;
    final Parser<? extends MessageLite> protobufKeyParser;
    final String typeUrl;

    @Override // com.google.crypto.tink.KeyManager
    public int getVersion() {
        return 0;
    }

    public static <P> KeyManager<P> create(String typeUrl, Class<P> primitiveClass, KeyData.KeyMaterialType keyMaterialType, Parser<? extends MessageLite> protobufKeyParser) {
        return new LegacyKeyManagerImpl(typeUrl, primitiveClass, keyMaterialType, protobufKeyParser);
    }

    LegacyKeyManagerImpl(String typeUrl, Class<P> primitiveClass, KeyData.KeyMaterialType keyMaterialType, Parser<? extends MessageLite> protobufKeyParser) {
        this.protobufKeyParser = protobufKeyParser;
        this.typeUrl = typeUrl;
        this.primitiveClass = primitiveClass;
        this.keyMaterialType = keyMaterialType;
    }

    @Override // com.google.crypto.tink.KeyManager
    public P getPrimitive(ByteString byteString) throws GeneralSecurityException {
        return (P) MutablePrimitiveRegistry.globalInstance().getPrimitive(MutableSerializationRegistry.globalInstance().parseKey(ProtoKeySerialization.create(this.typeUrl, byteString, this.keyMaterialType, OutputPrefixType.RAW, null), InsecureSecretKeyAccess.get()), this.primitiveClass);
    }

    @Override // com.google.crypto.tink.KeyManager
    public final P getPrimitive(MessageLite key) throws GeneralSecurityException {
        return getPrimitive(key.toByteString());
    }

    @Override // com.google.crypto.tink.KeyManager
    public final MessageLite newKey(ByteString serializedKeyFormat) throws GeneralSecurityException {
        try {
            return this.protobufKeyParser.parseFrom(newKeyData(serializedKeyFormat).getValue(), ExtensionRegistryLite.getEmptyRegistry());
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
        return this.typeUrl;
    }

    @Override // com.google.crypto.tink.KeyManager
    public final KeyData newKeyData(ByteString serializedKeyFormat) throws GeneralSecurityException {
        ProtoKeySerialization protoKeySerialization = (ProtoKeySerialization) MutableSerializationRegistry.globalInstance().serializeKey(MutableKeyCreationRegistry.globalInstance().createKey(MutableSerializationRegistry.globalInstance().parseParameters(ProtoParametersSerialization.checkedCreate((KeyTemplate) KeyTemplate.newBuilder().setTypeUrl(this.typeUrl).setValue(serializedKeyFormat).setOutputPrefixType(OutputPrefixType.RAW).build())), null), ProtoKeySerialization.class, InsecureSecretKeyAccess.get());
        return (KeyData) KeyData.newBuilder().setTypeUrl(protoKeySerialization.getTypeUrl()).setValue(protoKeySerialization.getValue()).setKeyMaterialType(protoKeySerialization.getKeyMaterialType()).build();
    }

    @Override // com.google.crypto.tink.KeyManager
    public final Class<P> getPrimitiveClass() {
        return this.primitiveClass;
    }

    private static class LegacyPrivateKeyManagerImpl<P> extends LegacyKeyManagerImpl<P> implements PrivateKeyManager<P> {
        protected LegacyPrivateKeyManagerImpl(String typeUrl, Class<P> primitiveClass, Parser<? extends MessageLite> protobufKeyParser) {
            super(typeUrl, primitiveClass, KeyData.KeyMaterialType.ASYMMETRIC_PRIVATE, protobufKeyParser);
        }

        @Override // com.google.crypto.tink.PrivateKeyManager
        public KeyData getPublicKeyData(ByteString serializedKey) throws GeneralSecurityException {
            Object key = MutableSerializationRegistry.globalInstance().parseKey(ProtoKeySerialization.create(this.typeUrl, serializedKey, this.keyMaterialType, OutputPrefixType.RAW, null), InsecureSecretKeyAccess.get());
            if (!(key instanceof PrivateKey)) {
                throw new GeneralSecurityException("Key not private key");
            }
            ProtoKeySerialization protoKeySerialization = (ProtoKeySerialization) MutableSerializationRegistry.globalInstance().serializeKey(((PrivateKey) key).getPublicKey(), ProtoKeySerialization.class, InsecureSecretKeyAccess.get());
            return (KeyData) KeyData.newBuilder().setTypeUrl(protoKeySerialization.getTypeUrl()).setValue(protoKeySerialization.getValue()).setKeyMaterialType(protoKeySerialization.getKeyMaterialType()).build();
        }
    }

    public static <P> PrivateKeyManager<P> createPrivateKeyManager(String typeUrl, Class<P> primitiveClass, Parser<? extends MessageLite> protobufKeyParser) {
        return new LegacyPrivateKeyManagerImpl(typeUrl, primitiveClass, protobufKeyParser);
    }
}
