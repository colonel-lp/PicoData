package mono.android.telephony;

import android.telephony.TelephonyCallback;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TelephonyCallback_DataActivationStateListenerImplementor implements IGCUserPeer, TelephonyCallback.DataActivationStateListener {
    public static final String __md_methods = "n_onDataActivationStateChanged:(I)V:GetOnDataActivationStateChanged_IHandler:Android.Telephony.TelephonyCallback/IDataActivationStateListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onDataActivationStateChanged(int i);

    static {
        Runtime.register("Android.Telephony.TelephonyCallback+IDataActivationStateListenerImplementor, Mono.Android", TelephonyCallback_DataActivationStateListenerImplementor.class, __md_methods);
    }

    public TelephonyCallback_DataActivationStateListenerImplementor() {
        if (getClass() == TelephonyCallback_DataActivationStateListenerImplementor.class) {
            TypeManager.Activate("Android.Telephony.TelephonyCallback+IDataActivationStateListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.telephony.TelephonyCallback.DataActivationStateListener
    public void onDataActivationStateChanged(int i) {
        n_onDataActivationStateChanged(i);
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
