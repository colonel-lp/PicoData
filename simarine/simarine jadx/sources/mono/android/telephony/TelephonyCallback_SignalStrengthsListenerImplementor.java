package mono.android.telephony;

import android.telephony.SignalStrength;
import android.telephony.TelephonyCallback;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TelephonyCallback_SignalStrengthsListenerImplementor implements IGCUserPeer, TelephonyCallback.SignalStrengthsListener {
    public static final String __md_methods = "n_onSignalStrengthsChanged:(Landroid/telephony/SignalStrength;)V:GetOnSignalStrengthsChanged_Landroid_telephony_SignalStrength_Handler:Android.Telephony.TelephonyCallback/ISignalStrengthsListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onSignalStrengthsChanged(SignalStrength signalStrength);

    static {
        Runtime.register("Android.Telephony.TelephonyCallback+ISignalStrengthsListenerImplementor, Mono.Android", TelephonyCallback_SignalStrengthsListenerImplementor.class, __md_methods);
    }

    public TelephonyCallback_SignalStrengthsListenerImplementor() {
        if (getClass() == TelephonyCallback_SignalStrengthsListenerImplementor.class) {
            TypeManager.Activate("Android.Telephony.TelephonyCallback+ISignalStrengthsListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.telephony.TelephonyCallback.SignalStrengthsListener
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        n_onSignalStrengthsChanged(signalStrength);
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
