package crc64338477404e88479c;

import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class DragAndDropGestureHandler_CustomLocalStateData implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.DragAndDropGestureHandler+CustomLocalStateData, Microsoft.Maui.Controls", DragAndDropGestureHandler_CustomLocalStateData.class, "");
    }

    public DragAndDropGestureHandler_CustomLocalStateData() {
        if (getClass() == DragAndDropGestureHandler_CustomLocalStateData.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.DragAndDropGestureHandler+CustomLocalStateData, Microsoft.Maui.Controls", "", this, new Object[0]);
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
