package kotlinx.atomicfu;

import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: compiled from: Trace.kt */
/* JADX INFO: loaded from: classes2.dex */
@Metadata(d1 = {"\u0000\u001e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\u001a\u001c\u0010\u0000\u001a\u00020\u00012\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u001a\u0012\u0010\u0006\u001a\u00020\u0001*\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b\u001a\u0012\u0010\t\u001a\u0004\u0018\u00010\b2\u0006\u0010\n\u001a\u00020\bH\u0002\"\u0011\u0010\u000b\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r¨\u0006\u000e"}, d2 = {"Trace", "Lkotlinx/atomicfu/TraceBase;", "size", "", "format", "Lkotlinx/atomicfu/TraceFormat;", "named", "name", "", "getSystemProperty", "key", "traceFormatDefault", "getTraceFormatDefault", "()Lkotlinx/atomicfu/TraceFormat;", "atomicfu"}, k = 2, mv = {2, 0, 0}, xi = 48)
public final class TraceKt {
    private static final TraceFormat traceFormatDefault;

    public static final TraceBase Trace(int i, TraceFormat format) {
        Intrinsics.checkNotNullParameter(format, "format");
        return new TraceImpl(i, format);
    }

    public static final TraceBase named(TraceBase traceBase, String name) {
        Intrinsics.checkNotNullParameter(traceBase, "<this>");
        Intrinsics.checkNotNullParameter(name, "name");
        return traceBase == TraceBase.None.INSTANCE ? traceBase : new NamedTrace(traceBase, name);
    }

    private static final String getSystemProperty(String str) {
        try {
            return System.getProperty(str);
        } catch (SecurityException unused) {
            return null;
        }
    }

    public static final TraceFormat getTraceFormatDefault() {
        return traceFormatDefault;
    }

    static {
        traceFormatDefault = getSystemProperty("kotlinx.atomicfu.trace.thread") != null ? new TraceFormatThread() : new TraceFormat();
    }

    public static /* synthetic */ TraceBase Trace$default(int i, TraceFormat traceFormat, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            i = 32;
        }
        if ((i2 & 2) != 0) {
            traceFormat = traceFormatDefault;
        }
        return Trace(i, traceFormat);
    }
}
