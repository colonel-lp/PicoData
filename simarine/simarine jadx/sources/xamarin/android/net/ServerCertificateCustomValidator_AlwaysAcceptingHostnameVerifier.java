package xamarin.android.net;

import java.util.ArrayList;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ServerCertificateCustomValidator_AlwaysAcceptingHostnameVerifier implements IGCUserPeer, HostnameVerifier {
    public static final String __md_methods = "n_verify:(Ljava/lang/String;Ljavax/net/ssl/SSLSession;)Z:GetVerify_Ljava_lang_String_Ljavax_net_ssl_SSLSession_Handler:Javax.Net.Ssl.IHostnameVerifierInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native boolean n_verify(String str, SSLSession sSLSession);

    static {
        Runtime.register("Xamarin.Android.Net.ServerCertificateCustomValidator+AlwaysAcceptingHostnameVerifier, Mono.Android", ServerCertificateCustomValidator_AlwaysAcceptingHostnameVerifier.class, __md_methods);
    }

    public ServerCertificateCustomValidator_AlwaysAcceptingHostnameVerifier() {
        if (getClass() == ServerCertificateCustomValidator_AlwaysAcceptingHostnameVerifier.class) {
            TypeManager.Activate("Xamarin.Android.Net.ServerCertificateCustomValidator+AlwaysAcceptingHostnameVerifier, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // javax.net.ssl.HostnameVerifier
    public boolean verify(String str, SSLSession sSLSession) {
        return n_verify(str, sSLSession);
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
