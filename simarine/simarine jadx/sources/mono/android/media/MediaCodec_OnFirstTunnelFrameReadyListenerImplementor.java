package mono.android.media;

import android.media.MediaCodec;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MediaCodec_OnFirstTunnelFrameReadyListenerImplementor implements IGCUserPeer, MediaCodec.OnFirstTunnelFrameReadyListener {
    public static final String __md_methods = "n_onFirstTunnelFrameReady:(Landroid/media/MediaCodec;)V:GetOnFirstTunnelFrameReady_Landroid_media_MediaCodec_Handler:Android.Media.MediaCodec/IOnFirstTunnelFrameReadyListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onFirstTunnelFrameReady(MediaCodec mediaCodec);

    static {
        Runtime.register("Android.Media.MediaCodec+IOnFirstTunnelFrameReadyListenerImplementor, Mono.Android", MediaCodec_OnFirstTunnelFrameReadyListenerImplementor.class, __md_methods);
    }

    public MediaCodec_OnFirstTunnelFrameReadyListenerImplementor() {
        if (getClass() == MediaCodec_OnFirstTunnelFrameReadyListenerImplementor.class) {
            TypeManager.Activate("Android.Media.MediaCodec+IOnFirstTunnelFrameReadyListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.media.MediaCodec.OnFirstTunnelFrameReadyListener
    public void onFirstTunnelFrameReady(MediaCodec mediaCodec) {
        n_onFirstTunnelFrameReady(mediaCodec);
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
