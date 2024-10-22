package dev.bodewig.autoserializable.api;

import java.lang.annotation.*;

/**
 * Binds a local {@link AutoSerializer} to a specific library class.
 * <p>
 * AutoSerializers can be used to customize the serialization and deserialization methods injected in the {@code
 * AutoSerializablePlugin}.
 * <p>
 * AutoSerializer implementations have to implement the {@link AutoSerializer} interface and specify the
 * {@link AutoSerializable} annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AutoSerializableAll.class)
public @interface AutoSerializable {

    /**
     * The class to bind the annotated {@code AutoSerializer} to
     *
     * @return the class to bind the annotated {@code AutoSerializer} to
     */
    Class<?> value();
}
