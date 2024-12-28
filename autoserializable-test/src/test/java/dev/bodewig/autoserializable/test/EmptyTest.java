package dev.bodewig.autoserializable.test;

import dev.bodewig.autoserializable.junit.AutoSerializableTestFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.stream.Stream;

public class EmptyTest {

    static AutoSerializableTestFactory factory;

    @BeforeAll
    static void setup() {
        factory = new AutoSerializableTestFactory(List.of());
    }

    @TestFactory
    Stream<DynamicTest> testAllClassesImplementSerializable() {
        return factory.testAllClassesImplementSerializable().build();
    }

    @TestFactory
    Stream<DynamicTest> testSerializersAnnotated() {
        return factory.testSerializersAnnotated().build();
    }
}
