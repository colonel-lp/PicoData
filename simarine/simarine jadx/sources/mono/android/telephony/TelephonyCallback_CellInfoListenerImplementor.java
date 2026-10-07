package mono.android.telephony;

import android.telephony.TelephonyCallback;
import java.util.ArrayList;
import java.util.List;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TelephonyCallback_CellInfoListenerImplementor implements IGCUserPeer, TelephonyCallback.CellInfoListener {
    public static final String __md_methods = "n_onCellInfoChanged:(Ljava/util/List;)V:GetOnCellInfoChanged_Ljava_util_List_Handler:Android.Telephony.TelephonyCallback/ICellInfoListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onCellInfoChanged(List list);

    static {
        Runtime.register("Android.Telephony.TelephonyCallback+ICellInfoListenerImplementor, Mono.Android", TelephonyCallback_CellInfoListenerImplementor.class, __md_methods);
    }

    public TelephonyCallback_CellInfoListenerImplementor() {
        if (getClass() == TelephonyCallback_CellInfoListenerImplementor.class) {
            TypeManager.Activate("Android.Telephony.TelephonyCallback+ICellInfoListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.telephony.TelephonyCallback.CellInfoListener
    public void onCellInfoChanged(List list) {
        n_onCellInfoChanged(list);
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
