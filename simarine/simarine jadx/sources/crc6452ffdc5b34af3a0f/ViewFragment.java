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
public class ViewFragment extends Fragment implements IGCUserPeer {
    public static final String __md_methods = "n_onCreateView:(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;:GetOnCreateView_Landroid_view_LayoutInflater_Landroid_view_ViewGroup_Landroid_os_Bundle_Handler\n";
    private ArrayList refList;

    private native View n_onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle);

    static {
        Runtime.register("Microsoft.Maui.Platform.ViewFragment, Microsoft.Maui", ViewFragment.class, __md_methods);
    }

    public ViewFragment() {
        if (getClass() == ViewFragment.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.ViewFragment, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    public ViewFragment(int i) {
        super(i);
        if (getClass() == ViewFragment.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.ViewFragment, Microsoft.Maui", "System.Int32, System.Private.CoreLib", this, new Object[]{Integer.valueOf(i)});
        }
    }

    public ViewFragment(View view) {
        if (getClass() == ViewFragment.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.ViewFragment, Microsoft.Maui", "Android.Views.View, Mono.Android", this, new Object[]{view});
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return n_onCreateView(layoutInflater, viewGroup, bundle);
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
