package crc64e1fb321c08285b90;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class BaseCellView extends LinearLayout implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Compatibility.BaseCellView, Microsoft.Maui.Controls", BaseCellView.class, "");
    }

    public BaseCellView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == BaseCellView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.BaseCellView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public BaseCellView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == BaseCellView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.BaseCellView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public BaseCellView(Context context) {
        super(context);
        if (getClass() == BaseCellView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.BaseCellView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
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
