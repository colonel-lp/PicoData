package com.google.crypto.tink.jwt;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.SecretKeyAccess;
import com.google.crypto.tink.internal.KeyParser;
import com.google.crypto.tink.internal.KeySerializer;
import com.google.crypto.tink.internal.MutableSerializationRegistry;
import com.google.crypto.tink.internal.ParametersParser;
import com.google.crypto.tink.internal.ParametersSerializer;
import com.google.crypto.tink.internal.ProtoKeySerialization;
import com.google.crypto.tink.internal.ProtoParametersSerialization;
import com.google.crypto.tink.internal.Serialization;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.proto.JwtHmacAlgorithm;
import com.google.crypto.tink.proto.JwtHmacKeyFormat;
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
final class JwtHmacProtoSerialization {
    private static final KeyParser<ProtoKeySerialization> KEY_PARSER;
    private static final KeySerializer<JwtHmacKey, ProtoKeySerialization> KEY_SERIALIZER;
    private static final ParametersParser<ProtoParametersSerialization> PARAMETERS_PARSER;
    private static final ParametersSerializer<JwtHmacParameters, ProtoParametersSerialization> PARAMETERS_SERIALIZER;
    private static final String TYPE_URL = "type.googleapis.com/google.crypto.tink.JwtHmacKey";
    private static final Bytes TYPE_URL_BYTES;

    static {
        Bytes bytesFromPrintableAscii = Util.toBytesFromPrintableAscii(TYPE_URL);
        TYPE_URL_BYTES = bytesFromPrintableAscii;
        PARAMETERS_SERIALIZER = ParametersSerializer.create(new ParametersSerializer.ParametersSerializationFunction() { // from class: com.google.crypto.tink.jwt.JwtHmacProtoSerialization$$ExternalSyntheticLambda0
            @Override // com.google.crypto.tink.internal.ParametersSerializer.ParametersSerializationFunction
            public final Serialization serializeParameters(Parameters parameters) {
                return JwtHmacProtoSerialization.serializeParameters((JwtHmacParameters) parameters);
            }
        }, JwtHmacParameters.class, ProtoParametersSerialization.class);
        PARAMETERS_PARSER = ParametersParser.create(new ParametersParser.ParametersParsingFunction() { // from class: com.google.crypto.tink.jwt.JwtHmacProtoSerialization$$ExternalSyntheticLambda1
            @Override // com.google.crypto.tink.internal.ParametersParser.ParametersParsingFunction
            public final Parameters parseParameters(Serialization serialization) {
                return JwtHmacProtoSerialization.parseParameters((ProtoParametersSerialization) serialization);
            }
        }, bytesFromPrintableAscii, ProtoParametersSerialization.class);
        KEY_SERIALIZER = KeySerializer.create(new KeySerializer.KeySerializationFunction() { // from class: com.google.crypto.tink.jwt.JwtHmacProtoSerialization$$ExternalSyntheticLambda2
            @Override // com.google.crypto.tink.internal.KeySerializer.KeySerializationFunction
            public final Serialization serializeKey(Key key, SecretKeyAccess secretKeyAccess) {
                return JwtHmacProtoSerialization.serializeKey((JwtHmacKey) key, secretKeyAccess);
            }
        }, JwtHmacKey.class, ProtoKeySerialization.class);
        KEY_PARSER = KeyParser.create(new KeyParser.KeyParsingFunction() { // from class: com.google.crypto.tink.jwt.JwtHmacProtoSerialization$$ExternalSyntheticLambda3
            @Override // com.google.crypto.tink.internal.KeyParser.KeyParsingFunction
            public final Key parseKey(Serialization serialization, SecretKeyAccess secretKeyAccess) {
                return JwtHmacProtoSerialization.parseKey((ProtoKeySerialization) serialization, secretKeyAccess);
            }
        }, bytesFromPrintableAscii, ProtoKeySerialization.class);
    }

    private static JwtHmacAlgorithm toProtoAlgorithm(JwtHmacParameters.Algorithm hashType) throws GeneralSecurityException {
        if (JwtHmacParameters.Algorithm.HS256.equals(hashType)) {
            return JwtHmacAlgorithm.HS256;
        }
        if (JwtHmacParameters.Algorithm.HS384.equals(hashType)) {
            return JwtHmacAlgorithm.HS384;
        }
        if (JwtHmacParameters.Algorithm.HS512.equals(hashType)) {
            return JwtHmacAlgorithm.HS512;
        }
        throw new GeneralSecurityException("Unable to serialize HashType " + hashType);
    }

