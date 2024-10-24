package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.jupiter.api.Test;

public class UnitTest {

	@Test
	public void instance() {
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
	public void visibility() {
		new BeanWithNoArgsConstructor.Implicit();
		new TestInterface.Impl();
	}

	@Test
	public void write() throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(new TestBean(0));
			oos.flush();
		}
	}

	@Test
	public void read() throws IOException, ClassNotFoundException {
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
}
