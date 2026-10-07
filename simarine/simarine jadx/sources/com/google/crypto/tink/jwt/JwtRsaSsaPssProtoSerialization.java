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
import com.google.crypto.tink.proto.JwtRsaSsaPssAlgorithm;
import com.google.crypto.tink.proto.JwtRsaSsaPssKeyFormat;
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
final class JwtRsaSsaPssProtoSerialization {
    private static final EnumTypeProtoConverter<JwtRsaSsaPssAlgorithm, JwtRsaSsaPssParameters.Algorithm> ALGORITHM_CONVERTER;
    private static final ParametersParser<ProtoParametersSerialization> PARAMETERS_PARSER;
    private static final ParametersSerializer<JwtRsaSsaPssParameters, ProtoParametersSerialization> PARAMETERS_SERIALIZER;
    private static final KeyParser<ProtoKeySerialization> PRIVATE_KEY_PARSER;
    private static final KeySerializer<JwtRsaSsaPssPrivateKey, ProtoKeySerialization> PRIVATE_KEY_SERIALIZER;
    private static final String PRIVATE_TYPE_URL = "type.googleapis.com/google.crypto.tink.JwtRsaSsaPssPrivateKey";
    private static final Bytes PRIVATE_TYPE_URL_BYTES;
    private static final KeyParser<ProtoKeySerialization> PUBLIC_KEY_PARSER;
    private static final KeySerializer<JwtRsaSsaPssPublicKey, ProtoKeySerialization> PUBLIC_KEY_SERIALIZER;
    private static final String PUBLIC_TYPE_URL = "type.googleapis.com/google.crypto.tink.JwtRsaSsaPssPublicKey";
    private static final Bytes PUBLIC_TYPE_URL_BYTES;

    static {
        Bytes bytesFromPrintableAscii = Util.toBytesFromPrintableAscii(PRIVATE_TYPE_URL);
        PRIVATE_TYPE_URL_BYTES = bytesFromPrintableAscii;
        Bytes bytesFromPrintableAscii2 = Util.toBytesFromPrintableAscii(PUBLIC_TYPE_URL);
        PUBLIC_TYPE_URL_BYTES = bytesFromPrintableAscii2;
        PARAMETERS_SERIALIZER = ParametersSerializer.create(new ParametersSerializer.ParametersSerializationFunction() { // from class: com.google.crypto.tink.jwt.JwtRsaSsaPssProtoSerialization$$ExternalSyntheticLambda0
            @Override // com.google.crypto.tink.internal.ParametersSerializer.ParametersSerializationFunction
            public final Serialization serializeParameters(Parameters parameters) {
                return JwtRsaSsaPssProtoSerialization.serializeParameters((JwtRsaSsaPssParameters) parameters);
            }
        }, JwtRsaSsaPssParameters.class, ProtoParametersSerialization.class);
        PARAMETERS_PARSER = ParametersParser.create(new ParametersParser.ParametersParsingFunction() { // from class: com.google.crypto.tink.jwt.JwtRsaSsaPssProtoSerialization$$ExternalSyntheticLambda1
            @Override // com.google.crypto.tink.internal.ParametersParser.ParametersParsingFunction
            public final Parameters parseParameters(Serialization serialization) {
                return JwtRsaSsaPssProtoSerialization.parseParameters((ProtoParametersSerialization) serialization);
            }
        }, bytesFromPrintableAscii, ProtoParametersSerialization.class);
        PUBLIC_KEY_SERIALIZER = KeySerializer.create(new KeySerializer.KeySerializationFunction() { // from class: com.google.crypto.tink.jwt.JwtRsaSsaPssProtoSerialization$$ExternalSyntheticLambda2
            @Override // com.google.crypto.tink.internal.KeySerializer.KeySerializationFunction
            public final Serialization serializeKey(Key key, SecretKeyAccess secretKeyAccess) {
                return JwtRsaSsaPssProtoSerialization.serializePublicKey((JwtRsaSsaPssPublicKey) key, secretKeyAccess);
            }
        }, JwtRsaSsaPssPublicKey.class, ProtoKeySerialization.class);
        PUBLIC_KEY_PARSER = KeyParser.create(new KeyParser.KeyParsingFunction() { // from class: com.google.crypto.tink.jwt.JwtRsaSsaPssProtoSerialization$$ExternalSyntheticLambda3
            @Override // com.google.crypto.tink.internal.KeyParser.KeyParsingFunction
            public final Key parseKey(Serialization serialization, SecretKeyAccess secretKeyAccess) {
                return JwtRsaSsaPssProtoSerialization.parsePublicKey((ProtoKeySerialization) serialization, secretKeyAccess);
            }
        }, bytesFromPrintableAscii2, ProtoKeySerialization.class);
        PRIVATE_KEY_SERIALIZER = KeySerializer.create(new KeySerializer.KeySerializationFunction() { // from class: com.google.crypto.tink.jwt.JwtRsaSsaPssProtoSerialization$$ExternalSyntheticLambda4
            @Override // com.google.crypto.tink.internal.KeySerializer.KeySerializationFunction
            public final Serialization serializeKey(Key key, SecretKeyAccess secretKeyAccess) {
                return JwtRsaSsaPssProtoSerialization.serializePrivateKey((JwtRsaSsaPssPrivateKey) key, secretKeyAccess);
            }
        }, JwtRsaSsaPssPrivateKey.class, ProtoKeySerialization.class);
        PRIVATE_KEY_PARSER = KeyParser.create(new KeyParser.KeyParsingFunction() { // from class: com.google.crypto.tink.jwt.JwtRsaSsaPssProtoSerialization$$ExternalSyntheticLambda5
            @Override // com.google.crypto.tink.internal.KeyParser.KeyParsingFunction
            public final Key parseKey(Serialization serialization, SecretKeyAccess secretKeyAccess) {
                return JwtRsaSsaPssProtoSerialization.parsePrivateKey((ProtoKeySerialization) serialization, secretKeyAccess);
            }
        }, bytesFromPrintableAscii, ProtoKeySerialization.class);
        ALGORITHM_CONVERTER = EnumTypeProtoConverter.builder().add(JwtRsaSsaPssAlgorithm.PS256, JwtRsaSsaPssParameters.Algorithm.PS256).add(JwtRsaSsaPssAlgorithm.PS384, JwtRsaSsaPssParameters.Algorithm.PS384).add(JwtRsaSsaPssAlgorithm.PS512, JwtRsaSsaPssParameters.Algorithm.PS512).build();
    }

