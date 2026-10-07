package mono.androidx.appcompat.widget;

import androidx.appcompat.widget.SearchView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class SearchView_OnCloseListenerImplementor implements IGCUserPeer, SearchView.OnCloseListener {
    public static final String __md_methods = "n_onClose:()Z:GetOnCloseHandler:AndroidX.AppCompat.Widget.SearchView/IOnCloseListenerInvoker, Xamarin.AndroidX.AppCompat\n";
    private ArrayList refList;

    private native boolean n_onClose();

    static {
        Runtime.register("AndroidX.AppCompat.Widget.SearchView+IOnCloseListenerImplementor, Xamarin.AndroidX.AppCompat", SearchView_OnCloseListenerImplementor.class, __md_methods);
    }

    public SearchView_OnCloseListenerImplementor() {
        if (getClass() == SearchView_OnCloseListenerImplementor.class) {
            TypeManager.Activate("AndroidX.AppCompat.Widget.SearchView+IOnCloseListenerImplementor, Xamarin.AndroidX.AppCompat", "", this, new Object[0]);
        }
    }

    @Override // androidx.appcompat.widget.SearchView.OnCloseListener
    public boolean onClose() {
        return n_onClose();
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
