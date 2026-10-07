package com.google.crypto.tink.internal;

/* JADX INFO: loaded from: classes2.dex */
public final class TinkBugException extends RuntimeException {

    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    public TinkBugException(String message) {
        super(message);
    }

    public TinkBugException(String message, Throwable cause) {
        super(message, cause);
    }

    public TinkBugException(Throwable cause) {
        super(cause);
    }

    public static <T> T exceptionIsBug(ThrowingSupplier<T> t) {
        try {
            return t.get();
        } catch (Exception e) {
            throw new TinkBugException(e);
        }
    }

    public static void exceptionIsBug(ThrowingRunnable v) {
        try {
            v.run();
        } catch (Exception e) {
            throw new TinkBugException(e);
        }
    }
}
