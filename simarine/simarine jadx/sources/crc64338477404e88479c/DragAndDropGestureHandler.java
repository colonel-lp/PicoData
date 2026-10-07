package crc64338477404e88479c;

import android.view.DragEvent;
import android.view.View;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class DragAndDropGestureHandler implements IGCUserPeer, View.OnDragListener {
    public static final String __md_methods = "n_onDrag:(Landroid/view/View;Landroid/view/DragEvent;)Z:GetOnDrag_Landroid_view_View_Landroid_view_DragEvent_Handler:Android.Views.View/IOnDragListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native boolean n_onDrag(View view, DragEvent dragEvent);

    static {
        Runtime.register("Microsoft.Maui.Controls.Platform.DragAndDropGestureHandler, Microsoft.Maui.Controls", DragAndDropGestureHandler.class, "n_onDrag:(Landroid/view/View;Landroid/view/DragEvent;)Z:GetOnDrag_Landroid_view_View_Landroid_view_DragEvent_Handler:Android.Views.View/IOnDragListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n");
    }

    public DragAndDropGestureHandler() {
        if (getClass() == DragAndDropGestureHandler.class) {
            TypeManager.Activate("Microsoft.Maui.Controls.Platform.DragAndDropGestureHandler, Microsoft.Maui.Controls", "", this, new Object[0]);
        }
    }

    @Override // android.view.View.OnDragListener
    public boolean onDrag(View view, DragEvent dragEvent) {
        return n_onDrag(view, dragEvent);
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
