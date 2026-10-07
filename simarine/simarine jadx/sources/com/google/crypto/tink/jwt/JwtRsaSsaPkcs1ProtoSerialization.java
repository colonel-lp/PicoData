package com.google.crypto.tink.jwt;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.SecretKeyAccess;
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
import com.google.crypto.tink.proto.JwtRsaSsaPkcs1Algorithm;
import com.google.crypto.tink.proto.JwtRsaSsaPkcs1KeyFormat;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.proto.KeyTemplate;
import com.google.crypto.tink.proto.OutputPrefixType;
import com.google.crypto.tink.shaded.protobuf.ByteString;
import com.google.crypto.tink.shaded.protobuf.ExtensionRegistryLite;
import com.google.crypto.tink.shaded.protobuf.InvalidProtocolBufferException;
import com.google.crypto.tink.util.Bytes;
import com.google.crypto.tink.util.SecretBigInteger;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
final class JwtRsaSsaPkcs1ProtoSerialization {
    private static final EnumTypeProtoConverter<JwtRsaSsaPkcs1Algorithm, JwtRsaSsaPkcs1Parameters.Algorithm> ALGORITHM_CONVERTER;
    private static final ParametersParser<ProtoParametersSerialization> PARAMETERS_PARSER;
    private static final ParametersSerializer<JwtRsaSsaPkcs1Parameters, ProtoParametersSerialization> PARAMETERS_SERIALIZER;
    private static final KeyParser<ProtoKeySerialization> PRIVATE_KEY_PARSER;
    private static final KeySerializer<JwtRsaSsaPkcs1PrivateKey, ProtoKeySerialization> PRIVATE_KEY_SERIALIZER;
    private static final String PRIVATE_TYPE_URL = "type.googleapis.com/google.crypto.tink.JwtRsaSsaPkcs1PrivateKey";
    private static final Bytes PRIVATE_TYPE_URL_BYTES;
    private static final KeyParser<ProtoKeySerialization> PUBLIC_KEY_PARSER;
    private static final KeySerializer<JwtRsaSsaPkcs1PublicKey, ProtoKeySerialization> PUBLIC_KEY_SERIALIZER;
    private static final String PUBLIC_TYPE_URL = "type.googleapis.com/google.crypto.tink.JwtRsaSsaPkcs1PublicKey";
    private static final Bytes PUBLIC_TYPE_URL_BYTES;

