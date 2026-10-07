package mono.androidx.core.view;

import androidx.core.view.WindowInsetsControllerCompat;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class WindowInsetsControllerCompat_OnControllableInsetsChangedListenerImplementor implements IGCUserPeer, WindowInsetsControllerCompat.OnControllableInsetsChangedListener {
    public static final String __md_methods = "n_onControllableInsetsChanged:(Landroidx/core/view/WindowInsetsControllerCompat;I)V:GetOnControllableInsetsChanged_Landroidx_core_view_WindowInsetsControllerCompat_IHandler:AndroidX.Core.View.WindowInsetsControllerCompat/IOnControllableInsetsChangedListenerInvoker, Xamarin.AndroidX.Core\n";
    private ArrayList refList;

    private native void n_onControllableInsetsChanged(WindowInsetsControllerCompat windowInsetsControllerCompat, int i);

    static {
        Runtime.register("AndroidX.Core.View.WindowInsetsControllerCompat+IOnControllableInsetsChangedListenerImplementor, Xamarin.AndroidX.Core", WindowInsetsControllerCompat_OnControllableInsetsChangedListenerImplementor.class, __md_methods);
    }

    public WindowInsetsControllerCompat_OnControllableInsetsChangedListenerImplementor() {
        if (getClass() == WindowInsetsControllerCompat_OnControllableInsetsChangedListenerImplementor.class) {
            TypeManager.Activate("AndroidX.Core.View.WindowInsetsControllerCompat+IOnControllableInsetsChangedListenerImplementor, Xamarin.AndroidX.Core", "", this, new Object[0]);
        }
    }

    @Override // androidx.core.view.WindowInsetsControllerCompat.OnControllableInsetsChangedListener
    public void onControllableInsetsChanged(WindowInsetsControllerCompat windowInsetsControllerCompat, int i) {
        n_onControllableInsetsChanged(windowInsetsControllerCompat, i);
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
