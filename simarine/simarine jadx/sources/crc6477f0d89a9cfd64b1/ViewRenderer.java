package crc6477f0d89a9cfd64b1;

import android.content.Context;
import android.util.AttributeSet;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public abstract class ViewRenderer extends ViewRenderer_2 implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Controls.Compatibility.Platform.Android.ViewRenderer, Microsoft.Maui.Controls.Compatibility", ViewRenderer.class, "");
    }

    public ViewRenderer(Context context) {
        super(context);
        if (getClass() == ViewRenderer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Compatibility.Platform.Android.ViewRenderer, Microsoft.Maui.Controls.Compatibility", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public ViewRenderer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == ViewRenderer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Compatibility.Platform.Android.ViewRenderer, Microsoft.Maui.Controls.Compatibility", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public ViewRenderer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == ViewRenderer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Compatibility.Platform.Android.ViewRenderer, Microsoft.Maui.Controls.Compatibility", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
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
