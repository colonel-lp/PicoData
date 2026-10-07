package crc645d80431ce5f73f11;

import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ReorderableItemsViewAdapter_2 extends GroupableItemsViewAdapter_2 implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Items.ReorderableItemsViewAdapter`2, Microsoft.Maui.Controls", ReorderableItemsViewAdapter_2.class, "");
    }

    public ReorderableItemsViewAdapter_2() {
        if (getClass() == ReorderableItemsViewAdapter_2.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.ReorderableItemsViewAdapter`2, Microsoft.Maui.Controls", "", this, new Object[0]);
        }
    }

    @Override // crc645d80431ce5f73f11.GroupableItemsViewAdapter_2, crc645d80431ce5f73f11.SelectableItemsViewAdapter_2, crc645d80431ce5f73f11.StructuredItemsViewAdapter_2, crc645d80431ce5f73f11.ItemsViewAdapter_2, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc645d80431ce5f73f11.GroupableItemsViewAdapter_2, crc645d80431ce5f73f11.SelectableItemsViewAdapter_2, crc645d80431ce5f73f11.StructuredItemsViewAdapter_2, crc645d80431ce5f73f11.ItemsViewAdapter_2, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
