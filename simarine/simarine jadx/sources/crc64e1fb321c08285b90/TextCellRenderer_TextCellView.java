package crc64e1fb321c08285b90;

import android.content.Context;
import android.util.AttributeSet;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class TextCellRenderer_TextCellView extends BaseCellView implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Controls.Handlers.Compatibility.TextCellRenderer+TextCellView, Microsoft.Maui.Controls", TextCellRenderer_TextCellView.class, "");
    }

    public TextCellRenderer_TextCellView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == TextCellRenderer_TextCellView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.TextCellRenderer+TextCellView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public TextCellRenderer_TextCellView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == TextCellRenderer_TextCellView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.TextCellRenderer+TextCellView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public TextCellRenderer_TextCellView(Context context) {
        super(context);
        if (getClass() == TextCellRenderer_TextCellView.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Handlers.Compatibility.TextCellRenderer+TextCellView, Microsoft.Maui.Controls", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // crc64e1fb321c08285b90.BaseCellView, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc64e1fb321c08285b90.BaseCellView, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
