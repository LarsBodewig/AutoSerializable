[![Available on Maven Central](https://img.shields.io/maven-central/v/dev.bodewig.autoserializable/autoserializable?label=Available%20on%20Maven%20Central)](https://central.sonatype.com/namespace/dev.bodewig.autoserializable)

# AutoSerializable

AutoSerializable is a [ByteBuddy](https://github.com/raphw/byte-buddy) plugin to mark all classes
as [Serializable](https://docs.oracle.com/en/java/javase/19/docs/api/java.base/java/io/Serializable.html) even after
compilation.

This plugin allows you to serialize 3rd-party library classes without modifying the library code or writing custom
serializers/deserializers for each class (which might not even be possible due to limitations in visiblity and
modularity).

## Usage

To use the plugin, you need to configure it with ByteBuddy. ByteBuddy offers plugins for maven and gradle with different
mojos and tasks to transform class files or whole JARs.

You can find examples for maven and gradle in the *autoserializable-test* project.
For gradle, however, there is a combined *autoserializable-gradle-plugin*.

```groovy
plugins {
  id "dev.bodewig.autoserializable" version "2.0.1-SNAPSHOT"
}
```

### Custom serializers

To inject custom serializers/deserializers into a class, your class has to implement `AutoSerializer` and be annotated
with `@AutoSerializable`. The class has to be part of the `source` processed by byte-buddy or be available on the
classpath during plugin execution. The class has to be public.

---

Run `git config --add include.path ../.gitconfig` to include the template config in your project config.
