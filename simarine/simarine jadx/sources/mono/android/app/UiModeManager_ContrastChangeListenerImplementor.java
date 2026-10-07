package mono.android.app;

import android.app.UiModeManager;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class UiModeManager_ContrastChangeListenerImplementor implements IGCUserPeer, UiModeManager.ContrastChangeListener {
    public static final String __md_methods = "n_onContrastChanged:(F)V:GetOnContrastChanged_FHandler:Android.App.UiModeManager/IContrastChangeListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onContrastChanged(float f);

    static {
        Runtime.register("Android.App.UiModeManager+IContrastChangeListenerImplementor, Mono.Android", UiModeManager_ContrastChangeListenerImplementor.class, __md_methods);
    }

    public UiModeManager_ContrastChangeListenerImplementor() {
        if (getClass() == UiModeManager_ContrastChangeListenerImplementor.class) {
            TypeManager.Activate("Android.App.UiModeManager+IContrastChangeListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.app.UiModeManager.ContrastChangeListener
    public void onContrastChanged(float f) {
        n_onContrastChanged(f);
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
