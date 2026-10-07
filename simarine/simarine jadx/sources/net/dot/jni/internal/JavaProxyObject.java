package net.dot.jni.internal;

import java.util.ArrayList;
import net.dot.jni.GCUserPeerable;
import net.dot.jni.ManagedPeer;

/* JADX INFO: loaded from: classes2.dex */
final class JavaProxyObject implements GCUserPeerable {
    ArrayList<Object> managedReferences = new ArrayList<>();

    public native boolean equals(Object obj);

    public native int hashCode();

    public native String toString();

    JavaProxyObject() {
    }

    static {
        ManagedPeer.registerNativeMembers(JavaProxyObject.class, "");
    }

    @Override // net.dot.jni.GCUserPeerable
    public void jiAddManagedReference(Object obj) {
        this.managedReferences.add(obj);
    }

    @Override // net.dot.jni.GCUserPeerable
    public void jiClearManagedReferences() {
        this.managedReferences.clear();
    }
}
