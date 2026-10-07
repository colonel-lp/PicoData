package mono.android.telephony;

import android.telephony.TelephonyCallback;
import java.util.ArrayList;
import java.util.List;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TelephonyCallback_PhysicalChannelConfigListenerImplementor implements IGCUserPeer, TelephonyCallback.PhysicalChannelConfigListener {
    public static final String __md_methods = "n_onPhysicalChannelConfigChanged:(Ljava/util/List;)V:GetOnPhysicalChannelConfigChanged_Ljava_util_List_Handler:Android.Telephony.TelephonyCallback/IPhysicalChannelConfigListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onPhysicalChannelConfigChanged(List list);

    static {
        Runtime.register("Android.Telephony.TelephonyCallback+IPhysicalChannelConfigListenerImplementor, Mono.Android", TelephonyCallback_PhysicalChannelConfigListenerImplementor.class, __md_methods);
    }

    public TelephonyCallback_PhysicalChannelConfigListenerImplementor() {
        if (getClass() == TelephonyCallback_PhysicalChannelConfigListenerImplementor.class) {
            TypeManager.Activate("Android.Telephony.TelephonyCallback+IPhysicalChannelConfigListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.telephony.TelephonyCallback.PhysicalChannelConfigListener
    public void onPhysicalChannelConfigChanged(List list) {
        n_onPhysicalChannelConfigChanged(list);
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
