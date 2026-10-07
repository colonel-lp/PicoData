package crc6452ffdc5b34af3a0f;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import crc643f2b18b2570eaa5a.PlatformGraphicsView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class PlatformTouchGraphicsView extends PlatformGraphicsView implements IGCUserPeer {
    public static final String __md_methods = "n_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\nn_onTouchEvent:(Landroid/view/MotionEvent;)Z:GetOnTouchEvent_Landroid_view_MotionEvent_Handler\nn_onHoverEvent:(Landroid/view/MotionEvent;)Z:GetOnHoverEvent_Landroid_view_MotionEvent_Handler\n";
    private ArrayList refList;

    private native boolean n_onHoverEvent(MotionEvent motionEvent);

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    private native boolean n_onTouchEvent(MotionEvent motionEvent);

    static {
        Runtime.register("Microsoft.Maui.Platform.PlatformTouchGraphicsView, Microsoft.Maui", PlatformTouchGraphicsView.class, __md_methods);
    }

    public PlatformTouchGraphicsView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (getClass() == PlatformTouchGraphicsView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.PlatformTouchGraphicsView, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    public PlatformTouchGraphicsView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == PlatformTouchGraphicsView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.PlatformTouchGraphicsView, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public PlatformTouchGraphicsView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == PlatformTouchGraphicsView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.PlatformTouchGraphicsView, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public PlatformTouchGraphicsView(Context context) {
        super(context);
        if (getClass() == PlatformTouchGraphicsView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.PlatformTouchGraphicsView, Microsoft.Maui", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        n_onLayout(z, i, i2, i3, i4);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return n_onTouchEvent(motionEvent);
    }

    @Override // android.view.View
    public boolean onHoverEvent(MotionEvent motionEvent) {
        return n_onHoverEvent(motionEvent);
    }

    @Override // crc643f2b18b2570eaa5a.PlatformGraphicsView, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc643f2b18b2570eaa5a.PlatformGraphicsView, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
