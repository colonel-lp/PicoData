package mono.android.telephony;

import android.telephony.TelephonyCallback;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TelephonyCallback_DataActivityListenerImplementor implements IGCUserPeer, TelephonyCallback.DataActivityListener {
    public static final String __md_methods = "n_onDataActivity:(I)V:GetOnDataActivity_IHandler:Android.Telephony.TelephonyCallback/IDataActivityListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onDataActivity(int i);

    static {
        Runtime.register("Android.Telephony.TelephonyCallback+IDataActivityListenerImplementor, Mono.Android", TelephonyCallback_DataActivityListenerImplementor.class, __md_methods);
    }

    public TelephonyCallback_DataActivityListenerImplementor() {
        if (getClass() == TelephonyCallback_DataActivityListenerImplementor.class) {
            TypeManager.Activate("Android.Telephony.TelephonyCallback+IDataActivityListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.telephony.TelephonyCallback.DataActivityListener
    public void onDataActivity(int i) {
        n_onDataActivity(i);
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
