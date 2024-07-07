package dev.bodewig.autoserializable.test;

import dev.bodewig.autoserializable.junit.AutoSerializableTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.commons.util.ReflectionUtils;

import java.net.URI;
import java.util.stream.Stream;

@DisplayName("AutoSerializable Test")
@Tag("autoserializable")
public class AutoSerializableJUnitTest {

    @TestFactory
    public Stream<DynamicTest> testAll() {
        URI sources =
                ReflectionUtils.getAllClasspathRootDirectories().stream().filter(path -> !path.endsWith("test-classes"))
                        .findAny().get().toUri(); // TODO: change to mvn
        return new AutoSerializableTestFactory(sources).testAllClassesImplementSerializable()
                .testSerializersAnnotated().testSerializersExtend().testSerializersUsed()
                .testAutoSerializablesInitialized().testClassesWithNoArgConstructorReadWrite().build();
    }
}
