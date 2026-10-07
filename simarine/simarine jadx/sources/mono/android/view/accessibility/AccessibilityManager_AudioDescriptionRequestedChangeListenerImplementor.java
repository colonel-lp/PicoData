package mono.android.view.accessibility;

import android.view.accessibility.AccessibilityManager;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class AccessibilityManager_AudioDescriptionRequestedChangeListenerImplementor implements IGCUserPeer, AccessibilityManager.AudioDescriptionRequestedChangeListener {
    public static final String __md_methods = "n_onAudioDescriptionRequestedChanged:(Z)V:GetOnAudioDescriptionRequestedChanged_ZHandler:Android.Views.Accessibility.AccessibilityManager/IAudioDescriptionRequestedChangeListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onAudioDescriptionRequestedChanged(boolean z);

    static {
        Runtime.register("Android.Views.Accessibility.AccessibilityManager+IAudioDescriptionRequestedChangeListenerImplementor, Mono.Android", AccessibilityManager_AudioDescriptionRequestedChangeListenerImplementor.class, __md_methods);
    }

    public AccessibilityManager_AudioDescriptionRequestedChangeListenerImplementor() {
        if (getClass() == AccessibilityManager_AudioDescriptionRequestedChangeListenerImplementor.class) {
            TypeManager.Activate("Android.Views.Accessibility.AccessibilityManager+IAudioDescriptionRequestedChangeListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.view.accessibility.AccessibilityManager.AudioDescriptionRequestedChangeListener
    public void onAudioDescriptionRequestedChanged(boolean z) {
        n_onAudioDescriptionRequestedChanged(z);
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
