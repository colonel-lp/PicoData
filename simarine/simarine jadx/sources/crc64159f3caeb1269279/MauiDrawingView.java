package crc64159f3caeb1269279;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import crc6452ffdc5b34af3a0f.PlatformTouchGraphicsView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiDrawingView extends PlatformTouchGraphicsView implements IGCUserPeer {
    public static final String __md_methods = "n_onTouchEvent:(Landroid/view/MotionEvent;)Z:GetOnTouchEvent_Landroid_view_MotionEvent_Handler\n";
    private ArrayList refList;

    private native boolean n_onTouchEvent(MotionEvent motionEvent);

    static {
        Runtime.register("CommunityToolkit.Maui.Core.Views.MauiDrawingView, CommunityToolkit.Maui.Core", MauiDrawingView.class, "n_onTouchEvent:(Landroid/view/MotionEvent;)Z:GetOnTouchEvent_Landroid_view_MotionEvent_Handler\n");
    }

    public MauiDrawingView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (getClass() == MauiDrawingView.class) {
            TypeManager.Activate("CommunityToolkit.Maui.Core.Views.MauiDrawingView, CommunityToolkit.Maui.Core", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    public MauiDrawingView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == MauiDrawingView.class) {
            TypeManager.Activate("CommunityToolkit.Maui.Core.Views.MauiDrawingView, CommunityToolkit.Maui.Core", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public MauiDrawingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == MauiDrawingView.class) {
            TypeManager.Activate("CommunityToolkit.Maui.Core.Views.MauiDrawingView, CommunityToolkit.Maui.Core", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public MauiDrawingView(Context context) {
        super(context);
        if (getClass() == MauiDrawingView.class) {
            TypeManager.Activate("CommunityToolkit.Maui.Core.Views.MauiDrawingView, CommunityToolkit.Maui.Core", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // crc6452ffdc5b34af3a0f.PlatformTouchGraphicsView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return n_onTouchEvent(motionEvent);
    }

    @Override // crc6452ffdc5b34af3a0f.PlatformTouchGraphicsView, crc643f2b18b2570eaa5a.PlatformGraphicsView, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc6452ffdc5b34af3a0f.PlatformTouchGraphicsView, crc643f2b18b2570eaa5a.PlatformGraphicsView, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
