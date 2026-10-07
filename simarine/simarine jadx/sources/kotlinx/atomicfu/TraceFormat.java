package kotlinx.atomicfu;

import androidx.core.app.NotificationCompat;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: compiled from: TraceFormat.kt */
/* JADX INFO: loaded from: classes2.dex */
@Metadata(d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0017\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0018\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0001H\u0017¨\u0006\t"}, d2 = {"Lkotlinx/atomicfu/TraceFormat;", "", "<init>", "()V", "format", "", "index", "", NotificationCompat.CATEGORY_EVENT, "atomicfu"}, k = 1, mv = {2, 0, 0}, xi = 48)
public class TraceFormat {
    public String format(int index, Object event) {
        Intrinsics.checkNotNullParameter(event, "event");
        return index + ": " + event;
    }
}
