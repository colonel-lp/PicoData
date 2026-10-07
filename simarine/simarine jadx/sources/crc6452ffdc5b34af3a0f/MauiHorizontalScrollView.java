package crc6452ffdc5b34af3a0f;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiHorizontalScrollView extends HorizontalScrollView implements IGCUserPeer {
    public static final String __md_methods = "n_draw:(Landroid/graphics/Canvas;)V:GetDraw_Landroid_graphics_Canvas_Handler\nn_onInterceptTouchEvent:(Landroid/view/MotionEvent;)Z:GetOnInterceptTouchEvent_Landroid_view_MotionEvent_Handler\nn_onTouchEvent:(Landroid/view/MotionEvent;)Z:GetOnTouchEvent_Landroid_view_MotionEvent_Handler\nn_isHorizontalScrollBarEnabled:()Z:GetIsHorizontalScrollBarEnabledHandler\nn_setHorizontalScrollBarEnabled:(Z)V:GetSetHorizontalScrollBarEnabled_ZHandler\nn_onScrollChanged:(IIII)V:GetOnScrollChanged_IIIIHandler\n";
    private ArrayList refList;

    private native void n_draw(Canvas canvas);

    private native boolean n_isHorizontalScrollBarEnabled();

    private native boolean n_onInterceptTouchEvent(MotionEvent motionEvent);

    private native void n_onScrollChanged(int i, int i2, int i3, int i4);

    private native boolean n_onTouchEvent(MotionEvent motionEvent);

    private native void n_setHorizontalScrollBarEnabled(boolean z);

    static {
        Runtime.register("Microsoft.Maui.Platform.MauiHorizontalScrollView, Microsoft.Maui", MauiHorizontalScrollView.class, __md_methods);
    }

    public MauiHorizontalScrollView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (getClass() == MauiHorizontalScrollView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiHorizontalScrollView, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    public MauiHorizontalScrollView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == MauiHorizontalScrollView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiHorizontalScrollView, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public MauiHorizontalScrollView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == MauiHorizontalScrollView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiHorizontalScrollView, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public MauiHorizontalScrollView(Context context) {
        super(context);
        if (getClass() == MauiHorizontalScrollView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiHorizontalScrollView, Microsoft.Maui", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // android.widget.HorizontalScrollView, android.view.View
    public void draw(Canvas canvas) {
        n_draw(canvas);
    }

    @Override // android.widget.HorizontalScrollView, android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return n_onInterceptTouchEvent(motionEvent);
    }

    @Override // android.widget.HorizontalScrollView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return n_onTouchEvent(motionEvent);
    }

    @Override // android.view.View
    public boolean isHorizontalScrollBarEnabled() {
        return n_isHorizontalScrollBarEnabled();
    }

    @Override // android.view.View
    public void setHorizontalScrollBarEnabled(boolean z) {
        n_setHorizontalScrollBarEnabled(z);
    }

    @Override // android.view.View
    public void onScrollChanged(int i, int i2, int i3, int i4) {
        n_onScrollChanged(i, i2, i3, i4);
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
