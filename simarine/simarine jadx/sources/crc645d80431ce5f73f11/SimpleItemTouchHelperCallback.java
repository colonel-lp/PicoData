package crc645d80431ce5f73f11;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback implements IGCUserPeer {
    public static final String __md_methods = "n_isLongPressDragEnabled:()Z:GetIsLongPressDragEnabledHandler\nn_getMovementFlags:(Landroidx/recyclerview/widget/RecyclerView;Landroidx/recyclerview/widget/RecyclerView$ViewHolder;)I:GetGetMovementFlags_Landroidx_recyclerview_widget_RecyclerView_Landroidx_recyclerview_widget_RecyclerView_ViewHolder_Handler\nn_onMove:(Landroidx/recyclerview/widget/RecyclerView;Landroidx/recyclerview/widget/RecyclerView$ViewHolder;Landroidx/recyclerview/widget/RecyclerView$ViewHolder;)Z:GetOnMove_Landroidx_recyclerview_widget_RecyclerView_Landroidx_recyclerview_widget_RecyclerView_ViewHolder_Landroidx_recyclerview_widget_RecyclerView_ViewHolder_Handler\nn_onSwiped:(Landroidx/recyclerview/widget/RecyclerView$ViewHolder;I)V:GetOnSwiped_Landroidx_recyclerview_widget_RecyclerView_ViewHolder_IHandler\n";
    private ArrayList refList;

    private native int n_getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder);

    private native boolean n_isLongPressDragEnabled();

    private native boolean n_onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2);

    private native void n_onSwiped(RecyclerView.ViewHolder viewHolder, int i);

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Items.SimpleItemTouchHelperCallback, Microsoft.Maui.Controls", SimpleItemTouchHelperCallback.class, __md_methods);
    }

    public SimpleItemTouchHelperCallback() {
        if (getClass() == SimpleItemTouchHelperCallback.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.SimpleItemTouchHelperCallback, Microsoft.Maui.Controls", "", this, new Object[0]);
        }
    }

    @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
    public boolean isLongPressDragEnabled() {
        return n_isLongPressDragEnabled();
    }

    @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return n_getMovementFlags(recyclerView, viewHolder);
    }

    @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
        return n_onMove(recyclerView, viewHolder, viewHolder2);
    }

    @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
        n_onSwiped(viewHolder, i);
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
