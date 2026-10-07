package crc640ec207abc449b2ca;

import android.content.Context;
import android.util.AttributeSet;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ShellFlyoutTemplatedContentRenderer_HeaderContainer extends ContainerView implements IGCUserPeer {
    public static final String __md_methods = "n_onMeasure:(II)V:GetOnMeasure_IIHandler\nn_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n";
    private ArrayList refList;

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    private native void n_onMeasure(int i, int i2);

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.Compatibility.ShellFlyoutTemplatedContentRenderer+HeaderContainer, Microsoft.Maui.Controls", ShellFlyoutTemplatedContentRenderer_HeaderContainer.class, __md_methods);
    }

    public ShellFlyoutTemplatedContentRenderer_HeaderContainer(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (getClass() == ShellFlyoutTemplatedContentRenderer_HeaderContainer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.ShellFlyoutTemplatedContentRenderer+HeaderContainer, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    public ShellFlyoutTemplatedContentRenderer_HeaderContainer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == ShellFlyoutTemplatedContentRenderer_HeaderContainer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.ShellFlyoutTemplatedContentRenderer+HeaderContainer, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public ShellFlyoutTemplatedContentRenderer_HeaderContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == ShellFlyoutTemplatedContentRenderer_HeaderContainer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.ShellFlyoutTemplatedContentRenderer+HeaderContainer, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public ShellFlyoutTemplatedContentRenderer_HeaderContainer(Context context) {
        super(context);
        if (getClass() == ShellFlyoutTemplatedContentRenderer_HeaderContainer.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.ShellFlyoutTemplatedContentRenderer+HeaderContainer, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // crc640ec207abc449b2ca.ContainerView, android.view.View
    public void onMeasure(int i, int i2) {
        n_onMeasure(i, i2);
    }

    @Override // crc640ec207abc449b2ca.ContainerView, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        n_onLayout(z, i, i2, i3, i4);
    }

    @Override // crc640ec207abc449b2ca.ContainerView, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc640ec207abc449b2ca.ContainerView, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
