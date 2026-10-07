package crc64b5e713d400f589b7;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.shapes.Shape;
import com.microsoft.maui.PlatformShadowDrawable;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiDrawable extends PaintDrawable implements IGCUserPeer, PlatformShadowDrawable {
    public static final String __md_methods = "n_onBoundsChange:(Landroid/graphics/Rect;)V:GetOnBoundsChange_Landroid_graphics_Rect_Handler\nn_onDraw:(Landroid/graphics/drawable/shapes/Shape;Landroid/graphics/Canvas;Landroid/graphics/Paint;)V:GetOnDraw_Landroid_graphics_drawable_shapes_Shape_Landroid_graphics_Canvas_Landroid_graphics_Paint_Handler\nn_canDrawShadow:()Z:GetCanDrawShadowHandler:Microsoft.Maui.IPlatformShadowDrawableInvoker, Microsoft.Maui\nn_drawShadow:(Landroid/graphics/Canvas;Landroid/graphics/Paint;Landroid/graphics/Path;)V:GetDrawShadow_Landroid_graphics_Canvas_Landroid_graphics_Paint_Landroid_graphics_Path_Handler:Microsoft.Maui.IPlatformShadowDrawableInvoker, Microsoft.Maui\n";
    private ArrayList refList;

    private native boolean n_canDrawShadow();

    private native void n_drawShadow(Canvas canvas, Paint paint, Path path);

    private native void n_onBoundsChange(Rect rect);

    private native void n_onDraw(Shape shape, Canvas canvas, Paint paint);

    static {
        Runtime.register("Microsoft.Maui.Graphics.MauiDrawable, Microsoft.Maui", MauiDrawable.class, __md_methods);
    }

    public MauiDrawable() {
        if (getClass() == MauiDrawable.class) {
            TypeManager.Activate("Microsoft.Maui.Graphics.MauiDrawable, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    public MauiDrawable(Context context) {
        if (getClass() == MauiDrawable.class) {
            TypeManager.Activate("Microsoft.Maui.Graphics.MauiDrawable, Microsoft.Maui", "Android.Content.Context, Mono.Android", this, new Object[]{context});
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

    @Override // com.microsoft.maui.PlatformShadowDrawable
    public boolean canDrawShadow() {
        return n_canDrawShadow();
    }

    @Override // com.microsoft.maui.PlatformShadowDrawable
    public void drawShadow(Canvas canvas, Paint paint, Path path) {
        n_drawShadow(canvas, paint, path);
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
