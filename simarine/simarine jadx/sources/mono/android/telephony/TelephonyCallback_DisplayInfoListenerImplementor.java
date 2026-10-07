package mono.android.telephony;

import android.telephony.TelephonyCallback;
import android.telephony.TelephonyDisplayInfo;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TelephonyCallback_DisplayInfoListenerImplementor implements IGCUserPeer, TelephonyCallback.DisplayInfoListener {
    public static final String __md_methods = "n_onDisplayInfoChanged:(Landroid/telephony/TelephonyDisplayInfo;)V:GetOnDisplayInfoChanged_Landroid_telephony_TelephonyDisplayInfo_Handler:Android.Telephony.TelephonyCallback/IDisplayInfoListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onDisplayInfoChanged(TelephonyDisplayInfo telephonyDisplayInfo);

    static {
        Runtime.register("Android.Telephony.TelephonyCallback+IDisplayInfoListenerImplementor, Mono.Android", TelephonyCallback_DisplayInfoListenerImplementor.class, __md_methods);
    }

    public TelephonyCallback_DisplayInfoListenerImplementor() {
        if (getClass() == TelephonyCallback_DisplayInfoListenerImplementor.class) {
            TypeManager.Activate("Android.Telephony.TelephonyCallback+IDisplayInfoListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.telephony.TelephonyCallback.DisplayInfoListener
    public void onDisplayInfoChanged(TelephonyDisplayInfo telephonyDisplayInfo) {
        n_onDisplayInfoChanged(telephonyDisplayInfo);
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
