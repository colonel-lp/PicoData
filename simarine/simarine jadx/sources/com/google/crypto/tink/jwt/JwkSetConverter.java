package com.google.crypto.tink.jwt;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.KeyStatus;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.internal.BigIntegerEncoding;
import com.google.crypto.tink.internal.JsonParser;
import com.google.crypto.tink.subtle.Base64;
import com.google.crypto.tink.tinkkey.KeyAccess;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.spec.ECPoint;
import java.util.Iterator;
import java.util.Optional;

/* JADX INFO: loaded from: classes2.dex */
public final class JwkSetConverter {
    public static String fromPublicKeysetHandle(KeysetHandle handle) throws GeneralSecurityException, IOException {
        KeysetHandle keysetHandleBuild = KeysetHandle.newBuilder(handle).build();
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < keysetHandleBuild.size(); i++) {
            KeysetHandle.Entry at = keysetHandleBuild.getAt(i);
            if (at.getStatus() == KeyStatus.ENABLED) {
                Key key = at.getKey();
                if (key instanceof JwtEcdsaPublicKey) {
                    jsonArray.add(convertJwtEcdsaKey((JwtEcdsaPublicKey) key));
                } else if (key instanceof JwtRsaSsaPkcs1PublicKey) {
                    jsonArray.add(convertJwtRsaSsaPkcs1Key((JwtRsaSsaPkcs1PublicKey) key));
                } else if (key instanceof JwtRsaSsaPssPublicKey) {
                    jsonArray.add(convertJwtRsaSsaPssKey((JwtRsaSsaPssPublicKey) key));
                } else {
                    throw new GeneralSecurityException("unsupported key with parameters " + key.getParameters());
                }
            }
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("keys", jsonArray);
        return jsonObject.toString();
    }

    public static KeysetHandle toPublicKeysetHandle(String jwkSet) throws GeneralSecurityException, IOException {
        try {
            JsonObject asJsonObject = JsonParser.parse(jwkSet).getAsJsonObject();
            KeysetHandle.Builder builderNewBuilder = KeysetHandle.newBuilder();
            Iterator<JsonElement> it = asJsonObject.get("keys").getAsJsonArray().iterator();
            while (true) {
                if (it.hasNext()) {
                    JsonObject asJsonObject2 = it.next().getAsJsonObject();
                    String strSubstring = getStringItem(asJsonObject2, "alg").substring(0, 2);
                    strSubstring.hashCode();
                    switch (strSubstring) {
                        case "ES":
                            builderNewBuilder.addEntry(KeysetHandle.importKey(convertToEcdsaKey(asJsonObject2)).withRandomId());
                            break;
                        case "PS":
                            builderNewBuilder.addEntry(KeysetHandle.importKey(convertToRsaSsaPssKey(asJsonObject2)).withRandomId());
                            break;
                        case "RS":
                            builderNewBuilder.addEntry(KeysetHandle.importKey(convertToRsaSsaPkcs1Key(asJsonObject2)).withRandomId());
                            break;
                        default:
                            throw new GeneralSecurityException("unexpected alg value: " + getStringItem(asJsonObject2, "alg"));
                    }
                } else {
                    if (builderNewBuilder.size() <= 0) {
                        throw new GeneralSecurityException("empty keyset");
                    }
                    builderNewBuilder.getAt(0).makePrimary();
                    return builderNewBuilder.build();
                }
            }
        } catch (IOException | IllegalStateException e) {
            throw new GeneralSecurityException("JWK set is invalid JSON", e);
        }
    }

