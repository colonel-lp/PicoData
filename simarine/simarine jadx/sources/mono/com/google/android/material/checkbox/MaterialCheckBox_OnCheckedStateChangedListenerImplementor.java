package mono.com.google.android.material.checkbox;

import com.google.android.material.checkbox.MaterialCheckBox;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MaterialCheckBox_OnCheckedStateChangedListenerImplementor implements IGCUserPeer, MaterialCheckBox.OnCheckedStateChangedListener {
    public static final String __md_methods = "n_onCheckedStateChangedListener:(Lcom/google/android/material/checkbox/MaterialCheckBox;I)V:GetOnCheckedStateChangedListener_Lcom_google_android_material_checkbox_MaterialCheckBox_IHandler:Google.Android.Material.CheckBox.MaterialCheckBox/IOnCheckedStateChangedListenerInvoker, Xamarin.Google.Android.Material\n";
    private ArrayList refList;

    private native void n_onCheckedStateChangedListener(MaterialCheckBox materialCheckBox, int i);

    static {
        Runtime.register("Google.Android.Material.CheckBox.MaterialCheckBox+IOnCheckedStateChangedListenerImplementor, Xamarin.Google.Android.Material", MaterialCheckBox_OnCheckedStateChangedListenerImplementor.class, __md_methods);
    }

    public MaterialCheckBox_OnCheckedStateChangedListenerImplementor() {
        if (getClass() == MaterialCheckBox_OnCheckedStateChangedListenerImplementor.class) {
            TypeManager.Activate("Google.Android.Material.CheckBox.MaterialCheckBox+IOnCheckedStateChangedListenerImplementor, Xamarin.Google.Android.Material", "", this, new Object[0]);
        }
    }

    @Override // com.google.android.material.checkbox.MaterialCheckBox.OnCheckedStateChangedListener
    public void onCheckedStateChangedListener(MaterialCheckBox materialCheckBox, int i) {
        n_onCheckedStateChangedListener(materialCheckBox, i);
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
