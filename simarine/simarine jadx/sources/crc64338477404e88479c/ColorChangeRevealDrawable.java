package crc64338477404e88479c;

import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ColorChangeRevealDrawable extends AnimationDrawable implements IGCUserPeer {
    public static final String __md_methods = "n_draw:(Landroid/graphics/Canvas;)V:GetDraw_Landroid_graphics_Canvas_Handler\n";
    private ArrayList refList;

    private native void n_draw(Canvas canvas);

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.ColorChangeRevealDrawable, Microsoft.Maui.Controls", ColorChangeRevealDrawable.class, "n_draw:(Landroid/graphics/Canvas;)V:GetDraw_Landroid_graphics_Canvas_Handler\n");
    }

    public ColorChangeRevealDrawable() {
        if (getClass() == ColorChangeRevealDrawable.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.ColorChangeRevealDrawable, Microsoft.Maui.Controls", "", this, new Object[0]);
        }
    }

    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.Drawable
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
