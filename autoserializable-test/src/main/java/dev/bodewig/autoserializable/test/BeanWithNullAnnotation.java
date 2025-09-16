package dev.bodewig.autoserializable.test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@SuppressWarnings("unused")
class BeanWithNullAnnotation {

    @Documented
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
    @interface Null {
    }

    @Null
    String myMethod() {
        return null;
    }

    @interface TypeAnnot {
    }

    @TypeAnnot
    static class AnnotedType {
    }
}
