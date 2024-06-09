package dev.bodewig.autoserializable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.*;

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
    void write() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(new TestBean(0));
            oos.flush();
        }
    }

    @Test
    void read() throws IOException, ClassNotFoundException {
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
    void wasInitalized() {
        assertNotNull(NonSerializableBean._writeObject);
        assertNotNull(TestInterface.Impl._writeObject);
    }

    @Test
    void customSerializer() {
        Executable trySerialize = () -> {
            try (ObjectOutputStream oos = new ObjectOutputStream(OutputStream.nullOutputStream())) {
                oos.writeObject(new NonSerializableBean());
            }
        };
        assertThrows(NotSerializableException.class, trySerialize);
        NonSerializableBean._writeObject = out -> {};
        assertDoesNotThrow(trySerialize);
    }
}
