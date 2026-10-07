package mono.android.app;

import android.app.KeyguardManager;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class KeyguardManager_KeyguardLockedStateListenerImplementor implements IGCUserPeer, KeyguardManager.KeyguardLockedStateListener {
    public static final String __md_methods = "n_onKeyguardLockedStateChanged:(Z)V:GetOnKeyguardLockedStateChanged_ZHandler:Android.App.KeyguardManager/IKeyguardLockedStateListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onKeyguardLockedStateChanged(boolean z);

    static {
        Runtime.register("Android.App.KeyguardManager+IKeyguardLockedStateListenerImplementor, Mono.Android", KeyguardManager_KeyguardLockedStateListenerImplementor.class, __md_methods);
    }

    public KeyguardManager_KeyguardLockedStateListenerImplementor() {
        if (getClass() == KeyguardManager_KeyguardLockedStateListenerImplementor.class) {
            TypeManager.Activate("Android.App.KeyguardManager+IKeyguardLockedStateListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.app.KeyguardManager.KeyguardLockedStateListener
    public void onKeyguardLockedStateChanged(boolean z) {
        n_onKeyguardLockedStateChanged(z);
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
