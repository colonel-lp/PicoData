package crc6452ffdc5b34af3a0f;

import android.graphics.Bitmap;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiWebViewClient extends WebViewClient implements IGCUserPeer {
    public static final String __md_methods = "n_shouldOverrideUrlLoading:(Landroid/webkit/WebView;Landroid/webkit/WebResourceRequest;)Z:GetShouldOverrideUrlLoading_Landroid_webkit_WebView_Landroid_webkit_WebResourceRequest_Handler\nn_onPageStarted:(Landroid/webkit/WebView;Ljava/lang/String;Landroid/graphics/Bitmap;)V:GetOnPageStarted_Landroid_webkit_WebView_Ljava_lang_String_Landroid_graphics_Bitmap_Handler\nn_onPageFinished:(Landroid/webkit/WebView;Ljava/lang/String;)V:GetOnPageFinished_Landroid_webkit_WebView_Ljava_lang_String_Handler\nn_onReceivedError:(Landroid/webkit/WebView;Landroid/webkit/WebResourceRequest;Landroid/webkit/WebResourceError;)V:GetOnReceivedError_Landroid_webkit_WebView_Landroid_webkit_WebResourceRequest_Landroid_webkit_WebResourceError_Handler\nn_onRenderProcessGone:(Landroid/webkit/WebView;Landroid/webkit/RenderProcessGoneDetail;)Z:GetOnRenderProcessGone_Landroid_webkit_WebView_Landroid_webkit_RenderProcessGoneDetail_Handler\n";
    private ArrayList refList;

    private native void n_onPageFinished(WebView webView, String str);

    private native void n_onPageStarted(WebView webView, String str, Bitmap bitmap);

    private native void n_onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError);

    private native boolean n_onRenderProcessGone(WebView webView, RenderProcessGoneDetail renderProcessGoneDetail);

    private native boolean n_shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest);

    static {
        Runtime.register("Microsoft.Maui.Platform.MauiWebViewClient, Microsoft.Maui", MauiWebViewClient.class, __md_methods);
    }

    public MauiWebViewClient() {
        if (getClass() == MauiWebViewClient.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiWebViewClient, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    @Override // android.webkit.WebViewClient
    public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
        return n_shouldOverrideUrlLoading(webView, webResourceRequest);
    }

    @Override // android.webkit.WebViewClient
    public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
        n_onPageStarted(webView, str, bitmap);
    }

    @Override // android.webkit.WebViewClient
    public void onPageFinished(WebView webView, String str) {
        n_onPageFinished(webView, str);
    }

    @Override // android.webkit.WebViewClient
    public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
        n_onReceivedError(webView, webResourceRequest, webResourceError);
    }

    @Override // android.webkit.WebViewClient
    public boolean onRenderProcessGone(WebView webView, RenderProcessGoneDetail renderProcessGoneDetail) {
        return n_onRenderProcessGone(webView, renderProcessGoneDetail);
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
