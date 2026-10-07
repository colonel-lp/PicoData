package com.google.crypto.tink.jwt;

import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.util.Optional;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public interface JwtPublicKeySignInternal {
    String signAndEncodeWithKid(RawJwt token, Optional<String> kid) throws GeneralSecurityException;
}
