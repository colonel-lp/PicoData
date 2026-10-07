package crc6452ffdc5b34af3a0f;

import android.content.Context;
import android.util.AttributeSet;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiSwipeRefreshLayout extends SwipeRefreshLayout implements IGCUserPeer {
    public static final String __md_methods = "n_canChildScrollUp:()Z:GetCanChildScrollUpHandler\n";
    private ArrayList refList;

    private native boolean n_canChildScrollUp();

    static {
        Runtime.register("Microsoft.Maui.Platform.MauiSwipeRefreshLayout, Microsoft.Maui", MauiSwipeRefreshLayout.class, __md_methods);
    }

    public MauiSwipeRefreshLayout(Context context) {
        super(context);
        if (getClass() == MauiSwipeRefreshLayout.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiSwipeRefreshLayout, Microsoft.Maui", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public MauiSwipeRefreshLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == MauiSwipeRefreshLayout.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiSwipeRefreshLayout, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    @Override // androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    public boolean canChildScrollUp() {
        return n_canChildScrollUp();
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
