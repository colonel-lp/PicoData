package crc6488302ad6e9e4df1a;

import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ImageLoaderResultCallback extends ImageLoaderCallbackBase_1 implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.ImageLoaderResultCallback, Microsoft.Maui", ImageLoaderResultCallback.class, "");
    }

    public ImageLoaderResultCallback() {
        if (getClass() == ImageLoaderResultCallback.class) {
            TypeManager.Activate("Microsoft.Maui.ImageLoaderResultCallback, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    @Override // crc6488302ad6e9e4df1a.ImageLoaderCallbackBase_1, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc6488302ad6e9e4df1a.ImageLoaderCallbackBase_1, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
