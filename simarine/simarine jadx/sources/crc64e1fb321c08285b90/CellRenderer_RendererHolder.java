package crc64e1fb321c08285b90;

import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class CellRenderer_RendererHolder implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Compatibility.CellRenderer+RendererHolder, Microsoft.Maui.Controls", CellRenderer_RendererHolder.class, "");
    }

    public CellRenderer_RendererHolder() {
        if (getClass() == CellRenderer_RendererHolder.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.CellRenderer+RendererHolder, Microsoft.Maui.Controls", "", this, new Object[0]);
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
