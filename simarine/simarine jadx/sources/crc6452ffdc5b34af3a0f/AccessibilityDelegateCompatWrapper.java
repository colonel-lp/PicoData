package crc6452ffdc5b34af3a0f;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeProviderCompat;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class AccessibilityDelegateCompatWrapper extends AccessibilityDelegateCompat implements IGCUserPeer {
    public static final String __md_methods = "n_onInitializeAccessibilityNodeInfo:(Landroid/view/View;Landroidx/core/view/accessibility/AccessibilityNodeInfoCompat;)V:GetOnInitializeAccessibilityNodeInfo_Landroid_view_View_Landroidx_core_view_accessibility_AccessibilityNodeInfoCompat_Handler\nn_sendAccessibilityEvent:(Landroid/view/View;I)V:GetSendAccessibilityEvent_Landroid_view_View_IHandler\nn_sendAccessibilityEventUnchecked:(Landroid/view/View;Landroid/view/accessibility/AccessibilityEvent;)V:GetSendAccessibilityEventUnchecked_Landroid_view_View_Landroid_view_accessibility_AccessibilityEvent_Handler\nn_dispatchPopulateAccessibilityEvent:(Landroid/view/View;Landroid/view/accessibility/AccessibilityEvent;)Z:GetDispatchPopulateAccessibilityEvent_Landroid_view_View_Landroid_view_accessibility_AccessibilityEvent_Handler\nn_onPopulateAccessibilityEvent:(Landroid/view/View;Landroid/view/accessibility/AccessibilityEvent;)V:GetOnPopulateAccessibilityEvent_Landroid_view_View_Landroid_view_accessibility_AccessibilityEvent_Handler\nn_onInitializeAccessibilityEvent:(Landroid/view/View;Landroid/view/accessibility/AccessibilityEvent;)V:GetOnInitializeAccessibilityEvent_Landroid_view_View_Landroid_view_accessibility_AccessibilityEvent_Handler\nn_onRequestSendAccessibilityEvent:(Landroid/view/ViewGroup;Landroid/view/View;Landroid/view/accessibility/AccessibilityEvent;)Z:GetOnRequestSendAccessibilityEvent_Landroid_view_ViewGroup_Landroid_view_View_Landroid_view_accessibility_AccessibilityEvent_Handler\nn_performAccessibilityAction:(Landroid/view/View;ILandroid/os/Bundle;)Z:GetPerformAccessibilityAction_Landroid_view_View_ILandroid_os_Bundle_Handler\nn_getAccessibilityNodeProvider:(Landroid/view/View;)Landroidx/core/view/accessibility/AccessibilityNodeProviderCompat;:GetGetAccessibilityNodeProvider_Landroid_view_View_Handler\n";
    private ArrayList refList;

    private native boolean n_dispatchPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent);

    private native AccessibilityNodeProviderCompat n_getAccessibilityNodeProvider(View view);

    private native void n_onInitializeAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent);

    private native void n_onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat);

    private native void n_onPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent);

    private native boolean n_onRequestSendAccessibilityEvent(ViewGroup viewGroup, View view, AccessibilityEvent accessibilityEvent);

    private native boolean n_performAccessibilityAction(View view, int i, Bundle bundle);

    private native void n_sendAccessibilityEvent(View view, int i);

    private native void n_sendAccessibilityEventUnchecked(View view, AccessibilityEvent accessibilityEvent);

    static {
        Runtime.register("Microsoft.Maui.Platform.AccessibilityDelegateCompatWrapper, Microsoft.Maui", AccessibilityDelegateCompatWrapper.class, __md_methods);
    }

    public AccessibilityDelegateCompatWrapper() {
        if (getClass() == AccessibilityDelegateCompatWrapper.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.AccessibilityDelegateCompatWrapper, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    public AccessibilityDelegateCompatWrapper(View.AccessibilityDelegate accessibilityDelegate) {
        super(accessibilityDelegate);
        if (getClass() == AccessibilityDelegateCompatWrapper.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.AccessibilityDelegateCompatWrapper, Microsoft.Maui", "Android.Views.View+AccessibilityDelegate, Mono.Android", this, new Object[]{accessibilityDelegate});
        }
    }

    public AccessibilityDelegateCompatWrapper(AccessibilityDelegateCompat accessibilityDelegateCompat) {
        if (getClass() == AccessibilityDelegateCompatWrapper.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.AccessibilityDelegateCompatWrapper, Microsoft.Maui", "AndroidX.Core.View.AccessibilityDelegateCompat, Xamarin.AndroidX.Core", this, new Object[]{accessibilityDelegateCompat});
        }
    }

    @Override // androidx.core.view.AccessibilityDelegateCompat
    public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
        n_onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfoCompat);
    }

    @Override // androidx.core.view.AccessibilityDelegateCompat
    public void sendAccessibilityEvent(View view, int i) {
        n_sendAccessibilityEvent(view, i);
    }

    @Override // androidx.core.view.AccessibilityDelegateCompat
    public void sendAccessibilityEventUnchecked(View view, AccessibilityEvent accessibilityEvent) {
        n_sendAccessibilityEventUnchecked(view, accessibilityEvent);
    }

    @Override // androidx.core.view.AccessibilityDelegateCompat
    public boolean dispatchPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
        return n_dispatchPopulateAccessibilityEvent(view, accessibilityEvent);
    }

    @Override // androidx.core.view.AccessibilityDelegateCompat
    public void onPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
        n_onPopulateAccessibilityEvent(view, accessibilityEvent);
    }

    @Override // androidx.core.view.AccessibilityDelegateCompat
    public void onInitializeAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
        n_onInitializeAccessibilityEvent(view, accessibilityEvent);
    }

    @Override // androidx.core.view.AccessibilityDelegateCompat
    public boolean onRequestSendAccessibilityEvent(ViewGroup viewGroup, View view, AccessibilityEvent accessibilityEvent) {
        return n_onRequestSendAccessibilityEvent(viewGroup, view, accessibilityEvent);
    }

    @Override // androidx.core.view.AccessibilityDelegateCompat
    public boolean performAccessibilityAction(View view, int i, Bundle bundle) {
        return n_performAccessibilityAction(view, i, bundle);
    }

    @Override // androidx.core.view.AccessibilityDelegateCompat
    public AccessibilityNodeProviderCompat getAccessibilityNodeProvider(View view) {
        return n_getAccessibilityNodeProvider(view);
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
