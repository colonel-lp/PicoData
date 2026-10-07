package crc648e35430423bd4943;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class SKGLSurfaceView extends GLSurfaceView implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("SkiaSharp.Views.Android.SKGLSurfaceView, SkiaSharp.Views.Android", SKGLSurfaceView.class, "");
    }

    public SKGLSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == SKGLSurfaceView.class) {
            TypeManager.Activate("SkiaSharp.Views.Android.SKGLSurfaceView, SkiaSharp.Views.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public SKGLSurfaceView(Context context) {
        super(context);
        if (getClass() == SKGLSurfaceView.class) {
            TypeManager.Activate("SkiaSharp.Views.Android.SKGLSurfaceView, SkiaSharp.Views.Android", "Android.Content.Context, Mono.Android", this, new Object[]{context});
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
