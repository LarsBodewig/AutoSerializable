package dev.bodewig.autoserializable.junit;

import dev.bodewig.autoserializable.api.AutoSerializable;
import dev.bodewig.autoserializable.api.AutoSerializableAll;
import dev.bodewig.autoserializable.api.AutoSerialized;
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
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Factory to create a stream of {@link DynamicTest}s for set of {@link AutoSerialized} types.
 * <p>
 * Unit tests using this factory have to return the stream and use the {@link org.junit.jupiter.api.TestFactory}
 * annotation.
 */
public class AutoSerializableTestFactory {

    /**
     * The classes found during object construction
     */
    protected List<Class<?>> classes;
    /**
     * The custom serializers found during object construction
     */
    protected List<Class<?>> serializers;
    /**
     * A stream of DynamicTests to run
     */
    protected List<DynamicTest> tests;

    /**
     * Creates a new factory instance with the given {@link URI}s
     *
     * @param sources where to find the {@code AutoSerialized} types and {@code AutoSerializer}s
     */
    public AutoSerializableTestFactory(URI... sources) {
        this(List.of(sources));
    }

    /**
     * Creates a new factory instance with the given {@link URI}s
     *
     * @param sources where to find the {@code AutoSerialized} types and {@code AutoSerializer}s
     */
    public AutoSerializableTestFactory(Collection<URI> sources) {
        classes = findClasses(sources);
        serializers = findSerializers(sources);
        tests = List.of();
    }

    // copy constructor
    private AutoSerializableTestFactory(List<Class<?>> classes, List<Class<?>> serializers, List<DynamicTest> tests) {
        this.classes = classes;
        this.serializers = serializers;
        this.tests = tests;
    }

    /**
     * Generates a name using the supplied method name and tested type for dynamic tests.
     *
     * @param method The name of the tested method
     * @return The generated name
     */
    protected static Function<Class<?>, String> nameGenerator(String method) {
        return clazz -> method + "_" + clazz.getName().substring(clazz.getName().lastIndexOf('.') + 1);
    }

    /**
     * Tests a collection of objects for serialization and deserialization failure. Throws if any exception occurs.
     *
     * @param instances the objects to test
     * @return a stream of {@link DynamicTest}s to be executed by JUnit
     */
    public static Stream<DynamicTest> testSerialization(Object... instances) {
        return testSerialization(List.of(instances));
    }

    /**
     * Tests a collection of objects for serialization and deserialization failure. Throws if any exception occurs.
     *
     * @param instances the objects to test
     * @return a stream of {@link DynamicTest}s to be executed by JUnit
     */
    public static Stream<DynamicTest> testSerialization(Collection<Object> instances) {
        return DynamicTest.stream(instances.stream(),
                instance -> "testSerialization_" + instance.getClass().getSimpleName(),
                AutoSerializableTestFactory::testSerialization);
    }

    /**
     * Tests a single object instance for serialization and deserialization failure. Throws if any exception occurs.
     *
     * @param instance the object to test
     */
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

    private AutoSerializableTestFactory test(Stream<Class<?>> inputStream, String testName,
                                             ThrowingConsumer<Class<?>> testExecutor) {
        return new AutoSerializableTestFactory(classes, serializers, Stream.concat(tests.stream(),
                DynamicTest.stream(inputStream, nameGenerator(testName),
                        clazz -> assertDoesNotThrow(() -> testExecutor.accept(clazz), testName + " failed on " +
                                Optional.ofNullable(clazz.getCanonicalName()).orElse("(anonymous class)")))).toList());
    }

    /**
     * Finds all classes in a collection of classpath elements.
     *
     * @param uris The classpath elements to search
     * @return The classes
     */
    protected List<Class<?>> findClasses(Collection<URI> uris) {
        return uris.stream()
                .flatMap(uri -> ReflectionUtils.findAllClassesInClasspathRoot(uri, x -> true, x -> true).stream())
                .distinct().toList();
    }

