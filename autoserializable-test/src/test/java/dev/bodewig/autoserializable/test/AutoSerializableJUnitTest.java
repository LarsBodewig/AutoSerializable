package dev.bodewig.autoserializable.test;

import dev.bodewig.autoserializable.junit.AutoSerializableTestFactory;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

class AutoSerializableJUnitTest {

    @TestFactory
    Stream<DynamicTest> testAll() {
        List<URI> sources;
        try (ScanResult scanResult = new ClassGraph().ignoreClassVisibility()
                .filterClasspathElements(
                        path -> path.endsWith("/autoserializable-test/target/classes")
                            || path.endsWith("/build/classes/java/main")
                            || path.contains("/build/transformedJars"))
                .scan()) {
            sources = scanResult.getClasspathURIs();
        }
        return new AutoSerializableTestFactory(sources).testAllClassesImplementSerializable().testSerializersAnnotated()
                .testSerializersExtend().testSerializersUsed().testAutoSerializablesInitialized()
                .testAnnotatedAutoSerialized().build();
    }
}
