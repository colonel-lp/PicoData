package crc6477f0d89a9cfd64b1;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class Platform_DefaultRenderer extends VisualElementRenderer_1 implements IGCUserPeer {
    public static final String __md_methods = "n_onTouchEvent:(Landroid/view/MotionEvent;)Z:GetOnTouchEvent_Landroid_view_MotionEvent_Handler\nn_dispatchTouchEvent:(Landroid/view/MotionEvent;)Z:GetDispatchTouchEvent_Landroid_view_MotionEvent_Handler\nn_setOnTouchListener:(Landroid/view/View$OnTouchListener;)V:GetSetOnTouchListener_Landroid_view_View_OnTouchListener_Handler\nn_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n";
    private ArrayList refList;

    private native boolean n_dispatchTouchEvent(MotionEvent motionEvent);

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    private native boolean n_onTouchEvent(MotionEvent motionEvent);

    private native void n_setOnTouchListener(View.OnTouchListener onTouchListener);

    static {
        Runtime.register("Microsoft.Maui.Controls.Compatibility.Platform.Android.Platform+DefaultRenderer, Microsoft.Maui.Controls.Compatibility", Platform_DefaultRenderer.class, __md_methods);
    }

    public Platform_DefaultRenderer(Context context) {
        super(context);
        if (getClass() == Platform_DefaultRenderer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Compatibility.Platform.Android.Platform+DefaultRenderer, Microsoft.Maui.Controls.Compatibility", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public Platform_DefaultRenderer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == Platform_DefaultRenderer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Compatibility.Platform.Android.Platform+DefaultRenderer, Microsoft.Maui.Controls.Compatibility", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public Platform_DefaultRenderer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == Platform_DefaultRenderer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Compatibility.Platform.Android.Platform+DefaultRenderer, Microsoft.Maui.Controls.Compatibility", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // com.microsoft.maui.MauiViewGroup, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return n_onTouchEvent(motionEvent);
    }

    @Override // crc6477f0d89a9cfd64b1.VisualElementRenderer_1, android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return n_dispatchTouchEvent(motionEvent);
    }

    @Override // android.view.View
    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        n_setOnTouchListener(onTouchListener);
    }

    @Override // crc6477f0d89a9cfd64b1.VisualElementRenderer_1, com.microsoft.maui.MauiViewGroup, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        n_onLayout(z, i, i2, i3, i4);
    }

    @Override // crc6477f0d89a9cfd64b1.VisualElementRenderer_1, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc6477f0d89a9cfd64b1.VisualElementRenderer_1, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
