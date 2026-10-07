package com.google.crypto.tink.signature.internal;

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
import com.google.crypto.tink.proto.HashType;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.proto.KeyTemplate;
import com.google.crypto.tink.proto.OutputPrefixType;
import com.google.crypto.tink.proto.RsaSsaPssKeyFormat;
import com.google.crypto.tink.proto.RsaSsaPssParams;
import com.google.crypto.tink.shaded.protobuf.ByteString;
import com.google.crypto.tink.shaded.protobuf.ExtensionRegistryLite;
import com.google.crypto.tink.shaded.protobuf.InvalidProtocolBufferException;
import com.google.crypto.tink.signature.RsaSsaPssParameters;
import com.google.crypto.tink.signature.RsaSsaPssPrivateKey;
import com.google.crypto.tink.signature.RsaSsaPssPublicKey;
import com.google.crypto.tink.util.Bytes;
import com.google.crypto.tink.util.SecretBigInteger;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class RsaSsaPssProtoSerialization {
    private static final EnumTypeProtoConverter<HashType, RsaSsaPssParameters.HashType> HASH_TYPE_CONVERTER;
    private static final ParametersParser<ProtoParametersSerialization> PARAMETERS_PARSER;
    private static final ParametersSerializer<RsaSsaPssParameters, ProtoParametersSerialization> PARAMETERS_SERIALIZER;
    private static final KeyParser<ProtoKeySerialization> PRIVATE_KEY_PARSER;
    private static final KeySerializer<RsaSsaPssPrivateKey, ProtoKeySerialization> PRIVATE_KEY_SERIALIZER;
    private static final String PRIVATE_TYPE_URL = "type.googleapis.com/google.crypto.tink.RsaSsaPssPrivateKey";
    private static final Bytes PRIVATE_TYPE_URL_BYTES;
    private static final KeyParser<ProtoKeySerialization> PUBLIC_KEY_PARSER;
    private static final KeySerializer<RsaSsaPssPublicKey, ProtoKeySerialization> PUBLIC_KEY_SERIALIZER;
    private static final String PUBLIC_TYPE_URL = "type.googleapis.com/google.crypto.tink.RsaSsaPssPublicKey";
    private static final Bytes PUBLIC_TYPE_URL_BYTES;
    private static final EnumTypeProtoConverter<OutputPrefixType, RsaSsaPssParameters.Variant> VARIANT_CONVERTER;

    static {
        Bytes bytesFromPrintableAscii = Util.toBytesFromPrintableAscii(PRIVATE_TYPE_URL);
        PRIVATE_TYPE_URL_BYTES = bytesFromPrintableAscii;
        Bytes bytesFromPrintableAscii2 = Util.toBytesFromPrintableAscii(PUBLIC_TYPE_URL);
        PUBLIC_TYPE_URL_BYTES = bytesFromPrintableAscii2;
        PARAMETERS_SERIALIZER = ParametersSerializer.create(new ParametersSerializer.ParametersSerializationFunction() { // from class: com.google.crypto.tink.signature.internal.RsaSsaPssProtoSerialization$$ExternalSyntheticLambda0
            @Override // com.google.crypto.tink.internal.ParametersSerializer.ParametersSerializationFunction
            public final Serialization serializeParameters(Parameters parameters) {
                return RsaSsaPssProtoSerialization.serializeParameters((RsaSsaPssParameters) parameters);
            }
        }, RsaSsaPssParameters.class, ProtoParametersSerialization.class);
        PARAMETERS_PARSER = ParametersParser.create(new ParametersParser.ParametersParsingFunction() { // from class: com.google.crypto.tink.signature.internal.RsaSsaPssProtoSerialization$$ExternalSyntheticLambda1
            @Override // com.google.crypto.tink.internal.ParametersParser.ParametersParsingFunction
            public final Parameters parseParameters(Serialization serialization) {
                return RsaSsaPssProtoSerialization.parseParameters((ProtoParametersSerialization) serialization);
            }
        }, bytesFromPrintableAscii, ProtoParametersSerialization.class);
        PUBLIC_KEY_SERIALIZER = KeySerializer.create(new KeySerializer.KeySerializationFunction() { // from class: com.google.crypto.tink.signature.internal.RsaSsaPssProtoSerialization$$ExternalSyntheticLambda2
            @Override // com.google.crypto.tink.internal.KeySerializer.KeySerializationFunction
            public final Serialization serializeKey(Key key, SecretKeyAccess secretKeyAccess) {
                return RsaSsaPssProtoSerialization.serializePublicKey((RsaSsaPssPublicKey) key, secretKeyAccess);
            }
        }, RsaSsaPssPublicKey.class, ProtoKeySerialization.class);
        PUBLIC_KEY_PARSER = KeyParser.create(new KeyParser.KeyParsingFunction() { // from class: com.google.crypto.tink.signature.internal.RsaSsaPssProtoSerialization$$ExternalSyntheticLambda3
            @Override // com.google.crypto.tink.internal.KeyParser.KeyParsingFunction
            public final Key parseKey(Serialization serialization, SecretKeyAccess secretKeyAccess) {
                return RsaSsaPssProtoSerialization.parsePublicKey((ProtoKeySerialization) serialization, secretKeyAccess);
            }
        }, bytesFromPrintableAscii2, ProtoKeySerialization.class);
        PRIVATE_KEY_SERIALIZER = KeySerializer.create(new KeySerializer.KeySerializationFunction() { // from class: com.google.crypto.tink.signature.internal.RsaSsaPssProtoSerialization$$ExternalSyntheticLambda4
            @Override // com.google.crypto.tink.internal.KeySerializer.KeySerializationFunction
            public final Serialization serializeKey(Key key, SecretKeyAccess secretKeyAccess) {
                return RsaSsaPssProtoSerialization.serializePrivateKey((RsaSsaPssPrivateKey) key, secretKeyAccess);
            }
        }, RsaSsaPssPrivateKey.class, ProtoKeySerialization.class);
        PRIVATE_KEY_PARSER = KeyParser.create(new KeyParser.KeyParsingFunction() { // from class: com.google.crypto.tink.signature.internal.RsaSsaPssProtoSerialization$$ExternalSyntheticLambda5
            @Override // com.google.crypto.tink.internal.KeyParser.KeyParsingFunction
            public final Key parseKey(Serialization serialization, SecretKeyAccess secretKeyAccess) {
                return RsaSsaPssProtoSerialization.parsePrivateKey((ProtoKeySerialization) serialization, secretKeyAccess);
            }
        }, bytesFromPrintableAscii, ProtoKeySerialization.class);
        VARIANT_CONVERTER = EnumTypeProtoConverter.builder().add(OutputPrefixType.RAW, RsaSsaPssParameters.Variant.NO_PREFIX).add(OutputPrefixType.TINK, RsaSsaPssParameters.Variant.TINK).add(OutputPrefixType.CRUNCHY, RsaSsaPssParameters.Variant.CRUNCHY).add(OutputPrefixType.LEGACY, RsaSsaPssParameters.Variant.LEGACY).build();
        HASH_TYPE_CONVERTER = EnumTypeProtoConverter.builder().add(HashType.SHA256, RsaSsaPssParameters.HashType.SHA256).add(HashType.SHA384, RsaSsaPssParameters.HashType.SHA384).add(HashType.SHA512, RsaSsaPssParameters.HashType.SHA512).build();
    }

    private static RsaSsaPssParams getProtoParams(RsaSsaPssParameters parameters) throws GeneralSecurityException {
        RsaSsaPssParams.Builder builderNewBuilder = RsaSsaPssParams.newBuilder();
        EnumTypeProtoConverter<HashType, RsaSsaPssParameters.HashType> enumTypeProtoConverter = HASH_TYPE_CONVERTER;
        return (RsaSsaPssParams) builderNewBuilder.setSigHash((HashType) enumTypeProtoConverter.toProtoEnum(parameters.getSigHashType())).setMgf1Hash((HashType) enumTypeProtoConverter.toProtoEnum(parameters.getMgf1HashType())).setSaltLength(parameters.getSaltLengthBytes()).build();
    }

    private static ByteString encodeBigInteger(BigInteger i) {
        return ByteString.copyFrom(BigIntegerEncoding.toBigEndianBytes(i));
    }

    private static com.google.crypto.tink.proto.RsaSsaPssPublicKey getProtoPublicKey(RsaSsaPssPublicKey key) throws GeneralSecurityException {
        return (com.google.crypto.tink.proto.RsaSsaPssPublicKey) com.google.crypto.tink.proto.RsaSsaPssPublicKey.newBuilder().setParams(getProtoParams(key.getParameters())).setN(encodeBigInteger(key.getModulus())).setE(encodeBigInteger(key.getParameters().getPublicExponent())).setVersion(0).build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoParametersSerialization serializeParameters(RsaSsaPssParameters parameters) throws GeneralSecurityException {
        return ProtoParametersSerialization.create((KeyTemplate) KeyTemplate.newBuilder().setTypeUrl(PRIVATE_TYPE_URL).setValue(((RsaSsaPssKeyFormat) RsaSsaPssKeyFormat.newBuilder().setParams(getProtoParams(parameters)).setModulusSizeInBits(parameters.getModulusSizeBits()).setPublicExponent(encodeBigInteger(parameters.getPublicExponent())).build()).toByteString()).setOutputPrefixType((OutputPrefixType) VARIANT_CONVERTER.toProtoEnum(parameters.getVariant())).build());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoKeySerialization serializePublicKey(RsaSsaPssPublicKey key, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        return ProtoKeySerialization.create(PUBLIC_TYPE_URL, getProtoPublicKey(key).toByteString(), KeyData.KeyMaterialType.ASYMMETRIC_PUBLIC, (OutputPrefixType) VARIANT_CONVERTER.toProtoEnum(key.getParameters().getVariant()), key.getIdRequirementOrNull());
    }

    private static ByteString encodeSecretBigInteger(SecretBigInteger i, SecretKeyAccess access) {
        return encodeBigInteger(i.getBigInteger(access));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoKeySerialization serializePrivateKey(RsaSsaPssPrivateKey key, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        SecretKeyAccess secretKeyAccessRequireAccess = SecretKeyAccess.requireAccess(access);
        return ProtoKeySerialization.create(PRIVATE_TYPE_URL, ((com.google.crypto.tink.proto.RsaSsaPssPrivateKey) com.google.crypto.tink.proto.RsaSsaPssPrivateKey.newBuilder().setVersion(0).setPublicKey(getProtoPublicKey(key.getPublicKey())).setD(encodeSecretBigInteger(key.getPrivateExponent(), secretKeyAccessRequireAccess)).setP(encodeSecretBigInteger(key.getPrimeP(), secretKeyAccessRequireAccess)).setQ(encodeSecretBigInteger(key.getPrimeQ(), secretKeyAccessRequireAccess)).setDp(encodeSecretBigInteger(key.getPrimeExponentP(), secretKeyAccessRequireAccess)).setDq(encodeSecretBigInteger(key.getPrimeExponentQ(), secretKeyAccessRequireAccess)).setCrt(encodeSecretBigInteger(key.getCrtCoefficient(), secretKeyAccessRequireAccess)).build()).toByteString(), KeyData.KeyMaterialType.ASYMMETRIC_PRIVATE, (OutputPrefixType) VARIANT_CONVERTER.toProtoEnum(key.getParameters().getVariant()), key.getIdRequirementOrNull());
    }

    private static BigInteger decodeBigInteger(ByteString data) {
        return BigIntegerEncoding.fromUnsignedBigEndianBytes(data.toByteArray());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static RsaSsaPssParameters parseParameters(ProtoParametersSerialization serialization) throws GeneralSecurityException {
        if (!serialization.getKeyTemplate().getTypeUrl().equals(PRIVATE_TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to RsaSsaPssProtoSerialization.parseParameters: " + serialization.getKeyTemplate().getTypeUrl());
        }
        try {
            RsaSsaPssKeyFormat from = RsaSsaPssKeyFormat.parseFrom(serialization.getKeyTemplate().getValue(), ExtensionRegistryLite.getEmptyRegistry());
            RsaSsaPssParameters.Builder builder = RsaSsaPssParameters.builder();
            EnumTypeProtoConverter<HashType, RsaSsaPssParameters.HashType> enumTypeProtoConverter = HASH_TYPE_CONVERTER;
            return builder.setSigHashType(enumTypeProtoConverter.fromProtoEnum(from.getParams().getSigHash())).setMgf1HashType(enumTypeProtoConverter.fromProtoEnum(from.getParams().getMgf1Hash())).setPublicExponent(decodeBigInteger(from.getPublicExponent())).setModulusSizeBits(from.getModulusSizeInBits()).setSaltLengthBytes(from.getParams().getSaltLength()).setVariant(VARIANT_CONVERTER.fromProtoEnum(serialization.getKeyTemplate().getOutputPrefixType())).build();
        } catch (InvalidProtocolBufferException e) {
            throw new GeneralSecurityException("Parsing RsaSsaPssParameters failed: ", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static RsaSsaPssPublicKey parsePublicKey(ProtoKeySerialization serialization, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        if (!serialization.getTypeUrl().equals(PUBLIC_TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to RsaSsaPssProtoSerialization.parsePublicKey: " + serialization.getTypeUrl());
        }
        try {
            com.google.crypto.tink.proto.RsaSsaPssPublicKey from = com.google.crypto.tink.proto.RsaSsaPssPublicKey.parseFrom(serialization.getValue(), ExtensionRegistryLite.getEmptyRegistry());
            if (from.getVersion() != 0) {
                throw new GeneralSecurityException("Only version 0 keys are accepted");
            }
            BigInteger bigIntegerDecodeBigInteger = decodeBigInteger(from.getN());
            int iBitLength = bigIntegerDecodeBigInteger.bitLength();
            RsaSsaPssParameters.Builder builder = RsaSsaPssParameters.builder();
            EnumTypeProtoConverter<HashType, RsaSsaPssParameters.HashType> enumTypeProtoConverter = HASH_TYPE_CONVERTER;
            return RsaSsaPssPublicKey.builder().setParameters(builder.setSigHashType(enumTypeProtoConverter.fromProtoEnum(from.getParams().getSigHash())).setMgf1HashType(enumTypeProtoConverter.fromProtoEnum(from.getParams().getMgf1Hash())).setPublicExponent(decodeBigInteger(from.getE())).setModulusSizeBits(iBitLength).setSaltLengthBytes(from.getParams().getSaltLength()).setVariant(VARIANT_CONVERTER.fromProtoEnum(serialization.getOutputPrefixType())).build()).setModulus(bigIntegerDecodeBigInteger).setIdRequirement(serialization.getIdRequirementOrNull()).build();
        } catch (InvalidProtocolBufferException | IllegalArgumentException unused) {
            throw new GeneralSecurityException("Parsing RsaSsaPssPublicKey failed");
        }
    }

    private static SecretBigInteger decodeSecretBigInteger(ByteString data, SecretKeyAccess access) {
        return SecretBigInteger.fromBigInteger(BigIntegerEncoding.fromUnsignedBigEndianBytes(data.toByteArray()), access);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static RsaSsaPssPrivateKey parsePrivateKey(ProtoKeySerialization serialization, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        if (!serialization.getTypeUrl().equals(PRIVATE_TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to RsaSsaPssProtoSerialization.parsePrivateKey: " + serialization.getTypeUrl());
        }
        try {
            com.google.crypto.tink.proto.RsaSsaPssPrivateKey from = com.google.crypto.tink.proto.RsaSsaPssPrivateKey.parseFrom(serialization.getValue(), ExtensionRegistryLite.getEmptyRegistry());
            if (from.getVersion() != 0) {
                throw new GeneralSecurityException("Only version 0 keys are accepted");
            }
            com.google.crypto.tink.proto.RsaSsaPssPublicKey publicKey = from.getPublicKey();
            if (publicKey.getVersion() != 0) {
                throw new GeneralSecurityException("Only version 0 keys are accepted");
            }
            BigInteger bigIntegerDecodeBigInteger = decodeBigInteger(publicKey.getN());
            int iBitLength = bigIntegerDecodeBigInteger.bitLength();
            BigInteger bigIntegerDecodeBigInteger2 = decodeBigInteger(publicKey.getE());
            RsaSsaPssParameters.Builder builder = RsaSsaPssParameters.builder();
            EnumTypeProtoConverter<HashType, RsaSsaPssParameters.HashType> enumTypeProtoConverter = HASH_TYPE_CONVERTER;
            RsaSsaPssPublicKey rsaSsaPssPublicKeyBuild = RsaSsaPssPublicKey.builder().setParameters(builder.setSigHashType(enumTypeProtoConverter.fromProtoEnum(publicKey.getParams().getSigHash())).setMgf1HashType(enumTypeProtoConverter.fromProtoEnum(publicKey.getParams().getMgf1Hash())).setPublicExponent(bigIntegerDecodeBigInteger2).setModulusSizeBits(iBitLength).setSaltLengthBytes(publicKey.getParams().getSaltLength()).setVariant(VARIANT_CONVERTER.fromProtoEnum(serialization.getOutputPrefixType())).build()).setModulus(bigIntegerDecodeBigInteger).setIdRequirement(serialization.getIdRequirementOrNull()).build();
            SecretKeyAccess secretKeyAccessRequireAccess = SecretKeyAccess.requireAccess(access);
            return RsaSsaPssPrivateKey.builder().setPublicKey(rsaSsaPssPublicKeyBuild).setPrimes(decodeSecretBigInteger(from.getP(), secretKeyAccessRequireAccess), decodeSecretBigInteger(from.getQ(), secretKeyAccessRequireAccess)).setPrivateExponent(decodeSecretBigInteger(from.getD(), secretKeyAccessRequireAccess)).setPrimeExponents(decodeSecretBigInteger(from.getDp(), secretKeyAccessRequireAccess), decodeSecretBigInteger(from.getDq(), secretKeyAccessRequireAccess)).setCrtCoefficient(decodeSecretBigInteger(from.getCrt(), secretKeyAccessRequireAccess)).build();
        } catch (InvalidProtocolBufferException | IllegalArgumentException unused) {
            throw new GeneralSecurityException("Parsing RsaSsaPssPrivateKey failed");
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

    private RsaSsaPssProtoSerialization() {
    }
}
