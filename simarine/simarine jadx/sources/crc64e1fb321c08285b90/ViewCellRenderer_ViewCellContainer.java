package crc64e1fb321c08285b90;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ViewCellRenderer_ViewCellContainer extends ViewGroup implements IGCUserPeer {
    public static final String __md_methods = "n_onInterceptTouchEvent:(Landroid/view/MotionEvent;)Z:GetOnInterceptTouchEvent_Landroid_view_MotionEvent_Handler\nn_dispatchTouchEvent:(Landroid/view/MotionEvent;)Z:GetDispatchTouchEvent_Landroid_view_MotionEvent_Handler\nn_addView:(Landroid/view/View;)V:GetAddView_Landroid_view_View_Handler\nn_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\nn_onMeasure:(II)V:GetOnMeasure_IIHandler\n";
    private ArrayList refList;

    private native void n_addView(View view);

    private native boolean n_dispatchTouchEvent(MotionEvent motionEvent);

    private native boolean n_onInterceptTouchEvent(MotionEvent motionEvent);

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    private native void n_onMeasure(int i, int i2);

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Compatibility.ViewCellRenderer+ViewCellContainer, Microsoft.Maui.Controls", ViewCellRenderer_ViewCellContainer.class, __md_methods);
    }

    public ViewCellRenderer_ViewCellContainer(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (getClass() == ViewCellRenderer_ViewCellContainer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.ViewCellRenderer+ViewCellContainer, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    public ViewCellRenderer_ViewCellContainer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == ViewCellRenderer_ViewCellContainer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.ViewCellRenderer+ViewCellContainer, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public ViewCellRenderer_ViewCellContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == ViewCellRenderer_ViewCellContainer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.ViewCellRenderer+ViewCellContainer, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public ViewCellRenderer_ViewCellContainer(Context context) {
        super(context);
        if (getClass() == ViewCellRenderer_ViewCellContainer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.ViewCellRenderer+ViewCellContainer, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return n_onInterceptTouchEvent(motionEvent);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return n_dispatchTouchEvent(motionEvent);
    }

    @Override // android.view.ViewGroup
    public void addView(View view) {
        n_addView(view);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        n_onLayout(z, i, i2, i3, i4);
    }

    @Override // android.view.View
    public void onMeasure(int i, int i2) {
        n_onMeasure(i, i2);
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
