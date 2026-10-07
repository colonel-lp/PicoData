package crc649ff77a65592e7d55;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TabbedPageManager_TempView extends View implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.TabbedPageManager+TempView, Microsoft.Maui.Controls", TabbedPageManager_TempView.class, "");
    }

    public TabbedPageManager_TempView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (getClass() == TabbedPageManager_TempView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.TabbedPageManager+TempView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    public TabbedPageManager_TempView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == TabbedPageManager_TempView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.TabbedPageManager+TempView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public TabbedPageManager_TempView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == TabbedPageManager_TempView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.TabbedPageManager+TempView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public TabbedPageManager_TempView(Context context) {
        super(context);
        if (getClass() == TabbedPageManager_TempView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.TabbedPageManager+TempView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android", this, new Object[]{context});
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
