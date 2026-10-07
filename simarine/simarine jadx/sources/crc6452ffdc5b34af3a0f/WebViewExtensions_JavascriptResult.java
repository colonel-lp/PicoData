package crc6452ffdc5b34af3a0f;

import android.webkit.ValueCallback;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class WebViewExtensions_JavascriptResult implements IGCUserPeer, ValueCallback {
    public static final String __md_methods = "n_onReceiveValue:(Ljava/lang/Object;)V:GetOnReceiveValue_Ljava_lang_Object_Handler:Android.Webkit.IValueCallbackInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onReceiveValue(Object obj);

    static {
        Runtime.register("Microsoft.Maui.Platform.WebViewExtensions+JavascriptResult, Microsoft.Maui", WebViewExtensions_JavascriptResult.class, __md_methods);
    }

    public WebViewExtensions_JavascriptResult() {
        if (getClass() == WebViewExtensions_JavascriptResult.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.WebViewExtensions+JavascriptResult, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    @Override // android.webkit.ValueCallback
    public void onReceiveValue(Object obj) {
        n_onReceiveValue(obj);
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
