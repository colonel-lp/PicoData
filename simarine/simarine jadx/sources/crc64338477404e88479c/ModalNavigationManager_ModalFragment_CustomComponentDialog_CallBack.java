package crc64338477404e88479c;

import androidx.activity.OnBackPressedCallback;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ModalNavigationManager_ModalFragment_CustomComponentDialog_CallBack extends OnBackPressedCallback implements IGCUserPeer {
    public static final String __md_methods = "n_handleOnBackPressed:()V:GetHandleOnBackPressedHandler\n";
    private ArrayList refList;

    private native void n_handleOnBackPressed();

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.ModalNavigationManager+ModalFragment+CustomComponentDialog+CallBack, Microsoft.Maui.Controls", ModalNavigationManager_ModalFragment_CustomComponentDialog_CallBack.class, __md_methods);
    }

    public ModalNavigationManager_ModalFragment_CustomComponentDialog_CallBack(boolean z) {
        super(z);
        if (getClass() == ModalNavigationManager_ModalFragment_CustomComponentDialog_CallBack.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.ModalNavigationManager+ModalFragment+CustomComponentDialog+CallBack, Microsoft.Maui.Controls", "System.Boolean, System.Private.CoreLib", this, new Object[]{Boolean.valueOf(z)});
        }
    }

    @Override // androidx.activity.OnBackPressedCallback
    public void handleOnBackPressed() {
        n_handleOnBackPressed();
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
