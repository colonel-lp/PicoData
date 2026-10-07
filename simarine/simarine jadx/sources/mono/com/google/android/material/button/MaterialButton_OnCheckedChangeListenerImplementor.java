package mono.com.google.android.material.button;

import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MaterialButton_OnCheckedChangeListenerImplementor implements IGCUserPeer, MaterialButton.OnCheckedChangeListener {
    public static final String __md_methods = "n_onCheckedChanged:(Lcom/google/android/material/button/MaterialButton;Z)V:GetOnCheckedChanged_Lcom_google_android_material_button_MaterialButton_ZHandler:Google.Android.Material.Button.MaterialButton/IOnCheckedChangeListenerInvoker, Xamarin.Google.Android.Material\n";
    private ArrayList refList;

    private native void n_onCheckedChanged(MaterialButton materialButton, boolean z);

    static {
        Runtime.register("Google.Android.Material.Button.MaterialButton+IOnCheckedChangeListenerImplementor, Xamarin.Google.Android.Material", MaterialButton_OnCheckedChangeListenerImplementor.class, __md_methods);
    }

    public MaterialButton_OnCheckedChangeListenerImplementor() {
        if (getClass() == MaterialButton_OnCheckedChangeListenerImplementor.class) {
            TypeManager.Activate("Google.Android.Material.Button.MaterialButton+IOnCheckedChangeListenerImplementor, Xamarin.Google.Android.Material", "", this, new Object[0]);
        }
    }

    @Override // com.google.android.material.button.MaterialButton.OnCheckedChangeListener
    public void onCheckedChanged(MaterialButton materialButton, boolean z) {
        n_onCheckedChanged(materialButton, z);
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
