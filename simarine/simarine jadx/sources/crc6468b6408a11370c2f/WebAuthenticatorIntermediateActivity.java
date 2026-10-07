package crc6468b6408a11370c2f;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class WebAuthenticatorIntermediateActivity extends Activity implements IGCUserPeer {
    public static final String __md_methods = "n_onCreate:(Landroid/os/Bundle;)V:GetOnCreate_Landroid_os_Bundle_Handler\nn_onResume:()V:GetOnResumeHandler\nn_onNewIntent:(Landroid/content/Intent;)V:GetOnNewIntent_Landroid_content_Intent_Handler\nn_onSaveInstanceState:(Landroid/os/Bundle;)V:GetOnSaveInstanceState_Landroid_os_Bundle_Handler\n";
    private ArrayList refList;

    private native void n_onCreate(Bundle bundle);

    private native void n_onNewIntent(Intent intent);

    private native void n_onResume();

    private native void n_onSaveInstanceState(Bundle bundle);

    static {
        Runtime.register("Microsoft.Maui.Authentication.WebAuthenticatorIntermediateActivity, Microsoft.Maui.Essentials", WebAuthenticatorIntermediateActivity.class, __md_methods);
    }

    public WebAuthenticatorIntermediateActivity() {
        if (getClass() == WebAuthenticatorIntermediateActivity.class) {
            TypeManager.Activate("Microsoft.Maui.Authentication.WebAuthenticatorIntermediateActivity, Microsoft.Maui.Essentials", "", this, new Object[0]);
        }
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        n_onCreate(bundle);
    }

    @Override // android.app.Activity
    public void onResume() {
        n_onResume();
    }

    @Override // android.app.Activity
    public void onNewIntent(Intent intent) {
        n_onNewIntent(intent);
    }

    @Override // android.app.Activity
    public void onSaveInstanceState(Bundle bundle) {
        n_onSaveInstanceState(bundle);
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
