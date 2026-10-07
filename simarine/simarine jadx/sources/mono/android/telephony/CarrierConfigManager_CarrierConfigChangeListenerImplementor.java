package mono.android.telephony;

import android.telephony.CarrierConfigManager;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class CarrierConfigManager_CarrierConfigChangeListenerImplementor implements IGCUserPeer, CarrierConfigManager.CarrierConfigChangeListener {
    public static final String __md_methods = "n_onCarrierConfigChanged:(IIII)V:GetOnCarrierConfigChanged_IIIIHandler:Android.Telephony.CarrierConfigManager/ICarrierConfigChangeListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onCarrierConfigChanged(int i, int i2, int i3, int i4);

    static {
        Runtime.register("Android.Telephony.CarrierConfigManager+ICarrierConfigChangeListenerImplementor, Mono.Android", CarrierConfigManager_CarrierConfigChangeListenerImplementor.class, __md_methods);
    }

    public CarrierConfigManager_CarrierConfigChangeListenerImplementor() {
        if (getClass() == CarrierConfigManager_CarrierConfigChangeListenerImplementor.class) {
            TypeManager.Activate("Android.Telephony.CarrierConfigManager+ICarrierConfigChangeListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.telephony.CarrierConfigManager.CarrierConfigChangeListener
    public void onCarrierConfigChanged(int i, int i2, int i3, int i4) {
        n_onCarrierConfigChanged(i, i2, i3, i4);
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
