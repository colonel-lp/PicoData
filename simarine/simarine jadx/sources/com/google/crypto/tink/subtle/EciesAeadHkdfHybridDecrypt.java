package com.google.crypto.tink.subtle;

import com.google.crypto.tink.HybridDecrypt;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.hybrid.EciesPrivateKey;
import com.google.crypto.tink.hybrid.internal.EciesDemHelper;
import com.google.crypto.tink.internal.BigIntegerEncoding;
import com.google.crypto.tink.internal.Util;
import java.security.GeneralSecurityException;
import java.security.interfaces.ECPrivateKey;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
public final class EciesAeadHkdfHybridDecrypt implements HybridDecrypt {
    private final EciesDemHelper.Dem dem;
    private final EllipticCurves.PointFormatType ecPointFormat;
    private final String hkdfHmacAlgo;
    private final byte[] hkdfSalt;
    private final byte[] outputPrefix;
    private final EciesHkdfRecipientKem recipientKem;
    private final ECPrivateKey recipientPrivateKey;

    private EciesAeadHkdfHybridDecrypt(final ECPrivateKey recipientPrivateKey, final byte[] hkdfSalt, String hkdfHmacAlgo, EllipticCurves.PointFormatType ecPointFormat, EciesDemHelper.Dem dem, byte[] outputPrefix) {
        this.recipientPrivateKey = recipientPrivateKey;
        this.recipientKem = new EciesHkdfRecipientKem(recipientPrivateKey);
        this.hkdfSalt = hkdfSalt;
        this.hkdfHmacAlgo = hkdfHmacAlgo;
        this.ecPointFormat = ecPointFormat;
        this.dem = dem;
        this.outputPrefix = outputPrefix;
    }

    public static HybridDecrypt create(EciesPrivateKey key) throws GeneralSecurityException {
        ECPrivateKey ecPrivateKey = EllipticCurves.getEcPrivateKey((EllipticCurves.CurveType) EciesAeadHkdfHybridEncrypt.CURVE_TYPE_CONVERTER.toProtoEnum(key.getParameters().getCurveType()), BigIntegerEncoding.toBigEndianBytes(key.getNistPrivateKeyValue().getBigInteger(InsecureSecretKeyAccess.get())));
        byte[] byteArray = new byte[0];
        if (key.getParameters().getSalt() != null) {
            byteArray = key.getParameters().getSalt().toByteArray();
        }
        return new EciesAeadHkdfHybridDecrypt(ecPrivateKey, byteArray, EciesAeadHkdfHybridEncrypt.toHmacAlgo(key.getParameters().getHashType()), (EllipticCurves.PointFormatType) EciesAeadHkdfHybridEncrypt.POINT_FORMAT_TYPE_CONVERTER.toProtoEnum(key.getParameters().getNistCurvePointFormat()), EciesDemHelper.getDem(key.getParameters()), key.getOutputPrefix().toByteArray());
    }

    @Override // com.google.crypto.tink.HybridDecrypt
    public byte[] decrypt(final byte[] ciphertext, final byte[] contextInfo) throws GeneralSecurityException {
        if (!Util.isPrefix(this.outputPrefix, ciphertext)) {
            throw new GeneralSecurityException("Invalid ciphertext (output prefix mismatch)");
        }
        int length = this.outputPrefix.length;
        int iEncodingSizeInBytes = EllipticCurves.encodingSizeInBytes(this.recipientPrivateKey.getParams().getCurve(), this.ecPointFormat) + length;
        if (ciphertext.length < iEncodingSizeInBytes) {
            throw new GeneralSecurityException("ciphertext too short");
        }
        return this.dem.decrypt(this.recipientKem.generateKey(Arrays.copyOfRange(ciphertext, length, iEncodingSizeInBytes), this.hkdfHmacAlgo, this.hkdfSalt, contextInfo, this.dem.getSymmetricKeySizeInBytes(), this.ecPointFormat), ciphertext, iEncodingSizeInBytes);
    }
}
