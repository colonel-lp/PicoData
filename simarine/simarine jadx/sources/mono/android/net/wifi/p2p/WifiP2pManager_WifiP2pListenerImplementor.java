package mono.android.net.wifi.p2p;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class WifiP2pManager_WifiP2pListenerImplementor implements IGCUserPeer, WifiP2pManager.WifiP2pListener {
    public static final String __md_methods = "n_onDeviceConfigurationChanged:(Landroid/net/wifi/p2p/WifiP2pDevice;)V:GetOnDeviceConfigurationChanged_Landroid_net_wifi_p2p_WifiP2pDevice_Handler:Android.Net.Wifi.P2p.WifiP2pManager/IWifiP2pListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onDiscoveryStateChanged:(I)V:GetOnDiscoveryStateChanged_IHandler:Android.Net.Wifi.P2p.WifiP2pManager/IWifiP2pListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onFrequencyChanged:(Landroid/net/wifi/p2p/WifiP2pInfo;Landroid/net/wifi/p2p/WifiP2pGroup;)V:GetOnFrequencyChanged_Landroid_net_wifi_p2p_WifiP2pInfo_Landroid_net_wifi_p2p_WifiP2pGroup_Handler:Android.Net.Wifi.P2p.WifiP2pManager/IWifiP2pListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onGroupCreated:(Landroid/net/wifi/p2p/WifiP2pInfo;Landroid/net/wifi/p2p/WifiP2pGroup;)V:GetOnGroupCreated_Landroid_net_wifi_p2p_WifiP2pInfo_Landroid_net_wifi_p2p_WifiP2pGroup_Handler:Android.Net.Wifi.P2p.WifiP2pManager/IWifiP2pListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onGroupCreating:()V:GetOnGroupCreatingHandler:Android.Net.Wifi.P2p.WifiP2pManager/IWifiP2pListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onGroupCreationFailed:(I)V:GetOnGroupCreationFailed_IHandler:Android.Net.Wifi.P2p.WifiP2pManager/IWifiP2pListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onGroupNegotiationRejectedByUser:()V:GetOnGroupNegotiationRejectedByUserHandler:Android.Net.Wifi.P2p.WifiP2pManager/IWifiP2pListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onGroupRemoved:()V:GetOnGroupRemovedHandler:Android.Net.Wifi.P2p.WifiP2pManager/IWifiP2pListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onListenStateChanged:(I)V:GetOnListenStateChanged_IHandler:Android.Net.Wifi.P2p.WifiP2pManager/IWifiP2pListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onP2pStateChanged:(I)V:GetOnP2pStateChanged_IHandler:Android.Net.Wifi.P2p.WifiP2pManager/IWifiP2pListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onPeerClientDisconnected:(Landroid/net/wifi/p2p/WifiP2pInfo;Landroid/net/wifi/p2p/WifiP2pGroup;)V:GetOnPeerClientDisconnected_Landroid_net_wifi_p2p_WifiP2pInfo_Landroid_net_wifi_p2p_WifiP2pGroup_Handler:Android.Net.Wifi.P2p.WifiP2pManager/IWifiP2pListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onPeerClientJoined:(Landroid/net/wifi/p2p/WifiP2pInfo;Landroid/net/wifi/p2p/WifiP2pGroup;)V:GetOnPeerClientJoined_Landroid_net_wifi_p2p_WifiP2pInfo_Landroid_net_wifi_p2p_WifiP2pGroup_Handler:Android.Net.Wifi.P2p.WifiP2pManager/IWifiP2pListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onPeerListChanged:(Landroid/net/wifi/p2p/WifiP2pDeviceList;)V:GetOnPeerListChanged_Landroid_net_wifi_p2p_WifiP2pDeviceList_Handler:Android.Net.Wifi.P2p.WifiP2pManager/IWifiP2pListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onDeviceConfigurationChanged(WifiP2pDevice wifiP2pDevice);

    private native void n_onDiscoveryStateChanged(int i);

    private native void n_onFrequencyChanged(WifiP2pInfo wifiP2pInfo, WifiP2pGroup wifiP2pGroup);

    private native void n_onGroupCreated(WifiP2pInfo wifiP2pInfo, WifiP2pGroup wifiP2pGroup);

    private native void n_onGroupCreating();

    private native void n_onGroupCreationFailed(int i);

    private native void n_onGroupNegotiationRejectedByUser();

    private native void n_onGroupRemoved();

    private native void n_onListenStateChanged(int i);

    private native void n_onP2pStateChanged(int i);

    private native void n_onPeerClientDisconnected(WifiP2pInfo wifiP2pInfo, WifiP2pGroup wifiP2pGroup);

    private native void n_onPeerClientJoined(WifiP2pInfo wifiP2pInfo, WifiP2pGroup wifiP2pGroup);

    private native void n_onPeerListChanged(WifiP2pDeviceList wifiP2pDeviceList);

    static {
        Runtime.register("Android.Net.Wifi.P2p.WifiP2pManager+IWifiP2pListenerImplementor, Mono.Android", WifiP2pManager_WifiP2pListenerImplementor.class, __md_methods);
    }

    public WifiP2pManager_WifiP2pListenerImplementor() {
        if (getClass() == WifiP2pManager_WifiP2pListenerImplementor.class) {
            TypeManager.Activate("Android.Net.Wifi.P2p.WifiP2pManager+IWifiP2pListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    public void onDeviceConfigurationChanged(WifiP2pDevice wifiP2pDevice) {
        n_onDeviceConfigurationChanged(wifiP2pDevice);
    }

    public void onDiscoveryStateChanged(int i) {
        n_onDiscoveryStateChanged(i);
    }

    public void onFrequencyChanged(WifiP2pInfo wifiP2pInfo, WifiP2pGroup wifiP2pGroup) {
        n_onFrequencyChanged(wifiP2pInfo, wifiP2pGroup);
    }

    public void onGroupCreated(WifiP2pInfo wifiP2pInfo, WifiP2pGroup wifiP2pGroup) {
        n_onGroupCreated(wifiP2pInfo, wifiP2pGroup);
    }

    public void onGroupCreating() {
        n_onGroupCreating();
    }

    public void onGroupCreationFailed(int i) {
        n_onGroupCreationFailed(i);
    }

    public void onGroupNegotiationRejectedByUser() {
        n_onGroupNegotiationRejectedByUser();
    }

    public void onGroupRemoved() {
        n_onGroupRemoved();
    }

    public void onListenStateChanged(int i) {
        n_onListenStateChanged(i);
    }

    public void onP2pStateChanged(int i) {
        n_onP2pStateChanged(i);
    }

    public void onPeerClientDisconnected(WifiP2pInfo wifiP2pInfo, WifiP2pGroup wifiP2pGroup) {
        n_onPeerClientDisconnected(wifiP2pInfo, wifiP2pGroup);
    }

    public void onPeerClientJoined(WifiP2pInfo wifiP2pInfo, WifiP2pGroup wifiP2pGroup) {
        n_onPeerClientJoined(wifiP2pInfo, wifiP2pGroup);
    }

    public void onPeerListChanged(WifiP2pDeviceList wifiP2pDeviceList) {
        n_onPeerListChanged(wifiP2pDeviceList);
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
