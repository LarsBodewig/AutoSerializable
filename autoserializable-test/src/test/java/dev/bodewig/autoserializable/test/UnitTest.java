package dev.bodewig.autoserializable.test;

import dev.bodewig.autoserializable.junit.AutoSerializableTestFactory;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class UnitTest {

    @Test
    void instance() {
        assertInstanceOf(Serializable.class, new TestBean(0));

        assertInstanceOf(Serializable.class, new BeanWithNoArgsConstructor.Explicit());
        assertInstanceOf(Serializable.class, new BeanWithNoArgsConstructor.Implicit());
        assertInstanceOf(Serializable.class, new BeanWithNoArgsConstructor.Inherited());

        assertInstanceOf(Serializable.class, new BeanWithNullAnnotation());
        assertInstanceOf(Serializable.class, new BeanWithNullAnnotation.AnnotedType());

        assertInstanceOf(Serializable.class, AnonymousClass.impl);
        assertInstanceOf(Serializable.class, AnonymousClass.second);
    }

    @Test
    void visibility() {
        new BeanWithNoArgsConstructor.Implicit();
        new TestInterface.Impl();
    }

    @Test
    void writeRead() {
        AutoSerializableTestFactory.testSerialization(new TestBean(0));
    }

    @Test
    void data() throws IOException, ClassNotFoundException {
        byte[] data;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos);) {
            oos.writeObject(new TestBean(1));
            oos.flush();
            data = baos.toByteArray();
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            Object obj = ois.readObject();
            assertInstanceOf(TestBean.class, obj);
            TestBean bean = (TestBean) obj;
            assertEquals(1, bean.zero);
        }
    }

    @Test
    void wasInitalized() throws IllegalAccessException, NoSuchFieldException {
        Field f1 = NonSerializableBean.class.getDeclaredField("_serializer");
        f1.setAccessible(true);
        assertNotNull(f1.get(null));

        Field f2 = TestInterface.Impl.class.getDeclaredField("_serializer");
        f2.setAccessible(true);
        assertNotNull(f2.get(null));
    }

    @Test
    void customSerializer() {
        AutoSerializableTestFactory.testSerialization(new NonSerializableBean());
    }
}
