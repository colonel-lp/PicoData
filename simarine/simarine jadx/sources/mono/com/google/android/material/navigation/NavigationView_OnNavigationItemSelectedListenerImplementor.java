package mono.com.google.android.material.navigation;

import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class NavigationView_OnNavigationItemSelectedListenerImplementor implements IGCUserPeer, NavigationView.OnNavigationItemSelectedListener {
    public static final String __md_methods = "n_onNavigationItemSelected:(Landroid/view/MenuItem;)Z:GetOnNavigationItemSelected_Landroid_view_MenuItem_Handler:Google.Android.Material.Navigation.NavigationView/IOnNavigationItemSelectedListenerInvoker, Xamarin.Google.Android.Material\n";
    private ArrayList refList;

    private native boolean n_onNavigationItemSelected(MenuItem menuItem);

    static {
        Runtime.register("Google.Android.Material.Navigation.NavigationView+IOnNavigationItemSelectedListenerImplementor, Xamarin.Google.Android.Material", NavigationView_OnNavigationItemSelectedListenerImplementor.class, __md_methods);
    }

    public NavigationView_OnNavigationItemSelectedListenerImplementor() {
        if (getClass() == NavigationView_OnNavigationItemSelectedListenerImplementor.class) {
            TypeManager.Activate("Google.Android.Material.Navigation.NavigationView+IOnNavigationItemSelectedListenerImplementor, Xamarin.Google.Android.Material", "", this, new Object[0]);
        }
    }

    @Override // com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
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
