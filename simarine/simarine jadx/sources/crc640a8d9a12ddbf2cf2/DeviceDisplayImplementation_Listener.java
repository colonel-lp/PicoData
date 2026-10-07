package crc640a8d9a12ddbf2cf2;

import android.content.Context;
import android.view.OrientationEventListener;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class DeviceDisplayImplementation_Listener extends OrientationEventListener implements IGCUserPeer {
    public static final String __md_methods = "n_onOrientationChanged:(I)V:GetOnOrientationChanged_IHandler\n";
    private ArrayList refList;

    private native void n_onOrientationChanged(int i);

    static {
        Runtime.register("Microsoft.Maui.Devices.DeviceDisplayImplementation+Listener, Microsoft.Maui.Essentials", DeviceDisplayImplementation_Listener.class, __md_methods);
    }

    public DeviceDisplayImplementation_Listener(Context context) {
        super(context);
        if (getClass() == DeviceDisplayImplementation_Listener.class) {
            TypeManager.Activate("Microsoft.Maui.Devices.DeviceDisplayImplementation+Listener, Microsoft.Maui.Essentials", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // android.view.OrientationEventListener
    public void onOrientationChanged(int i) {
        n_onOrientationChanged(i);
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