    /**
     * Finds the annotated custom serializers from a collection of classpath elements.
     *
     * @param uris The classpath elements to search
     * @return The custom serializers
     */
    protected List<Class<?>> findSerializers(Collection<URI> uris) {
        return uris.stream().flatMap(uri -> ReflectionUtils.findAllClassesInClasspathRoot(uri, ClassFilter.of(
                clazz -> clazz.isAnnotationPresent(AutoSerializable.class) ||
                        clazz.isAnnotationPresent(AutoSerializableAll.class) ||
                        AutoSerializer.class.isAssignableFrom(clazz))).stream()).distinct().toList();
    }

    /**
     * Returns a stream of {@link DynamicTest}s containing the selected tests for each source type
     * <p>
     * Unit tests have to return this stream and use the {@link org.junit.jupiter.api.TestFactory} annotation.
     *
     * @return the stream containing the {@link DynamicTest}s
     */
    public Stream<DynamicTest> build() {
        return tests.stream();
    }

    /**
     * Adds tests for all classes except {@link AutoSerializer}s to implement {@link Serializable} to the factory
     * instance.
     *
     * @return the factory instance
     */
    public AutoSerializableTestFactory testAllClassesImplementSerializable() {
        return test(classes.stream().filter(clazz -> !clazz.isAnnotation() && !serializers.contains(clazz)),
                "testAllClassesImplementSerializable", clazz -> assertTrue(Serializable.class.isAssignableFrom(clazz),
                        clazz.getCanonicalName() + " does not implement " + Serializable.class.getCanonicalName()));
    }

    /**
     * Adds tests for all classes implementing {@link AutoSerializer} to be annotated with {@link AutoSerializable}
     * to the factory instance.
     *
     * @return the factory instance
     */
    public AutoSerializableTestFactory testSerializersAnnotated() {
        return test(serializers.stream(), "testSerializersAnnotated",
                serializer -> assertTrue(AutoSerializer.class.isAssignableFrom(serializer),
                        "Class " + serializer.getCanonicalName() + " is annotated with " +
                                AutoSerializable.class.getCanonicalName() + " but does not extend " +
                                AutoSerializer.class));
    }

    /**
     * Adds tests for all classes annotated with {@link AutoSerializable} to implement {@link AutoSerializer} to the
     * factory instance.
     *
     * @return the factory instance
     */
    public AutoSerializableTestFactory testSerializersExtend() {
        return test(serializers.stream(), "testSerializersExtend", serializer -> assertTrue(
                serializer.isAnnotationPresent(AutoSerializable.class) ||
                        serializer.isAnnotationPresent(AutoSerializableAll.class),
                "Class " + serializer.getCanonicalName() + " extends " + AutoSerializer.class.getCanonicalName() +
                        " but is not annotated with " + AutoSerializable.class));
    }

    /**
     * Adds tests for all classes annotated with {@link AutoSerializable} to match a class in the provided scope to
     * the factory instance.
     *
     * @return the factory instance
     */
    public AutoSerializableTestFactory testSerializersUsed() {
        return test(serializers.stream(), "testSerializersUsed", serializer -> assertFalse(
                AnnotationUtils.findRepeatableAnnotations(serializer, AutoSerializable.class).stream()
                        .map(AutoSerializable::value).noneMatch(classes::contains),
                "AutoSerializer " + serializer.getCanonicalName() + " is annotated with " +
                        AutoSerializable.class.getCanonicalName() +
                        " but does not target any class in the provided test scope"));
    }

    /**
     * Adds tests for all classes except {@link AutoSerializer}s to have an initialized serializer field to the
     * factory instance.
     *
     * @return the factory instance
     */
    public AutoSerializableTestFactory testAutoSerializablesInitialized() {
        return test(classes.stream()
                .filter(clazz -> !clazz.isAnnotation() && !clazz.isInterface() && !serializers.contains(clazz))
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

    /**
     * Adds tests for all classes except {@link AutoSerializer}s to be annotated with {@link AutoSerialized} to the
     * factory instance.
     *
     * @return the factory instance
     */
    public AutoSerializableTestFactory testAnnotatedAutoSerialized() {
        return test(classes.stream().filter(clazz -> !clazz.isAnnotation() && !serializers.contains(clazz)),
                "testAnnotatedAutoSerialized", clazz -> assertTrue(clazz.isAnnotationPresent(AutoSerialized.class),
                        "Type " + clazz.getCanonicalName() + " was not annotated with " +
                                AutoSerialized.class.getCanonicalName()));
    }
}
