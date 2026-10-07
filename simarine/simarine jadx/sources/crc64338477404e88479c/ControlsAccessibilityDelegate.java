package crc64338477404e88479c;

import android.view.View;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import crc6452ffdc5b34af3a0f.AccessibilityDelegateCompatWrapper;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ControlsAccessibilityDelegate extends AccessibilityDelegateCompatWrapper implements IGCUserPeer {
    public static final String __md_methods = "n_onInitializeAccessibilityNodeInfo:(Landroid/view/View;Landroidx/core/view/accessibility/AccessibilityNodeInfoCompat;)V:GetOnInitializeAccessibilityNodeInfo_Landroid_view_View_Landroidx_core_view_accessibility_AccessibilityNodeInfoCompat_Handler\n";
    private ArrayList refList;

    private native void n_onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat);

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.ControlsAccessibilityDelegate, Microsoft.Maui.Controls", ControlsAccessibilityDelegate.class, "n_onInitializeAccessibilityNodeInfo:(Landroid/view/View;Landroidx/core/view/accessibility/AccessibilityNodeInfoCompat;)V:GetOnInitializeAccessibilityNodeInfo_Landroid_view_View_Landroidx_core_view_accessibility_AccessibilityNodeInfoCompat_Handler\n");
    }

    public ControlsAccessibilityDelegate() {
        if (getClass() == ControlsAccessibilityDelegate.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.ControlsAccessibilityDelegate, Microsoft.Maui.Controls", "", this, new Object[0]);
        }
    }

    public ControlsAccessibilityDelegate(View.AccessibilityDelegate accessibilityDelegate) {
        super(accessibilityDelegate);
        if (getClass() == ControlsAccessibilityDelegate.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.ControlsAccessibilityDelegate, Microsoft.Maui.Controls", "Android.Views.View+AccessibilityDelegate, Mono.Android", this, new Object[]{accessibilityDelegate});
        }
    }

    public ControlsAccessibilityDelegate(AccessibilityDelegateCompat accessibilityDelegateCompat) {
        if (getClass() == ControlsAccessibilityDelegate.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.ControlsAccessibilityDelegate, Microsoft.Maui.Controls", "AndroidX.Core.View.AccessibilityDelegateCompat, Xamarin.AndroidX.Core", this, new Object[]{accessibilityDelegateCompat});
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
