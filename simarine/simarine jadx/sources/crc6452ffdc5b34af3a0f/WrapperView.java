package crc6452ffdc5b34af3a0f;

import android.content.Context;
import android.graphics.Path;
import android.view.MotionEvent;
import com.microsoft.maui.PlatformWrapperView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class WrapperView extends PlatformWrapperView implements IGCUserPeer {
    public static final String __md_methods = "n_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\nn_dispatchTouchEvent:(Landroid/view/MotionEvent;)Z:GetDispatchTouchEvent_Landroid_view_MotionEvent_Handler\nn_getClipPath:(II)Landroid/graphics/Path;:GetGetClipPath_IIHandler\nn_getVisibility:()I:GetGetVisibilityHandler\nn_setVisibility:(I)V:GetSetVisibility_IHandler\n";
    private ArrayList refList;

    private native boolean n_dispatchTouchEvent(MotionEvent motionEvent);

    private native Path n_getClipPath(int i, int i2);

    private native int n_getVisibility();

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    private native void n_setVisibility(int i);

    static {
        Runtime.register("Microsoft.Maui.Platform.WrapperView, Microsoft.Maui", WrapperView.class, __md_methods);
    }

    public WrapperView(Context context) {
        super(context);
        if (getClass() == WrapperView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.WrapperView, Microsoft.Maui", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // com.microsoft.maui.PlatformWrapperView, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        n_onLayout(z, i, i2, i3, i4);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return n_dispatchTouchEvent(motionEvent);
    }

    @Override // com.microsoft.maui.PlatformContentViewGroup
    public Path getClipPath(int i, int i2) {
        return n_getClipPath(i, i2);
    }

    @Override // android.view.View
    public int getVisibility() {
        return n_getVisibility();
    }

    @Override // android.view.View
    public void setVisibility(int i) {
        n_setVisibility(i);
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
