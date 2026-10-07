package net.dot.jni.internal;

import java.util.ArrayList;
import net.dot.jni.GCUserPeerable;
import net.dot.jni.ManagedPeer;

/* JADX INFO: loaded from: classes2.dex */
final class JavaProxyThrowable extends Error implements GCUserPeerable {
    ArrayList<Object> managedReferences;

    static {
        ManagedPeer.registerNativeMembers(JavaProxyThrowable.class, "");
    }

    public JavaProxyThrowable() {
        this.managedReferences = new ArrayList<>();
    }

    public JavaProxyThrowable(String str) {
        super(str);
        this.managedReferences = new ArrayList<>();
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
