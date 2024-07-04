package dev.bodewig.autoserializable.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface AutoSerializer<T> {

    void writeObject(ObjectOutputStream out, T that) throws IOException;

    void readObject(ObjectInputStream in, T that) throws IOException, ClassNotFoundException;

    class DefaultSerializer implements AutoSerializer<Object> {

        @Override
        public void writeObject(ObjectOutputStream out, Object that) throws IOException {
            out.defaultWriteObject();
        }

        @Override
        public void readObject(ObjectInputStream in, Object that) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
        }
    }
}
