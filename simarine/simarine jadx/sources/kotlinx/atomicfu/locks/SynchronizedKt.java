package kotlinx.atomicfu.locks;

import androidx.exifinterface.media.ExifInterface;
import java.util.concurrent.locks.ReentrantLock;
import kotlin.Metadata;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.InlineMarker;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: compiled from: Synchronized.kt */
/* JADX INFO: loaded from: classes2.dex */
@Metadata(d1 = {"\u0000$\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\r\u0010\u0002\u001a\u00060\u0003j\u0002`\u0004H\u0087\b\u001a-\u0010\u0006\u001a\u0002H\u0007\"\u0004\b\u0000\u0010\u0007*\u00060\u0003j\u0002`\u00042\f\u0010\b\u001a\b\u0012\u0004\u0012\u0002H\u00070\tH\u0087\bø\u0001\u0000¢\u0006\u0002\u0010\n\u001a1\u0010\u000b\u001a\u0002H\u0007\"\u0004\b\u0000\u0010\u00072\n\u0010\f\u001a\u00060\u0001j\u0002`\r2\f\u0010\b\u001a\b\u0012\u0004\u0012\u0002H\u00070\tH\u0087\bø\u0001\u0000¢\u0006\u0002\u0010\u000e*\n\u0010\u0000\"\u00020\u00012\u00020\u0001*\n\u0010\u0005\"\u00020\u00032\u00020\u0003\u0082\u0002\u0007\n\u0005\b\u009920\u0001¨\u0006\u000f"}, d2 = {"SynchronizedObject", "", "reentrantLock", "Ljava/util/concurrent/locks/ReentrantLock;", "Lkotlinx/atomicfu/locks/ReentrantLock;", "ReentrantLock", "withLock", ExifInterface.GPS_DIRECTION_TRUE, "block", "Lkotlin/Function0;", "(Ljava/util/concurrent/locks/ReentrantLock;Lkotlin/jvm/functions/Function0;)Ljava/lang/Object;", "synchronized", "lock", "Lkotlinx/atomicfu/locks/SynchronizedObject;", "(Ljava/lang/Object;Lkotlin/jvm/functions/Function0;)Ljava/lang/Object;", "atomicfu"}, k = 2, mv = {2, 0, 0}, xi = 48)
public final class SynchronizedKt {
    private static final ReentrantLock reentrantLock() {
        return new ReentrantLock();
    }

    private static final <T> T withLock(ReentrantLock reentrantLock, Function0<? extends T> block) {
        Intrinsics.checkNotNullParameter(reentrantLock, "<this>");
        Intrinsics.checkNotNullParameter(block, "block");
        reentrantLock.lock();
        try {
            return block.invoke();
        } finally {
            InlineMarker.finallyStart(1);
            reentrantLock.unlock();
            InlineMarker.finallyEnd(1);
        }
    }

    /* JADX INFO: renamed from: synchronized, reason: not valid java name */
    private static final <T> T m1780synchronized(Object lock, Function0<? extends T> block) {
        T tInvoke;
        Intrinsics.checkNotNullParameter(lock, "lock");
        Intrinsics.checkNotNullParameter(block, "block");
        synchronized (lock) {
            try {
                tInvoke = block.invoke();
                InlineMarker.finallyStart(1);
            } finally {
                InlineMarker.finallyStart(1);
                InlineMarker.finallyEnd(1);
            }
        }
        return tInvoke;
    }
}
