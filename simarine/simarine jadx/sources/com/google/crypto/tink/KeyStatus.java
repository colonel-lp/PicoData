package com.google.crypto.tink;

import com.google.errorprone.annotations.Immutable;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class KeyStatus {
    private final String name;
    public static final KeyStatus ENABLED = new KeyStatus("ENABLED");
    public static final KeyStatus DISABLED = new KeyStatus("DISABLED");
    public static final KeyStatus DESTROYED = new KeyStatus("DESTROYED");

    private KeyStatus(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
