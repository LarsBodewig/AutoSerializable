[![Available on Maven Central](https://img.shields.io/maven-central/v/dev.bodewig.autoserializable/autoserializable?label=Available%20on%20Maven%20Central)](https://central.sonatype.com/artifact/dev.bodewig.autoserializable/autoserializable)

# AutoSerializable

AutoSerializable is a [ByteBuddy](https://github.com/raphw/byte-buddy) plugin to mark all classes as [Serializable](https://docs.oracle.com/en/java/javase/19/docs/api/java.base/java/io/Serializable.html) during compilation.

This plugin allows you to serialize 3rd-party library classes without modifying the library code or writing custom serializers/deserializers for each class (which might not even be possible due to limitations in visiblity and modularity). You do however have to shade the library, as the changes are not applied to dependencies automatically.

## Usage

To use the plugin, you need to configure it with ByteBuddy. For example using the gradle plugin:

```groovy
plugins {
  id 'net.bytebuddy.byte-buddy-gradle-plugin' version "$byteBuddyVersion"
}

byteBuddy {
  transformation {
    plugin = dev.bodewig.autoserializable.AutoSerializablePlugin.class
  }
}

dependencies {
  compileOnly "dev.bodewig.autoserializable:autoserializable:$autoserializableVersion"
}
```

---

Run `git config --add include.path ../.gitconfig` to include the template config in your project config.
