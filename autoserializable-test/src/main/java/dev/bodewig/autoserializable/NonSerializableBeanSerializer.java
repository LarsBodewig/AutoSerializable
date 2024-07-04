package dev.bodewig.autoserializable;

import dev.bodewig.autoserializable.api.AutoSerializable;
import dev.bodewig.autoserializable.api.AutoSerializer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@AutoSerializable(NonSerializableBean.class)
public class NonSerializableBeanSerializer implements AutoSerializer<NonSerializableBean> {

    @Override
    public void writeObject(ObjectOutputStream out, NonSerializableBean that) {
        // noop
    }

    @Override
    public void readObject(ObjectInputStream in, NonSerializableBean that) {
        that.io = System.out;
    }
}
