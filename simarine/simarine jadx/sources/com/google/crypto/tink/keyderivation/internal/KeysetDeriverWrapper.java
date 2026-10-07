package com.google.crypto.tink.keyderivation.internal;

import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.internal.KeysetHandleInterface;
import com.google.crypto.tink.internal.MutablePrimitiveRegistry;
import com.google.crypto.tink.internal.PrimitiveSet;
import com.google.crypto.tink.internal.PrimitiveWrapper;
import com.google.crypto.tink.keyderivation.KeysetDeriver;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public final class KeysetDeriverWrapper implements PrimitiveWrapper<KeyDeriver, KeysetDeriver> {
    private static final KeysetDeriverWrapper WRAPPER = new KeysetDeriverWrapper();

    private static class DeriverWithId {
        final KeyDeriver deriver;
        final int id;
        final boolean isPrimary;

        DeriverWithId(KeyDeriver deriver, int id, boolean isPrimary) {
            this.deriver = deriver;
            this.id = id;
            this.isPrimary = isPrimary;
        }
    }

    private static void validate(KeysetHandleInterface keysetHandle) throws GeneralSecurityException {
        if (keysetHandle.getPrimary() == null) {
            throw new GeneralSecurityException("Primitive set has no primary.");
        }
    }

    @Immutable
    private static class WrappedKeysetDeriver implements KeysetDeriver {
        private final List<DeriverWithId> derivers;

        private WrappedKeysetDeriver(List<DeriverWithId> derivers) {
            this.derivers = derivers;
        }

        private static KeysetHandle.Builder.Entry deriveAndGetEntry(byte[] salt, DeriverWithId deriverWithId) throws GeneralSecurityException {
            if (deriverWithId.deriver == null) {
                throw new GeneralSecurityException("Primitive set has non-full primitives -- this is probably a bug");
            }
            KeysetHandle.Builder.Entry entryImportKey = KeysetHandle.importKey(deriverWithId.deriver.deriveKey(salt));
            entryImportKey.withFixedId(deriverWithId.id);
            if (deriverWithId.isPrimary) {
                entryImportKey.makePrimary();
            }
            return entryImportKey;
        }

        @Override // com.google.crypto.tink.keyderivation.KeysetDeriver
        public KeysetHandle deriveKeyset(byte[] salt) throws GeneralSecurityException {
            KeysetHandle.Builder builderNewBuilder = KeysetHandle.newBuilder();
            Iterator<DeriverWithId> it = this.derivers.iterator();
            while (it.hasNext()) {
                builderNewBuilder.addEntry(deriveAndGetEntry(salt, it.next()));
            }
            return builderNewBuilder.build();
        }
    }

    KeysetDeriverWrapper() {
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.google.crypto.tink.internal.PrimitiveWrapper
    public KeysetDeriver wrap(final PrimitiveSet<KeyDeriver> primitiveSet) throws GeneralSecurityException {
        validate(primitiveSet.getKeysetHandle());
        KeysetHandleInterface keysetHandle = primitiveSet.getKeysetHandle();
        ArrayList arrayList = new ArrayList(keysetHandle.size());
        for (int i = 0; i < keysetHandle.size(); i++) {
            KeysetHandleInterface.Entry at = keysetHandle.getAt(i);
            arrayList.add(new DeriverWithId(primitiveSet.getPrimitiveForEntry(at), at.getId(), at.isPrimary()));
        }
        return new WrappedKeysetDeriver(arrayList);
    }

    @Override // com.google.crypto.tink.internal.PrimitiveWrapper
    public Class<KeysetDeriver> getPrimitiveClass() {
        return KeysetDeriver.class;
    }

    @Override // com.google.crypto.tink.internal.PrimitiveWrapper
    public Class<KeyDeriver> getInputPrimitiveClass() {
        return KeyDeriver.class;
    }

    public static void register() throws GeneralSecurityException {
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveWrapper(WRAPPER);
    }
}
