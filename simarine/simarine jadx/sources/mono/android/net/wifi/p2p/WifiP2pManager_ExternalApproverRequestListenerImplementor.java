package mono.android.net.wifi.p2p;

import android.net.MacAddress;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class WifiP2pManager_ExternalApproverRequestListenerImplementor implements IGCUserPeer, WifiP2pManager.ExternalApproverRequestListener {
    public static final String __md_methods = "n_onAttached:(Landroid/net/MacAddress;)V:GetOnAttached_Landroid_net_MacAddress_Handler:Android.Net.Wifi.P2p.WifiP2pManager/IExternalApproverRequestListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onConnectionRequested:(ILandroid/net/wifi/p2p/WifiP2pConfig;Landroid/net/wifi/p2p/WifiP2pDevice;)V:GetOnConnectionRequested_ILandroid_net_wifi_p2p_WifiP2pConfig_Landroid_net_wifi_p2p_WifiP2pDevice_Handler:Android.Net.Wifi.P2p.WifiP2pManager/IExternalApproverRequestListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onDetached:(Landroid/net/MacAddress;I)V:GetOnDetached_Landroid_net_MacAddress_IHandler:Android.Net.Wifi.P2p.WifiP2pManager/IExternalApproverRequestListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onPinGenerated:(Landroid/net/MacAddress;Ljava/lang/String;)V:GetOnPinGenerated_Landroid_net_MacAddress_Ljava_lang_String_Handler:Android.Net.Wifi.P2p.WifiP2pManager/IExternalApproverRequestListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onAttached(MacAddress macAddress);

    private native void n_onConnectionRequested(int i, WifiP2pConfig wifiP2pConfig, WifiP2pDevice wifiP2pDevice);

    private native void n_onDetached(MacAddress macAddress, int i);

    private native void n_onPinGenerated(MacAddress macAddress, String str);

    static {
        Runtime.register("Android.Net.Wifi.P2p.WifiP2pManager+IExternalApproverRequestListenerImplementor, Mono.Android", WifiP2pManager_ExternalApproverRequestListenerImplementor.class, __md_methods);
    }

    public WifiP2pManager_ExternalApproverRequestListenerImplementor() {
        if (getClass() == WifiP2pManager_ExternalApproverRequestListenerImplementor.class) {
            TypeManager.Activate("Android.Net.Wifi.P2p.WifiP2pManager+IExternalApproverRequestListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.net.wifi.p2p.WifiP2pManager.ExternalApproverRequestListener
    public void onAttached(MacAddress macAddress) {
        n_onAttached(macAddress);
    }

    @Override // android.net.wifi.p2p.WifiP2pManager.ExternalApproverRequestListener
    public void onConnectionRequested(int i, WifiP2pConfig wifiP2pConfig, WifiP2pDevice wifiP2pDevice) {
        n_onConnectionRequested(i, wifiP2pConfig, wifiP2pDevice);
    }

    @Override // android.net.wifi.p2p.WifiP2pManager.ExternalApproverRequestListener
    public void onDetached(MacAddress macAddress, int i) {
        n_onDetached(macAddress, i);
    }

    @Override // android.net.wifi.p2p.WifiP2pManager.ExternalApproverRequestListener
    public void onPinGenerated(MacAddress macAddress, String str) {
        n_onPinGenerated(macAddress, str);
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
