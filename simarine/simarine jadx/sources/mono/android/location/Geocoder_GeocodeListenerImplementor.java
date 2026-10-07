package mono.android.location;

import android.location.Geocoder;
import java.util.ArrayList;
import java.util.List;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class Geocoder_GeocodeListenerImplementor implements IGCUserPeer, Geocoder.GeocodeListener {
    public static final String __md_methods = "n_onGeocode:(Ljava/util/List;)V:GetOnGeocode_Ljava_util_List_Handler:Android.Locations.Geocoder/IGeocodeListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onError:(Ljava/lang/String;)V:GetOnError_Ljava_lang_String_Handler:Android.Locations.Geocoder/IGeocodeListener, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onError(String str);

    private native void n_onGeocode(List list);

    static {
        Runtime.register("Android.Locations.Geocoder+IGeocodeListenerImplementor, Mono.Android", Geocoder_GeocodeListenerImplementor.class, __md_methods);
    }

    public Geocoder_GeocodeListenerImplementor() {
        if (getClass() == Geocoder_GeocodeListenerImplementor.class) {
            TypeManager.Activate("Android.Locations.Geocoder+IGeocodeListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.location.Geocoder.GeocodeListener
    public void onGeocode(List list) {
        n_onGeocode(list);
    }

    @Override // android.location.Geocoder.GeocodeListener
    public void onError(String str) {
        n_onError(str);
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
