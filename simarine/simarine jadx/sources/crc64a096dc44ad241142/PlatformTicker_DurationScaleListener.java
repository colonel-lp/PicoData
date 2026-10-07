package crc64a096dc44ad241142;

import android.animation.ValueAnimator;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class PlatformTicker_DurationScaleListener implements IGCUserPeer, ValueAnimator.DurationScaleChangeListener {
    public static final String __md_methods = "n_onChanged:(F)V:GetOnChanged_FHandler:Android.Animation.ValueAnimator/IDurationScaleChangeListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onChanged(float f);

    static {
        Runtime.register("Microsoft.Maui.Animations.PlatformTicker+DurationScaleListener, Microsoft.Maui", PlatformTicker_DurationScaleListener.class, "n_onChanged:(F)V:GetOnChanged_FHandler:Android.Animation.ValueAnimator/IDurationScaleChangeListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n");
    }

    public PlatformTicker_DurationScaleListener() {
        if (getClass() == PlatformTicker_DurationScaleListener.class) {
            TypeManager.Activate("Microsoft.Maui.Animations.PlatformTicker+DurationScaleListener, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    @Override // android.animation.ValueAnimator.DurationScaleChangeListener
    public void onChanged(float f) {
        n_onChanged(f);
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