    private static JsonObject convertJwtEcdsaKey(JwtEcdsaPublicKey key) throws GeneralSecurityException {
        String str;
        String str2;
        int i;
        JwtEcdsaParameters.Algorithm algorithm = key.getParameters().getAlgorithm();
        if (algorithm.equals(JwtEcdsaParameters.Algorithm.ES256)) {
            str = "ES256";
            str2 = "P-256";
            i = 32;
        } else if (algorithm.equals(JwtEcdsaParameters.Algorithm.ES384)) {
            str = "ES384";
            str2 = "P-384";
            i = 48;
        } else if (algorithm.equals(JwtEcdsaParameters.Algorithm.ES512)) {
            str = "ES512";
            str2 = "P-521";
            i = 66;
        } else {
            throw new GeneralSecurityException("unknown algorithm");
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("kty", "EC");
        jsonObject.addProperty("crv", str2);
        BigInteger affineX = key.getPublicPoint().getAffineX();
        BigInteger affineY = key.getPublicPoint().getAffineY();
        jsonObject.addProperty("x", Base64.urlSafeEncode(BigIntegerEncoding.toBigEndianBytesOfFixedLength(affineX, i)));
        jsonObject.addProperty("y", Base64.urlSafeEncode(BigIntegerEncoding.toBigEndianBytesOfFixedLength(affineY, i)));
        jsonObject.addProperty("use", "sig");
        jsonObject.addProperty("alg", str);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add("verify");
        jsonObject.add("key_ops", jsonArray);
        Optional<String> kid = key.getKid();
        if (kid.isPresent()) {
            jsonObject.addProperty("kid", kid.get());
        }
        return jsonObject;
    }

    private static byte[] base64urlUInt(BigInteger n) {
        if (n.equals(BigInteger.ZERO)) {
            return new byte[]{0};
        }
        return BigIntegerEncoding.toUnsignedBigEndianBytes(n);
    }

    private static JsonObject convertJwtRsaSsaPkcs1Key(JwtRsaSsaPkcs1PublicKey key) throws GeneralSecurityException {
        String standardName = key.getParameters().getAlgorithm().getStandardName();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("kty", "RSA");
        jsonObject.addProperty("n", Base64.urlSafeEncode(base64urlUInt(key.getModulus())));
        jsonObject.addProperty("e", Base64.urlSafeEncode(base64urlUInt(key.getParameters().getPublicExponent())));
        jsonObject.addProperty("use", "sig");
        jsonObject.addProperty("alg", standardName);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add("verify");
        jsonObject.add("key_ops", jsonArray);
        Optional<String> kid = key.getKid();
        if (kid.isPresent()) {
            jsonObject.addProperty("kid", kid.get());
        }
        return jsonObject;
    }

    private static JsonObject convertJwtRsaSsaPssKey(JwtRsaSsaPssPublicKey key) throws GeneralSecurityException {
        String standardName = key.getParameters().getAlgorithm().getStandardName();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("kty", "RSA");
        jsonObject.addProperty("n", Base64.urlSafeEncode(base64urlUInt(key.getModulus())));
        jsonObject.addProperty("e", Base64.urlSafeEncode(base64urlUInt(key.getParameters().getPublicExponent())));
        jsonObject.addProperty("use", "sig");
        jsonObject.addProperty("alg", standardName);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add("verify");
        jsonObject.add("key_ops", jsonArray);
        Optional<String> kid = key.getKid();
        if (kid.isPresent()) {
            jsonObject.addProperty("kid", kid.get());
        }
        return jsonObject;
    }

    private static String getStringItem(JsonObject obj, String name) throws GeneralSecurityException {
        if (!obj.has(name)) {
            throw new GeneralSecurityException(name + " not found");
        }
        if (!obj.get(name).isJsonPrimitive() || !obj.get(name).getAsJsonPrimitive().isString()) {
            throw new GeneralSecurityException(name + " is not a string");
        }
        return obj.get(name).getAsString();
    }

    private static void expectStringItem(JsonObject obj, String name, String expectedValue) throws GeneralSecurityException {
        String stringItem = getStringItem(obj, name);
        if (stringItem.equals(expectedValue)) {
            return;
        }
        throw new GeneralSecurityException("unexpected " + name + " value: " + stringItem);
    }

    private static void validateUseIsSig(JsonObject jsonKey) throws GeneralSecurityException {
        if (jsonKey.has("use")) {
            expectStringItem(jsonKey, "use", "sig");
        }
    }

    private static void validateKeyOpsIsVerify(JsonObject jsonKey) throws GeneralSecurityException {
        if (jsonKey.has("key_ops")) {
            if (!jsonKey.get("key_ops").isJsonArray()) {
                throw new GeneralSecurityException("key_ops is not an array");
            }
            JsonArray asJsonArray = jsonKey.get("key_ops").getAsJsonArray();
            if (asJsonArray.size() != 1) {
                throw new GeneralSecurityException("key_ops must contain exactly one element");
            }
            if (!asJsonArray.get(0).isJsonPrimitive() || !asJsonArray.get(0).getAsJsonPrimitive().isString()) {
                throw new GeneralSecurityException("key_ops is not a string");
            }
            if (asJsonArray.get(0).getAsString().equals("verify")) {
                return;
            }
            throw new GeneralSecurityException("unexpected keyOps value: " + asJsonArray.get(0).getAsString());
        }
    }

    private static JwtRsaSsaPkcs1PublicKey convertToRsaSsaPkcs1Key(JsonObject jsonKey) throws GeneralSecurityException {
        JwtRsaSsaPkcs1Parameters.Algorithm algorithm;
        String stringItem = getStringItem(jsonKey, "alg");
        stringItem.hashCode();
        switch (stringItem) {
            case "RS256":
                algorithm = JwtRsaSsaPkcs1Parameters.Algorithm.RS256;
                break;
            case "RS384":
                algorithm = JwtRsaSsaPkcs1Parameters.Algorithm.RS384;
                break;
            case "RS512":
                algorithm = JwtRsaSsaPkcs1Parameters.Algorithm.RS512;
                break;
            default:
                throw new GeneralSecurityException("Unknown Rsa Algorithm: " + getStringItem(jsonKey, "alg"));
        }
        if (jsonKey.has("p") || jsonKey.has("q") || jsonKey.has("dp") || jsonKey.has("dq") || jsonKey.has("d") || jsonKey.has("qi")) {
            throw new UnsupportedOperationException("importing RSA private keys is not implemented");
        }
        expectStringItem(jsonKey, "kty", "RSA");
        validateUseIsSig(jsonKey);
        validateKeyOpsIsVerify(jsonKey);
        BigInteger bigInteger = new BigInteger(1, Base64.urlSafeDecode(getStringItem(jsonKey, "e")));
        BigInteger bigInteger2 = new BigInteger(1, Base64.urlSafeDecode(getStringItem(jsonKey, "n")));
        if (jsonKey.has("kid")) {
            return JwtRsaSsaPkcs1PublicKey.builder().setParameters(JwtRsaSsaPkcs1Parameters.builder().setModulusSizeBits(bigInteger2.bitLength()).setPublicExponent(bigInteger).setAlgorithm(algorithm).setKidStrategy(JwtRsaSsaPkcs1Parameters.KidStrategy.CUSTOM).build()).setModulus(bigInteger2).setCustomKid(getStringItem(jsonKey, "kid")).build();
        }
        return JwtRsaSsaPkcs1PublicKey.builder().setParameters(JwtRsaSsaPkcs1Parameters.builder().setModulusSizeBits(bigInteger2.bitLength()).setPublicExponent(bigInteger).setAlgorithm(algorithm).setKidStrategy(JwtRsaSsaPkcs1Parameters.KidStrategy.IGNORED).build()).setModulus(bigInteger2).build();
    }

    private static JwtRsaSsaPssPublicKey convertToRsaSsaPssKey(JsonObject jsonKey) throws GeneralSecurityException {
        JwtRsaSsaPssParameters.Algorithm algorithm;
        String stringItem = getStringItem(jsonKey, "alg");
        stringItem.hashCode();
        switch (stringItem) {
            case "PS256":
                algorithm = JwtRsaSsaPssParameters.Algorithm.PS256;
                break;
            case "PS384":
                algorithm = JwtRsaSsaPssParameters.Algorithm.PS384;
                break;
            case "PS512":
                algorithm = JwtRsaSsaPssParameters.Algorithm.PS512;
                break;
            default:
                throw new GeneralSecurityException("Unknown Rsa Algorithm: " + getStringItem(jsonKey, "alg"));
        }
        if (jsonKey.has("p") || jsonKey.has("q") || jsonKey.has("dq") || jsonKey.has("dq") || jsonKey.has("d") || jsonKey.has("qi")) {
            throw new UnsupportedOperationException("importing RSA private keys is not implemented");
        }
        expectStringItem(jsonKey, "kty", "RSA");
        validateUseIsSig(jsonKey);
        validateKeyOpsIsVerify(jsonKey);
        BigInteger bigInteger = new BigInteger(1, Base64.urlSafeDecode(getStringItem(jsonKey, "e")));
        BigInteger bigInteger2 = new BigInteger(1, Base64.urlSafeDecode(getStringItem(jsonKey, "n")));
        if (jsonKey.has("kid")) {
            return JwtRsaSsaPssPublicKey.builder().setParameters(JwtRsaSsaPssParameters.builder().setModulusSizeBits(bigInteger2.bitLength()).setPublicExponent(bigInteger).setAlgorithm(algorithm).setKidStrategy(JwtRsaSsaPssParameters.KidStrategy.CUSTOM).build()).setModulus(bigInteger2).setCustomKid(getStringItem(jsonKey, "kid")).build();
        }
        return JwtRsaSsaPssPublicKey.builder().setParameters(JwtRsaSsaPssParameters.builder().setModulusSizeBits(bigInteger2.bitLength()).setPublicExponent(bigInteger).setAlgorithm(algorithm).setKidStrategy(JwtRsaSsaPssParameters.KidStrategy.IGNORED).build()).setModulus(bigInteger2).build();
    }

    private static JwtEcdsaPublicKey convertToEcdsaKey(JsonObject jsonKey) throws GeneralSecurityException {
        JwtEcdsaParameters.Algorithm algorithm;
        String stringItem = getStringItem(jsonKey, "alg");
        stringItem.hashCode();
        switch (stringItem) {
            case "ES256":
                expectStringItem(jsonKey, "crv", "P-256");
                algorithm = JwtEcdsaParameters.Algorithm.ES256;
                break;
            case "ES384":
                expectStringItem(jsonKey, "crv", "P-384");
                algorithm = JwtEcdsaParameters.Algorithm.ES384;
                break;
            case "ES512":
                expectStringItem(jsonKey, "crv", "P-521");
                algorithm = JwtEcdsaParameters.Algorithm.ES512;
                break;
            default:
                throw new GeneralSecurityException("Unknown Ecdsa Algorithm: " + getStringItem(jsonKey, "alg"));
        }
        if (jsonKey.has("d")) {
            throw new UnsupportedOperationException("importing ECDSA private keys is not implemented");
        }
        expectStringItem(jsonKey, "kty", "EC");
        validateUseIsSig(jsonKey);
        validateKeyOpsIsVerify(jsonKey);
        ECPoint eCPoint = new ECPoint(new BigInteger(1, Base64.urlSafeDecode(getStringItem(jsonKey, "x"))), new BigInteger(1, Base64.urlSafeDecode(getStringItem(jsonKey, "y"))));
        if (jsonKey.has("kid")) {
            return JwtEcdsaPublicKey.builder().setParameters(JwtEcdsaParameters.builder().setKidStrategy(JwtEcdsaParameters.KidStrategy.CUSTOM).setAlgorithm(algorithm).build()).setPublicPoint(eCPoint).setCustomKid(getStringItem(jsonKey, "kid")).build();
        }
        return JwtEcdsaPublicKey.builder().setParameters(JwtEcdsaParameters.builder().setKidStrategy(JwtEcdsaParameters.KidStrategy.IGNORED).setAlgorithm(algorithm).build()).setPublicPoint(eCPoint).build();
    }

    @Deprecated
    public static String fromKeysetHandle(KeysetHandle handle, KeyAccess keyAccess) throws GeneralSecurityException, IOException {
        return fromPublicKeysetHandle(handle);
    }

    @Deprecated
    public static KeysetHandle toKeysetHandle(String jwkSet, KeyAccess keyAccess) throws GeneralSecurityException, IOException {
        return toPublicKeysetHandle(jwkSet);
    }

    private JwkSetConverter() {
    }
}
