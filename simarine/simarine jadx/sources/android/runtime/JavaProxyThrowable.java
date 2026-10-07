package android.runtime;

import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes.dex */
public class JavaProxyThrowable extends Error implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Android.Runtime.JavaProxyThrowable, Mono.Android", JavaProxyThrowable.class, "");
    }

    public JavaProxyThrowable() {
        if (getClass() == JavaProxyThrowable.class) {
            TypeManager.Activate("Android.Runtime.JavaProxyThrowable, Mono.Android", "", this, new Object[0]);
        }
    }

    public JavaProxyThrowable(String str, Throwable th, boolean z, boolean z2) {
        super(str, th, z, z2);
        if (getClass() == JavaProxyThrowable.class) {
            TypeManager.Activate("Android.Runtime.JavaProxyThrowable, Mono.Android", "System.String, System.Runtime:Java.Lang.Throwable, Mono.Android:System.Boolean, System.Runtime:System.Boolean, System.Runtime", this, new Object[]{str, th, Boolean.valueOf(z), Boolean.valueOf(z2)});
        }
    }

    public JavaProxyThrowable(String str, Throwable th) {
        super(str, th);
        if (getClass() == JavaProxyThrowable.class) {
            TypeManager.Activate("Android.Runtime.JavaProxyThrowable, Mono.Android", "System.String, System.Runtime:Java.Lang.Throwable, Mono.Android", this, new Object[]{str, th});
        }
    }

    public JavaProxyThrowable(String str) {
        super(str);
        if (getClass() == JavaProxyThrowable.class) {
            TypeManager.Activate("Android.Runtime.JavaProxyThrowable, Mono.Android", "System.String, System.Runtime", this, new Object[]{str});
        }
    }

    public JavaProxyThrowable(Throwable th) {
        super(th);
        if (getClass() == JavaProxyThrowable.class) {
            TypeManager.Activate("Android.Runtime.JavaProxyThrowable, Mono.Android", "Java.Lang.Throwable, Mono.Android", this, new Object[]{th});
        }
    }

    @Override // mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
