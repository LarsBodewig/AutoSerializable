package dev.bodewig.autoserializable.api;

import dev.bodewig.autoserializable.api.function.*;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Immutable builder for {@link java.io.ObjectOutputStream.PutField}
 *
 * @param <T> the type to serialize
 */
@SuppressWarnings("unused")
public class PutFieldBuilder<T> {

    private static final Logger logger = Logger.getLogger(PutFieldBuilder.class.getCanonicalName());
    private final Map<String, Consumer<ObjectOutputStream.PutField>> putOperations;
    private final T object;
    private final Map<String, ObjectStreamField> serializedFields;

    /**
     * Internal constructor to initialize the fields
     * @param object the instance to serialize
     */
    protected PutFieldBuilder(T object) {
        this.putOperations = new TreeMap<>();
        this.object = object;

        ObjectStreamClass streamClass = ObjectStreamClass.lookup(object.getClass());
        if (streamClass == null) {
            throw new FieldBuilderException(new NotSerializableException(object.getClass().getCanonicalName()));
        }
        serializedFields = Arrays.stream(streamClass.getFields())
                .collect(Collectors.toMap(ObjectStreamField::getName, Function.identity()));
    }

    /**
     * Creates a new {@code PutFieldBuilder} for the supplied instance
     *
     * @param object the instance to serialize
     * @return the builder instance
     * @param <T> the type of the instance
     */
    public static <T> PutFieldBuilder<T> of(T object) {
        return new PutFieldBuilder<>(object);
    }

    /**
     * Writes all fields from the object descriptor to the stream
     *
     * @return the builder instance
     */
    public PutFieldBuilder<T> all() {
        return with(serializedFields.keySet().toArray(new String[0]));
    }

