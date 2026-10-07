package mono.android.telephony;

import android.telephony.CellLocation;
import android.telephony.TelephonyCallback;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TelephonyCallback_CellLocationListenerImplementor implements IGCUserPeer, TelephonyCallback.CellLocationListener {
    public static final String __md_methods = "n_onCellLocationChanged:(Landroid/telephony/CellLocation;)V:GetOnCellLocationChanged_Landroid_telephony_CellLocation_Handler:Android.Telephony.TelephonyCallback/ICellLocationListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onCellLocationChanged(CellLocation cellLocation);

    static {
        Runtime.register("Android.Telephony.TelephonyCallback+ICellLocationListenerImplementor, Mono.Android", TelephonyCallback_CellLocationListenerImplementor.class, __md_methods);
    }

    public TelephonyCallback_CellLocationListenerImplementor() {
        if (getClass() == TelephonyCallback_CellLocationListenerImplementor.class) {
            TypeManager.Activate("Android.Telephony.TelephonyCallback+ICellLocationListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.telephony.TelephonyCallback.CellLocationListener
    public void onCellLocationChanged(CellLocation cellLocation) {
        n_onCellLocationChanged(cellLocation);
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
