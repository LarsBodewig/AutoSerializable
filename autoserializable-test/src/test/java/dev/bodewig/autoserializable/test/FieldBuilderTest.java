package dev.bodewig.autoserializable.test;

import dev.bodewig.autoserializable.junit.AutoSerializableTestFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FieldBuilderTest {

    @Test
    void serializable() {
        AutoSerializableTestFactory.testSerialization(new FieldBuilderBean());
    }

    @Test
    void values() {
        FieldBuilderBean original = new FieldBuilderBean();
        FieldBuilderBean clone = AutoSerializableTestFactory.testSerialization(original);
        Assertions.assertEquals(original, clone);
    }
}
