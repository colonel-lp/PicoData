package mono.android.media;

import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class AudioManager_OnCommunicationDeviceChangedListenerImplementor implements IGCUserPeer, AudioManager.OnCommunicationDeviceChangedListener {
    public static final String __md_methods = "n_onCommunicationDeviceChanged:(Landroid/media/AudioDeviceInfo;)V:GetOnCommunicationDeviceChanged_Landroid_media_AudioDeviceInfo_Handler:Android.Media.AudioManager/IOnCommunicationDeviceChangedListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onCommunicationDeviceChanged(AudioDeviceInfo audioDeviceInfo);

    static {
        Runtime.register("Android.Media.AudioManager+IOnCommunicationDeviceChangedListenerImplementor, Mono.Android", AudioManager_OnCommunicationDeviceChangedListenerImplementor.class, __md_methods);
    }

    public AudioManager_OnCommunicationDeviceChangedListenerImplementor() {
        if (getClass() == AudioManager_OnCommunicationDeviceChangedListenerImplementor.class) {
            TypeManager.Activate("Android.Media.AudioManager+IOnCommunicationDeviceChangedListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.media.AudioManager.OnCommunicationDeviceChangedListener
    public void onCommunicationDeviceChanged(AudioDeviceInfo audioDeviceInfo) {
        n_onCommunicationDeviceChanged(audioDeviceInfo);
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
