package com.google.crypto.tink.internal;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.SecretKeyAccess;
import com.google.crypto.tink.util.Bytes;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class SerializationRegistry {
    private final Map<ParserIndex, KeyParser<?>> keyParserMap;
    private final Map<SerializerIndex, KeySerializer<?, ?>> keySerializerMap;
    private final Map<ParserIndex, ParametersParser<?>> parametersParserMap;
    private final Map<SerializerIndex, ParametersSerializer<?, ?>> parametersSerializerMap;

    public static final class Builder {
        private final Map<ParserIndex, KeyParser<?>> keyParserMap;
        private final Map<SerializerIndex, KeySerializer<?, ?>> keySerializerMap;
        private final Map<ParserIndex, ParametersParser<?>> parametersParserMap;
        private final Map<SerializerIndex, ParametersSerializer<?, ?>> parametersSerializerMap;

        public Builder() {
            this.keySerializerMap = new HashMap();
            this.keyParserMap = new HashMap();
            this.parametersSerializerMap = new HashMap();
            this.parametersParserMap = new HashMap();
        }

        public Builder(SerializationRegistry registry) {
            this.keySerializerMap = new HashMap(registry.keySerializerMap);
            this.keyParserMap = new HashMap(registry.keyParserMap);
            this.parametersSerializerMap = new HashMap(registry.parametersSerializerMap);
            this.parametersParserMap = new HashMap(registry.parametersParserMap);
        }

        public <KeyT extends Key, SerializationT extends Serialization> Builder registerKeySerializer(KeySerializer<KeyT, SerializationT> serializer) throws GeneralSecurityException {
            SerializerIndex serializerIndex = new SerializerIndex(serializer.getKeyClass(), serializer.getSerializationClass());
            if (this.keySerializerMap.containsKey(serializerIndex)) {
                KeySerializer<?, ?> keySerializer = this.keySerializerMap.get(serializerIndex);
                if (!keySerializer.equals(serializer) || !serializer.equals(keySerializer)) {
                    throw new GeneralSecurityException("Attempt to register non-equal serializer for already existing object of type: " + serializerIndex);
                }
            } else {
                this.keySerializerMap.put(serializerIndex, serializer);
            }
            return this;
        }

        public <SerializationT extends Serialization> Builder registerKeyParser(KeyParser<SerializationT> parser) throws GeneralSecurityException {
            ParserIndex parserIndex = new ParserIndex(parser.getSerializationClass(), parser.getObjectIdentifier());
            if (this.keyParserMap.containsKey(parserIndex)) {
                KeyParser<?> keyParser = this.keyParserMap.get(parserIndex);
                if (!keyParser.equals(parser) || !parser.equals(keyParser)) {
                    throw new GeneralSecurityException("Attempt to register non-equal parser for already existing object of type: " + parserIndex);
                }
            } else {
                this.keyParserMap.put(parserIndex, parser);
            }
            return this;
        }

        public <ParametersT extends Parameters, SerializationT extends Serialization> Builder registerParametersSerializer(ParametersSerializer<ParametersT, SerializationT> serializer) throws GeneralSecurityException {
            SerializerIndex serializerIndex = new SerializerIndex(serializer.getParametersClass(), serializer.getSerializationClass());
            if (this.parametersSerializerMap.containsKey(serializerIndex)) {
                ParametersSerializer<?, ?> parametersSerializer = this.parametersSerializerMap.get(serializerIndex);
                if (!parametersSerializer.equals(serializer) || !serializer.equals(parametersSerializer)) {
                    throw new GeneralSecurityException("Attempt to register non-equal serializer for already existing object of type: " + serializerIndex);
                }
            } else {
                this.parametersSerializerMap.put(serializerIndex, serializer);
            }
            return this;
        }

        public <SerializationT extends Serialization> Builder registerParametersParser(ParametersParser<SerializationT> parser) throws GeneralSecurityException {
            ParserIndex parserIndex = new ParserIndex(parser.getSerializationClass(), parser.getObjectIdentifier());
            if (this.parametersParserMap.containsKey(parserIndex)) {
                ParametersParser<?> parametersParser = this.parametersParserMap.get(parserIndex);
                if (!parametersParser.equals(parser) || !parser.equals(parametersParser)) {
                    throw new GeneralSecurityException("Attempt to register non-equal parser for already existing object of type: " + parserIndex);
                }
            } else {
                this.parametersParserMap.put(parserIndex, parser);
            }
            return this;
        }

        public SerializationRegistry build() {
            return new SerializationRegistry(this);
        }
    }

    private SerializationRegistry(Builder builder) {
        this.keySerializerMap = new HashMap(builder.keySerializerMap);
        this.keyParserMap = new HashMap(builder.keyParserMap);
        this.parametersSerializerMap = new HashMap(builder.parametersSerializerMap);
        this.parametersParserMap = new HashMap(builder.parametersParserMap);
    }

    private static class SerializerIndex {
        private final Class<?> keyClass;
        private final Class<? extends Serialization> keySerializationClass;

        private SerializerIndex(Class<?> keyClass, Class<? extends Serialization> keySerializationClass) {
            this.keyClass = keyClass;
            this.keySerializationClass = keySerializationClass;
        }

        public boolean equals(Object o) {
            if (!(o instanceof SerializerIndex)) {
                return false;
            }
            SerializerIndex serializerIndex = (SerializerIndex) o;
            return serializerIndex.keyClass.equals(this.keyClass) && serializerIndex.keySerializationClass.equals(this.keySerializationClass);
        }

        public int hashCode() {
            return Objects.hash(this.keyClass, this.keySerializationClass);
        }

        public String toString() {
            return this.keyClass.getSimpleName() + " with serialization type: " + this.keySerializationClass.getSimpleName();
        }
    }

    private static class ParserIndex {
        private final Class<? extends Serialization> keySerializationClass;
        private final Bytes serializationIdentifier;

        private ParserIndex(Class<? extends Serialization> keySerializationClass, Bytes serializationIdentifier) {
            this.keySerializationClass = keySerializationClass;
            this.serializationIdentifier = serializationIdentifier;
        }

        public boolean equals(Object o) {
            if (!(o instanceof ParserIndex)) {
                return false;
            }
            ParserIndex parserIndex = (ParserIndex) o;
            return parserIndex.keySerializationClass.equals(this.keySerializationClass) && parserIndex.serializationIdentifier.equals(this.serializationIdentifier);
        }

        public int hashCode() {
            return Objects.hash(this.keySerializationClass, this.serializationIdentifier);
        }

        public String toString() {
            return this.keySerializationClass.getSimpleName() + ", object identifier: " + this.serializationIdentifier;
        }
    }

    public <SerializationT extends Serialization> boolean hasParserForKey(SerializationT serializedKey) {
        return this.keyParserMap.containsKey(new ParserIndex(serializedKey.getClass(), serializedKey.getObjectIdentifier()));
    }

    public <SerializationT extends Serialization> Key parseKey(SerializationT serializedKey, @Nullable SecretKeyAccess access) throws GeneralSecurityException {
        ParserIndex parserIndex = new ParserIndex(serializedKey.getClass(), serializedKey.getObjectIdentifier());
        if (!this.keyParserMap.containsKey(parserIndex)) {
            throw new GeneralSecurityException("No Key Parser for requested key type " + parserIndex + " available");
        }
        return this.keyParserMap.get(parserIndex).parseKey(serializedKey, access);
    }

    public <KeyT extends Key, SerializationT extends Serialization> boolean hasSerializerForKey(KeyT key, Class<SerializationT> serializationClass) {
        return this.keySerializerMap.containsKey(new SerializerIndex(key.getClass(), serializationClass));
    }

    public <KeyT extends Key, SerializationT extends Serialization> SerializationT serializeKey(KeyT keyt, Class<SerializationT> cls, @Nullable SecretKeyAccess secretKeyAccess) throws GeneralSecurityException {
        SerializerIndex serializerIndex = new SerializerIndex(keyt.getClass(), cls);
        if (!this.keySerializerMap.containsKey(serializerIndex)) {
            throw new GeneralSecurityException("No Key serializer for " + serializerIndex + " available");
        }
        return (SerializationT) this.keySerializerMap.get(serializerIndex).serializeKey(keyt, secretKeyAccess);
    }

    public <SerializationT extends Serialization> boolean hasParserForParameters(SerializationT serializedParameters) {
        return this.parametersParserMap.containsKey(new ParserIndex(serializedParameters.getClass(), serializedParameters.getObjectIdentifier()));
    }

    public <SerializationT extends Serialization> Parameters parseParameters(SerializationT serializedParameters) throws GeneralSecurityException {
        ParserIndex parserIndex = new ParserIndex(serializedParameters.getClass(), serializedParameters.getObjectIdentifier());
        if (!this.parametersParserMap.containsKey(parserIndex)) {
            throw new GeneralSecurityException("No Parameters Parser for requested key type " + parserIndex + " available");
        }
        return this.parametersParserMap.get(parserIndex).parseParameters(serializedParameters);
    }

    public <ParametersT extends Parameters, SerializationT extends Serialization> boolean hasSerializerForParameters(ParametersT parameters, Class<SerializationT> serializationClass) {
        return this.parametersSerializerMap.containsKey(new SerializerIndex(parameters.getClass(), serializationClass));
    }

    public <ParametersT extends Parameters, SerializationT extends Serialization> SerializationT serializeParameters(ParametersT parameterst, Class<SerializationT> cls) throws GeneralSecurityException {
        SerializerIndex serializerIndex = new SerializerIndex(parameterst.getClass(), cls);
        if (!this.parametersSerializerMap.containsKey(serializerIndex)) {
            throw new GeneralSecurityException("No Key Format serializer for " + serializerIndex + " available");
        }
        return (SerializationT) this.parametersSerializerMap.get(serializerIndex).serializeParameters(parameterst);
    }
}
