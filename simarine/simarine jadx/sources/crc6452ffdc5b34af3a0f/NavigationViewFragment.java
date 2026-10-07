package crc6452ffdc5b34af3a0f;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class NavigationViewFragment extends Fragment implements IGCUserPeer {
    public static final String __md_methods = "n_onCreateView:(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;:GetOnCreateView_Landroid_view_LayoutInflater_Landroid_view_ViewGroup_Landroid_os_Bundle_Handler\nn_onResume:()V:GetOnResumeHandler\nn_onDestroy:()V:GetOnDestroyHandler\nn_onCreateAnimation:(IZI)Landroid/view/animation/Animation;:GetOnCreateAnimation_IZIHandler\n";
    private ArrayList refList;

    private native Animation n_onCreateAnimation(int i, boolean z, int i2);

    private native View n_onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle);

    private native void n_onDestroy();

    private native void n_onResume();

    static {
        Runtime.register("Microsoft.Maui.Platform.NavigationViewFragment, Microsoft.Maui", NavigationViewFragment.class, __md_methods);
    }

    public NavigationViewFragment() {
        if (getClass() == NavigationViewFragment.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.NavigationViewFragment, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    public NavigationViewFragment(int i) {
        super(i);
        if (getClass() == NavigationViewFragment.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.NavigationViewFragment, Microsoft.Maui", "System.Int32, System.Private.CoreLib", this, new Object[]{Integer.valueOf(i)});
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return n_onCreateView(layoutInflater, viewGroup, bundle);
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        n_onResume();
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        n_onDestroy();
    }

    @Override // androidx.fragment.app.Fragment
    public Animation onCreateAnimation(int i, boolean z, int i2) {
        return n_onCreateAnimation(i, z, i2);
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
