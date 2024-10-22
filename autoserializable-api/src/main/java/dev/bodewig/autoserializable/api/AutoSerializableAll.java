package dev.bodewig.autoserializable.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows adding multiple {@link AutoSerializable} annotations on a {@link AutoSerializer}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoSerializableAll {

    /**
     * The {@code AutoSerializable} annotations
     *
     * @return the {@code AutoSerializable} annotations
     */
    AutoSerializable[] value();
}
