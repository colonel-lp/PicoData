package crc640c0c3c65b94d234b;

import android.content.Context;
import android.content.DialogInterface;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class BottomSheetDialog extends com.google.android.material.bottomsheet.BottomSheetDialog implements IGCUserPeer {
    public static final String __md_methods = "n_cancel:()V:GetCancelHandler\nn_dismiss:()V:GetDismissHandler\n";
    private ArrayList refList;

    private native void n_cancel();

    private native void n_dismiss();

    static {
        Runtime.register("Plugin.Maui.BottomSheet.Platform.Android.BottomSheetDialog, Plugin.Maui.BottomSheet", BottomSheetDialog.class, __md_methods);
    }

    public BottomSheetDialog(Context context) {
        super(context);
        if (getClass() == BottomSheetDialog.class) {
            TypeManager.Activate("Plugin.Maui.BottomSheet.Platform.Android.BottomSheetDialog, Plugin.Maui.BottomSheet", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public BottomSheetDialog(Context context, boolean z, DialogInterface.OnCancelListener onCancelListener) {
        super(context, z, onCancelListener);
        if (getClass() == BottomSheetDialog.class) {
            TypeManager.Activate("Plugin.Maui.BottomSheet.Platform.Android.BottomSheetDialog, Plugin.Maui.BottomSheet", "Android.Content.Context, Mono.Android:System.Boolean, System.Private.CoreLib:Android.Content.IDialogInterfaceOnCancelListener, Mono.Android", this, new Object[]{context, Boolean.valueOf(z), onCancelListener});
        }
    }

    public BottomSheetDialog(Context context, int i) {
        super(context, i);
        if (getClass() == BottomSheetDialog.class) {
            TypeManager.Activate("Plugin.Maui.BottomSheet.Platform.Android.BottomSheetDialog, Plugin.Maui.BottomSheet", "Android.Content.Context, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, Integer.valueOf(i)});
        }
    }

    @Override // com.google.android.material.bottomsheet.BottomSheetDialog, android.app.Dialog, android.content.DialogInterface
    public void cancel() {
        n_cancel();
    }

    @Override // androidx.appcompat.app.AppCompatDialog, android.app.Dialog, android.content.DialogInterface
    public void dismiss() {
        n_dismiss();
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
