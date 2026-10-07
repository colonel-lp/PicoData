package mono.android.telephony;

import android.telephony.TelephonyCallback;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TelephonyCallback_MessageWaitingIndicatorListenerImplementor implements IGCUserPeer, TelephonyCallback.MessageWaitingIndicatorListener {
    public static final String __md_methods = "n_onMessageWaitingIndicatorChanged:(Z)V:GetOnMessageWaitingIndicatorChanged_ZHandler:Android.Telephony.TelephonyCallback/IMessageWaitingIndicatorListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onMessageWaitingIndicatorChanged(boolean z);

    static {
        Runtime.register("Android.Telephony.TelephonyCallback+IMessageWaitingIndicatorListenerImplementor, Mono.Android", TelephonyCallback_MessageWaitingIndicatorListenerImplementor.class, __md_methods);
    }

    public TelephonyCallback_MessageWaitingIndicatorListenerImplementor() {
        if (getClass() == TelephonyCallback_MessageWaitingIndicatorListenerImplementor.class) {
            TypeManager.Activate("Android.Telephony.TelephonyCallback+IMessageWaitingIndicatorListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.telephony.TelephonyCallback.MessageWaitingIndicatorListener
    public void onMessageWaitingIndicatorChanged(boolean z) {
        n_onMessageWaitingIndicatorChanged(z);
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
