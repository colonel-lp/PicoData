package kotlinx.atomicfu;

import androidx.core.app.NotificationCompat;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: compiled from: Trace.kt */
/* JADX INFO: loaded from: classes2.dex */
@Metadata(d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0000\n\u0000\b\u0002\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0018\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0016¨\u0006\n"}, d2 = {"Lkotlinx/atomicfu/TraceFormatThread;", "Lkotlinx/atomicfu/TraceFormat;", "<init>", "()V", "format", "", "index", "", NotificationCompat.CATEGORY_EVENT, "", "atomicfu"}, k = 1, mv = {2, 0, 0}, xi = 48)
final class TraceFormatThread extends TraceFormat {
    @Override // kotlinx.atomicfu.TraceFormat
    public String format(int index, Object event) {
        Intrinsics.checkNotNullParameter(event, "event");
        return index + ": [" + Thread.currentThread().getName() + "] " + event;
    }
}
