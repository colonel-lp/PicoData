package crc64338477404e88479c;

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
public class FragmentContainer extends Fragment implements IGCUserPeer {
    public static final String __md_methods = "n_onCreateView:(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;:GetOnCreateView_Landroid_view_LayoutInflater_Landroid_view_ViewGroup_Landroid_os_Bundle_Handler\nn_onDestroy:()V:GetOnDestroyHandler\nn_onResume:()V:GetOnResumeHandler\n";
    private ArrayList refList;

    private native View n_onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle);

    private native void n_onDestroy();

    private native void n_onResume();

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.FragmentContainer, Microsoft.Maui.Controls", FragmentContainer.class, __md_methods);
    }

    public FragmentContainer() {
        if (getClass() == FragmentContainer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.FragmentContainer, Microsoft.Maui.Controls", "", this, new Object[0]);
        }
    }

    public FragmentContainer(int i) {
        super(i);
        if (getClass() == FragmentContainer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.FragmentContainer, Microsoft.Maui.Controls", "System.Int32, System.Private.CoreLib", this, new Object[]{Integer.valueOf(i)});
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return n_onCreateView(layoutInflater, viewGroup, bundle);
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        n_onDestroy();
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        n_onResume();
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
