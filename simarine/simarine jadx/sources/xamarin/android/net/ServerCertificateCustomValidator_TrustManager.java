package xamarin.android.net;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ServerCertificateCustomValidator_TrustManager implements IGCUserPeer, X509TrustManager, TrustManager {
    public static final String __md_methods = "n_checkClientTrusted:([Ljava/security/cert/X509Certificate;Ljava/lang/String;)V:GetCheckClientTrusted_arrayLjava_security_cert_X509Certificate_Ljava_lang_String_Handler:Javax.Net.Ssl.IX509TrustManagerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_checkServerTrusted:([Ljava/security/cert/X509Certificate;Ljava/lang/String;)V:GetCheckServerTrusted_arrayLjava_security_cert_X509Certificate_Ljava_lang_String_Handler:Javax.Net.Ssl.IX509TrustManagerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getAcceptedIssuers:()[Ljava/security/cert/X509Certificate;:GetGetAcceptedIssuersHandler:Javax.Net.Ssl.IX509TrustManagerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_checkClientTrusted(X509Certificate[] x509CertificateArr, String str);

    private native void n_checkServerTrusted(X509Certificate[] x509CertificateArr, String str);

    private native X509Certificate[] n_getAcceptedIssuers();

    static {
        Runtime.register("Xamarin.Android.Net.ServerCertificateCustomValidator+TrustManager, Mono.Android", ServerCertificateCustomValidator_TrustManager.class, __md_methods);
    }

    public ServerCertificateCustomValidator_TrustManager() {
        if (getClass() == ServerCertificateCustomValidator_TrustManager.class) {
            TypeManager.Activate("Xamarin.Android.Net.ServerCertificateCustomValidator+TrustManager, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // javax.net.ssl.X509TrustManager
    public void checkClientTrusted(X509Certificate[] x509CertificateArr, String str) {
        n_checkClientTrusted(x509CertificateArr, str);
    }

    @Override // javax.net.ssl.X509TrustManager
    public void checkServerTrusted(X509Certificate[] x509CertificateArr, String str) {
        n_checkServerTrusted(x509CertificateArr, str);
    }

    @Override // javax.net.ssl.X509TrustManager
    public X509Certificate[] getAcceptedIssuers() {
        return n_getAcceptedIssuers();
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
