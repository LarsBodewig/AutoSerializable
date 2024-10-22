package dev.bodewig.autoserializable.api;

import java.lang.annotation.*;

/**
 * Marks types transformed by the {@code AutoSerializablePlugin}.
 * <p>
 * The plugin made one or more of the following changes to the type:
 * <ul>
 *     <li>adds a private field and methods to serialize and deserialize the type</li>
 *     <li>makes all private types and fields package-private</li>
 *     <li>implements the {@link java.io.Serializable} interface</li>
 * </ul>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoSerialized {
}
