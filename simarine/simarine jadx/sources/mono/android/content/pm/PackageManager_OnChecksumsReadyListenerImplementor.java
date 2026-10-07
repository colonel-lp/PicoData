package mono.android.content.pm;

import android.content.pm.PackageManager;
import java.util.ArrayList;
import java.util.List;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class PackageManager_OnChecksumsReadyListenerImplementor implements IGCUserPeer, PackageManager.OnChecksumsReadyListener {
    public static final String __md_methods = "n_onChecksumsReady:(Ljava/util/List;)V:GetOnChecksumsReady_Ljava_util_List_Handler:Android.Content.PM.PackageManager/IOnChecksumsReadyListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onChecksumsReady(List list);

    static {
        Runtime.register("Android.Content.PM.PackageManager+IOnChecksumsReadyListenerImplementor, Mono.Android", PackageManager_OnChecksumsReadyListenerImplementor.class, __md_methods);
    }

    public PackageManager_OnChecksumsReadyListenerImplementor() {
        if (getClass() == PackageManager_OnChecksumsReadyListenerImplementor.class) {
            TypeManager.Activate("Android.Content.PM.PackageManager+IOnChecksumsReadyListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.content.pm.PackageManager.OnChecksumsReadyListener
    public void onChecksumsReady(List list) {
        n_onChecksumsReady(list);
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
