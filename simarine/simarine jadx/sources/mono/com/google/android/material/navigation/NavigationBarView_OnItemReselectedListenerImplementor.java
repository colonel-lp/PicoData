package mono.com.google.android.material.navigation;

import android.view.MenuItem;
import com.google.android.material.navigation.NavigationBarView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class NavigationBarView_OnItemReselectedListenerImplementor implements IGCUserPeer, NavigationBarView.OnItemReselectedListener {
    public static final String __md_methods = "n_onNavigationItemReselected:(Landroid/view/MenuItem;)V:GetOnNavigationItemReselected_Landroid_view_MenuItem_Handler:Google.Android.Material.Navigation.NavigationBarView/IOnItemReselectedListenerInvoker, Xamarin.Google.Android.Material\n";
    private ArrayList refList;

    private native void n_onNavigationItemReselected(MenuItem menuItem);

    static {
        Runtime.register("Google.Android.Material.Navigation.NavigationBarView+IOnItemReselectedListenerImplementor, Xamarin.Google.Android.Material", NavigationBarView_OnItemReselectedListenerImplementor.class, __md_methods);
    }

    public NavigationBarView_OnItemReselectedListenerImplementor() {
        if (getClass() == NavigationBarView_OnItemReselectedListenerImplementor.class) {
            TypeManager.Activate("Google.Android.Material.Navigation.NavigationBarView+IOnItemReselectedListenerImplementor, Xamarin.Google.Android.Material", "", this, new Object[0]);
        }
    }

    @Override // com.google.android.material.navigation.NavigationBarView.OnItemReselectedListener
    public void onNavigationItemReselected(MenuItem menuItem) {
        n_onNavigationItemReselected(menuItem);
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
