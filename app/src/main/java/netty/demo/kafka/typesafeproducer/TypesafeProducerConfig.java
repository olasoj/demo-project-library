package netty.demo.kafka.typesafeproducer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SecurityConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.Serializer;

import java.io.Serial;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public final class TypesafeProducerConfig {
    private final Map<String, Object> customEntries = new HashMap<>();
    private String bootstrapServers;
    private Class<? extends Serializer<?>> keySerializerClass;
    private Class<? extends Serializer<?>> valueSerializerClass;

    private static void tryInsertEntry(Map<String, Object> staging, String key, Object value) {
        staging.compute(key, (__key, existingValue) -> {
            if (existingValue == null) {
                return value;
            } else {
                throw new ConflictingPropertyException("Property " + key + " conflicts with an expected property");
            }
        });
    }

    private static Set<String> scanClassesForPropertyNames(Class<?>... classes) {
        return Arrays.stream(classes)
                .map(Class::getFields)
                .flatMap(Arrays::stream)
                .filter(TypesafeProducerConfig::isFieldConstant)
                .filter(TypesafeProducerConfig::isFieldStringType)
                .filter(not(TypesafeProducerConfig::isFieldDoc))
                .map(TypesafeProducerConfig::retrieveField)
                .collect(Collectors.toSet());
    }

    private static boolean isFieldConstant(Field field) {
        return Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers());
    }

    private static boolean isFieldStringType(Field field) {
        return field.getType().equals(String.class);
    }

    private static boolean isFieldDoc(Field field) {
        return field.getName().endsWith("_DOC");
    }

    private static String retrieveField(Field field) {
        try {
            return (String) field.get(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public TypesafeProducerConfig withBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
        return this;
    }

    public TypesafeProducerConfig withKeySerializerClass(Class<? extends Serializer<?>> keySerializerClass) {
        this.keySerializerClass = keySerializerClass;
        return this;
    }

    public TypesafeProducerConfig withValueSerializerClass(Class<? extends Serializer<?>> valueSerializerClass) {
        this.valueSerializerClass = valueSerializerClass;
        return this;
    }

    public TypesafeProducerConfig withCustomEntry(String propertyName, Object value) {
        Objects.requireNonNull(propertyName, "Property name cannot be null");
        customEntries.put(propertyName, value);
        return this;
    }

    public Map<String, Object> toMap() {
        final Map<String, Object> stagingConfig = new HashMap<>();

        if (!customEntries.isEmpty()) {
            final Set<String> supportedKeys = scanClassesForPropertyNames(
                    SecurityConfig.class,
                    SslConfigs.class,
                    SaslConfigs.class,
                    ProducerConfig.class,
                    CommonClientConfigs.class
            );

            final Optional<String> unsupportedKey = customEntries.keySet()
                    .stream()
                    .filter(not(supportedKeys::contains))
                    .findAny();

            if (unsupportedKey.isPresent()) {
                throw new UnsupportedPropertyException("Unsupported property " + unsupportedKey.get());
            }

            stagingConfig.putAll(customEntries);
        }

        Objects.requireNonNull(bootstrapServers, "Bootstrap servers not set");
        tryInsertEntry(stagingConfig, CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        Objects.requireNonNull(keySerializerClass, "Key serializer not set");
        tryInsertEntry(stagingConfig, ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializerClass.getName());

        Objects.requireNonNull(valueSerializerClass, "Value serializer not set");
        tryInsertEntry(stagingConfig, ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializerClass.getName());

        return stagingConfig;
    }

    public static final class UnsupportedPropertyException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 1L;

        private UnsupportedPropertyException(String s) {
            super(s);
        }
    }

    public static final class ConflictingPropertyException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 1L;

        private ConflictingPropertyException(String s) {
            super(s);
        }
    }
}
