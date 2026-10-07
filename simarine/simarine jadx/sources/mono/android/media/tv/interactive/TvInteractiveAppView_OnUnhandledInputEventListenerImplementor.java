package mono.android.media.tv.interactive;

import android.media.tv.interactive.TvInteractiveAppView;
import android.view.InputEvent;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TvInteractiveAppView_OnUnhandledInputEventListenerImplementor implements IGCUserPeer, TvInteractiveAppView.OnUnhandledInputEventListener {
    public static final String __md_methods = "n_onUnhandledInputEvent:(Landroid/view/InputEvent;)Z:GetOnUnhandledInputEvent_Landroid_view_InputEvent_Handler:Android.Media.TV.Interactive.TvInteractiveAppView/IOnUnhandledInputEventListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native boolean n_onUnhandledInputEvent(InputEvent inputEvent);

    static {
        Runtime.register("Android.Media.TV.Interactive.TvInteractiveAppView+IOnUnhandledInputEventListenerImplementor, Mono.Android", TvInteractiveAppView_OnUnhandledInputEventListenerImplementor.class, __md_methods);
    }

    public TvInteractiveAppView_OnUnhandledInputEventListenerImplementor() {
        if (getClass() == TvInteractiveAppView_OnUnhandledInputEventListenerImplementor.class) {
            TypeManager.Activate("Android.Media.TV.Interactive.TvInteractiveAppView+IOnUnhandledInputEventListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.media.tv.interactive.TvInteractiveAppView.OnUnhandledInputEventListener
    public boolean onUnhandledInputEvent(InputEvent inputEvent) {
        return n_onUnhandledInputEvent(inputEvent);
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
