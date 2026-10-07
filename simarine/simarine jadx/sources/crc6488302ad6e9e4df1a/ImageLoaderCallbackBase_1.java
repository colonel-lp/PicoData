package crc6488302ad6e9e4df1a;

import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public abstract class ImageLoaderCallbackBase_1 implements IGCUserPeer, com.microsoft.maui.ImageLoaderCallback {
    public static final String __md_methods = "n_onComplete:(Ljava/lang/Boolean;Landroid/graphics/drawable/Drawable;Ljava/lang/Runnable;)V:GetOnComplete_Ljava_lang_Boolean_Landroid_graphics_drawable_Drawable_Ljava_lang_Runnable_Handler:Microsoft.Maui.IImageLoaderCallbackInvoker, Microsoft.Maui\n";
    private ArrayList refList;

    private native void n_onComplete(Boolean bool, Drawable drawable, Runnable runnable);

    static {
        Runtime.register("Microsoft.Maui.ImageLoaderCallbackBase`1, Microsoft.Maui", ImageLoaderCallbackBase_1.class, __md_methods);
    }

    public ImageLoaderCallbackBase_1() {
        if (getClass() == ImageLoaderCallbackBase_1.class) {
            TypeManager.Activate("Microsoft.Maui.ImageLoaderCallbackBase`1, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    @Override // com.microsoft.maui.ImageLoaderCallback
    public void onComplete(Boolean bool, Drawable drawable, Runnable runnable) {
        n_onComplete(bool, drawable, runnable);
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
