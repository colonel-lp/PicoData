package mono.com.google.android.material.checkbox;

import com.google.android.material.checkbox.MaterialCheckBox;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MaterialCheckBox_OnErrorChangedListenerImplementor implements IGCUserPeer, MaterialCheckBox.OnErrorChangedListener {
    public static final String __md_methods = "n_onErrorChanged:(Lcom/google/android/material/checkbox/MaterialCheckBox;Z)V:GetOnErrorChanged_Lcom_google_android_material_checkbox_MaterialCheckBox_ZHandler:Google.Android.Material.CheckBox.MaterialCheckBox/IOnErrorChangedListenerInvoker, Xamarin.Google.Android.Material\n";
    private ArrayList refList;

    private native void n_onErrorChanged(MaterialCheckBox materialCheckBox, boolean z);

    static {
        Runtime.register("Google.Android.Material.CheckBox.MaterialCheckBox+IOnErrorChangedListenerImplementor, Xamarin.Google.Android.Material", MaterialCheckBox_OnErrorChangedListenerImplementor.class, __md_methods);
    }

    public MaterialCheckBox_OnErrorChangedListenerImplementor() {
        if (getClass() == MaterialCheckBox_OnErrorChangedListenerImplementor.class) {
            TypeManager.Activate("Google.Android.Material.CheckBox.MaterialCheckBox+IOnErrorChangedListenerImplementor, Xamarin.Google.Android.Material", "", this, new Object[0]);
        }
    }

    @Override // com.google.android.material.checkbox.MaterialCheckBox.OnErrorChangedListener
    public void onErrorChanged(MaterialCheckBox materialCheckBox, boolean z) {
        n_onErrorChanged(materialCheckBox, z);
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
