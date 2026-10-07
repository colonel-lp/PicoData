package crc640c0c3c65b94d234b;

import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class BottomSheetDialogTouchOutsideListener implements IGCUserPeer, View.OnTouchListener {
    public static final String __md_methods = "n_onTouch:(Landroid/view/View;Landroid/view/MotionEvent;)Z:GetOnTouch_Landroid_view_View_Landroid_view_MotionEvent_Handler:Android.Views.View/IOnTouchListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native boolean n_onTouch(View view, MotionEvent motionEvent);

    static {
        Runtime.register("Plugin.Maui.BottomSheet.Platform.Android.BottomSheetDialogTouchOutsideListener, Plugin.Maui.BottomSheet", BottomSheetDialogTouchOutsideListener.class, "n_onTouch:(Landroid/view/View;Landroid/view/MotionEvent;)Z:GetOnTouch_Landroid_view_View_Landroid_view_MotionEvent_Handler:Android.Views.View/IOnTouchListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n");
    }

    public BottomSheetDialogTouchOutsideListener() {
        if (getClass() == BottomSheetDialogTouchOutsideListener.class) {
            TypeManager.Activate("Plugin.Maui.BottomSheet.Platform.Android.BottomSheetDialogTouchOutsideListener, Plugin.Maui.BottomSheet", "", this, new Object[0]);
        }
    }

    public BottomSheetDialogTouchOutsideListener(AppCompatActivity appCompatActivity) {
        if (getClass() == BottomSheetDialogTouchOutsideListener.class) {
            TypeManager.Activate("Plugin.Maui.BottomSheet.Platform.Android.BottomSheetDialogTouchOutsideListener, Plugin.Maui.BottomSheet", "AndroidX.AppCompat.App.AppCompatActivity, Xamarin.AndroidX.AppCompat", this, new Object[]{appCompatActivity});
        }
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return n_onTouch(view, motionEvent);
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
