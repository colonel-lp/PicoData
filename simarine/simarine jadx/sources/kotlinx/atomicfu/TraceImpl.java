package kotlinx.atomicfu;

import androidx.core.app.NotificationCompat;
import java.util.concurrent.atomic.AtomicInteger;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: compiled from: Trace.kt */
/* JADX INFO: loaded from: classes2.dex */
@Metadata(d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0011\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0000\b\u0002\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u0010\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u000bH\u0016J\u0018\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0012\u001a\u00020\u000b2\u0006\u0010\u0013\u001a\u00020\u000bH\u0016J \u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0012\u001a\u00020\u000b2\u0006\u0010\u0013\u001a\u00020\u000b2\u0006\u0010\u0014\u001a\u00020\u000bH\u0016J(\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0012\u001a\u00020\u000b2\u0006\u0010\u0013\u001a\u00020\u000b2\u0006\u0010\u0014\u001a\u00020\u000b2\u0006\u0010\u0015\u001a\u00020\u000bH\u0016J\b\u0010\u0016\u001a\u00020\u0017H\u0016R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u0018\u0010\t\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\nX\u0082\u0004¢\u0006\u0004\n\u0002\u0010\fR\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0018"}, d2 = {"Lkotlinx/atomicfu/TraceImpl;", "Lkotlinx/atomicfu/TraceBase;", "size", "", "format", "Lkotlinx/atomicfu/TraceFormat;", "<init>", "(ILkotlinx/atomicfu/TraceFormat;)V", "mask", "trace", "", "", "[Ljava/lang/Object;", "index", "Ljava/util/concurrent/atomic/AtomicInteger;", "append", "", NotificationCompat.CATEGORY_EVENT, "event1", "event2", "event3", "event4", "toString", "", "atomicfu"}, k = 1, mv = {2, 0, 0}, xi = 48)
final class TraceImpl extends TraceBase {
    private final TraceFormat format;
    private final AtomicInteger index;
    private final int mask;
    private final int size;
    private final Object[] trace;

    public TraceImpl(int i, TraceFormat format) {
        Intrinsics.checkNotNullParameter(format, "format");
        this.format = format;
        if (i < 1) {
            throw new IllegalArgumentException("Failed requirement.".toString());
        }
        int iHighestOneBit = Integer.highestOneBit((i << 1) - 1);
        this.size = iHighestOneBit;
        this.mask = iHighestOneBit - 1;
        this.trace = new Object[iHighestOneBit];
        this.index = new AtomicInteger(0);
    }

    @Override // kotlinx.atomicfu.TraceBase
    public void append(Object event) {
        Intrinsics.checkNotNullParameter(event, "event");
        this.trace[this.mask & this.index.getAndIncrement()] = event;
    }

    @Override // kotlinx.atomicfu.TraceBase
    public void append(Object event1, Object event2) {
        Intrinsics.checkNotNullParameter(event1, "event1");
        Intrinsics.checkNotNullParameter(event2, "event2");
        int andAdd = this.index.getAndAdd(2);
        Object[] objArr = this.trace;
        int i = this.mask;
        objArr[andAdd & i] = event1;
        objArr[i & (andAdd + 1)] = event2;
    }

    @Override // kotlinx.atomicfu.TraceBase
    public void append(Object event1, Object event2, Object event3) {
        Intrinsics.checkNotNullParameter(event1, "event1");
        Intrinsics.checkNotNullParameter(event2, "event2");
        Intrinsics.checkNotNullParameter(event3, "event3");
        int andAdd = this.index.getAndAdd(3);
        Object[] objArr = this.trace;
        int i = this.mask;
        objArr[andAdd & i] = event1;
        objArr[(andAdd + 1) & i] = event2;
        objArr[i & (andAdd + 2)] = event3;
    }

    @Override // kotlinx.atomicfu.TraceBase
    public void append(Object event1, Object event2, Object event3, Object event4) {
        Intrinsics.checkNotNullParameter(event1, "event1");
        Intrinsics.checkNotNullParameter(event2, "event2");
        Intrinsics.checkNotNullParameter(event3, "event3");
        Intrinsics.checkNotNullParameter(event4, "event4");
        int andAdd = this.index.getAndAdd(4);
        Object[] objArr = this.trace;
        int i = this.mask;
        objArr[andAdd & i] = event1;
        objArr[(andAdd + 1) & i] = event2;
        objArr[(andAdd + 2) & i] = event3;
        objArr[i & (andAdd + 3)] = event4;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = this.index.get();
        int i2 = this.mask & i;
        int i3 = this.size;
        int i4 = 0;
        int i5 = i > i3 ? i - i3 : 0;
        int i6 = i2;
        do {
            Object obj = this.trace[i6];
            if (obj != null) {
                int i7 = i4 + 1;
                if (i4 > 0) {
                    sb.append('\n');
                }
                sb.append(this.format.format(i5, obj));
                i5++;
                i4 = i7;
            }
            i6 = (i6 + 1) & this.mask;
        } while (i6 != i2);
        String string = sb.toString();
        Intrinsics.checkNotNullExpressionValue(string, "toString(...)");
        return string;
    }
}
