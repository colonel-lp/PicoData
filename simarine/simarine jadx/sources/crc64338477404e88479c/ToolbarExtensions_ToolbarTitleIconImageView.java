package crc64338477404e88479c;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ToolbarExtensions_ToolbarTitleIconImageView extends AppCompatImageView implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.ToolbarExtensions+ToolbarTitleIconImageView, Microsoft.Maui.Controls", ToolbarExtensions_ToolbarTitleIconImageView.class, "");
    }

    public ToolbarExtensions_ToolbarTitleIconImageView(Context context) {
        super(context);
        if (getClass() == ToolbarExtensions_ToolbarTitleIconImageView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.ToolbarExtensions+ToolbarTitleIconImageView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public ToolbarExtensions_ToolbarTitleIconImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == ToolbarExtensions_ToolbarTitleIconImageView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.ToolbarExtensions+ToolbarTitleIconImageView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public ToolbarExtensions_ToolbarTitleIconImageView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == ToolbarExtensions_ToolbarTitleIconImageView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.ToolbarExtensions+ToolbarTitleIconImageView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
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
