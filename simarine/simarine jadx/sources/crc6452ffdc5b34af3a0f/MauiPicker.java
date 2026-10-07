package crc6452ffdc5b34af3a0f;

import android.content.Context;
import android.util.AttributeSet;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiPicker extends MauiPickerBase implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Platform.MauiPicker, Microsoft.Maui", MauiPicker.class, "");
    }

    public MauiPicker(Context context) {
        super(context);
        if (getClass() == MauiPicker.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiPicker, Microsoft.Maui", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public MauiPicker(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == MauiPicker.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiPicker, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public MauiPicker(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == MauiPicker.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiPicker, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // crc6452ffdc5b34af3a0f.MauiPickerBase, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc6452ffdc5b34af3a0f.MauiPickerBase, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
