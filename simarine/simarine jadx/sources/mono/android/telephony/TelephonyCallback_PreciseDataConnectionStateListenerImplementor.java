package mono.android.telephony;

import android.telephony.PreciseDataConnectionState;
import android.telephony.TelephonyCallback;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TelephonyCallback_PreciseDataConnectionStateListenerImplementor implements IGCUserPeer, TelephonyCallback.PreciseDataConnectionStateListener {
    public static final String __md_methods = "n_onPreciseDataConnectionStateChanged:(Landroid/telephony/PreciseDataConnectionState;)V:GetOnPreciseDataConnectionStateChanged_Landroid_telephony_PreciseDataConnectionState_Handler:Android.Telephony.TelephonyCallback/IPreciseDataConnectionStateListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onPreciseDataConnectionStateChanged(PreciseDataConnectionState preciseDataConnectionState);

    static {
        Runtime.register("Android.Telephony.TelephonyCallback+IPreciseDataConnectionStateListenerImplementor, Mono.Android", TelephonyCallback_PreciseDataConnectionStateListenerImplementor.class, __md_methods);
    }

    public TelephonyCallback_PreciseDataConnectionStateListenerImplementor() {
        if (getClass() == TelephonyCallback_PreciseDataConnectionStateListenerImplementor.class) {
            TypeManager.Activate("Android.Telephony.TelephonyCallback+IPreciseDataConnectionStateListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.telephony.TelephonyCallback.PreciseDataConnectionStateListener
    public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState preciseDataConnectionState) {
        n_onPreciseDataConnectionStateChanged(preciseDataConnectionState);
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
