package com.google.crypto.tink;

import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.Immutable;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
@CheckReturnValue
public final class InsecureSecretKeyAccess {
    private InsecureSecretKeyAccess() {
    }

    public static SecretKeyAccess get() {
        return SecretKeyAccess.instance();
    }
}
