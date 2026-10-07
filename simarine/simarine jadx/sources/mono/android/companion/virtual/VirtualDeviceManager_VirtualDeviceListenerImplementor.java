package mono.android.companion.virtual;

import android.companion.virtual.VirtualDeviceManager;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class VirtualDeviceManager_VirtualDeviceListenerImplementor implements IGCUserPeer, VirtualDeviceManager.VirtualDeviceListener {
    public static final String __md_methods = "n_onVirtualDeviceClosed:(I)V:GetOnVirtualDeviceClosed_IHandler:Android.Companion.Virtual.VirtualDeviceManager/IVirtualDeviceListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onVirtualDeviceCreated:(I)V:GetOnVirtualDeviceCreated_IHandler:Android.Companion.Virtual.VirtualDeviceManager/IVirtualDeviceListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onVirtualDeviceClosed(int i);

    private native void n_onVirtualDeviceCreated(int i);

    static {
        Runtime.register("Android.Companion.Virtual.VirtualDeviceManager+IVirtualDeviceListenerImplementor, Mono.Android", VirtualDeviceManager_VirtualDeviceListenerImplementor.class, __md_methods);
    }

    public VirtualDeviceManager_VirtualDeviceListenerImplementor() {
        if (getClass() == VirtualDeviceManager_VirtualDeviceListenerImplementor.class) {
            TypeManager.Activate("Android.Companion.Virtual.VirtualDeviceManager+IVirtualDeviceListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    public void onVirtualDeviceClosed(int i) {
        n_onVirtualDeviceClosed(i);
    }

    public void onVirtualDeviceCreated(int i) {
        n_onVirtualDeviceCreated(i);
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
