package mono.com.google.android.material.appbar;

import com.google.android.material.appbar.AppBarLayout;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class AppBarLayout_LiftOnScrollListenerImplementor implements IGCUserPeer, AppBarLayout.LiftOnScrollListener {
    public static final String __md_methods = "n_onUpdate:(FI)V:GetOnUpdate_FIHandler:Google.Android.Material.AppBar.AppBarLayout/ILiftOnScrollListenerInvoker, Xamarin.Google.Android.Material\n";
    private ArrayList refList;

    private native void n_onUpdate(float f, int i);

    static {
        Runtime.register("Google.Android.Material.AppBar.AppBarLayout+ILiftOnScrollListenerImplementor, Xamarin.Google.Android.Material", AppBarLayout_LiftOnScrollListenerImplementor.class, __md_methods);
    }

    public AppBarLayout_LiftOnScrollListenerImplementor() {
        if (getClass() == AppBarLayout_LiftOnScrollListenerImplementor.class) {
            TypeManager.Activate("Google.Android.Material.AppBar.AppBarLayout+ILiftOnScrollListenerImplementor, Xamarin.Google.Android.Material", "", this, new Object[0]);
        }
    }

    @Override // com.google.android.material.appbar.AppBarLayout.LiftOnScrollListener
    public void onUpdate(float f, int i) {
        n_onUpdate(f, i);
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
