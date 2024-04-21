# AutoSerializable

AutoSerializable is a [Lombok](https://github.com/projectlombok/lombok) fork with a single additional feature:
it marks all classes as [Serializable](https://docs.oracle.com/en/java/javase/19/docs/api/java.base/java/io/Serializable.html) during compilation.

This feature allows you to serialize 3rd-party library classes without modifying the library code or writing custom serializers/deserializers for each class (which might not even be possible due to limitations in visiblity and modularity). You do however have to compile the library yourself, as the changes are applied during compilation.

You can apply AutoSerializable the same way you [setup Lombok](https://projectlombok.org/setup/).

```xml
<groupId>dev.bodewig.autoserializable</groupId>
<artifactId>autoserializable</artifactId>
<version>1.18.32</version>
```

---

Run `git config --add include.path ../.gitconfig` to include the template config in your project config.
