package com.google.crypto.tink.proto;

import com.google.crypto.tink.shaded.protobuf.Internal;

/* JADX INFO: loaded from: classes2.dex */
public enum JwtHmacAlgorithm implements Internal.EnumLite {
    HS_UNKNOWN(0),
    HS256(1),
    HS384(2),
    HS512(3),
    UNRECOGNIZED(-1);

    public static final int HS256_VALUE = 1;
    public static final int HS384_VALUE = 2;
    public static final int HS512_VALUE = 3;
    public static final int HS_UNKNOWN_VALUE = 0;
    private static final Internal.EnumLiteMap<JwtHmacAlgorithm> internalValueMap = new Internal.EnumLiteMap<JwtHmacAlgorithm>() { // from class: com.google.crypto.tink.proto.JwtHmacAlgorithm.1
        @Override // com.google.crypto.tink.shaded.protobuf.Internal.EnumLiteMap
        public JwtHmacAlgorithm findValueByNumber(int number) {
            return JwtHmacAlgorithm.forNumber(number);
        }
    };
    private final int value;

    @Override // com.google.crypto.tink.shaded.protobuf.Internal.EnumLite
    public final int getNumber() {
        if (this == UNRECOGNIZED) {
            throw new IllegalArgumentException("Can't get the number of an unknown enum value.");
        }
        return this.value;
    }

    @Deprecated
    public static JwtHmacAlgorithm valueOf(int value) {
        return forNumber(value);
    }

    public static JwtHmacAlgorithm forNumber(int value) {
        if (value == 0) {
            return HS_UNKNOWN;
        }
        if (value == 1) {
            return HS256;
        }
        if (value == 2) {
            return HS384;
        }
        if (value != 3) {
            return null;
        }
        return HS512;
    }

    public static Internal.EnumLiteMap<JwtHmacAlgorithm> internalGetValueMap() {
        return internalValueMap;
    }

    public static Internal.EnumVerifier internalGetVerifier() {
        return JwtHmacAlgorithmVerifier.INSTANCE;
    }

    private static final class JwtHmacAlgorithmVerifier implements Internal.EnumVerifier {
        static final Internal.EnumVerifier INSTANCE = new JwtHmacAlgorithmVerifier();

        private JwtHmacAlgorithmVerifier() {
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Internal.EnumVerifier
        public boolean isInRange(int number) {
            return JwtHmacAlgorithm.forNumber(number) != null;
        }
    }

    JwtHmacAlgorithm(int value) {
        this.value = value;
    }
}
