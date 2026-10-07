package com.google.crypto.tink.jwt;

import com.google.crypto.tink.internal.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;

/* JADX INFO: loaded from: classes2.dex */
final class JsonUtil {
    static boolean isValidString(String s) {
        return JsonParser.isValidString(s);
    }

    static JsonObject parseJson(String jsonString) throws JwtInvalidException {
        try {
            return JsonParser.parse(jsonString).getAsJsonObject();
        } catch (JsonParseException | IOException | IllegalStateException e) {
            throw new JwtInvalidException("invalid JSON: " + e);
        }
    }

    static JsonArray parseJsonArray(String jsonString) throws JwtInvalidException {
        try {
            return JsonParser.parse(jsonString).getAsJsonArray();
        } catch (JsonParseException | IOException | IllegalStateException e) {
            throw new JwtInvalidException("invalid JSON: " + e);
        }
    }

    private JsonUtil() {
    }
}
