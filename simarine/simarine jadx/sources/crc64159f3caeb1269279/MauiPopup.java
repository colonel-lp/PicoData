package crc64159f3caeb1269279;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.MotionEvent;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiPopup extends Dialog implements IGCUserPeer, DialogInterface.OnCancelListener {
    public static final String __md_methods = "n_show:()V:GetShowHandler\nn_onTouchEvent:(Landroid/view/MotionEvent;)Z:GetOnTouchEvent_Landroid_view_MotionEvent_Handler\nn_onCancel:(Landroid/content/DialogInterface;)V:GetOnCancel_Landroid_content_DialogInterface_Handler:Android.Content.IDialogInterfaceOnCancelListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onCancel(DialogInterface dialogInterface);

    private native boolean n_onTouchEvent(MotionEvent motionEvent);

    private native void n_show();

    static {
        Runtime.register("CommunityToolkit.Maui.Core.Views.MauiPopup, CommunityToolkit.Maui.Core", MauiPopup.class, __md_methods);
    }

    public MauiPopup(Context context) {
        super(context);
        if (getClass() == MauiPopup.class) {
            TypeManager.Activate("CommunityToolkit.Maui.Core.Views.MauiPopup, CommunityToolkit.Maui.Core", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // android.app.Dialog
    public void show() {
        n_show();
    }

    @Override // android.app.Dialog
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return n_onTouchEvent(motionEvent);
    }

    @Override // android.content.DialogInterface.OnCancelListener
    public void onCancel(DialogInterface dialogInterface) {
        n_onCancel(dialogInterface);
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
