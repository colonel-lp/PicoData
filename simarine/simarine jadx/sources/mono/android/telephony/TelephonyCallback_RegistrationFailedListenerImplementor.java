package mono.android.telephony;

import android.telephony.CellIdentity;
import android.telephony.TelephonyCallback;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TelephonyCallback_RegistrationFailedListenerImplementor implements IGCUserPeer, TelephonyCallback.RegistrationFailedListener {
    public static final String __md_methods = "n_onRegistrationFailed:(Landroid/telephony/CellIdentity;Ljava/lang/String;III)V:GetOnRegistrationFailed_Landroid_telephony_CellIdentity_Ljava_lang_String_IIIHandler:Android.Telephony.TelephonyCallback/IRegistrationFailedListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onRegistrationFailed(CellIdentity cellIdentity, String str, int i, int i2, int i3);

    static {
        Runtime.register("Android.Telephony.TelephonyCallback+IRegistrationFailedListenerImplementor, Mono.Android", TelephonyCallback_RegistrationFailedListenerImplementor.class, __md_methods);
    }

    public TelephonyCallback_RegistrationFailedListenerImplementor() {
        if (getClass() == TelephonyCallback_RegistrationFailedListenerImplementor.class) {
            TypeManager.Activate("Android.Telephony.TelephonyCallback+IRegistrationFailedListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.telephony.TelephonyCallback.RegistrationFailedListener
    public void onRegistrationFailed(CellIdentity cellIdentity, String str, int i, int i2, int i3) {
        n_onRegistrationFailed(cellIdentity, str, i, i2, i3);
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
