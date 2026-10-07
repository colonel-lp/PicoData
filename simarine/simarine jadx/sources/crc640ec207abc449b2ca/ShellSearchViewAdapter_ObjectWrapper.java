package crc640ec207abc449b2ca;

import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ShellSearchViewAdapter_ObjectWrapper implements IGCUserPeer {
    public static final String __md_methods = "n_toString:()Ljava/lang/String;:GetToStringHandler\n";
    private ArrayList refList;

    private native String n_toString();

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.Compatibility.ShellSearchViewAdapter+ObjectWrapper, Microsoft.Maui.Controls", ShellSearchViewAdapter_ObjectWrapper.class, __md_methods);
    }

    public ShellSearchViewAdapter_ObjectWrapper() {
        if (getClass() == ShellSearchViewAdapter_ObjectWrapper.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.ShellSearchViewAdapter+ObjectWrapper, Microsoft.Maui.Controls", "", this, new Object[0]);
        }
    }

    public String toString() {
        return n_toString();
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
