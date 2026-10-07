package crc6452ffdc5b34af3a0f;

import android.content.Context;
import android.util.AttributeSet;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiMaterialButton extends MaterialButton implements IGCUserPeer {
    public static final String __md_methods = "n_getIconGravity:()I:GetGetIconGravityHandler\nn_setIconGravity:(I)V:GetSetIconGravity_IHandler\nn_onMeasure:(II)V:GetOnMeasure_IIHandler\nn_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n";
    private ArrayList refList;

    private native int n_getIconGravity();

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    private native void n_onMeasure(int i, int i2);

    private native void n_setIconGravity(int i);

    static {
        Runtime.register("Microsoft.Maui.Platform.MauiMaterialButton, Microsoft.Maui", MauiMaterialButton.class, __md_methods);
    }

    public MauiMaterialButton(Context context) {
        super(context);
        if (getClass() == MauiMaterialButton.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiMaterialButton, Microsoft.Maui", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public MauiMaterialButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == MauiMaterialButton.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiMaterialButton, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public MauiMaterialButton(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == MauiMaterialButton.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiMaterialButton, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // com.google.android.material.button.MaterialButton
    public int getIconGravity() {
        return n_getIconGravity();
    }

    @Override // com.google.android.material.button.MaterialButton
    public void setIconGravity(int i) {
        n_setIconGravity(i);
    }

    @Override // android.widget.TextView, android.view.View
    public void onMeasure(int i, int i2) {
        n_onMeasure(i, i2);
    }

    @Override // com.google.android.material.button.MaterialButton, androidx.appcompat.widget.AppCompatButton, android.widget.TextView, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        n_onLayout(z, i, i2, i3, i4);
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
