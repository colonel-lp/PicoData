package com.google.crypto.tink.jwt;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.SecretKeyAccess;
import com.google.crypto.tink.internal.BigIntegerEncoding;
import com.google.crypto.tink.internal.KeyParser;
import com.google.crypto.tink.internal.KeySerializer;
import com.google.crypto.tink.internal.MutableSerializationRegistry;
import com.google.crypto.tink.internal.ParametersParser;
import com.google.crypto.tink.internal.ParametersSerializer;
import com.google.crypto.tink.internal.ProtoKeySerialization;
import com.google.crypto.tink.internal.ProtoParametersSerialization;
import com.google.crypto.tink.internal.Serialization;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.proto.JwtEcdsaAlgorithm;
import com.google.crypto.tink.proto.JwtEcdsaKeyFormat;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.proto.KeyTemplate;
import com.google.crypto.tink.proto.OutputPrefixType;
import com.google.crypto.tink.shaded.protobuf.ByteString;
import com.google.crypto.tink.shaded.protobuf.ExtensionRegistryLite;
import com.google.crypto.tink.shaded.protobuf.InvalidProtocolBufferException;
import com.google.crypto.tink.util.Bytes;
import com.google.crypto.tink.util.SecretBigInteger;
import java.security.GeneralSecurityException;
import java.security.spec.ECPoint;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
final class JwtEcdsaProtoSerialization {
    private static final ParametersParser<ProtoParametersSerialization> PARAMETERS_PARSER;
    private static final ParametersSerializer<JwtEcdsaParameters, ProtoParametersSerialization> PARAMETERS_SERIALIZER;
    private static final KeyParser<ProtoKeySerialization> PRIVATE_KEY_PARSER;
    private static final KeySerializer<JwtEcdsaPrivateKey, ProtoKeySerialization> PRIVATE_KEY_SERIALIZER;
    private static final KeyParser<ProtoKeySerialization> PUBLIC_KEY_PARSER;
    private static final KeySerializer<JwtEcdsaPublicKey, ProtoKeySerialization> PUBLIC_KEY_SERIALIZER;
    private static final String PUBLIC_TYPE_URL = "type.googleapis.com/google.crypto.tink.JwtEcdsaPublicKey";
    private static final Bytes PUBLIC_TYPE_URL_BYTES;
    private static final String TYPE_URL = "type.googleapis.com/google.crypto.tink.JwtEcdsaPrivateKey";
    private static final Bytes TYPE_URL_BYTES;

    static {
        Bytes bytesFromPrintableAscii = Util.toBytesFromPrintableAscii(TYPE_URL);
        TYPE_URL_BYTES = bytesFromPrintableAscii;
        Bytes bytesFromPrintableAscii2 = Util.toBytesFromPrintableAscii(PUBLIC_TYPE_URL);
        PUBLIC_TYPE_URL_BYTES = bytesFromPrintableAscii2;
        PARAMETERS_SERIALIZER = ParametersSerializer.create(new ParametersSerializer.ParametersSerializationFunction() { // from class: com.google.crypto.tink.jwt.JwtEcdsaProtoSerialization$$ExternalSyntheticLambda0
            @Override // com.google.crypto.tink.internal.ParametersSerializer.ParametersSerializationFunction
            public final Serialization serializeParameters(Parameters parameters) {
                return JwtEcdsaProtoSerialization.serializeParameters((JwtEcdsaParameters) parameters);
            }
        }, JwtEcdsaParameters.class, ProtoParametersSerialization.class);
        PARAMETERS_PARSER = ParametersParser.create(new ParametersParser.ParametersParsingFunction() { // from class: com.google.crypto.tink.jwt.JwtEcdsaProtoSerialization$$ExternalSyntheticLambda1
            @Override // com.google.crypto.tink.internal.ParametersParser.ParametersParsingFunction
            public final Parameters parseParameters(Serialization serialization) {
                return JwtEcdsaProtoSerialization.parseParameters((ProtoParametersSerialization) serialization);
            }
        }, bytesFromPrintableAscii, ProtoParametersSerialization.class);
        PUBLIC_KEY_SERIALIZER = KeySerializer.create(new KeySerializer.KeySerializationFunction() { // from class: com.google.crypto.tink.jwt.JwtEcdsaProtoSerialization$$ExternalSyntheticLambda2
            @Override // com.google.crypto.tink.internal.KeySerializer.KeySerializationFunction
            public final Serialization serializeKey(Key key, SecretKeyAccess secretKeyAccess) {
                return JwtEcdsaProtoSerialization.serializePublicKey((JwtEcdsaPublicKey) key, secretKeyAccess);
            }
        }, JwtEcdsaPublicKey.class, ProtoKeySerialization.class);
        PUBLIC_KEY_PARSER = KeyParser.create(new KeyParser.KeyParsingFunction() { // from class: com.google.crypto.tink.jwt.JwtEcdsaProtoSerialization$$ExternalSyntheticLambda3
            @Override // com.google.crypto.tink.internal.KeyParser.KeyParsingFunction
            public final Key parseKey(Serialization serialization, SecretKeyAccess secretKeyAccess) {
                return JwtEcdsaProtoSerialization.parsePublicKey((ProtoKeySerialization) serialization, secretKeyAccess);
            }
        }, bytesFromPrintableAscii2, ProtoKeySerialization.class);
        PRIVATE_KEY_SERIALIZER = KeySerializer.create(new KeySerializer.KeySerializationFunction() { // from class: com.google.crypto.tink.jwt.JwtEcdsaProtoSerialization$$ExternalSyntheticLambda4
            @Override // com.google.crypto.tink.internal.KeySerializer.KeySerializationFunction
            public final Serialization serializeKey(Key key, SecretKeyAccess secretKeyAccess) {
                return JwtEcdsaProtoSerialization.serializePrivateKey((JwtEcdsaPrivateKey) key, secretKeyAccess);
            }
        }, JwtEcdsaPrivateKey.class, ProtoKeySerialization.class);
        PRIVATE_KEY_PARSER = KeyParser.create(new KeyParser.KeyParsingFunction() { // from class: com.google.crypto.tink.jwt.JwtEcdsaProtoSerialization$$ExternalSyntheticLambda5
            @Override // com.google.crypto.tink.internal.KeyParser.KeyParsingFunction
            public final Key parseKey(Serialization serialization, SecretKeyAccess secretKeyAccess) {
                return JwtEcdsaProtoSerialization.parsePrivateKey((ProtoKeySerialization) serialization, secretKeyAccess);
            }
        }, bytesFromPrintableAscii, ProtoKeySerialization.class);
    }

