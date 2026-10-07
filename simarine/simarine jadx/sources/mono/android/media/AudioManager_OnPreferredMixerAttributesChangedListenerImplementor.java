package mono.android.media;

import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.AudioMixerAttributes;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class AudioManager_OnPreferredMixerAttributesChangedListenerImplementor implements IGCUserPeer, AudioManager.OnPreferredMixerAttributesChangedListener {
    public static final String __md_methods = "n_onPreferredMixerAttributesChanged:(Landroid/media/AudioAttributes;Landroid/media/AudioDeviceInfo;Landroid/media/AudioMixerAttributes;)V:GetOnPreferredMixerAttributesChanged_Landroid_media_AudioAttributes_Landroid_media_AudioDeviceInfo_Landroid_media_AudioMixerAttributes_Handler:Android.Media.AudioManager/IOnPreferredMixerAttributesChangedListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onPreferredMixerAttributesChanged(AudioAttributes audioAttributes, AudioDeviceInfo audioDeviceInfo, AudioMixerAttributes audioMixerAttributes);

    static {
        Runtime.register("Android.Media.AudioManager+IOnPreferredMixerAttributesChangedListenerImplementor, Mono.Android", AudioManager_OnPreferredMixerAttributesChangedListenerImplementor.class, __md_methods);
    }

    public AudioManager_OnPreferredMixerAttributesChangedListenerImplementor() {
        if (getClass() == AudioManager_OnPreferredMixerAttributesChangedListenerImplementor.class) {
            TypeManager.Activate("Android.Media.AudioManager+IOnPreferredMixerAttributesChangedListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.media.AudioManager.OnPreferredMixerAttributesChangedListener
    public void onPreferredMixerAttributesChanged(AudioAttributes audioAttributes, AudioDeviceInfo audioDeviceInfo, AudioMixerAttributes audioMixerAttributes) {
        n_onPreferredMixerAttributesChanged(audioAttributes, audioDeviceInfo, audioMixerAttributes);
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
