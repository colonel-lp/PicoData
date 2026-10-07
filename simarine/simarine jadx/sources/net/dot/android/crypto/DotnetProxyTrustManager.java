package net.dot.android.crypto;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/* JADX INFO: loaded from: classes2.dex */
public final class DotnetProxyTrustManager implements X509TrustManager {
    private final long sslStreamProxyHandle;

    static native boolean verifyRemoteCertificate(long j);

    public DotnetProxyTrustManager(long j) {
        this.sslStreamProxyHandle = j;
    }

    @Override // javax.net.ssl.X509TrustManager
    public void checkClientTrusted(X509Certificate[] x509CertificateArr, String str) throws CertificateException {
        if (!verifyRemoteCertificate(this.sslStreamProxyHandle)) {
            throw new CertificateException();
        }
    }

    @Override // javax.net.ssl.X509TrustManager
    public void checkServerTrusted(X509Certificate[] x509CertificateArr, String str) throws CertificateException {
        if (!verifyRemoteCertificate(this.sslStreamProxyHandle)) {
            throw new CertificateException();
        }
    }

    @Override // javax.net.ssl.X509TrustManager
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