    private static JwtEcdsaAlgorithm toProtoAlgorithm(JwtEcdsaParameters.Algorithm algorithm) throws GeneralSecurityException {
        if (JwtEcdsaParameters.Algorithm.ES256.equals(algorithm)) {
            return JwtEcdsaAlgorithm.ES256;
        }
        if (JwtEcdsaParameters.Algorithm.ES384.equals(algorithm)) {
            return JwtEcdsaAlgorithm.ES384;
        }
        if (JwtEcdsaParameters.Algorithm.ES512.equals(algorithm)) {
            return JwtEcdsaAlgorithm.ES512;
        }
        throw new GeneralSecurityException("Unable to serialize algorithm: " + algorithm);
    }

    /* JADX INFO: renamed from: com.google.crypto.tink.jwt.JwtEcdsaProtoSerialization$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$google$crypto$tink$proto$JwtEcdsaAlgorithm;

        static {
            int[] iArr = new int[JwtEcdsaAlgorithm.values().length];
            $SwitchMap$com$google$crypto$tink$proto$JwtEcdsaAlgorithm = iArr;
            try {
                iArr[JwtEcdsaAlgorithm.ES256.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$proto$JwtEcdsaAlgorithm[JwtEcdsaAlgorithm.ES384.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$proto$JwtEcdsaAlgorithm[JwtEcdsaAlgorithm.ES512.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    private static JwtEcdsaParameters.Algorithm toAlgorithm(JwtEcdsaAlgorithm algorithm) throws GeneralSecurityException {
        int i = AnonymousClass1.$SwitchMap$com$google$crypto$tink$proto$JwtEcdsaAlgorithm[algorithm.ordinal()];
        if (i == 1) {
            return JwtEcdsaParameters.Algorithm.ES256;
        }
        if (i == 2) {
            return JwtEcdsaParameters.Algorithm.ES384;
        }
        if (i == 3) {
            return JwtEcdsaParameters.Algorithm.ES512;
        }
        throw new GeneralSecurityException("Unable to parse algorithm: " + algorithm.getNumber());
    }

    private static JwtEcdsaKeyFormat serializeToJwtEcdsaKeyFormat(JwtEcdsaParameters parameters) throws GeneralSecurityException {
        if (!parameters.getKidStrategy().equals(JwtEcdsaParameters.KidStrategy.IGNORED) && !parameters.getKidStrategy().equals(JwtEcdsaParameters.KidStrategy.BASE64_ENCODED_KEY_ID)) {
            throw new GeneralSecurityException("Unable to serialize Parameters object with KidStrategy " + parameters.getKidStrategy());
        }
        return (JwtEcdsaKeyFormat) JwtEcdsaKeyFormat.newBuilder().setVersion(0).setAlgorithm(toProtoAlgorithm(parameters.getAlgorithm())).build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoParametersSerialization serializeParameters(JwtEcdsaParameters parameters) throws GeneralSecurityException {
        OutputPrefixType outputPrefixType = OutputPrefixType.TINK;
        if (parameters.getKidStrategy().equals(JwtEcdsaParameters.KidStrategy.IGNORED)) {
            outputPrefixType = OutputPrefixType.RAW;
        }
        return ProtoParametersSerialization.create((KeyTemplate) KeyTemplate.newBuilder().setTypeUrl(TYPE_URL).setValue(serializeToJwtEcdsaKeyFormat(parameters).toByteString()).setOutputPrefixType(outputPrefixType).build());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static JwtEcdsaParameters parseParameters(ProtoParametersSerialization serialization) throws GeneralSecurityException {
        if (!serialization.getKeyTemplate().getTypeUrl().equals(TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to JwtEcdsaParameters.parseParameters: " + serialization.getKeyTemplate().getTypeUrl());
        }
        try {
            JwtEcdsaKeyFormat from = JwtEcdsaKeyFormat.parseFrom(serialization.getKeyTemplate().getValue(), ExtensionRegistryLite.getEmptyRegistry());
            if (from.getVersion() != 0) {
                throw new GeneralSecurityException("Parsing HmacParameters failed: unknown Version " + from.getVersion());
            }
            JwtEcdsaParameters.KidStrategy kidStrategy = serialization.getKeyTemplate().getOutputPrefixType().equals(OutputPrefixType.TINK) ? JwtEcdsaParameters.KidStrategy.BASE64_ENCODED_KEY_ID : null;
            if (serialization.getKeyTemplate().getOutputPrefixType().equals(OutputPrefixType.RAW)) {
                kidStrategy = JwtEcdsaParameters.KidStrategy.IGNORED;
            }
            if (kidStrategy == null) {
                throw new GeneralSecurityException("Invalid OutputPrefixType for JwtHmacKeyFormat");
            }
            return JwtEcdsaParameters.builder().setAlgorithm(toAlgorithm(from.getAlgorithm())).setKidStrategy(kidStrategy).build();
        } catch (InvalidProtocolBufferException e) {
            throw new GeneralSecurityException("Parsing JwtEcdsaKeyFormat failed: ", e);
        }
    }

    private static int getEncodingLength(JwtEcdsaParameters.Algorithm algorithm) throws GeneralSecurityException {
        if (algorithm.equals(JwtEcdsaParameters.Algorithm.ES256)) {
            return 33;
        }
        if (algorithm.equals(JwtEcdsaParameters.Algorithm.ES384)) {
            return 49;
        }
        if (algorithm.equals(JwtEcdsaParameters.Algorithm.ES512)) {
            return 67;
        }
        throw new GeneralSecurityException("Unknown algorithm: " + algorithm);
    }

    private static OutputPrefixType toProtoOutputPrefixType(JwtEcdsaParameters parameters) {
        if (parameters.getKidStrategy().equals(JwtEcdsaParameters.KidStrategy.BASE64_ENCODED_KEY_ID)) {
            return OutputPrefixType.TINK;
        }
        return OutputPrefixType.RAW;
    }

    private static com.google.crypto.tink.proto.JwtEcdsaPublicKey serializePublicKey(JwtEcdsaPublicKey key) throws GeneralSecurityException {
        int encodingLength = getEncodingLength(key.getParameters().getAlgorithm());
        ECPoint publicPoint = key.getPublicPoint();
        com.google.crypto.tink.proto.JwtEcdsaPublicKey.Builder y = com.google.crypto.tink.proto.JwtEcdsaPublicKey.newBuilder().setVersion(0).setAlgorithm(toProtoAlgorithm(key.getParameters().getAlgorithm())).setX(ByteString.copyFrom(BigIntegerEncoding.toBigEndianBytesOfFixedLength(publicPoint.getAffineX(), encodingLength))).setY(ByteString.copyFrom(BigIntegerEncoding.toBigEndianBytesOfFixedLength(publicPoint.getAffineY(), encodingLength)));
        if (key.getParameters().getKidStrategy().equals(JwtEcdsaParameters.KidStrategy.CUSTOM)) {
            y.setCustomKid((com.google.crypto.tink.proto.JwtEcdsaPublicKey.CustomKid) com.google.crypto.tink.proto.JwtEcdsaPublicKey.CustomKid.newBuilder().setValue(key.getKid().get()).build());
        }
        return (com.google.crypto.tink.proto.JwtEcdsaPublicKey) y.build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoKeySerialization serializePublicKey(JwtEcdsaPublicKey key, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        return ProtoKeySerialization.create(PUBLIC_TYPE_URL, serializePublicKey(key).toByteString(), KeyData.KeyMaterialType.ASYMMETRIC_PUBLIC, toProtoOutputPrefixType(key.getParameters()), key.getIdRequirementOrNull());
    }

    private static JwtEcdsaPublicKey parsePublicKeyFromProto(com.google.crypto.tink.proto.JwtEcdsaPublicKey protoKey, OutputPrefixType outputPrefixType, @Nullable Integer idRequirement) throws GeneralSecurityException {
        if (protoKey.getVersion() != 0) {
            throw new GeneralSecurityException("Only version 0 keys are accepted");
        }
        JwtEcdsaParameters.Builder builder = JwtEcdsaParameters.builder();
        JwtEcdsaPublicKey.Builder builder2 = JwtEcdsaPublicKey.builder();
        if (outputPrefixType.equals(OutputPrefixType.TINK)) {
            if (protoKey.hasCustomKid()) {
                throw new GeneralSecurityException("Keys serialized with OutputPrefixType TINK should not have a custom kid");
            }
            if (idRequirement == null) {
                throw new GeneralSecurityException("Keys serialized with OutputPrefixType TINK need an ID Requirement");
            }
            builder.setKidStrategy(JwtEcdsaParameters.KidStrategy.BASE64_ENCODED_KEY_ID);
            builder2.setIdRequirement(idRequirement);
        } else if (outputPrefixType.equals(OutputPrefixType.RAW)) {
            if (protoKey.hasCustomKid()) {
                builder.setKidStrategy(JwtEcdsaParameters.KidStrategy.CUSTOM);
                builder2.setCustomKid(protoKey.getCustomKid().getValue());
            } else {
                builder.setKidStrategy(JwtEcdsaParameters.KidStrategy.IGNORED);
            }
        }
        builder.setAlgorithm(toAlgorithm(protoKey.getAlgorithm()));
        builder2.setPublicPoint(new ECPoint(BigIntegerEncoding.fromUnsignedBigEndianBytes(protoKey.getX().toByteArray()), BigIntegerEncoding.fromUnsignedBigEndianBytes(protoKey.getY().toByteArray())));
        return builder2.setParameters(builder.build()).build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static JwtEcdsaPublicKey parsePublicKey(ProtoKeySerialization serialization, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        if (!serialization.getTypeUrl().equals(PUBLIC_TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to EcdsaProtoSerialization.parsePublicKey: " + serialization.getTypeUrl());
        }
        try {
            return parsePublicKeyFromProto(com.google.crypto.tink.proto.JwtEcdsaPublicKey.parseFrom(serialization.getValue(), ExtensionRegistryLite.getEmptyRegistry()), serialization.getOutputPrefixType(), serialization.getIdRequirementOrNull());
        } catch (InvalidProtocolBufferException unused) {
            throw new GeneralSecurityException("Parsing EcdsaPublicKey failed");
        }
    }

    private static com.google.crypto.tink.proto.JwtEcdsaPrivateKey serializePrivateKeyToProto(JwtEcdsaPrivateKey key, SecretKeyAccess access) throws GeneralSecurityException {
        return (com.google.crypto.tink.proto.JwtEcdsaPrivateKey) com.google.crypto.tink.proto.JwtEcdsaPrivateKey.newBuilder().setPublicKey(serializePublicKey(key.getPublicKey())).setKeyValue(ByteString.copyFrom(BigIntegerEncoding.toBigEndianBytesOfFixedLength(key.getPrivateValue().getBigInteger(access), getEncodingLength(key.getParameters().getAlgorithm())))).build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoKeySerialization serializePrivateKey(JwtEcdsaPrivateKey key, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        return ProtoKeySerialization.create(TYPE_URL, serializePrivateKeyToProto(key, SecretKeyAccess.requireAccess(access)).toByteString(), KeyData.KeyMaterialType.ASYMMETRIC_PRIVATE, toProtoOutputPrefixType(key.getParameters()), key.getIdRequirementOrNull());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static JwtEcdsaPrivateKey parsePrivateKey(ProtoKeySerialization serialization, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        if (!serialization.getTypeUrl().equals(TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to EcdsaProtoSerialization.parsePublicKey: " + serialization.getTypeUrl());
        }
        try {
            com.google.crypto.tink.proto.JwtEcdsaPrivateKey from = com.google.crypto.tink.proto.JwtEcdsaPrivateKey.parseFrom(serialization.getValue(), ExtensionRegistryLite.getEmptyRegistry());
            if (from.getVersion() != 0) {
                throw new GeneralSecurityException("Only version 0 keys are accepted");
            }
            return JwtEcdsaPrivateKey.create(parsePublicKeyFromProto(from.getPublicKey(), serialization.getOutputPrefixType(), serialization.getIdRequirementOrNull()), SecretBigInteger.fromBigInteger(BigIntegerEncoding.fromUnsignedBigEndianBytes(from.getKeyValue().toByteArray()), SecretKeyAccess.requireAccess(access)));
        } catch (InvalidProtocolBufferException unused) {
            throw new GeneralSecurityException("Parsing EcdsaPrivateKey failed");
        }
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

    private JwtEcdsaProtoSerialization() {
    }
}
