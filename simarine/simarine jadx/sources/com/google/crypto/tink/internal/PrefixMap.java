package com.google.crypto.tink.internal;

import com.google.crypto.tink.util.Bytes;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class PrefixMap<P> {
    private static final Bytes EMPTY_BYTES = Bytes.copyFrom(new byte[0]);
    private final Map<Bytes, List<P>> entries;

    public static class Builder<P> {
        private final Map<Bytes, List<P>> entries = new HashMap();

        public Builder<P> put(Bytes prefix, P primitive) throws GeneralSecurityException {
            List<P> list;
            if (prefix.size() != 0 && prefix.size() != 5) {
                throw new GeneralSecurityException("PrefixMap only supports 0 and 5 byte prefixes");
            }
            if (this.entries.containsKey(prefix)) {
                list = this.entries.get(prefix);
            } else {
                ArrayList arrayList = new ArrayList();
                this.entries.put(prefix, arrayList);
                list = arrayList;
            }
            list.add(primitive);
            return this;
        }

        public PrefixMap<P> build() {
            return new PrefixMap<>(this.entries);
        }
    }

    private static class ConcatenatedIterator<P> implements Iterator<P> {
        private final Iterator<P> it0;
        private final Iterator<P> it1;

        private ConcatenatedIterator(Iterator<P> it0, Iterator<P> it1) {
            this.it0 = it0;
            this.it1 = it1;
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.it0.hasNext() || this.it1.hasNext();
        }

        @Override // java.util.Iterator
        public P next() {
            if (this.it0.hasNext()) {
                return this.it0.next();
            }
            return this.it1.next();
        }
    }

    public Iterable<P> getAllWithMatchingPrefix(byte[] text) {
        final List<P> list = this.entries.get(EMPTY_BYTES);
        final List<P> list2 = text.length >= 5 ? this.entries.get(Bytes.copyFrom(text, 0, 5)) : null;
        if (list == null && list2 == null) {
            return new ArrayList();
        }
        if (list == null) {
            return list2;
        }
        return list2 == null ? list : new Iterable<P>() { // from class: com.google.crypto.tink.internal.PrefixMap.1
            @Override // java.lang.Iterable
            public Iterator<P> iterator() {
                return new ConcatenatedIterator(list2.iterator(), list.iterator());
            }
        };
    }

    private PrefixMap(Map<Bytes, List<P>> entries) {
        this.entries = entries;
    }
}
