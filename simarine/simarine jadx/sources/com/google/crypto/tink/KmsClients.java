package com.google.crypto.tink;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

/* JADX INFO: loaded from: classes2.dex */
public final class KmsClients {
    private static List<KmsClient> autoClients;
    private static final CopyOnWriteArrayList<KmsClient> clients = new CopyOnWriteArrayList<>();

    public static void add(KmsClient client) {
        clients.add(client);
    }

    public static KmsClient get(String keyUri) throws GeneralSecurityException {
        for (KmsClient kmsClient : clients) {
            if (kmsClient.doesSupport(keyUri)) {
                return kmsClient;
            }
        }
        throw new GeneralSecurityException("No KMS client does support: " + keyUri);
    }

    @Deprecated
    public static synchronized KmsClient getAutoLoaded(String keyUri) throws GeneralSecurityException {
        KmsClient next;
        if (autoClients == null) {
            autoClients = loadAutoKmsClients();
        }
        Iterator<KmsClient> it = autoClients.iterator();
        while (it.hasNext()) {
            next = it.next();
            if (next.doesSupport(keyUri)) {
            }
        }
        throw new GeneralSecurityException("No KMS client does support: " + keyUri);
        return next;
    }

    static void reset() {
        clients.clear();
    }

    private static List<KmsClient> loadAutoKmsClients() {
        ArrayList arrayList = new ArrayList();
        Iterator it = ServiceLoader.load(KmsClient.class).iterator();
        while (it.hasNext()) {
            arrayList.add((KmsClient) it.next());
        }
        return Collections.unmodifiableList(arrayList);
    }

    private KmsClients() {
    }
}
