package crc6452ffdc5b34af3a0f;

import android.os.Bundle;
import android.view.View;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class NavigationRootManager_ElementBasedFragment extends ScopedFragment implements IGCUserPeer {
    public static final String __md_methods = "n_onViewCreated:(Landroid/view/View;Landroid/os/Bundle;)V:GetOnViewCreated_Landroid_view_View_Landroid_os_Bundle_Handler\n";
    private ArrayList refList;

    private native void n_onViewCreated(View view, Bundle bundle);

    static {
        Runtime.register("Microsoft.Maui.Platform.NavigationRootManager+ElementBasedFragment, Microsoft.Maui", NavigationRootManager_ElementBasedFragment.class, __md_methods);
    }

    public NavigationRootManager_ElementBasedFragment() {
        if (getClass() == NavigationRootManager_ElementBasedFragment.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.NavigationRootManager+ElementBasedFragment, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    public NavigationRootManager_ElementBasedFragment(int i) {
        super(i);
        if (getClass() == NavigationRootManager_ElementBasedFragment.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.NavigationRootManager+ElementBasedFragment, Microsoft.Maui", "System.Int32, System.Private.CoreLib", this, new Object[]{Integer.valueOf(i)});
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        n_onViewCreated(view, bundle);
    }

    @Override // crc6452ffdc5b34af3a0f.ScopedFragment, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc6452ffdc5b34af3a0f.ScopedFragment, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
