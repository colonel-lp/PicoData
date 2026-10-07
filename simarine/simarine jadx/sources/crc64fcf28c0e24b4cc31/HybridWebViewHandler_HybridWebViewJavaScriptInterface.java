package crc64fcf28c0e24b4cc31;

import android.webkit.JavascriptInterface;
import com.microsoft.maui.HybridJavaScriptInterface;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class HybridWebViewHandler_HybridWebViewJavaScriptInterface extends HybridJavaScriptInterface implements IGCUserPeer {
    public static final String __md_methods = "n_sendMessage:(Ljava/lang/String;)V:GetSendMessage_Ljava_lang_String_Handler\n";
    private ArrayList refList;

    private native void n_sendMessage(String str);

    static {
        Runtime.register("Microsoft.Maui.Handlers.HybridWebViewHandler+HybridWebViewJavaScriptInterface, Microsoft.Maui", HybridWebViewHandler_HybridWebViewJavaScriptInterface.class, __md_methods);
    }

    public HybridWebViewHandler_HybridWebViewJavaScriptInterface() {
        if (getClass() == HybridWebViewHandler_HybridWebViewJavaScriptInterface.class) {
            TypeManager.Activate("Microsoft.Maui.Handlers.HybridWebViewHandler+HybridWebViewJavaScriptInterface, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    @Override // com.microsoft.maui.HybridJavaScriptInterface
    @JavascriptInterface
    public void sendMessage(String str) {
        n_sendMessage(str);
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
