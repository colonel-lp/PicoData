package crc6488302ad6e9e4df1a;

import android.app.Application;
import android.content.res.Configuration;
import java.util.ArrayList;
import mono.MonoPackageManager;
import mono.android.IGCUserPeer;

/* JADX INFO: loaded from: classes2.dex */
public abstract class MauiApplication extends Application implements IGCUserPeer {
    public static final String __md_methods = "n_onCreate:()V:GetOnCreateHandler\nn_onLowMemory:()V:GetOnLowMemoryHandler\nn_onTrimMemory:(I)V:GetOnTrimMemory_IHandler\nn_onConfigurationChanged:(Landroid/content/res/Configuration;)V:GetOnConfigurationChanged_Landroid_content_res_Configuration_Handler\n";
    private ArrayList refList;

    private native void n_onConfigurationChanged(Configuration configuration);

    private native void n_onCreate();

    private native void n_onLowMemory();

    private native void n_onTrimMemory(int i);

    public MauiApplication() {
        MonoPackageManager.setContext(this);
    }

    @Override // android.app.Application
    public void onCreate() {
        n_onCreate();
    }

    @Override // android.app.Application, android.content.ComponentCallbacks
    public void onLowMemory() {
        n_onLowMemory();
    }

    @Override // android.app.Application, android.content.ComponentCallbacks2
    public void onTrimMemory(int i) {
        n_onTrimMemory(i);
    }

    @Override // android.app.Application, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        n_onConfigurationChanged(configuration);
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
