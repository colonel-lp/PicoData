package crc640ec207abc449b2ca;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ShellFragmentStateAdapter extends FragmentStateAdapter implements IGCUserPeer {
    public static final String __md_methods = "n_getItemCount:()I:GetGetItemCountHandler\nn_createFragment:(I)Landroidx/fragment/app/Fragment;:GetCreateFragment_IHandler\nn_getItemId:(I)J:GetGetItemId_IHandler\nn_containsItem:(J)Z:GetContainsItem_JHandler\n";
    private ArrayList refList;

    private native boolean n_containsItem(long j);

    private native Fragment n_createFragment(int i);

    private native int n_getItemCount();

    private native long n_getItemId(int i);

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.Compatibility.ShellFragmentStateAdapter, Microsoft.Maui.Controls", ShellFragmentStateAdapter.class, "n_getItemCount:()I:GetGetItemCountHandler\nn_createFragment:(I)Landroidx/fragment/app/Fragment;:GetCreateFragment_IHandler\nn_getItemId:(I)J:GetGetItemId_IHandler\nn_containsItem:(J)Z:GetContainsItem_JHandler\n");
    }

    public ShellFragmentStateAdapter(Fragment fragment) {
        super(fragment);
        if (getClass() == ShellFragmentStateAdapter.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.ShellFragmentStateAdapter, Microsoft.Maui.Controls", "AndroidX.Fragment.App.Fragment, Xamarin.AndroidX.Fragment", this, new Object[]{fragment});
        }
    }

    public ShellFragmentStateAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        if (getClass() == ShellFragmentStateAdapter.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.ShellFragmentStateAdapter, Microsoft.Maui.Controls", "AndroidX.Fragment.App.FragmentActivity, Xamarin.AndroidX.Fragment", this, new Object[]{fragmentActivity});
        }
    }

    public ShellFragmentStateAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        if (getClass() == ShellFragmentStateAdapter.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.Compatibility.ShellFragmentStateAdapter, Microsoft.Maui.Controls", "AndroidX.Fragment.App.FragmentManager, Xamarin.AndroidX.Fragment:AndroidX.Lifecycle.Lifecycle, Xamarin.AndroidX.Lifecycle.Common.Jvm", this, new Object[]{fragmentManager, lifecycle});
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return n_getItemCount();
    }

    @Override // androidx.viewpager2.adapter.FragmentStateAdapter
    public Fragment createFragment(int i) {
        return n_createFragment(i);
    }

    @Override // androidx.viewpager2.adapter.FragmentStateAdapter, androidx.recyclerview.widget.RecyclerView.Adapter
    public long getItemId(int i) {
        return n_getItemId(i);
    }

    @Override // androidx.viewpager2.adapter.FragmentStateAdapter
    public boolean containsItem(long j) {
        return n_containsItem(j);
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
