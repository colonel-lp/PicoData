package crc64e1fb321c08285b90;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ListViewRenderer_ListViewSwipeRefreshLayoutListener implements IGCUserPeer, SwipeRefreshLayout.OnRefreshListener {
    public static final String __md_methods = "n_onRefresh:()V:GetOnRefreshHandler:AndroidX.SwipeRefreshLayout.Widget.SwipeRefreshLayout/IOnRefreshListenerInvoker, Xamarin.AndroidX.SwipeRefreshLayout\n";
    private ArrayList refList;

    private native void n_onRefresh();

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Compatibility.ListViewRenderer+ListViewSwipeRefreshLayoutListener, Microsoft.Maui.Controls", ListViewRenderer_ListViewSwipeRefreshLayoutListener.class, "n_onRefresh:()V:GetOnRefreshHandler:AndroidX.SwipeRefreshLayout.Widget.SwipeRefreshLayout/IOnRefreshListenerInvoker, Xamarin.AndroidX.SwipeRefreshLayout\n");
    }

    public ListViewRenderer_ListViewSwipeRefreshLayoutListener() {
        if (getClass() == ListViewRenderer_ListViewSwipeRefreshLayoutListener.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.ListViewRenderer+ListViewSwipeRefreshLayoutListener, Microsoft.Maui.Controls", "", this, new Object[0]);
        }
    }

    public ListViewRenderer_ListViewSwipeRefreshLayoutListener(ListViewRenderer listViewRenderer) {
        if (getClass() == ListViewRenderer_ListViewSwipeRefreshLayoutListener.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.ListViewRenderer+ListViewSwipeRefreshLayoutListener, Microsoft.Maui.Controls", "Microsoft.Maui.Controls.Handlers.Compatibility.ListViewRenderer, Microsoft.Maui.Controls", this, new Object[]{listViewRenderer});
        }
    }

    @Override // androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
    public void onRefresh() {
        n_onRefresh();
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
