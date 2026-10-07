package crc6452ffdc5b34af3a0f;

import android.view.View;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiAccessibilityDelegateCompat extends AccessibilityDelegateCompatWrapper implements IGCUserPeer {
    public static final String __md_methods = "n_onInitializeAccessibilityNodeInfo:(Landroid/view/View;Landroidx/core/view/accessibility/AccessibilityNodeInfoCompat;)V:GetOnInitializeAccessibilityNodeInfo_Landroid_view_View_Landroidx_core_view_accessibility_AccessibilityNodeInfoCompat_Handler\n";
    private ArrayList refList;

    private native void n_onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat);

    static {
        Runtime.register("Microsoft.Maui.Platform.MauiAccessibilityDelegateCompat, Microsoft.Maui", MauiAccessibilityDelegateCompat.class, "n_onInitializeAccessibilityNodeInfo:(Landroid/view/View;Landroidx/core/view/accessibility/AccessibilityNodeInfoCompat;)V:GetOnInitializeAccessibilityNodeInfo_Landroid_view_View_Landroidx_core_view_accessibility_AccessibilityNodeInfoCompat_Handler\n");
    }

    public MauiAccessibilityDelegateCompat() {
        if (getClass() == MauiAccessibilityDelegateCompat.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiAccessibilityDelegateCompat, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    public MauiAccessibilityDelegateCompat(View.AccessibilityDelegate accessibilityDelegate) {
        super(accessibilityDelegate);
        if (getClass() == MauiAccessibilityDelegateCompat.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiAccessibilityDelegateCompat, Microsoft.Maui", "Android.Views.View+AccessibilityDelegate, Mono.Android", this, new Object[]{accessibilityDelegate});
        }
    }

    public MauiAccessibilityDelegateCompat(AccessibilityDelegateCompat accessibilityDelegateCompat) {
        if (getClass() == MauiAccessibilityDelegateCompat.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiAccessibilityDelegateCompat, Microsoft.Maui", "AndroidX.Core.View.AccessibilityDelegateCompat, Xamarin.AndroidX.Core", this, new Object[]{accessibilityDelegateCompat});
        }
    }

    @Override // crc6452ffdc5b34af3a0f.AccessibilityDelegateCompatWrapper, androidx.core.view.AccessibilityDelegateCompat
    public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
        n_onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfoCompat);
    }

    @Override // crc6452ffdc5b34af3a0f.AccessibilityDelegateCompatWrapper, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc6452ffdc5b34af3a0f.AccessibilityDelegateCompatWrapper, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
