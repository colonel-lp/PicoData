package crc645d80431ce5f73f11;

import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public abstract class EdgeSnapHelper extends NongreedySnapHelper implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Items.EdgeSnapHelper, Microsoft.Maui.Controls", EdgeSnapHelper.class, "");
    }

    public EdgeSnapHelper() {
        if (getClass() == EdgeSnapHelper.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.EdgeSnapHelper, Microsoft.Maui.Controls", "", this, new Object[0]);
        }
    }

    @Override // crc645d80431ce5f73f11.NongreedySnapHelper, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc645d80431ce5f73f11.NongreedySnapHelper, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
