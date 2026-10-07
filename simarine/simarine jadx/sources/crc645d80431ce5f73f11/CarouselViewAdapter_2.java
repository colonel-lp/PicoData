package crc645d80431ce5f73f11;

import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class CarouselViewAdapter_2 extends ItemsViewAdapter_2 implements IGCUserPeer {
    public static final String __md_methods = "n_getItemCount:()I:GetGetItemCountHandler\nn_getItemViewType:(I)I:GetGetItemViewType_IHandler\nn_onBindViewHolder:(Landroidx/recyclerview/widget/RecyclerView$ViewHolder;I)V:GetOnBindViewHolder_Landroidx_recyclerview_widget_RecyclerView_ViewHolder_IHandler\n";
    private ArrayList refList;

    private native int n_getItemCount();

    private native int n_getItemViewType(int i);

    private native void n_onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i);

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Items.CarouselViewAdapter`2, Microsoft.Maui.Controls", CarouselViewAdapter_2.class, __md_methods);
    }

    public CarouselViewAdapter_2() {
        if (getClass() == CarouselViewAdapter_2.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.CarouselViewAdapter`2, Microsoft.Maui.Controls", "", this, new Object[0]);
        }
    }

    @Override // crc645d80431ce5f73f11.ItemsViewAdapter_2, androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return n_getItemCount();
    }

    @Override // crc645d80431ce5f73f11.ItemsViewAdapter_2, androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        return n_getItemViewType(i);
    }

    @Override // crc645d80431ce5f73f11.ItemsViewAdapter_2, androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        n_onBindViewHolder(viewHolder, i);
    }

    @Override // crc645d80431ce5f73f11.ItemsViewAdapter_2, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc645d80431ce5f73f11.ItemsViewAdapter_2, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
