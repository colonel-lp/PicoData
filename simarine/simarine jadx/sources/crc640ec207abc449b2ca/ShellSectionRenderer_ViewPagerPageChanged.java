package crc640ec207abc449b2ca;

import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ShellSectionRenderer_ViewPagerPageChanged extends ViewPager2.OnPageChangeCallback implements IGCUserPeer {
    public static final String __md_methods = "n_onPageSelected:(I)V:GetOnPageSelected_IHandler\n";
    private ArrayList refList;

    private native void n_onPageSelected(int i);

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.Compatibility.ShellSectionRenderer+ViewPagerPageChanged, Microsoft.Maui.Controls", ShellSectionRenderer_ViewPagerPageChanged.class, __md_methods);
    }

    public ShellSectionRenderer_ViewPagerPageChanged() {
        if (getClass() == ShellSectionRenderer_ViewPagerPageChanged.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.ShellSectionRenderer+ViewPagerPageChanged, Microsoft.Maui.Controls", "", this, new Object[0]);
        }
    }

    public ShellSectionRenderer_ViewPagerPageChanged(ShellSectionRenderer shellSectionRenderer) {
        if (getClass() == ShellSectionRenderer_ViewPagerPageChanged.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.ShellSectionRenderer+ViewPagerPageChanged, Microsoft.Maui.Controls", "Microsoft.Maui.Controls.Platform.Compatibility.ShellSectionRenderer, Microsoft.Maui.Controls", this, new Object[]{shellSectionRenderer});
        }
    }

    @Override // androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
    public void onPageSelected(int i) {
        n_onPageSelected(i);
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
