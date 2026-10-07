package mono.android.telephony;

import android.telephony.TelephonyCallback;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TelephonyCallback_ActiveDataSubscriptionIdListenerImplementor implements IGCUserPeer, TelephonyCallback.ActiveDataSubscriptionIdListener {
    public static final String __md_methods = "n_onActiveDataSubscriptionIdChanged:(I)V:GetOnActiveDataSubscriptionIdChanged_IHandler:Android.Telephony.TelephonyCallback/IActiveDataSubscriptionIdListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onActiveDataSubscriptionIdChanged(int i);

    static {
        Runtime.register("Android.Telephony.TelephonyCallback+IActiveDataSubscriptionIdListenerImplementor, Mono.Android", TelephonyCallback_ActiveDataSubscriptionIdListenerImplementor.class, __md_methods);
    }

    public TelephonyCallback_ActiveDataSubscriptionIdListenerImplementor() {
        if (getClass() == TelephonyCallback_ActiveDataSubscriptionIdListenerImplementor.class) {
            TypeManager.Activate("Android.Telephony.TelephonyCallback+IActiveDataSubscriptionIdListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.telephony.TelephonyCallback.ActiveDataSubscriptionIdListener
    public void onActiveDataSubscriptionIdChanged(int i) {
        n_onActiveDataSubscriptionIdChanged(i);
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
