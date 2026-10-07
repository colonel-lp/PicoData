package crc6452ffdc5b34af3a0f;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiHybridWebViewClient extends WebViewClient implements IGCUserPeer {
    public static final String __md_methods = "n_shouldInterceptRequest:(Landroid/webkit/WebView;Landroid/webkit/WebResourceRequest;)Landroid/webkit/WebResourceResponse;:GetShouldInterceptRequest_Landroid_webkit_WebView_Landroid_webkit_WebResourceRequest_Handler\n";
    private ArrayList refList;

    private native WebResourceResponse n_shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest);

    static {
        Runtime.register("Microsoft.Maui.Platform.MauiHybridWebViewClient, Microsoft.Maui", MauiHybridWebViewClient.class, __md_methods);
    }

    public MauiHybridWebViewClient() {
        if (getClass() == MauiHybridWebViewClient.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiHybridWebViewClient, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    @Override // android.webkit.WebViewClient
    public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
        return n_shouldInterceptRequest(webView, webResourceRequest);
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
