package crc64338477404e88479c;

import android.content.Context;
import androidx.activity.ComponentDialog;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ModalNavigationManager_ModalFragment_CustomComponentDialog extends ComponentDialog implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.ModalNavigationManager+ModalFragment+CustomComponentDialog, Microsoft.Maui.Controls", ModalNavigationManager_ModalFragment_CustomComponentDialog.class, "");
    }

    public ModalNavigationManager_ModalFragment_CustomComponentDialog(Context context) {
        super(context);
        if (getClass() == ModalNavigationManager_ModalFragment_CustomComponentDialog.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.ModalNavigationManager+ModalFragment+CustomComponentDialog, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public ModalNavigationManager_ModalFragment_CustomComponentDialog(Context context, int i) {
        super(context, i);
        if (getClass() == ModalNavigationManager_ModalFragment_CustomComponentDialog.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.ModalNavigationManager+ModalFragment+CustomComponentDialog, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, Integer.valueOf(i)});
        }
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
