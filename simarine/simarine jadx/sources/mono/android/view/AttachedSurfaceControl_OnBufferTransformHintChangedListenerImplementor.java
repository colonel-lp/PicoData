package mono.android.view;

import android.view.AttachedSurfaceControl;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class AttachedSurfaceControl_OnBufferTransformHintChangedListenerImplementor implements IGCUserPeer, AttachedSurfaceControl.OnBufferTransformHintChangedListener {
    public static final String __md_methods = "n_onBufferTransformHintChanged:(I)V:GetOnBufferTransformHintChanged_IHandler:Android.Views.IAttachedSurfaceControl/IOnBufferTransformHintChangedListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onBufferTransformHintChanged(int i);

    static {
        Runtime.register("Android.Views.IAttachedSurfaceControl+IOnBufferTransformHintChangedListenerImplementor, Mono.Android", AttachedSurfaceControl_OnBufferTransformHintChangedListenerImplementor.class, __md_methods);
    }

    public AttachedSurfaceControl_OnBufferTransformHintChangedListenerImplementor() {
        if (getClass() == AttachedSurfaceControl_OnBufferTransformHintChangedListenerImplementor.class) {
            TypeManager.Activate("Android.Views.IAttachedSurfaceControl+IOnBufferTransformHintChangedListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.view.AttachedSurfaceControl.OnBufferTransformHintChangedListener
    public void onBufferTransformHintChanged(int i) {
        n_onBufferTransformHintChanged(i);
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
