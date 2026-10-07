package crc6452ffdc5b34af3a0f;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.SearchView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiSearchView extends SearchView implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Platform.MauiSearchView, Microsoft.Maui", MauiSearchView.class, "");
    }

    public MauiSearchView(Context context) {
        super(context);
        if (getClass() == MauiSearchView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiSearchView, Microsoft.Maui", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public MauiSearchView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == MauiSearchView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiSearchView, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public MauiSearchView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == MauiSearchView.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiSearchView, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
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