    private static OutputPrefixType toProtoOutputPrefixType(JwtRsaSsaPssParameters parameters) {
        if (parameters.getKidStrategy().equals(JwtRsaSsaPssParameters.KidStrategy.BASE64_ENCODED_KEY_ID)) {
            return OutputPrefixType.TINK;
        }
        return OutputPrefixType.RAW;
    }

    private static ByteString encodeBigInteger(BigInteger i) {
        return ByteString.copyFrom(BigIntegerEncoding.toBigEndianBytes(i));
    }

    private static JwtRsaSsaPssKeyFormat getProtoKeyFormat(JwtRsaSsaPssParameters parameters) throws GeneralSecurityException {
        if (!parameters.getKidStrategy().equals(JwtRsaSsaPssParameters.KidStrategy.IGNORED) && !parameters.getKidStrategy().equals(JwtRsaSsaPssParameters.KidStrategy.BASE64_ENCODED_KEY_ID)) {
            throw new GeneralSecurityException("Unable to serialize Parameters object with KidStrategy " + parameters.getKidStrategy());
        }
        return (JwtRsaSsaPssKeyFormat) JwtRsaSsaPssKeyFormat.newBuilder().setVersion(0).setAlgorithm((JwtRsaSsaPssAlgorithm) ALGORITHM_CONVERTER.toProtoEnum(parameters.getAlgorithm())).setModulusSizeInBits(parameters.getModulusSizeBits()).setPublicExponent(encodeBigInteger(parameters.getPublicExponent())).build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoParametersSerialization serializeParameters(JwtRsaSsaPssParameters parameters) throws GeneralSecurityException {
        return ProtoParametersSerialization.create((KeyTemplate) KeyTemplate.newBuilder().setTypeUrl(PRIVATE_TYPE_URL).setValue(getProtoKeyFormat(parameters).toByteString()).setOutputPrefixType(toProtoOutputPrefixType(parameters)).build());
    }

    private static com.google.crypto.tink.proto.JwtRsaSsaPssPublicKey getProtoPublicKey(JwtRsaSsaPssPublicKey key) throws GeneralSecurityException {
        com.google.crypto.tink.proto.JwtRsaSsaPssPublicKey.Builder e = com.google.crypto.tink.proto.JwtRsaSsaPssPublicKey.newBuilder().setVersion(0).setAlgorithm((JwtRsaSsaPssAlgorithm) ALGORITHM_CONVERTER.toProtoEnum(key.getParameters().getAlgorithm())).setN(encodeBigInteger(key.getModulus())).setE(encodeBigInteger(key.getParameters().getPublicExponent()));
        if (key.getParameters().getKidStrategy().equals(JwtRsaSsaPssParameters.KidStrategy.CUSTOM)) {
            e.setCustomKid((com.google.crypto.tink.proto.JwtRsaSsaPssPublicKey.CustomKid) com.google.crypto.tink.proto.JwtRsaSsaPssPublicKey.CustomKid.newBuilder().setValue(key.getKid().get()).build());
        }
        return (com.google.crypto.tink.proto.JwtRsaSsaPssPublicKey) e.build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoKeySerialization serializePublicKey(JwtRsaSsaPssPublicKey key, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        return ProtoKeySerialization.create(PUBLIC_TYPE_URL, getProtoPublicKey(key).toByteString(), KeyData.KeyMaterialType.ASYMMETRIC_PUBLIC, toProtoOutputPrefixType(key.getParameters()), key.getIdRequirementOrNull());
    }

    private static ByteString encodeSecretBigInteger(SecretBigInteger i, SecretKeyAccess access) {
        return encodeBigInteger(i.getBigInteger(access));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoKeySerialization serializePrivateKey(JwtRsaSsaPssPrivateKey key, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        SecretKeyAccess secretKeyAccessRequireAccess = SecretKeyAccess.requireAccess(access);
        return ProtoKeySerialization.create(PRIVATE_TYPE_URL, ((com.google.crypto.tink.proto.JwtRsaSsaPssPrivateKey) com.google.crypto.tink.proto.JwtRsaSsaPssPrivateKey.newBuilder().setVersion(0).setPublicKey(getProtoPublicKey(key.getPublicKey())).setD(encodeSecretBigInteger(key.getPrivateExponent(), secretKeyAccessRequireAccess)).setP(encodeSecretBigInteger(key.getPrimeP(), secretKeyAccessRequireAccess)).setQ(encodeSecretBigInteger(key.getPrimeQ(), secretKeyAccessRequireAccess)).setDp(encodeSecretBigInteger(key.getPrimeExponentP(), secretKeyAccessRequireAccess)).setDq(encodeSecretBigInteger(key.getPrimeExponentQ(), secretKeyAccessRequireAccess)).setCrt(encodeSecretBigInteger(key.getCrtCoefficient(), secretKeyAccessRequireAccess)).build()).toByteString(), KeyData.KeyMaterialType.ASYMMETRIC_PRIVATE, toProtoOutputPrefixType(key.getParameters()), key.getIdRequirementOrNull());
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
    public static JwtRsaSsaPssParameters parseParameters(ProtoParametersSerialization serialization) throws GeneralSecurityException {
        if (!serialization.getKeyTemplate().getTypeUrl().equals(PRIVATE_TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to JwtRsaSsaPssProtoSerialization.parseParameters: " + serialization.getKeyTemplate().getTypeUrl());
        }
        try {
            JwtRsaSsaPssKeyFormat from = JwtRsaSsaPssKeyFormat.parseFrom(serialization.getKeyTemplate().getValue(), ExtensionRegistryLite.getEmptyRegistry());
            validateVersion(from.getVersion());
            JwtRsaSsaPssParameters.KidStrategy kidStrategy = serialization.getKeyTemplate().getOutputPrefixType().equals(OutputPrefixType.TINK) ? JwtRsaSsaPssParameters.KidStrategy.BASE64_ENCODED_KEY_ID : null;
            if (serialization.getKeyTemplate().getOutputPrefixType().equals(OutputPrefixType.RAW)) {
                kidStrategy = JwtRsaSsaPssParameters.KidStrategy.IGNORED;
            }
            if (kidStrategy == null) {
                throw new GeneralSecurityException("Invalid OutputPrefixType for JwtHmacKeyFormat");
            }
            return JwtRsaSsaPssParameters.builder().setKidStrategy(kidStrategy).setAlgorithm(ALGORITHM_CONVERTER.fromProtoEnum(from.getAlgorithm())).setPublicExponent(decodeBigInteger(from.getPublicExponent())).setModulusSizeBits(from.getModulusSizeInBits()).build();
        } catch (InvalidProtocolBufferException e) {
            throw new GeneralSecurityException("Parsing JwtRsaSsaPssParameters failed: ", e);
        }
    }

    private static JwtRsaSsaPssPublicKey getPublicKeyFromProto(com.google.crypto.tink.proto.JwtRsaSsaPssPublicKey protoKey, OutputPrefixType outputPrefixType, @Nullable Integer idRequirement) throws GeneralSecurityException {
        validateVersion(protoKey.getVersion());
        JwtRsaSsaPssParameters.Builder builder = JwtRsaSsaPssParameters.builder();
        JwtRsaSsaPssPublicKey.Builder builder2 = JwtRsaSsaPssPublicKey.builder();
        if (outputPrefixType.equals(OutputPrefixType.TINK)) {
            if (protoKey.hasCustomKid()) {
                throw new GeneralSecurityException("Keys serialized with OutputPrefixType TINK should not have a custom kid");
            }
            if (idRequirement == null) {
                throw new GeneralSecurityException("Keys serialized with OutputPrefixType TINK need an ID Requirement");
            }
            builder.setKidStrategy(JwtRsaSsaPssParameters.KidStrategy.BASE64_ENCODED_KEY_ID);
            builder2.setIdRequirement(idRequirement);
        } else if (outputPrefixType.equals(OutputPrefixType.RAW)) {
            if (protoKey.hasCustomKid()) {
                builder.setKidStrategy(JwtRsaSsaPssParameters.KidStrategy.CUSTOM);
                builder2.setCustomKid(protoKey.getCustomKid().getValue());
            } else {
                builder.setKidStrategy(JwtRsaSsaPssParameters.KidStrategy.IGNORED);
            }
        }
        BigInteger bigIntegerDecodeBigInteger = decodeBigInteger(protoKey.getN());
        builder.setAlgorithm(ALGORITHM_CONVERTER.fromProtoEnum(protoKey.getAlgorithm())).setPublicExponent(decodeBigInteger(protoKey.getE())).setModulusSizeBits(bigIntegerDecodeBigInteger.bitLength());
        builder2.setModulus(bigIntegerDecodeBigInteger).setParameters(builder.build());
        return builder2.build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static JwtRsaSsaPssPublicKey parsePublicKey(ProtoKeySerialization serialization, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        if (!serialization.getTypeUrl().equals(PUBLIC_TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to JwtRsaSsaPssProtoSerialization.parsePublicKey: " + serialization.getTypeUrl());
        }
        try {
            return getPublicKeyFromProto(com.google.crypto.tink.proto.JwtRsaSsaPssPublicKey.parseFrom(serialization.getValue(), ExtensionRegistryLite.getEmptyRegistry()), serialization.getOutputPrefixType(), serialization.getIdRequirementOrNull());
        } catch (InvalidProtocolBufferException unused) {
            throw new GeneralSecurityException("Parsing JwtRsaSsaPssPublicKey failed");
        }
    }

    private static SecretBigInteger decodeSecretBigInteger(ByteString data, SecretKeyAccess access) {
        return SecretBigInteger.fromBigInteger(BigIntegerEncoding.fromUnsignedBigEndianBytes(data.toByteArray()), access);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static JwtRsaSsaPssPrivateKey parsePrivateKey(ProtoKeySerialization serialization, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        if (!serialization.getTypeUrl().equals(PRIVATE_TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to JwtRsaSsaPssProtoSerialization.parsePrivateKey: " + serialization.getTypeUrl());
        }
        try {
            com.google.crypto.tink.proto.JwtRsaSsaPssPrivateKey from = com.google.crypto.tink.proto.JwtRsaSsaPssPrivateKey.parseFrom(serialization.getValue(), ExtensionRegistryLite.getEmptyRegistry());
            validateVersion(from.getVersion());
            JwtRsaSsaPssPublicKey publicKeyFromProto = getPublicKeyFromProto(from.getPublicKey(), serialization.getOutputPrefixType(), serialization.getIdRequirementOrNull());
            SecretKeyAccess secretKeyAccessRequireAccess = SecretKeyAccess.requireAccess(access);
            return JwtRsaSsaPssPrivateKey.builder().setPublicKey(publicKeyFromProto).setPrimes(decodeSecretBigInteger(from.getP(), secretKeyAccessRequireAccess), decodeSecretBigInteger(from.getQ(), secretKeyAccessRequireAccess)).setPrivateExponent(decodeSecretBigInteger(from.getD(), secretKeyAccessRequireAccess)).setPrimeExponents(decodeSecretBigInteger(from.getDp(), secretKeyAccessRequireAccess), decodeSecretBigInteger(from.getDq(), secretKeyAccessRequireAccess)).setCrtCoefficient(decodeSecretBigInteger(from.getCrt(), secretKeyAccessRequireAccess)).build();
        } catch (InvalidProtocolBufferException unused) {
            throw new GeneralSecurityException("Parsing JwtRsaSsaPssPrivateKey failed");
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

    private JwtRsaSsaPssProtoSerialization() {
    }
}
