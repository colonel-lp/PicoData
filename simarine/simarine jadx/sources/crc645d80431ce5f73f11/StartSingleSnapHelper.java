package crc645d80431ce5f73f11;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class StartSingleSnapHelper extends SingleSnapHelper implements IGCUserPeer {
    public static final String __md_methods = "n_calculateDistanceToFinalSnap:(Landroidx/recyclerview/widget/RecyclerView$LayoutManager;Landroid/view/View;)[I:GetCalculateDistanceToFinalSnap_Landroidx_recyclerview_widget_RecyclerView_LayoutManager_Landroid_view_View_Handler\n";
    private ArrayList refList;

    private native int[] n_calculateDistanceToFinalSnap(RecyclerView.LayoutManager layoutManager, View view);

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Items.StartSingleSnapHelper, Microsoft.Maui.Controls", StartSingleSnapHelper.class, "n_calculateDistanceToFinalSnap:(Landroidx/recyclerview/widget/RecyclerView$LayoutManager;Landroid/view/View;)[I:GetCalculateDistanceToFinalSnap_Landroidx_recyclerview_widget_RecyclerView_LayoutManager_Landroid_view_View_Handler\n");
    }

    public StartSingleSnapHelper() {
        if (getClass() == StartSingleSnapHelper.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.StartSingleSnapHelper, Microsoft.Maui.Controls", "", this, new Object[0]);
        }
    }

    @Override // androidx.recyclerview.widget.PagerSnapHelper, androidx.recyclerview.widget.SnapHelper
    public int[] calculateDistanceToFinalSnap(RecyclerView.LayoutManager layoutManager, View view) {
        return n_calculateDistanceToFinalSnap(layoutManager, view);
    }

    @Override // crc645d80431ce5f73f11.SingleSnapHelper, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc645d80431ce5f73f11.SingleSnapHelper, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
