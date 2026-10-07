package com.google.crypto.tink.aead;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.KeyManager;
import com.google.crypto.tink.KeyTemplate;
import com.google.crypto.tink.KmsClients;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.aead.internal.LegacyFullAead;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.KeyManagerRegistry;
import com.google.crypto.tink.internal.LegacyKeyManagerImpl;
import com.google.crypto.tink.internal.MutableKeyCreationRegistry;
import com.google.crypto.tink.internal.MutablePrimitiveRegistry;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.proto.KmsEnvelopeAeadKey;
import java.security.GeneralSecurityException;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public class KmsEnvelopeAeadKeyManager {
    private static final String TYPE_URL = "type.googleapis.com/google.crypto.tink.KmsEnvelopeAeadKey";
    private static final KeyManager<Aead> legacyKeyManager = LegacyKeyManagerImpl.create(getKeyType(), Aead.class, KeyData.KeyMaterialType.SYMMETRIC, KmsEnvelopeAeadKey.parser());
    private static final MutableKeyCreationRegistry.KeyCreator<LegacyKmsEnvelopeAeadParameters> KEY_CREATOR = new MutableKeyCreationRegistry.KeyCreator() { // from class: com.google.crypto.tink.aead.KmsEnvelopeAeadKeyManager$$ExternalSyntheticLambda0
        @Override // com.google.crypto.tink.internal.MutableKeyCreationRegistry.KeyCreator
        public final Key createKey(Parameters parameters, Integer num) {
            return KmsEnvelopeAeadKeyManager.newKey((LegacyKmsEnvelopeAeadParameters) parameters, num);
        }
    };
    private static final PrimitiveConstructor<LegacyKmsEnvelopeAeadKey, Aead> LEGACY_KMS_ENVELOPE_AEAD_PRIMITIVE_CONSTRUCTOR = PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.aead.KmsEnvelopeAeadKeyManager$$ExternalSyntheticLambda1
        @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
        public final Object constructPrimitive(Key key) {
            return KmsEnvelopeAeadKeyManager.create((LegacyKmsEnvelopeAeadKey) key);
        }
    }, LegacyKmsEnvelopeAeadKey.class, Aead.class);

    /* JADX INFO: Access modifiers changed from: private */
    public static LegacyKmsEnvelopeAeadKey newKey(LegacyKmsEnvelopeAeadParameters parameters, @Nullable Integer idRequirement) throws GeneralSecurityException {
        return LegacyKmsEnvelopeAeadKey.create(parameters, idRequirement);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Aead create(LegacyKmsEnvelopeAeadKey key) throws GeneralSecurityException {
        String kekUri = key.getParameters().getKekUri();
        return LegacyFullAead.create(KmsEnvelopeAead.create(key.getParameters().getDekParametersForNewKeys(), KmsClients.get(kekUri).getAead(kekUri)), key.getOutputPrefix());
    }

    static String getKeyType() {
        return TYPE_URL;
    }

    private static AeadParameters makeRawAesGcm(AesGcmParameters parameters) throws GeneralSecurityException {
        return AesGcmParameters.builder().setIvSizeBytes(parameters.getIvSizeBytes()).setKeySizeBytes(parameters.getKeySizeBytes()).setTagSizeBytes(parameters.getTagSizeBytes()).setVariant(AesGcmParameters.Variant.NO_PREFIX).build();
    }

    private static AeadParameters makeRawChaCha20Poly1305() {
        return ChaCha20Poly1305Parameters.create(ChaCha20Poly1305Parameters.Variant.NO_PREFIX);
    }

    private static AeadParameters makeRawXChaCha20Poly1305() {
        return XChaCha20Poly1305Parameters.create(XChaCha20Poly1305Parameters.Variant.NO_PREFIX);
    }

    private static AeadParameters makeRawAesCtrHmacAead(AesCtrHmacAeadParameters parameters) throws GeneralSecurityException {
        return AesCtrHmacAeadParameters.builder().setAesKeySizeBytes(parameters.getAesKeySizeBytes()).setHmacKeySizeBytes(parameters.getHmacKeySizeBytes()).setTagSizeBytes(parameters.getTagSizeBytes()).setIvSizeBytes(parameters.getIvSizeBytes()).setHashType(parameters.getHashType()).setVariant(AesCtrHmacAeadParameters.Variant.NO_PREFIX).build();
    }

    private static AeadParameters makeRawAesEax(AesEaxParameters parameters) throws GeneralSecurityException {
        return AesEaxParameters.builder().setIvSizeBytes(parameters.getIvSizeBytes()).setKeySizeBytes(parameters.getKeySizeBytes()).setTagSizeBytes(parameters.getTagSizeBytes()).setVariant(AesEaxParameters.Variant.NO_PREFIX).build();
    }

    private static AeadParameters makeRawAesGcmSiv(AesGcmSivParameters parameters) throws GeneralSecurityException {
        return AesGcmSivParameters.builder().setKeySizeBytes(parameters.getKeySizeBytes()).setVariant(AesGcmSivParameters.Variant.NO_PREFIX).build();
    }

    private static AeadParameters makeRaw(Parameters parameters) throws GeneralSecurityException {
        if (parameters instanceof AesGcmParameters) {
            return makeRawAesGcm((AesGcmParameters) parameters);
        }
        if (parameters instanceof ChaCha20Poly1305Parameters) {
            return makeRawChaCha20Poly1305();
        }
        if (parameters instanceof XChaCha20Poly1305Parameters) {
            return makeRawXChaCha20Poly1305();
        }
        if (parameters instanceof AesCtrHmacAeadParameters) {
            return makeRawAesCtrHmacAead((AesCtrHmacAeadParameters) parameters);
        }
        if (parameters instanceof AesEaxParameters) {
            return makeRawAesEax((AesEaxParameters) parameters);
        }
        if (parameters instanceof AesGcmSivParameters) {
            return makeRawAesGcmSiv((AesGcmSivParameters) parameters);
        }
        throw new IllegalArgumentException("Illegal parameters" + parameters);
    }

    private static LegacyKmsEnvelopeAeadParameters.DekParsingStrategy getRequiredParsingStrategy(AeadParameters parameters) {
        if (parameters instanceof AesGcmParameters) {
            return LegacyKmsEnvelopeAeadParameters.DekParsingStrategy.ASSUME_AES_GCM;
        }
        if (parameters instanceof ChaCha20Poly1305Parameters) {
            return LegacyKmsEnvelopeAeadParameters.DekParsingStrategy.ASSUME_CHACHA20POLY1305;
        }
        if (parameters instanceof XChaCha20Poly1305Parameters) {
            return LegacyKmsEnvelopeAeadParameters.DekParsingStrategy.ASSUME_XCHACHA20POLY1305;
        }
        if (parameters instanceof AesCtrHmacAeadParameters) {
            return LegacyKmsEnvelopeAeadParameters.DekParsingStrategy.ASSUME_AES_CTR_HMAC;
        }
        if (parameters instanceof AesEaxParameters) {
            return LegacyKmsEnvelopeAeadParameters.DekParsingStrategy.ASSUME_AES_EAX;
        }
        if (parameters instanceof AesGcmSivParameters) {
            return LegacyKmsEnvelopeAeadParameters.DekParsingStrategy.ASSUME_AES_GCM_SIV;
        }
        throw new IllegalArgumentException("Illegal parameters" + parameters);
    }

    public static KeyTemplate createKeyTemplate(String kekUri, KeyTemplate dekTemplate) {
        try {
            AeadParameters aeadParametersMakeRaw = makeRaw(dekTemplate.toParameters());
            return KeyTemplate.createFrom(LegacyKmsEnvelopeAeadParameters.builder().setKekUri(kekUri).setDekParsingStrategy(getRequiredParsingStrategy(aeadParametersMakeRaw)).setDekParametersForNewKeys(aeadParametersMakeRaw).build());
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Cannot create LegacyKmsEnvelopeAeadParameters for template: " + dekTemplate, e);
        }
    }

    public static void register(boolean newKeyAllowed) throws GeneralSecurityException {
        if (!TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_NOT_FIPS.isCompatible()) {
            throw new GeneralSecurityException("Registering KMS Envelope AEAD is not supported in FIPS mode");
        }
        LegacyKmsEnvelopeAeadProtoSerialization.register();
        MutableKeyCreationRegistry.globalInstance().add(KEY_CREATOR, LegacyKmsEnvelopeAeadParameters.class);
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveConstructor(LEGACY_KMS_ENVELOPE_AEAD_PRIMITIVE_CONSTRUCTOR);
        KeyManagerRegistry.globalInstance().registerKeyManager(legacyKeyManager, newKeyAllowed);
    }

    private KmsEnvelopeAeadKeyManager() {
    }
}
