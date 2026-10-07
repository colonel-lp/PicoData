package crc6477f0d89a9cfd64b1;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.microsoft.maui.MauiViewGroup;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public abstract class VisualElementRenderer_1 extends MauiViewGroup implements IGCUserPeer {
    public static final String __md_methods = "n_onInterceptTouchEvent:(Landroid/view/MotionEvent;)Z:GetOnInterceptTouchEvent_Landroid_view_MotionEvent_Handler\nn_dispatchTouchEvent:(Landroid/view/MotionEvent;)Z:GetDispatchTouchEvent_Landroid_view_MotionEvent_Handler\nn_onConfigurationChanged:(Landroid/content/res/Configuration;)V:GetOnConfigurationChanged_Landroid_content_res_Configuration_Handler\nn_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\nn_draw:(Landroid/graphics/Canvas;)V:GetDraw_Landroid_graphics_Canvas_Handler\n";
    private ArrayList refList;

    private native boolean n_dispatchTouchEvent(MotionEvent motionEvent);

    private native void n_draw(Canvas canvas);

    private native void n_onConfigurationChanged(Configuration configuration);

    private native boolean n_onInterceptTouchEvent(MotionEvent motionEvent);

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    static {
        Runtime.register("Microsoft.Maui.Controls.Compatibility.Platform.Android.VisualElementRenderer`1, Microsoft.Maui.Controls.Compatibility", VisualElementRenderer_1.class, __md_methods);
    }

    public VisualElementRenderer_1(Context context) {
        super(context);
        if (getClass() == VisualElementRenderer_1.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Compatibility.Platform.Android.VisualElementRenderer`1, Microsoft.Maui.Controls.Compatibility", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public VisualElementRenderer_1(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == VisualElementRenderer_1.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Compatibility.Platform.Android.VisualElementRenderer`1, Microsoft.Maui.Controls.Compatibility", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public VisualElementRenderer_1(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == VisualElementRenderer_1.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Compatibility.Platform.Android.VisualElementRenderer`1, Microsoft.Maui.Controls.Compatibility", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // com.microsoft.maui.MauiViewGroup, android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return n_onInterceptTouchEvent(motionEvent);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return n_dispatchTouchEvent(motionEvent);
    }

    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        n_onConfigurationChanged(configuration);
    }

    @Override // com.microsoft.maui.MauiViewGroup, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        n_onLayout(z, i, i2, i3, i4);
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        n_draw(canvas);
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