    /**
     * Writes the fields to the stream
     *
     * @param fieldNames the fields to write
     * @return the builder instance
     */
    public PutFieldBuilder<T> with(String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                Field f = object.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                Object value = f.get(object);
                if (f.getType().isPrimitive()) {
                    if (f.getType().equals(boolean.class)) {
                        putOperations.put(fieldName, pf -> pf.put(fieldName, (boolean) value));
                    } else if (f.getType().equals(byte.class)) {
                        putOperations.put(fieldName, pf -> pf.put(fieldName, (byte) value));
                    } else if (f.getType().equals(char.class)) {
                        putOperations.put(fieldName, pf -> pf.put(fieldName, (char) value));
                    } else if (f.getType().equals(short.class)) {
                        putOperations.put(fieldName, pf -> pf.put(fieldName, (short) value));
                    } else if (f.getType().equals(int.class)) {
                        putOperations.put(fieldName, pf -> pf.put(fieldName, (int) value));
                    } else if (f.getType().equals(float.class)) {
                        putOperations.put(fieldName, pf -> pf.put(fieldName, (float) value));
                    } else if (f.getType().equals(long.class)) {
                        putOperations.put(fieldName, pf -> pf.put(fieldName, (long) value));
                    } else if (f.getType().equals(double.class)) {
                        putOperations.put(fieldName, pf -> pf.put(fieldName, (double) value));
                    } else {
                        throw new IllegalStateException("Class <" + value.getClass() + "> is not primitive");
                    }
                } else {
                    putOperations.put(fieldName, pf -> pf.put(fieldName, value));
                }
            } catch (IllegalStateException | IllegalAccessException | NoSuchFieldException e) {
                throw new FieldBuilderException(e);
            }
        }
        return this;
    }

    /**
     * Skips the fields from being written
     *
     * @param fieldNames the fields to skip
     * @return the builder instance
     */
    public PutFieldBuilder<T> without(String... fieldNames) {
        for (String name : fieldNames) {
            if (!serializedFields.containsKey(name)) {
                throw new FieldBuilderException(new NoSuchFieldException(name));
            }
            putOperations.remove(name);
        }
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param value the value to write
     * @return the builder instance
     */
    public PutFieldBuilder<T> put(String fieldName, boolean value) {
        assertFieldInDescriptor(fieldName);
        if (!serializedFields.get(fieldName).getType().isPrimitive()) {
            return put(fieldName, (Object) value);
        }
        putOperations.put(fieldName, pf -> pf.put(fieldName, value));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param value the value to write
     * @return the builder instance
     */
    public PutFieldBuilder<T> put(String fieldName, byte value) {
        assertFieldInDescriptor(fieldName);
        if (!serializedFields.get(fieldName).getType().isPrimitive()) {
            return put(fieldName, (Object) value);
        }
        putOperations.put(fieldName, pf -> pf.put(fieldName, value));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param value the value to write
     * @return the builder instance
     */
    public PutFieldBuilder<T> put(String fieldName, char value) {
        assertFieldInDescriptor(fieldName);
        if (!serializedFields.get(fieldName).getType().isPrimitive()) {
            return put(fieldName, (Object) value);
        }
        putOperations.put(fieldName, pf -> pf.put(fieldName, value));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param value the value to write
     * @return the builder instance
     */
    public PutFieldBuilder<T> put(String fieldName, short value) {
        assertFieldInDescriptor(fieldName);
        if (!serializedFields.get(fieldName).getType().isPrimitive()) {
            return put(fieldName, (Object) value);
        }
        putOperations.put(fieldName, pf -> pf.put(fieldName, value));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param value the value to write
     * @return the builder instance
     */
    public PutFieldBuilder<T> put(String fieldName, int value) {
        assertFieldInDescriptor(fieldName);
        if (!serializedFields.get(fieldName).getType().isPrimitive()) {
            return put(fieldName, (Object) value);
        }
        putOperations.put(fieldName, pf -> pf.put(fieldName, value));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param value the value to write
     * @return the builder instance
     */
    public PutFieldBuilder<T> put(String fieldName, float value) {
        assertFieldInDescriptor(fieldName);
        if (!serializedFields.get(fieldName).getType().isPrimitive()) {
            return put(fieldName, (Object) value);
        }
        putOperations.put(fieldName, pf -> pf.put(fieldName, value));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param value the value to write
     * @return the builder instance
     */
    public PutFieldBuilder<T> put(String fieldName, long value) {
        assertFieldInDescriptor(fieldName);
        if (!serializedFields.get(fieldName).getType().isPrimitive()) {
            return put(fieldName, (Object) value);
        }
        putOperations.put(fieldName, pf -> pf.put(fieldName, value));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param value the value to write
     * @return the builder instance
     */
    public PutFieldBuilder<T> put(String fieldName, double value) {
        assertFieldInDescriptor(fieldName);
        if (!serializedFields.get(fieldName).getType().isPrimitive()) {
            return put(fieldName, (Object) value);
        }
        putOperations.put(fieldName, pf -> pf.put(fieldName, value));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param value the value to write
     * @return the builder instance
     */
    public PutFieldBuilder<T> put(String fieldName, Object value) {
        assertFieldInDescriptor(fieldName);
        putOperations.put(fieldName, pf -> pf.put(fieldName, value));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param function the supplier to compute a value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putComputed(String fieldName, BooleanSupplier function) {
        assertFieldInDescriptor(fieldName);
        if (!serializedFields.get(fieldName).getType().isPrimitive()) {
            return putComputed(fieldName, (Supplier<Object>) function::getAsBoolean);
        }
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.getAsBoolean()));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param function the supplier to compute a value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putComputed(String fieldName, CharSupplier function) {
        assertFieldInDescriptor(fieldName);
        if (!serializedFields.get(fieldName).getType().isPrimitive()) {
            return putComputed(fieldName, (Supplier<Object>) function::getAsChar);
        }
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.getAsChar()));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param function the supplier to compute a value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putComputed(String fieldName, ByteSupplier function) {
        assertFieldInDescriptor(fieldName);
        if (!serializedFields.get(fieldName).getType().isPrimitive()) {
            return putComputed(fieldName, (Supplier<Object>) function::getAsByte);
        }
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.getAsByte()));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param function the supplier to compute a value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putComputed(String fieldName, ShortSupplier function) {
        assertFieldInDescriptor(fieldName);
        if (!serializedFields.get(fieldName).getType().isPrimitive()) {
            return putComputed(fieldName, (Supplier<Object>) function::getAsShort);
        }
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.getAsShort()));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param function the supplier to compute a value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putComputed(String fieldName, IntSupplier function) {
        assertFieldInDescriptor(fieldName);
        if (!serializedFields.get(fieldName).getType().isPrimitive()) {
            return putComputed(fieldName, (Supplier<Object>) function::getAsInt);
        }
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.getAsInt()));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param function the supplier to compute a value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putComputed(String fieldName, FloatSupplier function) {
        assertFieldInDescriptor(fieldName);
        if (!serializedFields.get(fieldName).getType().isPrimitive()) {
            return putComputed(fieldName, (Supplier<Object>) function::getAsFloat);
        }
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.getAsFloat()));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param function the supplier to compute a value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putComputed(String fieldName, LongSupplier function) {
        assertFieldInDescriptor(fieldName);
        if (!serializedFields.get(fieldName).getType().isPrimitive()) {
            return putComputed(fieldName, (Supplier<Object>) function::getAsLong);
        }
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.getAsLong()));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param function the supplier to compute a value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putComputed(String fieldName, DoubleSupplier function) {
        assertFieldInDescriptor(fieldName);
        if (!serializedFields.get(fieldName).getType().isPrimitive()) {
            return putComputed(fieldName, (Supplier<Object>) function::getAsDouble);
        }
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.getAsDouble()));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param function the supplier to compute a value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putComputed(String fieldName, Supplier<?> function) {
        assertFieldInDescriptor(fieldName);
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.get()));
        return this;
    }

    /**
     * Writes a fixed value into the stream
     *
     * @param fieldName the field to write
     * @param function the supplier to compute a value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putComputed(String fieldName, Consumer<ObjectOutputStream.PutField> function) {
        assertFieldInDescriptor(fieldName);
        putOperations.put(fieldName, function);
        return this;
    }

    /**
     * Writes a transformed value into the stream
     *
     * @param fieldName the field to write
     * @param function the function to transform the value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putTransformed(String fieldName, BooleanUnaryOperator function) {
        boolean value = getFieldValue(fieldName);
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.applyAsBoolean(value)));
        return this;
    }

    /**
     * Writes a transformed value into the stream
     *
     * @param fieldName the field to write
     * @param function the function to transform the value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putTransformed(String fieldName, CharUnaryOperator function) {
        char value = getFieldValue(fieldName);
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.applyAsChar(value)));
        return this;
    }

    /**
     * Writes a transformed value into the stream
     *
     * @param fieldName the field to write
     * @param function the function to transform the value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putTransformed(String fieldName, ByteUnaryOperator function) {
        byte value = getFieldValue(fieldName);
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.applyAsByte(value)));
        return this;
    }

    /**
     * Writes a transformed value into the stream
     *
     * @param fieldName the field to write
     * @param function the function to transform the value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putTransformed(String fieldName, ShortUnaryOperator function) {
        short value = getFieldValue(fieldName);
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.applyAsShort(value)));
        return this;
    }

    /**
     * Writes a transformed value into the stream
     *
     * @param fieldName the field to write
     * @param function the function to transform the value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putTransformed(String fieldName, IntUnaryOperator function) {
        int value = getFieldValue(fieldName);
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.applyAsInt(value)));
        return this;
    }

    /**
     * Writes a transformed value into the stream
     *
     * @param fieldName the field to write
     * @param function the function to transform the value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putTransformed(String fieldName, FloatUnaryOperator function) {
        float value = getFieldValue(fieldName);
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.applyAsFloat(value)));
        return this;
    }

    /**
     * Writes a transformed value into the stream
     *
     * @param fieldName the field to write
     * @param function the function to transform the value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putTransformed(String fieldName, LongUnaryOperator function) {
        long value = getFieldValue(fieldName);
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.applyAsLong(value)));
        return this;
    }

    /**
     * Writes a transformed value into the stream
     *
     * @param fieldName the field to write
     * @param function the function to transform the value
     * @return the builder instance
     */
    public PutFieldBuilder<T> putTransformed(String fieldName, DoubleUnaryOperator function) {
        double value = getFieldValue(fieldName);
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.applyAsDouble(value)));
        return this;
    }

    /**
     * Writes a transformed value into the stream
     *
     * @param fieldName the field to write
     * @param function the function to transform the value
     * @param <V>       the target type of the transformation
     * @return the builder instance
     */
    public <V> PutFieldBuilder<T> putTransformed(String fieldName, UnaryOperator<V> function) {
        V value = getFieldValue(fieldName);
        putOperations.put(fieldName, pf -> pf.put(fieldName, function.apply(value)));
        return this;
    }

    /**
     * Applies the builder by writing the values to the stream
     *
     * @param out the stream to write to
     */
    public void writeFields(ObjectOutputStream out) {
        try {
            ObjectOutputStream.PutField putField = out.putFields();
            logger.info("putFields: " + putOperations.keySet());
            putOperations.values().forEach(op -> op.accept(putField));
            out.writeFields();
        } catch (IOException e) {
            throw new FieldBuilderException(e);
        }
    }

    @SuppressWarnings("unchecked") // responsibility of the caller
    private <V> V getFieldValue(String fieldName) {
        try {
            Field f = object.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (V) f.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new FieldBuilderException(e);
        }
    }

    private void assertFieldInDescriptor(String fieldName) {
        if (!serializedFields.containsKey(fieldName)) {
            throw new FieldBuilderException(
                    "Field '" + fieldName + "' is not part of the serialization descriptor for this version of " +
                            this.object.getClass().getCanonicalName() +
                            ". Did you exclude it by setting @SerialPersistenFields?");
        }
    }
}
