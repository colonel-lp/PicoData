package crc6452ffdc5b34af3a0f;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatEditText;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiAppCompatEditText extends AppCompatEditText implements IGCUserPeer {
    public static final String __md_methods = "n_onSelectionChanged:(II)V:GetOnSelectionChanged_IIHandler\n";
    private ArrayList refList;

    private native void n_onSelectionChanged(int i, int i2);

    static {
        Runtime.register("Microsoft.Maui.Platform.MauiAppCompatEditText, Microsoft.Maui", MauiAppCompatEditText.class, __md_methods);
    }

    public MauiAppCompatEditText(Context context) {
        super(context);
        if (getClass() == MauiAppCompatEditText.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiAppCompatEditText, Microsoft.Maui", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public MauiAppCompatEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == MauiAppCompatEditText.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiAppCompatEditText, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public MauiAppCompatEditText(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == MauiAppCompatEditText.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiAppCompatEditText, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // android.widget.TextView
    public void onSelectionChanged(int i, int i2) {
        n_onSelectionChanged(i, i2);
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
