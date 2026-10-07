package crc64b5e713d400f589b7;

import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class RadialGradientShaderFactory extends ShapeDrawable.ShaderFactory implements IGCUserPeer {
    public static final String __md_methods = "n_resize:(II)Landroid/graphics/Shader;:GetResize_IIHandler\n";
    private ArrayList refList;

    private native Shader n_resize(int i, int i2);

    static {
        Runtime.register("Microsoft.Maui.Graphics.RadialGradientShaderFactory, Microsoft.Maui", RadialGradientShaderFactory.class, "n_resize:(II)Landroid/graphics/Shader;:GetResize_IIHandler\n");
    }

    public RadialGradientShaderFactory() {
        if (getClass() == RadialGradientShaderFactory.class) {
            TypeManager.Activate("Microsoft.Maui.Graphics.RadialGradientShaderFactory, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    @Override // android.graphics.drawable.ShapeDrawable.ShaderFactory
    public Shader resize(int i, int i2) {
        return n_resize(i, i2);
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
