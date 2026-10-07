package crc64ba438d8f48cf7e75;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class IntermediateActivity extends Activity implements IGCUserPeer {
    public static final String __md_methods = "n_onCreate:(Landroid/os/Bundle;)V:GetOnCreate_Landroid_os_Bundle_Handler\nn_onSaveInstanceState:(Landroid/os/Bundle;)V:GetOnSaveInstanceState_Landroid_os_Bundle_Handler\nn_onActivityResult:(IILandroid/content/Intent;)V:GetOnActivityResult_IILandroid_content_Intent_Handler\n";
    private ArrayList refList;

    private native void n_onActivityResult(int i, int i2, Intent intent);

    private native void n_onCreate(Bundle bundle);

    private native void n_onSaveInstanceState(Bundle bundle);

    static {
        Runtime.register("Microsoft.Maui.ApplicationModel.IntermediateActivity, Microsoft.Maui.Essentials", IntermediateActivity.class, __md_methods);
    }

    public IntermediateActivity() {
        if (getClass() == IntermediateActivity.class) {
            TypeManager.Activate("Microsoft.Maui.ApplicationModel.IntermediateActivity, Microsoft.Maui.Essentials", "", this, new Object[0]);
        }
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        n_onCreate(bundle);
    }

    @Override // android.app.Activity
    public void onSaveInstanceState(Bundle bundle) {
        n_onSaveInstanceState(bundle);
    }

    @Override // android.app.Activity
    public void onActivityResult(int i, int i2, Intent intent) {
        n_onActivityResult(i, i2, intent);
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
