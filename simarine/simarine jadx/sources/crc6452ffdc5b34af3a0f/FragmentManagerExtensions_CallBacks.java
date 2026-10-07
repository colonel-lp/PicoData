package crc6452ffdc5b34af3a0f;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class FragmentManagerExtensions_CallBacks extends FragmentManager.FragmentLifecycleCallbacks implements IGCUserPeer {
    public static final String __md_methods = "n_onFragmentDestroyed:(Landroidx/fragment/app/FragmentManager;Landroidx/fragment/app/Fragment;)V:GetOnFragmentDestroyed_Landroidx_fragment_app_FragmentManager_Landroidx_fragment_app_Fragment_Handler\nn_onFragmentResumed:(Landroidx/fragment/app/FragmentManager;Landroidx/fragment/app/Fragment;)V:GetOnFragmentResumed_Landroidx_fragment_app_FragmentManager_Landroidx_fragment_app_Fragment_Handler\n";
    private ArrayList refList;

    private native void n_onFragmentDestroyed(FragmentManager fragmentManager, Fragment fragment);

    private native void n_onFragmentResumed(FragmentManager fragmentManager, Fragment fragment);

    static {
        Runtime.register("Microsoft.Maui.Platform.FragmentManagerExtensions+CallBacks, Microsoft.Maui", FragmentManagerExtensions_CallBacks.class, __md_methods);
    }

    public FragmentManagerExtensions_CallBacks() {
        if (getClass() == FragmentManagerExtensions_CallBacks.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.FragmentManagerExtensions+CallBacks, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    @Override // androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
    public void onFragmentDestroyed(FragmentManager fragmentManager, Fragment fragment) {
        n_onFragmentDestroyed(fragmentManager, fragment);
    }

    @Override // androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
    public void onFragmentResumed(FragmentManager fragmentManager, Fragment fragment) {
        n_onFragmentResumed(fragmentManager, fragment);
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
