package crc645d80431ce5f73f11;

import android.content.Context;
import android.util.AttributeSet;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class SizedItemContentView extends ItemContentView implements IGCUserPeer {
    public static final String __md_methods = "n_onMeasure:(II)V:GetOnMeasure_IIHandler\n";
    private ArrayList refList;

    private native void n_onMeasure(int i, int i2);

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Items.SizedItemContentView, Microsoft.Maui.Controls", SizedItemContentView.class, "n_onMeasure:(II)V:GetOnMeasure_IIHandler\n");
    }

    public SizedItemContentView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (getClass() == SizedItemContentView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.SizedItemContentView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    public SizedItemContentView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == SizedItemContentView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.SizedItemContentView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public SizedItemContentView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == SizedItemContentView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.SizedItemContentView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public SizedItemContentView(Context context) {
        super(context);
        if (getClass() == SizedItemContentView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Items.SizedItemContentView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // crc645d80431ce5f73f11.ItemContentView, android.view.View
    public void onMeasure(int i, int i2) {
        n_onMeasure(i, i2);
    }

    @Override // crc645d80431ce5f73f11.ItemContentView, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc645d80431ce5f73f11.ItemContentView, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
