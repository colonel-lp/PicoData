package crc6452ffdc5b34af3a0f;

import android.content.Context;
import android.util.AttributeSet;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiShapeableImageView extends ShapeableImageView implements IGCUserPeer {
    public static final String __md_methods = "n_onMeasure:(II)V:GetOnMeasure_IIHandler\n";
    private ArrayList refList;

    private native void n_onMeasure(int i, int i2);

    static {
        Runtime.register("Microsoft.Maui.Platform.MauiShapeableImageView, Microsoft.Maui", MauiShapeableImageView.class, "n_onMeasure:(II)V:GetOnMeasure_IIHandler\n");
    }

    public MauiShapeableImageView(Context context) {
        super(context);
        if (getClass() == MauiShapeableImageView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiShapeableImageView, Microsoft.Maui", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public MauiShapeableImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == MauiShapeableImageView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiShapeableImageView, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public MauiShapeableImageView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == MauiShapeableImageView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiShapeableImageView, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // com.google.android.material.imageview.ShapeableImageView, android.widget.ImageView, android.view.View
    public void onMeasure(int i, int i2) {
        n_onMeasure(i, i2);
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
