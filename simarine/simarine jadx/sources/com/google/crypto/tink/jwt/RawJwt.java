package com.google.crypto.tink.jwt;

import com.google.errorprone.annotations.Immutable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class RawJwt {
    private static final long MAX_TIMESTAMP_VALUE = 253402300799L;
    private final JsonObject payload;
    private final Optional<String> typeHeader;

    private RawJwt(Builder builder) {
        if (!builder.payload.has("exp") && !builder.withoutExpiration) {
            throw new IllegalArgumentException("neither setExpiration() nor withoutExpiration() was called");
        }
        if (builder.payload.has("exp") && builder.withoutExpiration) {
            throw new IllegalArgumentException("setExpiration() and withoutExpiration() must not be called together");
        }
        this.typeHeader = builder.typeHeader;
        this.payload = builder.payload.deepCopy();
    }

    private RawJwt(Optional<String> typeHeader, String jsonPayload) throws JwtInvalidException {
        this.typeHeader = typeHeader;
        this.payload = JsonUtil.parseJson(jsonPayload);
        validateStringClaim("iss");
        validateStringClaim("sub");
        validateStringClaim("jti");
        validateTimestampClaim("exp");
        validateTimestampClaim("nbf");
        validateTimestampClaim("iat");
        validateAudienceClaim();
    }

    private void validateStringClaim(String name) throws JwtInvalidException {
        if (this.payload.has(name)) {
            if (this.payload.get(name).isJsonPrimitive() && this.payload.get(name).getAsJsonPrimitive().isString()) {
                return;
            }
            throw new JwtInvalidException("invalid JWT payload: claim " + name + " is not a string.");
        }
    }

    private void validateTimestampClaim(String name) throws JwtInvalidException {
        if (this.payload.has(name)) {
            if (!this.payload.get(name).isJsonPrimitive() || !this.payload.get(name).getAsJsonPrimitive().isNumber()) {
                throw new JwtInvalidException("invalid JWT payload: claim " + name + " is not a number.");
            }
            double asDouble = this.payload.get(name).getAsJsonPrimitive().getAsDouble();
            if (asDouble > 2.53402300799E11d || asDouble < 0.0d) {
                throw new JwtInvalidException("invalid JWT payload: claim " + name + " has an invalid timestamp");
            }
        }
    }

    private void validateAudienceClaim() throws JwtInvalidException {
        if (this.payload.has("aud")) {
            if (!(this.payload.get("aud").isJsonPrimitive() && this.payload.get("aud").getAsJsonPrimitive().isString()) && getAudiences().size() < 1) {
                throw new JwtInvalidException("invalid JWT payload: claim aud is present but empty.");
            }
        }
    }

    static RawJwt fromJsonPayload(Optional<String> typeHeader, String jsonPayload) throws JwtInvalidException {
        return new RawJwt(typeHeader, jsonPayload);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private final JsonObject payload;
        private Optional<String> typeHeader;
        private boolean withoutExpiration;

        private Builder() {
            this.typeHeader = Optional.empty();
            this.withoutExpiration = false;
            this.payload = new JsonObject();
        }

        public Builder setTypeHeader(String value) {
            this.typeHeader = Optional.of(value);
            return this;
        }

        public Builder setIssuer(String value) {
            if (!JsonUtil.isValidString(value)) {
                throw new IllegalArgumentException();
            }
            this.payload.add("iss", new JsonPrimitive(value));
            return this;
        }

        public Builder setSubject(String value) {
            if (!JsonUtil.isValidString(value)) {
                throw new IllegalArgumentException();
            }
            this.payload.add("sub", new JsonPrimitive(value));
            return this;
        }

        public Builder setAudience(String value) {
            if (this.payload.has("aud") && this.payload.get("aud").isJsonArray()) {
                throw new IllegalArgumentException("setAudience can't be used together with setAudiences or addAudience");
            }
            if (!JsonUtil.isValidString(value)) {
                throw new IllegalArgumentException("invalid string");
            }
            this.payload.add("aud", new JsonPrimitive(value));
            return this;
        }

        public Builder setAudiences(List<String> values) {
            if (this.payload.has("aud") && !this.payload.get("aud").isJsonArray()) {
                throw new IllegalArgumentException("setAudiences can't be used together with setAudience");
            }
            if (values.isEmpty()) {
                throw new IllegalArgumentException("audiences must not be empty");
            }
            JsonArray jsonArray = new JsonArray();
            for (String str : values) {
                if (!JsonUtil.isValidString(str)) {
                    throw new IllegalArgumentException("invalid string");
                }
                jsonArray.add(str);
            }
            this.payload.add("aud", jsonArray);
            return this;
        }

        public Builder addAudience(String value) {
            JsonArray jsonArray;
            if (!JsonUtil.isValidString(value)) {
                throw new IllegalArgumentException("invalid string");
            }
            if (this.payload.has("aud")) {
                JsonElement jsonElement = this.payload.get("aud");
                if (!jsonElement.isJsonArray()) {
                    throw new IllegalArgumentException("addAudience can't be used together with setAudience");
                }
                jsonArray = jsonElement.getAsJsonArray();
            } else {
                jsonArray = new JsonArray();
            }
            jsonArray.add(value);
            this.payload.add("aud", jsonArray);
            return this;
        }

        public Builder setJwtId(String value) {
            if (!JsonUtil.isValidString(value)) {
                throw new IllegalArgumentException();
            }
            this.payload.add("jti", new JsonPrimitive(value));
            return this;
        }

        private void setTimestampClaim(String name, Instant value) {
            long epochSecond = value.getEpochSecond();
            if (epochSecond > RawJwt.MAX_TIMESTAMP_VALUE || epochSecond < 0) {
                throw new IllegalArgumentException("timestamp of claim " + name + " is out of range");
            }
            this.payload.add(name, new JsonPrimitive(Long.valueOf(epochSecond)));
        }

        public Builder setExpiration(Instant value) {
            setTimestampClaim("exp", value);
            return this;
        }

        public Builder withoutExpiration() {
            this.withoutExpiration = true;
            return this;
        }

        public Builder setNotBefore(Instant value) {
            setTimestampClaim("nbf", value);
            return this;
        }

        public Builder setIssuedAt(Instant value) {
            setTimestampClaim("iat", value);
            return this;
        }

        public Builder addBooleanClaim(String name, boolean value) {
            JwtNames.validate(name);
            this.payload.add(name, new JsonPrimitive(Boolean.valueOf(value)));
            return this;
        }

        public Builder addNumberClaim(String name, long value) {
            JwtNames.validate(name);
            this.payload.add(name, new JsonPrimitive(Long.valueOf(value)));
            return this;
        }

        public Builder addNumberClaim(String name, double value) {
            JwtNames.validate(name);
            this.payload.add(name, new JsonPrimitive(Double.valueOf(value)));
            return this;
        }

        public Builder addStringClaim(String name, String value) {
            if (!JsonUtil.isValidString(value)) {
                throw new IllegalArgumentException();
            }
            JwtNames.validate(name);
            this.payload.add(name, new JsonPrimitive(value));
            return this;
        }

        public Builder addNullClaim(String name) {
            JwtNames.validate(name);
            this.payload.add(name, JsonNull.INSTANCE);
            return this;
        }

        public Builder addJsonObjectClaim(String name, String encodedJsonObject) throws JwtInvalidException {
            JwtNames.validate(name);
            this.payload.add(name, JsonUtil.parseJson(encodedJsonObject));
            return this;
        }

        public Builder addJsonArrayClaim(String name, String encodedJsonArray) throws JwtInvalidException {
            JwtNames.validate(name);
            this.payload.add(name, JsonUtil.parseJsonArray(encodedJsonArray));
            return this;
        }

        public RawJwt build() {
            return new RawJwt(this);
        }
    }

    public String getJsonPayload() {
        return this.payload.toString();
    }

    boolean hasBooleanClaim(String name) {
        JwtNames.validate(name);
        return this.payload.has(name) && this.payload.get(name).isJsonPrimitive() && this.payload.get(name).getAsJsonPrimitive().isBoolean();
    }

    Boolean getBooleanClaim(String name) throws JwtInvalidException {
        JwtNames.validate(name);
        if (!this.payload.has(name)) {
            throw new JwtInvalidException("claim " + name + " does not exist");
        }
        if (!this.payload.get(name).isJsonPrimitive() || !this.payload.get(name).getAsJsonPrimitive().isBoolean()) {
            throw new JwtInvalidException("claim " + name + " is not a boolean");
        }
        return Boolean.valueOf(this.payload.get(name).getAsBoolean());
    }

    boolean hasNumberClaim(String name) {
        JwtNames.validate(name);
        return this.payload.has(name) && this.payload.get(name).isJsonPrimitive() && this.payload.get(name).getAsJsonPrimitive().isNumber();
    }

    Double getNumberClaim(String name) throws JwtInvalidException {
        JwtNames.validate(name);
        if (!this.payload.has(name)) {
            throw new JwtInvalidException("claim " + name + " does not exist");
        }
        if (!this.payload.get(name).isJsonPrimitive() || !this.payload.get(name).getAsJsonPrimitive().isNumber()) {
            throw new JwtInvalidException("claim " + name + " is not a number");
        }
        return Double.valueOf(this.payload.get(name).getAsDouble());
    }

    boolean hasStringClaim(String name) {
        JwtNames.validate(name);
        return this.payload.has(name) && this.payload.get(name).isJsonPrimitive() && this.payload.get(name).getAsJsonPrimitive().isString();
    }

    String getStringClaim(String name) throws JwtInvalidException {
        JwtNames.validate(name);
        return getStringClaimInternal(name);
    }

    private String getStringClaimInternal(String name) throws JwtInvalidException {
        if (!this.payload.has(name)) {
            throw new JwtInvalidException("claim " + name + " does not exist");
        }
        if (!this.payload.get(name).isJsonPrimitive() || !this.payload.get(name).getAsJsonPrimitive().isString()) {
            throw new JwtInvalidException("claim " + name + " is not a string");
        }
        return this.payload.get(name).getAsString();
    }

    boolean isNullClaim(String name) {
        JwtNames.validate(name);
        try {
            return JsonNull.INSTANCE.equals(this.payload.get(name));
        } catch (JsonParseException unused) {
            return false;
        }
    }

    boolean hasJsonObjectClaim(String name) {
        JwtNames.validate(name);
        return this.payload.has(name) && this.payload.get(name).isJsonObject();
    }

    String getJsonObjectClaim(String name) throws JwtInvalidException {
        JwtNames.validate(name);
        if (!this.payload.has(name)) {
            throw new JwtInvalidException("claim " + name + " does not exist");
        }
        if (!this.payload.get(name).isJsonObject()) {
            throw new JwtInvalidException("claim " + name + " is not a JSON object");
        }
        return this.payload.get(name).getAsJsonObject().toString();
    }

    boolean hasJsonArrayClaim(String name) {
        JwtNames.validate(name);
        return this.payload.has(name) && this.payload.get(name).isJsonArray();
    }

    String getJsonArrayClaim(String name) throws JwtInvalidException {
        JwtNames.validate(name);
        if (!this.payload.has(name)) {
            throw new JwtInvalidException("claim " + name + " does not exist");
        }
        if (!this.payload.get(name).isJsonArray()) {
            throw new JwtInvalidException("claim " + name + " is not a JSON array");
        }
        return this.payload.get(name).getAsJsonArray().toString();
    }

    boolean hasTypeHeader() {
        return this.typeHeader.isPresent();
    }

    String getTypeHeader() throws JwtInvalidException {
        if (!this.typeHeader.isPresent()) {
            throw new JwtInvalidException("type header is not set");
        }
        return this.typeHeader.get();
    }

    boolean hasIssuer() {
        return this.payload.has("iss");
    }

    String getIssuer() throws JwtInvalidException {
        return getStringClaimInternal("iss");
    }

    boolean hasSubject() {
        return this.payload.has("sub");
    }

    String getSubject() throws JwtInvalidException {
        return getStringClaimInternal("sub");
    }

    boolean hasJwtId() {
        return this.payload.has("jti");
    }

    String getJwtId() throws JwtInvalidException {
        return getStringClaimInternal("jti");
    }

    boolean hasAudiences() {
        return this.payload.has("aud");
    }

    List<String> getAudiences() throws JwtInvalidException {
        if (!hasAudiences()) {
            throw new JwtInvalidException("claim aud does not exist");
        }
        JsonElement jsonElement = this.payload.get("aud");
        if (jsonElement.isJsonPrimitive()) {
            if (!jsonElement.getAsJsonPrimitive().isString()) {
                throw new JwtInvalidException(String.format("invalid audience: got %s; want a string", jsonElement));
            }
            return Collections.unmodifiableList(Arrays.asList(jsonElement.getAsString()));
        }
        if (!jsonElement.isJsonArray()) {
            throw new JwtInvalidException("claim aud is not a string or a JSON array");
        }
        JsonArray asJsonArray = jsonElement.getAsJsonArray();
        ArrayList arrayList = new ArrayList(asJsonArray.size());
        for (int i = 0; i < asJsonArray.size(); i++) {
            if (!asJsonArray.get(i).isJsonPrimitive() || !asJsonArray.get(i).getAsJsonPrimitive().isString()) {
                throw new JwtInvalidException(String.format("invalid audience: got %s; want a string", asJsonArray.get(i)));
            }
            arrayList.add(asJsonArray.get(i).getAsString());
        }
        return Collections.unmodifiableList(arrayList);
    }

    private Instant getInstant(String name) throws JwtInvalidException {
        if (!this.payload.has(name)) {
            throw new JwtInvalidException("claim " + name + " does not exist");
        }
        if (!this.payload.get(name).isJsonPrimitive() || !this.payload.get(name).getAsJsonPrimitive().isNumber()) {
            throw new JwtInvalidException("claim " + name + " is not a timestamp");
        }
        try {
            return Instant.ofEpochMilli((long) (this.payload.get(name).getAsJsonPrimitive().getAsDouble() * 1000.0d));
        } catch (NumberFormatException e) {
            throw new JwtInvalidException("claim " + name + " is not a timestamp: " + e);
        }
    }

    boolean hasExpiration() {
        return this.payload.has("exp");
    }

    Instant getExpiration() throws JwtInvalidException {
        return getInstant("exp");
    }

    boolean hasNotBefore() {
        return this.payload.has("nbf");
    }

    Instant getNotBefore() throws JwtInvalidException {
        return getInstant("nbf");
    }

    boolean hasIssuedAt() {
        return this.payload.has("iat");
    }

    Instant getIssuedAt() throws JwtInvalidException {
        return getInstant("iat");
    }

    Set<String> customClaimNames() {
        HashSet hashSet = new HashSet();
        for (String str : this.payload.keySet()) {
            if (!JwtNames.isRegisteredName(str)) {
                hashSet.add(str);
            }
        }
        return Collections.unmodifiableSet(hashSet);
    }

    public String toString() {
        JsonObject jsonObject = new JsonObject();
        if (this.typeHeader.isPresent()) {
            jsonObject.add("typ", new JsonPrimitive(this.typeHeader.get()));
        }
        return jsonObject + "." + this.payload;
    }
}
