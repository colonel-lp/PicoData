package crc640ec207abc449b2ca;

import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public abstract class ShellItemRendererBase extends Fragment implements IGCUserPeer {
    public static final String __md_methods = "n_onDestroy:()V:GetOnDestroyHandler\n";
    private ArrayList refList;

    private native void n_onDestroy();

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.Compatibility.ShellItemRendererBase, Microsoft.Maui.Controls", ShellItemRendererBase.class, __md_methods);
    }

    public ShellItemRendererBase() {
        if (getClass() == ShellItemRendererBase.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.ShellItemRendererBase, Microsoft.Maui.Controls", "", this, new Object[0]);
        }
    }

    public ShellItemRendererBase(int i) {
        super(i);
        if (getClass() == ShellItemRendererBase.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.ShellItemRendererBase, Microsoft.Maui.Controls", "System.Int32, System.Private.CoreLib", this, new Object[]{Integer.valueOf(i)});
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        n_onDestroy();
    }

    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
