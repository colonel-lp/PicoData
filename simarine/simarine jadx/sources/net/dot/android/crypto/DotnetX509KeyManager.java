package net.dot.android.crypto;

import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import javax.net.ssl.X509KeyManager;

/* JADX INFO: loaded from: classes2.dex */
public final class DotnetX509KeyManager implements X509KeyManager {
    private static final String CLIENT_CERTIFICATE_ALIAS = "DOTNET_SSLStream_ClientCertificateContext";
    private final X509Certificate[] certificateChain;
    private final PrivateKey privateKey;

    @Override // javax.net.ssl.X509KeyManager
    public String chooseServerAlias(String str, Principal[] principalArr, Socket socket) {
        return null;
    }

    public DotnetX509KeyManager(KeyStore.PrivateKeyEntry privateKeyEntry) {
        if (privateKeyEntry == null) {
            throw new IllegalArgumentException("PrivateKeyEntry must not be null");
        }
        this.privateKey = privateKeyEntry.getPrivateKey();
        Certificate[] certificateChain = privateKeyEntry.getCertificateChain();
        ArrayList arrayList = new ArrayList();
        for (Certificate certificate : certificateChain) {
            if (certificate instanceof X509Certificate) {
                arrayList.add((X509Certificate) certificate);
            }
        }
        if (arrayList.size() == 0) {
            throw new IllegalArgumentException("No valid X509 certificates found in the chain");
        }
        this.certificateChain = (X509Certificate[]) arrayList.toArray(new X509Certificate[0]);
    }

    @Override // javax.net.ssl.X509KeyManager
    public String[] getClientAliases(String str, Principal[] principalArr) {
        return new String[]{CLIENT_CERTIFICATE_ALIAS};
    }

    @Override // javax.net.ssl.X509KeyManager
    public String chooseClientAlias(String[] strArr, Principal[] principalArr, Socket socket) {
        return CLIENT_CERTIFICATE_ALIAS;
    }

    @Override // javax.net.ssl.X509KeyManager
    public String[] getServerAliases(String str, Principal[] principalArr) {
        return new String[0];
    }

    @Override // javax.net.ssl.X509KeyManager
    public X509Certificate[] getCertificateChain(String str) {
        return this.certificateChain;
    }

    @Override // javax.net.ssl.X509KeyManager
    public PrivateKey getPrivateKey(String str) {
        return this.privateKey;
    }
}