    static {
        Bytes bytesFromPrintableAscii = Util.toBytesFromPrintableAscii(PRIVATE_TYPE_URL);
        PRIVATE_TYPE_URL_BYTES = bytesFromPrintableAscii;
        Bytes bytesFromPrintableAscii2 = Util.toBytesFromPrintableAscii(PUBLIC_TYPE_URL);
        PUBLIC_TYPE_URL_BYTES = bytesFromPrintableAscii2;
        PARAMETERS_SERIALIZER = ParametersSerializer.create(new ParametersSerializer.ParametersSerializationFunction() { // from class: com.google.crypto.tink.jwt.JwtRsaSsaPkcs1ProtoSerialization$$ExternalSyntheticLambda0
            @Override // com.google.crypto.tink.internal.ParametersSerializer.ParametersSerializationFunction
            public final Serialization serializeParameters(Parameters parameters) {
                return JwtRsaSsaPkcs1ProtoSerialization.serializeParameters((JwtRsaSsaPkcs1Parameters) parameters);
            }
        }, JwtRsaSsaPkcs1Parameters.class, ProtoParametersSerialization.class);
        PARAMETERS_PARSER = ParametersParser.create(new ParametersParser.ParametersParsingFunction() { // from class: com.google.crypto.tink.jwt.JwtRsaSsaPkcs1ProtoSerialization$$ExternalSyntheticLambda1
            @Override // com.google.crypto.tink.internal.ParametersParser.ParametersParsingFunction
            public final Parameters parseParameters(Serialization serialization) {
                return JwtRsaSsaPkcs1ProtoSerialization.parseParameters((ProtoParametersSerialization) serialization);
            }
        }, bytesFromPrintableAscii, ProtoParametersSerialization.class);
        PUBLIC_KEY_SERIALIZER = KeySerializer.create(new KeySerializer.KeySerializationFunction() { // from class: com.google.crypto.tink.jwt.JwtRsaSsaPkcs1ProtoSerialization$$ExternalSyntheticLambda2
            @Override // com.google.crypto.tink.internal.KeySerializer.KeySerializationFunction
            public final Serialization serializeKey(Key key, SecretKeyAccess secretKeyAccess) {
                return JwtRsaSsaPkcs1ProtoSerialization.serializePublicKey((JwtRsaSsaPkcs1PublicKey) key, secretKeyAccess);
            }
        }, JwtRsaSsaPkcs1PublicKey.class, ProtoKeySerialization.class);
        PUBLIC_KEY_PARSER = KeyParser.create(new KeyParser.KeyParsingFunction() { // from class: com.google.crypto.tink.jwt.JwtRsaSsaPkcs1ProtoSerialization$$ExternalSyntheticLambda3
            @Override // com.google.crypto.tink.internal.KeyParser.KeyParsingFunction
            public final Key parseKey(Serialization serialization, SecretKeyAccess secretKeyAccess) {
                return JwtRsaSsaPkcs1ProtoSerialization.parsePublicKey((ProtoKeySerialization) serialization, secretKeyAccess);
            }
        }, bytesFromPrintableAscii2, ProtoKeySerialization.class);
        PRIVATE_KEY_SERIALIZER = KeySerializer.create(new KeySerializer.KeySerializationFunction() { // from class: com.google.crypto.tink.jwt.JwtRsaSsaPkcs1ProtoSerialization$$ExternalSyntheticLambda4
            @Override // com.google.crypto.tink.internal.KeySerializer.KeySerializationFunction
            public final Serialization serializeKey(Key key, SecretKeyAccess secretKeyAccess) {
                return JwtRsaSsaPkcs1ProtoSerialization.serializePrivateKey((JwtRsaSsaPkcs1PrivateKey) key, secretKeyAccess);
            }
        }, JwtRsaSsaPkcs1PrivateKey.class, ProtoKeySerialization.class);
        PRIVATE_KEY_PARSER = KeyParser.create(new KeyParser.KeyParsingFunction() { // from class: com.google.crypto.tink.jwt.JwtRsaSsaPkcs1ProtoSerialization$$ExternalSyntheticLambda5
            @Override // com.google.crypto.tink.internal.KeyParser.KeyParsingFunction
            public final Key parseKey(Serialization serialization, SecretKeyAccess secretKeyAccess) {
                return JwtRsaSsaPkcs1ProtoSerialization.parsePrivateKey((ProtoKeySerialization) serialization, secretKeyAccess);
            }
        }, bytesFromPrintableAscii, ProtoKeySerialization.class);
        ALGORITHM_CONVERTER = EnumTypeProtoConverter.builder().add(JwtRsaSsaPkcs1Algorithm.RS256, JwtRsaSsaPkcs1Parameters.Algorithm.RS256).add(JwtRsaSsaPkcs1Algorithm.RS384, JwtRsaSsaPkcs1Parameters.Algorithm.RS384).add(JwtRsaSsaPkcs1Algorithm.RS512, JwtRsaSsaPkcs1Parameters.Algorithm.RS512).build();
    }

    private static OutputPrefixType toProtoOutputPrefixType(JwtRsaSsaPkcs1Parameters parameters) {
        if (parameters.getKidStrategy().equals(JwtRsaSsaPkcs1Parameters.KidStrategy.BASE64_ENCODED_KEY_ID)) {
            return OutputPrefixType.TINK;
        }
        return OutputPrefixType.RAW;
    }

    private static ByteString encodeBigInteger(BigInteger i) {
        return ByteString.copyFrom(BigIntegerEncoding.toBigEndianBytes(i));
    }

