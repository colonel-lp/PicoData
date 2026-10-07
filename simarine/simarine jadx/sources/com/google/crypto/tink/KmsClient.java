package com.google.crypto.tink;

import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
public interface KmsClient {
    boolean doesSupport(String keyUri);

    Aead getAead(String keyUri) throws GeneralSecurityException;

    KmsClient withCredentials(String credentialPath) throws GeneralSecurityException;

    KmsClient withDefaultCredentials() throws GeneralSecurityException;
}
