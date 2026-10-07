package mono.android.speech;

import android.speech.ModelDownloadListener;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ModelDownloadListenerImplementor implements IGCUserPeer, ModelDownloadListener {
    public static final String __md_methods = "n_onError:(I)V:GetOnError_IHandler:Android.Speech.IModelDownloadListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onProgress:(I)V:GetOnProgress_IHandler:Android.Speech.IModelDownloadListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onScheduled:()V:GetOnScheduledHandler:Android.Speech.IModelDownloadListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onSuccess:()V:GetOnSuccessHandler:Android.Speech.IModelDownloadListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onError(int i);

    private native void n_onProgress(int i);

    private native void n_onScheduled();

    private native void n_onSuccess();

    static {
        Runtime.register("Android.Speech.IModelDownloadListenerImplementor, Mono.Android", ModelDownloadListenerImplementor.class, __md_methods);
    }

    public ModelDownloadListenerImplementor() {
        if (getClass() == ModelDownloadListenerImplementor.class) {
            TypeManager.Activate("Android.Speech.IModelDownloadListenerImplementor, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.speech.ModelDownloadListener
    public void onError(int i) {
        n_onError(i);
    }

    @Override // android.speech.ModelDownloadListener
    public void onProgress(int i) {
        n_onProgress(i);
    }

    @Override // android.speech.ModelDownloadListener
    public void onScheduled() {
        n_onScheduled();
    }

    @Override // android.speech.ModelDownloadListener
    public void onSuccess() {
        n_onSuccess();
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
