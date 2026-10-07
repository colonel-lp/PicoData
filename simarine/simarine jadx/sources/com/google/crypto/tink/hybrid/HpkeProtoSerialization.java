package com.google.crypto.tink.hybrid;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.SecretKeyAccess;
import com.google.crypto.tink.hybrid.internal.HpkeUtil;
import com.google.crypto.tink.internal.BigIntegerEncoding;
import com.google.crypto.tink.internal.EnumTypeProtoConverter;
import com.google.crypto.tink.internal.KeyParser;
import com.google.crypto.tink.internal.KeySerializer;
import com.google.crypto.tink.internal.MutableSerializationRegistry;
import com.google.crypto.tink.internal.ParametersParser;
import com.google.crypto.tink.internal.ParametersSerializer;
import com.google.crypto.tink.internal.ProtoKeySerialization;
import com.google.crypto.tink.internal.ProtoParametersSerialization;
import com.google.crypto.tink.internal.Serialization;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.proto.HpkeAead;
import com.google.crypto.tink.proto.HpkeKdf;
import com.google.crypto.tink.proto.HpkeKem;
import com.google.crypto.tink.proto.HpkeKeyFormat;
import com.google.crypto.tink.proto.HpkeParams;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.proto.KeyTemplate;
import com.google.crypto.tink.proto.OutputPrefixType;
import com.google.crypto.tink.shaded.protobuf.ByteString;
import com.google.crypto.tink.shaded.protobuf.ExtensionRegistryLite;
import com.google.crypto.tink.shaded.protobuf.InvalidProtocolBufferException;
import com.google.crypto.tink.util.Bytes;
import com.google.crypto.tink.util.SecretBytes;
import java.security.GeneralSecurityException;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class HpkeProtoSerialization {
    private static final EnumTypeProtoConverter<HpkeAead, HpkeParameters.AeadId> AEAD_TYPE_CONVERTER;
    private static final EnumTypeProtoConverter<HpkeKdf, HpkeParameters.KdfId> KDF_TYPE_CONVERTER;
    private static final EnumTypeProtoConverter<HpkeKem, HpkeParameters.KemId> KEM_TYPE_CONVERTER;
    private static final ParametersParser<ProtoParametersSerialization> PARAMETERS_PARSER;
    private static final ParametersSerializer<HpkeParameters, ProtoParametersSerialization> PARAMETERS_SERIALIZER;
    private static final KeyParser<ProtoKeySerialization> PRIVATE_KEY_PARSER;
    private static final KeySerializer<HpkePrivateKey, ProtoKeySerialization> PRIVATE_KEY_SERIALIZER;
    private static final String PRIVATE_TYPE_URL = "type.googleapis.com/google.crypto.tink.HpkePrivateKey";
    private static final Bytes PRIVATE_TYPE_URL_BYTES;
    private static final KeyParser<ProtoKeySerialization> PUBLIC_KEY_PARSER;
    private static final KeySerializer<HpkePublicKey, ProtoKeySerialization> PUBLIC_KEY_SERIALIZER;
    private static final String PUBLIC_TYPE_URL = "type.googleapis.com/google.crypto.tink.HpkePublicKey";
    private static final Bytes PUBLIC_TYPE_URL_BYTES;
    private static final EnumTypeProtoConverter<OutputPrefixType, HpkeParameters.Variant> VARIANT_TYPE_CONVERTER;
    private static final int VERSION = 0;

    static {
        Bytes bytesFromPrintableAscii = Util.toBytesFromPrintableAscii(PRIVATE_TYPE_URL);
        PRIVATE_TYPE_URL_BYTES = bytesFromPrintableAscii;
        Bytes bytesFromPrintableAscii2 = Util.toBytesFromPrintableAscii(PUBLIC_TYPE_URL);
        PUBLIC_TYPE_URL_BYTES = bytesFromPrintableAscii2;
        PARAMETERS_SERIALIZER = ParametersSerializer.create(new ParametersSerializer.ParametersSerializationFunction() { // from class: com.google.crypto.tink.hybrid.HpkeProtoSerialization$$ExternalSyntheticLambda0
            @Override // com.google.crypto.tink.internal.ParametersSerializer.ParametersSerializationFunction
            public final Serialization serializeParameters(Parameters parameters) {
                return HpkeProtoSerialization.serializeParameters((HpkeParameters) parameters);
            }
        }, HpkeParameters.class, ProtoParametersSerialization.class);
        PARAMETERS_PARSER = ParametersParser.create(new ParametersParser.ParametersParsingFunction() { // from class: com.google.crypto.tink.hybrid.HpkeProtoSerialization$$ExternalSyntheticLambda1
            @Override // com.google.crypto.tink.internal.ParametersParser.ParametersParsingFunction
            public final Parameters parseParameters(Serialization serialization) {
                return HpkeProtoSerialization.parseParameters((ProtoParametersSerialization) serialization);
            }
        }, bytesFromPrintableAscii, ProtoParametersSerialization.class);
        PUBLIC_KEY_SERIALIZER = KeySerializer.create(new KeySerializer.KeySerializationFunction() { // from class: com.google.crypto.tink.hybrid.HpkeProtoSerialization$$ExternalSyntheticLambda2
            @Override // com.google.crypto.tink.internal.KeySerializer.KeySerializationFunction
            public final Serialization serializeKey(Key key, SecretKeyAccess secretKeyAccess) {
                return HpkeProtoSerialization.serializePublicKey((HpkePublicKey) key, secretKeyAccess);
            }
        }, HpkePublicKey.class, ProtoKeySerialization.class);
        PUBLIC_KEY_PARSER = KeyParser.create(new KeyParser.KeyParsingFunction() { // from class: com.google.crypto.tink.hybrid.HpkeProtoSerialization$$ExternalSyntheticLambda3
            @Override // com.google.crypto.tink.internal.KeyParser.KeyParsingFunction
            public final Key parseKey(Serialization serialization, SecretKeyAccess secretKeyAccess) {
                return HpkeProtoSerialization.parsePublicKey((ProtoKeySerialization) serialization, secretKeyAccess);
            }
        }, bytesFromPrintableAscii2, ProtoKeySerialization.class);
        PRIVATE_KEY_SERIALIZER = KeySerializer.create(new KeySerializer.KeySerializationFunction() { // from class: com.google.crypto.tink.hybrid.HpkeProtoSerialization$$ExternalSyntheticLambda4
            @Override // com.google.crypto.tink.internal.KeySerializer.KeySerializationFunction
            public final Serialization serializeKey(Key key, SecretKeyAccess secretKeyAccess) {
                return HpkeProtoSerialization.serializePrivateKey((HpkePrivateKey) key, secretKeyAccess);
            }
        }, HpkePrivateKey.class, ProtoKeySerialization.class);
        PRIVATE_KEY_PARSER = KeyParser.create(new KeyParser.KeyParsingFunction() { // from class: com.google.crypto.tink.hybrid.HpkeProtoSerialization$$ExternalSyntheticLambda5
            @Override // com.google.crypto.tink.internal.KeyParser.KeyParsingFunction
            public final Key parseKey(Serialization serialization, SecretKeyAccess secretKeyAccess) {
                return HpkeProtoSerialization.parsePrivateKey((ProtoKeySerialization) serialization, secretKeyAccess);
            }
        }, bytesFromPrintableAscii, ProtoKeySerialization.class);
        VARIANT_TYPE_CONVERTER = EnumTypeProtoConverter.builder().add(OutputPrefixType.RAW, HpkeParameters.Variant.NO_PREFIX).add(OutputPrefixType.TINK, HpkeParameters.Variant.TINK).add(OutputPrefixType.LEGACY, HpkeParameters.Variant.CRUNCHY).add(OutputPrefixType.CRUNCHY, HpkeParameters.Variant.CRUNCHY).build();
        KEM_TYPE_CONVERTER = EnumTypeProtoConverter.builder().add(HpkeKem.DHKEM_P256_HKDF_SHA256, HpkeParameters.KemId.DHKEM_P256_HKDF_SHA256).add(HpkeKem.DHKEM_P384_HKDF_SHA384, HpkeParameters.KemId.DHKEM_P384_HKDF_SHA384).add(HpkeKem.DHKEM_P521_HKDF_SHA512, HpkeParameters.KemId.DHKEM_P521_HKDF_SHA512).add(HpkeKem.DHKEM_X25519_HKDF_SHA256, HpkeParameters.KemId.DHKEM_X25519_HKDF_SHA256).build();
        KDF_TYPE_CONVERTER = EnumTypeProtoConverter.builder().add(HpkeKdf.HKDF_SHA256, HpkeParameters.KdfId.HKDF_SHA256).add(HpkeKdf.HKDF_SHA384, HpkeParameters.KdfId.HKDF_SHA384).add(HpkeKdf.HKDF_SHA512, HpkeParameters.KdfId.HKDF_SHA512).build();
        AEAD_TYPE_CONVERTER = EnumTypeProtoConverter.builder().add(HpkeAead.AES_128_GCM, HpkeParameters.AeadId.AES_128_GCM).add(HpkeAead.AES_256_GCM, HpkeParameters.AeadId.AES_256_GCM).add(HpkeAead.CHACHA20_POLY1305, HpkeParameters.AeadId.CHACHA20_POLY1305).build();
    }

    public static void register() throws GeneralSecurityException {
        register(MutableSerializationRegistry.globalInstance());
    }

    public static void register(MutableSerializationRegistry registry) throws GeneralSecurityException {
        registry.registerParametersSerializer(PARAMETERS_SERIALIZER);
        registry.registerParametersParser(PARAMETERS_PARSER);
        registry.registerKeySerializer(PUBLIC_KEY_SERIALIZER);
        registry.registerKeyParser(PUBLIC_KEY_PARSER);
        registry.registerKeySerializer(PRIVATE_KEY_SERIALIZER);
        registry.registerKeyParser(PRIVATE_KEY_PARSER);
    }

    private static HpkeParams toProtoParameters(HpkeParameters params) throws GeneralSecurityException {
        return (HpkeParams) HpkeParams.newBuilder().setKem((HpkeKem) KEM_TYPE_CONVERTER.toProtoEnum(params.getKemId())).setKdf((HpkeKdf) KDF_TYPE_CONVERTER.toProtoEnum(params.getKdfId())).setAead((HpkeAead) AEAD_TYPE_CONVERTER.toProtoEnum(params.getAeadId())).build();
    }

    private static com.google.crypto.tink.proto.HpkePublicKey toProtoPublicKey(HpkePublicKey key) throws GeneralSecurityException {
        return (com.google.crypto.tink.proto.HpkePublicKey) com.google.crypto.tink.proto.HpkePublicKey.newBuilder().setVersion(0).setParams(toProtoParameters(key.getParameters())).setPublicKey(ByteString.copyFrom(key.getPublicKeyBytes().toByteArray())).build();
    }

    private static com.google.crypto.tink.proto.HpkePrivateKey toProtoPrivateKey(HpkePrivateKey key, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        return (com.google.crypto.tink.proto.HpkePrivateKey) com.google.crypto.tink.proto.HpkePrivateKey.newBuilder().setVersion(0).setPublicKey(toProtoPublicKey(key.getPublicKey())).setPrivateKey(ByteString.copyFrom(key.getPrivateKeyBytes().toByteArray(SecretKeyAccess.requireAccess(access)))).build();
    }

    private static HpkeParameters fromProtoParameters(OutputPrefixType outputPrefixType, HpkeParams protoParams) throws GeneralSecurityException {
        return HpkeParameters.builder().setVariant(VARIANT_TYPE_CONVERTER.fromProtoEnum(outputPrefixType)).setKemId(KEM_TYPE_CONVERTER.fromProtoEnum(protoParams.getKem())).setKdfId(KDF_TYPE_CONVERTER.fromProtoEnum(protoParams.getKdf())).setAeadId(AEAD_TYPE_CONVERTER.fromProtoEnum(protoParams.getAead())).build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoParametersSerialization serializeParameters(HpkeParameters parameters) throws GeneralSecurityException {
        return ProtoParametersSerialization.create((KeyTemplate) KeyTemplate.newBuilder().setTypeUrl(PRIVATE_TYPE_URL).setValue(((HpkeKeyFormat) HpkeKeyFormat.newBuilder().setParams(toProtoParameters(parameters)).build()).toByteString()).setOutputPrefixType((OutputPrefixType) VARIANT_TYPE_CONVERTER.toProtoEnum(parameters.getVariant())).build());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoKeySerialization serializePublicKey(HpkePublicKey key, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        return ProtoKeySerialization.create(PUBLIC_TYPE_URL, toProtoPublicKey(key).toByteString(), KeyData.KeyMaterialType.ASYMMETRIC_PUBLIC, (OutputPrefixType) VARIANT_TYPE_CONVERTER.toProtoEnum(key.getParameters().getVariant()), key.getIdRequirementOrNull());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoKeySerialization serializePrivateKey(HpkePrivateKey key, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        return ProtoKeySerialization.create(PRIVATE_TYPE_URL, toProtoPrivateKey(key, access).toByteString(), KeyData.KeyMaterialType.ASYMMETRIC_PRIVATE, (OutputPrefixType) VARIANT_TYPE_CONVERTER.toProtoEnum(key.getParameters().getVariant()), key.getIdRequirementOrNull());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static HpkeParameters parseParameters(ProtoParametersSerialization serialization) throws GeneralSecurityException {
        if (!serialization.getKeyTemplate().getTypeUrl().equals(PRIVATE_TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to HpkeProtoSerialization.parseParameters: " + serialization.getKeyTemplate().getTypeUrl());
        }
        try {
            return fromProtoParameters(serialization.getKeyTemplate().getOutputPrefixType(), HpkeKeyFormat.parseFrom(serialization.getKeyTemplate().getValue(), ExtensionRegistryLite.getEmptyRegistry()).getParams());
        } catch (InvalidProtocolBufferException e) {
            throw new GeneralSecurityException("Parsing HpkeParameters failed: ", e);
        }
    }

    private static Bytes encodePublicKeyBytes(HpkeParameters.KemId kemId, byte[] publicKeyBytes) throws GeneralSecurityException {
        return Bytes.copyFrom(BigIntegerEncoding.toBigEndianBytesOfFixedLength(BigIntegerEncoding.fromUnsignedBigEndianBytes(publicKeyBytes), HpkeUtil.getEncodedPublicKeyLength(kemId)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static HpkePublicKey parsePublicKey(ProtoKeySerialization serialization, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        if (!serialization.getTypeUrl().equals(PUBLIC_TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to HpkeProtoSerialization.parsePublicKey: " + serialization.getTypeUrl());
        }
        try {
            com.google.crypto.tink.proto.HpkePublicKey from = com.google.crypto.tink.proto.HpkePublicKey.parseFrom(serialization.getValue(), ExtensionRegistryLite.getEmptyRegistry());
            if (from.getVersion() != 0) {
                throw new GeneralSecurityException("Only version 0 keys are accepted");
            }
            HpkeParameters hpkeParametersFromProtoParameters = fromProtoParameters(serialization.getOutputPrefixType(), from.getParams());
            return HpkePublicKey.create(hpkeParametersFromProtoParameters, encodePublicKeyBytes(hpkeParametersFromProtoParameters.getKemId(), from.getPublicKey().toByteArray()), serialization.getIdRequirementOrNull());
        } catch (InvalidProtocolBufferException unused) {
            throw new GeneralSecurityException("Parsing HpkePublicKey failed");
        }
    }

    private static SecretBytes encodePrivateKeyBytes(HpkeParameters.KemId kemId, byte[] privateKeyBytes, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        return SecretBytes.copyFrom(BigIntegerEncoding.toBigEndianBytesOfFixedLength(BigIntegerEncoding.fromUnsignedBigEndianBytes(privateKeyBytes), HpkeUtil.getEncodedPrivateKeyLength(kemId)), SecretKeyAccess.requireAccess(access));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static HpkePrivateKey parsePrivateKey(ProtoKeySerialization serialization, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        if (!serialization.getTypeUrl().equals(PRIVATE_TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to HpkeProtoSerialization.parsePrivateKey: " + serialization.getTypeUrl());
        }
        try {
            com.google.crypto.tink.proto.HpkePrivateKey from = com.google.crypto.tink.proto.HpkePrivateKey.parseFrom(serialization.getValue(), ExtensionRegistryLite.getEmptyRegistry());
            if (from.getVersion() != 0) {
                throw new GeneralSecurityException("Only version 0 keys are accepted");
            }
            com.google.crypto.tink.proto.HpkePublicKey publicKey = from.getPublicKey();
            if (publicKey.getVersion() != 0) {
                throw new GeneralSecurityException("Only version 0 keys are accepted");
            }
            HpkeParameters hpkeParametersFromProtoParameters = fromProtoParameters(serialization.getOutputPrefixType(), publicKey.getParams());
            return HpkePrivateKey.create(HpkePublicKey.create(hpkeParametersFromProtoParameters, encodePublicKeyBytes(hpkeParametersFromProtoParameters.getKemId(), publicKey.getPublicKey().toByteArray()), serialization.getIdRequirementOrNull()), encodePrivateKeyBytes(hpkeParametersFromProtoParameters.getKemId(), from.getPrivateKey().toByteArray(), access));
        } catch (InvalidProtocolBufferException unused) {
            throw new GeneralSecurityException("Parsing HpkePrivateKey failed");
        }
    }

    private HpkeProtoSerialization() {
    }
}
