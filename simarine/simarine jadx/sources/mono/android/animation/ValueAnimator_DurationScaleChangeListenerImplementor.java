package mono.android.animation;

import android.animation.ValueAnimator;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ValueAnimator_DurationScaleChangeListenerImplementor implements IGCUserPeer, ValueAnimator.DurationScaleChangeListener {
    public static final String __md_methods = "n_onChanged:(F)V:GetOnChanged_FHandler:Android.Animation.ValueAnimator/IDurationScaleChangeListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onChanged(float f);

    static {
        Runtime.register("Android.Animation.ValueAnimator+IDurationScaleChangeListenerImplementor, Mono.Android", ValueAnimator_DurationScaleChangeListenerImplementor.class, "n_onChanged:(F)V:GetOnChanged_FHandler:Android.Animation.ValueAnimator/IDurationScaleChangeListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n");
    }

    public ValueAnimator_DurationScaleChangeListenerImplementor() {
        if (getClass() == ValueAnimator_DurationScaleChangeListenerImplementor.class) {
            TypeManager.Activate("Android.Animation.ValueAnimator+IDurationScaleChangeListenerImplementor, Mono.Android", "", this, new Object[0]);
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
