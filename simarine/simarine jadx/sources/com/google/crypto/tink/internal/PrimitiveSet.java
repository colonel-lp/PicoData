package com.google.crypto.tink.internal;

import com.google.crypto.tink.CryptoFormat;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.KeyStatus;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.proto.KeyStatusType;
import com.google.crypto.tink.proto.Keyset;
import com.google.crypto.tink.util.Bytes;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class PrimitiveSet<P> {
    private final MonitoringAnnotations annotations;
    private final Map<Bytes, List<Entry<P>>> entries;
    private final List<Entry<P>> entriesInKeysetOrder;
    private final Entry<P> primary;
    private final Class<P> primitiveClass;
    private final PrimitiveConstructor.PrimitiveConstructionFunction<Key, P> primitiveConstructionFunction;

    public static final class Entry<P> implements KeysetHandleInterface.Entry {
        private final boolean isPrimary;
        private final Key key;
        private final int keyId;
        private final Bytes outputPrefix;
        private final KeyStatus status;

        private Entry(final Bytes outputPrefix, KeyStatus status, int keyId, Key key, boolean isPrimary) {
            this.outputPrefix = outputPrefix;
            this.status = status;
            this.keyId = keyId;
            this.key = key;
            this.isPrimary = isPrimary;
        }

        @Override // com.google.crypto.tink.internal.KeysetHandleInterface.Entry
        public KeyStatus getStatus() {
            return this.status;
        }

        final Bytes getOutputPrefix() {
            return this.outputPrefix;
        }

        @Override // com.google.crypto.tink.internal.KeysetHandleInterface.Entry
        public int getId() {
            return this.keyId;
        }

        @Override // com.google.crypto.tink.internal.KeysetHandleInterface.Entry
        public Key getKey() {
            return this.key;
        }

        @Nullable
        public Parameters getParameters() {
            Key key = this.key;
            if (key == null) {
                return null;
            }
            return key.getParameters();
        }

        @Override // com.google.crypto.tink.internal.KeysetHandleInterface.Entry
        public boolean isPrimary() {
            return this.isPrimary;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static <P> void storeEntryInPrimitiveSet(Entry<P> entry, Map<Bytes, List<Entry<P>>> entries, List<Entry<P>> entriesInKeysetOrder) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(entry);
        List<Entry<P>> listPut = entries.put(entry.getOutputPrefix(), Collections.unmodifiableList(arrayList));
        if (listPut != null) {
            ArrayList arrayList2 = new ArrayList();
            arrayList2.addAll(listPut);
            arrayList2.add(entry);
            entries.put(entry.getOutputPrefix(), Collections.unmodifiableList(arrayList2));
        }
        entriesInKeysetOrder.add(entry);
    }

    @Nullable
    Entry<P> getPrimary() {
        return this.primary;
    }

    public MonitoringAnnotations getAnnotations() {
        return this.annotations;
    }

    private class KeysetHandleImpl implements KeysetHandleInterface {
        private KeysetHandleImpl() {
        }

        @Override // com.google.crypto.tink.internal.KeysetHandleInterface
        public KeysetHandleInterface.Entry getPrimary() {
            return PrimitiveSet.this.primary;
        }

        @Override // com.google.crypto.tink.internal.KeysetHandleInterface
        public int size() {
            return PrimitiveSet.this.entriesInKeysetOrder.size();
        }

        @Override // com.google.crypto.tink.internal.KeysetHandleInterface
        public KeysetHandleInterface.Entry getAt(int i) {
            return (KeysetHandleInterface.Entry) PrimitiveSet.this.entriesInKeysetOrder.get(i);
        }
    }

    public KeysetHandleInterface getKeysetHandle() {
        return new KeysetHandleImpl();
    }

    public P getPrimitiveForEntry(KeysetHandleInterface.Entry entry) throws GeneralSecurityException {
        return this.primitiveConstructionFunction.constructPrimitive(entry.getKey());
    }

    Collection<List<Entry<P>>> getAll() {
        return this.entries.values();
    }

    List<Entry<P>> getAllInKeysetOrder() {
        return Collections.unmodifiableList(this.entriesInKeysetOrder);
    }

    private PrimitiveSet(Map<Bytes, List<Entry<P>>> entries, List<Entry<P>> entriesInKeysetOrder, Entry<P> primary, MonitoringAnnotations annotations, PrimitiveConstructor.PrimitiveConstructionFunction<Key, P> primitiveConstructionFunction, Class<P> primitiveClass) {
        this.entries = entries;
        this.entriesInKeysetOrder = entriesInKeysetOrder;
        this.primary = primary;
        this.primitiveConstructionFunction = primitiveConstructionFunction;
        this.primitiveClass = primitiveClass;
        this.annotations = annotations;
    }

    public Class<P> getPrimitiveClass() {
        return this.primitiveClass;
    }

    public static class Builder<P> {
        private MonitoringAnnotations annotations;
        private Map<Bytes, List<Entry<P>>> entries;
        private final List<Entry<P>> entriesInKeysetOrder;
        private Entry<P> primary;
        private final Class<P> primitiveClass;
        private PrimitiveConstructor.PrimitiveConstructionFunction<Key, P> primitiveConstructionFunction;

        static /* synthetic */ Object lambda$new$0(Key key) throws GeneralSecurityException {
            throw new GeneralSecurityException("No PrimitiveConstructionFunction specified");
        }

        private Builder<P> addEntry(Key key, Keyset.Key protoKey, boolean asPrimary) throws GeneralSecurityException {
            if (this.entries == null) {
                throw new IllegalStateException("addEntry cannot be called after build");
            }
            if (protoKey.getStatus() != KeyStatusType.ENABLED) {
                throw new GeneralSecurityException("only ENABLED key is allowed");
            }
            Entry<P> entry = new Entry<>(Bytes.copyFrom(CryptoFormat.getOutputPrefix(protoKey)), KeyStatus.ENABLED, protoKey.getKeyId(), key, asPrimary);
            PrimitiveSet.storeEntryInPrimitiveSet(entry, this.entries, this.entriesInKeysetOrder);
            if (asPrimary) {
                if (this.primary != null) {
                    throw new IllegalStateException("you cannot set two primary primitives");
                }
                this.primary = entry;
            }
            return this;
        }

        public Builder<P> add(Key key, Keyset.Key protoKey) throws GeneralSecurityException {
            return addEntry(key, protoKey, false);
        }

        public Builder<P> addPrimary(Key key, Keyset.Key protoKey) throws GeneralSecurityException {
            return addEntry(key, protoKey, true);
        }

        public Builder<P> setAnnotations(MonitoringAnnotations annotations) {
            if (this.entries == null) {
                throw new IllegalStateException("setAnnotations cannot be called after build");
            }
            this.annotations = annotations;
            return this;
        }

        public Builder<P> addPrimitiveConstructor(PrimitiveConstructor.PrimitiveConstructionFunction<Key, P> primitiveConstructionFunction) {
            this.primitiveConstructionFunction = primitiveConstructionFunction;
            return this;
        }

        public PrimitiveSet<P> build() throws GeneralSecurityException {
            Map<Bytes, List<Entry<P>>> map = this.entries;
            if (map == null) {
                throw new IllegalStateException("build cannot be called twice");
            }
            PrimitiveSet<P> primitiveSet = new PrimitiveSet<>(map, this.entriesInKeysetOrder, this.primary, this.annotations, this.primitiveConstructionFunction, this.primitiveClass);
            this.entries = null;
            return primitiveSet;
        }

        private Builder(Class<P> primitiveClass) {
            this.entries = new HashMap();
            this.entriesInKeysetOrder = new ArrayList();
            this.primitiveConstructionFunction = new PrimitiveConstructor.PrimitiveConstructionFunction() { // from class: com.google.crypto.tink.internal.PrimitiveSet$Builder$$ExternalSyntheticLambda0
                @Override // com.google.crypto.tink.internal.PrimitiveConstructor.PrimitiveConstructionFunction
                public final Object constructPrimitive(Key key) {
                    return PrimitiveSet.Builder.lambda$new$0(key);
                }
            };
            this.primitiveClass = primitiveClass;
            this.annotations = MonitoringAnnotations.EMPTY;
        }
    }

    public static <P> Builder<P> newBuilder(Class<P> primitiveClass) {
        return new Builder<>(primitiveClass);
    }
}
