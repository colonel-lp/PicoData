package mono.android.telephony;

import android.telephony.TelephonyCallback;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TelephonyCallback_CallForwardingIndicatorListenerImplementor implements IGCUserPeer, TelephonyCallback.CallForwardingIndicatorListener {
    public static final String __md_methods = "n_onCallForwardingIndicatorChanged:(Z)V:GetOnCallForwardingIndicatorChanged_ZHandler:Android.Telephony.TelephonyCallback/ICallForwardingIndicatorListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onCallForwardingIndicatorChanged(boolean z);

    static {
        Runtime.register("Android.Telephony.TelephonyCallback+ICallForwardingIndicatorListenerImplementor, Mono.Android", TelephonyCallback_CallForwardingIndicatorListenerImplementor.class, __md_methods);
    }

    public TelephonyCallback_CallForwardingIndicatorListenerImplementor() {
        if (getClass() == TelephonyCallback_CallForwardingIndicatorListenerImplementor.class) {
            TypeManager.Activate("Android.Telephony.TelephonyCallback+ICallForwardingIndicatorListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.telephony.TelephonyCallback.CallForwardingIndicatorListener
    public void onCallForwardingIndicatorChanged(boolean z) {
        n_onCallForwardingIndicatorChanged(z);
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
