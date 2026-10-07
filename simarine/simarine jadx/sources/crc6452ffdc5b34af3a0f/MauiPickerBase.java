package crc6452ffdc5b34af3a0f;

import android.content.Context;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatEditText;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiPickerBase extends AppCompatEditText implements IGCUserPeer {
    public static final String __md_methods = "n_getDefaultMovementMethod:()Landroid/text/method/MovementMethod;:GetGetDefaultMovementMethodHandler\n";
    private ArrayList refList;

    private native MovementMethod n_getDefaultMovementMethod();

    static {
        Runtime.register("Microsoft.Maui.Platform.MauiPickerBase, Microsoft.Maui", MauiPickerBase.class, __md_methods);
    }

    public MauiPickerBase(Context context) {
        super(context);
        if (getClass() == MauiPickerBase.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiPickerBase, Microsoft.Maui", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public MauiPickerBase(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == MauiPickerBase.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiPickerBase, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public MauiPickerBase(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == MauiPickerBase.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiPickerBase, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // android.widget.EditText, android.widget.TextView
    public MovementMethod getDefaultMovementMethod() {
        return n_getDefaultMovementMethod();
    }

    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
