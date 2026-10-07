package com.google.crypto.tink.proto;

import com.google.crypto.tink.shaded.protobuf.Internal;

/* JADX INFO: loaded from: classes2.dex */
public enum HpkeKem implements Internal.EnumLite {
    KEM_UNKNOWN(0),
    DHKEM_X25519_HKDF_SHA256(1),
    DHKEM_P256_HKDF_SHA256(2),
    DHKEM_P384_HKDF_SHA384(3),
    DHKEM_P521_HKDF_SHA512(4),
    UNRECOGNIZED(-1);

    public static final int DHKEM_P256_HKDF_SHA256_VALUE = 2;
    public static final int DHKEM_P384_HKDF_SHA384_VALUE = 3;
    public static final int DHKEM_P521_HKDF_SHA512_VALUE = 4;
    public static final int DHKEM_X25519_HKDF_SHA256_VALUE = 1;
    public static final int KEM_UNKNOWN_VALUE = 0;
    private static final Internal.EnumLiteMap<HpkeKem> internalValueMap = new Internal.EnumLiteMap<HpkeKem>() { // from class: com.google.crypto.tink.proto.HpkeKem.1
        @Override // com.google.crypto.tink.shaded.protobuf.Internal.EnumLiteMap
        public HpkeKem findValueByNumber(int number) {
            return HpkeKem.forNumber(number);
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
    public static HpkeKem valueOf(int value) {
        return forNumber(value);
    }

    public static HpkeKem forNumber(int value) {
        if (value == 0) {
            return KEM_UNKNOWN;
        }
        if (value == 1) {
            return DHKEM_X25519_HKDF_SHA256;
        }
        if (value == 2) {
            return DHKEM_P256_HKDF_SHA256;
        }
        if (value == 3) {
            return DHKEM_P384_HKDF_SHA384;
        }
        if (value != 4) {
            return null;
        }
        return DHKEM_P521_HKDF_SHA512;
    }

    public static Internal.EnumLiteMap<HpkeKem> internalGetValueMap() {
        return internalValueMap;
    }

    public static Internal.EnumVerifier internalGetVerifier() {
        return HpkeKemVerifier.INSTANCE;
    }

    private static final class HpkeKemVerifier implements Internal.EnumVerifier {
        static final Internal.EnumVerifier INSTANCE = new HpkeKemVerifier();

        private HpkeKemVerifier() {
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Internal.EnumVerifier
        public boolean isInRange(int number) {
            return HpkeKem.forNumber(number) != null;
        }
    }

    HpkeKem(int value) {
        this.value = value;
    }
}
