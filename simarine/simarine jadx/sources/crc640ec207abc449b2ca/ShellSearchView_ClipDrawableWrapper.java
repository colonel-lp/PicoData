package crc640ec207abc449b2ca;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.appcompat.graphics.drawable.DrawableWrapperCompat;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ShellSearchView_ClipDrawableWrapper extends DrawableWrapperCompat implements IGCUserPeer {
    public static final String __md_methods = "n_draw:(Landroid/graphics/Canvas;)V:GetDraw_Landroid_graphics_Canvas_Handler\n";
    private ArrayList refList;

    private native void n_draw(Canvas canvas);

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.Compatibility.ShellSearchView+ClipDrawableWrapper, Microsoft.Maui.Controls", ShellSearchView_ClipDrawableWrapper.class, "n_draw:(Landroid/graphics/Canvas;)V:GetDraw_Landroid_graphics_Canvas_Handler\n");
    }

    public ShellSearchView_ClipDrawableWrapper(Drawable drawable) {
        super(drawable);
        if (getClass() == ShellSearchView_ClipDrawableWrapper.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.ShellSearchView+ClipDrawableWrapper, Microsoft.Maui.Controls", "Android.Graphics.Drawables.Drawable, Mono.Android", this, new Object[]{drawable});
        }
    }

    @Override // androidx.appcompat.graphics.drawable.DrawableWrapperCompat, android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        n_draw(canvas);
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
