package kotlinx.atomicfu;

import androidx.core.app.NotificationCompat;
import kotlin.Metadata;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: compiled from: Trace.common.kt */
/* JADX INFO: loaded from: classes2.dex */
@Metadata(d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0017\u0018\u00002\u00020\u0001:\u0001\rB\t\b\u0000¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0001H\u0017J\u0018\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\u0001H\u0017J \u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\u0001H\u0017J(\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u0001H\u0017J\u001a\u0010\u000b\u001a\u00020\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\fH\u0087\nø\u0001\u0000\u0082\u0002\u0007\n\u0005\b\u009920\u0001¨\u0006\u000e"}, d2 = {"Lkotlinx/atomicfu/TraceBase;", "", "<init>", "()V", "append", "", NotificationCompat.CATEGORY_EVENT, "event1", "event2", "event3", "event4", "invoke", "Lkotlin/Function0;", "None", "atomicfu"}, k = 1, mv = {2, 0, 0}, xi = 48)
public class TraceBase {
    public void append(Object event) {
        Intrinsics.checkNotNullParameter(event, "event");
    }

    public void append(Object event1, Object event2) {
        Intrinsics.checkNotNullParameter(event1, "event1");
        Intrinsics.checkNotNullParameter(event2, "event2");
    }

    public void append(Object event1, Object event2, Object event3) {
        Intrinsics.checkNotNullParameter(event1, "event1");
        Intrinsics.checkNotNullParameter(event2, "event2");
        Intrinsics.checkNotNullParameter(event3, "event3");
    }

    public void append(Object event1, Object event2, Object event3, Object event4) {
        Intrinsics.checkNotNullParameter(event1, "event1");
        Intrinsics.checkNotNullParameter(event2, "event2");
        Intrinsics.checkNotNullParameter(event3, "event3");
        Intrinsics.checkNotNullParameter(event4, "event4");
    }

    private final void invoke(Function0<? extends Object> event) {
        Intrinsics.checkNotNullParameter(event, "event");
        append(event.invoke());
    }

    /* JADX INFO: compiled from: Trace.common.kt */
    @Metadata(d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003¨\u0006\u0004"}, d2 = {"Lkotlinx/atomicfu/TraceBase$None;", "Lkotlinx/atomicfu/TraceBase;", "<init>", "()V", "atomicfu"}, k = 1, mv = {2, 0, 0}, xi = 48)
    public static final class None extends TraceBase {
        public static final None INSTANCE = new None();

        private None() {
        }
    }
}
