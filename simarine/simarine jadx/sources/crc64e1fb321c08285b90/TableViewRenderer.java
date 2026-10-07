package crc64e1fb321c08285b90;

import android.content.Context;
import android.util.AttributeSet;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TableViewRenderer extends ViewRenderer_2 implements IGCUserPeer {
    public static final String __md_methods = "n_onAttachedToWindow:()V:GetOnAttachedToWindowHandler\nn_onMeasure:(II)V:GetOnMeasure_IIHandler\n";
    private ArrayList refList;

    private native void n_onAttachedToWindow();

    private native void n_onMeasure(int i, int i2);

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Compatibility.TableViewRenderer, Microsoft.Maui.Controls", TableViewRenderer.class, __md_methods);
    }

    public TableViewRenderer(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (getClass() == TableViewRenderer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.TableViewRenderer, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    public TableViewRenderer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == TableViewRenderer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.TableViewRenderer, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public TableViewRenderer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == TableViewRenderer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.TableViewRenderer, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public TableViewRenderer(Context context) {
        super(context);
        if (getClass() == TableViewRenderer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.TableViewRenderer, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        n_onAttachedToWindow();
    }

    @Override // crc64e1fb321c08285b90.VisualElementRenderer_1, android.view.View
    public void onMeasure(int i, int i2) {
        n_onMeasure(i, i2);
    }

    @Override // crc64e1fb321c08285b90.ViewRenderer_2, crc64e1fb321c08285b90.VisualElementRenderer_1, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc64e1fb321c08285b90.ViewRenderer_2, crc64e1fb321c08285b90.VisualElementRenderer_1, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
