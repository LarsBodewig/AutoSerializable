package dev.bodewig.autoserializable.api;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Repeatable(AutoSerializableAll.class)
public @interface AutoSerializable {

    Class<?> value();
}