    private static JwtRsaSsaPkcs1KeyFormat getProtoKeyFormat(JwtRsaSsaPkcs1Parameters parameters) throws GeneralSecurityException {
        if (!parameters.getKidStrategy().equals(JwtRsaSsaPkcs1Parameters.KidStrategy.IGNORED) && !parameters.getKidStrategy().equals(JwtRsaSsaPkcs1Parameters.KidStrategy.BASE64_ENCODED_KEY_ID)) {
            throw new GeneralSecurityException("Unable to serialize Parameters object with KidStrategy " + parameters.getKidStrategy());
        }
        return (JwtRsaSsaPkcs1KeyFormat) JwtRsaSsaPkcs1KeyFormat.newBuilder().setVersion(0).setAlgorithm((JwtRsaSsaPkcs1Algorithm) ALGORITHM_CONVERTER.toProtoEnum(parameters.getAlgorithm())).setModulusSizeInBits(parameters.getModulusSizeBits()).setPublicExponent(encodeBigInteger(parameters.getPublicExponent())).build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoParametersSerialization serializeParameters(JwtRsaSsaPkcs1Parameters parameters) throws GeneralSecurityException {
        return ProtoParametersSerialization.create((KeyTemplate) KeyTemplate.newBuilder().setTypeUrl(PRIVATE_TYPE_URL).setValue(getProtoKeyFormat(parameters).toByteString()).setOutputPrefixType(toProtoOutputPrefixType(parameters)).build());
    }

    private static com.google.crypto.tink.proto.JwtRsaSsaPkcs1PublicKey getProtoPublicKey(JwtRsaSsaPkcs1PublicKey key) throws GeneralSecurityException {
        com.google.crypto.tink.proto.JwtRsaSsaPkcs1PublicKey.Builder e = com.google.crypto.tink.proto.JwtRsaSsaPkcs1PublicKey.newBuilder().setVersion(0).setAlgorithm((JwtRsaSsaPkcs1Algorithm) ALGORITHM_CONVERTER.toProtoEnum(key.getParameters().getAlgorithm())).setN(encodeBigInteger(key.getModulus())).setE(encodeBigInteger(key.getParameters().getPublicExponent()));
        if (key.getParameters().getKidStrategy().equals(JwtRsaSsaPkcs1Parameters.KidStrategy.CUSTOM)) {
            e.setCustomKid((com.google.crypto.tink.proto.JwtRsaSsaPkcs1PublicKey.CustomKid) com.google.crypto.tink.proto.JwtRsaSsaPkcs1PublicKey.CustomKid.newBuilder().setValue(key.getKid().get()).build());
        }
        return (com.google.crypto.tink.proto.JwtRsaSsaPkcs1PublicKey) e.build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoKeySerialization serializePublicKey(JwtRsaSsaPkcs1PublicKey key, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        return ProtoKeySerialization.create(PUBLIC_TYPE_URL, getProtoPublicKey(key).toByteString(), KeyData.KeyMaterialType.ASYMMETRIC_PUBLIC, toProtoOutputPrefixType(key.getParameters()), key.getIdRequirementOrNull());
    }

    private static ByteString encodeSecretBigInteger(SecretBigInteger i, SecretKeyAccess access) {
        return encodeBigInteger(i.getBigInteger(access));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoKeySerialization serializePrivateKey(JwtRsaSsaPkcs1PrivateKey key, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        SecretKeyAccess secretKeyAccessRequireAccess = SecretKeyAccess.requireAccess(access);
        return ProtoKeySerialization.create(PRIVATE_TYPE_URL, ((com.google.crypto.tink.proto.JwtRsaSsaPkcs1PrivateKey) com.google.crypto.tink.proto.JwtRsaSsaPkcs1PrivateKey.newBuilder().setVersion(0).setPublicKey(getProtoPublicKey(key.getPublicKey())).setD(encodeSecretBigInteger(key.getPrivateExponent(), secretKeyAccessRequireAccess)).setP(encodeSecretBigInteger(key.getPrimeP(), secretKeyAccessRequireAccess)).setQ(encodeSecretBigInteger(key.getPrimeQ(), secretKeyAccessRequireAccess)).setDp(encodeSecretBigInteger(key.getPrimeExponentP(), secretKeyAccessRequireAccess)).setDq(encodeSecretBigInteger(key.getPrimeExponentQ(), secretKeyAccessRequireAccess)).setCrt(encodeSecretBigInteger(key.getCrtCoefficient(), secretKeyAccessRequireAccess)).build()).toByteString(), KeyData.KeyMaterialType.ASYMMETRIC_PRIVATE, toProtoOutputPrefixType(key.getParameters()), key.getIdRequirementOrNull());
    }

    private static BigInteger decodeBigInteger(ByteString data) {
        return BigIntegerEncoding.fromUnsignedBigEndianBytes(data.toByteArray());
    }

    private static void validateVersion(int version) throws GeneralSecurityException {
        if (version == 0) {
            return;
        }
        throw new GeneralSecurityException("Parsing failed: unknown version " + version);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static JwtRsaSsaPkcs1Parameters parseParameters(ProtoParametersSerialization serialization) throws GeneralSecurityException {
        if (!serialization.getKeyTemplate().getTypeUrl().equals(PRIVATE_TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to JwtRsaSsaPkcs1ProtoSerialization.parseParameters: " + serialization.getKeyTemplate().getTypeUrl());
        }
        try {
            JwtRsaSsaPkcs1KeyFormat from = JwtRsaSsaPkcs1KeyFormat.parseFrom(serialization.getKeyTemplate().getValue(), ExtensionRegistryLite.getEmptyRegistry());
            validateVersion(from.getVersion());
            JwtRsaSsaPkcs1Parameters.KidStrategy kidStrategy = serialization.getKeyTemplate().getOutputPrefixType().equals(OutputPrefixType.TINK) ? JwtRsaSsaPkcs1Parameters.KidStrategy.BASE64_ENCODED_KEY_ID : null;
            if (serialization.getKeyTemplate().getOutputPrefixType().equals(OutputPrefixType.RAW)) {
                kidStrategy = JwtRsaSsaPkcs1Parameters.KidStrategy.IGNORED;
            }
            if (kidStrategy == null) {
                throw new GeneralSecurityException("Invalid OutputPrefixType for JwtHmacKeyFormat");
            }
            return JwtRsaSsaPkcs1Parameters.builder().setKidStrategy(kidStrategy).setAlgorithm(ALGORITHM_CONVERTER.fromProtoEnum(from.getAlgorithm())).setPublicExponent(decodeBigInteger(from.getPublicExponent())).setModulusSizeBits(from.getModulusSizeInBits()).build();
        } catch (InvalidProtocolBufferException e) {
            throw new GeneralSecurityException("Parsing JwtRsaSsaPkcs1Parameters failed: ", e);
        }
    }

    private static JwtRsaSsaPkcs1PublicKey getPublicKeyFromProto(com.google.crypto.tink.proto.JwtRsaSsaPkcs1PublicKey protoKey, OutputPrefixType outputPrefixType, @Nullable Integer idRequirement) throws GeneralSecurityException {
        validateVersion(protoKey.getVersion());
        JwtRsaSsaPkcs1Parameters.Builder builder = JwtRsaSsaPkcs1Parameters.builder();
        JwtRsaSsaPkcs1PublicKey.Builder builder2 = JwtRsaSsaPkcs1PublicKey.builder();
        if (outputPrefixType.equals(OutputPrefixType.TINK)) {
            if (protoKey.hasCustomKid()) {
                throw new GeneralSecurityException("Keys serialized with OutputPrefixType TINK should not have a custom kid");
            }
            if (idRequirement == null) {
                throw new GeneralSecurityException("Keys serialized with OutputPrefixType TINK need an ID Requirement");
            }
            builder.setKidStrategy(JwtRsaSsaPkcs1Parameters.KidStrategy.BASE64_ENCODED_KEY_ID);
            builder2.setIdRequirement(idRequirement);
        } else if (outputPrefixType.equals(OutputPrefixType.RAW)) {
            if (protoKey.hasCustomKid()) {
                builder.setKidStrategy(JwtRsaSsaPkcs1Parameters.KidStrategy.CUSTOM);
                builder2.setCustomKid(protoKey.getCustomKid().getValue());
            } else {
                builder.setKidStrategy(JwtRsaSsaPkcs1Parameters.KidStrategy.IGNORED);
            }
        }
        BigInteger bigIntegerDecodeBigInteger = decodeBigInteger(protoKey.getN());
        builder.setAlgorithm(ALGORITHM_CONVERTER.fromProtoEnum(protoKey.getAlgorithm())).setPublicExponent(decodeBigInteger(protoKey.getE())).setModulusSizeBits(bigIntegerDecodeBigInteger.bitLength());
        builder2.setModulus(bigIntegerDecodeBigInteger).setParameters(builder.build());
        return builder2.build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static JwtRsaSsaPkcs1PublicKey parsePublicKey(ProtoKeySerialization serialization, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        if (!serialization.getTypeUrl().equals(PUBLIC_TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to JwtRsaSsaPkcs1ProtoSerialization.parsePublicKey: " + serialization.getTypeUrl());
        }
        try {
            return getPublicKeyFromProto(com.google.crypto.tink.proto.JwtRsaSsaPkcs1PublicKey.parseFrom(serialization.getValue(), ExtensionRegistryLite.getEmptyRegistry()), serialization.getOutputPrefixType(), serialization.getIdRequirementOrNull());
        } catch (InvalidProtocolBufferException unused) {
            throw new GeneralSecurityException("Parsing JwtRsaSsaPkcs1PublicKey failed");
        }
    }

    private static SecretBigInteger decodeSecretBigInteger(ByteString data, SecretKeyAccess access) {
        return SecretBigInteger.fromBigInteger(BigIntegerEncoding.fromUnsignedBigEndianBytes(data.toByteArray()), access);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static JwtRsaSsaPkcs1PrivateKey parsePrivateKey(ProtoKeySerialization serialization, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        if (!serialization.getTypeUrl().equals(PRIVATE_TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to JwtRsaSsaPkcs1ProtoSerialization.parsePrivateKey: " + serialization.getTypeUrl());
        }
        try {
            com.google.crypto.tink.proto.JwtRsaSsaPkcs1PrivateKey from = com.google.crypto.tink.proto.JwtRsaSsaPkcs1PrivateKey.parseFrom(serialization.getValue(), ExtensionRegistryLite.getEmptyRegistry());
            validateVersion(from.getVersion());
            JwtRsaSsaPkcs1PublicKey publicKeyFromProto = getPublicKeyFromProto(from.getPublicKey(), serialization.getOutputPrefixType(), serialization.getIdRequirementOrNull());
            SecretKeyAccess secretKeyAccessRequireAccess = SecretKeyAccess.requireAccess(access);
            return JwtRsaSsaPkcs1PrivateKey.builder().setPublicKey(publicKeyFromProto).setPrimes(decodeSecretBigInteger(from.getP(), secretKeyAccessRequireAccess), decodeSecretBigInteger(from.getQ(), secretKeyAccessRequireAccess)).setPrivateExponent(decodeSecretBigInteger(from.getD(), secretKeyAccessRequireAccess)).setPrimeExponents(decodeSecretBigInteger(from.getDp(), secretKeyAccessRequireAccess), decodeSecretBigInteger(from.getDq(), secretKeyAccessRequireAccess)).setCrtCoefficient(decodeSecretBigInteger(from.getCrt(), secretKeyAccessRequireAccess)).build();
        } catch (InvalidProtocolBufferException unused) {
            throw new GeneralSecurityException("Parsing JwtRsaSsaPkcs1PrivateKey failed");
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

    private JwtRsaSsaPkcs1ProtoSerialization() {
    }
}
