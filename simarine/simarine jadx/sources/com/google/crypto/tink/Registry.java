package com.google.crypto.tink;

import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.internal.KeyManagerRegistry;
import com.google.crypto.tink.internal.MutableParametersRegistry;
import com.google.crypto.tink.internal.MutablePrimitiveRegistry;
import com.google.crypto.tink.internal.PrimitiveSet;
import com.google.crypto.tink.prf.Prf;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.shaded.protobuf.ByteString;
import com.google.crypto.tink.shaded.protobuf.ExtensionRegistryLite;
import com.google.crypto.tink.shaded.protobuf.InvalidProtocolBufferException;
import com.google.crypto.tink.shaded.protobuf.MessageLite;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class Registry {
    private static final Logger logger = Logger.getLogger(Registry.class.getName());
    private static final ConcurrentMap<String, Catalogue<?>> catalogueMap = new ConcurrentHashMap();
    private static final Set<Class<?>> ALLOWED_PRIMITIVES = Collections.unmodifiableSet(createAllowedPrimitives());

    static synchronized void reset() {
        KeyManagerRegistry.resetGlobalInstanceTestOnly();
        MutablePrimitiveRegistry.resetGlobalInstanceTestOnly();
        catalogueMap.clear();
    }

    @Deprecated
    public static synchronized void addCatalogue(String catalogueName, Catalogue<?> catalogue) throws GeneralSecurityException {
        try {
            if (catalogueName == null) {
                throw new IllegalArgumentException("catalogueName must be non-null.");
            }
            if (catalogue == null) {
                throw new IllegalArgumentException("catalogue must be non-null.");
            }
            ConcurrentMap<String, Catalogue<?>> concurrentMap = catalogueMap;
            if (concurrentMap.containsKey(catalogueName.toLowerCase(Locale.US))) {
                if (!catalogue.getClass().getName().equals(concurrentMap.get(catalogueName.toLowerCase(Locale.US)).getClass().getName())) {
                    logger.warning("Attempted overwrite of a catalogueName catalogue for name " + catalogueName);
                    throw new GeneralSecurityException("catalogue for name " + catalogueName + " has been already registered");
                }
            }
            concurrentMap.put(catalogueName.toLowerCase(Locale.US), catalogue);
        } catch (Throwable th) {
            throw th;
        }
    }

    @Deprecated
    public static Catalogue<?> getCatalogue(String catalogueName) throws GeneralSecurityException {
        if (catalogueName == null) {
            throw new IllegalArgumentException("catalogueName must be non-null.");
        }
        Catalogue<?> catalogue = catalogueMap.get(catalogueName.toLowerCase(Locale.US));
        if (catalogue != null) {
            return catalogue;
        }
        String str = String.format("no catalogue found for %s. ", catalogueName);
        if (catalogueName.toLowerCase(Locale.US).startsWith("tinkaead")) {
            str = str + "Maybe call AeadConfig.register().";
        }
        if (catalogueName.toLowerCase(Locale.US).startsWith("tinkdeterministicaead")) {
            str = str + "Maybe call DeterministicAeadConfig.register().";
        } else if (catalogueName.toLowerCase(Locale.US).startsWith("tinkstreamingaead")) {
            str = str + "Maybe call StreamingAeadConfig.register().";
        } else if (catalogueName.toLowerCase(Locale.US).startsWith("tinkhybriddecrypt") || catalogueName.toLowerCase(Locale.US).startsWith("tinkhybridencrypt")) {
            str = str + "Maybe call HybridConfig.register().";
        } else if (catalogueName.toLowerCase(Locale.US).startsWith("tinkmac")) {
            str = str + "Maybe call MacConfig.register().";
        } else if (catalogueName.toLowerCase(Locale.US).startsWith("tinkpublickeysign") || catalogueName.toLowerCase(Locale.US).startsWith("tinkpublickeyverify")) {
            str = str + "Maybe call SignatureConfig.register().";
        } else if (catalogueName.toLowerCase(Locale.US).startsWith("tink")) {
            str = str + "Maybe call TinkConfig.register().";
        }
        throw new GeneralSecurityException(str);
    }

    public static synchronized <P> void registerKeyManager(final KeyManager<P> manager) throws GeneralSecurityException {
        registerKeyManager((KeyManager) manager, true);
    }

    private static Set<Class<?>> createAllowedPrimitives() {
        HashSet hashSet = new HashSet();
        hashSet.add(Aead.class);
        hashSet.add(DeterministicAead.class);
        hashSet.add(StreamingAead.class);
        hashSet.add(HybridEncrypt.class);
        hashSet.add(HybridDecrypt.class);
        hashSet.add(Mac.class);
        hashSet.add(Prf.class);
        hashSet.add(PublicKeySign.class);
        hashSet.add(PublicKeyVerify.class);
        return hashSet;
    }

    public static synchronized <P> void registerKeyManager(final KeyManager<P> manager, boolean newKeyAllowed) throws GeneralSecurityException {
        try {
            if (manager == null) {
                throw new IllegalArgumentException("key manager must be non-null.");
            }
            if (!ALLOWED_PRIMITIVES.contains(manager.getPrimitiveClass())) {
                throw new GeneralSecurityException("Registration of key managers for class " + manager.getPrimitiveClass() + " has been disabled. Please file an issue on https://github.com/tink-crypto/tink-java");
            }
            if (!TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_NOT_FIPS.isCompatible()) {
                throw new GeneralSecurityException("Registering key managers is not supported in FIPS mode");
            }
            KeyManagerRegistry.globalInstance().registerKeyManager(manager, newKeyAllowed);
        } catch (Throwable th) {
            throw th;
        }
    }

    @Deprecated
    public static synchronized <P> void registerKeyManager(String typeUrl, final KeyManager<P> manager) throws GeneralSecurityException {
        registerKeyManager(typeUrl, manager, true);
    }

    @Deprecated
    public static synchronized <P> void registerKeyManager(String typeUrl, final KeyManager<P> manager, boolean newKeyAllowed) throws GeneralSecurityException {
        try {
            if (manager == null) {
                throw new IllegalArgumentException("key manager must be non-null.");
            }
            if (!typeUrl.equals(manager.getKeyType())) {
                throw new GeneralSecurityException("Manager does not support key type " + typeUrl + ".");
            }
            registerKeyManager(manager, newKeyAllowed);
        } catch (Throwable th) {
            throw th;
        }
    }

    @Deprecated
    public static <P> KeyManager<P> getKeyManager(String typeUrl, Class<P> primitiveClass) throws GeneralSecurityException {
        return KeyManagerRegistry.globalInstance().getKeyManager(typeUrl, primitiveClass);
    }

    @Deprecated
    public static KeyManager<?> getUntypedKeyManager(String typeUrl) throws GeneralSecurityException {
        return KeyManagerRegistry.globalInstance().getUntypedKeyManager(typeUrl);
    }

    @Deprecated
    public static synchronized KeyData newKeyData(com.google.crypto.tink.proto.KeyTemplate keyTemplate) throws GeneralSecurityException {
        KeyManager<?> untypedKeyManager;
        untypedKeyManager = KeyManagerRegistry.globalInstance().getUntypedKeyManager(keyTemplate.getTypeUrl());
        if (KeyManagerRegistry.globalInstance().isNewKeyAllowed(keyTemplate.getTypeUrl())) {
        } else {
            throw new GeneralSecurityException("newKey-operation not permitted for key type " + keyTemplate.getTypeUrl());
        }
        return untypedKeyManager.newKeyData(keyTemplate.getValue());
    }

    @Deprecated
    public static synchronized KeyData newKeyData(KeyTemplate keyTemplate) throws GeneralSecurityException {
        try {
        } catch (InvalidProtocolBufferException e) {
            throw new GeneralSecurityException("Failed to parse serialized parameters", e);
        }
        return newKeyData(com.google.crypto.tink.proto.KeyTemplate.parseFrom(TinkProtoParametersFormat.serialize(keyTemplate.toParameters()), ExtensionRegistryLite.getEmptyRegistry()));
    }

    @Deprecated
    public static synchronized MessageLite newKey(com.google.crypto.tink.proto.KeyTemplate keyTemplate) throws GeneralSecurityException {
        KeyManager<?> untypedKeyManager;
        untypedKeyManager = getUntypedKeyManager(keyTemplate.getTypeUrl());
        if (KeyManagerRegistry.globalInstance().isNewKeyAllowed(keyTemplate.getTypeUrl())) {
        } else {
            throw new GeneralSecurityException("newKey-operation not permitted for key type " + keyTemplate.getTypeUrl());
        }
        return untypedKeyManager.newKey(keyTemplate.getValue());
    }

    @Deprecated
    public static synchronized MessageLite newKey(String typeUrl, MessageLite format) throws GeneralSecurityException {
        KeyManager<?> untypedKeyManager;
        untypedKeyManager = getUntypedKeyManager(typeUrl);
        if (KeyManagerRegistry.globalInstance().isNewKeyAllowed(typeUrl)) {
        } else {
            throw new GeneralSecurityException("newKey-operation not permitted for key type " + typeUrl);
        }
        return untypedKeyManager.newKey(format);
    }

    @Deprecated
    public static KeyData getPublicKeyData(String typeUrl, ByteString serializedPrivateKey) throws GeneralSecurityException {
        KeyManager<?> untypedKeyManager = getUntypedKeyManager(typeUrl);
        if (!(untypedKeyManager instanceof PrivateKeyManager)) {
            throw new GeneralSecurityException("manager for key type " + typeUrl + " is not a PrivateKeyManager");
        }
        return ((PrivateKeyManager) untypedKeyManager).getPublicKeyData(serializedPrivateKey);
    }

    @Deprecated
    public static <P> P getPrimitive(String typeUrl, MessageLite key, Class<P> primitiveClass) throws GeneralSecurityException {
        return KeyManagerRegistry.globalInstance().getKeyManager(typeUrl, primitiveClass).getPrimitive(key.toByteString());
    }

    public static <P> P getPrimitive(String typeUrl, ByteString serializedKey, Class<P> primitiveClass) throws GeneralSecurityException {
        return KeyManagerRegistry.globalInstance().getKeyManager(typeUrl, primitiveClass).getPrimitive(serializedKey);
    }

    public static <P> P getPrimitive(String str, byte[] bArr, Class<P> cls) throws GeneralSecurityException {
        return (P) getPrimitive(str, ByteString.copyFrom(bArr), cls);
    }

    public static <P> P getPrimitive(KeyData keyData, Class<P> cls) throws GeneralSecurityException {
        return (P) getPrimitive(keyData.getTypeUrl(), keyData.getValue(), cls);
    }

    static <KeyT extends Key, P> P getFullPrimitive(KeyT keyt, Class<P> cls) throws GeneralSecurityException {
        return (P) MutablePrimitiveRegistry.globalInstance().getPrimitive(keyt, cls);
    }

    public static <B, P> P wrap(PrimitiveSet<B> primitiveSet, Class<P> cls) throws GeneralSecurityException {
        return (P) MutablePrimitiveRegistry.globalInstance().wrap(primitiveSet, cls);
    }

    public static <P> P wrap(PrimitiveSet<P> primitiveSet) throws GeneralSecurityException {
        return (P) wrap(primitiveSet, primitiveSet.getPrimitiveClass());
    }

    public static synchronized List<String> keyTemplates() {
        return MutableParametersRegistry.globalInstance().getNames();
    }

    @Nullable
    public static Class<?> getInputPrimitive(Class<?> wrappedPrimitive) {
        try {
            return MutablePrimitiveRegistry.globalInstance().getInputPrimitiveClass(wrappedPrimitive);
        } catch (GeneralSecurityException unused) {
            return null;
        }
    }

    public static synchronized void restrictToFipsIfEmpty() throws GeneralSecurityException {
        KeyManagerRegistry.globalInstance().restrictToFipsIfEmptyAndGlobalInstance();
    }

    private Registry() {
    }
}
