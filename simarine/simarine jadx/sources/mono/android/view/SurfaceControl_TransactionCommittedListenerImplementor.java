package mono.android.view;

import android.view.SurfaceControl;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class SurfaceControl_TransactionCommittedListenerImplementor implements IGCUserPeer, SurfaceControl.TransactionCommittedListener {
    public static final String __md_methods = "n_onTransactionCommitted:()V:GetOnTransactionCommittedHandler:Android.Views.SurfaceControl/ITransactionCommittedListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onTransactionCommitted();

    static {
        Runtime.register("Android.Views.SurfaceControl+ITransactionCommittedListenerImplementor, Mono.Android", SurfaceControl_TransactionCommittedListenerImplementor.class, __md_methods);
    }

    public SurfaceControl_TransactionCommittedListenerImplementor() {
        if (getClass() == SurfaceControl_TransactionCommittedListenerImplementor.class) {
            TypeManager.Activate("Android.Views.SurfaceControl+ITransactionCommittedListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.view.SurfaceControl.TransactionCommittedListener
    public void onTransactionCommitted() {
        n_onTransactionCommitted();
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
