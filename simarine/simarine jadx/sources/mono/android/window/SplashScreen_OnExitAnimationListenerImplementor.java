package mono.android.window;

import android.window.SplashScreen;
import android.window.SplashScreenView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class SplashScreen_OnExitAnimationListenerImplementor implements IGCUserPeer, SplashScreen.OnExitAnimationListener {
    public static final String __md_methods = "n_onSplashScreenExit:(Landroid/window/SplashScreenView;)V:GetOnSplashScreenExit_Landroid_window_SplashScreenView_Handler:Android.Window.ISplashScreenOnExitAnimationListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onSplashScreenExit(SplashScreenView splashScreenView);

    static {
        Runtime.register("Android.Window.ISplashScreenOnExitAnimationListenerImplementor, Mono.Android", SplashScreen_OnExitAnimationListenerImplementor.class, __md_methods);
    }

    public SplashScreen_OnExitAnimationListenerImplementor() {
        if (getClass() == SplashScreen_OnExitAnimationListenerImplementor.class) {
            TypeManager.Activate("Android.Window.ISplashScreenOnExitAnimationListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.window.SplashScreen.OnExitAnimationListener
    public void onSplashScreenExit(SplashScreenView splashScreenView) {
        n_onSplashScreenExit(splashScreenView);
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
