package kotlinx.atomicfu;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.reflect.KProperty;

/* JADX INFO: compiled from: AtomicFU.kt */
/* JADX INFO: loaded from: classes2.dex */
@Metadata(d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\r\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u0000 #2\u00020\u0001:\u0001#B\u0019\b\u0000\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u001f\u0010\n\u001a\u00020\u00032\b\u0010\u000e\u001a\u0004\u0018\u00010\u00012\n\u0010\u000f\u001a\u0006\u0012\u0002\b\u00030\u0010H\u0087\nJ'\u0010\f\u001a\u00020\u00112\b\u0010\u000e\u001a\u0004\u0018\u00010\u00012\n\u0010\u000f\u001a\u0006\u0012\u0002\b\u00030\u00102\u0006\u0010\u0002\u001a\u00020\u0003H\u0087\nJ\u000e\u0010\u0012\u001a\u00020\u00112\u0006\u0010\u0002\u001a\u00020\u0003J\u0016\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00032\u0006\u0010\u0016\u001a\u00020\u0003J\u000e\u0010\u0017\u001a\u00020\u00032\u0006\u0010\u0002\u001a\u00020\u0003J\u0006\u0010\u0018\u001a\u00020\u0003J\u0006\u0010\u0019\u001a\u00020\u0003J\u000e\u0010\u001a\u001a\u00020\u00032\u0006\u0010\u001b\u001a\u00020\u0003J\u000e\u0010\u001c\u001a\u00020\u00032\u0006\u0010\u001b\u001a\u00020\u0003J\u0006\u0010\u001d\u001a\u00020\u0003J\u0006\u0010\u001e\u001a\u00020\u0003J\u0011\u0010\u001f\u001a\u00020\u00112\u0006\u0010\u001b\u001a\u00020\u0003H\u0086\nJ\u0011\u0010 \u001a\u00020\u00112\u0006\u0010\u001b\u001a\u00020\u0003H\u0086\nJ\b\u0010!\u001a\u00020\"H\u0016R\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR$\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0002\u001a\u00020\u0003@FX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\r¨\u0006$"}, d2 = {"Lkotlinx/atomicfu/AtomicLong;", "", "value", "", "trace", "Lkotlinx/atomicfu/TraceBase;", "<init>", "(JLkotlinx/atomicfu/TraceBase;)V", "getTrace", "()Lkotlinx/atomicfu/TraceBase;", "getValue", "()J", "setValue", "(J)V", "thisRef", "property", "Lkotlin/reflect/KProperty;", "", "lazySet", "compareAndSet", "", "expect", "update", "getAndSet", "getAndIncrement", "getAndDecrement", "getAndAdd", "delta", "addAndGet", "incrementAndGet", "decrementAndGet", "plusAssign", "minusAssign", "toString", "", "Companion", "atomicfu"}, k = 1, mv = {2, 0, 0}, xi = 48)
public final class AtomicLong {
    private static final Companion Companion = new Companion(null);
    private static final AtomicLongFieldUpdater<AtomicLong> FU = AtomicLongFieldUpdater.newUpdater(AtomicLong.class, "value");
    private final TraceBase trace;
    private volatile long value;

    public AtomicLong(long j, TraceBase trace) {
        Intrinsics.checkNotNullParameter(trace, "trace");
        this.trace = trace;
        this.value = j;
    }

    public final TraceBase getTrace() {
        return this.trace;
    }

    public final long getValue() {
        return this.value;
    }

    public final void setValue(long j) {
        this.value = j;
        if (this.trace != TraceBase.None.INSTANCE) {
            this.trace.append("set(" + j + ')');
        }
    }

    private final long getValue(Object thisRef, KProperty<?> property) {
        Intrinsics.checkNotNullParameter(property, "property");
        return getValue();
    }

    private final void setValue(Object thisRef, KProperty<?> property, long value) {
        Intrinsics.checkNotNullParameter(property, "property");
        setValue(value);
    }

    public final void lazySet(long value) {
        FU.lazySet(this, value);
        if (this.trace != TraceBase.None.INSTANCE) {
            this.trace.append("lazySet(" + value + ')');
        }
    }

    public final boolean compareAndSet(long expect, long update) {
        boolean zCompareAndSet = FU.compareAndSet(this, expect, update);
        if (zCompareAndSet && this.trace != TraceBase.None.INSTANCE) {
            this.trace.append("CAS(" + expect + ", " + update + ')');
        }
        return zCompareAndSet;
    }

    public final long getAndSet(long value) {
        long andSet = FU.getAndSet(this, value);
        if (this.trace != TraceBase.None.INSTANCE) {
            this.trace.append("getAndSet(" + value + "):" + andSet);
        }
        return andSet;
    }

    public final long getAndIncrement() {
        long andIncrement = FU.getAndIncrement(this);
        if (this.trace != TraceBase.None.INSTANCE) {
            this.trace.append("getAndInc():" + andIncrement);
        }
        return andIncrement;
    }

    public final long getAndDecrement() {
        long andDecrement = FU.getAndDecrement(this);
        if (this.trace != TraceBase.None.INSTANCE) {
            this.trace.append("getAndDec():" + andDecrement);
        }
        return andDecrement;
    }

    public final long getAndAdd(long delta) {
        long andAdd = FU.getAndAdd(this, delta);
        if (this.trace != TraceBase.None.INSTANCE) {
            this.trace.append("getAndAdd(" + delta + "):" + andAdd);
        }
        return andAdd;
    }

    public final long addAndGet(long delta) {
        long jAddAndGet = FU.addAndGet(this, delta);
        if (this.trace != TraceBase.None.INSTANCE) {
            this.trace.append("addAndGet(" + delta + "):" + jAddAndGet);
        }
        return jAddAndGet;
    }

    public final long incrementAndGet() {
        long jIncrementAndGet = FU.incrementAndGet(this);
        if (this.trace != TraceBase.None.INSTANCE) {
            this.trace.append("incAndGet():" + jIncrementAndGet);
        }
        return jIncrementAndGet;
    }

    public final long decrementAndGet() {
        long jDecrementAndGet = FU.decrementAndGet(this);
        if (this.trace != TraceBase.None.INSTANCE) {
            this.trace.append("decAndGet():" + jDecrementAndGet);
        }
        return jDecrementAndGet;
    }

    public final void plusAssign(long delta) {
        getAndAdd(delta);
    }

    public final void minusAssign(long delta) {
        getAndAdd(-delta);
    }

    public String toString() {
        return String.valueOf(this.value);
    }

    /* JADX INFO: compiled from: AtomicFU.kt */
    @Metadata(d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0082\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003R4\u0010\u0004\u001a&\u0012\f\u0012\n \u0007*\u0004\u0018\u00010\u00060\u0006 \u0007*\u0012\u0012\f\u0012\n \u0007*\u0004\u0018\u00010\u00060\u0006\u0018\u00010\u00050\u0005X\u0082\u0004¢\u0006\u0004\n\u0002\u0010\b¨\u0006\t"}, d2 = {"Lkotlinx/atomicfu/AtomicLong$Companion;", "", "<init>", "()V", "FU", "Ljava/util/concurrent/atomic/AtomicLongFieldUpdater;", "Lkotlinx/atomicfu/AtomicLong;", "kotlin.jvm.PlatformType", "Ljava/util/concurrent/atomic/AtomicLongFieldUpdater;", "atomicfu"}, k = 1, mv = {2, 0, 0}, xi = 48)
    private static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }
    }
}
