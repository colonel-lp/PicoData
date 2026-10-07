package com.google.crypto.tink.aead;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.SecretKeyAccess;
import com.google.crypto.tink.TinkProtoParametersFormat;
import com.google.crypto.tink.internal.KeyParser;
import com.google.crypto.tink.internal.KeySerializer;
import com.google.crypto.tink.internal.MutableSerializationRegistry;
import com.google.crypto.tink.internal.ParametersParser;
import com.google.crypto.tink.internal.ParametersSerializer;
import com.google.crypto.tink.internal.ProtoKeySerialization;
import com.google.crypto.tink.internal.ProtoParametersSerialization;
import com.google.crypto.tink.internal.Serialization;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.proto.KeyTemplate;
import com.google.crypto.tink.proto.KmsEnvelopeAeadKey;
import com.google.crypto.tink.proto.KmsEnvelopeAeadKeyFormat;
import com.google.crypto.tink.proto.OutputPrefixType;
import com.google.crypto.tink.shaded.protobuf.ExtensionRegistryLite;
import com.google.crypto.tink.shaded.protobuf.InvalidProtocolBufferException;
import com.google.crypto.tink.util.Bytes;
import java.security.GeneralSecurityException;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class LegacyKmsEnvelopeAeadProtoSerialization {
    private static final KeyParser<ProtoKeySerialization> KEY_PARSER;
    private static final KeySerializer<LegacyKmsEnvelopeAeadKey, ProtoKeySerialization> KEY_SERIALIZER;
    private static final ParametersParser<ProtoParametersSerialization> PARAMETERS_PARSER;
    private static final ParametersSerializer<LegacyKmsEnvelopeAeadParameters, ProtoParametersSerialization> PARAMETERS_SERIALIZER;
    private static final String TYPE_URL = "type.googleapis.com/google.crypto.tink.KmsEnvelopeAeadKey";
    private static final Bytes TYPE_URL_BYTES;

    static {
        Bytes bytesFromPrintableAscii = Util.toBytesFromPrintableAscii(TYPE_URL);
        TYPE_URL_BYTES = bytesFromPrintableAscii;
        PARAMETERS_SERIALIZER = ParametersSerializer.create(new ParametersSerializer.ParametersSerializationFunction() { // from class: com.google.crypto.tink.aead.LegacyKmsEnvelopeAeadProtoSerialization$$ExternalSyntheticLambda0
            @Override // com.google.crypto.tink.internal.ParametersSerializer.ParametersSerializationFunction
            public final Serialization serializeParameters(Parameters parameters) {
                return LegacyKmsEnvelopeAeadProtoSerialization.serializeParameters((LegacyKmsEnvelopeAeadParameters) parameters);
            }
        }, LegacyKmsEnvelopeAeadParameters.class, ProtoParametersSerialization.class);
        PARAMETERS_PARSER = ParametersParser.create(new ParametersParser.ParametersParsingFunction() { // from class: com.google.crypto.tink.aead.LegacyKmsEnvelopeAeadProtoSerialization$$ExternalSyntheticLambda1
            @Override // com.google.crypto.tink.internal.ParametersParser.ParametersParsingFunction
            public final Parameters parseParameters(Serialization serialization) {
                return LegacyKmsEnvelopeAeadProtoSerialization.parseParameters((ProtoParametersSerialization) serialization);
            }
        }, bytesFromPrintableAscii, ProtoParametersSerialization.class);
        KEY_SERIALIZER = KeySerializer.create(new KeySerializer.KeySerializationFunction() { // from class: com.google.crypto.tink.aead.LegacyKmsEnvelopeAeadProtoSerialization$$ExternalSyntheticLambda2
            @Override // com.google.crypto.tink.internal.KeySerializer.KeySerializationFunction
            public final Serialization serializeKey(Key key, SecretKeyAccess secretKeyAccess) {
                return LegacyKmsEnvelopeAeadProtoSerialization.serializeKey((LegacyKmsEnvelopeAeadKey) key, secretKeyAccess);
            }
        }, LegacyKmsEnvelopeAeadKey.class, ProtoKeySerialization.class);
        KEY_PARSER = KeyParser.create(new KeyParser.KeyParsingFunction() { // from class: com.google.crypto.tink.aead.LegacyKmsEnvelopeAeadProtoSerialization$$ExternalSyntheticLambda3
            @Override // com.google.crypto.tink.internal.KeyParser.KeyParsingFunction
            public final Key parseKey(Serialization serialization, SecretKeyAccess secretKeyAccess) {
                return LegacyKmsEnvelopeAeadProtoSerialization.parseKey((ProtoKeySerialization) serialization, secretKeyAccess);
            }
        }, bytesFromPrintableAscii, ProtoKeySerialization.class);
    }

    private static OutputPrefixType toProtoOutputPrefixType(LegacyKmsEnvelopeAeadParameters.Variant variant) throws GeneralSecurityException {
        if (LegacyKmsEnvelopeAeadParameters.Variant.TINK.equals(variant)) {
            return OutputPrefixType.TINK;
        }
        if (LegacyKmsEnvelopeAeadParameters.Variant.NO_PREFIX.equals(variant)) {
            return OutputPrefixType.RAW;
        }
        throw new GeneralSecurityException("Unable to serialize variant: " + variant);
    }

    /* JADX INFO: renamed from: com.google.crypto.tink.aead.LegacyKmsEnvelopeAeadProtoSerialization$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$google$crypto$tink$proto$OutputPrefixType;

        static {
            int[] iArr = new int[OutputPrefixType.values().length];
            $SwitchMap$com$google$crypto$tink$proto$OutputPrefixType = iArr;
            try {
                iArr[OutputPrefixType.TINK.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$proto$OutputPrefixType[OutputPrefixType.RAW.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    private static LegacyKmsEnvelopeAeadParameters.Variant toVariant(OutputPrefixType outputPrefixType) throws GeneralSecurityException {
        int i = AnonymousClass1.$SwitchMap$com$google$crypto$tink$proto$OutputPrefixType[outputPrefixType.ordinal()];
        if (i == 1) {
            return LegacyKmsEnvelopeAeadParameters.Variant.TINK;
        }
        if (i == 2) {
            return LegacyKmsEnvelopeAeadParameters.Variant.NO_PREFIX;
        }
        throw new GeneralSecurityException("Unable to parse OutputPrefixType: " + outputPrefixType.getNumber());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoParametersSerialization serializeParameters(LegacyKmsEnvelopeAeadParameters parameters) throws GeneralSecurityException {
        return ProtoParametersSerialization.create((KeyTemplate) KeyTemplate.newBuilder().setTypeUrl(TYPE_URL).setValue(serializeParametersToKmsEnvelopeAeadKeyFormat(parameters).toByteString()).setOutputPrefixType(toProtoOutputPrefixType(parameters.getVariant())).build());
    }

    private static KmsEnvelopeAeadKeyFormat serializeParametersToKmsEnvelopeAeadKeyFormat(LegacyKmsEnvelopeAeadParameters parameters) throws GeneralSecurityException {
        try {
            return (KmsEnvelopeAeadKeyFormat) KmsEnvelopeAeadKeyFormat.newBuilder().setKekUri(parameters.getKekUri()).setDekTemplate(KeyTemplate.parseFrom(TinkProtoParametersFormat.serialize(parameters.getDekParametersForNewKeys()), ExtensionRegistryLite.getEmptyRegistry())).build();
        } catch (InvalidProtocolBufferException e) {
            throw new GeneralSecurityException("Parsing KmsEnvelopeAeadKeyFormat failed: ", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ProtoKeySerialization serializeKey(LegacyKmsEnvelopeAeadKey key, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        return ProtoKeySerialization.create(TYPE_URL, ((KmsEnvelopeAeadKey) KmsEnvelopeAeadKey.newBuilder().setParams(serializeParametersToKmsEnvelopeAeadKeyFormat(key.getParameters())).build()).toByteString(), KeyData.KeyMaterialType.REMOTE, toProtoOutputPrefixType(key.getParameters().getVariant()), key.getIdRequirementOrNull());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static LegacyKmsEnvelopeAeadParameters parseParameters(ProtoParametersSerialization serialization) throws GeneralSecurityException {
        if (!serialization.getKeyTemplate().getTypeUrl().equals(TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to LegacyKmsEnvelopeAeadProtoSerialization.parseParameters: " + serialization.getKeyTemplate().getTypeUrl());
        }
        try {
            return parseParameters(KmsEnvelopeAeadKeyFormat.parseFrom(serialization.getKeyTemplate().getValue(), ExtensionRegistryLite.getEmptyRegistry()), serialization.getKeyTemplate().getOutputPrefixType());
        } catch (InvalidProtocolBufferException e) {
            throw new GeneralSecurityException("Parsing KmsEnvelopeAeadKeyFormat failed: ", e);
        }
    }

    private static LegacyKmsEnvelopeAeadParameters parseParameters(KmsEnvelopeAeadKeyFormat format, OutputPrefixType outputPrefixType) throws GeneralSecurityException {
        LegacyKmsEnvelopeAeadParameters.DekParsingStrategy dekParsingStrategy;
        Parameters parameters = TinkProtoParametersFormat.parse(((KeyTemplate) KeyTemplate.newBuilder().setTypeUrl(format.getDekTemplate().getTypeUrl()).setValue(format.getDekTemplate().getValue()).setOutputPrefixType(OutputPrefixType.RAW).build()).toByteArray());
        if (parameters instanceof AesGcmParameters) {
            dekParsingStrategy = LegacyKmsEnvelopeAeadParameters.DekParsingStrategy.ASSUME_AES_GCM;
        } else if (parameters instanceof ChaCha20Poly1305Parameters) {
            dekParsingStrategy = LegacyKmsEnvelopeAeadParameters.DekParsingStrategy.ASSUME_CHACHA20POLY1305;
        } else if (parameters instanceof XChaCha20Poly1305Parameters) {
            dekParsingStrategy = LegacyKmsEnvelopeAeadParameters.DekParsingStrategy.ASSUME_XCHACHA20POLY1305;
        } else if (parameters instanceof AesCtrHmacAeadParameters) {
            dekParsingStrategy = LegacyKmsEnvelopeAeadParameters.DekParsingStrategy.ASSUME_AES_CTR_HMAC;
        } else if (parameters instanceof AesEaxParameters) {
            dekParsingStrategy = LegacyKmsEnvelopeAeadParameters.DekParsingStrategy.ASSUME_AES_EAX;
        } else if (parameters instanceof AesGcmSivParameters) {
            dekParsingStrategy = LegacyKmsEnvelopeAeadParameters.DekParsingStrategy.ASSUME_AES_GCM_SIV;
        } else {
            throw new GeneralSecurityException("Unsupported DEK parameters when parsing " + parameters);
        }
        return LegacyKmsEnvelopeAeadParameters.builder().setVariant(toVariant(outputPrefixType)).setKekUri(format.getKekUri()).setDekParametersForNewKeys((AeadParameters) parameters).setDekParsingStrategy(dekParsingStrategy).build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static LegacyKmsEnvelopeAeadKey parseKey(ProtoKeySerialization serialization, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        if (!serialization.getTypeUrl().equals(TYPE_URL)) {
            throw new IllegalArgumentException("Wrong type URL in call to LegacyKmsEnvelopeAeadProtoSerialization.parseKey");
        }
        try {
            KmsEnvelopeAeadKey from = KmsEnvelopeAeadKey.parseFrom(serialization.getValue(), ExtensionRegistryLite.getEmptyRegistry());
            if (from.getVersion() != 0) {
                throw new GeneralSecurityException("KmsEnvelopeAeadKeys are only accepted with version 0, got " + from);
            }
            return LegacyKmsEnvelopeAeadKey.create(parseParameters(from.getParams(), serialization.getOutputPrefixType()), serialization.getIdRequirementOrNull());
        } catch (InvalidProtocolBufferException e) {
            throw new GeneralSecurityException("Parsing KmsEnvelopeAeadKey failed: ", e);
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

    private LegacyKmsEnvelopeAeadProtoSerialization() {
    }
}
