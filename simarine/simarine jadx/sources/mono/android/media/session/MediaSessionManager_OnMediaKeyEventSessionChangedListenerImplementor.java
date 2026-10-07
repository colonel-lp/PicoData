package mono.android.media.session;

import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MediaSessionManager_OnMediaKeyEventSessionChangedListenerImplementor implements IGCUserPeer, MediaSessionManager.OnMediaKeyEventSessionChangedListener {
    public static final String __md_methods = "n_onMediaKeyEventSessionChanged:(Ljava/lang/String;Landroid/media/session/MediaSession$Token;)V:GetOnMediaKeyEventSessionChanged_Ljava_lang_String_Landroid_media_session_MediaSession_Token_Handler:Android.Media.Session.MediaSessionManager/IOnMediaKeyEventSessionChangedListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onMediaKeyEventSessionChanged(String str, MediaSession.Token token);

    static {
        Runtime.register("Android.Media.Session.MediaSessionManager+IOnMediaKeyEventSessionChangedListenerImplementor, Mono.Android", MediaSessionManager_OnMediaKeyEventSessionChangedListenerImplementor.class, __md_methods);
    }

    public MediaSessionManager_OnMediaKeyEventSessionChangedListenerImplementor() {
        if (getClass() == MediaSessionManager_OnMediaKeyEventSessionChangedListenerImplementor.class) {
            TypeManager.Activate("Android.Media.Session.MediaSessionManager+IOnMediaKeyEventSessionChangedListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.media.session.MediaSessionManager.OnMediaKeyEventSessionChangedListener
    public void onMediaKeyEventSessionChanged(String str, MediaSession.Token token) {
        n_onMediaKeyEventSessionChanged(str, token);
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
