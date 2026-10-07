package mono.androidx.navigation;

import android.os.Bundle;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class NavController_OnDestinationChangedListenerImplementor implements IGCUserPeer, NavController.OnDestinationChangedListener {
    public static final String __md_methods = "n_onDestinationChanged:(Landroidx/navigation/NavController;Landroidx/navigation/NavDestination;Landroid/os/Bundle;)V:GetOnDestinationChanged_Landroidx_navigation_NavController_Landroidx_navigation_NavDestination_Landroid_os_Bundle_Handler:AndroidX.Navigation.NavController/IOnDestinationChangedListenerInvoker, Xamarin.AndroidX.Navigation.Runtime\n";
    private ArrayList refList;

    private native void n_onDestinationChanged(NavController navController, NavDestination navDestination, Bundle bundle);

    static {
        Runtime.register("AndroidX.Navigation.NavController+IOnDestinationChangedListenerImplementor, Xamarin.AndroidX.Navigation.Runtime", NavController_OnDestinationChangedListenerImplementor.class, __md_methods);
    }

    public NavController_OnDestinationChangedListenerImplementor() {
        if (getClass() == NavController_OnDestinationChangedListenerImplementor.class) {
            TypeManager.Activate("AndroidX.Navigation.NavController+IOnDestinationChangedListenerImplementor, Xamarin.AndroidX.Navigation.Runtime", "", this, new Object[0]);
        }
    }

    @Override // androidx.navigation.NavController.OnDestinationChangedListener
    public void onDestinationChanged(NavController navController, NavDestination navDestination, Bundle bundle) {
        n_onDestinationChanged(navController, navDestination, bundle);
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
