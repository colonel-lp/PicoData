package crc6452ffdc5b34af3a0f;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ScopedFragment extends Fragment implements IGCUserPeer {
    public static final String __md_methods = "n_onViewStateRestored:(Landroid/os/Bundle;)V:GetOnViewStateRestored_Landroid_os_Bundle_Handler\nn_onCreateView:(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;:GetOnCreateView_Landroid_view_LayoutInflater_Landroid_view_ViewGroup_Landroid_os_Bundle_Handler\nn_onDestroy:()V:GetOnDestroyHandler\n";
    private ArrayList refList;

    private native View n_onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle);

    private native void n_onDestroy();

    private native void n_onViewStateRestored(Bundle bundle);

    static {
        Runtime.register("Microsoft.Maui.Platform.ScopedFragment, Microsoft.Maui", ScopedFragment.class, __md_methods);
    }

    public ScopedFragment() {
        if (getClass() == ScopedFragment.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.ScopedFragment, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    public ScopedFragment(int i) {
        super(i);
        if (getClass() == ScopedFragment.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.ScopedFragment, Microsoft.Maui", "System.Int32, System.Private.CoreLib", this, new Object[]{Integer.valueOf(i)});
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewStateRestored(Bundle bundle) {
        n_onViewStateRestored(bundle);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return n_onCreateView(layoutInflater, viewGroup, bundle);
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        n_onDestroy();
    }

    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
