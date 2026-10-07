package crc6477f0d89a9cfd64b1;

import android.util.LruCache;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ImageCache_FormsLruCache extends LruCache implements IGCUserPeer {
    public static final String __md_methods = "n_sizeOf:(Ljava/lang/Object;Ljava/lang/Object;)I:GetSizeOf_Ljava_lang_Object_Ljava_lang_Object_Handler\n";
    private ArrayList refList;

    private native int n_sizeOf(Object obj, Object obj2);

    static {
        Runtime.register("Microsoft.Maui.Controls.Compatibility.Platform.Android.ImageCache+FormsLruCache, Microsoft.Maui.Controls.Compatibility", ImageCache_FormsLruCache.class, __md_methods);
    }

    public ImageCache_FormsLruCache(int i) {
        super(i);
        if (getClass() == ImageCache_FormsLruCache.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Compatibility.Platform.Android.ImageCache+FormsLruCache, Microsoft.Maui.Controls.Compatibility", "System.Int32, System.Private.CoreLib", this, new Object[]{Integer.valueOf(i)});
        }
    }

    @Override // android.util.LruCache
    public int sizeOf(Object obj, Object obj2) {
        return n_sizeOf(obj, obj2);
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
