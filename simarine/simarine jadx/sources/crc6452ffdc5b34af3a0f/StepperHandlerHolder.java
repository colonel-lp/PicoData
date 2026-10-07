package crc6452ffdc5b34af3a0f;

import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class StepperHandlerHolder implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Platform.StepperHandlerHolder, Microsoft.Maui", StepperHandlerHolder.class, "");
    }

    public StepperHandlerHolder() {
        if (getClass() == StepperHandlerHolder.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.StepperHandlerHolder, Microsoft.Maui", "", this, new Object[0]);
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
