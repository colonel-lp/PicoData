package com.google.crypto.tink;

import com.google.crypto.tink.internal.MutablePrimitiveRegistry;
import com.google.crypto.tink.proto.KeyTypeEntry;
import com.google.crypto.tink.proto.RegistryConfig;
import java.security.GeneralSecurityException;
import java.util.Iterator;

/* JADX INFO: loaded from: classes2.dex */
public final class Config {
    public static KeyTypeEntry getTinkKeyTypeEntry(String catalogueName, String primitiveName, String keyProtoName, int keyManagerVersion, boolean newKeyAllowed) {
        return (KeyTypeEntry) KeyTypeEntry.newBuilder().setPrimitiveName(primitiveName).setTypeUrl("type.googleapis.com/google.crypto.tink." + keyProtoName).setKeyManagerVersion(keyManagerVersion).setNewKeyAllowed(newKeyAllowed).setCatalogueName(catalogueName).build();
    }

    public static void register(RegistryConfig config) throws GeneralSecurityException {
        Iterator<KeyTypeEntry> it = config.getEntryList().iterator();
        while (it.hasNext()) {
            registerKeyType(it.next());
        }
    }

    public static void registerKeyType(KeyTypeEntry entry) throws GeneralSecurityException {
        validate(entry);
        if (entry.getCatalogueName().equals("TinkAead") || entry.getCatalogueName().equals("TinkMac") || entry.getCatalogueName().equals("TinkHybridDecrypt") || entry.getCatalogueName().equals("TinkHybridEncrypt") || entry.getCatalogueName().equals("TinkPublicKeySign") || entry.getCatalogueName().equals("TinkPublicKeyVerify") || entry.getCatalogueName().equals("TinkStreamingAead") || entry.getCatalogueName().equals("TinkDeterministicAead")) {
            return;
        }
        Catalogue<?> catalogue = Registry.getCatalogue(entry.getCatalogueName());
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveWrapper(catalogue.getPrimitiveWrapper());
        Registry.registerKeyManager(catalogue.getKeyManager(entry.getTypeUrl(), entry.getPrimitiveName(), entry.getKeyManagerVersion()), entry.getNewKeyAllowed());
    }

    private static void validate(KeyTypeEntry entry) throws GeneralSecurityException {
        if (entry.getTypeUrl().isEmpty()) {
            throw new GeneralSecurityException("Missing type_url.");
        }
        if (entry.getPrimitiveName().isEmpty()) {
            throw new GeneralSecurityException("Missing primitive_name.");
        }
        if (entry.getCatalogueName().isEmpty()) {
            throw new GeneralSecurityException("Missing catalogue_name.");
        }
    }

    private Config() {
    }
}
