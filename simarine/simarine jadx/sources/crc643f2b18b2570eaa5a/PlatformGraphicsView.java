package crc643f2b18b2570eaa5a;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class PlatformGraphicsView extends View implements IGCUserPeer {
    public static final String __md_methods = "n_draw:(Landroid/graphics/Canvas;)V:GetDraw_Landroid_graphics_Canvas_Handler\nn_onSizeChanged:(IIII)V:GetOnSizeChanged_IIIIHandler\n";
    private ArrayList refList;

    private native void n_draw(Canvas canvas);

    private native void n_onSizeChanged(int i, int i2, int i3, int i4);

    static {
        Runtime.register("Microsoft.Maui.Graphics.Platform.PlatformGraphicsView, Microsoft.Maui.Graphics", PlatformGraphicsView.class, __md_methods);
    }

    public PlatformGraphicsView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (getClass() == PlatformGraphicsView.class) {
            TypeManager.Activate("Microsoft.Maui.Graphics.Platform.PlatformGraphicsView, Microsoft.Maui.Graphics", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    public PlatformGraphicsView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == PlatformGraphicsView.class) {
            TypeManager.Activate("Microsoft.Maui.Graphics.Platform.PlatformGraphicsView, Microsoft.Maui.Graphics", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public PlatformGraphicsView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == PlatformGraphicsView.class) {
            TypeManager.Activate("Microsoft.Maui.Graphics.Platform.PlatformGraphicsView, Microsoft.Maui.Graphics", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public PlatformGraphicsView(Context context) {
        super(context);
        if (getClass() == PlatformGraphicsView.class) {
            TypeManager.Activate("Microsoft.Maui.Graphics.Platform.PlatformGraphicsView, Microsoft.Maui.Graphics", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        n_draw(canvas);
    }

    @Override // android.view.View
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        n_onSizeChanged(i, i2, i3, i4);
    }

    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
