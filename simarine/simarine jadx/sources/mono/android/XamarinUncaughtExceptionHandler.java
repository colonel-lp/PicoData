package mono.android;

/* JADX INFO: compiled from: Runtime.java */
/* JADX INFO: loaded from: classes2.dex */
final class XamarinUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    Thread.UncaughtExceptionHandler defaultHandler;

    public XamarinUncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.defaultHandler = uncaughtExceptionHandler;
    }

    @Override // java.lang.Thread.UncaughtExceptionHandler
    public final void uncaughtException(Thread thread, Throwable th) {
        Runtime.propagateUncaughtException(thread, th);
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = this.defaultHandler;
        if (uncaughtExceptionHandler != null) {
            uncaughtExceptionHandler.uncaughtException(thread, th);
        }
    }
}
