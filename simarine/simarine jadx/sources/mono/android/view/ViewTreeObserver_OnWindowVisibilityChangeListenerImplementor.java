package mono.android.view;

import android.view.ViewTreeObserver;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ViewTreeObserver_OnWindowVisibilityChangeListenerImplementor implements IGCUserPeer, ViewTreeObserver.OnWindowVisibilityChangeListener {
    public static final String __md_methods = "n_onWindowVisibilityChanged:(I)V:GetOnWindowVisibilityChanged_IHandler:Android.Views.ViewTreeObserver/IOnWindowVisibilityChangeListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onWindowVisibilityChanged(int i);

    static {
        Runtime.register("Android.Views.ViewTreeObserver+IOnWindowVisibilityChangeListenerImplementor, Mono.Android", ViewTreeObserver_OnWindowVisibilityChangeListenerImplementor.class, __md_methods);
    }

    public ViewTreeObserver_OnWindowVisibilityChangeListenerImplementor() {
        if (getClass() == ViewTreeObserver_OnWindowVisibilityChangeListenerImplementor.class) {
            TypeManager.Activate("Android.Views.ViewTreeObserver+IOnWindowVisibilityChangeListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.view.ViewTreeObserver.OnWindowVisibilityChangeListener
    public void onWindowVisibilityChanged(int i) {
        n_onWindowVisibilityChanged(i);
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
