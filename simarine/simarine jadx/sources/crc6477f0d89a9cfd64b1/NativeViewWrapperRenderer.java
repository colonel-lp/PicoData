package crc6477f0d89a9cfd64b1;

import android.content.Context;
import android.util.AttributeSet;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class NativeViewWrapperRenderer extends ViewRenderer_2 implements IGCUserPeer {
    public static final String __md_methods = "n_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\nn_onMeasure:(II)V:GetOnMeasure_IIHandler\n";
    private ArrayList refList;

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    private native void n_onMeasure(int i, int i2);

    static {
        Runtime.register("Microsoft.Maui.Controls.Compatibility.Platform.Android.NativeViewWrapperRenderer, Microsoft.Maui.Controls.Compatibility", NativeViewWrapperRenderer.class, "n_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\nn_onMeasure:(II)V:GetOnMeasure_IIHandler\n");
    }

    public NativeViewWrapperRenderer(Context context) {
        super(context);
        if (getClass() == NativeViewWrapperRenderer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Compatibility.Platform.Android.NativeViewWrapperRenderer, Microsoft.Maui.Controls.Compatibility", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public NativeViewWrapperRenderer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == NativeViewWrapperRenderer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Compatibility.Platform.Android.NativeViewWrapperRenderer, Microsoft.Maui.Controls.Compatibility", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public NativeViewWrapperRenderer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == NativeViewWrapperRenderer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Compatibility.Platform.Android.NativeViewWrapperRenderer, Microsoft.Maui.Controls.Compatibility", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // crc6477f0d89a9cfd64b1.ViewRenderer_2, crc6477f0d89a9cfd64b1.VisualElementRenderer_1, com.microsoft.maui.MauiViewGroup, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        n_onLayout(z, i, i2, i3, i4);
    }

    @Override // android.view.View
    public void onMeasure(int i, int i2) {
        n_onMeasure(i, i2);
    }

    @Override // crc6477f0d89a9cfd64b1.ViewRenderer_2, crc6477f0d89a9cfd64b1.VisualElementRenderer_1, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc6477f0d89a9cfd64b1.ViewRenderer_2, crc6477f0d89a9cfd64b1.VisualElementRenderer_1, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
