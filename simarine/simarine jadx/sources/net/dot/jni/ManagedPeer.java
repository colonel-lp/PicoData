package net.dot.jni;

/* JADX INFO: loaded from: classes2.dex */
public final class ManagedPeer {
    public static native void construct(Object obj, String str, Object... objArr);

    public static native void registerNativeMembers(Class<?> cls, String str);

    private ManagedPeer() {
    }
}
