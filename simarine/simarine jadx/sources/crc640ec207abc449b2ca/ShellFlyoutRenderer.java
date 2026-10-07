package crc640ec207abc449b2ca;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.drawerlayout.widget.DrawerLayout;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ShellFlyoutRenderer extends DrawerLayout implements IGCUserPeer {
    public static final String __md_methods = "n_onInterceptTouchEvent:(Landroid/view/MotionEvent;)Z:GetOnInterceptTouchEvent_Landroid_view_MotionEvent_Handler\nn_drawChild:(Landroid/graphics/Canvas;Landroid/view/View;J)Z:GetDrawChild_Landroid_graphics_Canvas_Landroid_view_View_JHandler\n";
    private ArrayList refList;

    private native boolean n_drawChild(Canvas canvas, View view, long j);

    private native boolean n_onInterceptTouchEvent(MotionEvent motionEvent);

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.Compatibility.ShellFlyoutRenderer, Microsoft.Maui.Controls", ShellFlyoutRenderer.class, __md_methods);
    }

    public ShellFlyoutRenderer(Context context) {
        super(context);
        if (getClass() == ShellFlyoutRenderer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.ShellFlyoutRenderer, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public ShellFlyoutRenderer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == ShellFlyoutRenderer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.ShellFlyoutRenderer, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public ShellFlyoutRenderer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == ShellFlyoutRenderer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.ShellFlyoutRenderer, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // androidx.drawerlayout.widget.DrawerLayout, android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return n_onInterceptTouchEvent(motionEvent);
    }

    @Override // androidx.drawerlayout.widget.DrawerLayout, android.view.ViewGroup
    public boolean drawChild(Canvas canvas, View view, long j) {
        return n_drawChild(canvas, view, j);
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
