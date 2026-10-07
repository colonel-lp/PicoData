package mono.android.net.wifi;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class WifiManager_LocalOnlyConnectionFailureListenerImplementor implements IGCUserPeer, WifiManager.LocalOnlyConnectionFailureListener {
    public static final String __md_methods = "n_onConnectionFailed:(Landroid/net/wifi/WifiNetworkSpecifier;I)V:GetOnConnectionFailed_Landroid_net_wifi_WifiNetworkSpecifier_IHandler:Android.Net.Wifi.WifiManager/ILocalOnlyConnectionFailureListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onConnectionFailed(WifiNetworkSpecifier wifiNetworkSpecifier, int i);

    static {
        Runtime.register("Android.Net.Wifi.WifiManager+ILocalOnlyConnectionFailureListenerImplementor, Mono.Android", WifiManager_LocalOnlyConnectionFailureListenerImplementor.class, __md_methods);
    }

    public WifiManager_LocalOnlyConnectionFailureListenerImplementor() {
        if (getClass() == WifiManager_LocalOnlyConnectionFailureListenerImplementor.class) {
            TypeManager.Activate("Android.Net.Wifi.WifiManager+ILocalOnlyConnectionFailureListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.net.wifi.WifiManager.LocalOnlyConnectionFailureListener
    public void onConnectionFailed(WifiNetworkSpecifier wifiNetworkSpecifier, int i) {
        n_onConnectionFailed(wifiNetworkSpecifier, i);
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
