[![Available on Maven Central](https://img.shields.io/maven-central/v/dev.bodewig.autoserializable/autoserializable?label=Available%20on%20Maven%20Central)](https://central.sonatype.com/namespace/dev.bodewig.autoserializable)

# AutoSerializable

AutoSerializable is a [ByteBuddy](https://github.com/raphw/byte-buddy) plugin to mark all classes as [Serializable](https://docs.oracle.com/en/java/javase/19/docs/api/java.base/java/io/Serializable.html) even after compilation.

This plugin allows you to serialize 3rd-party library classes without modifying the library code or writing custom
serializers/deserializers for each class (which might not even be possible due to limitations in visiblity and
modularity).

## Usage

To use the plugin, you need to configure it with ByteBuddy. ByteBuddy offers plugins for maven and gradle with different
mojos and tasks to transform class files or whole JARs.

For gradle, however, there is a *autoserializable-gradle-plugin*, that preconfigures the respective ByteBuddy tasks.

```groovy
plugins {
  id 'dev.bodewig.autoserializable.classes' version '2.0.1' 
  // extends the ByteBuddySimpleTask to serialize local project java classes
  id 'dev.bodewig.autoserializable.classes' version '2.0.1'
  // extends the ByteBuddyJarsTask to serialize dependencies of the autoSerializable configuration
}

dependencies {
  autoSerializable 'third.party.library' // (also includes transitive dependencies)
}
```

Both gradle plugins can be applied together.
You can find examples for maven and gradle in the *autoserializable-test* project.

### Custom serializers

To inject custom serializers/deserializers into a class, your class has to implement `AutoSerializer` and be annotated
with `@AutoSerializable`. The class has to be part of the `source` processed by byte-buddy or be available on the
classpath during plugin execution. The class has to be public.

The gradle plugin automatically supplies the compile classpath and local project java classes as classpath during plugin
execution to find custom serializers.

If you want to writer a custom serializer for a non-accessible library class or including a non-accessible library class
field, just place the serializer in a package with the same name as the library class.
All library classes and fields are made visible to their respective package during plugin execution automatically.

---

Run `git config --add include.path ../.gitconfig` to include the template config in your project config.
