package crc64159f3caeb1269279;

import android.content.Context;
import android.util.AttributeSet;
import crc6452ffdc5b34af3a0f.ContentViewGroup;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiSemanticOrderView extends ContentViewGroup implements IGCUserPeer {
    public static final String __md_methods = "n_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n";
    private ArrayList refList;

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    static {
        Runtime.register("CommunityToolkit.Maui.Core.Views.MauiSemanticOrderView, CommunityToolkit.Maui.Core", MauiSemanticOrderView.class, "n_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n");
    }

    public MauiSemanticOrderView(Context context) {
        super(context);
        if (getClass() == MauiSemanticOrderView.class) {
            TypeManager.Activate("CommunityToolkit.Maui.Core.Views.MauiSemanticOrderView, CommunityToolkit.Maui.Core", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public MauiSemanticOrderView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == MauiSemanticOrderView.class) {
            TypeManager.Activate("CommunityToolkit.Maui.Core.Views.MauiSemanticOrderView, CommunityToolkit.Maui.Core", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public MauiSemanticOrderView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == MauiSemanticOrderView.class) {
            TypeManager.Activate("CommunityToolkit.Maui.Core.Views.MauiSemanticOrderView, CommunityToolkit.Maui.Core", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public MauiSemanticOrderView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (getClass() == MauiSemanticOrderView.class) {
            TypeManager.Activate("CommunityToolkit.Maui.Core.Views.MauiSemanticOrderView, CommunityToolkit.Maui.Core", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    @Override // crc6452ffdc5b34af3a0f.ContentViewGroup, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        n_onLayout(z, i, i2, i3, i4);
    }

    @Override // crc6452ffdc5b34af3a0f.ContentViewGroup, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc6452ffdc5b34af3a0f.ContentViewGroup, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
