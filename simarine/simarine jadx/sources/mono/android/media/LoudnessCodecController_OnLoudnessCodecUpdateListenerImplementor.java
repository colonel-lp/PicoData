package mono.android.media;

import android.media.LoudnessCodecController;
import android.media.MediaCodec;
import android.os.Bundle;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class LoudnessCodecController_OnLoudnessCodecUpdateListenerImplementor implements IGCUserPeer, LoudnessCodecController.OnLoudnessCodecUpdateListener {
    public static final String __md_methods = "n_onLoudnessCodecUpdate:(Landroid/media/MediaCodec;Landroid/os/Bundle;)Landroid/os/Bundle;:GetOnLoudnessCodecUpdate_Landroid_media_MediaCodec_Landroid_os_Bundle_Handler:Android.Media.LoudnessCodecController/IOnLoudnessCodecUpdateListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native Bundle n_onLoudnessCodecUpdate(MediaCodec mediaCodec, Bundle bundle);

    static {
        Runtime.register("Android.Media.LoudnessCodecController+IOnLoudnessCodecUpdateListenerImplementor, Mono.Android", LoudnessCodecController_OnLoudnessCodecUpdateListenerImplementor.class, __md_methods);
    }

    public LoudnessCodecController_OnLoudnessCodecUpdateListenerImplementor() {
        if (getClass() == LoudnessCodecController_OnLoudnessCodecUpdateListenerImplementor.class) {
            TypeManager.Activate("Android.Media.LoudnessCodecController+IOnLoudnessCodecUpdateListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    public Bundle onLoudnessCodecUpdate(MediaCodec mediaCodec, Bundle bundle) {
        return n_onLoudnessCodecUpdate(mediaCodec, bundle);
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
