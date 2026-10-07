package mono.android.media;

import android.media.Spatializer;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class Spatializer_OnSpatializerStateChangedListenerImplementor implements IGCUserPeer, Spatializer.OnSpatializerStateChangedListener {
    public static final String __md_methods = "n_onSpatializerAvailableChanged:(Landroid/media/Spatializer;Z)V:GetOnSpatializerAvailableChanged_Landroid_media_Spatializer_ZHandler:Android.Media.Spatializer/IOnSpatializerStateChangedListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onSpatializerEnabledChanged:(Landroid/media/Spatializer;Z)V:GetOnSpatializerEnabledChanged_Landroid_media_Spatializer_ZHandler:Android.Media.Spatializer/IOnSpatializerStateChangedListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onSpatializerAvailableChanged(Spatializer spatializer, boolean z);

    private native void n_onSpatializerEnabledChanged(Spatializer spatializer, boolean z);

    static {
        Runtime.register("Android.Media.Spatializer+IOnSpatializerStateChangedListenerImplementor, Mono.Android", Spatializer_OnSpatializerStateChangedListenerImplementor.class, __md_methods);
    }

    public Spatializer_OnSpatializerStateChangedListenerImplementor() {
        if (getClass() == Spatializer_OnSpatializerStateChangedListenerImplementor.class) {
            TypeManager.Activate("Android.Media.Spatializer+IOnSpatializerStateChangedListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.media.Spatializer.OnSpatializerStateChangedListener
    public void onSpatializerAvailableChanged(Spatializer spatializer, boolean z) {
        n_onSpatializerAvailableChanged(spatializer, z);
    }

    @Override // android.media.Spatializer.OnSpatializerStateChangedListener
    public void onSpatializerEnabledChanged(Spatializer spatializer, boolean z) {
        n_onSpatializerEnabledChanged(spatializer, z);
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
