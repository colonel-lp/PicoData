package mono.android.telephony;

import android.telephony.TelephonyCallback;
import android.telephony.ims.ImsReasonInfo;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TelephonyCallback_ImsCallDisconnectCauseListenerImplementor implements IGCUserPeer, TelephonyCallback.ImsCallDisconnectCauseListener {
    public static final String __md_methods = "n_onImsCallDisconnectCauseChanged:(Landroid/telephony/ims/ImsReasonInfo;)V:GetOnImsCallDisconnectCauseChanged_Landroid_telephony_ims_ImsReasonInfo_Handler:Android.Telephony.TelephonyCallback/IImsCallDisconnectCauseListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onImsCallDisconnectCauseChanged(ImsReasonInfo imsReasonInfo);

    static {
        Runtime.register("Android.Telephony.TelephonyCallback+IImsCallDisconnectCauseListenerImplementor, Mono.Android", TelephonyCallback_ImsCallDisconnectCauseListenerImplementor.class, __md_methods);
    }

    public TelephonyCallback_ImsCallDisconnectCauseListenerImplementor() {
        if (getClass() == TelephonyCallback_ImsCallDisconnectCauseListenerImplementor.class) {
            TypeManager.Activate("Android.Telephony.TelephonyCallback+IImsCallDisconnectCauseListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.telephony.TelephonyCallback.ImsCallDisconnectCauseListener
    public void onImsCallDisconnectCauseChanged(ImsReasonInfo imsReasonInfo) {
        n_onImsCallDisconnectCauseChanged(imsReasonInfo);
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
