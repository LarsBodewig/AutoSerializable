package dev.bodewig.autoserializable.test;

import dev.bodewig.autoserializable.api.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@SuppressWarnings("unused")
@AutoSerializable(FieldBuilderBean.class)
@SerialPersistentFields({"a", "b", "c", "f", "g", "h"})
public class FieldBuilderBeanSerializer extends AutoSerializer<FieldBuilderBean> {

    @Override
    public void writeObject(ObjectOutputStream out, FieldBuilderBean that) {
        PutFieldBuilder.of(that) //
                .all() //
                .without("b", "c") //
                .put("b", 5) //
                .putComputed("c", () -> true) //
                .writeFields(out);
    }

    @Override
    public void readObject(ObjectInputStream in, FieldBuilderBean that) {
        GetFieldBuilder.of(that) //
                .all() //
                .readComputed("d", () -> "new") //
                .readDefault("b", 1) //
                .read("e", 5) //
                .read("io", System.out) //
                .readFields(in);
    }
}
