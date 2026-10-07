package microsoft.maui.essentials;

import androidx.core.content.FileProvider;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class fileProvider extends FileProvider implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Storage.FileProvider, Microsoft.Maui.Essentials", fileProvider.class, "");
    }

    public fileProvider() {
        if (getClass() == fileProvider.class) {
            TypeManager.Activate("Microsoft.Maui.Storage.FileProvider, Microsoft.Maui.Essentials", "", this, new Object[0]);
        }
    }

    public fileProvider(int i) {
        super(i);
        if (getClass() == fileProvider.class) {
            TypeManager.Activate("Microsoft.Maui.Storage.FileProvider, Microsoft.Maui.Essentials", "System.Int32, System.Private.CoreLib", this, new Object[]{Integer.valueOf(i)});
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
