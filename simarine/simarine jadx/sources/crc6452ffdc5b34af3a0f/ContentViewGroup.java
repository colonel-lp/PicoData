package crc6452ffdc5b34af3a0f;

import android.content.Context;
import android.graphics.Path;
import android.util.AttributeSet;
import com.microsoft.maui.PlatformContentViewGroup;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ContentViewGroup extends PlatformContentViewGroup implements IGCUserPeer {
    public static final String __md_methods = "n_onMeasure:(II)V:GetOnMeasure_IIHandler\nn_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\nn_getClipPath:(II)Landroid/graphics/Path;:GetGetClipPath_IIHandler\n";
    private ArrayList refList;

    private native Path n_getClipPath(int i, int i2);

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    private native void n_onMeasure(int i, int i2);

    static {
        Runtime.register("Microsoft.Maui.Platform.ContentViewGroup, Microsoft.Maui", ContentViewGroup.class, __md_methods);
    }

    public ContentViewGroup(Context context) {
        super(context);
        if (getClass() == ContentViewGroup.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.ContentViewGroup, Microsoft.Maui", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public ContentViewGroup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == ContentViewGroup.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.ContentViewGroup, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public ContentViewGroup(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == ContentViewGroup.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.ContentViewGroup, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public ContentViewGroup(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (getClass() == ContentViewGroup.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.ContentViewGroup, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    @Override // android.view.View
    public void onMeasure(int i, int i2) {
        n_onMeasure(i, i2);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        n_onLayout(z, i, i2, i3, i4);
    }

    @Override // com.microsoft.maui.PlatformContentViewGroup
    public Path getClipPath(int i, int i2) {
        return n_getClipPath(i, i2);
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
