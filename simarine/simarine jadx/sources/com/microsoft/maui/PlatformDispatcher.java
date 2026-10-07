package com.microsoft.maui;

import android.os.Handler;
import android.os.Looper;

/* JADX INFO: loaded from: classes2.dex */
public class PlatformDispatcher extends Handler {
    private PlatformDispatcher(Looper looper) {
        super(looper);
    }

    public static PlatformDispatcher create() {
        Looper looperMyLooper = Looper.myLooper();
        if (looperMyLooper == null || looperMyLooper != Looper.getMainLooper()) {
            return null;
        }
        return new PlatformDispatcher(looperMyLooper);
    }

    public boolean isDispatchRequired() {
        return Looper.myLooper() != getLooper();
    }
}
