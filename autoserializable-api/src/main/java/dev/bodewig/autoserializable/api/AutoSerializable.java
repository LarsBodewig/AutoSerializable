package dev.bodewig.autoserializable.api;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AutoSerializableAll.class)
public @interface AutoSerializable {

    Class<?> value();
}
