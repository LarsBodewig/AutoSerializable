package dev.bodewig.autoserializable.junit;

import dev.bodewig.autoserializable.api.AutoSerializable;
import dev.bodewig.autoserializable.api.AutoSerializableAll;
import dev.bodewig.autoserializable.api.AutoSerializer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.commons.util.ReflectionUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class AutoSerializableTestFactory {

    protected Set<Class<?>> classes;
    protected Set<Class<?>> serializers;
    protected Stream<DynamicTest> tests;

    public AutoSerializableTestFactory(URI... sources) {
        this(List.of(sources));
    }

    public AutoSerializableTestFactory(Collection<URI> sources) {
        classes = findClasses(sources);
        serializers = findSerializers(sources);
        tests = Stream.empty();
    }

    // copy constructor
    private AutoSerializableTestFactory(Set<Class<?>> classes, Set<Class<?>> serializers, Stream<DynamicTest> tests) {
        this.classes = classes;
        this.serializers = serializers;
        this.tests = tests;
    }

    private AutoSerializableTestFactory test(Stream<Class<?>> inputStream, String testName,
                                             ThrowingConsumer<Class<?>> testExecutor) {
        return new AutoSerializableTestFactory(classes, serializers, Stream.concat(tests,
                DynamicTest.stream(inputStream, nameGenerator(testName),
                        clazz -> assertDoesNotThrow(() -> testExecutor.accept(clazz), testName + " failed on " +
                                Optional.ofNullable(clazz.getCanonicalName()).orElse("(anonymous class)")))));
    }

    protected Set<Class<?>> findClasses(Collection<URI> uris) {
        return uris.stream()
                .flatMap(uri -> ReflectionUtils.findAllClassesInClasspathRoot(uri, x -> true, x -> true).stream())
                .collect(Collectors.toSet());
    }

    protected Set<Class<?>> findSerializers(Collection<URI> uris) {
        return uris.stream().flatMap(uri -> ReflectionUtils.findAllClassesInClasspathRoot(uri, ClassFilter.of(
                clazz -> AnnotationUtils.isAnnotated(clazz, AutoSerializable.class) ||
                        AnnotationUtils.isAnnotated(clazz, AutoSerializableAll.class) ||
                        AutoSerializer.class.isAssignableFrom(clazz))).stream()).collect(Collectors.toSet());
    }

    protected static Function<Class<?>, String> nameGenerator(String method) {
        return clazz -> method + "_" + clazz.getName().substring(clazz.getName().lastIndexOf('.') + 1);
    }

    public Stream<DynamicTest> build() {
        return tests;
    }

    public AutoSerializableTestFactory testAllClassesImplementSerializable() {
        return test(classes.stream().filter(Predicate.not(serializers::contains)),
                "testAllClassesImplementSerializable", clazz -> assertTrue(Serializable.class.isAssignableFrom(clazz),
                        clazz.getCanonicalName() + " does not implement " + Serializable.class.getCanonicalName()));
    }

    public AutoSerializableTestFactory testSerializersAnnotated() {
        return test(serializers.stream(), "testSerializersAnnotated",
                serializer -> assertTrue(AutoSerializer.class.isAssignableFrom(serializer),
                        "Class " + serializer.getCanonicalName() + " is annotated with " +
                                AutoSerializable.class.getCanonicalName() + " but does not extend " +
                                AutoSerializer.class));
    }

    public AutoSerializableTestFactory testSerializersExtend() {
        return test(serializers.stream(), "testSerializersExtend", serializer -> assertTrue(
                AnnotationUtils.isAnnotated(serializer, AutoSerializable.class) ||
                        AnnotationUtils.isAnnotated(serializer, AutoSerializableAll.class),
                "Class " + serializer.getCanonicalName() + " extends " + AutoSerializer.class.getCanonicalName() +
                        " but is not annotated with " + AutoSerializable.class));
    }

    public AutoSerializableTestFactory testSerializersUsed() {
        return test(serializers.stream(), "testSerializersUsed", serializer -> assertFalse(
                AnnotationUtils.findRepeatableAnnotations(serializer, AutoSerializable.class).stream()
                        .map(AutoSerializable::value).noneMatch(classes::contains),
                "AutoSerializer " + serializer.getCanonicalName() + " is annotated with " +
                        AutoSerializable.class.getCanonicalName() +
                        " but does not target any class in the provided test scope"));
    }

    public AutoSerializableTestFactory testAutoSerializablesInitialized() {
        return test(
                classes.stream().filter(Predicate.not(serializers::contains)).filter(Predicate.not(Class::isInterface))
                        .filter(clazz -> {
                            try {
                                clazz.getDeclaredField("_serializer");
                            } catch (NoSuchFieldException e) {
                                return false;
                            }
                            return true;
                        }), "testAutoSerializablesInitialized", clazz -> {
                    Field field = clazz.getDeclaredField("_serializer");
                    field.setAccessible(true);
                    assertNotNull(field.get(null), // null for static field
                            "Generated constant '_serializer' of " + clazz.getCanonicalName() +
                                    " was not initialized. The byte code is defect.");
                });
    }

    public static Stream<DynamicTest> testSerialization(Object... instances) {
        return testSerialization(List.of(instances));
    }

    public static Stream<DynamicTest> testSerialization(Collection<Object> instances) {
        return DynamicTest.stream(instances.stream(),
                instance -> "testSerialization_" + instance.getClass().getSimpleName(),
                AutoSerializableTestFactory::testSerialization);
    }

    public static void testSerialization(Object instance) {
        assertDoesNotThrow(() -> {
            byte[] data;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(instance);
                data = baos.toByteArray();
            }
            try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                ois.readObject();
            }
        }, "testSerialization failed on " +
                Optional.ofNullable(instance.getClass().getCanonicalName()).orElse("(anonymous class)"));
    }
}
