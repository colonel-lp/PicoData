package com.google.crypto.tink.daead;

import com.google.crypto.tink.DeterministicAead;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.daead.internal.LegacyFullDeterministicAead;
import com.google.crypto.tink.internal.KeysetHandleInterface;
import com.google.crypto.tink.internal.LegacyProtoKey;
import com.google.crypto.tink.internal.MonitoringClient;
import com.google.crypto.tink.internal.MonitoringUtil;
import com.google.crypto.tink.internal.MutableMonitoringRegistry;
import com.google.crypto.tink.internal.MutablePrimitiveRegistry;
import com.google.crypto.tink.internal.PrefixMap;
import com.google.crypto.tink.internal.PrimitiveConstructor;
import com.google.crypto.tink.internal.PrimitiveRegistry;
import com.google.crypto.tink.internal.PrimitiveSet;
import com.google.crypto.tink.internal.PrimitiveWrapper;
import com.google.crypto.tink.util.Bytes;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public class DeterministicAeadWrapper implements PrimitiveWrapper<DeterministicAead, DeterministicAead> {
    private static final DeterministicAeadWrapper WRAPPER = new DeterministicAeadWrapper();
    private static final PrimitiveConstructor<LegacyProtoKey, DeterministicAead> LEGACY_FULL_DAEAD_PRIMITIVE_CONSTRUCTOR = PrimitiveConstructor.create(new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.daead.DeterministicAeadWrapper$$ExternalSyntheticLambda0
        @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
        public final Object constructPrimitive(Key key) {
            return LegacyFullDeterministicAead.create((LegacyProtoKey) key);
        }
    }, LegacyProtoKey.class, DeterministicAead.class);

    private static class DeterministicAeadWithId {
        public final DeterministicAead daead;
        public final int id;

        public DeterministicAeadWithId(DeterministicAead daead, int id) {
            this.daead = daead;
            this.id = id;
        }
    }

    private static Bytes getOutputPrefix(Key key) throws GeneralSecurityException {
        if (key instanceof DeterministicAeadKey) {
            return ((DeterministicAeadKey) key).getOutputPrefix();
        }
        if (key instanceof LegacyProtoKey) {
            return ((LegacyProtoKey) key).getOutputPrefix();
        }
        throw new GeneralSecurityException("Cannot get output prefix for key of class " + key.getClass().getName() + " with parameters " + key.getParameters());
    }

    private static class WrappedDeterministicAead implements DeterministicAead {
        private final PrefixMap<DeterministicAeadWithId> allDaeads;
        private final MonitoringClient.Logger decLogger;
        private final MonitoringClient.Logger encLogger;
        private final DeterministicAeadWithId primary;

        public WrappedDeterministicAead(DeterministicAeadWithId primary, PrefixMap<DeterministicAeadWithId> allDaeads, MonitoringClient.Logger encLogger, MonitoringClient.Logger decLogger) {
            this.primary = primary;
            this.allDaeads = allDaeads;
            this.encLogger = encLogger;
            this.decLogger = decLogger;
        }

        @Override // com.google.crypto.tink.DeterministicAead
        public byte[] encryptDeterministically(final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
            try {
                byte[] bArrEncryptDeterministically = this.primary.daead.encryptDeterministically(plaintext, associatedData);
                this.encLogger.log(this.primary.id, plaintext.length);
                return bArrEncryptDeterministically;
            } catch (GeneralSecurityException e) {
                this.encLogger.logFailure();
                throw e;
            }
        }

        @Override // com.google.crypto.tink.DeterministicAead
        public byte[] decryptDeterministically(final byte[] ciphertext, final byte[] associatedData) throws GeneralSecurityException {
            for (DeterministicAeadWithId deterministicAeadWithId : this.allDaeads.getAllWithMatchingPrefix(ciphertext)) {
                try {
                    byte[] bArrDecryptDeterministically = deterministicAeadWithId.daead.decryptDeterministically(ciphertext, associatedData);
                    this.decLogger.log(deterministicAeadWithId.id, ciphertext.length);
                    return bArrDecryptDeterministically;
                } catch (GeneralSecurityException unused) {
                }
            }
            this.decLogger.logFailure();
            throw new GeneralSecurityException("decryption failed");
        }
    }

    DeterministicAeadWrapper() {
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.google.crypto.tink.internal.PrimitiveWrapper
    public DeterministicAead wrap(final PrimitiveSet<DeterministicAead> primitives) throws GeneralSecurityException {
        MonitoringClient.Logger loggerCreateLogger;
        MonitoringClient.Logger loggerCreateLogger2;
        PrefixMap.Builder builder = new PrefixMap.Builder();
        KeysetHandleInterface keysetHandle = primitives.getKeysetHandle();
        for (int i = 0; i < keysetHandle.size(); i++) {
            KeysetHandleInterface.Entry at = keysetHandle.getAt(i);
            builder.put(getOutputPrefix(at.getKey()), new DeterministicAeadWithId(primitives.getPrimitiveForEntry(at), at.getId()));
        }
        if (!primitives.getAnnotations().isEmpty()) {
            MonitoringClient monitoringClient = MutableMonitoringRegistry.globalInstance().getMonitoringClient();
            loggerCreateLogger = monitoringClient.createLogger(keysetHandle, primitives.getAnnotations(), "daead", "encrypt");
            loggerCreateLogger2 = monitoringClient.createLogger(keysetHandle, primitives.getAnnotations(), "daead", "decrypt");
        } else {
            loggerCreateLogger = MonitoringUtil.DO_NOTHING_LOGGER;
            loggerCreateLogger2 = MonitoringUtil.DO_NOTHING_LOGGER;
        }
        return new WrappedDeterministicAead(new DeterministicAeadWithId(primitives.getPrimitiveForEntry(keysetHandle.getPrimary()), keysetHandle.getPrimary().getId()), builder.build(), loggerCreateLogger, loggerCreateLogger2);
    }

    @Override // com.google.crypto.tink.internal.PrimitiveWrapper
    public Class<DeterministicAead> getPrimitiveClass() {
        return DeterministicAead.class;
    }

    @Override // com.google.crypto.tink.internal.PrimitiveWrapper
    public Class<DeterministicAead> getInputPrimitiveClass() {
        return DeterministicAead.class;
    }

    public static void register() throws GeneralSecurityException {
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveWrapper(WRAPPER);
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveConstructor(LEGACY_FULL_DAEAD_PRIMITIVE_CONSTRUCTOR);
    }

    public static void registerToInternalPrimitiveRegistry(PrimitiveRegistry.Builder primitiveRegistryBuilder) throws GeneralSecurityException {
        primitiveRegistryBuilder.registerPrimitiveWrapper(WRAPPER);
    }
}
