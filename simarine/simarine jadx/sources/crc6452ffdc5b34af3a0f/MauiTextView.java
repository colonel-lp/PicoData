package crc6452ffdc5b34af3a0f;

import android.content.Context;
import com.microsoft.maui.PlatformAppCompatTextView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiTextView extends PlatformAppCompatTextView implements IGCUserPeer {
    public static final String __md_methods = "n_onLayoutFormatted:(ZIIII)V:GetOnLayoutFormatted_ZIIIIHandler\n";
    private ArrayList refList;

    private native void n_onLayoutFormatted(boolean z, int i, int i2, int i3, int i4);

    static {
        Runtime.register("Microsoft.Maui.Platform.MauiTextView, Microsoft.Maui", MauiTextView.class, __md_methods);
    }

    public MauiTextView(Context context) {
        super(context);
        if (getClass() == MauiTextView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiTextView, Microsoft.Maui", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // com.microsoft.maui.PlatformAppCompatTextView
    public void onLayoutFormatted(boolean z, int i, int i2, int i3, int i4) {
        n_onLayoutFormatted(z, i, i2, i3, i4);
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
