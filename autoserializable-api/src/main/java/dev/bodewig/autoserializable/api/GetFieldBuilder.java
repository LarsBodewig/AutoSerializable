package dev.bodewig.autoserializable.api;

import dev.bodewig.autoserializable.api.function.*;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Immutable builder for {@link java.io.ObjectInputStream.GetField}
 *
 * @param <T> the type to deserialize
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class GetFieldBuilder<T> {

    private static final Logger logger = Logger.getLogger(GetFieldBuilder.class.getCanonicalName());
    private final Map<String, ThrowingFunction<ObjectInputStream.GetField, Object, Exception>> getOperations;
    private final T object;
    private final Map<String, Field> declaredFields;
    private final Set<String> serializedFields;

    /**
     * Internal constructor to initialize the fields
     *
     * @param object the instance to deserialize
     */
    protected GetFieldBuilder(T object) {
        this.getOperations = new TreeMap<>();
        this.object = object;

        ObjectStreamClass streamClass = ObjectStreamClass.lookup(object.getClass());
        if (streamClass == null) {
            throw new FieldBuilderException(new NotSerializableException(object.getClass().getCanonicalName()));
        }
        declaredFields = Arrays.stream(object.getClass().getDeclaredFields())
                .collect(Collectors.toMap(Field::getName, Function.identity()));
        serializedFields =
                Arrays.stream(streamClass.getFields()).map(ObjectStreamField::getName).collect(Collectors.toSet());
    }

    /**
     * Creates a new {@code GetFieldBuilder} for the supplied instance
     *
     * @param object the instance to deserialize
     * @param <T>    the type of the instance
     * @return the builder instance
     */
    public static <T> GetFieldBuilder<T> of(T object) {
        return new GetFieldBuilder<>(object);
    }

    /**
     * Reads all serialized fields from the stream
     *
     * @return the builder instance
     */
    public GetFieldBuilder<T> all() {
        return with(serializedFields.toArray(new String[0]));
    }

    /**
     * Reads the fields from the stream
     *
     * @param fieldNames the fields to read
     * @return the builder instance
     */
    public GetFieldBuilder<T> with(String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (!declaredFields.containsKey(fieldName)) {
                throw new FieldBuilderException(new NoSuchFieldException(fieldName));
            }
            Field field = declaredFields.get(fieldName);
            if (field.getType().isPrimitive()) {
                if (field.getType().equals(boolean.class)) {
                    readDefault(fieldName, false);
                } else if (field.getType().equals(byte.class)) {
                    readDefault(fieldName, (byte) 0);
                } else if (field.getType().equals(char.class)) {
                    readDefault(fieldName, (char) 0);
                } else if (field.getType().equals(short.class)) {
                    readDefault(fieldName, (short) 0);
                } else if (field.getType().equals(int.class)) {
                    readDefault(fieldName, 0);
                } else if (field.getType().equals(float.class)) {
                    readDefault(fieldName, (float) 0);
                } else if (field.getType().equals(long.class)) {
                    readDefault(fieldName, (long) 0);
                } else if (field.getType().equals(double.class)) {
                    readDefault(fieldName, (double) 0);
                } else {
                    throw new IllegalStateException("Class <" + field.getType() + "> is not primitive");
                }
            } else {
                readDefault(fieldName, field.getType().cast(null));
            }
        }
        return this;
    }

    /**
     * Skips the fields in the stream
     *
     * @param fieldNames the fields to skip
     * @return the builder instance
     */
    public GetFieldBuilder<T> without(String... fieldNames) {
        for (String name : fieldNames) {
            if (!declaredFields.containsKey(name)) {
                throw new FieldBuilderException(new NoSuchFieldException(name));
            }
            getOperations.remove(name);
        }
        return this;
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param value     the value to set
     * @return the builder instance
     */
    public GetFieldBuilder<T> read(String fieldName, boolean value) {
        return read(fieldName, (Object) value);
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param value     the value to set
     * @return the builder instance
     */
    public GetFieldBuilder<T> read(String fieldName, char value) {
        return read(fieldName, (Object) value);
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param value     the value to set
     * @return the builder instance
     */
    public GetFieldBuilder<T> read(String fieldName, byte value) {
        return read(fieldName, (Object) value);
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param value     the value to set
     * @return the builder instance
     */
    public GetFieldBuilder<T> read(String fieldName, short value) {
        return read(fieldName, (Object) value);
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param value     the value to set
     * @return the builder instance
     */
    public GetFieldBuilder<T> read(String fieldName, int value) {
        return read(fieldName, (Object) value);
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param value     the value to set
     * @return the builder instance
     */
    public GetFieldBuilder<T> read(String fieldName, float value) {
        return read(fieldName, (Object) value);
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param value     the value to set
     * @return the builder instance
     */
    public GetFieldBuilder<T> read(String fieldName, long value) {
        return read(fieldName, (Object) value);
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param value     the value to set
     * @return the builder instance
     */
    public GetFieldBuilder<T> read(String fieldName, double value) {
        return read(fieldName, (Object) value);
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param value     the value to set
     * @return the builder instance
     */
    public GetFieldBuilder<T> read(String fieldName, Object value) {
        assertFieldInDeclaredFields(fieldName);
        Field f = declaredFields.get(fieldName);
        if (f.getType().isPrimitive() && value == null) {
            throw new FieldBuilderException(new IllegalArgumentException("Cannot assign null to primitive property"));
        }
        getOperations.put(fieldName, gf -> value);
        return this;
    }

    /**
     * Reads a field from the stream with the supplied default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readDefault(String fieldName, boolean defaultValue) {
        Field field = assertFieldInDescriptor(fieldName);
        if (field.getType().isPrimitive()) {
            getOperations.put(fieldName, gf -> gf.get(fieldName, defaultValue));
        } else {
            getOperations.put(fieldName, gf -> gf.get(fieldName, Boolean.valueOf(defaultValue)));
        }
        return this;
    }

    /**
     * Reads a field from the stream with the supplied default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readDefault(String fieldName, byte defaultValue) {
        return readNumeric(fieldName, defaultValue);
    }

    /**
     * Reads a field from the stream with the supplied default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readDefault(String fieldName, char defaultValue) {
        Field field = assertFieldInDescriptor(fieldName);
        if (field.getType().isPrimitive()) {
            getOperations.put(fieldName, gf -> gf.get(fieldName, defaultValue));
        } else {
            getOperations.put(fieldName, gf -> gf.get(fieldName, Character.valueOf(defaultValue)));
        }
        return this;
    }

    /**
     * Reads a field from the stream with the supplied default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readDefault(String fieldName, short defaultValue) {
        return readNumeric(fieldName, defaultValue);
    }

    /**
     * Reads a field from the stream with the supplied default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readDefault(String fieldName, int defaultValue) {
        return readNumeric(fieldName, defaultValue);
    }

    /**
     * Reads a field from the stream with the supplied default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readDefault(String fieldName, float defaultValue) {
        return readNumeric(fieldName, defaultValue);
    }

    /**
     * Reads a field from the stream with the supplied default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readDefault(String fieldName, long defaultValue) {
        return readNumeric(fieldName, defaultValue);
    }

    /**
     * Reads a field from the stream with the supplied default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readDefault(String fieldName, double defaultValue) {
        return readNumeric(fieldName, defaultValue);
    }

    /**
     * Reads a field from the stream with the supplied default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readDefault(String fieldName, Object defaultValue) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName,
                gf -> gf.get(fieldName, defaultValue != null ? field.getType().cast(defaultValue) : null));
        return this;
    }

    private GetFieldBuilder<T> readNumeric(String fieldName, Number defaultValue) {
        Field field = assertFieldInDescriptor(fieldName);
        if (field.getType().isPrimitive()) {
            if (field.getType().equals(byte.class)) {
                getOperations.put(fieldName, gf -> gf.get(fieldName, defaultValue.byteValue()));
            } else if (field.getType().equals(short.class)) {
                getOperations.put(fieldName, gf -> gf.get(fieldName, defaultValue.shortValue()));
            } else if (field.getType().equals(int.class)) {
                getOperations.put(fieldName, gf -> gf.get(fieldName, defaultValue.intValue()));
            } else if (field.getType().equals(float.class)) {
                getOperations.put(fieldName, gf -> gf.get(fieldName, defaultValue.floatValue()));
            } else if (field.getType().equals(long.class)) {
                getOperations.put(fieldName, gf -> gf.get(fieldName, defaultValue.longValue()));
            } else if (field.getType().equals(double.class)) {
                getOperations.put(fieldName, gf -> gf.get(fieldName, defaultValue.doubleValue()));
            } else {
                throw new IllegalStateException("Class <" + field.getType() + "> is not numeric");
            }
        } else {
            if (field.getType().equals(Byte.class)) {
                getOperations.put(fieldName, gf -> gf.get(fieldName, Byte.valueOf(defaultValue.byteValue())));
            } else if (field.getType().equals(Short.class)) {
                getOperations.put(fieldName, gf -> gf.get(fieldName, Short.valueOf(defaultValue.shortValue())));
            } else if (field.getType().equals(Integer.class)) {
                getOperations.put(fieldName, gf -> gf.get(fieldName, Integer.valueOf(defaultValue.intValue())));
            } else if (field.getType().equals(Float.class)) {
                getOperations.put(fieldName, gf -> gf.get(fieldName, Float.valueOf(defaultValue.floatValue())));
            } else if (field.getType().equals(Long.class)) {
                getOperations.put(fieldName, gf -> gf.get(fieldName, Long.valueOf(defaultValue.longValue())));
            } else if (field.getType().equals(Double.class)) {
                getOperations.put(fieldName, gf -> gf.get(fieldName, Double.valueOf(defaultValue.doubleValue())));
            } else {
                throw new IllegalStateException("Class <" + field.getType() + "> is not numeric");
            }
        }
        return this;
    }

    /**
     * Reads a field from the stream with the computed default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the supplier to compute a value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputedDefault(String fieldName, BooleanSupplier defaultValue) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName,
                gf -> gf.get(fieldName, field.getType().cast(gf.defaulted(fieldName) && defaultValue.getAsBoolean())));
        return this;
    }

    /**
     * Reads a field from the stream with the computed default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the supplier to compute a value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputedDefault(String fieldName, ByteSupplier defaultValue) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName, gf -> gf.get(fieldName,
                field.getType().cast(gf.defaulted(fieldName) ? defaultValue.getAsByte() : (byte) 0)));
        return this;
    }

    /**
     * Reads a field from the stream with the computed default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the supplier to compute a value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputedDefault(String fieldName, CharSupplier defaultValue) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName, gf -> gf.get(fieldName,
                field.getType().cast(gf.defaulted(fieldName) ? defaultValue.getAsChar() : (char) 0)));
        return this;
    }

    /**
     * Reads a field from the stream with the computed default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the supplier to compute a value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputedDefault(String fieldName, ShortSupplier defaultValue) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName, gf -> gf.get(fieldName,
                field.getType().cast(gf.defaulted(fieldName) ? defaultValue.getAsShort() : (short) 0)));
        return this;
    }

    /**
     * Reads a field from the stream with the computed default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the supplier to compute a value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputedDefault(String fieldName, IntSupplier defaultValue) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName,
                gf -> gf.get(fieldName, field.getType().cast(gf.defaulted(fieldName) ? defaultValue.getAsInt() : 0)));
        return this;
    }

    /**
     * Reads a field from the stream with the computed default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the supplier to compute a value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputedDefault(String fieldName, FloatSupplier defaultValue) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName, gf -> gf.get(fieldName,
                field.getType().cast(gf.defaulted(fieldName) ? defaultValue.getAsFloat() : (float) 0)));
        return this;
    }

    /**
     * Reads a field from the stream with the computed default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the supplier to compute a value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputedDefault(String fieldName, LongSupplier defaultValue) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName, gf -> gf.get(fieldName,
                field.getType().cast(gf.defaulted(fieldName) ? defaultValue.getAsLong() : (long) 0)));
        return this;
    }

    /**
     * Reads a field from the stream with the computed default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the supplier to compute a value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputedDefault(String fieldName, DoubleSupplier defaultValue) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName, gf -> gf.get(fieldName,
                field.getType().cast(gf.defaulted(fieldName) ? defaultValue.getAsDouble() : (double) 0)));
        return this;
    }

    /**
     * Reads a field from the stream with the computed default value
     *
     * @param fieldName    the field to read
     * @param defaultValue the supplier to compute a value if the field is missing in the object descriptor
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputedDefault(String fieldName, Supplier<?> defaultValue) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName,
                gf -> gf.get(fieldName, field.getType().cast(gf.defaulted(fieldName) ? defaultValue.get() : null)));
        return this;
    }

    /**
     * Reads a field from the stream and transforms the value using the function
     *
     * @param fieldName the field to read
     * @param function  the function to transform the value
     * @return the builder instance
     */
    public GetFieldBuilder<T> readTransformed(String fieldName, BooleanUnaryOperator function) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName, gf -> function.applyAsBoolean(gf.get(fieldName, false)));
        return this;
    }

    /**
     * Reads a field from the stream and transforms the value using the function
     *
     * @param fieldName the field to read
     * @param function  the function to transform the value
     * @return the builder instance
     */
    public GetFieldBuilder<T> readTransformed(String fieldName, ByteUnaryOperator function) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName, gf -> function.applyAsByte(gf.get(fieldName, (byte) 0)));
        return this;
    }

    /**
     * Reads a field from the stream and transforms the value using the function
     *
     * @param fieldName the field to read
     * @param function  the function to transform the value
     * @return the builder instance
     */
    public GetFieldBuilder<T> readTransformed(String fieldName, CharUnaryOperator function) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName, gf -> function.applyAsChar(gf.get(fieldName, (char) 0)));
        return this;
    }

    /**
     * Reads a field from the stream and transforms the value using the function
     *
     * @param fieldName the field to read
     * @param function  the function to transform the value
     * @return the builder instance
     */
    public GetFieldBuilder<T> readTransformed(String fieldName, ShortUnaryOperator function) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName, gf -> function.applyAsShort(gf.get(fieldName, (short) 0)));
        return this;
    }

    /**
     * Reads a field from the stream and transforms the value using the function
     *
     * @param fieldName the field to read
     * @param function  the function to transform the value
     * @return the builder instance
     */
    public GetFieldBuilder<T> readTransformed(String fieldName, IntUnaryOperator function) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName, gf -> function.applyAsInt(gf.get(fieldName, (int) 0)));
        return this;
    }

    /**
     * Reads a field from the stream and transforms the value using the function
     *
     * @param fieldName the field to read
     * @param function  the function to transform the value
     * @return the builder instance
     */
    public GetFieldBuilder<T> readTransformed(String fieldName, FloatUnaryOperator function) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName, gf -> function.applyAsFloat(gf.get(fieldName, (float) 0)));
        return this;
    }

    /**
     * Reads a field from the stream and transforms the value using the function
     *
     * @param fieldName the field to read
     * @param function  the function to transform the value
     * @return the builder instance
     */
    public GetFieldBuilder<T> readTransformed(String fieldName, LongUnaryOperator function) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName, gf -> function.applyAsLong(gf.get(fieldName, (long) 0)));
        return this;
    }

    /**
     * Reads a field from the stream and transforms the value using the function
     *
     * @param fieldName the field to read
     * @param function  the function to transform the value
     * @return the builder instance
     */
    public GetFieldBuilder<T> readTransformed(String fieldName, DoubleUnaryOperator function) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName, gf -> function.applyAsDouble(gf.get(fieldName, (double) 0)));
        return this;
    }

    /**
     * Reads a field from the stream and transforms the value using the function
     *
     * @param fieldName the field to read
     * @param function  the function to transform the value
     * @param <V>       the target type of the transformation
     * @return the builder instance
     */
    @SuppressWarnings("unchecked") // unavoidable due to deserialization
    public <V> GetFieldBuilder<T> readTransformed(String fieldName, UnaryOperator<V> function) {
        Field field = assertFieldInDescriptor(fieldName);
        getOperations.put(fieldName, gf -> function.apply((V) gf.get(fieldName, null)));
        return this;
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param function  the supplier to compute a value
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputed(String fieldName, BooleanSupplier function) {
        return readComputed(fieldName, (Supplier<Object>) function::getAsBoolean);
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param function  the supplier to compute a value
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputed(String fieldName, CharSupplier function) {
        return readComputed(fieldName, (Supplier<Object>) function::getAsChar);
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param function  the supplier to compute a value
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputed(String fieldName, ByteSupplier function) {
        return readComputed(fieldName, (Supplier<Object>) function::getAsByte);
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param function  the supplier to compute a value
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputed(String fieldName, ShortSupplier function) {
        return readComputed(fieldName, (Supplier<Object>) function::getAsShort);
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param function  the supplier to compute a value
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputed(String fieldName, IntSupplier function) {
        return readComputed(fieldName, (Supplier<Object>) function::getAsInt);
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param function  the supplier to compute a value
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputed(String fieldName, FloatSupplier function) {
        return readComputed(fieldName, (Supplier<Object>) function::getAsFloat);
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param function  the supplier to compute a value
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputed(String fieldName, LongSupplier function) {
        return readComputed(fieldName, (Supplier<Object>) function::getAsLong);
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param function  the supplier to compute a value
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputed(String fieldName, DoubleSupplier function) {
        return readComputed(fieldName, (Supplier<Object>) function::getAsDouble);
    }

    /**
     * Reads a fixed value into the object
     *
     * @param fieldName the field to read
     * @param function  the supplier to compute a value
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputed(String fieldName, Supplier<Object> function) {
        assertFieldInDeclaredFields(fieldName);
        getOperations.put(fieldName, gf -> function.get());
        return this;
    }

    /**
     * Reads a value from the custom function into the object
     *
     * @param fieldName the field to read
     * @param function  a custom function to read and transform a value from {@code GetField}
     * @return the builder instance
     */
    public GetFieldBuilder<T> readComputed(String fieldName,
                                           ThrowingFunction<ObjectInputStream.GetField, Object, Exception> function) {
        assertFieldInDeclaredFields(fieldName);
        getOperations.put(fieldName, function);
        return this;
    }

    /**
     * Applies the builder by reading the values from the stream into the object
     *
     * @param in the stream to read from
     */
    public void readFields(ObjectInputStream in) {
        try {
            logger.info("getFields: " + getOperations.keySet());
            ObjectInputStream.GetField getField = in.readFields();
            for (Map.Entry<String, ThrowingFunction<ObjectInputStream.GetField, Object, Exception>> op :
                    getOperations.entrySet()) {
                Object value = op.getValue().apply(getField);
                Field f = declaredFields.get(op.getKey());
                f.setAccessible(true);
                if (f.getType().isPrimitive() && value instanceof Number number) {
                    if (f.getType().equals(byte.class) || f.getType().equals(Byte.class)) {
                        value = number.byteValue();
                    } else if (f.getType().equals(short.class) || f.getType().equals(Short.class)) {
                        value = number.shortValue();
                    } else if (f.getType().equals(int.class) || f.getType().equals(Integer.class)) {
                        value = number.intValue();
                    } else if (f.getType().equals(long.class) || f.getType().equals(Long.class)) {
                        value = number.longValue();
                    } else if (f.getType().equals(float.class) || f.getType().equals(Float.class)) {
                        value = number.floatValue();
                    } else if (f.getType().equals(double.class) || f.getType().equals(Double.class)) {
                        value = number.doubleValue();
                    }
                }
                f.set(object, value);
            }
        } catch (Exception e) {
            throw new FieldBuilderException(e);
        }
    }

    private void assertFieldInDeclaredFields(String fieldName) {
        if (!declaredFields.containsKey(fieldName)) {
            throw new FieldBuilderException(new NoSuchFieldException(fieldName));
        }
    }

    private Field assertFieldInDescriptor(String fieldName) {
        if (!serializedFields.contains(fieldName)) {
            throw new FieldBuilderException(
                    "Field '" + fieldName + "' is not part of the serialization descriptor for this version of " +
                            this.object.getClass().getCanonicalName() +
                            ". If you want to set the value of a specific field, use read(fieldName, value) " +
                            "instead.");
        }
        return declaredFields.get(fieldName);
    }
}
