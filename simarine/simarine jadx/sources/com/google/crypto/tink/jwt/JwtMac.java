package com.google.crypto.tink.jwt;

import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public interface JwtMac {
    String computeMacAndEncode(RawJwt token) throws GeneralSecurityException;

    VerifiedJwt verifyMacAndDecode(String compact, JwtValidator validator) throws GeneralSecurityException;
}
