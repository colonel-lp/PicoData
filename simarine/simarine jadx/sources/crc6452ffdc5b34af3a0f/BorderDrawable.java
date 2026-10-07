package crc6452ffdc5b34af3a0f;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.shapes.Shape;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class BorderDrawable extends PaintDrawable implements IGCUserPeer {
    public static final String __md_methods = "n_onBoundsChange:(Landroid/graphics/Rect;)V:GetOnBoundsChange_Landroid_graphics_Rect_Handler\nn_onDraw:(Landroid/graphics/drawable/shapes/Shape;Landroid/graphics/Canvas;Landroid/graphics/Paint;)V:GetOnDraw_Landroid_graphics_drawable_shapes_Shape_Landroid_graphics_Canvas_Landroid_graphics_Paint_Handler\n";
    private ArrayList refList;

    private native void n_onBoundsChange(Rect rect);

    private native void n_onDraw(Shape shape, Canvas canvas, Paint paint);

    static {
        Runtime.register("Microsoft.Maui.Platform.BorderDrawable, Microsoft.Maui", BorderDrawable.class, __md_methods);
    }

    public BorderDrawable() {
        if (getClass() == BorderDrawable.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.BorderDrawable, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    public BorderDrawable(Context context) {
        if (getClass() == BorderDrawable.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.BorderDrawable, Microsoft.Maui", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // android.graphics.drawable.ShapeDrawable, android.graphics.drawable.Drawable
    public void onBoundsChange(Rect rect) {
        n_onBoundsChange(rect);
    }

    @Override // android.graphics.drawable.ShapeDrawable
    public void onDraw(Shape shape, Canvas canvas, Paint paint) {
        n_onDraw(shape, canvas, paint);
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
