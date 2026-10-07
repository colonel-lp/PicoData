package crc645d80431ce5f73f11;

import android.content.Context;
import android.util.AttributeSet;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiRecyclerView_3 extends RecyclerView implements IGCUserPeer {
    public static final String __md_methods = "n_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n";
    private ArrayList refList;

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Items.MauiRecyclerView`3, Microsoft.Maui.Controls", MauiRecyclerView_3.class, "n_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n");
    }

    public MauiRecyclerView_3(Context context) {
        super(context);
        if (getClass() == MauiRecyclerView_3.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.MauiRecyclerView`3, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public MauiRecyclerView_3(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == MauiRecyclerView_3.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.MauiRecyclerView`3, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public MauiRecyclerView_3(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == MauiRecyclerView_3.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.MauiRecyclerView`3, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        n_onLayout(z, i, i2, i3, i4);
    }

    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
