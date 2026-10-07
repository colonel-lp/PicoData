package mono.android.view.accessibility;

import android.view.accessibility.AccessibilityManager;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class AccessibilityManager_AccessibilityServicesStateChangeListenerImplementor implements IGCUserPeer, AccessibilityManager.AccessibilityServicesStateChangeListener {
    public static final String __md_methods = "n_onAccessibilityServicesStateChanged:(Landroid/view/accessibility/AccessibilityManager;)V:GetOnAccessibilityServicesStateChanged_Landroid_view_accessibility_AccessibilityManager_Handler:Android.Views.Accessibility.AccessibilityManager/IAccessibilityServicesStateChangeListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onAccessibilityServicesStateChanged(AccessibilityManager accessibilityManager);

    static {
        Runtime.register("Android.Views.Accessibility.AccessibilityManager+IAccessibilityServicesStateChangeListenerImplementor, Mono.Android", AccessibilityManager_AccessibilityServicesStateChangeListenerImplementor.class, __md_methods);
    }

    public AccessibilityManager_AccessibilityServicesStateChangeListenerImplementor() {
        if (getClass() == AccessibilityManager_AccessibilityServicesStateChangeListenerImplementor.class) {
            TypeManager.Activate("Android.Views.Accessibility.AccessibilityManager+IAccessibilityServicesStateChangeListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.view.accessibility.AccessibilityManager.AccessibilityServicesStateChangeListener
    public void onAccessibilityServicesStateChanged(AccessibilityManager accessibilityManager) {
        n_onAccessibilityServicesStateChanged(accessibilityManager);
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
