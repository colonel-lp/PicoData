package mono.android.telephony;

import android.telephony.TelephonyCallback;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TelephonyCallback_DataConnectionStateListenerImplementor implements IGCUserPeer, TelephonyCallback.DataConnectionStateListener {
    public static final String __md_methods = "n_onDataConnectionStateChanged:(II)V:GetOnDataConnectionStateChanged_IIHandler:Android.Telephony.TelephonyCallback/IDataConnectionStateListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onDataConnectionStateChanged(int i, int i2);

    static {
        Runtime.register("Android.Telephony.TelephonyCallback+IDataConnectionStateListenerImplementor, Mono.Android", TelephonyCallback_DataConnectionStateListenerImplementor.class, __md_methods);
    }

    public TelephonyCallback_DataConnectionStateListenerImplementor() {
        if (getClass() == TelephonyCallback_DataConnectionStateListenerImplementor.class) {
            TypeManager.Activate("Android.Telephony.TelephonyCallback+IDataConnectionStateListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.telephony.TelephonyCallback.DataConnectionStateListener
    public void onDataConnectionStateChanged(int i, int i2) {
        n_onDataConnectionStateChanged(i, i2);
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
