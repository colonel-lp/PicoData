package com.google.crypto.tink.internal;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class JsonParser {
    private static final JsonElementTypeAdapter JSON_ELEMENT = new JsonElementTypeAdapter(null);

    public static boolean isValidString(String s) {
        int length = s.length();
        int i = 0;
        while (i != length) {
            char cCharAt = s.charAt(i);
            int i2 = i + 1;
            if (!Character.isSurrogate(cCharAt)) {
                i = i2;
            } else {
                if (Character.isLowSurrogate(cCharAt) || i2 == length || !Character.isLowSurrogate(s.charAt(i2))) {
                    return false;
                }
                i += 2;
            }
        }
        return true;
    }

    private static final class LazilyParsedNumber extends Number {
        private final String value;

        public LazilyParsedNumber(String value) {
            this.value = value;
        }

        @Override // java.lang.Number
        public int intValue() {
            try {
                try {
                    return Integer.parseInt(this.value);
                } catch (NumberFormatException unused) {
                    return (int) Long.parseLong(this.value);
                }
            } catch (NumberFormatException unused2) {
                return new BigDecimal(this.value).intValue();
            }
        }

        @Override // java.lang.Number
        public long longValue() {
            try {
                return Long.parseLong(this.value);
            } catch (NumberFormatException unused) {
                return new BigDecimal(this.value).longValue();
            }
        }

        @Override // java.lang.Number
        public float floatValue() {
            return Float.parseFloat(this.value);
        }

        @Override // java.lang.Number
        public double doubleValue() {
            return Double.parseDouble(this.value);
        }

        public String toString() {
            return this.value;
        }

        private Object writeReplace() throws NotSerializableException {
            throw new NotSerializableException("serialization is not supported");
        }

        private void readObject(ObjectInputStream in) throws NotSerializableException {
            throw new NotSerializableException("serialization is not supported");
        }

        public int hashCode() {
            return this.value.hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof LazilyParsedNumber) {
                return this.value.equals(((LazilyParsedNumber) obj).value);
            }
            return false;
        }
    }

    private static final class JsonElementTypeAdapter extends TypeAdapter<JsonElement> {
        private static final int RECURSION_LIMIT = 100;

        private JsonElementTypeAdapter() {
        }

        /* synthetic */ JsonElementTypeAdapter(AnonymousClass1 anonymousClass1) {
            this();
        }

        @Nullable
        private JsonElement tryBeginNesting(JsonReader in, JsonToken peeked) throws IOException {
            int i = AnonymousClass1.$SwitchMap$com$google$gson$stream$JsonToken[peeked.ordinal()];
            if (i == 1) {
                in.beginArray();
                return new JsonArray();
            }
            if (i != 2) {
                return null;
            }
            in.beginObject();
            return new JsonObject();
        }

        private JsonElement readTerminal(JsonReader in, JsonToken peeked) throws IOException {
            int i = AnonymousClass1.$SwitchMap$com$google$gson$stream$JsonToken[peeked.ordinal()];
            if (i == 3) {
                String strNextString = in.nextString();
                if (!JsonParser.isValidString(strNextString)) {
                    throw new IOException("illegal characters in string");
                }
                return new JsonPrimitive(strNextString);
            }
            if (i == 4) {
                return new JsonPrimitive(new LazilyParsedNumber(in.nextString()));
            }
            if (i == 5) {
                return new JsonPrimitive(Boolean.valueOf(in.nextBoolean()));
            }
            if (i == 6) {
                in.nextNull();
                return JsonNull.INSTANCE;
            }
            throw new IllegalStateException("Unexpected token: " + peeked);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // com.google.gson.TypeAdapter
        public JsonElement read(JsonReader in) throws IOException {
            String strNextName;
            JsonToken jsonTokenPeek = in.peek();
            JsonElement jsonElementTryBeginNesting = tryBeginNesting(in, jsonTokenPeek);
            if (jsonElementTryBeginNesting == null) {
                return readTerminal(in, jsonTokenPeek);
            }
            ArrayDeque arrayDeque = new ArrayDeque();
            while (true) {
                if (in.hasNext()) {
                    if (jsonElementTryBeginNesting instanceof JsonObject) {
                        strNextName = in.nextName();
                        if (!JsonParser.isValidString(strNextName)) {
                            throw new IOException("illegal characters in string");
                        }
                    } else {
                        strNextName = null;
                    }
                    JsonToken jsonTokenPeek2 = in.peek();
                    JsonElement jsonElementTryBeginNesting2 = tryBeginNesting(in, jsonTokenPeek2);
                    boolean z = jsonElementTryBeginNesting2 != null;
                    if (jsonElementTryBeginNesting2 == null) {
                        jsonElementTryBeginNesting2 = readTerminal(in, jsonTokenPeek2);
                    }
                    if (jsonElementTryBeginNesting instanceof JsonArray) {
                        ((JsonArray) jsonElementTryBeginNesting).add(jsonElementTryBeginNesting2);
                    } else {
                        JsonObject jsonObject = (JsonObject) jsonElementTryBeginNesting;
                        if (jsonObject.has(strNextName)) {
                            throw new IOException("duplicate key: " + strNextName);
                        }
                        jsonObject.add(strNextName, jsonElementTryBeginNesting2);
                    }
                    if (z) {
                        arrayDeque.addLast(jsonElementTryBeginNesting);
                        if (arrayDeque.size() > 100) {
                            throw new IOException("too many recursions");
                        }
                        jsonElementTryBeginNesting = jsonElementTryBeginNesting2;
                    } else {
                        continue;
                    }
                } else {
                    if (jsonElementTryBeginNesting instanceof JsonArray) {
                        in.endArray();
                    } else {
                        in.endObject();
                    }
                    if (arrayDeque.isEmpty()) {
                        return jsonElementTryBeginNesting;
                    }
                    jsonElementTryBeginNesting = (JsonElement) arrayDeque.removeLast();
                }
            }
        }

        @Override // com.google.gson.TypeAdapter
        public void write(JsonWriter out, JsonElement value) {
            throw new UnsupportedOperationException("write is not supported");
        }
    }

    /* JADX INFO: renamed from: com.google.crypto.tink.internal.JsonParser$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$google$gson$stream$JsonToken;

        static {
            int[] iArr = new int[JsonToken.values().length];
            $SwitchMap$com$google$gson$stream$JsonToken = iArr;
            try {
                iArr[JsonToken.BEGIN_ARRAY.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$google$gson$stream$JsonToken[JsonToken.BEGIN_OBJECT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$google$gson$stream$JsonToken[JsonToken.STRING.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$google$gson$stream$JsonToken[JsonToken.NUMBER.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$google$gson$stream$JsonToken[JsonToken.BOOLEAN.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$google$gson$stream$JsonToken[JsonToken.NULL.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
        }
    }

    public static JsonElement parse(String json) throws IOException {
        try {
            JsonReader jsonReader = new JsonReader(new StringReader(json));
            jsonReader.setLenient(false);
            return JSON_ELEMENT.read(jsonReader);
        } catch (NumberFormatException e) {
            throw new IOException(e);
        }
    }

    public static long getParsedNumberAsLongOrThrow(Number number) {
        if (!(number instanceof LazilyParsedNumber)) {
            throw new IllegalArgumentException("does not contain a parsed number.");
        }
        return Long.parseLong(number.toString());
    }

    private JsonParser() {
    }
}
