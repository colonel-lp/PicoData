package crc645d80431ce5f73f11;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class SimpleViewHolder extends RecyclerView.ViewHolder implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Items.SimpleViewHolder, Microsoft.Maui.Controls", SimpleViewHolder.class, "");
    }

    public SimpleViewHolder(View view) {
        super(view);
        if (getClass() == SimpleViewHolder.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.SimpleViewHolder, Microsoft.Maui.Controls", "Android.Views.View, Mono.Android", this, new Object[]{view});
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
