package mono.android.net.http;

import android.net.http.UrlRequest;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class UrlRequest_StatusListenerImplementor implements IGCUserPeer, UrlRequest.StatusListener {
    public static final String __md_methods = "n_onStatus:(I)V:GetOnStatus_IHandler:Android.Net.Http.UrlRequest/IStatusListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onStatus(int i);

    static {
        Runtime.register("Android.Net.Http.UrlRequest+IStatusListenerImplementor, Mono.Android", UrlRequest_StatusListenerImplementor.class, __md_methods);
    }

    public UrlRequest_StatusListenerImplementor() {
        if (getClass() == UrlRequest_StatusListenerImplementor.class) {
            TypeManager.Activate("Android.Net.Http.UrlRequest+IStatusListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.net.http.UrlRequest.StatusListener
    public void onStatus(int i) {
        n_onStatus(i);
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
