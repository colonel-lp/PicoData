package mono.android.net.nsd;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class NsdManager_ResolveListenerImplementor implements IGCUserPeer, NsdManager.ResolveListener {
    public static final String __md_methods = "n_onResolveFailed:(Landroid/net/nsd/NsdServiceInfo;I)V:GetOnResolveFailed_Landroid_net_nsd_NsdServiceInfo_IHandler:Android.Net.Nsd.NsdManager/IResolveListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onServiceResolved:(Landroid/net/nsd/NsdServiceInfo;)V:GetOnServiceResolved_Landroid_net_nsd_NsdServiceInfo_Handler:Android.Net.Nsd.NsdManager/IResolveListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onResolutionStopped:(Landroid/net/nsd/NsdServiceInfo;)V:GetOnResolutionStopped_Landroid_net_nsd_NsdServiceInfo_Handler:Android.Net.Nsd.NsdManager/IResolveListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onStopResolutionFailed:(Landroid/net/nsd/NsdServiceInfo;I)V:GetOnStopResolutionFailed_Landroid_net_nsd_NsdServiceInfo_IHandler:Android.Net.Nsd.NsdManager/IResolveListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onResolutionStopped(NsdServiceInfo nsdServiceInfo);

    private native void n_onResolveFailed(NsdServiceInfo nsdServiceInfo, int i);

    private native void n_onServiceResolved(NsdServiceInfo nsdServiceInfo);

    private native void n_onStopResolutionFailed(NsdServiceInfo nsdServiceInfo, int i);

    static {
        Runtime.register("Android.Net.Nsd.NsdManager+IResolveListenerImplementor, Mono.Android", NsdManager_ResolveListenerImplementor.class, __md_methods);
    }

    public NsdManager_ResolveListenerImplementor() {
        if (getClass() == NsdManager_ResolveListenerImplementor.class) {
            TypeManager.Activate("Android.Net.Nsd.NsdManager+IResolveListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.net.nsd.NsdManager.ResolveListener
    public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
        n_onResolveFailed(nsdServiceInfo, i);
    }

    @Override // android.net.nsd.NsdManager.ResolveListener
    public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
        n_onServiceResolved(nsdServiceInfo);
    }

    @Override // android.net.nsd.NsdManager.ResolveListener
    public void onResolutionStopped(NsdServiceInfo nsdServiceInfo) {
        n_onResolutionStopped(nsdServiceInfo);
    }

    @Override // android.net.nsd.NsdManager.ResolveListener
    public void onStopResolutionFailed(NsdServiceInfo nsdServiceInfo, int i) {
        n_onStopResolutionFailed(nsdServiceInfo, i);
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
