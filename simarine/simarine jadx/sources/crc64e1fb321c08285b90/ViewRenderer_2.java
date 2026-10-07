package crc64e1fb321c08285b90;

import android.content.Context;
import android.util.AttributeSet;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public abstract class ViewRenderer_2 extends VisualElementRenderer_1 implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Compatibility.ViewRenderer`2, Microsoft.Maui.Controls", ViewRenderer_2.class, "");
    }

    public ViewRenderer_2(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (getClass() == ViewRenderer_2.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.ViewRenderer`2, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    public ViewRenderer_2(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == ViewRenderer_2.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.ViewRenderer`2, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public ViewRenderer_2(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == ViewRenderer_2.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.ViewRenderer`2, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public ViewRenderer_2(Context context) {
        super(context);
        if (getClass() == ViewRenderer_2.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.ViewRenderer`2, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // crc64e1fb321c08285b90.VisualElementRenderer_1, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc64e1fb321c08285b90.VisualElementRenderer_1, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
