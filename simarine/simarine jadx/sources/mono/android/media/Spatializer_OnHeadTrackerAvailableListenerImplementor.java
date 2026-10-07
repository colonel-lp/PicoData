package mono.android.media;

import android.media.Spatializer;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class Spatializer_OnHeadTrackerAvailableListenerImplementor implements IGCUserPeer, Spatializer.OnHeadTrackerAvailableListener {
    public static final String __md_methods = "n_onHeadTrackerAvailableChanged:(Landroid/media/Spatializer;Z)V:GetOnHeadTrackerAvailableChanged_Landroid_media_Spatializer_ZHandler:Android.Media.Spatializer/IOnHeadTrackerAvailableListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onHeadTrackerAvailableChanged(Spatializer spatializer, boolean z);

    static {
        Runtime.register("Android.Media.Spatializer+IOnHeadTrackerAvailableListenerImplementor, Mono.Android", Spatializer_OnHeadTrackerAvailableListenerImplementor.class, __md_methods);
    }

    public Spatializer_OnHeadTrackerAvailableListenerImplementor() {
        if (getClass() == Spatializer_OnHeadTrackerAvailableListenerImplementor.class) {
            TypeManager.Activate("Android.Media.Spatializer+IOnHeadTrackerAvailableListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.media.Spatializer.OnHeadTrackerAvailableListener
    public void onHeadTrackerAvailableChanged(Spatializer spatializer, boolean z) {
        n_onHeadTrackerAvailableChanged(spatializer, z);
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
