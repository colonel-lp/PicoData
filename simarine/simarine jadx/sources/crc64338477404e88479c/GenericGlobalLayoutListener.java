package crc64338477404e88479c;

import android.view.ViewTreeObserver;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class GenericGlobalLayoutListener implements IGCUserPeer, ViewTreeObserver.OnGlobalLayoutListener {
    public static final String __md_methods = "n_onGlobalLayout:()V:GetOnGlobalLayoutHandler:Android.Views.ViewTreeObserver/IOnGlobalLayoutListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onGlobalLayout();

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.GenericGlobalLayoutListener, Microsoft.Maui.Controls", GenericGlobalLayoutListener.class, "n_onGlobalLayout:()V:GetOnGlobalLayoutHandler:Android.Views.ViewTreeObserver/IOnGlobalLayoutListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n");
    }

    public GenericGlobalLayoutListener() {
        if (getClass() == GenericGlobalLayoutListener.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.GenericGlobalLayoutListener, Microsoft.Maui.Controls", "", this, new Object[0]);
        }
    }

    @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
    public void onGlobalLayout() {
        n_onGlobalLayout();
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
