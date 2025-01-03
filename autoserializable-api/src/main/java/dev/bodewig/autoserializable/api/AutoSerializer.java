package dev.bodewig.autoserializable.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * AutoSerializers can be used to customize the serialization and deserialization methods injected in the {@code
 * AutoSerializablePlugin}.
 * <p>
 * Custom implementations have to extend this default implementation of {@code Serializable} and specify the {@link
 * AutoSerializable} annotation.
 *
 * @param <T> the type to serialize and deserialize
 */
public class AutoSerializer<T> {

    /**
     * Default constructor
     */
    public AutoSerializer() {
    }

    /**
     * The custom serializer injected by the {@code AutoSerializablePlugin}
     *
     * @param out  the stream to write to
     * @param that the object to serialize
     * @throws IOException if I/O errors occur while writing to the underlying {@link java.io.OutputStream}
     */
    public void writeObject(ObjectOutputStream out, T that) throws IOException {
        out.defaultWriteObject();
    }

    /**
     * The custom deserializer injected by the {@code AutoSerializablePlugin}
     *
     * @param in   the stream to read from
     * @param that the object to deserialize
     * @throws IOException            if an I/O error occurs
     * @throws ClassNotFoundException if the class of a serialized object could not be found
     */
    public void readObject(ObjectInputStream in, T that) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
}
