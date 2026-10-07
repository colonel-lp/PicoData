package mono.android.media;

import android.media.AudioManager;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class AudioManager_OnModeChangedListenerImplementor implements IGCUserPeer, AudioManager.OnModeChangedListener {
    public static final String __md_methods = "n_onModeChanged:(I)V:GetOnModeChanged_IHandler:Android.Media.AudioManager/IOnModeChangedListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onModeChanged(int i);

    static {
        Runtime.register("Android.Media.AudioManager+IOnModeChangedListenerImplementor, Mono.Android", AudioManager_OnModeChangedListenerImplementor.class, __md_methods);
    }

    public AudioManager_OnModeChangedListenerImplementor() {
        if (getClass() == AudioManager_OnModeChangedListenerImplementor.class) {
            TypeManager.Activate("Android.Media.AudioManager+IOnModeChangedListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.media.AudioManager.OnModeChangedListener
    public void onModeChanged(int i) {
        n_onModeChanged(i);
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
