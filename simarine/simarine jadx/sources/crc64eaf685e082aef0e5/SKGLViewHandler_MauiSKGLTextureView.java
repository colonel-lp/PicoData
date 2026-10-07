package crc64eaf685e082aef0e5;

import android.content.Context;
import android.util.AttributeSet;
import crc648e35430423bd4943.SKGLTextureView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class SKGLViewHandler_MauiSKGLTextureView extends SKGLTextureView implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("SkiaSharp.Views.Maui.Handlers.SKGLViewHandler+MauiSKGLTextureView, SkiaSharp.Views.Maui.Core", SKGLViewHandler_MauiSKGLTextureView.class, "");
    }

    public SKGLViewHandler_MauiSKGLTextureView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == SKGLViewHandler_MauiSKGLTextureView.class) {
            TypeManager.Activate("SkiaSharp.Views.Maui.Handlers.SKGLViewHandler+MauiSKGLTextureView, SkiaSharp.Views.Maui.Core", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public SKGLViewHandler_MauiSKGLTextureView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == SKGLViewHandler_MauiSKGLTextureView.class) {
            TypeManager.Activate("SkiaSharp.Views.Maui.Handlers.SKGLViewHandler+MauiSKGLTextureView, SkiaSharp.Views.Maui.Core", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public SKGLViewHandler_MauiSKGLTextureView(Context context) {
        super(context);
        if (getClass() == SKGLViewHandler_MauiSKGLTextureView.class) {
            TypeManager.Activate("SkiaSharp.Views.Maui.Handlers.SKGLViewHandler+MauiSKGLTextureView, SkiaSharp.Views.Maui.Core", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // crc648e35430423bd4943.SKGLTextureView, crc648e35430423bd4943.GLTextureView, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc648e35430423bd4943.SKGLTextureView, crc648e35430423bd4943.GLTextureView, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
