package com.google.crypto.tink.hybrid.internal;

import com.google.crypto.tink.HybridDecrypt;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.Registry;
import com.google.crypto.tink.internal.LegacyProtoKey;
import com.google.crypto.tink.internal.OutputPrefixUtil;
import com.google.crypto.tink.internal.ProtoKeySerialization;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.proto.OutputPrefixType;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class LegacyFullHybridDecrypt implements HybridDecrypt {
    private final byte[] outputPrefix;
    private final HybridDecrypt rawHybridDecrypt;

    public static HybridDecrypt create(LegacyProtoKey key) throws GeneralSecurityException {
        byte[] byteArray;
        ProtoKeySerialization serialization = key.getSerialization(InsecureSecretKeyAccess.get());
        HybridDecrypt hybridDecrypt = (HybridDecrypt) Registry.getPrimitive((KeyData) KeyData.newBuilder().setTypeUrl(serialization.getTypeUrl()).setValue(serialization.getValue()).setKeyMaterialType(serialization.getKeyMaterialType()).build(), HybridDecrypt.class);
        OutputPrefixType outputPrefixType = serialization.getOutputPrefixType();
        int i = AnonymousClass1.$SwitchMap$com$google$crypto$tink$proto$OutputPrefixType[outputPrefixType.ordinal()];
        if (i == 1) {
            byteArray = OutputPrefixUtil.EMPTY_PREFIX.toByteArray();
        } else if (i == 2 || i == 3) {
            byteArray = OutputPrefixUtil.getLegacyOutputPrefix(key.getIdRequirementOrNull().intValue()).toByteArray();
        } else if (i == 4) {
            byteArray = OutputPrefixUtil.getTinkOutputPrefix(key.getIdRequirementOrNull().intValue()).toByteArray();
        } else {
            throw new GeneralSecurityException("unknown output prefix type " + outputPrefixType);
        }
        return new LegacyFullHybridDecrypt(hybridDecrypt, byteArray);
    }

    /* JADX INFO: renamed from: com.google.crypto.tink.hybrid.internal.LegacyFullHybridDecrypt$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$google$crypto$tink$proto$OutputPrefixType;

        static {
            int[] iArr = new int[OutputPrefixType.values().length];
            $SwitchMap$com$google$crypto$tink$proto$OutputPrefixType = iArr;
            try {
                iArr[OutputPrefixType.RAW.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$proto$OutputPrefixType[OutputPrefixType.LEGACY.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$proto$OutputPrefixType[OutputPrefixType.CRUNCHY.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$google$crypto$tink$proto$OutputPrefixType[OutputPrefixType.TINK.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    private LegacyFullHybridDecrypt(HybridDecrypt rawHybridDecrypt, byte[] outputPrefix) {
        this.rawHybridDecrypt = rawHybridDecrypt;
        this.outputPrefix = outputPrefix;
    }

    @Override // com.google.crypto.tink.HybridDecrypt
    public byte[] decrypt(final byte[] ciphertext, final byte[] contextInfo) throws GeneralSecurityException {
        byte[] bArr = this.outputPrefix;
        if (bArr.length == 0) {
            return this.rawHybridDecrypt.decrypt(ciphertext, contextInfo);
        }
        if (!Util.isPrefix(bArr, ciphertext)) {
            throw new GeneralSecurityException("Invalid ciphertext (output prefix mismatch)");
        }
        return this.rawHybridDecrypt.decrypt(Arrays.copyOfRange(ciphertext, this.outputPrefix.length, ciphertext.length), contextInfo);
    }
}
