package mono.android.view;

import android.view.ContentInfo;
import android.view.OnReceiveContentListener;
import android.view.View;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class OnReceiveContentListenerImplementor implements IGCUserPeer, OnReceiveContentListener {
    public static final String __md_methods = "n_onReceiveContent:(Landroid/view/View;Landroid/view/ContentInfo;)Landroid/view/ContentInfo;:GetOnReceiveContent_Landroid_view_View_Landroid_view_ContentInfo_Handler:Android.Views.IOnReceiveContentListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native ContentInfo n_onReceiveContent(View view, ContentInfo contentInfo);

    static {
        Runtime.register("Android.Views.IOnReceiveContentListenerImplementor, Mono.Android", OnReceiveContentListenerImplementor.class, __md_methods);
    }

    public OnReceiveContentListenerImplementor() {
        if (getClass() == OnReceiveContentListenerImplementor.class) {
            TypeManager.Activate("Android.Views.IOnReceiveContentListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.view.OnReceiveContentListener
    public ContentInfo onReceiveContent(View view, ContentInfo contentInfo) {
        return n_onReceiveContent(view, contentInfo);
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
