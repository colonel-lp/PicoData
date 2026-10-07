package crc648e35430423bd4943;

import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class SKGLTextureView_InternalRenderer extends SKGLTextureViewRenderer implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("SkiaSharp.Views.Android.SKGLTextureView+InternalRenderer, SkiaSharp.Views.Android", SKGLTextureView_InternalRenderer.class, "");
    }

    public SKGLTextureView_InternalRenderer() {
        if (getClass() == SKGLTextureView_InternalRenderer.class) {
            TypeManager.Activate("SkiaSharp.Views.Android.SKGLTextureView+InternalRenderer, SkiaSharp.Views.Android", "", this, new Object[0]);
        }
    }

    @Override // crc648e35430423bd4943.SKGLTextureViewRenderer, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc648e35430423bd4943.SKGLTextureViewRenderer, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
