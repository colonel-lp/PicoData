package mono.android.os;

import android.os.PowerManager;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class PowerManager_WakeLockStateListenerImplementor implements IGCUserPeer, PowerManager.WakeLockStateListener {
    public static final String __md_methods = "n_onStateChanged:(Z)V:GetOnStateChanged_ZHandler:Android.OS.PowerManager/IWakeLockStateListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onStateChanged(boolean z);

    static {
        Runtime.register("Android.OS.PowerManager+IWakeLockStateListenerImplementor, Mono.Android", PowerManager_WakeLockStateListenerImplementor.class, __md_methods);
    }

    public PowerManager_WakeLockStateListenerImplementor() {
        if (getClass() == PowerManager_WakeLockStateListenerImplementor.class) {
            TypeManager.Activate("Android.OS.PowerManager+IWakeLockStateListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.os.PowerManager.WakeLockStateListener
    public void onStateChanged(boolean z) {
        n_onStateChanged(z);
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
