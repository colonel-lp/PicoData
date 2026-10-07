package crc64338477404e88479c;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.DialogFragment;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ModalNavigationManager_ModalFragment extends DialogFragment implements IGCUserPeer {
    public static final String __md_methods = "n_onCreateDialog:(Landroid/os/Bundle;)Landroid/app/Dialog;:GetOnCreateDialog_Landroid_os_Bundle_Handler\nn_onCreateView:(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;:GetOnCreateView_Landroid_view_LayoutInflater_Landroid_view_ViewGroup_Landroid_os_Bundle_Handler\nn_onCreate:(Landroid/os/Bundle;)V:GetOnCreate_Landroid_os_Bundle_Handler\nn_onStart:()V:GetOnStartHandler\nn_onDismiss:(Landroid/content/DialogInterface;)V:GetOnDismiss_Landroid_content_DialogInterface_Handler\nn_onDestroy:()V:GetOnDestroyHandler\n";
    private ArrayList refList;

    private native void n_onCreate(Bundle bundle);

    private native Dialog n_onCreateDialog(Bundle bundle);

    private native View n_onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle);

    private native void n_onDestroy();

    private native void n_onDismiss(DialogInterface dialogInterface);

    private native void n_onStart();

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.ModalNavigationManager+ModalFragment, Microsoft.Maui.Controls", ModalNavigationManager_ModalFragment.class, __md_methods);
    }

    public ModalNavigationManager_ModalFragment() {
        if (getClass() == ModalNavigationManager_ModalFragment.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.ModalNavigationManager+ModalFragment, Microsoft.Maui.Controls", "", this, new Object[0]);
        }
    }

    public ModalNavigationManager_ModalFragment(int i) {
        super(i);
        if (getClass() == ModalNavigationManager_ModalFragment.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.ModalNavigationManager+ModalFragment, Microsoft.Maui.Controls", "System.Int32, System.Private.CoreLib", this, new Object[]{Integer.valueOf(i)});
        }
    }

    @Override // androidx.fragment.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        return n_onCreateDialog(bundle);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return n_onCreateView(layoutInflater, viewGroup, bundle);
    }

    @Override // androidx.fragment.app.DialogFragment, androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        n_onCreate(bundle);
    }

    @Override // androidx.fragment.app.DialogFragment, androidx.fragment.app.Fragment
    public void onStart() {
        n_onStart();
    }

    @Override // androidx.fragment.app.DialogFragment, android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        n_onDismiss(dialogInterface);
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        n_onDestroy();
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
