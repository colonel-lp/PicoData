package com.google.crypto.tink.proto;

import com.google.crypto.tink.shaded.protobuf.Internal;

/* JADX INFO: loaded from: classes2.dex */
public enum HpkeKdf implements Internal.EnumLite {
    KDF_UNKNOWN(0),
    HKDF_SHA256(1),
    HKDF_SHA384(2),
    HKDF_SHA512(3),
    UNRECOGNIZED(-1);

    public static final int HKDF_SHA256_VALUE = 1;
    public static final int HKDF_SHA384_VALUE = 2;
    public static final int HKDF_SHA512_VALUE = 3;
    public static final int KDF_UNKNOWN_VALUE = 0;
    private static final Internal.EnumLiteMap<HpkeKdf> internalValueMap = new Internal.EnumLiteMap<HpkeKdf>() { // from class: com.google.crypto.tink.proto.HpkeKdf.1
        @Override // com.google.crypto.tink.shaded.protobuf.Internal.EnumLiteMap
        public HpkeKdf findValueByNumber(int number) {
            return HpkeKdf.forNumber(number);
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
    public static HpkeKdf valueOf(int value) {
        return forNumber(value);
    }

    public static HpkeKdf forNumber(int value) {
        if (value == 0) {
            return KDF_UNKNOWN;
        }
        if (value == 1) {
            return HKDF_SHA256;
        }
        if (value == 2) {
            return HKDF_SHA384;
        }
        if (value != 3) {
            return null;
        }
        return HKDF_SHA512;
    }

    public static Internal.EnumLiteMap<HpkeKdf> internalGetValueMap() {
        return internalValueMap;
    }

    public static Internal.EnumVerifier internalGetVerifier() {
        return HpkeKdfVerifier.INSTANCE;
    }

    private static final class HpkeKdfVerifier implements Internal.EnumVerifier {
        static final Internal.EnumVerifier INSTANCE = new HpkeKdfVerifier();

        private HpkeKdfVerifier() {
        }

        @Override // com.google.crypto.tink.shaded.protobuf.Internal.EnumVerifier
        public boolean isInRange(int number) {
            return HpkeKdf.forNumber(number) != null;
        }
    }

    HpkeKdf(int value) {
        this.value = value;
    }
}
