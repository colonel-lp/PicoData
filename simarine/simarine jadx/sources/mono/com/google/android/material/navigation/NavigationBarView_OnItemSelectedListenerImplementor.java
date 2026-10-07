package mono.com.google.android.material.navigation;

import android.view.MenuItem;
import com.google.android.material.navigation.NavigationBarView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class NavigationBarView_OnItemSelectedListenerImplementor implements IGCUserPeer, NavigationBarView.OnItemSelectedListener {
    public static final String __md_methods = "n_onNavigationItemSelected:(Landroid/view/MenuItem;)Z:GetOnNavigationItemSelected_Landroid_view_MenuItem_Handler:Google.Android.Material.Navigation.NavigationBarView/IOnItemSelectedListenerInvoker, Xamarin.Google.Android.Material\n";
    private ArrayList refList;

    private native boolean n_onNavigationItemSelected(MenuItem menuItem);

    static {
        Runtime.register("Google.Android.Material.Navigation.NavigationBarView+IOnItemSelectedListenerImplementor, Xamarin.Google.Android.Material", NavigationBarView_OnItemSelectedListenerImplementor.class, __md_methods);
    }

    public NavigationBarView_OnItemSelectedListenerImplementor() {
        if (getClass() == NavigationBarView_OnItemSelectedListenerImplementor.class) {
            TypeManager.Activate("Google.Android.Material.Navigation.NavigationBarView+IOnItemSelectedListenerImplementor, Xamarin.Google.Android.Material", "", this, new Object[0]);
        }
    }

    @Override // com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        return n_onNavigationItemSelected(menuItem);
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
