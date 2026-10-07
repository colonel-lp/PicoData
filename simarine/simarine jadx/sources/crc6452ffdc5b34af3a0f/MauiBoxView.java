package crc6452ffdc5b34af3a0f;

import android.content.Context;
import android.util.AttributeSet;
import crc643f2b18b2570eaa5a.PlatformGraphicsView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiBoxView extends PlatformGraphicsView implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Platform.MauiBoxView, Microsoft.Maui", MauiBoxView.class, "");
    }

    public MauiBoxView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (getClass() == MauiBoxView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiBoxView, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    public MauiBoxView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == MauiBoxView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiBoxView, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public MauiBoxView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == MauiBoxView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiBoxView, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public MauiBoxView(Context context) {
        super(context);
        if (getClass() == MauiBoxView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiBoxView, Microsoft.Maui", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // crc643f2b18b2570eaa5a.PlatformGraphicsView, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc643f2b18b2570eaa5a.PlatformGraphicsView, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
