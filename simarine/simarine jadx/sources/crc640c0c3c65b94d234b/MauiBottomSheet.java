package crc640c0c3c65b94d234b;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiBottomSheet extends View implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Plugin.Maui.BottomSheet.Platform.Android.MauiBottomSheet, Plugin.Maui.BottomSheet", MauiBottomSheet.class, "");
    }

    public MauiBottomSheet(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (getClass() == MauiBottomSheet.class) {
            TypeManager.Activate("Plugin.Maui.BottomSheet.Platform.Android.MauiBottomSheet, Plugin.Maui.BottomSheet", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    public MauiBottomSheet(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == MauiBottomSheet.class) {
            TypeManager.Activate("Plugin.Maui.BottomSheet.Platform.Android.MauiBottomSheet, Plugin.Maui.BottomSheet", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public MauiBottomSheet(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == MauiBottomSheet.class) {
            TypeManager.Activate("Plugin.Maui.BottomSheet.Platform.Android.MauiBottomSheet, Plugin.Maui.BottomSheet", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public MauiBottomSheet(Context context) {
        super(context);
        if (getClass() == MauiBottomSheet.class) {
            TypeManager.Activate("Plugin.Maui.BottomSheet.Platform.Android.MauiBottomSheet, Plugin.Maui.BottomSheet", "Android.Content.Context, Mono.Android", this, new Object[]{context});
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
