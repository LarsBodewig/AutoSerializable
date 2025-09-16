package dev.bodewig.autoserializable.api;

import java.io.ObjectStreamField;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be used in combination with {@link AutoSerializable} to define the {@link ObjectStreamField}{@code []
 * serialPersistenFields} in the AutoSerialized class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SerialPersistentFields {

    /**
     * The name used for the {@code ObjectStreamField[]} in the AutoSerialized class
     */
    String FIELD_NAME = "serialPersistentFields";

    /**
     * The fields to include in {@code serialPersistentFields}
     *
     * @return the fields to include in {@code serialPersistentFields}
     */
    String[] value();
}
