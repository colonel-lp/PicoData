package com.google.crypto.tink.hybrid.internal;

import com.google.crypto.tink.subtle.Bytes;
import com.google.crypto.tink.subtle.EllipticCurves;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
final class NistCurvesHpkeKem implements HpkeKem {
    private final EllipticCurves.CurveType curve;
    private final HkdfHpkeKdf hkdf;

    /* JADX INFO: renamed from: com.google.crypto.tink.hybrid.internal.NistCurvesHpkeKem$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$CurveType;

        static {
            int[] iArr = new int[EllipticCurves.CurveType.values().length];
            $SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$CurveType = iArr;
            try {
                iArr[EllipticCurves.CurveType.NIST_P256.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$CurveType[EllipticCurves.CurveType.NIST_P384.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$CurveType[EllipticCurves.CurveType.NIST_P521.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    static NistCurvesHpkeKem fromCurve(EllipticCurves.CurveType curve) throws GeneralSecurityException {
        int i = AnonymousClass1.$SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$CurveType[curve.ordinal()];
        if (i == 1) {
            return new NistCurvesHpkeKem(new HkdfHpkeKdf("HmacSha256"), EllipticCurves.CurveType.NIST_P256);
        }
        if (i == 2) {
            return new NistCurvesHpkeKem(new HkdfHpkeKdf("HmacSha384"), EllipticCurves.CurveType.NIST_P384);
        }
        if (i == 3) {
            return new NistCurvesHpkeKem(new HkdfHpkeKdf("HmacSha512"), EllipticCurves.CurveType.NIST_P521);
        }
        throw new GeneralSecurityException("invalid curve type: " + curve);
    }

    private NistCurvesHpkeKem(HkdfHpkeKdf hkdf, EllipticCurves.CurveType curve) {
        this.hkdf = hkdf;
        this.curve = curve;
    }

    private byte[] deriveKemSharedSecret(byte[] dhSharedSecret, byte[] senderEphemeralPublicKey, byte[] recipientPublicKey) throws GeneralSecurityException {
        return extractAndExpand(dhSharedSecret, Bytes.concat(senderEphemeralPublicKey, recipientPublicKey));
    }

    private byte[] deriveKemSharedSecret(byte[] dhSharedSecret, byte[] senderEphemeralPublicKey, byte[] recipientPublicKey, byte[] senderPublicKey) throws GeneralSecurityException {
        return extractAndExpand(dhSharedSecret, Bytes.concat(senderEphemeralPublicKey, recipientPublicKey, senderPublicKey));
    }

    private byte[] extractAndExpand(byte[] dhSharedSecret, byte[] kemContext) throws GeneralSecurityException {
        byte[] bArrKemSuiteId = HpkeUtil.kemSuiteId(getKemId());
        HkdfHpkeKdf hkdfHpkeKdf = this.hkdf;
        return hkdfHpkeKdf.extractAndExpand(null, dhSharedSecret, "eae_prk", kemContext, "shared_secret", bArrKemSuiteId, hkdfHpkeKdf.getMacLength());
    }

    HpkeKemEncapOutput encapsulate(byte[] recipientPublicKey, KeyPair senderEphemeralKeyPair) throws GeneralSecurityException {
        byte[] bArrComputeSharedSecret = EllipticCurves.computeSharedSecret((ECPrivateKey) senderEphemeralKeyPair.getPrivate(), EllipticCurves.getEcPublicKey(this.curve, EllipticCurves.PointFormatType.UNCOMPRESSED, recipientPublicKey));
        byte[] bArrPointEncode = EllipticCurves.pointEncode(this.curve, EllipticCurves.PointFormatType.UNCOMPRESSED, ((ECPublicKey) senderEphemeralKeyPair.getPublic()).getW());
        return new HpkeKemEncapOutput(deriveKemSharedSecret(bArrComputeSharedSecret, bArrPointEncode, recipientPublicKey), bArrPointEncode);
    }

    @Override // com.google.crypto.tink.hybrid.internal.HpkeKem
    public HpkeKemEncapOutput encapsulate(byte[] recipientPublicKey) throws GeneralSecurityException {
        return encapsulate(recipientPublicKey, EllipticCurves.generateKeyPair(this.curve));
    }

    HpkeKemEncapOutput authEncapsulate(byte[] recipientPublicKey, KeyPair senderEphemeralKeyPair, HpkeKemPrivateKey senderPrivateKey) throws GeneralSecurityException {
        ECPublicKey ecPublicKey = EllipticCurves.getEcPublicKey(this.curve, EllipticCurves.PointFormatType.UNCOMPRESSED, recipientPublicKey);
        byte[] bArrConcat = Bytes.concat(EllipticCurves.computeSharedSecret((ECPrivateKey) senderEphemeralKeyPair.getPrivate(), ecPublicKey), EllipticCurves.computeSharedSecret(EllipticCurves.getEcPrivateKey(this.curve, senderPrivateKey.getSerializedPrivate().toByteArray()), ecPublicKey));
        byte[] bArrPointEncode = EllipticCurves.pointEncode(this.curve, EllipticCurves.PointFormatType.UNCOMPRESSED, ((ECPublicKey) senderEphemeralKeyPair.getPublic()).getW());
        return new HpkeKemEncapOutput(deriveKemSharedSecret(bArrConcat, bArrPointEncode, recipientPublicKey, senderPrivateKey.getSerializedPublic().toByteArray()), bArrPointEncode);
    }

    @Override // com.google.crypto.tink.hybrid.internal.HpkeKem
    public HpkeKemEncapOutput authEncapsulate(byte[] recipientPublicKey, HpkeKemPrivateKey senderPrivateKey) throws GeneralSecurityException {
        return authEncapsulate(recipientPublicKey, EllipticCurves.generateKeyPair(this.curve), senderPrivateKey);
    }

    @Override // com.google.crypto.tink.hybrid.internal.HpkeKem
    public byte[] decapsulate(byte[] encapsulatedKey, HpkeKemPrivateKey recipientPrivateKey) throws GeneralSecurityException {
        return deriveKemSharedSecret(EllipticCurves.computeSharedSecret(EllipticCurves.getEcPrivateKey(this.curve, recipientPrivateKey.getSerializedPrivate().toByteArray()), EllipticCurves.getEcPublicKey(this.curve, EllipticCurves.PointFormatType.UNCOMPRESSED, encapsulatedKey)), encapsulatedKey, recipientPrivateKey.getSerializedPublic().toByteArray());
    }

    @Override // com.google.crypto.tink.hybrid.internal.HpkeKem
    public byte[] authDecapsulate(byte[] encapsulatedKey, HpkeKemPrivateKey recipientPrivateKey, byte[] senderPublicKey) throws GeneralSecurityException {
        ECPrivateKey ecPrivateKey = EllipticCurves.getEcPrivateKey(this.curve, recipientPrivateKey.getSerializedPrivate().toByteArray());
        return deriveKemSharedSecret(Bytes.concat(EllipticCurves.computeSharedSecret(ecPrivateKey, EllipticCurves.getEcPublicKey(this.curve, EllipticCurves.PointFormatType.UNCOMPRESSED, encapsulatedKey)), EllipticCurves.computeSharedSecret(ecPrivateKey, EllipticCurves.getEcPublicKey(this.curve, EllipticCurves.PointFormatType.UNCOMPRESSED, senderPublicKey))), encapsulatedKey, recipientPrivateKey.getSerializedPublic().toByteArray(), senderPublicKey);
    }

    @Override // com.google.crypto.tink.hybrid.internal.HpkeKem
    public byte[] getKemId() throws GeneralSecurityException {
        int i = AnonymousClass1.$SwitchMap$com$google$crypto$tink$subtle$EllipticCurves$CurveType[this.curve.ordinal()];
        if (i == 1) {
            return HpkeUtil.P256_HKDF_SHA256_KEM_ID;
        }
        if (i == 2) {
            return HpkeUtil.P384_HKDF_SHA384_KEM_ID;
        }
        if (i == 3) {
            return HpkeUtil.P521_HKDF_SHA512_KEM_ID;
        }
        throw new GeneralSecurityException("Could not determine HPKE KEM ID");
    }
}
