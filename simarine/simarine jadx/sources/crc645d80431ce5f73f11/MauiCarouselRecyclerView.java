package crc645d80431ce5f73f11;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiCarouselRecyclerView extends MauiRecyclerView_3 implements IGCUserPeer {
    public static final String __md_methods = "n_onInterceptTouchEvent:(Landroid/view/MotionEvent;)Z:GetOnInterceptTouchEvent_Landroid_view_MotionEvent_Handler\nn_onTouchEvent:(Landroid/view/MotionEvent;)Z:GetOnTouchEvent_Landroid_view_MotionEvent_Handler\nn_onAttachedToWindow:()V:GetOnAttachedToWindowHandler\nn_onDetachedFromWindow:()V:GetOnDetachedFromWindowHandler\nn_onMeasure:(II)V:GetOnMeasure_IIHandler\n";
    private ArrayList refList;

    private native void n_onAttachedToWindow();

    private native void n_onDetachedFromWindow();

    private native boolean n_onInterceptTouchEvent(MotionEvent motionEvent);

    private native void n_onMeasure(int i, int i2);

    private native boolean n_onTouchEvent(MotionEvent motionEvent);

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Items.MauiCarouselRecyclerView, Microsoft.Maui.Controls", MauiCarouselRecyclerView.class, __md_methods);
    }

    public MauiCarouselRecyclerView(Context context) {
        super(context);
        if (getClass() == MauiCarouselRecyclerView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.MauiCarouselRecyclerView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public MauiCarouselRecyclerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == MauiCarouselRecyclerView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.MauiCarouselRecyclerView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public MauiCarouselRecyclerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == MauiCarouselRecyclerView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.MauiCarouselRecyclerView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView, android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return n_onInterceptTouchEvent(motionEvent);
    }

    @Override // androidx.recyclerview.widget.RecyclerView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return n_onTouchEvent(motionEvent);
    }

    @Override // androidx.recyclerview.widget.RecyclerView, android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        n_onAttachedToWindow();
    }

    @Override // androidx.recyclerview.widget.RecyclerView, android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        n_onDetachedFromWindow();
    }

    @Override // androidx.recyclerview.widget.RecyclerView, android.view.View
    public void onMeasure(int i, int i2) {
        n_onMeasure(i, i2);
    }

    @Override // crc645d80431ce5f73f11.MauiRecyclerView_3, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc645d80431ce5f73f11.MauiRecyclerView_3, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
