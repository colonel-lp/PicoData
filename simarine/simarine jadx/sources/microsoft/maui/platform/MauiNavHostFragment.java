package microsoft.maui.platform;

import androidx.navigation.fragment.NavHostFragment;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiNavHostFragment extends NavHostFragment implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Platform.MauiNavHostFragment, Microsoft.Maui", MauiNavHostFragment.class, "");
    }

    public MauiNavHostFragment() {
        if (getClass() == MauiNavHostFragment.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiNavHostFragment, Microsoft.Maui", "", this, new Object[0]);
        }
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
