package crc645d80431ce5f73f11;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class CenterSnapHelper extends NongreedySnapHelper implements IGCUserPeer {
    public static final String __md_methods = "n_findSnapView:(Landroidx/recyclerview/widget/RecyclerView$LayoutManager;)Landroid/view/View;:GetFindSnapView_Landroidx_recyclerview_widget_RecyclerView_LayoutManager_Handler\n";
    private ArrayList refList;

    private native View n_findSnapView(RecyclerView.LayoutManager layoutManager);

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Items.CenterSnapHelper, Microsoft.Maui.Controls", CenterSnapHelper.class, __md_methods);
    }

    public CenterSnapHelper() {
        if (getClass() == CenterSnapHelper.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.CenterSnapHelper, Microsoft.Maui.Controls", "", this, new Object[0]);
        }
    }

    @Override // androidx.recyclerview.widget.LinearSnapHelper, androidx.recyclerview.widget.SnapHelper
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        return n_findSnapView(layoutManager);
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
