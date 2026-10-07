package crc6436e425876cb621d9;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class FragmentLifecycleManager extends FragmentManager.FragmentLifecycleCallbacks implements IGCUserPeer {
    public static final String __md_methods = "n_onFragmentAttached:(Landroidx/fragment/app/FragmentManager;Landroidx/fragment/app/Fragment;Landroid/content/Context;)V:GetOnFragmentAttached_Landroidx_fragment_app_FragmentManager_Landroidx_fragment_app_Fragment_Landroid_content_Context_Handler\nn_onFragmentCreated:(Landroidx/fragment/app/FragmentManager;Landroidx/fragment/app/Fragment;Landroid/os/Bundle;)V:GetOnFragmentCreated_Landroidx_fragment_app_FragmentManager_Landroidx_fragment_app_Fragment_Landroid_os_Bundle_Handler\nn_onFragmentDestroyed:(Landroidx/fragment/app/FragmentManager;Landroidx/fragment/app/Fragment;)V:GetOnFragmentDestroyed_Landroidx_fragment_app_FragmentManager_Landroidx_fragment_app_Fragment_Handler\nn_onFragmentDetached:(Landroidx/fragment/app/FragmentManager;Landroidx/fragment/app/Fragment;)V:GetOnFragmentDetached_Landroidx_fragment_app_FragmentManager_Landroidx_fragment_app_Fragment_Handler\nn_onFragmentPaused:(Landroidx/fragment/app/FragmentManager;Landroidx/fragment/app/Fragment;)V:GetOnFragmentPaused_Landroidx_fragment_app_FragmentManager_Landroidx_fragment_app_Fragment_Handler\nn_onFragmentPreAttached:(Landroidx/fragment/app/FragmentManager;Landroidx/fragment/app/Fragment;Landroid/content/Context;)V:GetOnFragmentPreAttached_Landroidx_fragment_app_FragmentManager_Landroidx_fragment_app_Fragment_Landroid_content_Context_Handler\nn_onFragmentPreCreated:(Landroidx/fragment/app/FragmentManager;Landroidx/fragment/app/Fragment;Landroid/os/Bundle;)V:GetOnFragmentPreCreated_Landroidx_fragment_app_FragmentManager_Landroidx_fragment_app_Fragment_Landroid_os_Bundle_Handler\nn_onFragmentResumed:(Landroidx/fragment/app/FragmentManager;Landroidx/fragment/app/Fragment;)V:GetOnFragmentResumed_Landroidx_fragment_app_FragmentManager_Landroidx_fragment_app_Fragment_Handler\nn_onFragmentSaveInstanceState:(Landroidx/fragment/app/FragmentManager;Landroidx/fragment/app/Fragment;Landroid/os/Bundle;)V:GetOnFragmentSaveInstanceState_Landroidx_fragment_app_FragmentManager_Landroidx_fragment_app_Fragment_Landroid_os_Bundle_Handler\nn_onFragmentStarted:(Landroidx/fragment/app/FragmentManager;Landroidx/fragment/app/Fragment;)V:GetOnFragmentStarted_Landroidx_fragment_app_FragmentManager_Landroidx_fragment_app_Fragment_Handler\nn_onFragmentStopped:(Landroidx/fragment/app/FragmentManager;Landroidx/fragment/app/Fragment;)V:GetOnFragmentStopped_Landroidx_fragment_app_FragmentManager_Landroidx_fragment_app_Fragment_Handler\nn_onFragmentViewCreated:(Landroidx/fragment/app/FragmentManager;Landroidx/fragment/app/Fragment;Landroid/view/View;Landroid/os/Bundle;)V:GetOnFragmentViewCreated_Landroidx_fragment_app_FragmentManager_Landroidx_fragment_app_Fragment_Landroid_view_View_Landroid_os_Bundle_Handler\nn_onFragmentViewDestroyed:(Landroidx/fragment/app/FragmentManager;Landroidx/fragment/app/Fragment;)V:GetOnFragmentViewDestroyed_Landroidx_fragment_app_FragmentManager_Landroidx_fragment_app_Fragment_Handler\n";
    private ArrayList refList;

    private native void n_onFragmentAttached(FragmentManager fragmentManager, Fragment fragment, Context context);

    private native void n_onFragmentCreated(FragmentManager fragmentManager, Fragment fragment, Bundle bundle);

    private native void n_onFragmentDestroyed(FragmentManager fragmentManager, Fragment fragment);

    private native void n_onFragmentDetached(FragmentManager fragmentManager, Fragment fragment);

    private native void n_onFragmentPaused(FragmentManager fragmentManager, Fragment fragment);

    private native void n_onFragmentPreAttached(FragmentManager fragmentManager, Fragment fragment, Context context);

    private native void n_onFragmentPreCreated(FragmentManager fragmentManager, Fragment fragment, Bundle bundle);

    private native void n_onFragmentResumed(FragmentManager fragmentManager, Fragment fragment);

    private native void n_onFragmentSaveInstanceState(FragmentManager fragmentManager, Fragment fragment, Bundle bundle);

    private native void n_onFragmentStarted(FragmentManager fragmentManager, Fragment fragment);

    private native void n_onFragmentStopped(FragmentManager fragmentManager, Fragment fragment);

    private native void n_onFragmentViewCreated(FragmentManager fragmentManager, Fragment fragment, View view, Bundle bundle);

    private native void n_onFragmentViewDestroyed(FragmentManager fragmentManager, Fragment fragment);

    static {
        Runtime.register("CommunityToolkit.Maui.Core.Services.FragmentLifecycleManager, CommunityToolkit.Maui.Core", FragmentLifecycleManager.class, __md_methods);
    }

    public FragmentLifecycleManager() {
        if (getClass() == FragmentLifecycleManager.class) {
            TypeManager.Activate("CommunityToolkit.Maui.Core.Services.FragmentLifecycleManager, CommunityToolkit.Maui.Core", "", this, new Object[0]);
        }
    }

    @Override // androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
    public void onFragmentAttached(FragmentManager fragmentManager, Fragment fragment, Context context) {
        n_onFragmentAttached(fragmentManager, fragment, context);
    }

    @Override // androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
    public void onFragmentCreated(FragmentManager fragmentManager, Fragment fragment, Bundle bundle) {
        n_onFragmentCreated(fragmentManager, fragment, bundle);
    }

    @Override // androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
    public void onFragmentDestroyed(FragmentManager fragmentManager, Fragment fragment) {
        n_onFragmentDestroyed(fragmentManager, fragment);
    }

    @Override // androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
    public void onFragmentDetached(FragmentManager fragmentManager, Fragment fragment) {
        n_onFragmentDetached(fragmentManager, fragment);
    }

    @Override // androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
    public void onFragmentPaused(FragmentManager fragmentManager, Fragment fragment) {
        n_onFragmentPaused(fragmentManager, fragment);
    }

    @Override // androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
    public void onFragmentPreAttached(FragmentManager fragmentManager, Fragment fragment, Context context) {
        n_onFragmentPreAttached(fragmentManager, fragment, context);
    }

    @Override // androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
    public void onFragmentPreCreated(FragmentManager fragmentManager, Fragment fragment, Bundle bundle) {
        n_onFragmentPreCreated(fragmentManager, fragment, bundle);
    }

    @Override // androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
    public void onFragmentResumed(FragmentManager fragmentManager, Fragment fragment) {
        n_onFragmentResumed(fragmentManager, fragment);
    }

    @Override // androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
    public void onFragmentSaveInstanceState(FragmentManager fragmentManager, Fragment fragment, Bundle bundle) {
        n_onFragmentSaveInstanceState(fragmentManager, fragment, bundle);
    }

    @Override // androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
    public void onFragmentStarted(FragmentManager fragmentManager, Fragment fragment) {
        n_onFragmentStarted(fragmentManager, fragment);
    }

    @Override // androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
    public void onFragmentStopped(FragmentManager fragmentManager, Fragment fragment) {
        n_onFragmentStopped(fragmentManager, fragment);
    }

    @Override // androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
    public void onFragmentViewCreated(FragmentManager fragmentManager, Fragment fragment, View view, Bundle bundle) {
        n_onFragmentViewCreated(fragmentManager, fragment, view, bundle);
    }

    @Override // androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
    public void onFragmentViewDestroyed(FragmentManager fragmentManager, Fragment fragment) {
        n_onFragmentViewDestroyed(fragmentManager, fragment);
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
