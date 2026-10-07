package com.google.crypto.tink.proto;

import com.google.crypto.tink.shaded.protobuf.Internal;

/* JADX INFO: loaded from: classes2.dex */
public enum JwtRsaSsaPkcs1Algorithm implements Internal.EnumLite {
    RS_UNKNOWN(0),
    RS256(1),
    RS384(2),
    RS512(3),
    UNRECOGNIZED(-1);

    public static final int RS256_VALUE = 1;
    public static final int RS384_VALUE = 2;
    public static final int RS512_VALUE = 3;
    public static final int RS_UNKNOWN_VALUE = 0;
    private static final Internal.EnumLiteMap<JwtRsaSsaPkcs1Algorithm> internalValueMap = new Internal.EnumLiteMap<JwtRsaSsaPkcs1Algorithm>() { // from class: com.google.crypto.tink.proto.JwtRsaSsaPkcs1Algorithm.1
        @Override // com.google.crypto.tink.shaded.protobuf.Internal.EnumLiteMap
        public JwtRsaSsaPkcs1Algorithm findValueByNumber(int number) {
            return JwtRsaSsaPkcs1Algorithm.forNumber(number);
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
    public static JwtRsaSsaPkcs1Algorithm valueOf(int value) {
        return forNumber(value);
    }

    public static JwtRsaSsaPkcs1Algorithm forNumber(int value) {
        if (value == 0) {
            return RS_UNKNOWN;
        }
        if (value == 1) {
            return RS256;
        }
        if (value == 2) {
            return RS384;
        }
        if (value != 3) {
            return null;
        }
        return RS512;
    }

    public static Internal.EnumLiteMap<JwtRsaSsaPkcs1Algorithm> internalGetValueMap() {
        return internalValueMap;
    }

    public static Internal.EnumVerifier internalGetVerifier() {
        return JwtRsaSsaPkcs1AlgorithmVerifier.INSTANCE;
    }

    private static final class JwtRsaSsaPkcs1AlgorithmVerifier implements Internal.EnumVerifier {
        static final Internal.EnumVerifier INSTANCE = new JwtRsaSsaPkcs1AlgorithmVerifier();

        private JwtRsaSsaPkcs1AlgorithmVerifier() {
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Internal.EnumVerifier
        public boolean isInRange(int number) {
            return JwtRsaSsaPkcs1Algorithm.forNumber(number) != null;
        }
    }

    JwtRsaSsaPkcs1Algorithm(int value) {
        this.value = value;
    }
}
