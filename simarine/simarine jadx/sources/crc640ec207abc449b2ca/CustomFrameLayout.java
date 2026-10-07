package crc640ec207abc449b2ca;

import android.content.Context;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class CustomFrameLayout extends FrameLayout implements IGCUserPeer {
    public static final String __md_methods = "n_onApplyWindowInsets:(Landroid/view/WindowInsets;)Landroid/view/WindowInsets;:GetOnApplyWindowInsets_Landroid_view_WindowInsets_Handler\n";
    private ArrayList refList;

    private native WindowInsets n_onApplyWindowInsets(WindowInsets windowInsets);

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.Compatibility.CustomFrameLayout, Microsoft.Maui.Controls", CustomFrameLayout.class, __md_methods);
    }

    public CustomFrameLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (getClass() == CustomFrameLayout.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.CustomFrameLayout, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    public CustomFrameLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == CustomFrameLayout.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.CustomFrameLayout, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public CustomFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == CustomFrameLayout.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.CustomFrameLayout, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public CustomFrameLayout(Context context) {
        super(context);
        if (getClass() == CustomFrameLayout.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.CustomFrameLayout, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        return n_onApplyWindowInsets(windowInsets);
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
