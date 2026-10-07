package com.google.crypto.tink.daead.internal;

import com.google.crypto.tink.DeterministicAead;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.Registry;
import com.google.crypto.tink.internal.LegacyProtoKey;
import com.google.crypto.tink.internal.OutputPrefixUtil;
import com.google.crypto.tink.internal.ProtoKeySerialization;
import com.google.crypto.tink.internal.Util;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.proto.OutputPrefixType;
import com.google.crypto.tink.subtle.Bytes;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
public class LegacyFullDeterministicAead implements DeterministicAead {
    private final byte[] identifier;
    private final OutputPrefixType outputPrefixType;
    private final DeterministicAead rawDaead;

    public static DeterministicAead create(LegacyProtoKey key) throws GeneralSecurityException {
        byte[] byteArray;
        ProtoKeySerialization serialization = key.getSerialization(InsecureSecretKeyAccess.get());
        DeterministicAead deterministicAead = (DeterministicAead) Registry.getPrimitive((KeyData) KeyData.newBuilder().setTypeUrl(serialization.getTypeUrl()).setValue(serialization.getValue()).setKeyMaterialType(serialization.getKeyMaterialType()).build(), DeterministicAead.class);
        OutputPrefixType outputPrefixType = serialization.getOutputPrefixType();
        int i = AnonymousClass1.$SwitchMap$com$google$crypto$tink$proto$OutputPrefixType[outputPrefixType.ordinal()];
        if (i == 1) {
            byteArray = OutputPrefixUtil.EMPTY_PREFIX.toByteArray();
        } else if (i == 2 || i == 3) {
            byteArray = OutputPrefixUtil.getLegacyOutputPrefix(key.getIdRequirementOrNull().intValue()).toByteArray();
        } else if (i == 4) {
            byteArray = OutputPrefixUtil.getTinkOutputPrefix(key.getIdRequirementOrNull().intValue()).toByteArray();
        } else {
            throw new GeneralSecurityException("unknown output prefix type " + outputPrefixType.getNumber());
        }
        return new LegacyFullDeterministicAead(deterministicAead, outputPrefixType, byteArray);
    }

    /* JADX INFO: renamed from: com.google.crypto.tink.daead.internal.LegacyFullDeterministicAead$1, reason: invalid class name */
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

    private LegacyFullDeterministicAead(DeterministicAead rawDaead, OutputPrefixType outputPrefixType, byte[] identifier) {
        this.rawDaead = rawDaead;
        this.outputPrefixType = outputPrefixType;
        this.identifier = identifier;
    }

    @Override // com.google.crypto.tink.DeterministicAead
    public byte[] encryptDeterministically(final byte[] plaintext, final byte[] associatedData) throws GeneralSecurityException {
        if (this.outputPrefixType == OutputPrefixType.RAW) {
            return this.rawDaead.encryptDeterministically(plaintext, associatedData);
        }
        return Bytes.concat(this.identifier, this.rawDaead.encryptDeterministically(plaintext, associatedData));
    }

    @Override // com.google.crypto.tink.DeterministicAead
    public byte[] decryptDeterministically(final byte[] ciphertext, final byte[] associatedData) throws GeneralSecurityException {
        if (this.outputPrefixType == OutputPrefixType.RAW) {
            return this.rawDaead.decryptDeterministically(ciphertext, associatedData);
        }
        if (!Util.isPrefix(this.identifier, ciphertext)) {
            throw new GeneralSecurityException("wrong prefix");
        }
        return this.rawDaead.decryptDeterministically(Arrays.copyOfRange(ciphertext, 5, ciphertext.length), associatedData);
    }
}
