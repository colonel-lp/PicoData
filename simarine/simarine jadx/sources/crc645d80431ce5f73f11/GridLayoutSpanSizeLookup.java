package crc645d80431ce5f73f11;

import androidx.recyclerview.widget.GridLayoutManager;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class GridLayoutSpanSizeLookup extends GridLayoutManager.SpanSizeLookup implements IGCUserPeer {
    public static final String __md_methods = "n_getSpanSize:(I)I:GetGetSpanSize_IHandler\n";
    private ArrayList refList;

    private native int n_getSpanSize(int i);

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Items.GridLayoutSpanSizeLookup, Microsoft.Maui.Controls", GridLayoutSpanSizeLookup.class, __md_methods);
    }

    public GridLayoutSpanSizeLookup() {
        if (getClass() == GridLayoutSpanSizeLookup.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.GridLayoutSpanSizeLookup, Microsoft.Maui.Controls", "", this, new Object[0]);
        }
    }

    @Override // androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
    public int getSpanSize(int i) {
        return n_getSpanSize(i);
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
