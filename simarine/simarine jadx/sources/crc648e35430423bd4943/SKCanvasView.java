package crc648e35430423bd4943;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class SKCanvasView extends View implements IGCUserPeer {
    public static final String __md_methods = "n_onDraw:(Landroid/graphics/Canvas;)V:GetOnDraw_Landroid_graphics_Canvas_Handler\nn_onSizeChanged:(IIII)V:GetOnSizeChanged_IIIIHandler\nn_onDetachedFromWindow:()V:GetOnDetachedFromWindowHandler\nn_onAttachedToWindow:()V:GetOnAttachedToWindowHandler\n";
    private ArrayList refList;

    private native void n_onAttachedToWindow();

    private native void n_onDetachedFromWindow();

    private native void n_onDraw(Canvas canvas);

    private native void n_onSizeChanged(int i, int i2, int i3, int i4);

    static {
        Runtime.register("SkiaSharp.Views.Android.SKCanvasView, SkiaSharp.Views.Android", SKCanvasView.class, __md_methods);
    }

    public SKCanvasView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (getClass() == SKCanvasView.class) {
            TypeManager.Activate("SkiaSharp.Views.Android.SKCanvasView, SkiaSharp.Views.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    public SKCanvasView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == SKCanvasView.class) {
            TypeManager.Activate("SkiaSharp.Views.Android.SKCanvasView, SkiaSharp.Views.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public SKCanvasView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == SKCanvasView.class) {
            TypeManager.Activate("SkiaSharp.Views.Android.SKCanvasView, SkiaSharp.Views.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public SKCanvasView(Context context) {
        super(context);
        if (getClass() == SKCanvasView.class) {
            TypeManager.Activate("SkiaSharp.Views.Android.SKCanvasView, SkiaSharp.Views.Android", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        n_onDraw(canvas);
    }

    @Override // android.view.View
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        n_onSizeChanged(i, i2, i3, i4);
    }

    @Override // android.view.View
    public void onDetachedFromWindow() {
        n_onDetachedFromWindow();
    }

    @Override // android.view.View
    public void onAttachedToWindow() {
        n_onAttachedToWindow();
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
