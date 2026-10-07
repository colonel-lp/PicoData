package crc6452ffdc5b34af3a0f;

import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiWebChromeClient extends WebChromeClient implements IGCUserPeer {
    public static final String __md_methods = "n_onShowFileChooser:(Landroid/webkit/WebView;Landroid/webkit/ValueCallback;Landroid/webkit/WebChromeClient$FileChooserParams;)Z:GetOnShowFileChooser_Landroid_webkit_WebView_Landroid_webkit_ValueCallback_Landroid_webkit_WebChromeClient_FileChooserParams_Handler\n";
    private ArrayList refList;

    private native boolean n_onShowFileChooser(WebView webView, ValueCallback valueCallback, WebChromeClient.FileChooserParams fileChooserParams);

    static {
        Runtime.register("Microsoft.Maui.Platform.MauiWebChromeClient, Microsoft.Maui", MauiWebChromeClient.class, __md_methods);
    }

    public MauiWebChromeClient() {
        if (getClass() == MauiWebChromeClient.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiWebChromeClient, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    @Override // android.webkit.WebChromeClient
    public boolean onShowFileChooser(WebView webView, ValueCallback valueCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        return n_onShowFileChooser(webView, valueCallback, fileChooserParams);
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
