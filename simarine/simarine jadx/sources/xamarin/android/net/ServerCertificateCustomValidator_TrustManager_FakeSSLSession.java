package xamarin.android.net;

import java.security.Principal;
import java.security.cert.Certificate;
import java.util.ArrayList;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.security.cert.X509Certificate;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class ServerCertificateCustomValidator_TrustManager_FakeSSLSession implements IGCUserPeer, SSLSession {
    public static final String __md_methods = "n_getApplicationBufferSize:()I:GetGetApplicationBufferSizeHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getCipherSuite:()Ljava/lang/String;:GetGetCipherSuiteHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getCreationTime:()J:GetGetCreationTimeHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_isValid:()Z:GetIsValidHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getLastAccessedTime:()J:GetGetLastAccessedTimeHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getLocalPrincipal:()Ljava/security/Principal;:GetGetLocalPrincipalHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getPacketBufferSize:()I:GetGetPacketBufferSizeHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getPeerHost:()Ljava/lang/String;:GetGetPeerHostHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getPeerPort:()I:GetGetPeerPortHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getPeerPrincipal:()Ljava/security/Principal;:GetGetPeerPrincipalHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getProtocol:()Ljava/lang/String;:GetGetProtocolHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getSessionContext:()Ljavax/net/ssl/SSLSessionContext;:GetGetSessionContextHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getId:()[B:GetGetIdHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getLocalCertificates:()[Ljava/security/cert/Certificate;:GetGetLocalCertificatesHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getPeerCertificateChain:()[Ljavax/security/cert/X509Certificate;:GetGetPeerCertificateChainHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getPeerCertificates:()[Ljava/security/cert/Certificate;:GetGetPeerCertificatesHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getValue:(Ljava/lang/String;)Ljava/lang/Object;:GetGetValue_Ljava_lang_String_Handler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getValueNames:()[Ljava/lang/String;:GetGetValueNamesHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_invalidate:()V:GetInvalidateHandler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_putValue:(Ljava/lang/String;Ljava/lang/Object;)V:GetPutValue_Ljava_lang_String_Ljava_lang_Object_Handler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_removeValue:(Ljava/lang/String;)V:GetRemoveValue_Ljava_lang_String_Handler:Javax.Net.Ssl.ISSLSessionInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native int n_getApplicationBufferSize();

    private native String n_getCipherSuite();

    private native long n_getCreationTime();

    private native byte[] n_getId();

    private native long n_getLastAccessedTime();

    private native Certificate[] n_getLocalCertificates();

    private native Principal n_getLocalPrincipal();

    private native int n_getPacketBufferSize();

    private native X509Certificate[] n_getPeerCertificateChain();

    private native Certificate[] n_getPeerCertificates();

    private native String n_getPeerHost();

    private native int n_getPeerPort();

    private native Principal n_getPeerPrincipal();

    private native String n_getProtocol();

    private native SSLSessionContext n_getSessionContext();

    private native Object n_getValue(String str);

    private native String[] n_getValueNames();

    private native void n_invalidate();

    private native boolean n_isValid();

    private native void n_putValue(String str, Object obj);

    private native void n_removeValue(String str);

    static {
        Runtime.register("Xamarin.Android.Net.ServerCertificateCustomValidator+TrustManager+FakeSSLSession, Mono.Android", ServerCertificateCustomValidator_TrustManager_FakeSSLSession.class, __md_methods);
    }

    public ServerCertificateCustomValidator_TrustManager_FakeSSLSession() {
        if (getClass() == ServerCertificateCustomValidator_TrustManager_FakeSSLSession.class) {
            TypeManager.Activate("Xamarin.Android.Net.ServerCertificateCustomValidator+TrustManager+FakeSSLSession, Mono.Android", "", this, new Object[0]);
        }
    }

    public ServerCertificateCustomValidator_TrustManager_FakeSSLSession(java.security.cert.X509Certificate[] x509CertificateArr) {
        if (getClass() == ServerCertificateCustomValidator_TrustManager_FakeSSLSession.class) {
            TypeManager.Activate("Xamarin.Android.Net.ServerCertificateCustomValidator+TrustManager+FakeSSLSession, Mono.Android", "Java.Security.Cert.X509Certificate[], Mono.Android", this, new Object[]{x509CertificateArr});
        }
    }

    @Override // javax.net.ssl.SSLSession
    public int getApplicationBufferSize() {
        return n_getApplicationBufferSize();
    }

    @Override // javax.net.ssl.SSLSession
    public String getCipherSuite() {
        return n_getCipherSuite();
    }

    @Override // javax.net.ssl.SSLSession
    public long getCreationTime() {
        return n_getCreationTime();
    }

    @Override // javax.net.ssl.SSLSession
    public boolean isValid() {
        return n_isValid();
    }

    @Override // javax.net.ssl.SSLSession
    public long getLastAccessedTime() {
        return n_getLastAccessedTime();
    }

    @Override // javax.net.ssl.SSLSession
    public Principal getLocalPrincipal() {
        return n_getLocalPrincipal();
    }

    @Override // javax.net.ssl.SSLSession
    public int getPacketBufferSize() {
        return n_getPacketBufferSize();
    }

    @Override // javax.net.ssl.SSLSession
    public String getPeerHost() {
        return n_getPeerHost();
    }

    @Override // javax.net.ssl.SSLSession
    public int getPeerPort() {
        return n_getPeerPort();
    }

    @Override // javax.net.ssl.SSLSession
    public Principal getPeerPrincipal() {
        return n_getPeerPrincipal();
    }

    @Override // javax.net.ssl.SSLSession
    public String getProtocol() {
        return n_getProtocol();
    }

    @Override // javax.net.ssl.SSLSession
    public SSLSessionContext getSessionContext() {
        return n_getSessionContext();
    }

    @Override // javax.net.ssl.SSLSession
    public byte[] getId() {
        return n_getId();
    }

    @Override // javax.net.ssl.SSLSession
    public Certificate[] getLocalCertificates() {
        return n_getLocalCertificates();
    }

    @Override // javax.net.ssl.SSLSession
    public X509Certificate[] getPeerCertificateChain() {
        return n_getPeerCertificateChain();
    }

    @Override // javax.net.ssl.SSLSession
    public Certificate[] getPeerCertificates() {
        return n_getPeerCertificates();
    }

    @Override // javax.net.ssl.SSLSession
    public Object getValue(String str) {
        return n_getValue(str);
    }

    @Override // javax.net.ssl.SSLSession
    public String[] getValueNames() {
        return n_getValueNames();
    }

    @Override // javax.net.ssl.SSLSession
    public void invalidate() {
        n_invalidate();
    }

    @Override // javax.net.ssl.SSLSession
    public void putValue(String str, Object obj) {
        n_putValue(str, obj);
    }

    @Override // javax.net.ssl.SSLSession
    public void removeValue(String str) {
        n_removeValue(str);
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