    /* JADX INFO: renamed from: com.google.crypto.tink.jwt.JwtHmacProtoSerialization$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$google$crypto$tink$proto$JwtHmacAlgorithm;

        static {
            int[] iArr = new int[JwtHmacAlgorithm.values().length];
            $SwitchMap$com$google$crypto$tink$proto$JwtHmacAlgorithm = iArr;
            try {
                iArr[JwtHmacAlgorithm.HS256.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$proto$JwtHmacAlgorithm[JwtHmacAlgorithm.HS384.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$proto$JwtHmacAlgorithm[JwtHmacAlgorithm.HS512.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    private static JwtHmacParameters.Algorithm toAlgorithm(JwtHmacAlgorithm hashType) throws GeneralSecurityException {
        int i = AnonymousClass1.$SwitchMap$com$google$crypto$tink$proto$JwtHmacAlgorithm[hashType.ordinal()];
        if (i == 1) {
            return JwtHmacParameters.Algorithm.HS256;
        }
        if (i == 2) {
            return JwtHmacParameters.Algorithm.HS384;
        }
        if (i == 3) {
            return JwtHmacParameters.Algorithm.HS512;
        }
        throw new GeneralSecurityException("Unable to parse HashType: " + hashType.getNumber());
    }

    private static JwtHmacKeyFormat serializeToJwtHmacKeyFormat(JwtHmacParameters parameters) throws GeneralSecurityException {
        if (parameters.getKidStrategy().equals(JwtHmacParameters.KidStrategy.CUSTOM)) {
            throw new GeneralSecurityException("Unable to serialize Parameters object with KidStrategy CUSTOM");
        }
        return (JwtHmacKeyFormat) JwtHmacKeyFormat.newBuilder().setVersion(0).setAlgorithm(toProtoAlgorithm(parameters.getAlgorithm())).setKeySize(parameters.getKeySizeBytes()).build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoParametersSerialization serializeParameters(JwtHmacParameters parameters) throws GeneralSecurityException {
        OutputPrefixType outputPrefixType = OutputPrefixType.TINK;
        if (parameters.getKidStrategy().equals(JwtHmacParameters.KidStrategy.IGNORED)) {
            outputPrefixType = OutputPrefixType.RAW;
        }
        return ProtoParametersSerialization.create((KeyTemplate) KeyTemplate.newBuilder().setTypeUrl(TYPE_URL).setValue(serializeToJwtHmacKeyFormat(parameters).toByteString()).setOutputPrefixType(outputPrefixType).build());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoKeySerialization serializeKey(JwtHmacKey key, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        OutputPrefixType outputPrefixType;
        com.google.crypto.tink.proto.JwtHmacKey.Builder builderNewBuilder = com.google.crypto.tink.proto.JwtHmacKey.newBuilder();
        builderNewBuilder.setVersion(0).setAlgorithm(toProtoAlgorithm(key.getParameters().getAlgorithm())).setKeyValue(ByteString.copyFrom(key.getKeyBytes().toByteArray(SecretKeyAccess.requireAccess(access))));
        if (key.getParameters().getKidStrategy().equals(JwtHmacParameters.KidStrategy.CUSTOM)) {
            builderNewBuilder.setCustomKid(com.google.crypto.tink.proto.JwtHmacKey.CustomKid.newBuilder().setValue(key.getKid().get()));
            outputPrefixType = OutputPrefixType.RAW;
        } else {
            outputPrefixType = null;
        }
        if (key.getParameters().getKidStrategy().equals(JwtHmacParameters.KidStrategy.IGNORED)) {
            outputPrefixType = OutputPrefixType.RAW;
        }
        if (key.getParameters().getKidStrategy().equals(JwtHmacParameters.KidStrategy.BASE64_ENCODED_KEY_ID)) {
            outputPrefixType = OutputPrefixType.TINK;
        }
        if (outputPrefixType == null) {
            throw new GeneralSecurityException("Unknown KID Strategy in " + key.getParameters().getKidStrategy());
        }
        return ProtoKeySerialization.create(TYPE_URL, ((com.google.crypto.tink.proto.JwtHmacKey) builderNewBuilder.build()).toByteString(), KeyData.KeyMaterialType.SYMMETRIC, outputPrefixType, key.getIdRequirementOrNull());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static JwtHmacParameters parseParameters(ProtoParametersSerialization serialization) throws GeneralSecurityException {
        if (!serialization.getKeyTemplate().getTypeUrl().equals(TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to JwtHmacProtoSerialization.parseParameters: " + serialization.getKeyTemplate().getTypeUrl());
        }
        try {
            JwtHmacKeyFormat from = JwtHmacKeyFormat.parseFrom(serialization.getKeyTemplate().getValue(), ExtensionRegistryLite.getEmptyRegistry());
            if (from.getVersion() != 0) {
                throw new GeneralSecurityException("Parsing HmacParameters failed: unknown Version " + from.getVersion());
            }
            JwtHmacParameters.KidStrategy kidStrategy = serialization.getKeyTemplate().getOutputPrefixType().equals(OutputPrefixType.TINK) ? JwtHmacParameters.KidStrategy.BASE64_ENCODED_KEY_ID : null;
            if (serialization.getKeyTemplate().getOutputPrefixType().equals(OutputPrefixType.RAW)) {
                kidStrategy = JwtHmacParameters.KidStrategy.IGNORED;
            }
            if (kidStrategy == null) {
                throw new GeneralSecurityException("Invalid OutputPrefixType for JwtHmacKeyFormat");
            }
            return JwtHmacParameters.builder().setAlgorithm(toAlgorithm(from.getAlgorithm())).setKeySizeBytes(from.getKeySize()).setKidStrategy(kidStrategy).build();
        } catch (InvalidProtocolBufferException e) {
            throw new GeneralSecurityException("Parsing HmacParameters failed: ", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static JwtHmacKey parseKey(ProtoKeySerialization serialization, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        if (!serialization.getTypeUrl().equals(TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to HmacProtoSerialization.parseKey");
        }
        try {
            com.google.crypto.tink.proto.JwtHmacKey from = com.google.crypto.tink.proto.JwtHmacKey.parseFrom(serialization.getValue(), ExtensionRegistryLite.getEmptyRegistry());
            if (from.getVersion() != 0) {
                throw new GeneralSecurityException("Only version 0 keys are accepted");
            }
            JwtHmacParameters.Builder builder = JwtHmacParameters.builder();
            JwtHmacKey.Builder builder2 = JwtHmacKey.builder();
            if (serialization.getOutputPrefixType().equals(OutputPrefixType.TINK)) {
                if (from.hasCustomKid()) {
                    throw new GeneralSecurityException("Keys serialized with OutputPrefixType TINK should not have a custom kid");
                }
                Integer idRequirementOrNull = serialization.getIdRequirementOrNull();
                if (idRequirementOrNull == null) {
                    throw new GeneralSecurityException("Keys serialized with OutputPrefixType TINK need an ID Requirement");
                }
                builder.setKidStrategy(JwtHmacParameters.KidStrategy.BASE64_ENCODED_KEY_ID);
                builder2.setIdRequirement(idRequirementOrNull.intValue());
            } else if (serialization.getOutputPrefixType().equals(OutputPrefixType.RAW)) {
                if (from.hasCustomKid()) {
                    builder.setKidStrategy(JwtHmacParameters.KidStrategy.CUSTOM);
                    builder2.setCustomKid(from.getCustomKid().getValue());
                } else {
                    builder.setKidStrategy(JwtHmacParameters.KidStrategy.IGNORED);
                }
            }
            builder.setAlgorithm(toAlgorithm(from.getAlgorithm()));
            builder.setKeySizeBytes(from.getKeyValue().size());
            return builder2.setKeyBytes(SecretBytes.copyFrom(from.getKeyValue().toByteArray(), SecretKeyAccess.requireAccess(access))).setParameters(builder.build()).build();
        } catch (InvalidProtocolBufferException | IllegalArgumentException unused) {
            throw new GeneralSecurityException("Parsing HmacKey failed");
        }
    }

    public static void register() throws GeneralSecurityException {
        register(MutableSerializationRegistry.globalInstance());
    }

    public static void register(MutableSerializationRegistry registry) throws GeneralSecurityException {
        registry.registerParametersSerializer(PARAMETERS_SERIALIZER);
        registry.registerParametersParser(PARAMETERS_PARSER);
        registry.registerKeySerializer(KEY_SERIALIZER);
        registry.registerKeyParser(KEY_PARSER);
    }

    private JwtHmacProtoSerialization() {
    }
}
