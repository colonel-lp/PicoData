package crc64a057c3230d7882d7;

import crc6488302ad6e9e4df1a.MauiApplication;
import java.util.ArrayList;
import mono.MonoPackageManager;
import mono.android.IGCUserPeer;

/* JADX INFO: loaded from: classes2.dex */
public class MainApplication extends MauiApplication implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    public MainApplication() {
        MonoPackageManager.setContext(this);
    }

    @Override // crc6488302ad6e9e4df1a.MauiApplication, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc6488302ad6e9e4df1a.MauiApplication, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
