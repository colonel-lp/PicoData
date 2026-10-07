package crc6452ffdc5b34af3a0f;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiMaterialButton_MauiResizableDrawable extends LayerDrawable implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Platform.MauiMaterialButton+MauiResizableDrawable, Microsoft.Maui", MauiMaterialButton_MauiResizableDrawable.class, "");
    }

    public MauiMaterialButton_MauiResizableDrawable(Drawable[] drawableArr) {
        super(drawableArr);
        if (getClass() == MauiMaterialButton_MauiResizableDrawable.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiMaterialButton+MauiResizableDrawable, Microsoft.Maui", "Android.Graphics.Drawables.Drawable[], Mono.Android", this, new Object[]{drawableArr});
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
